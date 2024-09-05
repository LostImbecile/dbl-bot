package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.AutoRespond;
import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.interfaces.Command;

public class ResponseToggleCommand implements Command {

	@Override
	public String getName() {
		return "response toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		AutoRespond resp = AutoRespondContext.getAutoRespond(msg);
		resp.setDisabled(!resp.isDisabled());
		if(resp.isDisabled())
			msg.getChannel().sendMessage("Disabled :ok_hand:");
		else
			msg.getChannel().sendMessage("Enabled :ok_hand:");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
