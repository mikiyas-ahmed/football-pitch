package com.miki.footballpitch.booking;

import com.miki.footballpitch.booking.model.Booking;
import com.miki.footballpitch.booking.model.BookingFacade;
import com.miki.footballpitch.booking.model.BookingRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingEntityServiceTest
{
    @Mock
    private BookingRepository bookingRepository;
    private BookingFacade bookingFacade;
    private BookingRequest validRequest;
    private LocalDateTime testDateTime;
    private Long testPlayerId;
    private Long testPitchId;
    private int testOneHourInMinute;

    @BeforeEach
    void setUp()
    {
        testDateTime = LocalDateTime.of(2024, 1, 15, 14, 0);
        testPlayerId = 123L;
        testPitchId = 1L;
        testOneHourInMinute = 60;

        validRequest = createBookingRequest(testPitchId, testPlayerId, testDateTime, testOneHourInMinute);

        int maxAllowedBookingMinute = 120;
        bookingFacade = new BookingService(bookingRepository, maxAllowedBookingMinute);
    }

    @Test
    void shouldBookPitchSuccessfully()
    {
        // Given
        stubEmptyDailyBookingsForPlayer();
        stubNoConflictingBookingsForPitch();
        BookingEntity expectedBookingEntity = createBookingEntity(testPitchId, testPlayerId, testDateTime, testOneHourInMinute);
        stubSuccessfulBookingSave(expectedBookingEntity);

        // When
        Booking result = bookingFacade.bookPitch(validRequest);

        // Then
        assertBookingDetailsMatch(result, testPitchId, testPlayerId, testDateTime, testOneHourInMinute);
        verifyBookingSaved();
    }

    @Test
    void shouldThrowExceptionWhenExceedingDailyLimit()
    {
        // Given
        BookingEntity existingBookingWithNinetyMinutes = createBookingEntity(12L, 12L, LocalDateTime.now(), 90);
        stubExistingDailyBookingsForPlayer(List.of(existingBookingWithNinetyMinutes));

        // When & Then
        assertDailyLimitExceededException(() -> bookingFacade.bookPitch(validRequest));
        verifyNoBookingSaved();
    }

    @Test
    void shouldAllowBookingAtDailyLimit()
    {
        // Given
        BookingEntity existingBookingWithSixtyMinutes = createBookingEntity(12L, 12L, LocalDateTime.now(), 60);
        stubExistingDailyBookingsForPlayer(List.of(existingBookingWithSixtyMinutes));
        stubNoConflictingBookingsForPitch();
        BookingEntity expectedBookingEntity = createBookingEntity(testPitchId, testPlayerId, testDateTime, 60);
        stubSuccessfulBookingSave(expectedBookingEntity);

        // When
        Booking result = bookingFacade.bookPitch(validRequest);

        // Then
        assertBookingIsNotNull(result);
        verifyBookingSaved();
    }

    @Test
    void shouldThrowExceptionWhenPitchAlreadyBooked()
    {
        // Given
        stubEmptyDailyBookingsForPlayer();
        BookingEntity conflictingBooking = createConflictingBookingEntity();
        stubConflictingBookingsForPitch(List.of(conflictingBooking));

        // When & Then
        assertPitchAlreadyBookedException(() -> bookingFacade.bookPitch(validRequest));
        verifyNoBookingSaved();
    }

    @Test
    void shouldCalculateDailyTotalFromMultipleBookings()
    {
        // Given
        List<BookingEntity> multipleExistingBookings = createMultipleBookingEntitiesExceedingLimit();
        stubExistingDailyBookingsForPlayer(multipleExistingBookings);
        BookingRequest requestForThirtyMinutes = createBookingRequest(testPitchId, testPlayerId, testDateTime, 30);

        // When & Then
        assertDailyLimitExceededException(() -> bookingFacade.bookPitch(requestForThirtyMinutes));
    }

    @Test
    void shouldGetBookingsForPlayerOnDate()
    {
        // Given
        LocalDateTime queryDate = LocalDateTime.of(2024, 1, 15, 10, 0);
        List<BookingEntity> expectedBookingEntities = createTwoBookingEntitiesForPlayer();
        stubExistingDailyBookingsForPlayer(expectedBookingEntities);
        List<Booking> expectedBookings = mapBookingEntitiesToDtos(expectedBookingEntities);

        // When
        List<Booking> result = bookingFacade.getBookingsForPlayerOnDate(testPlayerId, queryDate);

        // Then
        assertBookingListMatches(result, expectedBookings);
        verifyDailyBookingsQueried();
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsFound()
    {
        // Given
        LocalDateTime queryDate = LocalDateTime.of(2024, 1, 15, 10, 0);
        stubEmptyDailyBookingsForPlayer();

        // When
        List<Booking> result = bookingFacade.getBookingsForPlayerOnDate(testPlayerId, queryDate);

        // Then
        assertBookingListIsEmpty(result);
        verifyDailyBookingsQueried();
    }

    // common instance initialization
    private BookingEntity createBookingEntity(Long pitchId, Long playerId, LocalDateTime startTime, int durationMinutes)
    {
        return new BookingEntity(pitchId, playerId, startTime, durationMinutes);
    }

    private BookingEntity createConflictingBookingEntity()
    {
        return createBookingEntity(testPitchId, 1L, testDateTime.plusMinutes(30), 60);
    }

    private List<BookingEntity> createMultipleBookingEntitiesExceedingLimit()
    {
        BookingEntity firstBooking = createBookingEntity(testPitchId, 1L, testDateTime.plusMinutes(30), 40);
        BookingEntity secondBooking = createBookingEntity(testPitchId, 1L, testDateTime.plusMinutes(30), 60);
        return Arrays.asList(firstBooking, secondBooking);
    }

    private List<BookingEntity> createTwoBookingEntitiesForPlayer()
    {
        return Arrays.asList(
                createBookingEntity(testPitchId, testPlayerId, testDateTime, 60),
                createBookingEntity(2L, testPlayerId, testDateTime.plusHours(2), 90)
        );
    }

    private BookingRequest createBookingRequest(Long pitchId, Long playerId, LocalDateTime dateTime, int durationMinutes)
    {
        return new BookingRequest(pitchId, playerId, dateTime, durationMinutes);
    }

    private List<Booking> mapBookingEntitiesToDtos(List<BookingEntity> bookingEntities)
    {
        return bookingEntities.stream().map(BookingEntity::mapToDto).toList();
    }

    // Common Mocking
    private void stubEmptyDailyBookingsForPlayer()
    {
        when(bookingRepository.findByPlayerIdAndStartTimeBetween(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());
    }

    private void stubExistingDailyBookingsForPlayer(List<BookingEntity> existingBookings)
    {
        when(bookingRepository.findByPlayerIdAndStartTimeBetween(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(existingBookings);
    }

    private void stubNoConflictingBookingsForPitch()
    {
        when(bookingRepository.findByPitchIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());
    }

    private void stubConflictingBookingsForPitch(List<BookingEntity> conflictingBookings)
    {
        when(bookingRepository.findByPitchIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(conflictingBookings);
    }

    private void stubSuccessfulBookingSave(BookingEntity bookingEntityToReturn)
    {
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(bookingEntityToReturn);
    }

    // Assertion
    private void assertBookingDetailsMatch(Booking actual, Long expectedPitchId, Long expectedPlayerId,
                                           LocalDateTime expectedStartTime, int expectedDurationMinutes)
    {
        SoftAssertions bookingAssertions = new SoftAssertions();
        bookingAssertions.assertThat(actual).isNotNull();
        bookingAssertions.assertThat(actual.pitchId()).isEqualTo(expectedPitchId);
        bookingAssertions.assertThat(actual.playerId()).isEqualTo(expectedPlayerId);
        bookingAssertions.assertThat(actual.startTime()).isEqualTo(expectedStartTime);
        bookingAssertions.assertThat(actual.durationMinutes()).isEqualTo(expectedDurationMinutes);
        bookingAssertions.assertAll();
    }

    private void assertBookingIsNotNull(Booking booking)
    {
        Assertions.assertThat(booking).isNotNull();
    }

    private void assertBookingListMatches(List<Booking> actual, List<Booking> expected)
    {
        SoftAssertions bookingListAssertions = new SoftAssertions();
        bookingListAssertions.assertThat(actual).isNotNull();
        bookingListAssertions.assertThat(actual.size()).isEqualTo(expected.size());
        bookingListAssertions.assertThat(actual).containsExactlyElementsOf(expected);
        bookingListAssertions.assertAll();
    }

    private void assertBookingListIsEmpty(List<Booking> bookingList)
    {
        SoftAssertions bookingListAssertions = new SoftAssertions();
        bookingListAssertions.assertThat(bookingList).isNotNull();
        bookingListAssertions.assertThat(bookingList).isEmpty();
        bookingListAssertions.assertAll();
    }

    private void assertDailyLimitExceededException(Runnable action)
    {
        assertThatThrownBy(action::run, "Player exceeds daily limit of 2 hours")
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void assertPitchAlreadyBookedException(Runnable action)
    {
        assertThatThrownBy(action::run, "Pitch is already booked for this time")
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Verification
    private void verifyBookingSaved()
    {
        verify(bookingRepository).save(any(BookingEntity.class));
    }

    private void verifyNoBookingSaved()
    {
        verify(bookingRepository, never()).save(any());
    }

    private void verifyDailyBookingsQueried()
    {
        verify(bookingRepository).findByPlayerIdAndStartTimeBetween(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }
}