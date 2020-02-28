package net.rptools.maptool.client.ui.notebook;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class NoteBookController {

  @FXML
  private ResourceBundle resources;

  @FXML
  private URL location;

  @FXML
  private BorderPane noteBookPanel;

  @FXML
  private TreeTableView<?> noteBookTreeTableView;

  @FXML
  private TreeTableColumn<?, ?> groupColumn;

  @FXML
  private TreeTableColumn<?, ?> nameColumn;

  @FXML
  private TreeTableColumn<?, ?> referenceColumn;

  @FXML
  private StackPane mainViewStackPane;

  @FXML
  private AnchorPane notePane;

  @FXML
  private WebView noteWebView;

  @FXML
  private AnchorPane editorPane;

  @FXML
  private AnchorPane detailsAnchorPane;

  @FXML
  private HBox buttonHBox;

  @FXML
  void initialize() {
    assert noteBookPanel != null : "fx:id=\"noteBookPanel\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert noteBookTreeTableView != null : "fx:id=\"noteBookTreeTableView\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert groupColumn != null : "fx:id=\"groupColumn\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert nameColumn != null : "fx:id=\"nameColumn\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert referenceColumn != null : "fx:id=\"referenceColumn\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert mainViewStackPane != null : "fx:id=\"mainViewStackPane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert notePane != null : "fx:id=\"notePane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert noteWebView != null : "fx:id=\"noteWebView\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert editorPane != null : "fx:id=\"editorPane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert detailsAnchorPane != null : "fx:id=\"detailsAnchorPane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert buttonHBox != null : "fx:id=\"buttonHBox\" was not injected: check your FXML file 'NoteBook.fxml'.";

  }
}
