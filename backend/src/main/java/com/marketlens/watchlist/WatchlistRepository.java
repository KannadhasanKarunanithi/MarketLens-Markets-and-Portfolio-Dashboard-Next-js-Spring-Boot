package com.marketlens.watchlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

    List<Watchlist> findByUserIdOrderByPositionAscCreatedAtAsc(UUID userId);

    Optional<Watchlist> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
