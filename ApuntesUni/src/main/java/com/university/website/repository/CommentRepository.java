package com.university.website.repository;

import com.university.website.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByNote_IdOrderByCreatedAtDesc(Long noteId);

    List<Comment> findByNote_IdAndDeletedFalseOrderByCreatedAtDesc(Long noteId);

    long countByNote_IdAndDeletedFalse(Long noteId);
}
