/**
 * Sample Skeleton for 'NoteBookContents.fxml' Controller Class
 */

package net.rptools.maptool.client.ui.notebook;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import net.rptools.maptool.model.notebook.NoteBook;

public class NoteBookContentsController {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="nameTextField"
  private TextField nameTextField; // Value injected by FXMLLoader

  @FXML // fx:id="versionTextField"
  private TextField versionTextField; // Value injected by FXMLLoader

  @FXML // fx:id="namespaceTextField"
  private TextField namespaceTextField; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert nameTextField != null : "fx:id=\"nameTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert versionTextField != null : "fx:id=\"versionTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert namespaceTextField != null : "fx:id=\"namespaceTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";

  }

  public void setNoteBook(NoteBook noteBook) {
    nameTextField.setText(noteBook.getName());
    versionTextField.setText(noteBook.getVersion());
    namespaceTextField.setText(noteBook.getNamespace());
  }
}
