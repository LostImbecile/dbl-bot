package com.github.egubot.logging;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import java.io.OutputStream;

public class StreamRedirector {

	private static final Map<String, OutputStream> outputStreams = new HashMap<>();
	private static final PrintStream defaultPrintStream = System.out;

	public static void registerStream(String identifier, OutputStream outputStream) {
		outputStreams.put(identifier, outputStream);
	}

	public static void println(String identifier, Object message) {
		OutputStream outputStream = outputStreams.get(identifier);
		if (outputStream != null) {
			PrintStream printStream = new PrintStream(outputStream);
			printStream.println(message);
			printStream.flush();
		}
		defaultPrintStream.println(message);
	}

	public static void print(String identifier, Object message) {
		OutputStream outputStream = outputStreams.get(identifier);
		if (outputStream != null) {
			PrintStream printStream = new PrintStream(outputStream);
			printStream.print(message);
			printStream.flush();
		}
		defaultPrintStream.print(message);
	}

	public static void printlnOnce(String identifier, Object message) {
		OutputStream outputStream = outputStreams.get(identifier);
		if (outputStream != null) {
			PrintStream printStream = new PrintStream(outputStream);
			printStream.println(message);
			printStream.flush();
		}
	}

	public static void main(String[] args) {
		StreamRedirector.registerStream("info", System.out);
		StreamRedirector.registerStream("logs", System.err);

		StreamRedirector.println("info", "This is an information message.");
		StreamRedirector.println("logs", "This is a log message.");
	}
}
