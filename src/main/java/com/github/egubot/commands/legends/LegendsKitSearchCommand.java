package com.github.egubot.commands.legends;

import java.io.IOException;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.legends.LegendsKitSearch;
import com.github.egubot.interfaces.Command;

public class LegendsKitSearchCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "search kit";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		try {
			if (!arguments.isBlank()) {
				msg.getChannel().sendMessage("Searching; Could take up to 10 minutes.");
				new LegendsKitSearch(arguments, msg.getChannel());
			}else {
				msg.getChannel().sendMessage("Type your keywords or sentences, comma separated.");
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
