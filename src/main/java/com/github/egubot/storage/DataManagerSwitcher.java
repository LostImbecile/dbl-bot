package com.github.egubot.storage;

import java.util.List;
import java.util.TimerTask;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.DataManager;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.shared.TimedAction;

public class DataManagerSwitcher implements DataManager, Shutdownable {
	private static final int MINUTE = 60 * 1000;
	private boolean isOnlineCapable = false;
	private boolean isOnline = false;
	private DataManager manager;

	private TimedAction uploadTimer = null;
	private DiscordApi api;
	private String storageKey;
	private String resourcePath;
	private String dataName;
	private boolean verbose;

	public DataManagerSwitcher(String dataName) {
		this.dataName = dataName;
	}

	public DataManagerSwitcher(DiscordApi api, String storageKey, String resourcePath, String dataName,
			boolean verbose) {
		this.api = api;
		this.storageKey = storageKey;
		this.resourcePath = resourcePath;
		this.dataName = dataName;
		this.verbose = verbose;
		this.isOnlineCapable = true;
		this.uploadTimer = new TimedAction(10L * MINUTE, null, null);
	}

	public void toggleManager(boolean isOnline) {
		if(isOnline && !isOnlineCapable)
			System.out.println("Not enough info to use online storage.");
		
		if (isOnline && isOnlineCapable) {
			try {
				manager = new OnlineDataManager(api, storageKey, resourcePath, dataName, verbose);
				this.isOnline = true;
			} catch (Exception e) {
				this.isOnline = false;
				e.printStackTrace();
				System.out.println("Switching to local storage...");
				manager = new LocalDataManager();
			}
		} else {
			this.isOnline = false;
			manager = new LocalDataManager();
		}
		updateObjects();
	}

	public void writeData(Messageable e, boolean isImmediate) {
		if (isImmediate || !isOnline || uploadTimer == null) {
			writeData(e);
		} else {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					writeData(null);
				}
			};
			uploadTimer.startOneInstanceSingleTimer(task, 3);
			e.sendMessage("Updated <:drink:1184466286944735272>");
		}
	}
	@Override
	public void writeData(Messageable e) {
		updateDataFromObjects();
		manager.writeData(e);
		updateObjects();
		
		// Make sure any running timer stops
		if(uploadTimer != null && uploadTimer.isTimerOn())
			uploadTimer.cancelSingleTimer();
	}

	public void sendData(Messageable e) {
		updateDataFromObjects();
		manager.sendData(e);
	}

	public int getLockedDataEndIndex() {
		return manager.getLockedDataEndIndex();
	}

	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		manager.setLockedDataEndIndex(lockedDataEndIndex);
	}

	public List<String> getData() {
		return manager.getData();
	}

	public void setData(List<String> data) {
		manager.setData(data);
	}

	public void updateObjects() {
		// For classes that convert data to objects
	}

	public void updateDataFromObjects() {
		// For classes that convert data to objects
	}

	@Override
	public void shutdown() {
		if (uploadTimer != null) {
			uploadTimer.terminateTimer();
			if (uploadTimer.isTimerOn()) {
				uploadTimer = null;
				writeData(null);
			}

		}
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

}
