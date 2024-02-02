package com.github.egubot.facades;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.azure.services.Translate;
import com.github.egubot.shared.FileUtilities;

public class TranslateFacade {
	private static final Logger logger = LogManager.getLogger(TranslateFacade.class.getName());
	private Translate translate = new Translate();
	private boolean isTranslateOn = false;

	public boolean checkCommands(Message msg, String lowCaseTxt) {
		if (isTranslateOn) {

			try {
				if (lowCaseTxt.length() < 140 && !translate.detectLanguage(lowCaseTxt, true).matches("en|Error.*")) {
					msg.getChannel().sendMessage(translate.post(lowCaseTxt, true));
				}

			} catch (IOException e1) {
				logger.error("Failed to translate.", e1);
			}

		}
		if (lowCaseTxt.matches("b-translate(?s).*")) {
			if (lowCaseTxt.equals("b-translate set on")) {
				isTranslateOn = true;
				return true;
			}
			if (lowCaseTxt.equals("b-translate set off")) {
				isTranslateOn = false;
				return true;
			}
			if (lowCaseTxt.contains("b-translate set")) {
				String st = lowCaseTxt.replaceFirst("b-translate set", "").strip();
				if (st.contains("-")) {
					String[] toFrom = st.split("-");
					translate.setFrom(toFrom[0]);
					translate.setTo(toFrom[1]);
				} else {
					translate.setTo(st);
					translate.setFrom("");
				}
				return true;
			}
			if (lowCaseTxt.contains("b-translate languages")) {

				try {
					msg.getChannel().sendMessage(FileUtilities.toInputStream(Translate.getTranslateLanguages()),
							"languages.txt");
				} catch (IOException e1) {
					msg.getChannel().sendMessage("Failed to send :thumbs_down");
				}

				return true;
			}

			try {
				if (msg.getMessageReference().isPresent()) {

					Message ref = msg.getMessageReference().get().getMessage().get();
					String content = ref.getContent();
					if (content.isBlank()) {
						msg.getChannel().sendMessage(createTranslateEmbed(ref, translate));
					} else {
						msg.getChannel().sendMessage(translate.post(content, true),
								createTranslateEmbed(ref, translate));
					}
				} else {
					String content = lowCaseTxt.replaceFirst("b-translate", "").strip();

					if (content.isBlank()) {
						msg.getChannel().sendMessage(createTranslateEmbed(msg, translate));
					} else {
						msg.getChannel().sendMessage(translate.post(content, true),
								createTranslateEmbed(msg, translate));

					}

				}
			} catch (IOException e1) {
				logger.error("Failed to translate.", e1);
				msg.getChannel().sendMessage("Failed to connect to endpoint :thumbs_down:");
			}

			return true;
		}
		return false;
	}
	
	public static EmbedBuilder[] createTranslateEmbed(Message msg, Translate translate) {
		String content;
		List<Embed> embeds = msg.getEmbeds();
		EmbedBuilder[] translatedEmbeds = new EmbedBuilder[10];
		
		if (!embeds.isEmpty()) {
			Embed embed;
			boolean isError = true;
			for (int i = 0; i < embeds.size() && i < 10; i++) {
				embed = embeds.get(i);
				translatedEmbeds[i] = new EmbedBuilder();

				try {
					content = embed.getDescription().get();
					content = translate.post(content, true);
					translatedEmbeds[i].setDescription(content);
					isError = false;
				} catch (Exception e) {

				}

				try {
					content = embed.getAuthor().get().getName();
					content = translate.post(content, false);
					translatedEmbeds[i].setAuthor(content);
					isError = false;
				} catch (Exception e) {
				}

				try {
					content = embed.getTitle().get();
					content = translate.post(content, false);
					translatedEmbeds[i].setTitle(content);
					isError = false;
				} catch (Exception e) {
				}

				try {
					translatedEmbeds[i].setThumbnail(embed.getThumbnail().get().getUrl().toExternalForm());
				} catch (Exception e) {
				}

				try {
					translatedEmbeds[i].setImage(embed.getImage().get().getUrl().toExternalForm());
				} catch (Exception e) {
				}
				try {
					translatedEmbeds[i].setColor(embed.getColor().get());
				} catch (Exception e) {
				}

			}
			if (isError) {
				msg.getChannel().sendMessage("Error: Translating Embed Failed.");
				return new EmbedBuilder[0];
			}
			return translatedEmbeds;
		} 
		return new EmbedBuilder[0];
	}
}
