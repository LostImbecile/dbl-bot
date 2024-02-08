package com.github.egubot.facades;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.Embed;

import com.github.egubot.webautomation.AIResponseGenerator;
import com.github.egubot.webautomation.Ezgif;

public class WebDriverFacade {
	private static final Logger logger = LogManager.getLogger(WebDriverFacade.class.getName());
	private static final Pattern IMAGE_PATTERN = Pattern.compile("\\.(?:jpg|jpeg|png|mp3|ogg|wav)+",
			Pattern.CASE_INSENSITIVE);

	public static boolean checkCommands(Message msg, String lowCaseText) {
		if (lowCaseText.matches("b-insult(?s).*")) {
			String[] options = lowCaseText.replaceFirst("b-insult", "").split(">>");
			if (options.length < 2) {
				msg.getChannel().sendMessage("Hast thou no target, no foe, or no purpose in mind?");
			} else {
				try (AIResponseGenerator a = new AIResponseGenerator()) {
					msg.getChannel().sendMessage("Will be whispered in time.");
					String response = a.getResponse(options[0], options[1]);
					msg.getAuthor().asUser().get().sendMessage(response);
				} catch (Exception e) {
					logger.error("Failed to get response from online AI.", e);
					msg.getAuthor().asUser().get().sendMessage("Perhaps not.");
				}
			}
			return true;
		}

		if (lowCaseText.matches("b-convert(?s).*")) {
			if (lowCaseText.contains("b-convert gif")) {
				checkEzgifCommands(msg, true, false);
			} else if (lowCaseText.contains("b-convert vid")) {
				checkEzgifCommands(msg, false, true);
			} else {
				checkEzgifCommands(msg, false, false);
			}
			return true;
		}
		return false;
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
				msg.reply("Be sure to download it before expiry.\n" + a.videoToGIF(link));
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
