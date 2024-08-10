package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;
import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.features.MessageFormats;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.legends.LegendsNewsPiece;

public class LegendsNewsCommand implements Command{

	@Override
	public String getName() {
		return "news";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (LegendsCommandsContext.getNewsManager() != null) {
			LegendsNewsPiece piece = LegendsCommandsContext.getNewsManager().getlatestArticle();
			if (piece != null) {
				msg.getChannel().sendMessage(MessageFormats.buildLegendsNewsEmbed(piece));
			}
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
