package com.github.egubot.commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.MessageUtils;

public class KickCommand implements Command {
	public static final String TICK = "✅";
	public static final String CROSS = "❌";

	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String getDescription() {
		return "Kick a user from the server";
	}

	@Override
	public String getUsage() {
		return getName() + " <@user>";
	}

	@Override
	public String getCategory() {
		return "Moderation";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.MOD;
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
			if(!server.canYouKickUsers()) {
				msg.getChannel().sendMessage("I don't have the perms");
				return true;
			}
			msg.getChannel().sendMessage("React with ✅ to confirm. You have 10 seconds.").thenAcceptAsync(message -> {
				message.addReactions(TICK, CROSS);

				ReactionAddListener reactionAddListener = event -> {
					User user = event.requestUser().join();
					if (user.isBot())
						return;

					switch (event.getEmoji().asUnicodeEmoji().orElse("")) {
					case TICK:
						if (server.canYouKickUsers()
								&& UserInfoUtilities.isUserEqual(msg.getAuthor(), user.getIdAsString())) {
							for (String userId : userIDs) {
								User userToKick = UserInfoUtilities.getUserById(userId);
								if (userToKick != null)
									server.kickUser(userToKick);
							}
							message.delete();
						}
						return;
					case CROSS:
						if (UserInfoUtilities.isUserEqual(msg.getAuthor(), user.getIdAsString())) {
							userIDs.clear();
							message.delete();
						}
						return;
					default:
						return;
					}

				};

				message.addReactionAddListener(reactionAddListener).removeAfter(12, TimeUnit.SECONDS)
						.addRemoveHandler(message::delete);
			});
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}