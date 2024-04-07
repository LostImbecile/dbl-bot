package com.github.egubot.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.gui.controllers.BotInfoController;
import com.github.egubot.logging.TextAreaOutputStream;
import com.github.egubot.logging.JavaFXAppender;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.main.Main;
import com.github.egubot.shared.Shared;

public class GUIApplication extends Application {
	private static final Logger logger = LogManager.getLogger(GUIApplication.class.getName());
	// Store the command-line arguments
	private String[] args;

	@Override
	public void init() throws Exception {
		super.init();
		this.args = getParameters().getRaw().toArray(new String[0]);
	}

	@Override
	public void start(Stage primaryStage) {
		try {

			// Load the FXML file
			FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/BotInfo.fxml"));
			Parent mainRoot = mainLoader.load();
			BotInfoController mainController = (BotInfoController) mainLoader.getController();

			JavaFXAppender.setTextArea(mainController.getLogsArea());

			configureMainWindow(primaryStage, mainRoot, mainController);
			
			StreamRedirector.registerStream("info", new TextAreaOutputStream(mainController.getInfoArea()));
			StreamRedirector.registerStream("events", new TextAreaOutputStream(mainController.getEventsArea()));
			StreamRedirector.registerStream("logs", new TextAreaOutputStream(mainController.getLogsArea()));
			
			

			new Thread(() -> {
				try {
					 Main.main(getArguments());
				} catch (Exception e) {
					logger.fatal(e);
					Shared.getShutdown().initiateShutdown(1);
				}

			}).start();
			
			primaryStage.show();
			
		} catch (Exception e) {
			logger.fatal(e);
			e.printStackTrace();
			Shared.getShutdown().initiateShutdown(1);
		}
	}

	public void configureMainWindow(Stage primaryStage, Parent mainRoot, BotInfoController mainController) {
		Scene scene = new Scene(mainRoot);

		scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
		// Set the scene and show the stage
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(462 + 20);
		primaryStage.setMinWidth(740 + 20);
		primaryStage.setTitle("Your GUI Title");
		setIcon(primaryStage);

		// Set up action on close
		primaryStage.setOnCloseRequest(event -> {
			// Perform actions on exit here
			StreamRedirector.println("info","Exiting the application...");
			Shared.getShutdown().initiateShutdown(0);
		});
	}

	public void setIcon(Stage stage) {
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/discordIcon.png")));
	}

	public String[] getArguments() {
		return args;
	}

	public static void main(String[] args) {
		Application.launch(GUIApplication.class, args);
	}
}
