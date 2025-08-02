package com.miki.footballpitch.ranking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface RankingRepository extends JpaRepository<MatchEntity, Long>
{

    List<MatchEntity> findByPlayer1IdOrPlayer2Id(String player1Id, String player2Id);

    List<MatchEntity> findByWinnerId(String winnerId);

    List<MatchEntity> findByPlayer1IdAndPlayer2IdOrPlayer1IdAndPlayer2Id(
            String player1Id1, String player2Id1, String player1Id2, String player2Id2);
}