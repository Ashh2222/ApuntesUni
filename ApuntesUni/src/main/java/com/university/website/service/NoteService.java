package com.university.website.service;

import com.university.website.dto.NoteFilterDto;
import com.university.website.dto.NoteUploadDto;
import com.university.website.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {

    Note uploadNote(NoteUploadDto dto, Long uploaderId);

    Note findById(Long id);

    Note findByIdWithIncrementViews(Long id);

    Page<Note> searchNotes(NoteFilterDto filterDto);

    Page<Note> findUserNotes(Long uploaderId, Pageable pageable);

    Page<Note> findPendingNotes(Pageable pageable);

    Note updateNote(Long noteId, Long currentUserId, NoteUploadDto dto);

    void deleteNote(Long noteId, Long currentUserId);

    void approveNote(Long noteId);

    void rejectNote(Long noteId);

    List<Note> findRecentNotes(int limit);

    List<Note> findPopularNotes(int limit);
}
