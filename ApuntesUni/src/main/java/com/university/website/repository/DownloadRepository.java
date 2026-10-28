package com.university.website.repository;

import com.university.website.model.Download;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    long countByNote_Id(Long noteId);

    Page<Download> findByUser_IdOrderByDownloadDateDesc(Long userId, Pageable pageable);

    long countByUser_Id(Long userId);
}
