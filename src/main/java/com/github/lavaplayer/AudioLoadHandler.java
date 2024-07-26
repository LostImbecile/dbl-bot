package com.github.lavaplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioLoadHandler implements AudioLoadResultHandler {
	private static final Logger logger = LogManager.getLogger(AudioLoadHandler.class.getName());

	Message msg;
	long serverID;

	public AudioLoadHandler(Message msg, long serverID) {
		this.msg = msg;
		this.serverID = serverID;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		TrackScheduler.queue(track, serverID);
		logger.debug("Queued track {}", track.getInfo().uri);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		TrackScheduler.queue(playlist, serverID);

		logger.debug("Queued playlist {} ", playlist.getName());
	}

	@Override
	public void noMatches() {
		msg.reply("Failed to find track :thumbs_down:");
		if (!TrackScheduler.isServerPlaying(serverID)) {
			TrackScheduler.destroy(serverID);
		}
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		msg.reply("Loading of a video failed");
		if (!TrackScheduler.isServerPlaying(serverID)) {
			TrackScheduler.destroy(serverID);
		}
		logger.debug(exception);
	}

}
