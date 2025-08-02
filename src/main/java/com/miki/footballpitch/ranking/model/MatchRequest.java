package com.miki.footballpitch.ranking.model;

import jakarta.validation.constraints.NotBlank;

public record MatchRequest(
        @NotBlank(message = "Player 1 ID is required")
        String player1Id,

        @NotBlank(message = "Player 2 ID is required")
        String player2Id,

        @NotBlank(message = "Winner ID is required")
        String winnerId
) {}