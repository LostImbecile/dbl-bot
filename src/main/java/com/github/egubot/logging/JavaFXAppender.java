package com.github.egubot.logging;

import javafx.scene.control.TextArea;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "JavaFXAppender", category = "Core", elementType = "appender", printObject = true)
public class JavaFXAppender extends AbstractAppender {

	private static TextArea textArea;

	protected JavaFXAppender(String name, boolean ignoreExceptions, PatternLayout layout, Filter filter) {
		super(name, filter, layout, ignoreExceptions, null);
	}

	public static void setTextArea(TextArea area) {
		textArea = area;
	}

	@PluginFactory
	public static JavaFXAppender createAppender(@PluginAttribute("name") String name,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
			@PluginElement("layout") PatternLayout layout, @PluginElement("filters") Filter filter) {

		return new JavaFXAppender(name, ignoreExceptions, layout, filter);
	}

	@Override
	public void append(LogEvent event) {
		if (textArea != null && getLayout() != null && event != null) {
			textArea.appendText(getLayout().toSerializable(event).toString());
		}
	}
}
