package com.github.egubot.features;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TenorLinkFetcher {
	private static final Pattern gifPattern = Pattern.compile("(https://media1\\.tenor\\.com/[^\" ]*?\\.gif)");
	private static final Pattern mp4Pattern = Pattern.compile("(https://media\\.tenor\\.com/[^\" ]*?\\.mp4)");
	
	private TenorLinkFetcher() {
	}

	/**
	 * Fetches the gif and mp4 links from the tenor url
	 * 
	 * @param tenorUrl
	 * 
	 * @return gif and mp4 links as 0 and 1 respectively
	 */
	public static String[] fetchMediaUrls(String tenorUrl) {
		String[] links = new String[2];
		links[0] = null;
		links[1] = null;
		try {
			URL url = new URL(tenorUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			try (InputStream inputStream = connection.getInputStream()) {

				StringBuilder content = new StringBuilder();
				byte[] buffer = new byte[4096];
				int bytesRead;

				boolean foundGif = false;
				boolean foundMp4 = false;

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					// Append newly read data to the content string builder
					content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));

					// Check if content matches any pattern
					Matcher gifMatcher = gifPattern.matcher(content);
					Matcher mp4Matcher = mp4Pattern.matcher(content);

					if (gifMatcher.find()) {
						links[0] = gifMatcher.group().replace("\\u002F", "/");
						foundGif = true;
					}

					if (mp4Matcher.find()) {
						links[1] = mp4Matcher.group().replace("\\u002F", "/");
						foundMp4 = true;
					}

					if (foundGif && foundMp4)
						break;

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return links;
	}
}
