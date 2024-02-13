package com.github.egubot.webautomation;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GetYoutubeVideoInfo {
	private static final Pattern pattern = Pattern.compile(" - YouTube$");

	public static String getName(String id) {
		Document document;
		try {
			document = Jsoup.connect("https://www.youtube.com/watch?v=" + id).get();
			String title = document.title();
			Matcher matcher = pattern.matcher(title);
			if (matcher.find()) {
				title = matcher.replaceAll("");
			}
			return title;
		} catch (IOException e) {
			return null;
		}
	}
}
