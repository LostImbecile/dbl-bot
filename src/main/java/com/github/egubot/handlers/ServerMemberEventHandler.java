package com.github.egubot.handlers;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;

import com.github.egubot.features.ServerNotificationsFeature;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.utils.DateUtils;

public class ServerMemberEventHandler implements ServerMemberJoinListener, ServerMemberLeaveListener, Shutdownable {
    private static final Logger logger = LogManager.getLogger(ServerMemberEventHandler.class.getName());
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    
    public ServerMemberEventHandler() {
        Shared.getShutdown().registerShutdownable(this);
    }
    
    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        executorService.submit(() -> handleMemberJoin(event));
    }
    
    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        executorService.submit(() -> handleMemberLeave(event));
    }
    
    private void handleMemberJoin(ServerMemberJoinEvent event) {
        try {
            Server server = event.getServer();
            long serverID = server.getId();
            
            if (!ServerNotificationsFeature.isJoinEnabled(serverID)) {
                return;
            }
            
            TextChannel channel = ServerNotificationsFeature.getChannel(server, serverID);
            if (channel == null) {
                return;
            }
            
            User user = event.getUser();
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Member Joined")
                .setDescription(user.getDisplayName(server) + " has joined the server!")
                .setColor(Color.GREEN)
                .setThumbnail(user.getAvatar())
                .addField("User", user.getMentionTag(), true)
                .addField("Account Created", DateUtils.getDate(user.getCreationTimestamp()), true)
                .addField("Member Count", String.valueOf(server.getMemberCount()), true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + user.getId());
            
            channel.sendMessage(embed);
            
        } catch (Exception e) {
            logger.error("Error handling member join event", e);
        }
    }
    
    private void handleMemberLeave(ServerMemberLeaveEvent event) {
        try {
            Server server = event.getServer();
            long serverID = server.getId();
            
            if (!ServerNotificationsFeature.isLeaveEnabled(serverID)) {
                return;
            }
            
            TextChannel channel = ServerNotificationsFeature.getChannel(server, serverID);
            if (channel == null) {
                return;
            }
            
            User user = event.getUser();
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Member Left")
                .setDescription(user.getDisplayName(server) + " has left the server.")
                .setColor(Color.RED)
                .setThumbnail(user.getAvatar())
                .addField("User", user.getDiscriminatedName(), true)
                .addField("Account Created", DateUtils.getDate(user.getCreationTimestamp()), true)
                .addField("Member Count", String.valueOf(server.getMemberCount()), true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + user.getId());
            
            channel.sendMessage(embed);
            
        } catch (Exception e) {
            logger.error("Error handling member leave event", e);
        }
    }
    
    @Override
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.error("Server Member Event Handler Executor Service shutdown was forced.");
            }
        } catch (InterruptedException e) {
            logger.error("Server Member Event Handler shutdown failed.", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public int getShutdownPriority() {
        return 10;
    }
}