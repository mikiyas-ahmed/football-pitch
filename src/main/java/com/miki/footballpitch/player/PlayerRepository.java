package com.miki.footballpitch.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PlayerRepository extends JpaRepository<PlayerEntity, String>
{
    boolean existsByEmail(String email);
}