package com.university.website.repository;

import com.university.website.model.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    Optional<NotificationSettings> findByUser_Id(Long userId);
}
