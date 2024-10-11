package com.github.egubot.facades;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.build.LegendsMaintenance;
import com.github.egubot.build.LegendsNews;
import com.github.egubot.features.legends.LegendsEmbedBuilder;
import com.github.egubot.features.legends.LegendsNewsScraper;
import com.github.egubot.interfaces.NewsScraper;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.managers.NewsFeedManager;
import com.github.egubot.objects.legends.LegendsNewsPiece;
import com.github.egubot.shared.TimedAction;
import com.github.egubot.shared.utils.MessageUtils;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.LocalDataManager;

public class LegendsNewsContext implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(LegendsNewsContext.class.getName());
	private static TimedAction newsTimer;
	private static NewsFeedManager<LegendsNewsPiece> newsManager;
	private static LegendsNews registeredNewsServers;
	private static LegendsMaintenance registeredMaintenanceServers;
	private static final String CACHE_FILE = LocalDataManager.STORAGE_FOLDER + File.separator
			+ "legends_news_cache.json";
	private static final int NEWS_REFRESH_INTERVAL = ConfigManager.getIntProperty("News_Refresh_Interval_Hour");
	private static final long HOUR = 60 * 60 * 1000;

	public static void initialise() {
		try {
			registeredNewsServers = new LegendsNews();
			registeredMaintenanceServers = new LegendsMaintenance();
		} catch (Exception e) {
			registeredNewsServers = null;
			registeredMaintenanceServers = null;
			logger.error("Failed to initialise Legends News Context", e);
			return;
		}
		NewsScraper<LegendsNewsPiece> scraper = new LegendsNewsScraper();

		newsManager = new NewsFeedManager<>(scraper, LegendsNewsPiece[].class, CACHE_FILE);
		if (newsManager.getlatestArticle() == null) {
			newsManager = null;
		} else {
			int interval = NEWS_REFRESH_INTERVAL > 1 ? NEWS_REFRESH_INTERVAL : 3;
			ConfigManager.setIntProperty("News_Refresh_Interval_Hour", interval);

			newsTimer = new TimedAction(interval * HOUR, null, null);
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					if (!registeredNewsServers.getData().isEmpty()) {
						logger.debug("Fetching Legends News");
						List<LegendsNewsPiece> pieces = newsManager.getNewArticles(3);
						if (!pieces.isEmpty()) {
							sendNews(pieces);
						}
					}
				}
			};
			newsTimer.startRecurringTimer(task, false, false);

			new Thread(task).start();
		}

	}

	protected static void sendNews(List<LegendsNewsPiece> pieces) {
		List<String> newsServers = registeredNewsServers.getData();
		LegendsNewsPiece maintenance = null;
		for (LegendsNewsPiece piece : pieces) {
			if (piece.getDescription() != null) {
				maintenance = piece;
				pieces.remove(piece);
				break;
			}
		}

		logger.debug("Sending {} Legends News Pieces for {} Servers", pieces.size(), newsServers.size());
		List<EmbedBuilder> newsEmbeds = new ArrayList<>();
		for (LegendsNewsPiece piece : pieces) {
			newsEmbeds.add(LegendsEmbedBuilder.buildLegendsNewsEmbed(piece));
		}
		for (String server : newsServers) {
			List<Messageable> channels = MessageUtils.getChannels(server);
			String pings = MessageUtils.getPings(server);
			for (Messageable channel : channels) {
				for (int i = 0; i < newsEmbeds.size(); i++) {
					EmbedBuilder embed = newsEmbeds.get(i);
					if (i == 0)
						channel.sendMessage(pings, embed);
					else
						channel.sendMessage(embed);
				}
			}
		}

		if (maintenance != null) {
			List<String> maintenanceServers = registeredMaintenanceServers.getData();
			logger.debug("Sending Legends Maintenance Notice for {}", maintenanceServers.size());

			EmbedBuilder maintenanceEmbed = LegendsEmbedBuilder.buildLegendsNewsEmbed(maintenance);
			for (String server : maintenanceServers) {
				List<Messageable> channels = MessageUtils.getChannels(server);
				String pings = MessageUtils.getPings(server);
				for (Messageable channel : channels) {
					channel.sendMessage(pings, maintenanceEmbed);
				}
			}
		}

	}

	public static void shutdownStatic() {
		if (newsTimer != null)
			newsTimer.terminateTimer();
		if (registeredNewsServers != null)
			registeredNewsServers.shutdown();
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static NewsFeedManager<LegendsNewsPiece> getNewsManager() {
		return newsManager;
	}

	public static void registerNewsServer(Message msg, String args) {
		if (registeredNewsServers != null)
			registeredNewsServers.registerServer(msg, args);
	}

	public static void removeNewsServer(Message msg) {
		if (registeredNewsServers != null)
			registeredNewsServers.removeServer(msg);
	}

	public static void updateNewsServer(Message msg, String args) {
		if (registeredNewsServers != null)
			registeredNewsServers.update(msg, args);
	}

	public static void registerMaintenanceServer(Message msg, String args) {
		if (registeredMaintenanceServers != null)
			registeredMaintenanceServers.registerServer(msg, args);
	}

	public static void removeMaintenanceServer(Message msg) {
		if (registeredMaintenanceServers != null)
			registeredMaintenanceServers.removeServer(msg);
	}

	public static void updateMaintenanceServer(Message msg, String args) {
		if (registeredMaintenanceServers != null)
			registeredMaintenanceServers.update(msg, args);
	}

}
