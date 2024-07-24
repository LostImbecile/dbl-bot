package com.github.lavaplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerVoiceChannel;

import com.github.egubot.info.ServerInfoUtilities;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.track.YoutubeAudioTrack;

public class TrackScheduler extends AudioEventAdapter {
	private static final Logger logger = LogManager.getLogger(TrackScheduler.class.getName());
	private static final Map<Long, AudioPlaylist> playlists = new HashMap<>();
	private static final Map<Long, AudioPlayer> players = new HashMap<>();

	private AudioPlayer player;
	private long serverID;

	public TrackScheduler(AudioPlayer player, long serverID) {
		this.player = player;
		this.serverID = serverID;
		players.put(serverID, player);
	}

	public static boolean isServerPlayListEmpty(long serverID) {
		return playlists.get(serverID) == null || playlists.get(serverID).getTracks().isEmpty();
	}

	public static boolean isServerPlaying(long serverID) {
		AudioPlayer player = getServerAudioPlayer(serverID);
		if (player == null) {
			return false;
		}
		return getServerAudioPlayer(serverID).getPlayingTrack() != null;
	}

	public static AudioPlayer getServerAudioPlayer(long serverID) {
		return players.getOrDefault(serverID, null);
	}

	public static void queue(AudioTrack track, long serverID) {
		try {
			AudioPlaylist playlist = playlists.get(serverID);
			if (playlist == null) {
				ArrayList<AudioTrack> list = new ArrayList<>();
				list.add(track);
				playlists.put(serverID, new BasicAudioPlaylist(serverID + "", list, track, false));
			} else {
				playlist.getTracks().add(track);
			}

			if (getServerAudioPlayer(serverID) != null && !isServerPlaying(serverID)) {
				getServerAudioPlayer(serverID).playTrack(track);
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	public static void queue(AudioPlaylist incomingPlayList, long serverID) {
		try {
			AudioPlaylist playlist = playlists.get(serverID);
			int maxVideos = Math.min(20, incomingPlayList.getTracks().size() - 1);
			if (playlist == null) {
				AudioPlaylist newList = new BasicAudioPlaylist(serverID + "",
						incomingPlayList.getTracks().subList(0, maxVideos), null, false);
				playlists.put(serverID, newList);
			} else {
				playlist.getTracks().addAll(incomingPlayList.getTracks().subList(0, maxVideos));
				playlists.put(serverID, playlist);
			}

			if (getServerAudioPlayer(serverID) != null && !isServerPlaying(serverID))
				getServerAudioPlayer(serverID).playTrack(incomingPlayList.getTracks().get(0));
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	public static Map<String, Long> getPlayListInfo(long serverID) {
		AudioPlaylist list = playlists.get(serverID);
		if (list == null || list.getTracks().isEmpty()) {
			return Collections.emptyMap();
		} else {
			List<AudioTrack> tracks = list.getTracks();
			Map<String, Long> trackInfo = new LinkedHashMap<>();
			for (int i = 0; i < tracks.size() && i < 10; i++) {
				AudioTrack audioTrack = tracks.get(i);
				if (audioTrack.getSourceManager() instanceof YoutubeAudioSourceManager) {
					YoutubeAudioTrack ytTrack = (YoutubeAudioTrack) audioTrack;
					String name = ytTrack.getInfo().title;
					String url = ytTrack.getInfo().uri;
					trackInfo.put("(" + i + ") [" + name + "](" + url + ")", audioTrack.getDuration());
				} else
					trackInfo.put("(" + i + ") " + audioTrack.getIdentifier(), audioTrack.getDuration());
			}

			return trackInfo;
		}
	}

	public static AudioTrack getCurrentTrack(long serverID) {
		AudioPlayer player = getServerAudioPlayer(serverID);
		if (player != null)
			return player.getPlayingTrack();
		return null;
	}

	public static int getCurrentTrackIndex(long serverID) {
		AudioPlaylist list = playlists.get(serverID);
		if (list != null && !list.getTracks().isEmpty()) {
			return list.getTracks().indexOf(getCurrentTrack(serverID));
		}
		return -1;
	}

	public static void destroy(long serverID) {
		playlists.remove(serverID);
		if (getServerAudioPlayer(serverID) != null)
			getServerAudioPlayer(serverID).destroy();
		players.remove(serverID);
		disconnect(serverID);
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

	public static void skip(long serverID, int amount) {
		goToNextTrack(serverID, amount);
		resume(serverID);
	}

	public static void pause(long serverID) {
		if (getServerAudioPlayer(serverID) != null) {
			getServerAudioPlayer(serverID).setPaused(true);
		}
	}

	public static void resume(long serverID) {
		if (getServerAudioPlayer(serverID) != null) {
			getServerAudioPlayer(serverID).setPaused(false);
		}
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			goToNextTrack(track);
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

	private void goToNextTrack(AudioTrack track) {
		AudioPlaylist playlist = playlists.get(serverID);
		if (player != null && playlist != null) {
			List<AudioTrack> list = playlist.getTracks();
			int currentIndex = getCurrentTrackIndex(track);
			int nextIndex = 1 + currentIndex;

			if (nextIndex < list.size() && currentIndex != -1) {
				player.playTrack(list.get(nextIndex));
				playlist.getTracks().remove(currentIndex);
				return;
			}
		}

		destroy(serverID);
	}

	private int getCurrentTrackIndex(AudioTrack track) {
		AudioPlaylist list = playlists.get(serverID);
		return list.getTracks().indexOf(track);
	}

	private static void goToNextTrack(long serverID, int i) {
		AudioPlayer serverPlayer = getServerAudioPlayer(serverID);
		AudioPlaylist playlist = playlists.get(serverID);
		if (serverPlayer != null && playlist != null) {
			List<AudioTrack> list = playlist.getTracks();

			int currentIndex = getCurrentTrackIndex(serverID);
			int nextIndex = i + currentIndex;

			if (nextIndex < list.size() && currentIndex != -1) {
				serverPlayer.playTrack(list.get(nextIndex));
				return;
			}
		}
		destroy(serverID);
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		goToNextTrack(track);
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		goToNextTrack(track);
	}

	public AudioPlayer getPlayer() {
		return player;
	}

	public void setPlayer(AudioPlayer player) {
		this.player = player;
	}

	public Long getServerID() {
		return serverID;
	}

	public void setServerID(long serverID) {
		this.serverID = serverID;
	}

	public static Map<Long, AudioPlaylist> getPlaylists() {
		return playlists;
	}

	public static void disconnect(long serverID) {
		try {
			ServerVoiceChannel channel = ServerInfoUtilities.getConnectedVoiceChannel(serverID);
			if (channel == null)
				return;
			channel.disconnect();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
