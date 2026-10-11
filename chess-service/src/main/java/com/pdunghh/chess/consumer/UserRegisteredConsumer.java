package com.pdunghh.chess.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.pdunghh.chess.service.PlayerStatsService;
import com.pdunghh.shared.event.RabbitMQConstants;
import com.pdunghh.shared.event.UserRegisteredEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final PlayerStatsService playerStatsService;

    @RabbitListener(
            queues = RabbitMQConstants.CHESS_PLAYER_STATS_USER_REGISTERED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent from RabbitMQ: userId={}, username={}",
                event.userId(), event.username());
        playerStatsService.handleUserRegistered(event);
    }
}
