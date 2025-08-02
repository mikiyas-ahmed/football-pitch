package com.miki.footballpitch.player;

import com.miki.footballpitch.player.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest
{

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTypeRepository playerTypeRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void shouldRegisterPlayerSuccessfully()
    {
        PlayerRegistrationRequest request = createValidPlayerRequest();
        PlayerEntity savedPlayer = createPlayerEntity("PL1", "John Doe", "john@test.com", "MEMBER");
        PlayerTypeEntity memberType = createPlayerTypeEntity("MEMBER", "Club Member", 14);

        mockPlayerNotExists();
        mockEmailNotExists();
        mockPlayerTypeExists("MEMBER", memberType);
        mockPlayerSave(savedPlayer);

        Player result = playerService.registerPlayer(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("PL1");
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@test.com");
        assertThat(result.type()).isEqualTo("MEMBER");
        verifyPlayerSaved();
    }

    @Test
    void shouldThrowExceptionWhenPlayerIdAlreadyExists()
    {
        PlayerRegistrationRequest request = createValidPlayerRequest();

        mockPlayerExists("PL1");

        assertThatThrownBy(() -> playerService.registerPlayer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player with ID PL1 already exists");

        verifyNoPlayerSaved();
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists()
    {
        PlayerRegistrationRequest request = createValidPlayerRequest();

        mockPlayerNotExists();
        mockEmailExists("john@test.com");

        assertThatThrownBy(() -> playerService.registerPlayer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player with email john@test.com already exists");

        verifyNoPlayerSaved();
    }

    @Test
    void shouldThrowExceptionWhenPlayerTypeDoesNotExist()
    {
        PlayerRegistrationRequest request = createValidPlayerRequest();

        mockPlayerNotExists();
        mockEmailNotExists();
        mockPlayerTypeNotExists("MEMBER");

        assertThatThrownBy(() -> playerService.registerPlayer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid player type: MEMBER");

        verifyNoPlayerSaved();
    }

    @Test
    void shouldGetPlayerSuccessfully()
    {
        PlayerEntity playerEntity = createPlayerEntity("PL1", "John Doe", "john@test.com", "MEMBER");

        mockPlayerFound("PL1", playerEntity);

        Player result = playerService.getPlayer("PL1");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("PL1");
        assertThat(result.name()).isEqualTo("John Doe");
        verifyPlayerSearched("PL1");
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotFound()
    {
        mockPlayerNotFound("PL1");

        assertThatThrownBy(() -> playerService.getPlayer("PL1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player not found: PL1");
    }

    @Test
    void shouldGetAllPlayersSuccessfully()
    {
        List<PlayerEntity> playerEntities = List.of(
                createPlayerEntity("PL1", "John Doe", "john@test.com", "MEMBER"),
                createPlayerEntity("PL2", "Jane Smith", "jane@test.com", "GUEST")
        );

        mockAllPlayersFound(playerEntities);

        List<Player> result = playerService.getAllPlayers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Player::id).containsExactly("PL1", "PL2");
        verifyAllPlayersSearched();
    }

    @Test
    void shouldReturnTrueWhenPlayerExists()
    {
        mockPlayerExists("PL1");

        boolean result = playerService.playerExists("PL1");

        assertThat(result).isTrue();
        verifyPlayerExistenceChecked("PL1");
    }

    @Test
    void shouldReturnFalseWhenPlayerDoesNotExist()
    {
        mockPlayerNotExists();

        boolean result = playerService.playerExists("PL1");

        assertThat(result).isFalse();
        verifyPlayerExistenceChecked("PL1");
    }

    @Test
    void shouldGetPlayerMaxAdvanceDaysSuccessfully()
    {
        PlayerEntity playerEntity = createPlayerEntity("PL1", "John Doe", "john@test.com", "MEMBER");
        PlayerTypeEntity memberType = createPlayerTypeEntity("MEMBER", "Club Member", 14);

        mockPlayerFound("PL1", playerEntity);
        mockPlayerTypeExists("MEMBER", memberType);

        int result = playerService.getPlayerMaxAdvanceDays("PL1");

        assertThat(result).isEqualTo(14);
        verifyPlayerSearched("PL1");
        verifyPlayerTypeSearched("MEMBER");
    }

    @Test
    void shouldThrowExceptionWhenGettingMaxAdvanceDaysForNonExistentPlayer()
    {
        mockPlayerNotFound("PL1");

        assertThatThrownBy(() -> playerService.getPlayerMaxAdvanceDays("PL1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player not found: PL1");
    }

    @Test
    void shouldGetActivePlayerTypesSuccessfully()
    {
        List<PlayerTypeEntity> activeTypes = List.of(
                createPlayerTypeEntity("MEMBER", "Club Member", 14),
                createPlayerTypeEntity("GUEST", "Guest Player", 3)
        );

        mockActivePlayerTypesFound(activeTypes);

        List<PlayerType> result = playerService.getActivePlayerTypes();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PlayerType::code).containsExactly("MEMBER", "GUEST");
        verifyActivePlayerTypesSearched();
    }

    @Test
    void shouldGetPlayerTypeSuccessfully()
    {
        PlayerTypeEntity memberType = createPlayerTypeEntity("MEMBER", "Club Member", 14);

        mockPlayerTypeExists("MEMBER", memberType);

        PlayerType result = playerService.getPlayerType("MEMBER");

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("MEMBER");
        assertThat(result.name()).isEqualTo("Club Member");
        assertThat(result.maxAdvanceDays()).isEqualTo(14);
        verifyPlayerTypeSearched("MEMBER");
    }

    // Helper methods for mocking
    private void mockPlayerNotExists()
    {
        when(playerRepository.existsById(any())).thenReturn(false);
    }

    private void mockPlayerExists(String playerId)
    {
        when(playerRepository.existsById(playerId)).thenReturn(true);
    }

    private void mockEmailNotExists()
    {
        when(playerRepository.existsByEmail(any())).thenReturn(false);
    }

    private void mockEmailExists(String email)
    {
        when(playerRepository.existsByEmail(email)).thenReturn(true);
    }

    private void mockPlayerTypeExists(String code, PlayerTypeEntity entity)
    {
        when(playerTypeRepository.findByCodeAndActiveTrue(code)).thenReturn(Optional.of(entity));
    }

    private void mockPlayerTypeNotExists(String code)
    {
        when(playerTypeRepository.findByCodeAndActiveTrue(code)).thenReturn(Optional.empty());
    }

    private void mockPlayerSave(PlayerEntity savedPlayer)
    {
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(savedPlayer);
    }

    private void mockPlayerFound(String playerId, PlayerEntity entity)
    {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(entity));
    }

    private void mockPlayerNotFound(String playerId)
    {
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());
    }

    private void mockAllPlayersFound(List<PlayerEntity> entities)
    {
        when(playerRepository.findAll()).thenReturn(entities);
    }

    private void mockActivePlayerTypesFound(List<PlayerTypeEntity> entities)
    {
        when(playerTypeRepository.findByActiveTrue()).thenReturn(entities);
    }

    // Helper methods for verification
    private void verifyPlayerSaved()
    {
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    private void verifyNoPlayerSaved()
    {
        verify(playerRepository, never()).save(any(PlayerEntity.class));
    }

    private void verifyPlayerSearched(String playerId)
    {
        verify(playerRepository).findById(playerId);
    }

    private void verifyPlayerExistenceChecked(String playerId)
    {
        verify(playerRepository).existsById(playerId);
    }

    private void verifyAllPlayersSearched()
    {
        verify(playerRepository).findAll();
    }

    private void verifyPlayerTypeSearched(String code)
    {
        verify(playerTypeRepository).findByCodeAndActiveTrue(code);
    }

    private void verifyActivePlayerTypesSearched()
    {
        verify(playerTypeRepository).findByActiveTrue();
    }

    // Helper methods for test data creation
    private PlayerRegistrationRequest createValidPlayerRequest()
    {
        return new PlayerRegistrationRequest("PL1", "John Doe", "john@test.com", "MEMBER");
    }

    private PlayerEntity createPlayerEntity(String id, String name, String email, String type)
    {
        PlayerEntity entity = new PlayerEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setEmail(email);
        entity.setPlayerTypeCode(type);
        entity.setRegistrationDate(LocalDateTime.now());
        return entity;
    }

    private PlayerTypeEntity createPlayerTypeEntity(String code, String name, int maxAdvanceDays)
    {
        PlayerTypeEntity entity = new PlayerTypeEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setMaxAdvanceDays(maxAdvanceDays);
        entity.setActive(true);
        return entity;
    }
}