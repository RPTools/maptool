/**
 * Controller for the Note Book contents.
 */

package net.rptools.maptool.client.ui.notebook;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import net.rptools.maptool.model.notebook.NoteBook;

public class NoteBookContentsController {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="titleLabel"
  private TitledPane titleLabel; // Value injected by FXMLLoader

  @FXML // fx:id="nameLabel"
  private Label nameLabel; // Value injected by FXMLLoader

  @FXML // fx:id="versionLabel"
  private Label versionLabel; // Value injected by FXMLLoader

  @FXML // fx:id="namespaceLabel"
  private Label namespaceLabel; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert versionLabel != null : "fx:id=\"versionLabel\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert nameLabel != null : "fx:id=\"nameLabel\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert namespaceLabel != null : "fx:id=\"namespaceLabel\" was not injected: check your FXML file 'NoteBookContents.fxml'.";

  }

  void setNoteBook(NoteBook noteBook) {
    titleLabel.setText(noteBook.getName() + " (" + noteBook.getVersion() + ")");
    nameLabel.setText(noteBook.getName());
    versionLabel.setText(noteBook.getVersion());
    namespaceLabel.setText(noteBook.getNamespace());
  }
}
