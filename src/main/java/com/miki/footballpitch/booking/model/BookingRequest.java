package com.miki.footballpitch.booking.model;

import java.time.LocalDateTime;

public record BookingRequest(Long pitchId, Long playerId, LocalDateTime startTime, int durationMinutes) {}
