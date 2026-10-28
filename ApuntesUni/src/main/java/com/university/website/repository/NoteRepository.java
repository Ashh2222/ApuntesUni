package com.university.website.repository;

import com.university.website.model.Note;
import com.university.website.model.enums.FileFormat;
import com.university.website.model.enums.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    Page<Note> findByStatus(NoteStatus status, Pageable pageable);

    Page<Note> findByUploader_Id(Long uploaderId, Pageable pageable);

    Page<Note> findByUploader_IdAndStatus(Long uploaderId, NoteStatus status, Pageable pageable);

    long countByStatus(NoteStatus status);

    long countByUploader_Id(Long uploaderId);

    @Query("SELECT n FROM Note n WHERE n.status = :status ORDER BY n.uploadDate DESC")
    List<Note> findTop6Recent(@Param("status") NoteStatus status, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.status = :status ORDER BY n.viewsCount DESC")
    List<Note> findTopPopular(@Param("status") NoteStatus status, Pageable pageable);

    @Query("""
        SELECT DISTINCT n FROM Note n
        LEFT JOIN n.subject s
        LEFT JOIN s.academicPrograms p
        LEFT JOIN n.topic t
        WHERE (:status IS NULL OR n.status = :status)
          AND (:programId IS NULL OR p.id = :programId)
          AND (:subjectId IS NULL OR s.id = :subjectId)
          AND (:topicId IS NULL OR t.id = :topicId)
          AND (:format IS NULL OR n.format = :format)
          AND (:query IS NULL OR (
                LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(n.description) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(n.tags) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
          ))
    """)
    Page<Note> searchWithFilters(
            @Param("status") NoteStatus status,
            @Param("programId") Long programId,
            @Param("subjectId") Long subjectId,
            @Param("topicId") Long topicId,
            @Param("format") FileFormat format,
            @Param("query") String query,
            Pageable pageable
    );
}
