package com.miki.footballpitch.booking.model;


import java.time.LocalDateTime;
import java.util.List;

public interface BookingFacade
{
    Booking bookPitch(BookingRequest request);

    List<Booking> getBookingsForPlayerOnDate(Long playerId, LocalDateTime date);
}
