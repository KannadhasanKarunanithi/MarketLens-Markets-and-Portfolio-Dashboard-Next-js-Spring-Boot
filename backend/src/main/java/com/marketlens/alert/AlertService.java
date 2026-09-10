package com.marketlens.alert;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.alert.dto.AlertDtos.AlertResponse;
import com.marketlens.alert.dto.AlertDtos.CreateAlertRequest;
import com.marketlens.common.error.NotFoundException;
import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alerts;
    private final InstrumentRepository instruments;

    public AlertService(AlertRepository alerts, InstrumentRepository instruments) {
        this.alerts = alerts;
        this.instruments = instruments;
    }

    public AlertResponse create(UUID userId, CreateAlertRequest request) {
        Instrument instrument = instruments.findById(request.instrumentId())
                .orElseThrow(() -> new NotFoundException("Instrument not found"));
        Alert alert = alerts.save(new Alert(userId, request.instrumentId(), request.direction(),
                request.threshold(), request.note()));
        return AlertResponse.of(alert, instrument.getSymbol(), instrument.getName());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> listFor(UUID userId) {
        Map<UUID, Instrument> byId = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        return alerts.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(alert -> {
                    Instrument instrument = byId.get(alert.getInstrumentId());
                    return AlertResponse.of(alert,
                            instrument == null ? "?" : instrument.getSymbol(),
                            instrument == null ? "Unknown" : instrument.getName());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return alerts.countByUserIdAndStatusAndAcknowledgedFalse(userId, Alert.Status.TRIGGERED);
    }

    public AlertResponse acknowledge(UUID userId, UUID id) {
        Alert alert = require(userId, id);
        alert.acknowledge();
        Instrument instrument = instruments.findById(alert.getInstrumentId()).orElse(null);
        return AlertResponse.of(alert,
                instrument == null ? "?" : instrument.getSymbol(),
                instrument == null ? "Unknown" : instrument.getName());
    }

    public void delete(UUID userId, UUID id) {
        alerts.delete(require(userId, id));
    }

    private Alert require(UUID userId, UUID id) {
        return alerts.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Alert not found"));
    }
}
