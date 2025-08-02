package com.miki.footballpitch.player.model;

import java.util.List;

public interface PlayerFacade
{

    Player registerPlayer(PlayerRegistrationRequest playerRequest);

    Player getPlayer(String playerId);

    List<Player> getAllPlayers();

    boolean playerExists(String playerId);

    int getPlayerMaxAdvanceDays(String playerId);

    List<PlayerType> getActivePlayerTypes();

    PlayerType getPlayerType(String typeCode);
}