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
import com.github.egubot.webautomation.Ezgif;
import com.github.egubot.webautomation.GrabYoutubeVideo;

public class WebDriverFacade {
	private static final Logger logger = LogManager.getLogger(WebDriverFacade.class.getName());
	private static final Pattern IMAGE_PATTERN = Pattern.compile("\\.(?:jpg|jpeg|png|mp3|ogg|wav)+",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern YOUTUBE_COMMAND_PATTERN = Pattern.compile("(?i)b-grab(?:\\s?mp3)?\\s*<?([^>]+)>?");

	public static boolean checkCommands(Message msg, String msgText, String lowCaseText) {
		if (lowCaseText.matches("b-insult(?s).*")) {
			checkInsultCommands(msg, lowCaseText);
			return true;
		}

		if (lowCaseText.matches("b-convert(?s).*")) {
			checkEzgifCommands(msg, lowCaseText.contains("b-convert gif"), lowCaseText.contains("b-convert vid"));
			return true;
		}

		if (lowCaseText.matches("b-grab(?s).*")) {
			msg.getChannel().sendMessage("one moment");
			checkGrabCommands(msg, msgText);
			return true;
		}
		return false;
	}

	private static void checkInsultCommands(Message msg, String lowCaseText) {
		String[] options = lowCaseText.replaceFirst("b-insult", "").split(">>");
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

	private static void checkGrabCommands(Message msg, String msgText) {
		try {
			EmbedBuilder embed = null;
			if (msgText.contains("youtu")) {
				embed = getYoutubeLinkEmbed(msgText);
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

	private static EmbedBuilder getYoutubeLinkEmbed(String text) {
		Matcher matcher = YOUTUBE_COMMAND_PATTERN.matcher(text);
		String link = matcher.replaceAll("$1").strip();
		String newLink = null;
		String extension = "";
		if (!link.isBlank() && link.contains("https")) {
			try (GrabYoutubeVideo a = new GrabYoutubeVideo()) {
				if (text.contains("mp3")) {
					extension = ".mp3";
					newLink = a.getAudio(link);

				} else {
					extension = ".mp4";
					newLink = a.getVideo(link);

				}
			}
		}
		if (newLink == null)
			return null;

		return new EmbedBuilder().setAuthor("Click to download " + extension, newLink,
				"https://cdn-icons-png.flaticon.com/256/1384/1384060.png").setDescription("not sus trust me");
	}

	private static void checkEzgifCommands(Message msg, boolean isKnownGif, boolean isKnownVid) {
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

		msg.getChannel().sendMessage("Processing. Will take up to 2 minutes.");
		try (Ezgif a = new Ezgif()) {
			if (!isKnownVid && (isKnownGif || !link.contains(".gif")) && !link.contains("tenor")) {
				msg.reply("Be sure to download it locally before expiry.\n" + a.videoToGif(link));
			} else {
				if (link.contains("tenor") && link.contains(".mp4"))
					msg.reply(link);
				else
					msg.reply("Be sure to download it before expiry.\n" + a.gifToVideo(link));
			}
		} catch (Exception e) {
			logger.error("Failed to get response from ezgif AI.", e);
			msg.reply("Failed :thumbs_down:");
		}
	}

}
