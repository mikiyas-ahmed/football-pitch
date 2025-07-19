package com.miki.footballpitch.booking;

import com.miki.footballpitch.booking.model.Booking;
import com.miki.footballpitch.booking.model.BookingFacade;
import com.miki.footballpitch.booking.model.BookingRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
class BookingService implements BookingFacade
{
    final int maxAllowedBookingMinute;

    private final BookingRepository bookingRepository;

    BookingService(BookingRepository bookingRepository, @Value("${max_booking_minute}") int maxAllowedBookingMinute)
    {
        this.bookingRepository = bookingRepository;
        this.maxAllowedBookingMinute = maxAllowedBookingMinute;
    }

    @Override
    public Booking bookPitch(BookingRequest request)
    {
        LocalDateTime start = request.startTime();
        LocalDateTime end = start.plusMinutes(request.durationMinutes());

        List<BookingEntity> dailyBookingEntities = bookingRepository.findByPlayerIdAndStartTimeBetween(
                request.playerId(),
                start.toLocalDate().atStartOfDay(),
                start.toLocalDate().atTime(23, 59)
        );

        validateMaxAllowedMinute(request, dailyBookingEntities);

        validateBookingConflict(request, start, end);

        BookingEntity bookingEntity = bookingRepository.save(new BookingEntity(
                request.pitchId(), request.playerId(), request.startTime(), request.durationMinutes()
        ));

        return bookingEntity.mapToDto();
    }

    @Override
    public List<Booking> getBookingsForPlayerOnDate(Long playerId, LocalDateTime date)
    {
        List<BookingEntity> byPlayerIdAndStartTimeBetween = bookingRepository.findByPlayerIdAndStartTimeBetween(
                playerId,
                date.toLocalDate().atStartOfDay(),
                date.toLocalDate().atTime(23, 59)
        );
        return byPlayerIdAndStartTimeBetween.stream().map(BookingEntity::mapToDto).toList();
    }

    private void validateBookingConflict(BookingRequest request, LocalDateTime start, LocalDateTime end)
    {
        List<BookingEntity> conflicts = bookingRepository.findByPitchIdAndStartTimeLessThanAndEndTimeGreaterThan(
                request.pitchId(), start, end
        );

        if (!conflicts.isEmpty())
        {
            throw new IllegalArgumentException("Pitch is already booked for this time");
        }
    }

    private void validateMaxAllowedMinute(BookingRequest request, List<BookingEntity> dailyBookingEntities)
    {
        int totalMinutes = dailyBookingEntities.stream()
                .mapToInt(BookingEntity::getDurationMinutes)
                .sum();

        if (totalMinutes + request.durationMinutes() > maxAllowedBookingMinute)
        {
            throw new IllegalArgumentException("Player exceeds daily limit of 2 hours");
        }
    }

}
