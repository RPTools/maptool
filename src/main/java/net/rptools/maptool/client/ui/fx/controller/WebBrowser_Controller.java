package net.rptools.maptool.client.ui.fx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class WebBrowser_Controller {
  private WebEngine webEngine;

  @FXML private ResourceBundle resources;

  @FXML private URL location;

  @FXML private Button refreshButton;

  @FXML private TextField urlTextField;

  @FXML private WebView mainWebView;

  @FXML private Label leftStatusLabel;

  @FXML private Font x3;

  @FXML private Color x4;

  @FXML
  void initialize() {
    assert refreshButton != null
        : "fx:id=\"refreshButton\" was not injected: check your FXML file 'WebBrowser.fxml'.";
    assert urlTextField != null
        : "fx:id=\"urlTextField\" was not injected: check your FXML file 'WebBrowser.fxml'.";
    assert mainWebView != null
        : "fx:id=\"mainWebView\" was not injected: check your FXML file 'WebBrowser.fxml'.";
    assert leftStatusLabel != null
        : "fx:id=\"leftStatusLabel\" was not injected: check your FXML file 'WebBrowser.fxml'.";
    assert x3 != null : "fx:id=\"x3\" was not injected: check your FXML file 'WebBrowser.fxml'.";
    assert x4 != null : "fx:id=\"x4\" was not injected: check your FXML file 'WebBrowser.fxml'.";

    webEngine = mainWebView.getEngine();
    refreshWebView();
  }

  @FXML
  void launchWiki(ActionEvent event) {}

  @FXML
  void refreshButton_OnAction(ActionEvent event) {
    refreshWebView();
  }

  private void refreshWebView() {
    log.info("URL: " + urlTextField.getText());
    log.debug("isJavaScriptEnabled: " + webEngine.isJavaScriptEnabled());
    webEngine.load(urlTextField.getText());
  }
}
