package com.miki.footballpitch.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface PlayerTypeRepository extends JpaRepository<PlayerTypeEntity, String>
{

    List<PlayerTypeEntity> findByActiveTrue();

    Optional<PlayerTypeEntity> findByCodeAndActiveTrue(String code);
}