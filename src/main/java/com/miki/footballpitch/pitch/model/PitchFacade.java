package com.miki.footballpitch.pitch.model;

import java.util.List;

public interface PitchFacade
{

    Pitch createPitch(PitchRequest pitchRequest);

    List<Pitch> getAllPitches();

    List<Pitch> getActivePitches();

    Pitch getPitch(String pitchId);

    void deactivatePitch(String pitchId);

    boolean pitchExists(String pitchId);
}