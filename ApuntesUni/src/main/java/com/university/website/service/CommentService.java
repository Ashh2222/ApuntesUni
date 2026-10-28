package com.university.website.service;

import com.university.website.dto.CommentCreateDto;
import com.university.website.model.Comment;

import java.util.List;

public interface CommentService {

    Comment addComment(Long noteId, Long userId, CommentCreateDto dto);

    void deleteComment(Long commentId, Long currentUserId);

    List<Comment> findActiveCommentsByNote(Long noteId);
}
