package com.miki.footballpitch.player.model;

import jakarta.validation.constraints.*;

public record PlayerRegistrationRequest(
        @NotBlank(message = "Player ID cannot be blank")
        @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Player ID must be 2-10 alphanumeric characters")
        String id,

        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Player type cannot be null")
        String type
) { }

//ADR
//ENUM Configuration
//Observablity
//
