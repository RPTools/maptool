/**
 * Controller for the Note Book UI
 */

package net.rptools.maptool.client.ui.notebook;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import net.rptools.maptool.model.notebook.NoteBook;

public class NoteBooksController {


  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="noteBookAccordion"
  private Accordion noteBookAccordion; // Value injected by FXMLLoader

  @FXML // fx:id="importNoteBookButton"
  private Button importNoteBookButton; // Value injected by FXMLLoader

  @FXML // fx:id="newNoteBookButton"
  private Button newNoteBookButton; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert noteBookAccordion != null : "fx:id=\"noteBookAccordion\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert importNoteBookButton != null : "fx:id=\"importNoteBookButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert newNoteBookButton != null : "fx:id=\"newNoteBookButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    NoteBook testNoteBook = NoteBook.createNoteBook("Test", "Test Desc", "1.1.1", "maptool.test");
    addNoteBook(testNoteBook);
  }


  public void addNoteBook(NoteBook noteBook) {
    String title = noteBook.getName() + "( " + noteBook.getVersionedNameSpace() + ")";
    ListView<String> listView = new ListView<>();
    listView.getItems().add(noteBook.getName());
    TitledPane titledPane = new TitledPane(title, listView);
    noteBookAccordion.getPanes().add(titledPane);
  }
}
