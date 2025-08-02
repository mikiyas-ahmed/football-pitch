package com.miki.footballpitch.player;

import com.miki.footballpitch.player.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class PlayerService implements PlayerFacade
{

    private final PlayerRepository playerRepository;
    private final PlayerTypeRepository playerTypeRepository;

    @Override
    @Transactional
    public Player registerPlayer(PlayerRegistrationRequest playerRequest)
    {
        validatePlayerRequest(playerRequest);

        PlayerEntity playerEntity = new PlayerEntity(
                playerRequest.id(),
                playerRequest.name(),
                playerRequest.email(),
                playerRequest.type()
        );

        PlayerEntity savedPlayer = playerRepository.save(playerEntity);
        return toPlayer(savedPlayer);
    }

    @Override
    public Player getPlayer(String playerId)
    {
        return playerRepository.findById(playerId)
                .map(this::toPlayer)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    @Override
    public List<Player> getAllPlayers()
    {
        return playerRepository.findAll()
                .stream()
                .map(this::toPlayer)
                .toList();
    }

    @Override
    public boolean playerExists(String playerId)
    {
        return playerRepository.existsById(playerId);
    }

    @Override
    public int getPlayerMaxAdvanceDays(String playerId)
    {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        return playerTypeRepository.findByCodeAndActiveTrue(player.getPlayerTypeCode())
                .map(PlayerTypeEntity::getMaxAdvanceDays)
                .orElseThrow(() -> new IllegalArgumentException("Invalid player type: " + player.getPlayerTypeCode()));
    }

    @Override
    public List<PlayerType> getActivePlayerTypes()
    {
        return playerTypeRepository.findByActiveTrue()
                .stream()
                .map(this::toPlayerType)
                .toList();
    }

    @Override
    public PlayerType getPlayerType(String typeCode)
    {
        return playerTypeRepository.findByCodeAndActiveTrue(typeCode)
                .map(this::toPlayerType)
                .orElseThrow(() -> new IllegalArgumentException("Player type not found: " + typeCode));
    }

    private void validatePlayerRequest(PlayerRegistrationRequest playerRequest)
    {
        if (playerRepository.existsById(playerRequest.id()))
        {
            throw new IllegalArgumentException("Player with ID " + playerRequest.id() + " already exists");
        }

        if (playerRepository.existsByEmail(playerRequest.email()))
        {
            throw new IllegalArgumentException("Player with email " + playerRequest.email() + " already exists");
        }

        if (!playerTypeRepository.findByCodeAndActiveTrue(playerRequest.type()).isPresent())
        {
            throw new IllegalArgumentException("Invalid player type: " + playerRequest.type());
        }
    }

    private Player toPlayer(PlayerEntity entity)
    {
        return new Player(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPlayerTypeCode(),
                entity.getRegistrationDate()
        );
    }

    private PlayerType toPlayerType(PlayerTypeEntity entity)
    {
        return new PlayerType(entity.getCode(), entity.getName(), entity.getMaxAdvanceDays(), entity.isActive());
    }
}