package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;
import com.github.egubot.main.Main;
import com.github.egubot.main.Restart;

public class RestartCommand implements Command {

	@Override
	public String getName() {
		return "restart";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (arguments.isBlank() || arguments.equals(Bot.getYourself().getMentionTag())) {
			boolean isOwner = UserInfoUtilities.isOwner(msg);

			if (isOwner) {
				msg.getChannel().sendMessage("Restarting...").join();
				Main.logger.warn("Restart message invoked.");
				Restart.restart();
			} else {
				msg.getChannel().sendMessage("<a:no:1195656310356717689>");
			}
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
