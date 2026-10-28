package com.university.website.repository;

import com.university.website.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Subject> findAllByActiveTrueOrderByNameAsc();

    @Query("SELECT DISTINCT s FROM Subject s JOIN s.academicPrograms p WHERE p.id = :programId AND s.active = true ORDER BY s.name ASC")
    List<Subject> findByAcademicProgramId(@Param("programId") Long programId);

    @Query("SELECT s FROM Subject s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Subject> searchByNameOrCode(@Param("query") String query);
}
