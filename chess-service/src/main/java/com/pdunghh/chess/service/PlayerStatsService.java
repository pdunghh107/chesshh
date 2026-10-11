package com.pdunghh.chess.service;

import com.pdunghh.shared.event.UserRegisteredEvent;

public interface PlayerStatsService {

    void handleUserRegistered(UserRegisteredEvent event);
}
