package com.marketlens.alert;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.marketlens.alert.dto.AlertDtos.AlertResponse;
import com.marketlens.alert.dto.AlertDtos.CreateAlertRequest;
import com.marketlens.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> list() {
        return alertService.listFor(CurrentUser.require().id());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", alertService.unreadCount(CurrentUser.require().id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(@Valid @RequestBody CreateAlertRequest request) {
        return alertService.create(CurrentUser.require().id(), request);
    }

    @PostMapping("/{id}/acknowledge")
    public AlertResponse acknowledge(@PathVariable UUID id) {
        return alertService.acknowledge(CurrentUser.require().id(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        alertService.delete(CurrentUser.require().id(), id);
    }
}
