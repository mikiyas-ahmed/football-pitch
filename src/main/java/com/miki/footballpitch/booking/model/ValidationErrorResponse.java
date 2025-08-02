package com.miki.footballpitch.booking.model;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        String message,
        LocalDateTime timestamp,
        List<String> errors)
{
    public static ValidationErrorResponse of(String message, List<String> errors)
    {
        return new ValidationErrorResponse(message, LocalDateTime.now(), errors);
    }
}