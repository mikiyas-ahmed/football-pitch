package com.miki.footballpitch.player.model;

public record PlayerType(
        String code,
        String name,
        int maxAdvanceDays,
        boolean active
) {
}