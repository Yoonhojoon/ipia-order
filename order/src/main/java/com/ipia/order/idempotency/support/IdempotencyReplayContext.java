package com.ipia.order.idempotency.support;

public class IdempotencyReplayContext {
    private final String idempotencyKey;
    private final boolean replayed;
    private final String source; // redis | db | fresh
    private final Long recordedAtEpochMs; // 최초 처리 시각

    public IdempotencyReplayContext(String idempotencyKey, boolean replayed, String source, Long recordedAtEpochMs) {
        this.idempotencyKey = idempotencyKey;
        this.replayed = replayed;
        this.source = source;
        this.recordedAtEpochMs = recordedAtEpochMs;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public boolean isReplayed() { return replayed; }
    public String getSource() { return source; }
    public Long getRecordedAtEpochMs() { return recordedAtEpochMs; }
}


