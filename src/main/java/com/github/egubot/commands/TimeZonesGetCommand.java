package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.TimeZonesContext;
import com.github.egubot.interfaces.Command;

public class TimeZonesGetCommand implements Command {

	@Override
	public String getName() {
		return "timezones";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		var tz = TimeZonesContext.getServerTimeZones(msg);
		if (tz == null) {
			msg.getChannel().sendMessage("This command must be used in a server.");
			return true;
		}
		String out = tz.formatCurrentTimes();
		if (out == null) {
			msg.getChannel().sendMessage("No time zones configured. Use 'timezones add <ZoneId>' to add some.");
			return true;
		}
		msg.getChannel().sendMessage("```java\nCurrent times:\n" + out + "\n```");
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Get time for preconfigured time zones";
	}
	
	@Override
	public String getCategory() {
		return "Features";
	}
	
	@Override
	public String getUsage() {
		return getName();
	}
}