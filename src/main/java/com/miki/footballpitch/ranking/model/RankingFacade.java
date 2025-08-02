package com.miki.footballpitch.ranking.model;

import java.util.List;

public interface RankingFacade
{

    Match submitMatchResult(MatchRequest matchRequest);

    List<PlayerRanking> getRankings();

    PlayerRanking getPlayerRanking(String playerId);

    List<Match> getPlayerMatches(String playerId);
}