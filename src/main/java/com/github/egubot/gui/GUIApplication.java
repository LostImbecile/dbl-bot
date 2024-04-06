package com.github.egubot.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BotInfo.fxml"));
			Parent root = loader.load();
			
			BotInfoController controller = (BotInfoController) loader.getController();

			JavaFXAppender.setTextArea(controller.getLogsArea());
			// Set up the scene
			Scene scene = new Scene(root);

			scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
			// Set the scene and show the stage
			primaryStage.setScene(scene);
			primaryStage.setMinHeight(462 + 20);
			primaryStage.setMinWidth(740 + 20);
			primaryStage.setTitle("Your GUI Title");
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/discordIcon.png")));
			primaryStage.show();

			// Set up action on close
			primaryStage.setOnCloseRequest(event -> {
				// Perform actions on exit here
				System.out.println("Exiting the application...");
				Shared.getShutdown().initiateShutdown(0);
			});

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

	// Method to access the command-line arguments
	public String[] getArguments() {
		return args;
	}

	public static void main(String[] args) {
		Application.launch(GUIApplication.class, args);
	}
}
