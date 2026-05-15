package com.pulsenotify.notification.repository;


import java.util.UUID;
import java.util.List;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.pulsenotify.notification.model.Notification;
import com.pulsenotify.notification.model.NotificationStatus;


public interface NotificationRepository extends JpaRepository<Notification, UUID>{


    List<Notification> findByRecipient(String recipient);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByCreatedAtAfter(Instant createdAt);

    @Query("SELECT n.status AS status, COUNT(n) AS count FROM Notification n GROUP BY n.status")
    List<StatusCount> countGroupedByStatus();

    @Query("SELECT n.channel AS channel, COUNT(n) AS count FROM Notification n GROUP BY n.channel")
    List<ChannelCount> countGroupedByChannel();

}
