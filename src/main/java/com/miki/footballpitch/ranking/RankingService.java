package com.miki.footballpitch.ranking;

import com.miki.footballpitch.ranking.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
class RankingService implements RankingFacade
{

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository)
    {
        this.rankingRepository = rankingRepository;
    }

    @Override
    @Transactional
    public Match submitMatchResult(MatchRequest matchRequest)
    {
        validateMatchRequest(matchRequest);

        MatchEntity matchEntity = new MatchEntity(
                matchRequest.player1Id(),
                matchRequest.player2Id(),
                matchRequest.winnerId()
        );

        MatchEntity savedMatch = rankingRepository.save(matchEntity);
        return toMatch(savedMatch);
    }

    @Override
    public List<PlayerRanking> getRankings()
    {
        List<String> allPlayerIds = getAllPlayerIds();

        return allPlayerIds.stream()
                .map(this::calculatePlayerRanking)
                .sorted((r1, r2) -> Integer.compare(r2.points(), r1.points()))
                .toList();
    }

    @Override
    public PlayerRanking getPlayerRanking(String playerId)
    {
        return calculatePlayerRanking(playerId);
    }

    @Override
    public List<Match> getPlayerMatches(String playerId)
    {
        return rankingRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId)
                .stream()
                .map(this::toMatch)
                .toList();
    }

    private void validateMatchRequest(MatchRequest matchRequest)
    {
        if (matchRequest.player1Id().equals(matchRequest.player2Id()))
        {
            throw new IllegalArgumentException("A player cannot play against themselves");
        }

        if (!matchRequest.winnerId().equals(matchRequest.player1Id()) &&
                !matchRequest.winnerId().equals(matchRequest.player2Id()))
        {
            throw new IllegalArgumentException("Winner must be one of the participating players");
        }
    }

    private PlayerRanking calculatePlayerRanking(String playerId)
    {
        List<MatchEntity> playerMatches = rankingRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId);

        int wins = (int) playerMatches.stream()
                .filter(match -> match.getWinnerId().equals(playerId))
                .count();

        int losses = playerMatches.size() - wins;
        int points = wins * 3;

        return PlayerRanking.create(playerId, points, wins, losses);
    }

    private List<String> getAllPlayerIds()
    {
        return rankingRepository.findAll()
                .stream()
                .flatMap(match -> Stream.of(match.getPlayer1Id(), match.getPlayer2Id()))
                .distinct()
                .toList();
    }

    private Match toMatch(MatchEntity entity)
    {
        return new Match(
                entity.getId(),
                entity.getPlayer1Id(),
                entity.getPlayer2Id(),
                entity.getWinnerId(),
                entity.getMatchDate()
        );
    }
}