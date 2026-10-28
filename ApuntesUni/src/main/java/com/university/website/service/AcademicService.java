package com.university.website.service;

import com.university.website.dto.AcademicProgramDto;
import com.university.website.dto.SubjectDto;
import com.university.website.dto.TopicDto;
import com.university.website.model.AcademicProgram;
import com.university.website.model.Subject;
import com.university.website.model.Topic;

import java.util.List;

public interface AcademicService {

    // Programas Académicos
    List<AcademicProgram> findAllPrograms();

    AcademicProgram findProgramById(Long id);

    AcademicProgram saveProgram(AcademicProgramDto dto);

    void deleteProgram(Long id);

    // Materias
    List<Subject> findAllSubjects();

    List<Subject> findSubjectsByProgram(Long programId);

    Subject findSubjectById(Long id);

    Subject saveSubject(SubjectDto dto);

    void deleteSubject(Long id);

    // Temas
    List<Topic> findTopicsBySubject(Long subjectId);

    Topic findTopicById(Long id);

    Topic saveTopic(TopicDto dto);

    void deleteTopic(Long id);
}
