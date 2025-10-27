package com.github.egubot.commands;

import java.util.ArrayList;
import java.util.List;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.TimeZonesContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class TimeZonesAddCommand implements Command {

    @Override
    public String getName() {
        return "timezones add";
    }

    @Override
    public String getDescription() {
        return "Add time zones with optional custom names";
    }

    @Override
    public String getUsage() {
        return getName() + " <name:ZoneId> or <ZoneId>\n" +
               "Supports region codes: BST, EST, PST, CST, MST, JST, CET, IST, AEST\n" +
               "Examples:\n" +
               "  timezones add UK:BST\n" +
               "  timezones add UK:Europe/London\n" +
               "  timezones add EST PST JST\n" +
               "  timezones add NYC:America/New_York Tokyo:Asia/Tokyo\n" +
               "  timezones add UTC+5:30";
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

        String[] parts = arguments.replace(",", " ").trim().split("\\s+");
        List<String> added = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        for (String p : parts) {
            if (p.isBlank()) continue;
            
            String customName = null;
            String zoneInput = p;
            
            if (p.contains(":")) {
                String[] nameParts = p.split(":", 2);
                if (nameParts.length == 2 && !nameParts[0].isBlank()) {
                    customName = nameParts[0];
                    zoneInput = nameParts[1];
                }
            }
            
            boolean ok = tz.addZone(zoneInput, customName);
            if (ok) {
                String display = customName != null ? customName : zoneInput;
                added.add(display);
            } else {
                failed.add(p);
            }
        }

        if (!added.isEmpty()) {
            msg.getChannel().sendMessage("✅ Added: " + String.join(", ", added));
        }
        if (!failed.isEmpty()) {
            msg.getChannel().sendMessage("❌ Invalid or duplicate: " + String.join(", ", failed));
        }
        return true;
    }

    @Override
    public boolean isStartsWithPrefix() {
        return true;
    }
}