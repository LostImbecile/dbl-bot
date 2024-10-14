package com.github.egubot.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.objects.finance.ServerFinanceData;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.shared.utils.JSONUtilities;
import com.github.egubot.shared.utils.MessageUtils;
import com.github.egubot.storage.DataManagerHandler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class UserBalance extends DataManagerHandler {
	private Map<Long, UserFinanceData> balanceMap;
	private ServerFinanceData serverFinanceData;

	public UserBalance(long serverID) {
		super(serverID + File.separator + "User_Balance", true);
	}

	public synchronized boolean setBalance(Message msg, String msgText) {
		try {
			if (msg.getMentionedUsers().isEmpty()) {
				double balance = Double.parseDouble(msgText);
				getUserData(msg).setBalance(balance);
			} else {
				double balance = Double.parseDouble(msgText.split(" ")[1]);
				long userID = getUserID(msg);
				getUserData(userID).setBalance(balance);
			}
			writeData(null);
			return true;
		} catch (Exception e) {
			msg.getChannel().sendMessage("Invalid amount");
		}
		return false;
	}

	public synchronized void resetBalance(Message msg) {
		getUserData(getUserID(msg)).setBalance(0);
		writeData(msg.getChannel());
	}

	public double getBalance(Message msg) {
		return getUserData(getUserID(msg)).getBalance();
	}

	private long getUserID(Message msg) {
		if (!MessageUtils.getPingedUsers(msg.getContent()).isEmpty())
			return Long.parseLong(MessageUtils.getPingedUsers(msg.getContent()).get(0));
		return msg.getAuthor().getId();
	}

	public UserFinanceData getUserData(Message msg) {
		long userID = msg.getAuthor().getId();
		return getUserData(userID);
	}

	public UserFinanceData getUserData(long userID) {
		return balanceMap.computeIfAbsent(userID, k -> new UserFinanceData(userID));
	}

	public void setUserData(Message msg, UserFinanceData userData) {
		setUserData(msg.getAuthor().getId(), userData);
	}

	public void setUserData(long userID, UserFinanceData userData) {
		if (userData != null && userID == userData.getUserID()) {
			balanceMap.put(userID, userData);
			writeData(null);
		}
	}

	public void setUserData(UserPair userPair) {
		if (userPair.getUser1() != null)
			setUserData(userPair.getUser1().getUserID(), userPair.getUser1());
		if (userPair.getUser2() != null)
			setUserData(userPair.getUser2().getUserID(), userPair.getUser2());
	}

	@Override
	public void updateObjects() {
		try {
			Gson gson = new Gson();
			String jsonData = ConvertObjects.listToText(getData());
			serverFinanceData = gson.fromJson(jsonData, ServerFinanceData.class);
		} catch (JsonSyntaxException e) {
			logger.error("Syntax Error updating objects", e);
		}
		if (balanceMap == null)
			balanceMap = new ConcurrentHashMap<>();

		if (serverFinanceData == null) {
			serverFinanceData = new ServerFinanceData();
		} else {
			for (UserFinanceData userData : serverFinanceData.getUsers()) {
				balanceMap.put(userData.getUserID(), userData);
			}
		}

	}

	@Override
	public void updateDataFromObjects() {
		if (serverFinanceData == null) {
			return;
		}
		serverFinanceData.setUsers(new ArrayList<>(balanceMap.values().stream().toList()));
		Gson gson = new Gson();
		String jsonData = gson.toJson(serverFinanceData);
		jsonData = JSONUtilities.prettify(jsonData);
		setData(ConvertObjects.textToList(jsonData));
	}

	public ServerFinanceData getServerFinanceData() {
		return serverFinanceData;
	}

	public void setServerFinanceData(ServerFinanceData serverFinanceData) {
		this.serverFinanceData = serverFinanceData;
	}

}
