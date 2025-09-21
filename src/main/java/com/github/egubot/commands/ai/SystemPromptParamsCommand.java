package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import com.github.egubot.interfaces.Command;

public class SystemPromptParamsCommand implements Command {

	@Override
	public String getName() {
		return "sys params";
	}

	@Override
	public String getDescription() {
		return "Display all available system prompt parameters and their descriptions";
	}

	@Override
	public String getUsage() {
		return getName();
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
		if (!msg.getAuthor().asUser().isPresent()) {
			return false;
		}
		
		if (!msg.getServer().isPresent()) {
			msg.getChannel().sendMessage("This command can only be used in a server.");
			return true;
		}
		
		if (!msg.getServer().get().hasPermission(msg.getAuthor().asUser().get(), PermissionType.ADMINISTRATOR)) {
			msg.getChannel().sendMessage("You need administrator permissions to view system prompt parameters.");
			return true;
		}
		
		StringBuilder params = new StringBuilder();
		params.append("**Available System Prompt Parameters:**\n\n");
		
		params.append("**Global Parameters** (available everywhere):\n");
		params.append("`{date}` - Current date\n");
		params.append("`{botName}` - Bot's base name\n");
		params.append("`{ownerName}` - Bot owner's name\n");
		params.append("`{model}` - AI model being used\n");
		params.append("`{tokenLimit}` - Token limit for the model\n\n");
		
		params.append("**Server-Specific Parameters** (only in server contexts):\n");
		params.append("`{serverId}` - Discord server ID\n");
		params.append("`{serverName}` - Name of the Discord server\n");
		params.append("`{botNickname}` - Bot's nickname in this server (falls back to base name)\n");
		params.append("`{memberCount}` - Number of members in the server\n");
		params.append("`{serverOwner}` - Name of the server owner\n\n");
		
		params.append("**Usage Example:**\n");
		params.append("```\nYou are {botNickname} in {serverName} with {memberCount} members.\n");
		params.append("Today is {date} and you're running on {model}.\n```\n\n");
		
		params.append("Use these parameters in your system prompts with `b-sys set` or `b-sys default set`");
		
		msg.getChannel().sendMessage(params.toString());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}