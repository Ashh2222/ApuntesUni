package com.university.website.repository;

import com.university.website.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Favorite> findByUser_IdAndNote_Id(Long userId, Long noteId);

    boolean existsByUser_IdAndNote_Id(Long userId, Long noteId);

    void deleteByUser_IdAndNote_Id(Long userId, Long noteId);

    long countByUser_Id(Long userId);
}
