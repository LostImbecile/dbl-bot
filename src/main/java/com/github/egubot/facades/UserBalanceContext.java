package com.github.egubot.facades;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;

public class UserBalanceContext implements Shutdownable {
    private static Map<Long, UserBalance> balanceMap = new ConcurrentHashMap<>();

    public static UserBalance getUserBalance(Message msg) {
        long serverID = ServerInfoUtilities.getServerID(msg);
        if (serverID == -1) {
            return null;
        }
        return balanceMap == null ? null : balanceMap.computeIfAbsent(serverID, k -> new UserBalance(serverID));
    }

    public static void shutdownStatic() {
        Map<Long, UserBalance> map = balanceMap;
        balanceMap = null;
        if (map == null)
            return;
        for (UserBalance balance : map.values()) {
            balance.shutdown();
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