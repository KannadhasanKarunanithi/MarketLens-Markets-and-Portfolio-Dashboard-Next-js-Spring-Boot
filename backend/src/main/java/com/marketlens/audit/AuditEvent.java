package com.marketlens.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String path;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(nullable = false)
    private Instant at;

    protected AuditEvent() {
    }

    public AuditEvent(String actor, String method, String path, int statusCode) {
        this.id = UUID.randomUUID();
        this.actor = actor;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.at = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Instant getAt() {
        return at;
    }
}
