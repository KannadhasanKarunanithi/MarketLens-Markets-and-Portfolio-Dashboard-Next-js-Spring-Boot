package com.marketlens.pricebar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PriceBarRepository extends JpaRepository<PriceBar, UUID> {

    List<PriceBar> findByInstrumentIdAndDateGreaterThanEqualOrderByDateAsc(UUID instrumentId, LocalDate from);

    List<PriceBar> findByInstrumentIdOrderByDateAsc(UUID instrumentId);

    Optional<PriceBar> findByInstrumentIdAndDate(UUID instrumentId, LocalDate date);

    Optional<PriceBar> findTopByInstrumentIdOrderByDateDesc(UUID instrumentId);

    @Query("select count(b) from PriceBar b")
    long countAll();

    @Query("""
            select b from PriceBar b
            where b.date >= :from
            order by b.instrumentId asc, b.date asc
            """)
    List<PriceBar> findAllSince(LocalDate from);
}
