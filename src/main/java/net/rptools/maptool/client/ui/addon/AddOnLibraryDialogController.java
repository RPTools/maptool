/**
 * Sample Skeleton for 'AddOnLibraryDialog.fxml' Controller Class
 */

package net.rptools.maptool.client.ui.addon;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class AddOnLibraryDialogController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="addButton"
    private Button addButton; // Value injected by FXMLLoader

    @FXML // fx:id="addOnsTable"
    private TableView<?> addOnsTable; // Value injected by FXMLLoader

    @FXML // fx:id="closeButton"
    private Button closeButton; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'AddOnLibraryDialog.fxml'.";
        assert addOnsTable != null : "fx:id=\"addOnsTable\" was not injected: check your FXML file 'AddOnLibraryDialog.fxml'.";
        assert closeButton != null : "fx:id=\"closeButton\" was not injected: check your FXML file 'AddOnLibraryDialog.fxml'.";

    }

}
