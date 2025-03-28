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
