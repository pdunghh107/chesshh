package com.pdunghh.auth.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.auth.entity.OutboxEvent;
import com.pdunghh.auth.repository.OutboxEventRepository;
import com.pdunghh.auth.service.OutboxPublisherService;
import com.pdunghh.shared.event.RabbitMQConstants;
import com.pdunghh.shared.event.UserRegisteredEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherServiceImpl implements OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;

    @Override
    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                "PENDING",
                PageRequest.of(0, BATCH_SIZE)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                if ("USER_REGISTERED".equals(event.getEventType())) {
                    UserRegisteredEvent userEvent = objectMapper.readValue(event.getPayload(), UserRegisteredEvent.class);
                    rabbitTemplate.convertAndSend(
                            RabbitMQConstants.USER_EXCHANGE,
                            RabbitMQConstants.USER_REGISTERED_ROUTING_KEY,
                            userEvent
                    );
                }

                event.setStatus("SENT");
                event.setSentAt(Instant.now());
                log.info("Successfully published outbox event id: {}, type: {}", event.getId(), event.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id: {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus("FAILED");
                }
            }
        }

        outboxEventRepository.saveAll(pendingEvents);
    }
}
