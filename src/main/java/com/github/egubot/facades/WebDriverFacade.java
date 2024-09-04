package com.github.egubot.facades;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.webautomation.InsultGenerator;
import com.github.egubot.features.TenorLinkFetcher;
import com.github.egubot.info.MessageInfoUtilities;
import com.github.egubot.webautomation.Ezgif;
import com.github.egubot.webautomation.GrabYoutubeVideo;

public class WebDriverFacade {
	private static final Logger logger = LogManager.getLogger(WebDriverFacade.class.getName());
	private static final Pattern UNSUPPORTED_MEDIA_PATTERN = Pattern.compile("\\.(?:jpg|jpeg|png|mp3|ogg|wav|webp)+",
			Pattern.CASE_INSENSITIVE);
	private static final String YOUTUBE_ICON = "https://cdn-icons-png.flaticon.com/256/1384/1384060.png";

	private WebDriverFacade() {
	}

	public static void checkInsultCommands(Message msg, String text) {
		String[] options = text.split(">>");
		if (options.length < 2) {
			msg.getChannel().sendMessage("Hast thou no target, no foe, or no purpose in mind?");
		} else {
			try (InsultGenerator a = new InsultGenerator()) {
				msg.getChannel().sendMessage("Will be whispered in time.");
				String response = a.getResponse(options[0], options[1]);
				msg.getAuthor().asUser().get().sendMessage(response);
			} catch (Exception e) {
				logger.error("Failed to get response from online AI.", e);
				msg.getAuthor().asUser().get().sendMessage("Perhaps not.");
			}
		}
	}

	public static void checkGrabCommands(Message msg, String text) {
		try {
			EmbedBuilder embed = null;
			if (text.contains("youtu")) {
				embed = getYoutubeLinkEmbed(text);
			}

			if (embed != null)
				msg.reply(embed);
			else
				msg.reply(":thumbs_down:");

		} catch (Exception e) {
			logger.error("Couldn't get video link", e);
			msg.reply("Failed :thumbs_down:");
		}
	}

	public static EmbedBuilder getYoutubeLinkEmbed(String text) {
		String link = text.replace("<", "").replace(">", "");
		String[] result = null;
		String newLink = null;
		String title = "Click To Download ";

		if (!link.isBlank() && link.contains("https")) {
			try (GrabYoutubeVideo a = new GrabYoutubeVideo()) {
				if (text.contains("mp3")) {
					title += "Audio";
					result = a.getAudio(link);

				} else {
					title += "Video";
					result = a.getVideo(link);

				}
			}
		}

		if (result == null)
			return null;

		newLink = result[0];

		if (result[1] == null || result[2] == null) {
			return new EmbedBuilder().setAuthor(title, newLink, YOUTUBE_ICON);
		} else {
			return new EmbedBuilder().setAuthor(title, newLink, YOUTUBE_ICON).setThumbnail(result[1])
					.setDescription(result[2]);
		}
	}

	public static void checkEzgifCommands(Message msg, boolean isGif) {
		CompletableFuture<List<String>> linksFuture;
		if (msg.getReferencedMessage().isPresent()) {
			linksFuture = MessageInfoUtilities.getReferencedMessageLinks(msg)
					.thenApply(optionalLinks -> optionalLinks.orElse(List.of()));
		} else {
			linksFuture = MessageInfoUtilities.getMessageLinks(msg);
		}

		linksFuture.thenAccept(links -> {
			if (links.isEmpty()) {
				msg.getChannel().sendMessage("Convert what?");
				return;
			}

			convertLink(msg, isGif, links);
		});
	}

	private static void convertLink(Message msg, boolean isGif, List<String> links) {
		String link = null;
		
		for (String potentialLink : links) {
		  Matcher matcher = UNSUPPORTED_MEDIA_PATTERN.matcher(potentialLink);
		  if (!potentialLink.isBlank() && potentialLink.contains("https") && !matcher.find()) {
		    link = potentialLink;
		    break;
		  }
		}

		if (link == null) {
		  msg.getChannel().sendMessage("Not a video or gif");
		  return;
		}

		if (link.contains("tenor.com")) {
			if (link.startsWith("https://tenor.com")) {
				String[] results = TenorLinkFetcher.fetchMediaUrls(link);
				msg.reply(results[1]);
			} else if (isGif)
				msg.reply(link);
			else
				msg.reply(link.replace("media.tenor.com", "media1.tenor.com/m").replace("AAAPo/", "AAAAC/")
						.replace(".mp4", ".gif"));

			return;
		}

		msg.getChannel().sendMessage("Processing. Will take up to 2 minutes.");
		try (Ezgif a = new Ezgif()) {
			if (!isGif) {
				msg.reply("Be sure to download it locally before expiry.\n" + a.videoToGif(link));
			} else {
				msg.reply("Be sure to download it before expiry.\n" + a.gifToVideo(link));
			}
		} catch (Exception e) {
			logger.error("Failed to get response from ezgif AI.", e);
			msg.reply("Failed :thumbs_down:");
		}
	}

}
