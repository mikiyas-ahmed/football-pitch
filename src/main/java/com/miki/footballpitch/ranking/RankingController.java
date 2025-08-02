package com.miki.footballpitch.ranking;

import com.miki.footballpitch.ranking.model.Match;
import com.miki.footballpitch.ranking.model.MatchRequest;
import com.miki.footballpitch.ranking.model.PlayerRanking;
import com.miki.footballpitch.ranking.model.RankingFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
class RankingController
{

    private final RankingFacade rankingFacade;

    public RankingController(RankingFacade rankingFacade)
    {
        this.rankingFacade = rankingFacade;
    }

    @PostMapping("/matches")
    @ResponseStatus(HttpStatus.CREATED)
    public Match submitMatchResult(@Valid @RequestBody MatchRequest matchRequest)
    {
        return rankingFacade.submitMatchResult(matchRequest);
    }

    @GetMapping("/ranking")
    public List<PlayerRanking> getRankings()
    {
        return rankingFacade.getRankings();
    }

    @GetMapping("/ranking/{playerId}")
    public PlayerRanking getPlayerRanking(@PathVariable String playerId)
    {
        return rankingFacade.getPlayerRanking(playerId);
    }

    @GetMapping("/players/{playerId}/matches")
    public List<Match> getPlayerMatches(@PathVariable String playerId)
    {
        return rankingFacade.getPlayerMatches(playerId);
    }
}