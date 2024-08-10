package com.github.egubot.features.legends;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.egubot.interfaces.NewsScraper;
import com.github.egubot.objects.legends.LegendsNewsPiece;
import com.github.egubot.shared.Shared;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LegendsNewsScraper implements NewsScraper<LegendsNewsPiece> {
	private static final Logger logger = LogManager.getLogger(LegendsNewsScraper.class.getName());
	private static final String BASE_URL = "https://dblegends.net/news";

	public List<LegendsNewsPiece> scrapeNews(int pageCount) {
		List<LegendsNewsPiece> articles = new ArrayList<>();

		for (int i = 1; i <= pageCount; i++) {
			String url = BASE_URL + (i > 1 ? "?page=" + i : "");
			try {
				Elements newsItems = getNewsItems(url);

				for (Element item : newsItems) {
					LegendsNewsPiece article = getPiece(item);
					if (article != null)
						articles.add(article);
				}
			} catch (IOException e) {
				logger.error("Failed to scrape news from {}: {}", url, e.getMessage());
			}
		}

		return articles;
	}

	private LegendsNewsPiece getPiece(Element item) {
		LegendsNewsPiece article = new LegendsNewsPiece();

		// Extract ID from href attribute
		String href = item.select("a").attr("href");
		try {
			article.setId(Long.parseLong(href.replaceAll("\\D", "")));
		} catch (NumberFormatException e) {
			logger.error("Failed to extract ID from href: {}", href);
			return null;
		}

		// Extract title
		article.setTitle(item.select("h2").text());

		// Extract URL
		article.setUrl(href);

		// Extract banner URL
		article.setBannerUrl(item.select("img").attr("src"));

		// Extract time range
		String timeText = item.select("p").text();
		String[] times = timeText.replace("Start Time: ", "").replace("End Time: ", "").split(" ～ ");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (z)");
		
		ZonedDateTime startTime = ZonedDateTime.parse(times[0], formatter);
		ZonedDateTime endTime = ZonedDateTime.parse(times[1], formatter);
		startTime =  startTime.withZoneSameInstant(Shared.getZoneID());
		endTime = endTime.withZoneSameInstant(Shared.getZoneID());
		
		article.setStartTime(formatter.format(startTime));
		article.setEndTime(formatter.format(endTime));
		return article;
	}

	private static Elements getNewsItems(String url) throws IOException {
		Document doc = getDoc(url);
		return doc.select(".news-item.newzooms");
	}

	private static Document getDoc(String url) throws IOException {
		return Jsoup.connect(url).get();
	}

	@Override
	public LegendsNewsPiece getLatestArticle() {
		try {
			Elements items = getNewsItems(BASE_URL);
			return getPiece(items.get(0));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean isSourceAccessible() {
		try {
			getDoc(BASE_URL);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getSourceName() {
		return "dblegends.net";
	}
}
