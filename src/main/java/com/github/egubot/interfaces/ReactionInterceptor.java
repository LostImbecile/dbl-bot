package com.github.egubot.interfaces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

public interface ReactionInterceptor extends HasPriority {
	public static final Logger logger = LogManager.getLogger(ReactionInterceptor.class.getName());

	public String getName();

	public boolean handleReactionAdd(ReactionAddEvent event, Message message, User user, String emoji) throws Exception;

	public boolean handleReactionRemove(ReactionRemoveEvent event, Message message, User user, String emoji) throws Exception;

	public default boolean canHandle(String emoji, Message message) {
		return true;
	}
}