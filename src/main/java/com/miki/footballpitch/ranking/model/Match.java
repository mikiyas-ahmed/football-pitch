package com.miki.footballpitch.ranking.model;


import java.time.LocalDateTime;

public record Match(
        Long id,
        String player1Id,
        String player2Id,
        String winnerId,
        LocalDateTime matchDate
) {}