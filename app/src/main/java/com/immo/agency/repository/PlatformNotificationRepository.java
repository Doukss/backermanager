package com.immo.agency.repository;

import com.immo.agency.entity.PlatformNotification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformNotificationRepository extends JpaRepository<PlatformNotification, UUID> {
    List<PlatformNotification> findTop20ByOrderByCreatedAtDesc();
}
