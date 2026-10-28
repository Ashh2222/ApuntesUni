package com.university.website.service.impl;

import com.university.website.dto.CommentCreateDto;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.exception.UnauthorizedActionException;
import com.university.website.model.Comment;
import com.university.website.model.Note;
import com.university.website.model.User;
import com.university.website.repository.CommentRepository;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            NoteRepository noteRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Comment addComment(Long noteId, Long userId, CommentCreateDto dto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado: " + noteId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        Comment comment = new Comment(user, note, dto.getContent().trim());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado: " + commentId));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + currentUserId));

        if (!comment.getUser().getId().equals(currentUserId) &&
                !currentUser.isAdmin() && !currentUser.isModerator()) {
            throw new UnauthorizedActionException("No tienes permiso para eliminar este comentario.");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findActiveCommentsByNote(Long noteId) {
        return commentRepository.findByNote_IdAndDeletedFalseOrderByCreatedAtDesc(noteId);
    }
}
