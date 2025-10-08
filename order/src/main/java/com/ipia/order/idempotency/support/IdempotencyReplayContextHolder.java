package com.ipia.order.idempotency.support;

public final class IdempotencyReplayContextHolder {
    private static final ThreadLocal<IdempotencyReplayContext> CTX = new ThreadLocal<>();

    private IdempotencyReplayContextHolder() {}

    public static void set(IdempotencyReplayContext context) { CTX.set(context); }
    public static IdempotencyReplayContext get() { return CTX.get(); }
    public static void clear() { CTX.remove(); }
}


