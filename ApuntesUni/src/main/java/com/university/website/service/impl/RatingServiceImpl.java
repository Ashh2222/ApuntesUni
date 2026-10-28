package com.university.website.service.impl;

import com.university.website.dto.RatingDto;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.Note;
import com.university.website.model.Rating;
import com.university.website.model.Student;
import com.university.website.model.User;
import com.university.website.repository.NoteRepository;
import com.university.website.repository.RatingRepository;
import com.university.website.repository.StudentRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public RatingServiceImpl(
            RatingRepository ratingRepository,
            NoteRepository noteRepository,
            UserRepository userRepository,
            StudentRepository studentRepository) {
        this.ratingRepository = ratingRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public Rating rateNote(Long noteId, Long userId, RatingDto dto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado: " + noteId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        Optional<Rating> existing = ratingRepository.findByUser_IdAndNote_Id(userId, noteId);
        Rating rating;
        if (existing.isPresent()) {
            rating = existing.get();
            rating.updateValue(dto.getValue(), dto.getFeedbackNote());
        } else {
            rating = new Rating(user, note, dto.getValue(), dto.getFeedbackNote());

            // Si el autor es estudiante y recibe buena puntuación (>= 4), sumamos puntos de reputación
            if (dto.getValue() >= 4 && note.getUploader() instanceof Student studentUploader) {
                studentUploader.addReputation(dto.getValue());
                studentRepository.save(studentUploader);
            }
        }

        return ratingRepository.save(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rating> findUserRating(Long noteId, Long userId) {
        return ratingRepository.findByUser_IdAndNote_Id(userId, noteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long noteId) {
        return ratingRepository.findAverageRatingByNoteId(noteId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRatings(Long noteId) {
        return ratingRepository.countByNote_Id(noteId);
    }
}
