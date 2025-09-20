package com.github.egubot.commands.translate;

import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.azure.services.Translate;
import com.github.egubot.facades.TranslateFacade;
import com.github.egubot.interfaces.Command;

public class TranslateCommand implements Command {

	@Override
	public String getName() {
		return "translate";
	}

	@Override
	public String getDescription() {
		return "Translate text between different languages";
	}

	@Override
	public String getUsage() {
		return getName() + " <text>";
	}

	@Override
	public String getCategory() {
		return "Translation";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		Translate translate = TranslateFacade.getTranslate();
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

				if (arguments.isBlank()) {
					msg.getChannel().sendMessage(createTranslateEmbed(msg, translate));
				} else {
					msg.getChannel().sendMessage(translate.post(arguments, true),
							createTranslateEmbed(msg, translate));
				}

			}
		} catch (IOException e1) {
			logger.error("Failed to translate.", e1);
			msg.getChannel().sendMessage("Failed to connect to endpoint :thumbs_down:");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
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