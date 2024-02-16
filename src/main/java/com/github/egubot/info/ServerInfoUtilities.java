package com.github.egubot.info;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

public class ServerInfoUtilities {
	
	public static Server getServer(Message msg) {
		return msg.getServer().get();
	}
}
