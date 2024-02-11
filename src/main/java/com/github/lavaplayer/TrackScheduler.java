package com.github.lavaplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerVoiceChannel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

public class TrackScheduler extends AudioEventAdapter {
	private static final Logger logger = LogManager.getLogger(TrackScheduler.class.getName());
	static final Map<String, AudioPlaylist> playlists = new HashMap<>();

	private AudioPlayer player;
	private String serverID;
	private ServerVoiceChannel channel = null;

	public TrackScheduler(AudioPlayer player, String serverID) {
		this.player = player;
		this.serverID = serverID;
	}

	public static boolean isServerPlayListEmpty(String serverID) {
		return playlists.get(serverID) == null || playlists.get(serverID).getTracks().isEmpty();
	}

	public void queue(AudioTrack track, String serverID) {
		try {
			AudioPlaylist playlist = playlists.get(serverID);
			if (playlist == null) {
				ArrayList<AudioTrack> list = new ArrayList<>();
				list.add(track);
				playlists.put(serverID, new BasicAudioPlaylist(serverID, list, track, false));
			} else {
				playlist.getTracks().add(track);
			}

			if (player.getPlayingTrack() == null)
				player.playTrack(track);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	public void queue(AudioPlaylist incomingPlayList, String serverID) {
		try {
			AudioPlaylist playlist = playlists.get(serverID);
			int maxVideos = Math.min(20, incomingPlayList.getTracks().size() - 1);
			if (playlist == null) {
				AudioPlaylist newList = new BasicAudioPlaylist(serverID,
						incomingPlayList.getTracks().subList(0, maxVideos), null, false);
				playlists.put(serverID, newList);
			} else {
				playlist.getTracks().addAll(incomingPlayList.getTracks().subList(0, maxVideos));
				playlists.put(serverID, playlist);
			}

			if (player.getPlayingTrack() == null)
				player.playTrack(incomingPlayList.getTracks().get(0));
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	public static void destroy(String serverID) {
		playlists.remove(serverID);
	}

	@Override
	public void onPlayerPause(AudioPlayer player) {
		// Player was paused
	}

	@Override
	public void onPlayerResume(AudioPlayer player) {
		// Player was resumed
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		// A track started playing
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			goToNextTrack(player, track);
		}

		// endReason == FINISHED: A track finished or died by an exception (mayStartNext
		// = true).
		// endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
		// endReason == STOPPED: The player was stopped.
		// endReason == REPLACED: Another track started playing while this had not
		// finished
		// endReason == CLEANUP: Player hasn't been queried for a while, if you want you
		// can put a
		// clone of this back to your queue
	}

	private void goToNextTrack(AudioPlayer player, AudioTrack track) {
		List<AudioTrack> list = playlists.get(serverID).getTracks();
		list.remove(track);
		if (!list.isEmpty())
			player.playTrack(list.get(0));
		else
			channel.disconnect();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		goToNextTrack(player, track);
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		goToNextTrack(player, track);
	}

	public AudioPlayer getPlayer() {
		return player;
	}

	public void setPlayer(AudioPlayer player) {
		this.player = player;
	}

	public String getServerID() {
		return serverID;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public static Map<String, AudioPlaylist> getPlaylists() {
		return playlists;
	}

	public ServerVoiceChannel getChannel() {
		return channel;
	}

	public void setChannel(ServerVoiceChannel channel) {
		this.channel = channel;
	}

	public void disconnect() {
		if (channel != null)
			channel.disconnect();
	}
}
