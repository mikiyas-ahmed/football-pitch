package com.miki.footballpitch.pitch;

import com.miki.footballpitch.pitch.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pitches")
@RequiredArgsConstructor
class PitchController
{

    private final PitchFacade pitchFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pitch createPitch(@Valid @RequestBody PitchRequest pitchRequest)
    {
        return pitchFacade.createPitch(pitchRequest);
    }

    @GetMapping
    public List<Pitch> getAllPitches()
    {
        return pitchFacade.getAllPitches();
    }

    @GetMapping("/active")
    public List<Pitch> getActivePitches()
    {
        return pitchFacade.getActivePitches();
    }

    @GetMapping("/{pitchId}")
    public Pitch getPitch(@PathVariable String pitchId)
    {
        return pitchFacade.getPitch(pitchId);
    }

    @DeleteMapping("/{pitchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivatePitch(@PathVariable String pitchId)
    {
        pitchFacade.deactivatePitch(pitchId);
    }
}