package com.miki.footballpitch.booking.model;

import java.time.LocalDateTime;


public record Booking( Long id, Long pitchId, Long playerId, LocalDateTime startTime, int durationMinutes) {}