package com.university.website.repository;

import com.university.website.model.RecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long> {

    Optional<RecoveryToken> findByToken(String token);

    void deleteByUser_Id(Long userId);
}
