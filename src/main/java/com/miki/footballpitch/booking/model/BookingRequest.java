package com.miki.footballpitch.booking.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull(message = "Pitch ID cannot be null")
        @Min(value = 1, message = "Pitch ID must be positive")
        Long pitchId,

        @NotNull(message = "Player ID cannot be null")
        @Min(value = 1, message = "Player ID must be positive")
        Long playerId,

        @NotNull(message = "Start time cannot be null")
        @Future(message = "Start time must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startTime,

        @NotNull(message = "Duration cannot be null")
        @Min(value = 15, message = "Minimum booking duration is 15 minutes")
        @Max(value = 120, message = "Maximum booking duration is 120 minutes")
        Integer durationMinutes
) {}