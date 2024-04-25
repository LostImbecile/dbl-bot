package com.github.egubot.facades;

import com.meta.llama3.Llama3AI;

public class Llama3Context extends AIContext{
	
	static {
		setModel(new Llama3AI());
	}
}
