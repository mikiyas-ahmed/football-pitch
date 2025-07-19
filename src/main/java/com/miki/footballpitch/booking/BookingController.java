package com.miki.footballpitch.booking;

import com.miki.footballpitch.booking.model.Booking;
import com.miki.footballpitch.booking.model.BookingFacade;
import com.miki.footballpitch.booking.model.BookingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
class BookingController
{

    private final BookingFacade bookingFacade;

    BookingController(BookingFacade bookingFacade)
    {
        this.bookingFacade = bookingFacade;
    }

    @PostMapping
    public ResponseEntity<Booking> book(@RequestBody BookingRequest request)
    {
            Booking booking = bookingFacade.bookPitch(request);
            return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}


//    Rewrite using AssertJ (Test object pattern)
//    Add MockMvc tests for controller
//        Add ArchUnit tests
//        All classes that reside under “model” package MUST be public
//    All classes that DO NOT reside under “model” package MUST NOT be public
//
//Player domain
//        Write the missing impl
//        Add validation
//
//        Missing for today
//        Define ADRs

//  unit  test/ acceptance test/
//   test in isolation integration point

//    If you have time
//      - test in isolation the integreation points (DB, Controllers)
//        unit tests, acceptance tests, integration tests
