package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.GitHubDetailsGrabber;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.ConvertObjects;
import java.io.InputStream;

public class GetGithubFileStructureCommand implements Command {
	@Override
	public String getName() {
		return "github list";
	}

	@Override
	public String getDescription() {
		return "Get the file structure of a GitHub repository";
	}

	@Override
	public String getUsage() {
		return getName() + " <github_repo_url>";
	}

	@Override
	public String getCategory() {
		return "Development";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		InputStream contents = ConvertObjects
				.stringToInputStream(GitHubDetailsGrabber.getGitHubFileStructure(arguments));
		if (contents != null) {
			msg.getChannel().sendMessage(contents, "file_structure.txt");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}