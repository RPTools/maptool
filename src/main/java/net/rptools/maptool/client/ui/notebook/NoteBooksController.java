/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.notebook;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapTool.CampaignEvent;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.notebook.NoteBook;
import net.rptools.maptool.model.notebook.NoteBookManager;
import net.rptools.maptool.model.notebook.entry.DirectoryEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntryType;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookTableTreeModel;

/** Controller class for the Note Books UI. */
public class NoteBooksController {

  /** The Pane to display the different select content / content editors on. */
  private Pane noteBookContentsPane;

  /** Controller class for the UI for displaying the contents of a {@link NoteBook}. */
  private NoteBookContentsController noteBookContentsController;

  /** The next number for a new {@link @NoteBook}. */
  private int nextNewNoteBookNumber = 1;

  private NoteBook currentNoteBook;

  private final Map<NoteBook, TitledPane> noteBookTitledPaneMap = new HashMap<>();
  private final Map<NoteBook, TreeView<NoteBookEntry>> noteBookTreeMap = new HashMap<>();

  private final PropertyChangeListener noteBookManagerPCL = this::noteBookManagerChange;

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="noteBookAccordion"
  private Accordion noteBookAccordion; // Value injected by FXMLLoader

  @FXML // fx:id="newDirectoryButton"
  private Button newDirectoryButton; // Value injected by FXMLLoader

  @FXML // fx:id="newNoteButton"
  private Button newNoteButton; // Value injected by FXMLLoader

  @FXML // fx:id="importNoteBookButton"
  private Button importNoteBookButton; // Value injected by FXMLLoader

  @FXML // fx:id="newNoteBookButton"
  private Button newNoteBookButton; // Value injected by FXMLLoader

  @FXML // fx:id="contentsPane"
  private AnchorPane contentsPane; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert noteBookAccordion != null
        : "fx:id=\"noteBookAccordion\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert newDirectoryButton != null
        : "fx:id=\"newDirectoryButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert newNoteButton != null
        : "fx:id=\"newNoteButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert importNoteBookButton != null
        : "fx:id=\"importNoteBookButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert newNoteBookButton != null
        : "fx:id=\"newNoteBookButton\" was not injected: check your FXML file 'NoteBooks.fxml'.";
    assert contentsPane != null
        : "fx:id=\"contentsPane\" was not injected: check your FXML file 'NoteBooks.fxml'.";

    // Load the UI used to display the details of the Note Book    // Load the UI used to display
    // the details of the Note Book
    var loader =
        new FXMLLoader(
            getClass().getResource("/net/rptools/maptool/client/ui/fxml/NoteBookContents.fxml"),
            resources);

    try {
      noteBookContentsPane = loader.load();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    noteBookContentsController = loader.getController();
    noteBookAccordion
        .expandedPaneProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue != null) {
                NoteBook nb = (NoteBook) newValue.getUserData();
                setCurrentNoteBook(nb);
                showNoteBook(nb);
              }
            });

    newNoteBookButton.setOnAction(e -> createNewNoteBook());
    newDirectoryButton.setOnAction(e -> createNewDirectory());
    MapTool.getCampaign().getNoteBookManager().addPropertyChangeListener(noteBookManagerPCL);

    MapTool.getEventDispatcher()
        .addListener(
            e -> campaignChanged((Campaign) e.getOldValue(), (Campaign) e.getNewValue()),
            CampaignEvent.Changed);
  }

  private void setCurrentNoteBook(NoteBook nb) {
    currentNoteBook = nb;
  }

  private NoteBook getCurrentNoteBook() {
    return currentNoteBook;
  }

  private void createNewDirectory() {
    final NoteBook noteBook = getCurrentNoteBook();

    SwingUtilities.invokeLater(
        () -> {
          String dialogTitle = I18N.getText("noteBooks.dialog.createDirectory.title");
          Object result =
              JOptionPane.showInputDialog(
                  MapTool.getFrame(),
                  dialogTitle,
                  dialogTitle,
                  JOptionPane.QUESTION_MESSAGE,
                  null,
                  null,
                  "/dir");

          noteBook.putEntry(new DirectoryEntry(result.toString()));
        });
  }

  private void noteBookManagerChange(PropertyChangeEvent pce) {
    Set<NoteBook> added = new HashSet<>();
    Set<NoteBook> removed = new HashSet<>();
    if (NoteBookManager.NOTE_BOOKS_ADDED.equals(pce.getPropertyName())) {
      added.addAll((Set<NoteBook>) pce.getNewValue());
      removed.addAll((Set<NoteBook>) pce.getOldValue());
    } else if (NoteBookManager.NOTE_BOOKS_REMOVED.equals(pce.getPropertyName())) {
      removed.addAll((Set<NoteBook>) pce.getOldValue());
    }
    for (NoteBook nb : removed) {
      noteBookRemoved(nb);
    }
    for (NoteBook nb : added) {
      noteBookAdded(nb);
    }
  }

  /** Creates a new {@link NoteBook} and adds it to the campaign. */
  public void createNewNoteBook() {
    int nbNum = findNextNoteBookNumber();
    String name = I18N.getText("noteBooks.defaultNewName", nbNum);
    String nameSpace = I18N.getText("noteBooks.defaultNewNameSpace", nbNum);

    NoteBook noteBook =
        NoteBook.createNoteBook(
            name,
            "",
            "0.0.1",
            nameSpace,
            MapTool.getPlayer().getName(),
            I18N.getText("noteBooks.defaultNewLicense"),
            "",
            I18N.getText("noteBooks.defaultNewReadMe"));
    MapTool.getCampaign().getNoteBookManager().addNoteBook(noteBook);
  }

  private void campaignChanged(Campaign oldCampaign, Campaign newCampaign) {
    Platform.runLater(
        () -> {
          noteBookAccordion.getPanes().clear();
          noteBookTitledPaneMap.clear();
          noteBookTreeMap.clear();
          if (oldCampaign != null) {
            oldCampaign.getNoteBookManager().removePropertyChangeListener(noteBookManagerPCL);
          }

          if (newCampaign != null) {
            NoteBookManager noteBookManager = newCampaign.getNoteBookManager();
            for (NoteBook nb : noteBookManager.getNoteBooks()) {
              noteBookAdded(nb);
            }
            noteBookManager.addPropertyChangeListener(noteBookManagerPCL);
          }
        });
  }

  private void noteBookAdded(NoteBook noteBook) {
    Platform.runLater(
        () -> {
          // Only add if it has not been previously added.
          if (!noteBookTitledPaneMap.containsKey(noteBook)) {
            String title = noteBook.getName() + "( " + noteBook.getVersionedNameSpace() + " )";
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
                          setText(null);
                          setGraphic(null);
                        } else {
                          if (item.getType() == NoteBookEntryType.DIRECTORY
                              && "/".equals(item.getPath())) {
                            setText(I18N.getText("noteBooks.noteBook.treeNode"));
                          } else {
                            setText(item.getName());
                          }
                        }
                      }
                    });
            treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            treeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                    ((observable, oldValue, newValue) -> {
                      System.out.println(newValue.getValue().getPath());
                      // TODO: CDW
                    }));
            ScrollPane scrollPane = new ScrollPane(treeView);
            AnchorPane anchorPane = new AnchorPane(scrollPane);
            AnchorPane.setTopAnchor(scrollPane, 0.0);
            AnchorPane.setBottomAnchor(scrollPane, 0.0);
            AnchorPane.setLeftAnchor(scrollPane, 0.0);
            AnchorPane.setRightAnchor(scrollPane, 0.0);
            TitledPane titledPane = new TitledPane(title, anchorPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
            titledPane.setUserData(noteBook);
            treeView.setPrefHeight(Region.USE_COMPUTED_SIZE);

            noteBookAccordion.getPanes().add(titledPane);
            noteBookAccordion.setExpandedPane(titledPane);
            noteBookTitledPaneMap.put(noteBook, titledPane);
            noteBookTreeMap.put(noteBook, treeView);
          }
        });
  }

  private void noteBookRemoved(NoteBook noteBook) {
    if (noteBookTitledPaneMap.containsKey(noteBook)) {
      noteBookAccordion.getPanes().remove(noteBookTitledPaneMap.get(noteBook));
      noteBookTitledPaneMap.remove(noteBook);
    }
  }

  /**
   * Returns the next available new {@link NoteBook} name.
   *
   * @return the next available new {@link NoteBook} name.
   */
  private int findNextNoteBookNumber() {
    NoteBookManager noteBookManager = MapTool.getCampaign().getNoteBookManager();
    Set<String> existingNames =
        noteBookManager.getNoteBooks().stream().map(NoteBook::getName).collect(Collectors.toSet());

    // This is not the fastest way of doing things but in general its going to be more than fast
    // enough.
    String name;
    for (int i = nextNewNoteBookNumber; i < Integer.MAX_VALUE; i++) {
      name = I18N.getText("noteBooks.defaultNewName", i);
      if (!existingNames.contains(name)) {
        nextNewNoteBookNumber = i + 1;
        return i;
      }
    }

    // We tried, and honestly if we get here someone is doing something bizarre and has way too much
    // time on their hands so this is fine.
    nextNewNoteBookNumber = 0;
    return Integer.MAX_VALUE;
  }

  /**
   * Shows the {@link NoteBook} details in the content pane.
   *
   * @param noteBook the {@link NoteBook} to show the details of.
   */
  private void showNoteBook(NoteBook noteBook) {
    contentsPane.getChildren().clear();
    contentsPane.getChildren().add(noteBookContentsPane);
    noteBookContentsController.setNoteBook(noteBook);
  }
}
