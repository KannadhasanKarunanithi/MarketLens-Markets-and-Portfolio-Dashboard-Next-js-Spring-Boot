package com.marketlens.instrument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstrumentRepository extends JpaRepository<Instrument, UUID> {

    Optional<Instrument> findBySymbol(String symbol);

    List<Instrument> findByActiveTrueOrderBySymbolAsc();

    List<Instrument> findByAssetClassAndActiveTrueOrderBySymbolAsc(AssetClass assetClass);

    @Query("""
            select i from Instrument i
            where i.active = true
              and (lower(i.symbol) like lower(concat('%', :term, '%'))
                   or lower(i.name) like lower(concat('%', :term, '%')))
            order by i.symbol asc
            """)
    List<Instrument> search(@Param("term") String term, Limit limit);
}
