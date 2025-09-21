package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.interfaces.DiscordTimerTask;

public class ParrotCommand implements Command, DiscordTimerTask {

	@Override
	public String getName() {
		return "parrot";
	}

	@Override
	public String getDescription() {
		return "Make the bot repeat a message";
	}

	@Override
	public String getUsage() {
		return getName() + " <message>";
	}

	@Override
	public String getCategory() {
		return "Bot Control";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.OWNER;
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isOwner(msg))
			msg.getChannel().sendMessage(arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public boolean execute(long targetChannel, String arguments) throws Exception {
		ServerInfoUtilities.getTextableRegularServerChannel(targetChannel).sendMessage(arguments);
		return true;
	}

}