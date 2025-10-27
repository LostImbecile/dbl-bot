package com.github.egubot.facades;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.ServerTimeZones;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;

public class TimeZonesContext implements Shutdownable {
    private static Map<Long, ServerTimeZones> zonesMap = new ConcurrentHashMap<>();

    public static ServerTimeZones getServerTimeZones(Message msg) {
        long serverID = ServerInfoUtilities.getServerID(msg);
        if (serverID == -1) return null;
        return zonesMap == null ? null : zonesMap.computeIfAbsent(serverID, k -> new ServerTimeZones(serverID));
    }

    public static void shutdownStatic() {
        Map<Long, ServerTimeZones> map = zonesMap;
        zonesMap = null;
        if (map == null) return;
        for (ServerTimeZones stz : map.values()) {
            stz.shutdown();
        }
    }

    @Override
    public void shutdown() {
        shutdownStatic();
    }

    @Override
    public int getShutdownPriority() {
        return 0;
    }
}