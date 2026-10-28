package com.university.website.service;

import com.university.website.dto.RatingDto;
import com.university.website.model.Rating;

import java.util.Optional;

public interface RatingService {

    Rating rateNote(Long noteId, Long userId, RatingDto dto);

    Optional<Rating> findUserRating(Long noteId, Long userId);

    Double getAverageRating(Long noteId);

    long countRatings(Long noteId);
}
