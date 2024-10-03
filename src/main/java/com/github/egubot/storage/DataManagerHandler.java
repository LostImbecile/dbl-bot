package com.github.egubot.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.DataManager;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.interfaces.UpdatableObjects;

public class DataManagerHandler implements DataManager, Shutdownable, UpdatableObjects {
	protected static final Logger logger = LogManager.getLogger(DataManagerHandler.class.getName());
	private DataManagerSwitcher manager;

	public DataManagerHandler(String dataName) throws IOException {
		this.setManager(new DataManagerSwitcher(dataName));
		updateObjects();
	}

	public DataManagerHandler(String storageKey, String resourcePath, String dataName, boolean verbose)
			throws IOException {
		this.setManager(new DataManagerSwitcher(storageKey, resourcePath, dataName, verbose));
		updateObjects();
	}

	public DataManagerHandler(String storageKey, InputStream localInput, String dataName, boolean verbose)
			throws IOException {
		this.setManager(new DataManagerSwitcher(storageKey, localInput, dataName, verbose));
		updateObjects();
	}

	public DataManagerHandler(String storageKey, String resourcePath, String dataName, long uniqueID, boolean verbose)
			throws IOException {
		this.setManager(new DataManagerSwitcher(storageKey, resourcePath, dataName, uniqueID, verbose));
		updateObjects();
	}

	public DataManagerHandler(String resourcePath, String dataName, long uniqueID, boolean verbose) throws IOException {
		this.setManager(new DataManagerSwitcher( resourcePath, dataName, uniqueID, verbose) );
	}

	@Override
	public List<String> getData() {
		return manager.getData();
	}

	@Override
	public void setData(List<String> data) {
		manager.setData(data);
	}

	@Override
	public int getLockedDataEndIndex() {
		return manager.getLockedDataEndIndex();
	}

	@Override
	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		manager.setLockedDataEndIndex(lockedDataEndIndex);
	}

	@Override
	public void sendData(Messageable e) {
		updateDataFromObjects();
		manager.sendData(e);
	}

	@Override
	public void writeData(Messageable e) {
		writeDataDelayed(e);
	}

	public void writeDataDelayed(Messageable e) {
		updateDataFromObjects();
		manager.writeData(e, false);
	}

	public void writeDataNow(Messageable e) {
		updateDataFromObjects();
		manager.writeData(e, true);
	}

	@Override
	public void readData(Messageable e) {
		manager.readData(e);
		updateObjects();
	}

	@Override
	public void initialise(boolean verbose) throws IOException {
		manager.initialise(verbose);
	}

	public DataManagerSwitcher getManager() {
		return manager;
	}

	public void setManager(DataManagerSwitcher manager) {
		this.manager = manager;
	}

	@Override
	public void shutdown() {
		manager.shutdown();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	@Override
	public void updateObjects() {
		// For classes that update objects
	}

	@Override
	public void updateDataFromObjects() {
		// For classes that update objects
	}

}
