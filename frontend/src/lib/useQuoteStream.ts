"use client";

import { useEffect, useRef, useState } from "react";
import type { Quote } from "./types";

/**
 * Subscribes to the backend quote stream and keeps a live map of the latest
 * quote per symbol. Falls back silently if the stream drops.
 */
export function useQuoteStream(): Record<string, Quote> {
  const [quotes, setQuotes] = useState<Record<string, Quote>>({});
  const sourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    const source = new EventSource("/api/quotes/stream");
    sourceRef.current = source;

    source.addEventListener("quotes", (event) => {
      try {
        const incoming = JSON.parse((event as MessageEvent).data) as Quote[];
        setQuotes((current) => {
          const next = { ...current };
          for (const quote of incoming) next[quote.symbol] = quote;
          return next;
        });
      } catch {
        // ignore malformed frame
      }
    });

    source.onerror = () => {
      source.close();
    };

    return () => {
      source.close();
      sourceRef.current = null;
    };
  }, []);

  return quotes;
}
