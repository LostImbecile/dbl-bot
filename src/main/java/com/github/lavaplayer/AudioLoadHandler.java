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
	boolean fromSearch = false;

	public AudioLoadHandler(Message msg, long serverID) {
		this.msg = msg;
		this.serverID = serverID;
	}

	public AudioLoadHandler(Message msg, long serverID, boolean fromSearch) {
		this.msg = msg;
		this.serverID = serverID;
		this.fromSearch = fromSearch;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		TrackScheduler.queue(track, serverID);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		if (!fromSearch)
			TrackScheduler.queue(playlist, serverID);
		else
			TrackScheduler.queue(playlist.getTracks().get(0), serverID);
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
		logger.error(exception);
	}

}
