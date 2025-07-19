package com.miki.footballpitch.booking;

import com.miki.footballpitch.booking.model.Booking;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
class BookingEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pitchId;
    private Long playerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;

    BookingEntity(Long pitchId, Long playerId, LocalDateTime startTime, int durationMinutes)
    {
        this.pitchId = pitchId;
        this.playerId = playerId;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.endTime = startTime.plusMinutes(durationMinutes);
    }

    Booking mapToDto()
    {
         return new Booking(this.id,
                 this.pitchId,
                 this.playerId,
                 this.startTime,
                 this.durationMinutes);
    }

    static BookingEntity mopToEntity(Booking booking)
    {
        return new BookingEntity(booking.pitchId(),
                booking.playerId(),
                booking.startTime(),
                booking.durationMinutes());
    }
}

