package com.university.website.service;

import com.university.website.dto.ReportCreateDto;
import com.university.website.dto.ReportResolutionDto;
import com.university.website.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    Report createReport(Long noteId, Long reporterId, ReportCreateDto dto);

    Page<Report> findPendingReports(Pageable pageable);

    void resolveReport(Long reportId, Long moderatorId, ReportResolutionDto dto, boolean hideNote);

    void dismissReport(Long reportId, Long moderatorId, ReportResolutionDto dto);

    long countPendingReports();
}
