package com.miki.footballpitch.pitch;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pitches")
@Data
@NoArgsConstructor
class PitchEntity
{

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active;

    public PitchEntity(String id, String name)
    {
        this.id = id;
        this.name = name;
        this.active = true;
    }
}