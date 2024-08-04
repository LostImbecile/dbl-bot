package com.github.egubot.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.DataManager;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.interfaces.Toggleable;
import com.github.egubot.shared.TimedAction;

public class DataManagerSwitcher implements DataManager, Shutdownable, Toggleable {
	protected static final Logger logger = LogManager.getLogger(DataManagerSwitcher.class.getName());
	private static final int MINUTE = 60 * 1000;

	private static List<Toggleable> toggleables = new ArrayList<>();

	private static volatile boolean isOnline = false;
	private boolean isOnlineCapable = false;
	private DataManager manager = null;

	private TimedAction uploadTimer = null;
	private String storageKey;
	private String resourcePath;
	private String dataName;
	private boolean verbose;
	private InputStream localInput = null;
	private long uniqueID = -1;

	public DataManagerSwitcher() {
		registerToggleable(this);
	}

	public DataManagerSwitcher(String dataName) throws IOException {
		this();
		this.dataName = dataName;
		this.isOnlineCapable = false;
		toggle();
	}

	public DataManagerSwitcher(String storageKey, String resourcePath, String dataName, boolean verbose)
			throws IOException {
		this();
		this.storageKey = storageKey;
		this.resourcePath = resourcePath;
		this.dataName = dataName;
		this.verbose = verbose;
		this.isOnlineCapable = true;
		this.uploadTimer = new TimedAction(10L * MINUTE, null, null);
		toggle();
	}

	public DataManagerSwitcher(String storageKey, InputStream localInput, String dataName, boolean verbose)
			throws IOException {
		this();
		this.storageKey = storageKey;
		this.localInput = localInput;
		this.dataName = dataName;
		this.verbose = verbose;
		this.isOnlineCapable = true;
		this.uploadTimer = new TimedAction(10L * MINUTE, null, null);
		toggle();
	}

	public DataManagerSwitcher(String storageKey, String resourcePath, String dataName, long uniqueID, boolean verbose)
			throws IOException {
		this();
		this.storageKey = storageKey;
		this.resourcePath = resourcePath;
		this.dataName = dataName;
		this.verbose = verbose;
		this.uniqueID = uniqueID;
		this.isOnlineCapable = true;
		this.uploadTimer = new TimedAction(10L * MINUTE, null, null);
		toggle();
	}

	@Override
	public synchronized void toggle() throws IOException {
		if (manager == null)
			switchManager(true);
		else {
			// Avoid loss of data
			writeData(null);
			List<String> data = getData();
			int lockedData = getLockedDataEndIndex();

			switchManager(false);

			setData(data);
			setLockedDataEndIndex(lockedData);
			writeData(null);
		}
	}

	private void switchManager(boolean initialise) throws IOException {
		if (isOnline() && !isOnlineCapable)
			logger.warn("Not enough info to use online storage.");

		if (isOnline() && isOnlineCapable) {
			try {
				getOnlineManager(initialise);
				return;
			} catch (Exception e) {
				isOnlineCapable = false;
				logger.error(e);
				logger.warn("Online manager failed. Switching to local storage...");
			}
		}

		getLocalManager(initialise);

	}

	private void getLocalManager(boolean initialise) throws IOException {
		String name = dataName;
		if (uniqueID > 0)
			name = uniqueID + File.separator + dataName;
		manager = new LocalDataManager(name);
		if (initialise)
			initialise(verbose);
	}

	private void getOnlineManager(boolean initialise) throws IOException {
		String name = dataName;
		if (localInput == null)
			manager = new OnlineDataManager(storageKey, resourcePath, name);
		else
			manager = new OnlineDataManager(storageKey, localInput, name);

		if (initialise)
			initialise(verbose);
	}

	public synchronized void writeData(Messageable e, boolean isImmediate) {
		if (isImmediate || !isOnline() || uploadTimer == null) {
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
	public synchronized void writeData(Messageable e) {
		// Make sure any running timer stops
		if (uploadTimer != null && uploadTimer.isTimerOn())
			uploadTimer.cancelSingleTimer();

		manager.writeData(e);
	}

	@Override
	public void initialise(boolean verbose) throws IOException {
		manager.initialise(verbose);
	}

	@Override
	public void readData(Messageable e) {
		manager.readData(e);
	}

	public void sendData(Messageable e) {
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

	@Override
	public void shutdown() {
		if (uploadTimer != null) {
			if (uploadTimer.isTimerOn()) {
				writeData(null);
			}
			uploadTimer.terminateTimer();
		}
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static synchronized boolean isOnline() {
		return isOnline;
	}

	public static synchronized void setOnline(boolean isOnline) {
		DataManagerSwitcher.isOnline = isOnline;
		notifyToggleables();
	}

	private static synchronized void registerToggleable(Toggleable toggleable) {
		toggleables.add(toggleable);
	}

	private static synchronized void notifyToggleables() {
		for (Toggleable toggleable : toggleables) {
			try {
				toggleable.toggle();
			} catch (Exception e) {
				logger.error("Failed to toggle storage for class", e);
			}
		}
	}

}
