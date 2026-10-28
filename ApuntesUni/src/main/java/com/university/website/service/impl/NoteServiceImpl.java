package com.university.website.service.impl;

import com.university.website.dto.NoteFilterDto;
import com.university.website.dto.NoteUploadDto;
import com.university.website.exception.BadRequestException;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.exception.UnauthorizedActionException;
import com.university.website.model.Note;
import com.university.website.model.Subject;
import com.university.website.model.Topic;
import com.university.website.model.User;
import com.university.website.model.enums.FileFormat;
import com.university.website.model.enums.NoteStatus;
import com.university.website.model.enums.UserRole;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.SubjectRepository;
import com.university.website.repository.TopicRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.NoteService;
import com.university.website.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final StorageService storageService;

    public NoteServiceImpl(
            NoteRepository noteRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            StorageService storageService) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.storageService = storageService;
    }

    @Override
    public Note uploadNote(NoteUploadDto dto, Long uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + uploaderId));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + dto.getSubjectId()));

        Topic topic = null;
        if (dto.getTopicId() != null) {
            topic = topicRepository.findById(dto.getTopicId()).orElse(null);
        }

        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            throw new BadRequestException("Debes adjuntar un archivo para el apunte.");
        }

        // Almacenar archivo físicamente
        String storagePath = storageService.store(dto.getFile());
        long sizeBytes = dto.getFile().getSize();
        String originalFileName = dto.getFile().getOriginalFilename();
        String mimeType = dto.getFile().getContentType();
        FileFormat format = detectFormat(originalFileName);

        Note note = new Note();
        note.setTitle(dto.getTitle().trim());
        note.setDescription(dto.getDescription());
        note.setOriginalFileName(originalFileName);
        note.setStoragePath(storagePath);
        note.setFileSizeBytes(sizeBytes);
        note.setMimeType(mimeType != null ? mimeType : "application/octet-stream");
        note.setFormat(format);
        note.setPageCount(1); // Estimación básica
        note.setStatus(NoteStatus.PUBLISHED); // Publicación directa (apuntes colaborativos)
        note.setUploadDate(LocalDateTime.now());
        note.setUploader(uploader);
        note.setSubject(subject);
        note.setTopic(topic);
        note.setTags(dto.getTags());

        Note saved = noteRepository.save(note);
        log.info("Apunte '{}' creado exitosamente por usuario {}", saved.getTitle(), uploader.getEmail());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Note findById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado con ID: " + id));
    }

    @Override
    public Note findByIdWithIncrementViews(Long id) {
        Note note = findById(id);
        note.incrementViews();
        return noteRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Note> searchNotes(NoteFilterDto filterDto) {
        Sort sort = switch (filterDto.getSortBy() != null ? filterDto.getSortBy() : "recent") {
            case "views" -> Sort.by(Sort.Direction.DESC, "viewsCount");
            case "downloads" -> Sort.by(Sort.Direction.DESC, "viewsCount"); // proxy de popularidad
            default -> Sort.by(Sort.Direction.DESC, "uploadDate");
        };

        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize(), sort);
        String query = filterDto.getQuery() != null && !filterDto.getQuery().isBlank() ? filterDto.getQuery().trim() : null;

        return noteRepository.searchWithFilters(
                NoteStatus.PUBLISHED,
                filterDto.getProgramId(),
                filterDto.getSubjectId(),
                filterDto.getTopicId(),
                filterDto.getFormat(),
                query,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Note> findUserNotes(Long uploaderId, Pageable pageable) {
        return noteRepository.findByUploader_Id(uploaderId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Note> findPendingNotes(Pageable pageable) {
        return noteRepository.findByStatus(NoteStatus.PENDING_APPROVAL, pageable);
    }

    @Override
    public Note updateNote(Long noteId, Long currentUserId, NoteUploadDto dto) {
        Note note = findById(noteId);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + currentUserId));

        // Permisos: autor o moderador/admin
        if (!note.getUploader().getId().equals(currentUserId) &&
                !currentUser.isAdmin() && !currentUser.isModerator()) {
            throw new UnauthorizedActionException("No tienes permiso para modificar este apunte.");
        }

        Topic topic = null;
        if (dto.getTopicId() != null) {
            topic = topicRepository.findById(dto.getTopicId()).orElse(null);
        }

        note.updateMetadata(dto.getTitle(), dto.getDescription(), topic);
        note.setTags(dto.getTags());

        // Si sube un nuevo archivo en la edición
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            storageService.delete(note.getStoragePath());
            String newStoragePath = storageService.store(dto.getFile());
            note.setStoragePath(newStoragePath);
            note.setOriginalFileName(dto.getFile().getOriginalFilename());
            note.setFileSizeBytes(dto.getFile().getSize());
            note.setMimeType(dto.getFile().getContentType());
            note.setFormat(detectFormat(dto.getFile().getOriginalFilename()));
        }

        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(Long noteId, Long currentUserId) {
        Note note = findById(noteId);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + currentUserId));

        if (!note.getUploader().getId().equals(currentUserId) &&
                !currentUser.isAdmin() && !currentUser.isModerator()) {
            throw new UnauthorizedActionException("No tienes permiso para eliminar este apunte.");
        }

        // Eliminar archivo físico
        storageService.delete(note.getStoragePath());

        // Eliminar de base de datos
        noteRepository.delete(note);
        log.info("Apunte {} eliminado por usuario {}", noteId, currentUser.getEmail());
    }

    @Override
    public void approveNote(Long noteId) {
        Note note = findById(noteId);
        note.approve();
        noteRepository.save(note);
        log.info("Apunte {} aprobado por moderación", noteId);
    }

    @Override
    public void rejectNote(Long noteId) {
        Note note = findById(noteId);
        note.reject();
        noteRepository.save(note);
        log.info("Apunte {} rechazado por moderación", noteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> findRecentNotes(int limit) {
        return noteRepository.findTop6Recent(NoteStatus.PUBLISHED, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> findPopularNotes(int limit) {
        return noteRepository.findTopPopular(NoteStatus.PUBLISHED, PageRequest.of(0, limit));
    }

    private FileFormat detectFormat(String filename) {
        if (filename == null) return FileFormat.PDF;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return FileFormat.PDF;
        if (lower.endsWith(".docx")) return FileFormat.DOCX;
        if (lower.endsWith(".pptx")) return FileFormat.PPTX;
        if (lower.endsWith(".png")) return FileFormat.PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return FileFormat.JPG;
        return FileFormat.PDF;
    }
}
