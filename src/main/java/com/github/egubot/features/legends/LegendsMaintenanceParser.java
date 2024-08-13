package com.github.egubot.features.legends;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegendsMaintenanceParser {
	private LegendsMaintenanceParser() {
	}

	public static String getMaintenanceDetails(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Element baseDiv = doc.selectFirst("div.base");

		if (baseDiv == null) {
			return null;
		}

		List<MaintenanceInfo> maintenanceInfos = new ArrayList<>();
		Elements children = baseDiv.children();

		String currentTopic = "";
		Pattern dateTimePattern = Pattern.compile("(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2})");

		String generalMaintenanceStart = null;

		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);

			if (child.hasClass("topic")) {
				currentTopic = child.text();
			} else if (child.is("strong")) {
				Matcher matcher = dateTimePattern.matcher(child.text());
				if (matcher.find()) {
					String startTime = matcher.group(1);
					String discordStartTimestamp = convertToDiscordTimestamp(startTime);

					if (generalMaintenanceStart == null) {
						generalMaintenanceStart = discordStartTimestamp;
						continue;
					}

					// Look for the end time in the next elements
					String endTime = null;
					for (int j = i + 1; j < children.size(); j++) {
						Element nextChild = children.get(j);
						i++;
						if (nextChild.is("strong")) {
							Matcher endMatcher = dateTimePattern.matcher(nextChild.text());
							if (endMatcher.find()) {
								endTime = endMatcher.group(1);
								break;
							}
						}
					}

					if (endTime != null) {
						String discordEndTimestamp = convertToDiscordTimestamp(endTime);
						maintenanceInfos
								.add(new MaintenanceInfo(currentTopic, discordStartTimestamp, discordEndTimestamp));
					}
				}
			}
		}

		// Print the formatted results
		StringBuilder result = new StringBuilder();
		result.append("**General Maintenance Start:**\n ").append(generalMaintenanceStart).append("\n\n");

		Map<String, String> groupedTopics = new HashMap<>();
		for (MaintenanceInfo info : maintenanceInfos) {
			String key = info.startTime + " ~ " + info.endTime;
			if (groupedTopics.containsKey(key)) {
				String existingTopics = groupedTopics.get(key);
				groupedTopics.put(key, existingTopics + ", " + info.topic);
			} else {
				groupedTopics.put(key, info.topic);
			}
		}
		
		for (Map.Entry<String, String> entry : groupedTopics.entrySet()) {
			result.append("**").append(entry.getValue()).append(":**\n ");
			result.append(entry.getKey()).append("\n\n");
		}

		return result.toString();
	}

	private static class MaintenanceInfo {
		String topic;
		String startTime;
		String endTime;

		MaintenanceInfo(String topic, String startTime, String endTime) {
			this.topic = topic;
			this.startTime = startTime;
			this.endTime = endTime;
		}
	}

	private static String convertToDiscordTimestamp(String dateTimeStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
		ZoneId jstZone = ZoneId.of("Asia/Tokyo");
		Instant instant = localDateTime.atZone(jstZone).toInstant();
		long epochSeconds = instant.getEpochSecond();
		return "<t:" + epochSeconds + ":f>";
	}

	public static void main(String[] args) throws IOException {
		System.out.println(LegendsMaintenanceParser.getMaintenanceDetails("https://dblegends.net/news/4016"));
	}
}