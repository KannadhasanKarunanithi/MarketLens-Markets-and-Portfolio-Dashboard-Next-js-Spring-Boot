package com.marketlens.audit;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository events;

    public AuditService(AuditEventRepository events) {
        this.events = events;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String method, String path, int statusCode) {
        events.save(new AuditEvent(actor, method, path, statusCode));
    }

    @Transactional(readOnly = true)
    public Page<AuditEventView> list(int page, int size) {
        return events.findAllByOrderByAtDesc(PageRequest.of(page, Math.min(size, 100)))
                .map(e -> new AuditEventView(e.getId(), e.getActor(), e.getMethod(), e.getPath(),
                        e.getStatusCode(), e.getAt()));
    }

    public record AuditEventView(UUID id, String actor, String method, String path, int statusCode,
            Instant at) {
    }
}
