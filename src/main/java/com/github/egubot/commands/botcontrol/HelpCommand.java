package com.github.egubot.commands.botcontrol;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.handlers.GenericPaginationHandler;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;
import com.github.egubot.managers.commands.CommandRegistry;

public class HelpCommand implements Command {

	public static final String EQUALISE = String.format("%n%80s", "‏‏‎ ").replace(" ", "\u2005");

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getDescription() {
		return "Shows all available commands with descriptions, usage, and categories";
	}

	@Override
	public String getUsage() {
		return getName() + " [category|permission|page]";
	}

	@Override
	public String getCategory() {
		return "Bot Control";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		List<Command> allCommands = new ArrayList<>();
		allCommands.addAll(CommandRegistry.getPrefixCommandmap().values());
		allCommands.addAll(CommandRegistry.getNoPrefixCommandmap().values());
		
		String filter = arguments.trim().toLowerCase();
		List<Command> filteredCommands;
		String title;
		
		if (filter.isEmpty()) {
			filteredCommands = allCommands;
			title = "All Commands";
		} else if (isCategory(filter, allCommands)) {
			filteredCommands = allCommands.stream()
				.filter(cmd -> cmd.getCategory().toLowerCase().equals(filter))
				.collect(Collectors.toList());
			title = "Commands in category: " + capitalize(filter);
		} else if (isPermissionLevel(filter)) {
			PermissionLevel level = PermissionLevel.valueOf(filter.toUpperCase());
			filteredCommands = allCommands.stream()
				.filter(cmd -> cmd.getPermissionLevel() == level)
				.collect(Collectors.toList());
			title = "Commands for: " + level.getDisplayName();
		} else {
			try {
				int page = Integer.parseInt(filter);
				filteredCommands = allCommands;
				title = "All Commands (Page " + page + ")";
			} catch (NumberFormatException e) {
				filteredCommands = allCommands;
				title = "All Commands";
			}
		}
		
		filteredCommands.sort(Comparator.comparing(Command::getCategory)
			.thenComparing(Command::getName));
		
		if (filteredCommands.isEmpty()) {
			msg.getChannel().sendMessage("No commands found for: " + arguments);
			return true;
		}
		
		final String finalTitle = title;
		GenericPaginationHandler<Command> handler = new GenericPaginationHandler<>(
			Bot.getApi(),
			filteredCommands,
			this::createCommandEmbed,
			(currentPage, totalPages) -> String.format("%s - Page %d of %d", finalTitle, currentPage, totalPages),
			5,
			5
		);
		
		String availableCategories = getAvailableCategories(allCommands);
		MessageBuilder messageBuilder = new MessageBuilder()
			.append("**" + title + "**" + EQUALISE)
			.append("Use `" + Bot.getPrefix() + "help [category|permission|page]` to filter commands." + EQUALISE)
			.append("Categories: " + availableCategories + EQUALISE)
			.append("Permissions: everyone, mod, admin, owner");
		
		handler.sendInitialMessage(messageBuilder, msg.getChannel());
		return true;
	}
	
	private EmbedBuilder createCommandEmbed(Command command) {
		EmbedBuilder embed = new EmbedBuilder()
			.setTitle(Bot.getPrefix() + command.getName())
			.addField("Category", command.getCategory(), true)
			.addField("Permission", command.getPermissionLevel().getDisplayName(), true);
		
		if (command.getDescription() != null && !command.getDescription().trim().isEmpty()) {
			embed.setDescription(command.getDescription());
		}
		
		if (command.getUsage() != null && !command.getUsage().trim().isEmpty()) {
			embed.addField("Usage", "`" + Bot.getPrefix() + command.getUsage() + "`", false);
		}
		
		embed.setColor(getColorForPermission(command.getPermissionLevel()));
		
		return embed;
	}
	
	private Color getColorForPermission(PermissionLevel level) {
		switch (level) {
			case EVERYONE: return new Color(0, 255, 0);
			case MOD: return new Color(255, 255, 0);
			case ADMIN: return new Color(255, 128, 0);
			case OWNER: return new Color(255, 0, 0);
			default: return new Color(136, 136, 136);
		}
	}
	
	private boolean isCategory(String filter, List<Command> commands) {
		return commands.stream()
			.anyMatch(cmd -> cmd.getCategory().toLowerCase().equals(filter));
	}
	
	private boolean isPermissionLevel(String filter) {
		return Arrays.stream(PermissionLevel.values())
			.anyMatch(level -> level.name().toLowerCase().equals(filter));
	}
	
	private String getAvailableCategories(List<Command> commands) {
		return commands.stream()
			.map(Command::getCategory)
			.distinct()
			.sorted()
			.collect(Collectors.joining(", "));
	}
	
	private String capitalize(String str) {
		if (str == null || str.isEmpty()) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}