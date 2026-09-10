package com.marketlens.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.common.error.BadRequestException;
import com.marketlens.common.error.NotFoundException;
import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.transaction.dto.TransactionDtos.RecordTransactionRequest;
import com.marketlens.transaction.dto.TransactionDtos.TransactionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactions;
    private final InstrumentRepository instruments;

    public TransactionService(TransactionRepository transactions, InstrumentRepository instruments) {
        this.transactions = transactions;
        this.instruments = instruments;
    }

    public TransactionResponse record(UUID userId, RecordTransactionRequest request) {
        Instrument instrument = instruments.findById(request.instrumentId())
                .orElseThrow(() -> new NotFoundException("Instrument not found"));
        if (!instrument.isTradeable()) {
            throw new BadRequestException(instrument.getSymbol() + " is an index and cannot be traded");
        }
        if (request.type() == TransactionType.SELL) {
            BigDecimal held = heldQuantity(userId, request.instrumentId());
            if (request.quantity().compareTo(held) > 0) {
                throw new BadRequestException(
                        "You only hold " + held.stripTrailingZeros().toPlainString() + " units of "
                                + instrument.getSymbol());
            }
        }
        Transaction saved = transactions.save(new Transaction(
                userId,
                request.instrumentId(),
                request.type(),
                request.quantity(),
                request.price(),
                request.fees() == null ? BigDecimal.ZERO : request.fees(),
                request.tradedOn(),
                request.note()));
        return TransactionResponse.of(saved, instrument.getSymbol(), instrument.getName());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listFor(UUID userId) {
        Map<UUID, Instrument> byId = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        return transactions.findByUserIdOrderByTradedOnDescCreatedAtDesc(userId).stream()
                .map(tx -> {
                    Instrument instrument = byId.get(tx.getInstrumentId());
                    return TransactionResponse.of(tx,
                            instrument == null ? "?" : instrument.getSymbol(),
                            instrument == null ? "Unknown" : instrument.getName());
                })
                .toList();
    }

    public void delete(UUID userId, UUID id) {
        Transaction tx = transactions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        transactions.delete(tx);
    }

    private BigDecimal heldQuantity(UUID userId, UUID instrumentId) {
        BigDecimal held = BigDecimal.ZERO;
        for (Transaction tx : transactions
                .findByUserIdAndInstrumentIdOrderByTradedOnAscCreatedAtAsc(userId, instrumentId)) {
            if (tx.getType() == TransactionType.BUY) {
                held = held.add(tx.getQuantity());
            } else if (tx.getType() == TransactionType.SELL) {
                held = held.subtract(tx.getQuantity());
            }
        }
        return held;
    }
}
