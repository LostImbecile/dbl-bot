package com.github.lavaplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioLoadHandler implements AudioLoadResultHandler {
	private static final Logger logger = LogManager.getLogger(AudioLoadHandler.class.getName());
	AudioPlayer player;
	TrackScheduler trackScheduler;
	Message msg;
	String serverID;

	public AudioLoadHandler(TrackScheduler trackScheduler, Message msg) {
		this.trackScheduler = trackScheduler;
		this.player = trackScheduler.getPlayer();
		this.serverID = trackScheduler.getServerID();
		this.msg = msg;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		trackScheduler.queue(track, serverID);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		trackScheduler.queue(playlist, serverID);
	}

	@Override
	public void noMatches() {
		msg.reply("Failed to find track :thumbs_down:");
		if (TrackScheduler.isServerPlayListEmpty(serverID)) {
			trackScheduler.disconnect();
		}

	}

	@Override
	public void loadFailed(FriendlyException exception) {
		msg.reply(exception.getMessage());
		logger.error(exception);
	}

}
