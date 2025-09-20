package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.Attributes;
import com.github.egubot.shared.utils.FileUtilities;
import com.github.egubot.shared.utils.JSONUtilities;

public class AttributesSendCommand implements Command{

	@Override
	public String getName() {
		return "send attributes";
	}

	@Override
	public String getDescription() {
		return "Display available attributes for auto-response system configuration";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Auto-Response";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
	
	@Override
	public boolean execute(Message msg, String arguments) {
		msg.getChannel().sendMessage(
				FileUtilities.toInputStream(JSONUtilities.toJsonPrettyPrint(new Attributes(), Attributes.class)),
				"Attributes.txt");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}


}