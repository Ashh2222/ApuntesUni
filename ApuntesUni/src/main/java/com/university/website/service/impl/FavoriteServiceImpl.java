package com.university.website.service.impl;

import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.Favorite;
import com.university.website.model.Note;
import com.university.website.model.User;
import com.university.website.repository.FavoriteRepository;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public FavoriteServiceImpl(
            FavoriteRepository favoriteRepository,
            NoteRepository noteRepository,
            UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean toggleFavorite(Long noteId, Long userId) {
        Optional<Favorite> existing = favoriteRepository.findByUser_IdAndNote_Id(userId, noteId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        } else {
            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado: " + noteId));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

            Favorite favorite = new Favorite(user, note);
            favoriteRepository.save(favorite);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long noteId, Long userId) {
        return favoriteRepository.existsByUser_IdAndNote_Id(userId, noteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Favorite> findUserFavorites(Long userId, Pageable pageable) {
        return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }
}
