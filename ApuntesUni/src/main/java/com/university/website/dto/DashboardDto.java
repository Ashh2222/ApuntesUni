package com.university.website.dto;

import com.university.website.model.Note;

import java.util.List;

public record DashboardDto(
        String studentName,
        String programName,
        Integer semester,
        long enrolledSubjectsCount,
        long downloadsCount,
        long uploadsCount,
        List<SubjectSummaryDto> catalogSubjects,
        List<Note> recentNotes,
        List<Note> popularNotes
) {}
