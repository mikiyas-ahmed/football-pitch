package com.miki.footballpitch.player;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_types")
@Data
@NoArgsConstructor
class PlayerTypeEntity
{

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_advance_days", nullable = false)
    private int maxAdvanceDays;

    @Column(nullable = false)
    private boolean active;

    public PlayerTypeEntity(String code, String name, int maxAdvanceDays)
    {
        this.code = code;
        this.name = name;
        this.maxAdvanceDays = maxAdvanceDays;
        this.active = true;
    }
}