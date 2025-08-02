package com.miki.footballpitch.ranking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "matches")
@NoArgsConstructor
@Getter
@Setter
class MatchEntity
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player1_id", nullable = false)
    private String player1Id;

    @Column(name = "player2_id", nullable = false)
    private String player2Id;

    @Column(name = "winner_id", nullable = false)
    private String winnerId;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    public MatchEntity(String player1Id, String player2Id, String winnerId)
    {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = winnerId;
        this.matchDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchEntity that = (MatchEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public String toString()
    {
        return "MatchEntity{" +
                "id=" + id +
                ", player1Id='" + player1Id + '\'' +
                ", player2Id='" + player2Id + '\'' +
                ", winnerId='" + winnerId + '\'' +
                ", matchDate=" + matchDate +
                '}';
    }
}