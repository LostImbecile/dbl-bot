package com.github.egubot.commands.gemini;

import java.util.List;

import org.javacord.api.entity.message.Message;

import com.github.egubot.ai.AIModelHandler;
import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.ConvertObjects;

public class GeminiListModelsCommand implements Command {

	@Override
	public String getName() {
		return "gem list";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		try {
			AIModelHandler handler = AIContext.getGemini();
			List<String> list = handler.getModelList();
			
			if (list == null) {
				msg.getChannel().sendMessage("Failed to retrieve models list - API error");
				return true;
			}
			
			if (list.isEmpty()) {
				msg.getChannel().sendMessage("No models available");
				return true;
			}
			
			String modelsText = ConvertObjects.listToText(list, "\n");
			String response = "```m\n" + modelsText + "```";
			
			if (response.length() > 1900) {
				String[] parts = modelsText.split("\n");
				StringBuilder currentPart = new StringBuilder("```m\n");
				
				for (String model : parts) {
					if (currentPart.length() + model.length() + 5 > 1900) {
						currentPart.append("```");
						msg.getChannel().sendMessage(currentPart.toString());
						currentPart = new StringBuilder("```m\n" + model + "\n");
					} else {
						currentPart.append(model).append("\n");
					}
				}
				
				if (currentPart.length() > 5) {
					currentPart.append("```");
					msg.getChannel().sendMessage(currentPart.toString());
				}
			} else {
				msg.getChannel().sendMessage(response);
			}
			
		} catch (Exception e) {
			msg.getChannel().sendMessage("Error retrieving models: " + e.getMessage());
		}
		
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}