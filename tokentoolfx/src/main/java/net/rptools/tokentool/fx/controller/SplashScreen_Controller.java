package net.rptools.tokentool.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

public class SplashScreen_Controller {

	@FXML
	private StackPane splashLayout; // Value injected by FXMLLoader

	@FXML
	private ProgressBar loadProgress; // Value injected by FXMLLoader

	@FXML
	private Label progressText; // Value injected by FXMLLoader

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert splashLayout != null : "fx:id=\"splashLayout\" was not injected: check your FXML file 'SplashScreenLoader.fxml'.";
		assert loadProgress != null : "fx:id=\"loadProgress\" was not injected: check your FXML file 'SplashScreenLoader.fxml'.";
		assert progressText != null : "fx:id=\"progressText\" was not injected: check your FXML file 'SplashScreenLoader.fxml'.";
	}

	public void setLoadProgress(Double progress) {
		this.loadProgress.setProgress(progress);
	}

	public String getProgressText() {
		return progressText.getText();
	}

	public void setProgressText(String text) {
		this.progressText.setText(text);
	}
}
