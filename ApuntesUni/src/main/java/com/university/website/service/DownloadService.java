package com.university.website.service;

import com.university.website.model.Download;
import com.university.website.model.Note;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DownloadService {

    record DownloadResult(Resource resource, Note note, String filename, String contentType) {}

    DownloadResult downloadNote(Long noteId, Long userId, String ipAddress, String userAgent);

    Page<Download> getUserDownloads(Long userId, Pageable pageable);

    long getUserDownloadCount(Long userId);
}
