package com.marketlens.quote;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.marketlens.quote.dto.QuoteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Fans out quote updates to any connected browser as server sent events.
 */
@Component
public class QuoteStream {

    private static final Logger log = LoggerFactory.getLogger(QuoteStream.class);

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter open() {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        emitters.add(emitter);
        try {
            emitter.send(SseEmitter.event().name("hello").data("connected"));
        } catch (IOException ex) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void publish(List<QuoteResponse> quotes) {
        if (emitters.isEmpty() || quotes.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("quotes").data(quotes));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            }
        }
        log.debug("Published {} quotes to {} listeners", quotes.size(), emitters.size());
    }

    public int listenerCount() {
        return emitters.size();
    }
}
