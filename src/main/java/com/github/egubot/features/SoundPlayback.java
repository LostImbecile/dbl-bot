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

import com.github.egubot.main.Bot;
import com.github.egubot.shared.ConvertObjects;
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
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

public class SoundPlayback {
	private static final Logger logger = LogManager.getLogger(SoundPlayback.class.getName());
	private static final int MINUTE = 60 * 60 * 1000;

	private static final AudioPlayerManager remotePlayerManager;
	private static final AudioPlayerManager localPlayerManager;

	private static String prefix;

	private static final Pattern prefixPattern;
	private static final Pattern angleBracketPattern;
	private static final User bot;

	private SoundPlayback() {
		// Static class
	}

	static {
		try {
			prefix = Bot.getPrefix();
			prefixPattern = Pattern.compile(Pattern.quote(prefix + "play"), Pattern.CASE_INSENSITIVE);
			angleBracketPattern = Pattern.compile("[<>]");

			remotePlayerManager = new DefaultAudioPlayerManager();
			localPlayerManager = new DefaultAudioPlayerManager();
			
			initialisePlayerManagers();

			bot = Bot.getApi().getYourself();
		} catch (Exception e) {
			logger.error("Couldn't inilialise lavaplayer", e);
			throw e;
		}
	}

	private static void initialisePlayerManagers() {
		AudioSourceManagers.registerRemoteSources(remotePlayerManager);
		AudioSourceManagers.registerLocalSource(localPlayerManager);

		if (ConfigManager.getBooleanProperty("Is_Buffer_Huge")) {
			remotePlayerManager.setFrameBufferDuration(3 * MINUTE);
			localPlayerManager.setFrameBufferDuration(3 * MINUTE);
		} else {
			remotePlayerManager.setFrameBufferDuration(200);
			localPlayerManager.setFrameBufferDuration(200);
		}
		remotePlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
		remotePlayerManager.registerSourceManager(new BandcampAudioSourceManager());
		remotePlayerManager.registerSourceManager(new BeamAudioSourceManager());
		remotePlayerManager.registerSourceManager(new GetyarnAudioSourceManager());
		remotePlayerManager.registerSourceManager(new NicoAudioSourceManager());
		remotePlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		remotePlayerManager.registerSourceManager(new VimeoAudioSourceManager());
		remotePlayerManager.registerSourceManager(new YandexMusicAudioSourceManager());

		localPlayerManager.registerSourceManager(new LocalAudioSourceManager());
	}

	public static boolean checkMusicCommands(Message msg, String lowCaseTxt) {
		try {
			if (lowCaseTxt.startsWith(prefix)) {

				if (lowCaseTxt.contains(prefix + "play")) {
					try {
						SoundPlayback.play(msg);
					} catch (Exception e1) {
						logger.error("Play error", e1);
					}
					return true;
				}

				if (lowCaseTxt.contains(prefix + "cancel")) {
					TrackScheduler.destroy(getServer(msg).getIdAsString());
					return true;
				}

				if (lowCaseTxt.contains(prefix + "skip")) {
					TrackScheduler.skip(getServer(msg).getIdAsString());
					return true;
				}

				if (lowCaseTxt.contains(prefix + "pause")) {
					TrackScheduler.pause(getServer(msg).getIdAsString());
					return true;
				}

				if (lowCaseTxt.contains(prefix + "resume")) {
					TrackScheduler.resume(getServer(msg).getIdAsString());
					return true;
				}

				if (lowCaseTxt.contains(prefix + "buffer big")) {
					remotePlayerManager.setFrameBufferDuration(3 * MINUTE);
					localPlayerManager.setFrameBufferDuration(3 * MINUTE);
					return true;
				}

				if (lowCaseTxt.contains(prefix + "buffer small")) {
					remotePlayerManager.setFrameBufferDuration(200);
					localPlayerManager.setFrameBufferDuration(200);
					return true;
				}

				if (lowCaseTxt.contains(prefix + "info")) {
					getPlaylistInfo(msg);
					return true;
				}

			}
		} catch (Exception e) {
			logger.error(e);
		}

		return false;
	}

	private static void getPlaylistInfo(Message msg) {
		Server server = getServer(msg);
		Map<String, Long> map = TrackScheduler.getPlayListInfo(server.getIdAsString());
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor(server.getName(), null, server.getIcon().get());
		embed.setColor(Color.RED);
		StringBuilder description = new StringBuilder(50);
		for (Entry<String, Long> entry : map.entrySet()) {
			description.append(convertTrackInfo(entry));
			description.append("\n");
		}
		embed.setDescription(description.toString());
		msg.getChannel().sendMessage(embed);
	}

	private static String convertTrackInfo(Entry<String, Long> entry) {
		return GetYoutubeVideoInfo.getName(entry.getKey()) + " - "
				+ ConvertObjects.convertMilliSecondsToTime(entry.getValue());
	}

	private static Server getServer(Message msg) {
		return msg.getServer().get();
	}

	public static void play(Message msg) {
		ServerVoiceChannel channel = getVoiceChannel(msg);
		if (channel == null)
			return;

		String name = getPlayArgument(msg.getContent());

		String serverID = getServer(msg).getIdAsString();
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

	private static void connectToVoiceChannel(Message msg, ServerVoiceChannel channel, String name, String serverID,
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
		});
	}

	private static void loadTracks(Message msg, String name, String serverID, AudioPlayerManager manager) {
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
		String name = message;
		Matcher prefixMatcher = prefixPattern.matcher(name);
		if (prefixMatcher.find()) {
			name = prefixMatcher.replaceFirst("").strip();
		}
		Matcher angleBracketMatcher = angleBracketPattern.matcher(name);
		name = angleBracketMatcher.replaceAll("");
		return name;
	}

}
