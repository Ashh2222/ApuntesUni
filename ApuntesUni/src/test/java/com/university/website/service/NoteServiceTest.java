package com.university.website.service;

import com.university.website.dto.NoteUploadDto;
import com.university.website.model.*;
import com.university.website.model.enums.FileFormat;
import com.university.website.model.enums.NoteStatus;
import com.university.website.model.enums.UserRole;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.SubjectRepository;
import com.university.website.repository.TopicRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Student uploader;
    private Subject subject;
    private Topic topic;

    @BeforeEach
    void setUp() {
        uploader = new Student();
        uploader.setId(1L);
        uploader.setFullName("Ana Romero");
        uploader.setEmail("ana@uni.edu.pe");
        uploader.setUserRole(UserRole.STUDENT);

        subject = new Subject();
        subject.setId(10L);
        subject.setName("Cálculo I");
        subject.setCode("MA101");

        topic = new Topic();
        topic.setId(100L);
        topic.setName("Integral definida");
        topic.setSubject(subject);
    }

    @Test
    @DisplayName("Debe subir un apunte y guardar archivo físico y metadatos")
    void testUploadNoteSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resumen.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        NoteUploadDto dto = new NoteUploadDto();
        dto.setTitle("Resumen de Cálculo");
        dto.setDescription("Ejercicios resueltos paso a paso");
        dto.setSubjectId(10L);
        dto.setTopicId(100L);
        dto.setFile(file);

        when(userRepository.findById(1L)).thenReturn(Optional.of(uploader));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
        when(topicRepository.findById(100L)).thenReturn(Optional.of(topic));
        when(storageService.store(any())).thenReturn("unique-storage-key.pdf");
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note n = invocation.getArgument(0);
            n.setId(50L);
            return n;
        });

        Note result = noteService.uploadNote(dto, 1L);

        assertNotNull(result);
        assertEquals("Resumen de Cálculo", result.getTitle());
        assertEquals("unique-storage-key.pdf", result.getStoragePath());
        assertEquals(NoteStatus.PUBLISHED, result.getStatus());
        verify(storageService).store(file);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Debe incrementar el contador de vistas al consultar el detalle")
    void testFindByIdWithIncrementViews() {
        Note note = new Note();
        note.setId(50L);
        note.setTitle("Apuntes");
        note.setViewsCount(5);

        when(noteRepository.findById(50L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));

        Note result = noteService.findByIdWithIncrementViews(50L);

        assertEquals(6, result.getViewsCount());
        verify(noteRepository).save(note);
    }

    @Test
    @DisplayName("Debe eliminar apunte y su archivo físico si el usuario es el autor")
    void testDeleteNoteByAuthor() {
        Note note = new Note();
        note.setId(50L);
        note.setStoragePath("file-to-delete.pdf");
        note.setUploader(uploader);

        when(noteRepository.findById(50L)).thenReturn(Optional.of(note));
        when(userRepository.findById(1L)).thenReturn(Optional.of(uploader));

        noteService.deleteNote(50L, 1L);

        verify(storageService).delete("file-to-delete.pdf");
        verify(noteRepository).delete(note);
    }
}
