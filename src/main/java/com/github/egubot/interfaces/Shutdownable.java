package com.github.egubot.interfaces;

public interface Shutdownable extends Comparable<Shutdownable>{
	void shutdown();
	int getShutdownPriority();

	@Override
	default int compareTo(Shutdownable other) {
		return getShutdownPriority() - other.getShutdownPriority();
	}
}
