package com.university.website.service.impl;

import com.university.website.dto.ReportCreateDto;
import com.university.website.dto.ReportResolutionDto;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.Note;
import com.university.website.model.Report;
import com.university.website.model.User;
import com.university.website.model.enums.ReportStatus;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.ReportRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(
            ReportRepository reportRepository,
            NoteRepository noteRepository,
            UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Report createReport(Long noteId, Long reporterId, ReportCreateDto dto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado: " + noteId));
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + reporterId));

        Report report = new Report(reporter, note, dto.getReason(), dto.getSeverity(), dto.getDescription().trim());
        return reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findPendingReports(Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING, pageable);
    }

    @Override
    public void resolveReport(Long reportId, Long moderatorId, ReportResolutionDto dto, boolean hideNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado: " + reportId));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderador no encontrado: " + moderatorId));

        report.resolve(dto.getResolutionNotes(), moderator);
        reportRepository.save(report);

        if (hideNote) {
            Note note = report.getNote();
            note.hide();
            noteRepository.save(note);
        }
    }

    @Override
    public void dismissReport(Long reportId, Long moderatorId, ReportResolutionDto dto) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado: " + reportId));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderador no encontrado: " + moderatorId));

        report.dismiss(dto.getResolutionNotes(), moderator);
        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReports() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }
}
