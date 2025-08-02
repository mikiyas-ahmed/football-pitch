package com.miki.footballpitch.pitch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PitchRepository extends JpaRepository<PitchEntity, String>
{
    List<PitchEntity> findByActiveTrue();
}