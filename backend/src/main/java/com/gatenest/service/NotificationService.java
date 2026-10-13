package com.gatenest.service;

import com.gatenest.model.Notification;
import com.gatenest.model.NotificationType;
import com.gatenest.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(Long userId, String title, String message, String linkUrl, NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLinkUrl(linkUrl);
        notification.setNotificationType(type);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId, int limit) {
        return notificationRepository.findByUserId(userId, limit);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
