package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.GitHubDetailsGrabber;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.ConvertObjects;
import java.io.InputStream;

public class GetGithubFileContentsCommand implements Command {
	@Override
	public String getName() {
		return "github";
	}

	@Override
	public String getDescription() {
		return "Get the contents of a file from a GitHub repository";
	}

	@Override
	public String getUsage() {
		return getName() + " <github_file_url>";
	}

	@Override
	public String getCategory() {
		return "Development";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		String fileName = GitHubDetailsGrabber.getFileNameFromUrl(arguments);
		if (fileName == null) {
			fileName = "README.md";
		}
		InputStream contents = ConvertObjects
				.stringToInputStream(GitHubDetailsGrabber.getGitHubFileOrReadmeContents(arguments));
		if (contents != null) {
			msg.getChannel().sendMessage(contents, fileName);
		}

		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}