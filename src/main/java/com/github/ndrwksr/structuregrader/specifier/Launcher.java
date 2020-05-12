package com.github.ndrwksr.structuregrader.specifier;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Launcher extends Application {

	public static void main(String[] args) {
		launch(args);
	}

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

