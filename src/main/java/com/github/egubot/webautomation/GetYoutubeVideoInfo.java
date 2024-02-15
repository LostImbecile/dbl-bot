package com.github.egubot.webautomation;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.github.egubot.managers.KeyManager;
import com.github.egubot.objects.YoutubeInfo;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.JSONUtilities;

public class GetYoutubeVideoInfo {
	private static final Logger logger = LogManager.getLogger(GetYoutubeVideoInfo.class.getName());
	private static final String API_KEY = KeyManager.getToken("Youtube_API_Key");
	private static final Pattern pattern = Pattern.compile(" - YouTube$");

	private GetYoutubeVideoInfo() {
	}

	public static String getURL(String id) {
		return "https://www.youtube.com/watch?v=" + id;
	}

	public static String getName(String id) {
		if (!API_KEY.isBlank()) {
			try {
				YoutubeInfo info = getVideoInfo(id);
				if (info == null || info.getItems().length == 0)
					return null;
				return info.getItems()[0].getSnippet().getTitle();
			} catch (Exception e) {
				logger.error("Couldn't get video name", e);
				return null;
			}
		} else {
			try {
				return getNameFromWebsite(id);
			} catch (IOException e) {
				return null;
			}
		}
	}

	public static String getThumb(String id) {
		// Preferred due to sheer speed
		return "http://img.youtube.com/vi/" + id + "/0.jpg";
	}

	private static String getNameFromWebsite(String id) throws IOException {
		Document document;
		document = Jsoup.connect("https://www.youtube.com/watch?v=" + id).get();
		String title = document.title();
		Matcher matcher = pattern.matcher(title);
		if (matcher.find()) {
			title = matcher.replaceAll("");
		}
		return title;
	}

	public static String getDefaultThumb(String id) {
		try {
			YoutubeInfo info = getVideoInfo(id);
			if (info == null || info.getItems().length == 0)
				return null;
			return info.getItems()[0].getSnippet().getThumbnails().getDefault().getUrl();
		} catch (Exception e) {
			logger.error("Couldn't get video thumb", e);
			return null;
		}
	}

	public static String getHighThumb(String id) {
		try {
			YoutubeInfo info = getVideoInfo(id);
			if (info == null || info.getItems().length == 0)
				return null;
			return info.getItems()[0].getSnippet().getThumbnails().getHighThumbnail().getUrl();
		} catch (Exception e) {
			logger.error("Couldn't get video thumb", e);
			return null;
		}
	}

	public static String getMaxThumb(String id) {
		try {
			YoutubeInfo info = getVideoInfo(id);
			if (info == null || info.getItems().length == 0)
				return null;
			return info.getItems()[0].getSnippet().getThumbnails().getMaxThumbnail().getUrl();
		} catch (Exception e) {
			logger.error("Couldn't get video thumb", e);
			return null;
		}
	}

	public static YoutubeInfo getVideoInfo(String videoID) throws IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(
					"https://www.googleapis.com/youtube/v3/videos?part=snippet&id=" + videoID + "&key=" + API_KEY);

			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String st = FileUtilities.readInputStream(entity.getContent());

				return JSONUtilities.jsonToClass(st, YoutubeInfo.class);

			}
		} catch (Exception e) {
			logger.error("Couldn't get video info", e);
		}
		return null;
	}

}
