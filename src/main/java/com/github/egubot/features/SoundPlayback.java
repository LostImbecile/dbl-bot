package com.github.egubot.features;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.main.Bot;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.webautomation.GetYoutubeVideoInfo;
import com.github.lavaplayer.AudioLoadHandler;
import com.github.lavaplayer.LavaplayerAudioSource;
import com.github.lavaplayer.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.track.YoutubeAudioTrack;

public class SoundPlayback {
	private static final Logger logger = LogManager.getLogger(SoundPlayback.class.getName());

	private static final AudioPlayerManager remotePlayerManager;
	private static final AudioPlayerManager localPlayerManager;

	private static final Pattern angleBracketPattern;
	private static final User bot;

	private SoundPlayback() {
	}

	static {
		try {
			angleBracketPattern = Pattern.compile("[<>]");

			remotePlayerManager = new DefaultAudioPlayerManager();
			localPlayerManager = new DefaultAudioPlayerManager();

			initialisePlayerManagers();

			bot = Bot.getYourself();
		} catch (Exception e) {
			logger.error("Couldn't inilialise lavaplayer", e);
			throw e;
		}
	}

	private static void initialisePlayerManagers() {
		AudioSourceManagers.registerRemoteSources(remotePlayerManager);
		AudioSourceManagers.registerLocalSource(localPlayerManager);

		int bufferSize = ConfigManager.getIntProperty("Player_Buffer_Size_MS");

		if (bufferSize < 0) {
			bufferSize = 400;
			ConfigManager.setIntProperty("Player_Buffer_Size_MS", bufferSize);
		}
		updateBufferDuration(bufferSize);

		remotePlayerManager.registerSourceManager(new YoutubeAudioSourceManager()); // lavalink youtube source
		remotePlayerManager.registerSourceManager(new BandcampAudioSourceManager());
		remotePlayerManager.registerSourceManager(new BeamAudioSourceManager());
		remotePlayerManager.registerSourceManager(new GetyarnAudioSourceManager());
		remotePlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		remotePlayerManager.registerSourceManager(new VimeoAudioSourceManager());

		localPlayerManager.registerSourceManager(new LocalAudioSourceManager());
	}

	public static void updateBufferDuration(int bufferSize) {
		remotePlayerManager.setFrameBufferDuration(bufferSize);
		localPlayerManager.setFrameBufferDuration(bufferSize);
	}

	public static void getCurrentTrackInfo(Message msg) {
		Server server = ServerInfoUtilities.getServer(msg);
		AudioTrack track = TrackScheduler.getCurrentTrack(server.getId());
		
		EmbedBuilder embed = new EmbedBuilder();
		if (track.getSourceManager() instanceof YoutubeAudioSourceManager) {
			YoutubeAudioTrack ytTrack = (YoutubeAudioTrack) track;
			embed.setAuthor(ytTrack.getInfo().title, ytTrack.getInfo().uri,
					server.getIcon().get());
			embed.setImage(ytTrack.getInfo().artworkUrl);
		}
		
		embed.setColor(Color.RED);

		addProgressBar(track, embed);
		msg.getChannel().sendMessage(embed);
	}

	private static void addProgressBar(AudioTrack track, EmbedBuilder embed) {
		String positionString = ConvertObjects.convertMilliSecondsToTime(track.getPosition());
		String durationString = ConvertObjects.convertMilliSecondsToTime(track.getDuration());

		int maxWidth = 90;
		double progress = (double) track.getPosition() / track.getDuration();
		int finishedWidth = (int) Math.ceil(maxWidth * progress);
		String finishedString = String.format("%" + finishedWidth / 2 + "s", " ").replace(" ", "-");
		String progressBar = String.format("[%s%" + (maxWidth - finishedWidth) + "s]", finishedString, " ").replace(" ",
				"\u2005");

		embed.setDescription("**" + positionString + " " + progressBar + " " + durationString + "**");
	}

	public static void getPlaylistInfo(Message msg) {
		Server server = ServerInfoUtilities.getServer(msg);
		Map<String, Long> map = TrackScheduler.getPlayListInfo(server.getId());
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor(server.getName(), null, server.getIcon().get());
		embed.setColor(Color.RED);
		StringBuilder description = new StringBuilder(50);
		for (Entry<String, Long> entry : map.entrySet()) {
			description.append(convertTrackInfoToText(entry));
			description.append("\n");
		}
		embed.setDescription(description.toString());
		msg.getChannel().sendMessage(embed);
	}

	private static String convertTrackInfoToText(Entry<String, Long> entry) {
		String name = GetYoutubeVideoInfo.getName(entry.getKey());
		String url = GetYoutubeVideoInfo.getURL(entry.getKey());
		String hyperLink = "[" + name + "](" + url + ")";
		String duration = ConvertObjects.convertMilliSecondsToTime(entry.getValue());
		return hyperLink + " - " + duration;
	}

	public static void play(Message msg) {
		ServerVoiceChannel channel = getVoiceChannel(msg);
		if (channel == null)
			return;

		String name = getPlayArgument(msg.getContent());

		long serverID = ServerInfoUtilities.getServerID(msg);
		AudioPlayerManager manager = getManager(name);

		boolean isNewPlayer = false;
		AudioPlayer player;
		if (TrackScheduler.getServerAudioPlayer(serverID) == null) {
			player = manager.createPlayer();
			TrackScheduler trackScheduler = new TrackScheduler(player, serverID);
			player.addListener(trackScheduler);
			isNewPlayer = true;
		} else {
			player = TrackScheduler.getServerAudioPlayer(serverID);
		}

		try {
			if (isNewPlayer || !channel.isConnected(bot)) {
				connectToVoiceChannel(msg, channel, name, serverID, manager, player);
			} else {
				loadTracks(msg, name, serverID, manager);
			}
		} catch (Exception e) {
			TrackScheduler.destroy(serverID);
		}

	}

	private static void connectToVoiceChannel(Message msg, ServerVoiceChannel channel, String name, long serverID,
			AudioPlayerManager manager, AudioPlayer player) {
		channel.connect().thenAccept(audioConnection -> {

			channel.addServerVoiceChannelMemberLeaveListener(event -> {
				if (channel.getConnectedUserIds().size() < 2 || !channel.isConnected(bot)) {
					TrackScheduler.destroy(serverID);
				}
			});

			AudioSource source = new LavaplayerAudioSource(player);
			audioConnection.setAudioSource(source);
			loadTracks(msg, name, serverID, manager);
		}).exceptionally(e -> {
		    logger.error("Failed to connect to voice channel", e);
		    return null;
		});;
	}

	private static void loadTracks(Message msg, String name, long serverID, AudioPlayerManager manager) {
		if (!name.contains("search"))
			manager.loadItem(name, new AudioLoadHandler(msg, serverID));
		else
			manager.loadItem(name, new AudioLoadHandler(msg, serverID, true));
	}

	private static AudioPlayerManager getManager(String name) {
		AudioPlayerManager manager;

		if (name.contains("https") || name.contains("search"))
			manager = remotePlayerManager;
		else
			manager = localPlayerManager;
		return manager;
	}

	private static ServerVoiceChannel getVoiceChannel(Message msg) {
		ServerVoiceChannel channel;
		try {
			channel = msg.getAuthor().getConnectedVoiceChannel().get();
		} catch (NoSuchElementException e) {
			msg.getChannel().sendMessage("Connect to a voice channel first.");
			return null;
		}
		return channel;
	}

	private static String getPlayArgument(String message) {
		String name = message.strip();

		Matcher angleBracketMatcher = angleBracketPattern.matcher(name);
		name = angleBracketMatcher.replaceAll("");
		return name;
	}

	public static AudioPlayerManager getRemoteplayermanager() {
		return remotePlayerManager;
	}

	public static AudioPlayerManager getLocalplayermanager() {
		return localPlayerManager;
	}

}
