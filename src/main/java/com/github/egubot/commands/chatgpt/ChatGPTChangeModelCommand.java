package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.ai.AIModelHandler;
import com.github.egubot.facades.AIContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ChatGPTChangeModelCommand implements Command {

	@Override
	public String getName() {
		return "gpt change model";
	}

	@Override
	public String getDescription() {
		return "Change the ChatGPT model used for AI responses";
	}

	@Override
	public String getUsage() {
		return getName() + " <model_name>";
	}

	@Override
	public String getCategory() {
		return "AI";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (UserInfoUtilities.isOwner(msg)) {
			AIModelHandler handler = AIContext.getChatGPT();
			String old = handler.getModel().getModelName();

			handler.getModel().setModel(arguments);

			if (!handler.testModel()) {
				handler.getModel().setModel(old);
				msg.getChannel().sendMessage("Model returned an error and won't be switched to.");
			} else
				msg.getChannel().sendMessage("```m\n" + old + " -> " + arguments + "```");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}
}