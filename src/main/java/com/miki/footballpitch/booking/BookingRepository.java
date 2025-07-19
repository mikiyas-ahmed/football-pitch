package com.miki.footballpitch.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

interface BookingRepository extends JpaRepository<BookingEntity, Long>
{
    List<BookingEntity> findByPlayerIdAndStartTimeBetween(Long playerId, LocalDateTime start, LocalDateTime end);

    List<BookingEntity> findByPitchIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long pitchId,
            LocalDateTime newBookingEndTime,
            LocalDateTime newBookingStartTime);
}
