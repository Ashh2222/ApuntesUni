package com.university.website.service;

import com.university.website.model.Note;
import com.university.website.model.Notification;
import com.university.website.model.NotificationSettings;
import com.university.website.model.User;
import com.university.website.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Notification sendNotification(User user, NotificationType type, String title, String message, Long referenceId, String referenceType, Note relatedNote);

    Page<Notification> getUserNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    NotificationSettings getSettings(Long userId);

    NotificationSettings updateSettings(Long userId, boolean email, boolean comment, boolean rating, boolean report, boolean subject);
}
