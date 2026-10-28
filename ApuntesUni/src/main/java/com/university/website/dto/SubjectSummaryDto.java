package com.university.website.dto;

public record SubjectSummaryDto(
        Long id,
        String code,
        String name,
        int credits,
        long notesCount
) {}
