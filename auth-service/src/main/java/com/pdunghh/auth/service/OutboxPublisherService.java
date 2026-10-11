package com.pdunghh.auth.service;

public interface OutboxPublisherService {

    void publishPendingEvents();
}
