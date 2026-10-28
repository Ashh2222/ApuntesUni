package com.university.website.repository;

import com.university.website.model.AcademicProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicProgramRepository extends JpaRepository<AcademicProgram, Long> {

    Optional<AcademicProgram> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<AcademicProgram> findAllByActiveTrueOrderByNameAsc();
}
