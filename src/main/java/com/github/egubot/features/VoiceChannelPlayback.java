package com.github.egubot.features;

import java.util.NoSuchElementException;

import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.main.BotApi;
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

public class VoiceChannelPlayback {
	private static final AudioPlayerManager remotePlayerManager = new DefaultAudioPlayerManager();
	private static final AudioPlayerManager localPlayerManager = new DefaultAudioPlayerManager();

	static {
		AudioSourceManagers.registerRemoteSources(remotePlayerManager);
		AudioSourceManagers.registerLocalSource(localPlayerManager);

		remotePlayerManager.setFrameBufferDuration(20000);
		remotePlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
		remotePlayerManager.registerSourceManager(new BandcampAudioSourceManager());
		remotePlayerManager.registerSourceManager(new BeamAudioSourceManager());
		remotePlayerManager.registerSourceManager(new GetyarnAudioSourceManager());
		remotePlayerManager.registerSourceManager(new NicoAudioSourceManager());
		remotePlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		remotePlayerManager.registerSourceManager(new VimeoAudioSourceManager());
		remotePlayerManager.registerSourceManager(new YandexMusicAudioSourceManager());

		localPlayerManager.setFrameBufferDuration(20000);
		localPlayerManager.registerSourceManager(new LocalAudioSourceManager());

	}

	public static void play(Message msg) {
		String name = msg.getContent().replace("b-play", "").replace("<", "").replace(">", "").strip();
		String serverID = msg.getServer().get().getIdAsString();
		AudioPlayerManager manager;

		if (name.contains("https") || name.contains("search"))
			manager = remotePlayerManager;
		else
			manager = localPlayerManager;
		AudioPlayer player = manager.createPlayer();

		TrackScheduler trackScheduler = new TrackScheduler(player, serverID);
		player.addListener(trackScheduler);

		manager.loadItem(name, new AudioLoadHandler(trackScheduler, msg));

		try {
			ServerVoiceChannel channel = msg.getAuthor().getConnectedVoiceChannel().get();
			if (!channel.isConnected(BotApi.getApi().getYourself()) || TrackScheduler.isServerPlayListEmpty(serverID)) {
				channel.connect().thenAccept(audioConnection -> {
					AudioSource source = new LavaplayerAudioSource(player);
					audioConnection.setAudioSource(source);
					trackScheduler.setChannel(channel);
				});
			}

			channel.addServerVoiceChannelMemberLeaveListener(event -> {
				if (channel.getConnectedUserIds().size() < 2 || !channel.isConnected(BotApi.getApi().getYourself())) {
					channel.disconnect();
					player.destroy();
					TrackScheduler.destroy(serverID);
					trackScheduler.disconnect();
				}
			});
		} catch (NoSuchElementException e) {
			msg.getChannel().sendMessage("Connect to a voice channel first.");
		} catch (Exception e) {
			player.destroy();
			TrackScheduler.destroy(serverID);
			trackScheduler.disconnect();
		}

	}

}
