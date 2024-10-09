package com.github.egubot.interfaces;

public interface HasPriority extends Comparable<HasPriority>{
	int getPriority();
	
	@Override
	default int compareTo(HasPriority other) {
		return getPriority() - other.getPriority();
	}
}
