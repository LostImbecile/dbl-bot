package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.TimeZonesContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class TimeZonesRemoveCommand implements Command {

    @Override
    public String getName() {
        return "timezones remove";
    }

    @Override
    public String getDescription() {
        return "Remove a time zone by index, name, or ZoneId";
    }

    @Override
    public String getUsage() {
        return getName() + " <index|name|ZoneId>\nExamples:\n" +
               "  timezones remove 1\n" +
               "  timezones remove UK\n" +
               "  timezones remove Europe/London";
    }

    @Override
    public String getCategory() {
        return "Features";
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.MOD;
    }

    @Override
    public boolean execute(Message msg, String arguments) throws Exception {
        if (!UserInfoUtilities.canManageServer(msg)) {
            msg.getChannel().sendMessage("❌ You need Manage Server permission to modify time zones.");
            return true;
        }
        if (arguments == null || arguments.isBlank()) {
            msg.getChannel().sendMessage("Usage: " + getUsage());
            return true;
        }
        var tz = TimeZonesContext.getServerTimeZones(msg);
        if (tz == null) {
            msg.getChannel().sendMessage("This command must be used in a server.");
            return true;
        }

        boolean removed = tz.removeZone(arguments.trim());
        if (removed) {
            msg.getChannel().sendMessage("✅ Removed timezone.");
        } else {
            msg.getChannel().sendMessage("❌ Not found. Use the index from 'timezones' output, or provide a valid name/ZoneId.");
        }
        return true;
    }

    @Override
    public boolean isStartsWithPrefix() {
        return true;
    }
}