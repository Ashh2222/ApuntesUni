package com.university.website.service.impl;

import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.Download;
import com.university.website.model.Note;
import com.university.website.model.User;
import com.university.website.repository.DownloadRepository;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.DownloadService;
import com.university.website.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DownloadServiceImpl implements DownloadService {

    private final DownloadRepository downloadRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
    public DownloadResult downloadNote(Long noteId, Long userId, String ipAddress, String userAgent) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado con ID: " + noteId));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // Si el usuario existe, registrar la descarga
        if (user != null) {
            Download download = new Download(user, note, ipAddress, userAgent);
            downloadRepository.save(download);
        }

        // Cargar archivo físico
        Resource resource = storageService.loadAsResource(note.getStoragePath());
        String contentType = note.getMimeType() != null ? note.getMimeType() : "application/octet-stream";
        String filename = note.getOriginalFileName() != null ? note.getOriginalFileName() : "apunte_" + noteId;

        return new DownloadResult(resource, note, filename, contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Download> getUserDownloads(Long userId, Pageable pageable) {
        return downloadRepository.findByUser_IdOrderByDownloadDateDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserDownloadCount(Long userId) {
        return downloadRepository.countByUser_Id(userId);
    }
}
