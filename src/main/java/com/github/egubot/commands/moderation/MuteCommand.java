package com.github.egubot.commands.moderation;

import java.time.Duration;
import java.util.List;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.DateUtils;
import com.github.egubot.shared.utils.MessageUtils;

public class MuteCommand implements Command {
	public static final String TICK = "✅";
	public static final String CROSS = "❌";

	@Override
	public String getName() {
		return "mute";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isAdmin(msg)) {
			List<String> userIDs = MessageUtils.getPingedUsers(arguments);
			if (userIDs.isEmpty()) {
				msg.getChannel().sendMessage("Who?");
				return true;
			}
			Server server = ServerInfoUtilities.getServer(msg);
			if(!server.canYouTimeoutUsers()) {
				msg.getChannel().sendMessage("I don't have the perms");
				return true;
			}
			
			String[] args = arguments.split("\\s+");
			if (args.length < 2) {
				msg.getChannel().sendMessage("Timing out user for 10m (default).");
				server.timeoutUser(UserInfoUtilities.getUserById(userIDs.get(0)), Duration.ofMinutes(10));
			} else if (DateUtils.isValidDelay(args[1])) {
				Duration muteDur = DateUtils.parseDelayString(args[1]);
				if (Duration.ofDays(28).compareTo(muteDur) < 0) {
					msg.getChannel().sendMessage("Timing out user for " + args[1]);
					server.timeoutUser(UserInfoUtilities.getUserById(userIDs.get(0)), muteDur);
				} else {
					msg.getChannel().sendMessage("Timing out user for 28d");
					server.timeoutUser(UserInfoUtilities.getUserById(userIDs.get(0)), Duration.ofDays(28));
				}
			} else {
				msg.getChannel().sendMessage("Invalid Delay. Format: 0w0d0h0m0s (max of 4 weeks)");
			}

		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
