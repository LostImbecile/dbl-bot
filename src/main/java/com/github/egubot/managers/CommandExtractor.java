package com.github.egubot.managers;

import com.github.egubot.main.Bot;

public class CommandExtractor {
	private PrefixTrieNode prefixRoot; // Root of the prefix trie
	private TrieNode commandRoot; // Root of the command trie
	private String currentPrefix; // Current prefix being used for commands

	public CommandExtractor() {
		prefixRoot = new PrefixTrieNode();
		commandRoot = new TrieNode();
		setCurrentPrefix(Bot.getPrefix()); // Default prefix
	}

	// Inserts a word into the command trie.
	public void insertCommand(String word) {
		word = word.toLowerCase();
		TrieNode current = commandRoot;
		for (char c : word.toCharArray()) {
			int index = c - 'a'; // Calculate the index for the current character
			if (c == ' ') {
				index = 26; // Space
			}
			if (current.children[index] == null) {
				current.children[index] = new TrieNode();
			}
			current = current.children[index];
		}
		current.isEndOfCommand = true;
	}

	/*
	 * Inserts a prefix into the prefix trie, wiping the existing tree clean.
	 * Only updates the tree if the prefix was fully added, which is the case when:
	 * 1) There are no illegal characters used
	 * 2) The prefix is not empty
	 */
	private boolean insertPrefix(String prefix) {
		prefix = prefix.toLowerCase();
		PrefixTrieNode newPrefixRoot = new PrefixTrieNode();
		boolean prefixAdded = false;

		PrefixTrieNode current = newPrefixRoot;
		for (char c : prefix.toCharArray()) {
			int charIndex = getCharIndex(c);
			if (charIndex >= 0) {
				if (current.children[charIndex] == null) {
					current.children[charIndex] = new PrefixTrieNode();
					current = current.children[charIndex];
					prefixAdded = true;
				} else {
					prefixAdded = false;
					break;
				}

			}
		}
		current.isEndOfPrefix = true;

		if (prefixAdded) {
			this.prefixRoot = newPrefixRoot;
			this.currentPrefix = prefix;
		}
		return prefixAdded;
	}

	public int findPrefix(String str) {
		str = str.toLowerCase();
		PrefixTrieNode prefixCurrent = prefixRoot;
		int index = 0;
		int len = str.length();

		// Step 1: Traverse the prefix tree
		while (index < len) {
			char c = str.charAt(index);
			int charIndex = getCharIndex(c);
			if (charIndex >= 0) {
				prefixCurrent = prefixCurrent.children[charIndex];
			} else {
				// Prefix not found
				return -1;
			}
			if (prefixCurrent == null)
				return -1;

			index++;
			if (prefixCurrent.isEndOfPrefix) {
				// Prefix found
				break;
			}
		}

		return index;
	}

	// Defaults to detecting prefix
	public int findCommand(String str) {
		return findCommand(str, false);
	}

	// Detects commands in a string and returns the command and the end index of the
	// command if found, null otherwise.
	public int findCommand(String str, boolean ignorePrefix) {
		str = str.toLowerCase();
		int index = 0;
		if (!ignorePrefix)
			index = findPrefix(str);
		if (index < 0)
			return -1;
		int len = str.length();

		TrieNode commandCurrent = commandRoot;
		int commandEndIndex = -1; // Variable to store the index of the last detected command
		while (index < len) {
			char c = str.charAt(index);
			int arrayIndex = c - 'a';

			// Check if there's a path
			if (c == ' ') {
				commandCurrent = commandCurrent.children[26]; // Space
			} else if (arrayIndex >= 0 && arrayIndex < 26) {
				commandCurrent = commandCurrent.children[arrayIndex];
			} else {
				// Invalid character, stop
				return commandEndIndex;
			}
			
			if (commandCurrent == null) {
				// No next path
				break; 
			}

			index++;

			if (commandCurrent.isEndOfCommand) {
				// Command found, but continue searching for longer commands
				commandEndIndex = index;
			}
		}
		
		return commandEndIndex;
	}

	// Removes a command from the string and returns the index of its end.
	public int removeCommand(String str, int endIndex) {
		while (endIndex < str.length() && Character.isWhitespace(str.charAt(endIndex))) {
			endIndex++;
		}
		return endIndex;
	}

	public synchronized String getCurrentPrefix() {
		return currentPrefix;
	}

	public synchronized void setCurrentPrefix(String currentPrefix) {
		insertPrefix(currentPrefix);
	}

	private static class TrieNode {
		TrieNode[] children;
		boolean isEndOfCommand;

		public TrieNode() {
			// Assuming lowercase alphabet characters only
			children = new TrieNode[27];
			isEndOfCommand = false;
		}
	}

	private static class PrefixTrieNode {
		PrefixTrieNode[] children;
		boolean isEndOfPrefix;

		public PrefixTrieNode() {
			// 26 letters of the alphabet + 14 special characters
			children = new PrefixTrieNode[26 + 14];
			isEndOfPrefix = false;
		}
	}

	private int getCharIndex(char c) {
		// Characters 'a' to 'z' are mapped to indices 0 to 25
		if (c >= 'a' && c <= 'z') {
			return c - 'a';
		}
		// Special characters are mapped to indices 26 to 39
		switch (c) {
		case '?':
			return 26;
		case '!':
			return 27;
		case '-':
			return 28;
		case '>':
			return 29;
		case '<':
			return 30;
		case '$':
			return 31;
		case '#':
			return 32;
		case '@':
			return 33;
		case '%':
			return 34;
		case '^':
			return 35;
		case '&':
			return 36;
		case '\\':
			return 37;
		case ':':
			return 38;
		case ';':
			return 39;
		default:
			return -1; // Invalid character
		}
	}

}
