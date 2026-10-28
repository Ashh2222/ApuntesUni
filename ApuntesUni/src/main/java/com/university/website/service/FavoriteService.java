package com.university.website.service;

import com.university.website.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {

    boolean toggleFavorite(Long noteId, Long userId);

    boolean isFavorite(Long noteId, Long userId);

    Page<Favorite> findUserFavorites(Long userId, Pageable pageable);
}
