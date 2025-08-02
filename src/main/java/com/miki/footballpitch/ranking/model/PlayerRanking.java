package com.miki.footballpitch.ranking.model;

public record PlayerRanking(
        String playerId,
        int points,
        int wins,
        int losses,
        int totalMatches
)
{
    public static PlayerRanking create(String playerId, int points, int wins, int losses)
    {
        return new PlayerRanking(playerId, points, wins, losses, wins + losses);
    }
}