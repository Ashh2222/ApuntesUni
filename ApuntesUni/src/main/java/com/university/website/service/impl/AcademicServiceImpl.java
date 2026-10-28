package com.university.website.service.impl;

import com.university.website.dto.AcademicProgramDto;
import com.university.website.dto.SubjectDto;
import com.university.website.dto.TopicDto;
import com.university.website.exception.BadRequestException;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.AcademicProgram;
import com.university.website.model.Subject;
import com.university.website.model.Topic;
import com.university.website.repository.AcademicProgramRepository;
import com.university.website.repository.SubjectRepository;
import com.university.website.repository.TopicRepository;
import com.university.website.service.AcademicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AcademicServiceImpl implements AcademicService {

    private final AcademicProgramRepository programRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public AcademicServiceImpl(
            AcademicProgramRepository programRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {
        this.programRepository = programRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicProgram> findAllPrograms() {
        return programRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicProgram findProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa académico no encontrado: " + id));
    }

    @Override
    public AcademicProgram saveProgram(AcademicProgramDto dto) {
        AcademicProgram program;
        if (dto.getId() != null) {
            program = findProgramById(dto.getId());
        } else {
            if (programRepository.existsByCodeIgnoreCase(dto.getCode())) {
                throw new BadRequestException("Ya existe un programa con el código: " + dto.getCode());
            }
            program = new AcademicProgram();
        }

        program.setCode(dto.getCode().trim().toUpperCase());
        program.setName(dto.getName().trim());
        program.setDescription(dto.getDescription());
        program.setFaculty(dto.getFaculty());
        program.setTotalSemesters(dto.getTotalSemesters());
        program.setType(dto.getType());
        program.setActive(dto.isActive());

        if (dto.getRelatedProgramIds() != null && !dto.getRelatedProgramIds().isEmpty()) {
            List<AcademicProgram> related = programRepository.findAllById(dto.getRelatedProgramIds());
            program.getRelatedPrograms().clear();
            program.getRelatedPrograms().addAll(related);
        }

        return programRepository.save(program);
    }

    @Override
    public void deleteProgram(Long id) {
        AcademicProgram program = findProgramById(id);
        program.setActive(false);
        programRepository.save(program);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> findAllSubjects() {
        return subjectRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> findSubjectsByProgram(Long programId) {
        return subjectRepository.findByAcademicProgramId(programId);
    }

    @Override
    @Transactional(readOnly = true)
    public Subject findSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + id));
    }

    @Override
    public Subject saveSubject(SubjectDto dto) {
        Subject subject;
        if (dto.getId() != null) {
            subject = findSubjectById(dto.getId());
        } else {
            if (subjectRepository.existsByCodeIgnoreCase(dto.getCode())) {
                throw new BadRequestException("Ya existe una materia con el código: " + dto.getCode());
            }
            subject = new Subject();
        }

        subject.setCode(dto.getCode().trim().toUpperCase());
        subject.setName(dto.getName().trim());
        subject.setDescription(dto.getDescription());
        subject.setCredits(dto.getCredits());
        subject.setActive(dto.isActive());

        // Asignar carreras que la cursan
        if (dto.getAcademicProgramIds() != null) {
            List<AcademicProgram> programs = programRepository.findAllById(dto.getAcademicProgramIds());
            subject.getAcademicPrograms().clear();
            subject.getAcademicPrograms().addAll(programs);
        }

        // Asignar prerrequisitos
        if (dto.getPrerequisiteIds() != null) {
            List<Subject> prereqs = subjectRepository.findAllById(dto.getPrerequisiteIds());
            subject.getPrerequisiteSubjects().clear();
            subject.getPrerequisiteSubjects().addAll(prereqs);
        }

        return subjectRepository.save(subject);
    }

    @Override
    public void deleteSubject(Long id) {
        Subject subject = findSubjectById(id);
        subject.setActive(false);
        subjectRepository.save(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> findTopicsBySubject(Long subjectId) {
        return topicRepository.findBySubject_IdOrderByOrderIndexAsc(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Topic findTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado: " + id));
    }

    @Override
    public Topic saveTopic(TopicDto dto) {
        Topic topic;
        if (dto.getId() != null) {
            topic = findTopicById(dto.getId());
        } else {
            topic = new Topic();
        }

        Subject subject = findSubjectById(dto.getSubjectId());
        topic.setCode(dto.getCode());
        topic.setName(dto.getName().trim());
        topic.setDescription(dto.getDescription());
        topic.setOrderIndex(dto.getOrderIndex());
        topic.setLearningObjective(dto.getLearningObjective());
        topic.setSubject(subject);

        return topicRepository.save(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        Topic topic = findTopicById(id);
        topicRepository.delete(topic);
    }
}
