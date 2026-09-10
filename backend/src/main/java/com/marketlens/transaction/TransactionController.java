package com.marketlens.transaction;

import java.util.List;
import java.util.UUID;

import com.marketlens.common.security.CurrentUser;
import com.marketlens.transaction.dto.TransactionDtos.RecordTransactionRequest;
import com.marketlens.transaction.dto.TransactionDtos.TransactionResponse;
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
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> list() {
        return transactionService.listFor(CurrentUser.require().id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(@Valid @RequestBody RecordTransactionRequest request) {
        return transactionService.record(CurrentUser.require().id(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        transactionService.delete(CurrentUser.require().id(), id);
    }
}
