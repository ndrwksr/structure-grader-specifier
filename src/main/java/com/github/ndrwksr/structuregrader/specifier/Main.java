package com.github.ndrwksr.structuregrader.specifier;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Starts the application. No arguments are anticipated.
 */
public class Main extends javafx.application.Application {
	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * Launches the JavaFX application from the source .fxml. Invoked by JavaFX.
	 *
	 * @param primaryStage The stage provided by JavaFX to run the application on.
	 * @throws Exception If the application cannot be started, an exception of indeterminate type will be thrown.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
		final MainController mainController = new MainController();
		loader.setController(mainController);
		final AnchorPane root = loader.load();
		mainController.setRoot(root);

		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
}
