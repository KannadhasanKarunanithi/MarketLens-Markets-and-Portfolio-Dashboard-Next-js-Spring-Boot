package com.marketlens.alert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Alert> findByStatus(Alert.Status status);

    Optional<Alert> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatusAndAcknowledgedFalse(UUID userId, Alert.Status status);
}
