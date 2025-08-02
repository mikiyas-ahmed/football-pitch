package com.miki.footballpitch.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miki.footballpitch.booking.model.Booking;
import com.miki.footballpitch.booking.model.BookingFacade;
import com.miki.footballpitch.booking.model.BookingRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BookingControllerTest
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingFacade bookingFacade;

    private BookingRequest validBookingRequest;
    private BookingRequest invalidBookingRequestNullFields;
    private BookingRequest invalidBookingRequestNegativeValues;
    private BookingRequest invalidBookingRequestPastDate;
    private BookingRequest invalidBookingRequestInvalidDuration;

    private Booking expectedBooking;
    private LocalDateTime futureDateTime;
    private LocalDateTime pastDateTime;
    private Long validPlayerId;
    private Long validPitchId;

    @BeforeEach
    void setUp()
    {
        futureDateTime = LocalDateTime.now().plusDays(1);
        pastDateTime = LocalDateTime.now().minusDays(1);
        validPlayerId = 123L;
        validPitchId = 1L;

        expectedBooking = new Booking(1L, validPitchId, validPlayerId, futureDateTime, 60);

        validBookingRequest = createValidBookingRequest();
        invalidBookingRequestNullFields = createInvalidBookingRequestWithNullFields();
        invalidBookingRequestNegativeValues = createInvalidBookingRequestWithNegativeValues();
        invalidBookingRequestPastDate = createInvalidBookingRequestWithPastDate();
        invalidBookingRequestInvalidDuration = createInvalidBookingRequestWithInvalidDuration();
    }

    @Test
    void bookPitch_whenValidRequest_shouldSucceedWith201() throws Exception
    {
        // Given
        BDDMockito.given(bookingFacade.bookPitch(any(BookingRequest.class))).willReturn(expectedBooking);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(expectedBooking.id().intValue())))
                .andExpect(jsonPath("$.pitchId", is(expectedBooking.pitchId().intValue())))
                .andExpect(jsonPath("$.playerId", is(expectedBooking.playerId().intValue())))
                .andExpect(jsonPath("$.startTime", is(formatDateTime(expectedBooking.startTime()))))
                .andExpect(jsonPath("$.durationMinutes", is(expectedBooking.durationMinutes())));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenDailyLimitExceeded_shouldFailWith400()
    {
        // Given
        BDDMockito.given(bookingFacade.bookPitch(any(BookingRequest.class)))
                .willThrow(new IllegalArgumentException("Player exceeds daily limit of 2 hours"));

        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Business rule violation")))
                .andExpect(jsonPath("$.errors[0]", is("Player exceeds daily limit of 2 hours")));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenPitchAlreadyBooked_shouldFailWith400()
    {
        // Given
        BDDMockito.given(bookingFacade.bookPitch(any(BookingRequest.class)))
                .willThrow(new IllegalArgumentException("Pitch is already booked for this time"));

        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Business rule violation")))
                .andExpect(jsonPath("$.errors[0]", is("Pitch is already booked for this time")));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenNullFields_shouldFailWith400()
    {
        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingRequestNullFields))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenNegativeValues_shouldFailWith400()
    {
        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingRequestNegativeValues))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenPastDate_shouldFailWith400()
    {
        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingRequestPastDate))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenInvalidDuration_shouldFailWith400()
    {
        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingRequestInvalidDuration))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @SneakyThrows
    void bookPitch_whenMalformedJson_shouldFailWith400()
    {
        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invalid\": json}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForPlayerOnDate_whenValidParameters_shouldSucceedWith200() throws Exception
    {
        // Given
        LocalDateTime queryDate = futureDateTime;
        List<Booking> expectedBookings = createMultipleBookingsForPlayer();
        BDDMockito.given(bookingFacade.getBookingsForPlayerOnDate(any(Long.class), any(LocalDateTime.class)))
                .willReturn(expectedBookings);

        // When & Then
        mockMvc.perform(get("/bookings")
                        .param("playerId", validPlayerId.toString())
                        .param("date", formatDateTime(queryDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedBookings.size())))
                .andExpect(jsonPath("$[0].id", is(expectedBookings.get(0).id().intValue())))
                .andExpect(jsonPath("$[0].pitchId", is(expectedBookings.get(0).pitchId().intValue())))
                .andExpect(jsonPath("$[0].playerId", is(expectedBookings.get(0).playerId().intValue())))
                .andExpect(jsonPath("$[0].startTime", is(formatDateTime(expectedBookings.get(0).startTime()))))
                .andExpect(jsonPath("$[0].durationMinutes", is(expectedBookings.get(0).durationMinutes())));
    }

    @Test
    void getBookingsForPlayerOnDate_whenNoBookingsFound_shouldSucceedWith200() throws Exception
    {
        // Given
        LocalDateTime queryDate = futureDateTime;
        BDDMockito.given(bookingFacade.getBookingsForPlayerOnDate(any(Long.class), any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/bookings")
                        .param("playerId", validPlayerId.toString())
                        .param("date", formatDateTime(queryDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getBookingsForPlayerOnDate_whenMissingPlayerId_shouldFailWith400() throws Exception
    {
        // Given
        LocalDateTime queryDate = futureDateTime;

        // When & Then
        mockMvc.perform(get("/bookings")
                        .param("date", formatDateTime(queryDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForPlayerOnDate_whenMissingDate_shouldFailWith400() throws Exception
    {
        // When & Then
        mockMvc.perform(get("/bookings")
                        .param("playerId", validPlayerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForPlayerOnDate_whenInvalidPlayerId_shouldFailWith400() throws Exception
    {
        // Given
        LocalDateTime queryDate = futureDateTime;

        // When & Then
        mockMvc.perform(get("/bookings")
                        .param("playerId", "0") // Invalid - not positive
                        .param("date", formatDateTime(queryDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    void getBookingsForPlayerOnDate_whenInvalidDateFormat_shouldFailWith400() throws Exception
    {
        // When & Then
        mockMvc.perform(get("/bookings")
                        .param("playerId", validPlayerId.toString())
                        .param("date", "invalid-date-format")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Helper Methods
    private BookingRequest createValidBookingRequest()
    {
        return new BookingRequest(validPitchId, validPlayerId, futureDateTime, 60);
    }

    private BookingRequest createInvalidBookingRequestWithNullFields()
    {
        return new BookingRequest(null, null, null, null);
    }

    private BookingRequest createInvalidBookingRequestWithNegativeValues()
    {
        return new BookingRequest(-1L, -1L, futureDateTime, -10);
    }

    private BookingRequest createInvalidBookingRequestWithPastDate()
    {
        return new BookingRequest(validPitchId, validPlayerId, pastDateTime, 60);
    }

    private BookingRequest createInvalidBookingRequestWithInvalidDuration()
    {
        return new BookingRequest(validPitchId, validPlayerId, futureDateTime, 10); // Below minimum 15 minutes
    }

    private List<Booking> createMultipleBookingsForPlayer()
    {
        return Arrays.asList(
                new Booking(1L, validPitchId, validPlayerId, futureDateTime, 60),
                new Booking(2L, 2L, validPlayerId, futureDateTime.plusHours(2), 90)
        );
    }

    private String formatDateTime(LocalDateTime dateTime)
    {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}