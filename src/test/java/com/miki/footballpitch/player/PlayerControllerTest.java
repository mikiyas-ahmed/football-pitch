package com.miki.footballpitch.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miki.footballpitch.player.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayerFacade playerFacade;

    @Test
    void shouldRegisterPlayerSuccessfully() throws Exception {
        PlayerRegistrationRequest request = createValidPlayerRequest();
        Player registeredPlayer = createPlayer("PL1", "John Doe", "john@test.com", "MEMBER");

        when(playerFacade.registerPlayer(any(PlayerRegistrationRequest.class)))
                .thenReturn(registeredPlayer);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("PL1"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.type").value("MEMBER"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPlayerRegistration() throws Exception {
        PlayerRegistrationRequest invalidRequest = new PlayerRegistrationRequest(
                "", "", "invalid-email", "");

        // Don't mock the facade - let validation fail at Spring level
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidPlayerId() throws Exception {
        PlayerRegistrationRequest invalidRequest = new PlayerRegistrationRequest(
                "invalid-id", "John Doe", "john@test.com", "MEMBER");

        // Don't mock the facade - let validation fail at Spring level
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidPlayerType() throws Exception {
        PlayerRegistrationRequest invalidRequest = new PlayerRegistrationRequest(
                "PL1", "John Doe", "john@test.com", null);

        // Don't mock the facade for validation error tests - let validation fail naturally
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetPlayerSuccessfully() throws Exception {
        Player player = createPlayer("PL1", "John Doe", "john@test.com", "MEMBER");

        when(playerFacade.getPlayer("PL1")).thenReturn(player);

        mockMvc.perform(get("/api/players/PL1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PL1"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.type").value("MEMBER"));
    }

    @Test
    void shouldReturnNotFoundWhenPlayerDoesNotExist() throws Exception {
        when(playerFacade.getPlayer("PL999"))
                .thenThrow(new IllegalArgumentException("Player not found: PL999"));

        mockMvc.perform(get("/api/players/PL999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllPlayersSuccessfully() throws Exception {
        List<Player> players = List.of(
                createPlayer("PL1", "John Doe", "john@test.com", "MEMBER"),
                createPlayer("PL2", "Jane Smith", "jane@test.com", "GUEST")
        );

        when(playerFacade.getAllPlayers()).thenReturn(players);

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("PL1"))
                .andExpect(jsonPath("$[1].id").value("PL2"));
    }

    @Test
    void shouldGetActivePlayerTypesSuccessfully() throws Exception {
        List<PlayerType> playerTypes = List.of(
                new PlayerType("MEMBER", "Club Member", 14, true),
                new PlayerType("GUEST", "Guest Player", 3, true)
        );

        when(playerFacade.getActivePlayerTypes()).thenReturn(playerTypes);

        mockMvc.perform(get("/api/players/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("MEMBER"))
                .andExpect(jsonPath("$[0].maxAdvanceDays").value(14))
                .andExpect(jsonPath("$[1].code").value("GUEST"))
                .andExpect(jsonPath("$[1].maxAdvanceDays").value(3));
    }

    @Test
    void shouldGetPlayerTypeSuccessfully() throws Exception {
        PlayerType memberType = new PlayerType("MEMBER", "Club Member", 14, true);

        when(playerFacade.getPlayerType("MEMBER")).thenReturn(memberType);

        mockMvc.perform(get("/api/players/types/MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MEMBER"))
                .andExpect(jsonPath("$.name").value("Club Member"))
                .andExpect(jsonPath("$.maxAdvanceDays").value(14))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetMaxAdvanceBookingDaysSuccessfully() throws Exception {
        when(playerFacade.getPlayerMaxAdvanceDays("PL1")).thenReturn(14);

        mockMvc.perform(get("/api/players/PL1/max-advance-days"))
                .andExpect(status().isOk())
                .andExpect(content().string("14"));
    }

    @Test
    void shouldCheckCanPlayerBookAtTimeSuccessfully() throws Exception {
        when(playerFacade.getPlayerMaxAdvanceDays("PL1")).thenReturn(14);

        LocalDateTime futureTime = LocalDateTime.now().plusDays(10);

        mockMvc.perform(get("/api/players/PL1/can-book")
                        .param("bookingTime", futureTime.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalseWhenBookingTimeTooFarInAdvance() throws Exception {
        when(playerFacade.getPlayerMaxAdvanceDays("PL1")).thenReturn(14);

        LocalDateTime tooFarFuture = LocalDateTime.now().plusDays(20);

        mockMvc.perform(get("/api/players/PL1/can-book")
                        .param("bookingTime", tooFarFuture.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldHandleIllegalArgumentExceptionAs404() throws Exception {
        when(playerFacade.getPlayer("PL999"))
                .thenThrow(new IllegalArgumentException("Player not found: PL999"));

        mockMvc.perform(get("/api/players/PL999"))
                .andExpect(status().isNotFound());
    }

    private PlayerRegistrationRequest createValidPlayerRequest() {
        return new PlayerRegistrationRequest("PL1", "John Doe", "john@test.com", "MEMBER");
    }

    private Player createPlayer(String id, String name, String email, String type) {
        return new Player(id, name, email, type, LocalDateTime.now());
    }
}