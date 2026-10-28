package com.university.website.repository;

import com.university.website.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUser_IdAndNote_Id(Long userId, Long noteId);

    boolean existsByUser_IdAndNote_Id(Long userId, Long noteId);

    @Query("SELECT COALESCE(AVG(r.value), 0.0) FROM Rating r WHERE r.note.id = :noteId")
    Double findAverageRatingByNoteId(@Param("noteId") Long noteId);

    long countByNote_Id(Long noteId);
}
