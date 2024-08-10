package com.github.egubot.facades;

import java.io.File;
import java.util.List;
import java.util.TimerTask;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.build.LegendsNews;
import com.github.egubot.features.MessageFormats;
import com.github.egubot.features.legends.LegendsNewsScraper;
import com.github.egubot.interfaces.NewsScraper;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.managers.NewsFeedManager;
import com.github.egubot.objects.legends.LegendsNewsPiece;
import com.github.egubot.shared.TimedAction;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.LocalDataManager;

public class LegendsNewsContext implements Shutdownable {
	private static TimedAction newsTimer;
	private static NewsFeedManager<LegendsNewsPiece> newsManager;
	private static LegendsNews registeredServers;
	private static final String CACHE_FILE = LocalDataManager.STORAGE_FOLDER + File.separator
			+ "legends_news_cache.json";
	private static final int NEWS_REFRESH_INTERVAL = ConfigManager.getIntProperty("News_Refresh_Interval_Hour");
	private static final long HOUR = 60 * 60 * 1000;

	public static void initialise() {
		try {
			registeredServers = new LegendsNews();
		} catch (Exception e) {
			registeredServers = null;
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
					List<LegendsNewsPiece> pieces = newsManager.getNewArticles(3);
					if (!pieces.isEmpty()) {
						sendNews(pieces);
					}
				}
			};
			newsTimer.startRecurringTimer(task, false, false);
			
			task.run();
		}
	}

	protected static void sendNews(List<LegendsNewsPiece> pieces) {
		List<String> servers = registeredServers.getData();
		for (String server : servers) {
			List<Messageable> channels = LegendsNews.getChannels(server);
			for (Messageable channel : channels) {
				for (LegendsNewsPiece piece : pieces) {
					channel.sendMessage(LegendsNews.getPings(server), MessageFormats.buildLegendsNewsEmbed(piece));
				}
			}
		}

	}

	public static void shutdownStatic() {
		newsTimer.terminateTimer();
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

	public static void registerServer(Message msg, String args) {
		if (registeredServers != null)
			registeredServers.registerServer(msg, args);
	}

	public static void removeServer(Message msg) {
		if (registeredServers != null)
			registeredServers.removeServer(msg);
	}

	public static void updateServer(Message msg, String args) {
		if (registeredServers != null)
			registeredServers.update(msg, args);
	}

}
