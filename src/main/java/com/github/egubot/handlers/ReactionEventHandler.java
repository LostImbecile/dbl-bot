package com.github.egubot.handlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.managers.reactions.ReactionManager;
import com.github.egubot.shared.Shared;

public class ReactionEventHandler implements ReactionAddListener, ReactionRemoveListener, Shutdownable {
    private static final Logger logger = LogManager.getLogger(ReactionEventHandler.class.getName());
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    public ReactionEventHandler() {
        Shared.getShutdown().registerShutdownable(this);
    }
    
    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        executorService.submit(() -> handleReactionAdd(event));
    }
    
    @Override
    public void onReactionRemove(ReactionRemoveEvent event) {
        executorService.submit(() -> handleReactionRemove(event));
    }
    
    private void handleReactionAdd(ReactionAddEvent event) {
        try {
            event.requestMessage().thenAccept(message -> {
                User user = event.getUser().orElse(null);
                
                if (user == null || user.isBot()) {
                    return;
                }
                
                String emoji = event.getEmoji().asUnicodeEmoji().orElse(
                    event.getEmoji().asCustomEmoji().map(Nameable::getName).orElse("")
                );
                
                try {
                    ReactionManager.processReactionAdd(event, message, user, emoji);
                } catch (Exception e) {
                    logger.error("Error processing reaction add", e);
                }
            }).exceptionally(ex -> {
                logger.error("Error processing reaction add event", ex);
                return null;
            });
        } catch (Exception e) {
            logger.error("Error in reaction add handler", e);
        }
    }
    
    private void handleReactionRemove(ReactionRemoveEvent event) {
        try {
            event.requestMessage().thenAccept(message -> {
                User user = event.getUser().orElse(null);
                
                if (user == null) {
                    return;
                }
                
                String emoji = event.getEmoji().asUnicodeEmoji().orElse(
                    event.getEmoji().asCustomEmoji().map(Nameable::getName).orElse("")
                );
                
                try {
                    ReactionManager.processReactionRemove(event, message, user, emoji);
                } catch (Exception e) {
                    logger.error("Error processing reaction remove", e);
                }
            }).exceptionally(ex -> {
                logger.error("Error processing reaction remove event", ex);
                return null;
            });
        } catch (Exception e) {
            logger.error("Error in reaction remove handler", e);
        }
    }
    
    @Override
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.error("Reaction Event Handler Executor Service shutdown was forced.");
            }
        } catch (InterruptedException e) {
            logger.error("Reaction Event Handler shutdown failed.", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public int getShutdownPriority() {
        return 10;
    }
}