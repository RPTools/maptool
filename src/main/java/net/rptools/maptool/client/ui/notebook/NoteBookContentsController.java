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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.notebook.NoteBook;

public class NoteBookContentsController {

  private boolean editMode;

  private boolean versionHasError = false;
  private boolean namespaceHasError = false;
  private boolean nameHasError = false;
  private boolean urlHasError = false;

  private NoteBook noteBook;

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="namespaceTextField"
  private TextField namespaceTextField; // Value injected by FXMLLoader

  @FXML // fx:id="versionTextField"
  private TextField versionTextField; // Value injected by FXMLLoader

  @FXML // fx:id="nameTextField"
  private TextField nameTextField; // Value injected by FXMLLoader

  @FXML // fx:id="authorTextField"
  private TextField authorTextField; // Value injected by FXMLLoader

  @FXML // fx:id="urlTextField"
  private TextField urlTextField; // Value injected by FXMLLoader

  @FXML // fx:id="descriptionTextArea"
  private TextArea descriptionTextArea; // Value injected by FXMLLoader

  @FXML // fx:id="dependencyTableView"
  private TableView<?> dependencyTableView; // Value injected by FXMLLoader

  @FXML // fx:id="licenseButton"
  private Button licenseButton; // Value injected by FXMLLoader

  @FXML // fx:id="readMeButton"
  private Button readMeButton; // Value injected by FXMLLoader

  @FXML // fx:id="editButton"
  private Button editButton; // Value injected by FXMLLoader

  @FXML // fx:id="okButton"
  private Button okButton; // Value injected by FXMLLoader

  @FXML // fx:id="cancelButton"
  private Button cancelButton; // Value injected by FXMLLoader

  @FXML // fx:id="nameSpaceErrorLabel"
  private Label nameSpaceErrorLabel; // Value injected by FXMLLoader

  @FXML // fx:id="versionErrorLabel"
  private Label versionErrorLabel; // Value injected by FXMLLoader

  @FXML // fx:id="urlErrorLabel"
  private Label urlErrorLabel; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert namespaceTextField != null
        : "fx:id=\"namespaceTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert versionTextField != null
        : "fx:id=\"versionTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert nameTextField != null
        : "fx:id=\"nameTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert authorTextField != null
        : "fx:id=\"authorTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert urlTextField != null
        : "fx:id=\"urlTextField\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert descriptionTextArea != null
        : "fx:id=\"descriptionTextArea\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert dependencyTableView != null
        : "fx:id=\"dependencyTableView\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert licenseButton != null
        : "fx:id=\"licenseButton\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert readMeButton != null
        : "fx:id=\"readMeButton\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert editButton != null
        : "fx:id=\"editButton\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert okButton != null
        : "fx:id=\"okButton\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert cancelButton != null
        : "fx:id=\"cancelButton\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert nameSpaceErrorLabel != null
        : "fx:id=\"nameSpaceErrorLabel\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert versionErrorLabel != null
        : "fx:id=\"versionErrorLabel\" was not injected: check your FXML file 'NoteBookContents.fxml'.";
    assert urlErrorLabel != null
        : "fx:id=\"urlErrorLabel\" was not injected: check your FXML file 'NoteBookContents.fxml'.";

    namespaceTextField
        .textProperty()
        .addListener((observable, oldValue, newValue) -> validateNamespace(newValue));
    versionTextField
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> validateVersion(newValue)));
    nameTextField
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> validateName(newValue)));
    urlTextField
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> validateURL(newValue)));

    editButton.setOnAction(e -> setEditMode(true));
    cancelButton.setOnAction(
        e -> {
          setFieldsFromNoteBook(noteBook);
          setEditMode(false);
        });
    okButton.setOnAction(
        e -> {
          updateNoteBook(noteBook);
          setFieldsFromNoteBook(noteBook);
          setEditMode(false);
        });
  }

  private void updateNoteBook(NoteBook nb) {
    noteBook.setName(nameTextField.getText());
    noteBook.setNamespace(namespaceTextField.getText());
    noteBook.setVersion(versionTextField.getText());
    noteBook.setAuthor(authorTextField.getText());
    noteBook.setURL(urlTextField.getText());
    noteBook.setDescription(descriptionTextArea.getText());
  }

  private void setFieldsFromNoteBook(NoteBook noteBook) {
    nameTextField.setText(noteBook.getName());
    versionTextField.setText(noteBook.getVersion());
    namespaceTextField.setText(noteBook.getNamespace());
    authorTextField.setText(noteBook.getAuthor());
    urlTextField.setText(noteBook.getURL());
    descriptionTextArea.setText(noteBook.getDescription());
    if (noteBook.isInternal()) {
      editButton.setDisable(true);
    } else {
      editButton.setDisable(false);
      validateInputs();
    }
  }

  public void setNoteBook(NoteBook nb) {
    noteBook = nb;
    setFieldsFromNoteBook(noteBook);
    setEditMode(false);
  }

  private void setOkButtonStatus() {
    okButton.setDisable(nameHasError | namespaceHasError | versionHasError | urlHasError);
  }

  private void validateInputs() {
    validateNamespace(namespaceTextField.getText());
    validateVersion(versionTextField.getText());
  }

  private void validateNamespace(String namespace) {
    Optional<String> error = NoteBook.nameSpaceError(namespace);
    if (error.isPresent()) {
      nameSpaceErrorLabel.setText(error.get());
    } else {
      nameSpaceErrorLabel.setText("");
    }

    namespaceHasError = error.isPresent();
    setOkButtonStatus();
  }

  private void validateVersion(String version) {
    Optional<String> error = NoteBook.versionError(version);
    if (error.isPresent()) {
      versionErrorLabel.setText(error.get());
    } else {
      versionErrorLabel.setText("");
    }

    versionHasError = error.isPresent();
    setOkButtonStatus();
  }

  private void validateURL(String url) {
    urlHasError = false;
    urlErrorLabel.setText("");
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      urlHasError = true;
      urlErrorLabel.setText(I18N.getText("noteBooks.error.url.invalidFormat"));
    }
  }

  private void validateName(String name) {
    if (name.trim().isEmpty()) {
      nameHasError = true;
    } else {
      nameHasError = false;
    }
    setOkButtonStatus();
  }

  public void setEditMode(boolean eMode) {
    editMode = eMode;
    nameTextField.setEditable(editMode);
    versionTextField.setEditable(editMode);
    namespaceTextField.setEditable(editMode);
    authorTextField.setEditable(editMode);
    urlTextField.setEditable(editMode);
    descriptionTextArea.setEditable(editMode);
    okButton.setVisible(editMode);
    cancelButton.setVisible(editMode);
    readMeButton.setVisible(!editMode);
    licenseButton.setVisible(!editMode);
    editButton.setVisible(!editMode);
  }

  public boolean isEditMode() {
    return editMode;
  }
}
