package com.university.website.service.impl;

import com.university.website.exception.ResourceNotFoundException;
import com.university.website.exception.UnauthorizedActionException;
import com.university.website.model.Note;
import com.university.website.model.Notification;
import com.university.website.model.NotificationSettings;
import com.university.website.model.User;
import com.university.website.model.enums.NotificationType;
import com.university.website.repository.NotificationRepository;
import com.university.website.repository.NotificationSettingsRepository;
import com.university.website.repository.UserRepository;
import com.university.website.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Override
    public Notification sendNotification(User user, NotificationType type, String title, String message, Long referenceId, String referenceType, Note relatedNote) {
        // Consultar configuraciones del usuario para verificar si tiene activada la notificación
        NotificationSettings settings = settingsRepository.findByUser_Id(user.getId())
                .orElseGet(() -> settingsRepository.save(new NotificationSettings(user)));

        boolean shouldSend = switch (type) {
            case COMMENT_RECEIVED -> settings.isCommentAlerts();
            case RATING_RECEIVED -> settings.isRatingAlerts();
            case REPORT_UPDATE -> settings.isReportAlerts();
            default -> true;
        };

        if (!shouldSend) {
            log.debug("Notificación descartada para usuario {} por configuración de preferencias", user.getId());
            return null;
        }

        Notification notification = new Notification(user, type, title, message, referenceId, referenceType);
        notification.setRelatedNote(relatedNote);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("No tienes permiso para modificar esta notificación");
        }

        notification.markAsRead();
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        list.forEach(Notification::markAsRead);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSettings getSettings(Long userId) {
        return settingsRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    NotificationSettings ns = new NotificationSettings();
                    ns.enableAll();
                    return ns;
                });
    }

    @Override
    public NotificationSettings updateSettings(Long userId, boolean email, boolean comment, boolean rating, boolean report, boolean subject) {
        NotificationSettings settings = settingsRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
                    return new NotificationSettings(user);
                });

        settings.updatePreferences(email, comment, rating, report, subject);
        return settingsRepository.save(settings);
    }
}
