package com.marketlens.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdOrderByTradedOnDescCreatedAtDesc(UUID userId);

    List<Transaction> findByUserIdOrderByTradedOnAscCreatedAtAsc(UUID userId);

    List<Transaction> findByUserIdAndInstrumentIdOrderByTradedOnAscCreatedAtAsc(UUID userId, UUID instrumentId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserId(UUID userId);
}
