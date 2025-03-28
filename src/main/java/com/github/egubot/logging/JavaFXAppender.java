package com.github.egubot.logging;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.HashMap;
import java.util.Map;

@Plugin(name = "JavaFXAppender", category = "Core", elementType = "appender", printObject = true)
public class JavaFXAppender extends AbstractAppender {
	static final Logger logger = LogManager.getLogger(JavaFXAppender.class.getName());

	private static Map<Level, Map<String, TextArea>> textAreas = new HashMap<>();

	protected JavaFXAppender(String name, boolean ignoreExceptions, PatternLayout layout, Filter filter) {
		super(name, filter, layout, ignoreExceptions, null);
	}

	public static void registerTextArea(String loggerName, Level level, TextArea textArea) {
		if (!level.equals(Level.OFF))
			textAreas.computeIfAbsent(level, k -> new HashMap<>()).put(loggerName, textArea);
	}

	@PluginFactory
	public static JavaFXAppender createAppender(@PluginAttribute("name") String name,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
			@PluginElement("layout") PatternLayout layout, @PluginElement("filters") Filter filter) {

		return new JavaFXAppender(name, ignoreExceptions, layout, filter);
	}

	@Override
	public void append(LogEvent event) {
		if (event != null && getLayout() != null && event.getLoggerName() != null) {
		try {
			
				Level eventLevel = event.getLevel();
				Map<String, TextArea> levelTextAreas = getLevelTextAreas(eventLevel);
				if (!levelTextAreas.isEmpty()) {
					String loggerName = event.getLoggerName();
					TextArea matchedTextArea = getLongestMatch(loggerName, levelTextAreas);

					String message = getLayout().toSerializable(event).toString() + "\n";
					if (matchedTextArea != null && !message.isBlank()) {
						Platform.runLater(
								() -> matchedTextArea.appendText(message));
					}
				}
			
		} catch (Exception e) {
				logger.error("Couldn't append log event. Logger name: {}", event.getLoggerName() + "", e);
			}
		}
	}

	private TextArea getLongestMatch(String loggerName, Map<String, TextArea> levelTextAreas) {
		String longestMatch = "";
		TextArea matchedTextArea = null;

		for (Map.Entry<String, TextArea> entry : levelTextAreas.entrySet()) {
			if (loggerName.startsWith(entry.getKey()) && entry.getKey().length() > longestMatch.length()) {
				longestMatch = entry.getKey();
				matchedTextArea = entry.getValue();
			}
		}
		if (longestMatch.isEmpty()) {
			matchedTextArea = levelTextAreas.getOrDefault("all", null);
		}
		return matchedTextArea;
	}

	private Map<String, TextArea> getLevelTextAreas(Level level) {
		Map<String, TextArea> result = new HashMap<>();
		for (Map.Entry<Level, Map<String, TextArea>> entry : textAreas.entrySet()) {
			if (level.intLevel() <= entry.getKey().intLevel()) {
				result.putAll(entry.getValue());
			}
		}
		return result;
	}
}