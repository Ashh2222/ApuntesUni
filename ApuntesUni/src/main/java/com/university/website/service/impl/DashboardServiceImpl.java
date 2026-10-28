package com.university.website.service.impl;

import com.university.website.dto.DashboardDto;
import com.university.website.dto.SubjectSummaryDto;
import com.university.website.model.Note;
import com.university.website.model.Student;
import com.university.website.model.StudentProgram;
import com.university.website.model.Subject;
import com.university.website.model.User;
import com.university.website.model.enums.EnrollmentStatus;
import com.university.website.model.enums.NoteStatus;
import com.university.website.repository.DownloadRepository;
import com.university.website.repository.EnrollmentRepository;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.StudentProgramRepository;
import com.university.website.repository.StudentRepository;
import com.university.website.repository.SubjectRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentProgramRepository studentProgramRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DownloadRepository downloadRepository;
    private final NoteRepository noteRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public DashboardDto getDashboardData(Long userId) {
        String studentName = "Estudiante";
        String programName = "Ingeniería";
        Integer semester = 1;
        long enrolledSubjectsCount = 0;
        long downloadsCount = 0;
        long uploadsCount = 0;

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                studentName = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail();
                uploadsCount = noteRepository.countByUploader_Id(userId);
                downloadsCount = downloadRepository.countByUser_Id(userId);

                Student student = studentRepository.findById(userId).orElse(null);
                if (student != null) {
                    var activeEnrollments = enrollmentRepository.findByStudent_IdAndStatus(userId, EnrollmentStatus.ACTIVE);
                    enrolledSubjectsCount = activeEnrollments.size();

                    var primaryProgramOpt = studentProgramRepository.findByStudent_IdAndPrimaryProgramTrue(userId);
                    if (primaryProgramOpt.isPresent()) {
                        StudentProgram sp = primaryProgramOpt.get();
                        programName = sp.getAcademicProgram().getName();
                        semester = sp.getCurrentSemester();
                    } else {
                        var programs = studentProgramRepository.findByStudent_Id(userId);
                        if (!programs.isEmpty()) {
                            StudentProgram sp = programs.get(0);
                            programName = sp.getAcademicProgram().getName();
                            semester = sp.getCurrentSemester();
                        }
                    }
                }
            }
        }

        // Obtener lista de materias activas del catálogo con su conteo de notas
        List<Subject> subjects = subjectRepository.findAllByActiveTrueOrderByNameAsc();
        List<SubjectSummaryDto> catalogSubjects = new ArrayList<>();
        for (Subject s : subjects) {
            long notesCount = s.getNotes() != null ? s.getNotes().stream().filter(Note::isPublished).count() : 0;
            catalogSubjects.add(new SubjectSummaryDto(s.getId(), s.getCode(), s.getName(), s.getCredits(), notesCount));
        }

        // Obtener apuntes recientes y populares
        List<Note> recentNotes = noteRepository.findTop6Recent(NoteStatus.PUBLISHED, PageRequest.of(0, 6));
        List<Note> popularNotes = noteRepository.findTopPopular(NoteStatus.PUBLISHED, PageRequest.of(0, 6));

        return new DashboardDto(
                studentName,
                programName,
                semester,
                enrolledSubjectsCount,
                downloadsCount,
                uploadsCount,
                catalogSubjects,
                recentNotes,
                popularNotes
        );
    }
}
