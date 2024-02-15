package com.github.egubot.managers;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

	private TrieNode root;

	public CommandManager() {
        root = new TrieNode();
    }

	// Inserts a word into the trie.
	public void insert(String word) {
		TrieNode current = root;
		for (char c : word.toCharArray()) {
			current.children.putIfAbsent(Character.toLowerCase(c), new TrieNode());
			current = current.children.get(Character.toLowerCase(c));
		}
		current.isEndOfCommand = true;
	}

	// Detects commands in a string and returns the command and the end index of the
	// command if found, null otherwise.
	public CommandInfo detectCommand(String str) {
		TrieNode current = root;
		int index = 0;
		int len = str.length();
		while (index < len) {
			char c = str.charAt(index);
			if (!Character.isLetter(c)) // Break if non-letter character is encountered
				break;

			current = current.children.get(Character.toLowerCase(c));
			if (current == null) {
				break;
			}
			if (current.isEndOfCommand) {
				return new CommandInfo(str.substring(0, index + 1), index);
			}
			index++;
		}
		return null;
	}

	// Removes a command from the string and returns the index of its end.
	public int removeCommand(String str, int endIndex) {
		while (endIndex < str.length() && Character.isWhitespace(str.charAt(endIndex))) {
			endIndex++;
		}
		return endIndex;
	}

	private static class TrieNode {
		Map<Character, TrieNode> children;
		boolean isEndOfCommand;

		public TrieNode() {
			children = new HashMap<>();
			isEndOfCommand = false;
		}
	}
	
	public static class CommandInfo {
        public String command;
        public int endIndex;

        public CommandInfo(String command, int endIndex) {
            this.command = command;
            this.endIndex = endIndex;
        }
    }
}
