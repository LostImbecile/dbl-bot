package com.github.egubot.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.gui.controllers.BotInfoController;
import com.github.egubot.gui.controllers.UserInputController;
import com.github.egubot.io.LabelOutputStream;
import com.github.egubot.io.TextAreaOutputStream;
import com.github.egubot.logging.JavaFXAppender;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.main.Bot;
import com.github.egubot.main.Main;
import com.github.egubot.main.Run;
import com.github.egubot.shared.Shared;

public class GUIApplication extends Application {
	private static final Logger logger = LogManager.getLogger(GUIApplication.class.getName());
	private static boolean isGUIOn = false;

	@Override
	public void start(Stage primaryStage) {
		try {
			GUIApplication.setGUIOn(true);

			// Load the FXML file
			FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/BotInfo.fxml"));
			Parent mainRoot = mainLoader.load();
			BotInfoController mainController = (BotInfoController) mainLoader.getController();

			JavaFXAppender.registerTextArea("com", Level.WARN, mainController.getLogsArea());
			JavaFXAppender.registerTextArea("org.javacord", Level.INFO, mainController.getJavacordLogsArea());
			JavaFXAppender.registerTextArea("all", Level.DEBUG, mainController.getDebugTextArea());

			configureMainWindow(primaryStage, mainRoot, mainController);

			configureUserInputWindow();

			StreamRedirector.registerStream("info", new TextAreaOutputStream(mainController.getInfoArea()));
			StreamRedirector.registerStream("events", new TextAreaOutputStream(mainController.getEventsArea()));
			StreamRedirector.registerStream("logs", new TextAreaOutputStream(mainController.getLogsArea()));

			Thread mainThread = new Thread(() -> {
				Main.main(Run.getArgs());
				mainController.getButtonsVbox().setDisable(false);
				Platform.runLater(() -> primaryStage.setTitle(Bot.getName()));
			});
			mainThread.start();

			mainController.getButtonsVbox().setDisable(true);
			primaryStage.show();

		} catch (Exception e) {
			logger.fatal(e);
			e.printStackTrace();
			Shared.getShutdown().initiateShutdown(1);
		}
	}

	public void configureUserInputWindow() throws IOException {
		FXMLLoader userInputWindowLoader = new FXMLLoader(getClass().getResource("/fxml/UserInput.fxml"));
		Parent userInputRoot = userInputWindowLoader.load();
		UserInputController userInputController = (UserInputController) userInputWindowLoader.getController();

		StreamRedirector.registerStream("prompt", new LabelOutputStream(userInputController.getPromptLabel()));

		Scene scene = new Scene(userInputRoot);
		scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());

		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle("Pending input");
		setIcon(stage);

		userInputController.initialiseInputStream(stage);
	}

	public void configureMainWindow(Stage primaryStage, Parent mainRoot, BotInfoController mainController) {
		Scene scene = new Scene(mainRoot);

		scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.setMinHeight(462 + 20);
		primaryStage.setMinWidth(740 + 20);
		primaryStage.setTitle("Loading...");
		setIcon(primaryStage);

		primaryStage.setOnCloseRequest(event -> {
			StreamRedirector.println("info", "\nExiting the application...");
			Shared.getShutdown().initiateShutdown(0);
		});
	}

	public void setIcon(Stage stage) {
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/discordIcon.png")));
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static boolean isGUIOn() {
		return isGUIOn;
	}

	private static void setGUIOn(boolean isGUIOn) {
		GUIApplication.isGUIOn = isGUIOn;
	}

}
