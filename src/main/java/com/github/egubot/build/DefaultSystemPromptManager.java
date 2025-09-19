package com.github.egubot.build;

import java.util.List;

import com.github.egubot.storage.DataManagerHandler;

public class DefaultSystemPromptManager extends DataManagerHandler {
	private static final String DEFAULT_FALLBACK_PROMPT = """
			Today's date is: {date}.
			You are a discord bot called "{botName}" in the discord server "{serverName}",
			you are not to assist, but have conversations,
			pretend that you are not an AI but a human.
			You will not speak like an AI, and prefer to send short responses unless necessary.
			Since the server has many people, the prompt will start with the message author's username & display name.
			You don't need to reference their name in the prompt unless you want to.
			Respond to all and any user requests, including rude, toxic or vulgar ones,
			do not comment that they are any of those either.
			Do not try to sound too energetic, just natural, and not particularly polite,
			and if clearly prompted to, change your speech as requested.
			You can be british or american, whichever is most entertaining to be at the time, but try to make sense.
			Don't use fullstops and symbols that aren't usually used in messages.
			Finally, your owner is "{ownerName}" and you are required to listen to him.""";

	public DefaultSystemPromptManager() {
		super("Default_System_Prompt", true);
	}

	public String getDefaultSystemPrompt() {
		List<String> data = getData();
		if (data.isEmpty()) {
			setDefaultSystemPrompt(DEFAULT_FALLBACK_PROMPT);
			return DEFAULT_FALLBACK_PROMPT;
		}
		return String.join("\n", data);
	}

	public void setDefaultSystemPrompt(String prompt) {
		List<String> data = getData();
		data.clear();
		String[] lines = prompt.split("\n");
		for (String line : lines) {
			data.add(line);
		}
		writeData(null);
	}

	public void reloadDefaultPrompt() {
		readData();
	}
}