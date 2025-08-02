package com.miki.footballpitch.pitch.model;

import jakarta.validation.constraints.NotBlank;

public record PitchRequest(
        @NotBlank(message = "Pitch ID is required")
        String id,

        @NotBlank(message = "Pitch name is required")
        String name
) {}