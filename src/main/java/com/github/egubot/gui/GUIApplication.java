package com.github.egubot.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.gui.controllers.BotInfoController;
import com.github.egubot.gui.controllers.SendMessagesController;
import com.github.egubot.gui.controllers.SettingsController;
import com.github.egubot.logging.JavaFXAppender;
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
			
			configureMainWindow(primaryStage, mainRoot);
			
			configureSettingsWindow(mainController);
			
			configureSendMessagesWindow(mainController);

			new Thread(() -> {
				try {
				//	Main.main(getArguments());
				} catch (Exception e) {
					logger.fatal(e);
					Shared.getShutdown().initiateShutdown(1);
				}

			}).start();

			logger.warn("test");

		} catch (Exception e) {
			logger.fatal(e);
			e.printStackTrace();
			Shared.getShutdown().initiateShutdown(1);
		}
	}

	@SuppressWarnings("unused")
	private void configureSendMessagesWindow(BotInfoController mainController) throws IOException {
		FXMLLoader sendMessagesLoader = new FXMLLoader(getClass().getResource("/fxml/SendMessages.fxml"));
		Parent sendMessagesRootRoot = sendMessagesLoader.load();
		SendMessagesController sendMessagesController = (SendMessagesController) sendMessagesLoader.getController();
		
		Stage sendMessagesStageStage = new Stage();
	    sendMessagesStageStage.setTitle("Send Messages With Bot");
	    setIcon(sendMessagesStageStage);

	    Scene scene = new Scene(sendMessagesRootRoot);
	    scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
	    sendMessagesStageStage.setScene(scene);
	    
		mainController.getSendMessagesButton().setOnAction(e->{
		    sendMessagesStageStage.show();
		});
	}

	@SuppressWarnings("unused")
	public void configureSettingsWindow(BotInfoController mainController) throws IOException {
		FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
		Parent settingsRoot = settingsLoader.load();
		SettingsController settingsController = (SettingsController) settingsLoader.getController();
		
		Stage settingsStage = new Stage();
	    settingsStage.setTitle("Settings");
	    setIcon(settingsStage);

	    // Create a Scene for the settings
	    Scene scene = new Scene(settingsRoot);
	    scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
	    // Set the settings scene to the stage
	    settingsStage.setScene(scene);

		mainController.getSettingsButton().setOnAction(e->{
		    // Show the settings stage
		    settingsStage.show();
		});
		
	}

	public void configureMainWindow(Stage primaryStage, Parent mainRoot) {
		// Set up the scene
		Scene scene = new Scene(mainRoot);
		
		scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
		// Set the scene and show the stage
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(462 + 20);
		primaryStage.setMinWidth(740 + 20);
		primaryStage.setTitle("Your GUI Title");
		setIcon(primaryStage);
		primaryStage.show();

		// Set up action on close
		primaryStage.setOnCloseRequest(event -> {
			// Perform actions on exit here
			System.out.println("Exiting the application...");
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
