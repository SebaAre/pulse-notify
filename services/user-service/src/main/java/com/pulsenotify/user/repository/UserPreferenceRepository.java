package com.pulsenotify.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.user.model.UserPreference;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    Optional<UserPreference> findByUserIdAndChannel (UUID userId, NotificationChannel channel);

}