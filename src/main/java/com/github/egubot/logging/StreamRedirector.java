package com.github.egubot.logging;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;

public class StreamRedirector {
	public static final Logger logger = LogManager.getLogger(StreamRedirector.class.getName());
	
	private static final Map<String, OutputStream> outputStreams = new HashMap<>();
	private static final PrintStream defaultPrintStream = System.out;

	public static void registerStream(String identifier, OutputStream outputStream) {
		outputStreams.put(identifier, outputStream);
	}

	public static void removeStream(String identifier) {
		outputStreams.remove(identifier);
	}

	public static void clearStream(String identifier) {
		OutputStream outputStream = outputStreams.get(identifier);
		if (outputStream != null) {
			try {
				outputStream.flush();
				if (outputStream instanceof TextAreaOutputStream textAreaOutputStream) {
					textAreaOutputStream.reset();
				} else if (outputStream instanceof java.io.ByteArrayOutputStream byteArrayOutputStream) {
					byteArrayOutputStream.reset();
				}
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
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
	}
}
