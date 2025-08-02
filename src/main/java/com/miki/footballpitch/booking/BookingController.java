package com.miki.footballpitch.booking;

import com.miki.footballpitch.booking.model.Booking;
import com.miki.footballpitch.booking.model.BookingFacade;
import com.miki.footballpitch.booking.model.BookingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@Validated
class BookingController
{
    private final BookingFacade bookingFacade;

    BookingController(BookingFacade bookingFacade)
    {
        this.bookingFacade = bookingFacade;
    }

    @PostMapping
    public ResponseEntity<Booking> book(@RequestBody @Valid BookingRequest request)
    {
            Booking booking = bookingFacade.bookPitch(request);
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getBookingsForPlayerOnDate(
            @RequestParam
            @NotNull(message = "Player ID cannot be null")
            @Positive(message = "Player ID must be positive")
            Long playerId,

            @RequestParam
            @NotNull(message = "Date cannot be null")
            LocalDateTime date
    )
    {
        List<Booking> booking = bookingFacade.getBookingsForPlayerOnDate(playerId, date);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}
