package com.github.egubot.managers.reactions;

import java.util.List;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import com.github.egubot.interfaces.ReactionInterceptor;

public class ReactionManager {
	private static final ReactionRegistry registry = ReactionRegistry.getInstance();

	private ReactionManager() {
	}

	public static boolean processReactionAdd(ReactionAddEvent event, Message message, User user, String emoji) throws Exception {
		List<ReactionInterceptor> interceptors = registry.getInterceptors();
		
		for (ReactionInterceptor interceptor : interceptors) {
			if (interceptor.canHandle(emoji, message) && interceptor.handleReactionAdd(event, message, user, emoji)) {
					return true;
				}
			
		}
		
		return false;
	}

	public static boolean processReactionRemove(ReactionRemoveEvent event, Message message, User user, String emoji) throws Exception {
		List<ReactionInterceptor> interceptors = registry.getInterceptors();
		
		for (ReactionInterceptor interceptor : interceptors) {
			if (interceptor.canHandle(emoji, message) && interceptor.handleReactionRemove(event, message, user, emoji)) {
					return true;
				}
			
		}
		
		return false;
	}
}