package com.github.egubot.interfaces;

public interface Shutdownable {
	void shutdown();
	int getShutdownPriority();
}
