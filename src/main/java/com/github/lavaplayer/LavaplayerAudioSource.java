package com.github.lavaplayer;

import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;

import com.github.egubot.main.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

public class LavaplayerAudioSource extends AudioSourceBase {

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    /**
     * Creates a new lavaplayer audio source.
     *
     * @param audioPlayer An audio player from Lavaplayer.
     */
    public LavaplayerAudioSource(AudioPlayer audioPlayer) {
        super(Bot.getApi());
        this.audioPlayer = audioPlayer;
    }

    @Override
    public byte[] getNextFrame() {
        if (lastFrame == null) {
            return new byte[0];
        }
        return applyTransformers(lastFrame.getData());
    }

    @Override
    public boolean hasFinished() {
        return false;
    }

    @Override
    public boolean hasNextFrame() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public AudioSource copy() {
        return new LavaplayerAudioSource(audioPlayer);
    }
}
