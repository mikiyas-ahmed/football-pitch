package com.miki.footballpitch.player;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
class PlayerEntity
{

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "player_type_code", nullable = false)
    private String playerTypeCode;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    public PlayerEntity(String id, String name, String email, String playerTypeCode)
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.playerTypeCode = playerTypeCode;
        this.registrationDate = LocalDateTime.now();
    }
}