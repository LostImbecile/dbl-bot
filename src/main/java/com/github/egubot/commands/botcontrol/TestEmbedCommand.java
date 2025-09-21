package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.interfaces.Command;

public class TestEmbedCommand implements Command {

	@Override
	public String getName() {
		return "embed";
	}

	@Override
	public String getDescription() {
		return "Test embed functionality with sample content";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Development";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.OWNER;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!arguments.isBlank()) {
			try {
				String type = arguments.substring(0, arguments.indexOf(" "));
				String sub = arguments.substring(arguments.indexOf(" ") + 1);

				switch (type) {
				case "footer":
					msg.getChannel().sendMessage(new EmbedBuilder().setFooter(sub));
					break;
				case "image":
					msg.getChannel().sendMessage(new EmbedBuilder().setImage(sub));
					break;
				case "thumb":
					msg.getChannel().sendMessage(new EmbedBuilder().setThumbnail(sub));
					break;
				case "title":
					msg.getChannel().sendMessage(new EmbedBuilder().setTitle(sub));
					break;
				default:
					msg.getChannel().sendMessage(new EmbedBuilder().setDescription(arguments));
					break;
				}
			} catch (Exception e) {
				msg.getChannel().sendMessage(new EmbedBuilder().setDescription(arguments));
			}

		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}