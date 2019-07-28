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
package net.rptools.maptool.client.ui.fx.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;

public class MacroProperties_Controller {

  @FXML private ResourceBundle resources;

  @FXML private URL location;

  @FXML private TitledPane macroDetailsTitledPane;

  @FXML private Label buttonLabel;

  @FXML private Label wdithLabel;

  @FXML private Label fontLabel;

  @FXML private Label hotKeyLabel;

  @FXML private Label sortOrder;

  @FXML private CheckBox displayHotkeyCheckbox;

  @FXML private CheckBox includeLabelCheckbox;

  @FXML private CheckBox autoExecuteCheckbox;

  @FXML private CheckBox applyToSelectedTokensCheckbox;

  @FXML private HBox widthHbox;

  @FXML private HBox fontHbox;

  @FXML private TitledPane macroCommonalityTitledPane;

  @FXML
  void initialize() {
    assert macroDetailsTitledPane != null
        : "fx:id=\"macroDetailsTitledPane\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert buttonLabel != null
        : "fx:id=\"buttonLabel\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert wdithLabel != null
        : "fx:id=\"wdithLabel\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert fontLabel != null
        : "fx:id=\"fontLabel\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert hotKeyLabel != null
        : "fx:id=\"hotKeyLabel\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert sortOrder != null
        : "fx:id=\"sortOrder\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert displayHotkeyCheckbox != null
        : "fx:id=\"displayHotkeyCheckbox\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert includeLabelCheckbox != null
        : "fx:id=\"includeLabelCheckbox\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert autoExecuteCheckbox != null
        : "fx:id=\"autoExecuteCheckbox\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert applyToSelectedTokensCheckbox != null
        : "fx:id=\"applyToSelectedTokensCheckbox\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert widthHbox != null
        : "fx:id=\"widthHbox\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert fontHbox != null
        : "fx:id=\"fontHbox\" was not injected: check your FXML file 'MacroProperties.fxml'.";
    assert macroCommonalityTitledPane != null
        : "fx:id=\"macroCommonalityTitledPane\" was not injected: check your FXML file 'MacroProperties.fxml'.";

    // You can set this in FXML via expandedPane="$macroDetailsTitledPane"...
    // but scene builder will remove it :(
    macroDetailsTitledPane.setExpanded(true);
  }
}
