package com.github.egubot.facades;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.webautomation.InsultGenerator;
import com.github.egubot.features.TenorLinkFetcher;
import com.github.egubot.webautomation.Ezgif;
import com.github.egubot.webautomation.GrabYoutubeVideo;

public class WebDriverFacade {
	private static final Logger logger = LogManager.getLogger(WebDriverFacade.class.getName());
	private static final Pattern IMAGE_PATTERN = Pattern.compile("\\.(?:jpg|jpeg|png|mp3|ogg|wav)+",
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

	public static void checkEzgifCommands(Message msg, boolean isKnownGif, boolean isKnownVid) {
		String link;
		List<Embed> embeds;
		List<MessageAttachment> attachments;
		if (msg.getReferencedMessage().isPresent()) {
			Message ref = msg.getReferencedMessage().get();
			embeds = ref.getEmbeds();
			attachments = ref.getAttachments();
		} else {
			try {
				// Wait for embed to show up
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			embeds = msg.getEmbeds();
			attachments = msg.getAttachments();
		}
		if (embeds.isEmpty() && attachments.isEmpty()) {
			msg.getChannel().sendMessage("Convert what?");
			return;
		} else if (embeds.isEmpty()) {
			link = attachments.get(0).getUrl().toString();
		} else if (embeds.get(0).getVideo().isPresent()) {
			link = embeds.get(0).getVideo().get().getUrl().toString();
		} else {
			try {
				link = embeds.get(0).getUrl().get().toExternalForm();
			} catch (Exception e) {
				link = "";
			}
		}
		Matcher matcher = IMAGE_PATTERN.matcher(link);
		if (link.isBlank() || !link.contains("https") || matcher.find()) {
			msg.getChannel().sendMessage("Not a video or gif");
			return;
		}

		if (link.contains("tenor")) {
			String[] results = TenorLinkFetcher.fetchMediaUrls(link);
			if(isKnownVid || link.contains(".mp4"))
				msg.reply(results[0]);
			else
				msg.reply(results[1]);
			return;
		}

		msg.getChannel().sendMessage("Processing. Will take up to 2 minutes.");
		try (Ezgif a = new Ezgif()) {
			if (!isKnownVid && (isKnownGif || !link.contains(".gif"))) {
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
