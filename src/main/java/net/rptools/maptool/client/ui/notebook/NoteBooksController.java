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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.notebook.NoteBook;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntryType;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookTableTreeModel;

public class NoteBooksController {


  private Pane noteBookContentsPane;

  private NoteBookContentsController noteBookContentsController;

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="importNoteBookButton"
  private Button importNoteBookButton; // Value injected by FXMLLoader

  @FXML // fx:id="newNoteBookButton"
  private Button newNoteBookButton; // Value injected by FXMLLoader

  @FXML // fx:id="noteBookAccordion"
  private Accordion noteBookAccordion; // Value injected by FXMLLoader

  @FXML // fx:id="contentsPane"
  private AnchorPane contentsPane; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert importNoteBookButton != null : "fx:id=\"importNoteBookButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert newNoteBookButton != null : "fx:id=\"newNoteBookButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert noteBookAccordion != null : "fx:id=\"noteBookAccordion\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert contentsPane != null : "fx:id=\"contentsPane\" was not injected: check your FXML file 'NoteBooks.fxml'.";

    NoteBook testNoteBook = NoteBook.createNoteBook("Test", "Test Desc", "1.1.1", "maptool.test", "Craig Wisniewski", "CCBY3", "wwww.somewhere.com", "Blah");
    addNoteBook(testNoteBook);


    var loader =
        new FXMLLoader(
            getClass().getResource("/net/rptools/maptool/client/ui/fxml/NoteBookContents.fxml"),
            resources);

    try {
      noteBookContentsPane = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    noteBookContentsController = loader.getController();

  }


  public void addNoteBook(NoteBook noteBook) {
    String title = noteBook.getName() + "( " + noteBook.getVersionedNameSpace() + ")";
    TreeView<NoteBookEntry> treeView = new TreeView<>();
    NoteBookTableTreeModel treeModel = NoteBookTableTreeModel.getTreeModelFor(noteBook);
    treeView.setRoot(treeModel.getRoot());
    treeView.setCellFactory(
        p ->
            new TreeCell<>() {
              @Override
              protected void updateItem(NoteBookEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                  setText("");
                  setGraphic(null);
                } else {
                  if (item.getType() == NoteBookEntryType.DIRECTORY && "/".equals(item.getName())) {
                    setText(I18N.getText("noteBooks.noteBook.treeNode"));
                  } else {
                    setText(item.getName());
                  }
                }
              }
            });
    treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    treeView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
    }));
    TitledPane titledPane = new TitledPane(title, treeView);
    titledPane.setUserData(noteBook);

    noteBookAccordion.getPanes().add(titledPane);
    noteBookAccordion.expandedPaneProperty().addListener((observable, oldValue, newValue) -> {
      NoteBook nb = (NoteBook) newValue.getUserData();
      showNoteBook(nb);
    });
  }

  private void showNoteBook(NoteBook noteBook) {
    contentsPane.getChildren().clear();
    contentsPane.getChildren().add(noteBookContentsPane);
    noteBookContentsController.setNoteBook(noteBook);
  }
}
