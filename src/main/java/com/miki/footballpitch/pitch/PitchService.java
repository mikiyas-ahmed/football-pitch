package com.miki.footballpitch.pitch;

import com.miki.footballpitch.pitch.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class PitchService implements PitchFacade
{

    private final PitchRepository pitchRepository;

    @Override
    @Transactional
    public Pitch createPitch(PitchRequest pitchRequest)
    {
        if (pitchRepository.existsById(pitchRequest.id()))
        {
            throw new IllegalArgumentException("Pitch with ID " + pitchRequest.id() + " already exists");
        }

        PitchEntity pitchEntity = new PitchEntity(pitchRequest.id(), pitchRequest.name());
        PitchEntity savedPitch = pitchRepository.save(pitchEntity);
        return toPitch(savedPitch);
    }

    @Override
    public List<Pitch> getAllPitches()
    {
        return pitchRepository.findAll()
                .stream()
                .map(this::toPitch)
                .toList();
    }

    @Override
    public List<Pitch> getActivePitches()
    {
        return pitchRepository.findByActiveTrue()
                .stream()
                .map(this::toPitch)
                .toList();
    }

    @Override
    public Pitch getPitch(String pitchId)
    {
        return pitchRepository.findById(pitchId)
                .map(this::toPitch)
                .orElseThrow(() -> new IllegalArgumentException("Pitch not found: " + pitchId));
    }

    @Override
    @Transactional
    public void deactivatePitch(String pitchId)
    {
        PitchEntity pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new IllegalArgumentException("Pitch not found: " + pitchId));

        pitch.setActive(false);
        pitchRepository.save(pitch);
    }

    @Override
    public boolean pitchExists(String pitchId)
    {
        return pitchRepository.existsById(pitchId);
    }

    private Pitch toPitch(PitchEntity entity)
    {
        return new Pitch(entity.getId(), entity.getName(), entity.isActive());
    }
}