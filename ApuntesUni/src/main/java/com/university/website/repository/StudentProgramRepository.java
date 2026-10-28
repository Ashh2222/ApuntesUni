package com.university.website.repository;

import com.university.website.model.StudentProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProgramRepository extends JpaRepository<StudentProgram, Long> {

    List<StudentProgram> findByStudent_Id(Long studentId);

    Optional<StudentProgram> findByStudent_IdAndPrimaryProgramTrue(Long studentId);

    boolean existsByStudent_IdAndAcademicProgram_Id(Long studentId, Long academicProgramId);
}
