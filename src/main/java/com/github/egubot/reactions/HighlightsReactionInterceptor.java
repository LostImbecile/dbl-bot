package com.github.egubot.reactions;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import com.github.egubot.interfaces.ReactionInterceptor;

public class HighlightsReactionInterceptor implements ReactionInterceptor {

	@Override
	public String getName() {
		return "highlights";
	}

	@Override
	public boolean handleReactionAdd(ReactionAddEvent event, Message message, User user, String emoji) throws Exception {
		logger.debug("Reaction added: {} by user {} on message: {}", emoji, user.getDiscriminatedName(), message.getContent());
		
		return false;
	}

	@Override
	public boolean handleReactionRemove(ReactionRemoveEvent event, Message message, User user, String emoji) throws Exception {
		logger.debug("Reaction removed: {} by user {} on message: {}", emoji, user.getDiscriminatedName(), message.getContent());
		
		return false;
	}

	@Override
	public boolean canHandle(String emoji, Message message) {
		return true;
	}

	@Override
	public int getPriority() {
		return 100;
	}
}