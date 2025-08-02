package com.miki.footballpitch.player;

import com.miki.footballpitch.player.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
class PlayerController
{

    private final PlayerFacade playerFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player registerPlayer(@Valid @RequestBody PlayerRegistrationRequest request)
    {
        return playerFacade.registerPlayer(request);
    }

    @GetMapping("/{playerId}")
    public Player getPlayer(@PathVariable String playerId)
    {
        return playerFacade.getPlayer(playerId);
    }

    @GetMapping
    public List<Player> getAllPlayers()
    {
        return playerFacade.getAllPlayers();
    }

    @GetMapping("/types")
    public List<PlayerType> getActivePlayerTypes()
    {
        return playerFacade.getActivePlayerTypes();
    }

    @GetMapping("/types/{typeCode}")
    public PlayerType getPlayerType(@PathVariable String typeCode)
    {
        return playerFacade.getPlayerType(typeCode);
    }

    @GetMapping("/{playerId}/max-advance-days")
    public ResponseEntity<Integer> getMaxAdvanceBookingDays(@PathVariable String playerId)
    {
        int maxDays = playerFacade.getPlayerMaxAdvanceDays(playerId);
        return ResponseEntity.ok(maxDays);
    }

    @GetMapping("/{playerId}/can-book")
    public ResponseEntity<Boolean> canPlayerBookAtTime(
            @PathVariable String playerId,
            @RequestParam LocalDateTime bookingTime)
    {
        int maxAdvanceDays = playerFacade.getPlayerMaxAdvanceDays(playerId);
        LocalDateTime maxBookingTime = LocalDateTime.now().plusDays(maxAdvanceDays);
        boolean canBook = !bookingTime.isAfter(maxBookingTime);

        return ResponseEntity.ok(canBook);
    }
}