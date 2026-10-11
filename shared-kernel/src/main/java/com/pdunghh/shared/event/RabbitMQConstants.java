package com.pdunghh.shared.event;

public final class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    // Exchange
    public static final String USER_EXCHANGE = "chesshh.user.exchange";
    public static final String USER_DLX = "chesshh.user.dlx";

    // Routing Keys
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";
    public static final String USER_REGISTERED_DLQ_ROUTING_KEY = "chess.player-stats.user-registered.dlq";

    // Queues
    public static final String CHESS_PLAYER_STATS_USER_REGISTERED_QUEUE = "chess.player-stats.user-registered.queue";
    public static final String CHESS_PLAYER_STATS_USER_REGISTERED_DLQ = "chess.player-stats.user-registered.dlq";
}
