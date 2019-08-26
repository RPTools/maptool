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

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.log4j.Log4j2;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.fx.model.MacroEditorData;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Log4j2
public class MacroEditor_Controller {
  private WebView webView = new WebView();
  private WebEngine webEngine;
  private TreeItem<MacroEditorData> treeItemRoot = new TreeItem<>();

  @FXML private ResourceBundle resources;

  @FXML private URL location;

  @FXML private SplitPane macroEditorSplitPane;

  @FXML private AnchorPane macroListPane;
  @FXML private AnchorPane macroEditPane;
  @FXML private AnchorPane macroDetailsPane;

  @FXML private TreeTableView<MacroEditorData> macroTreeTableView;
  @FXML private TreeTableColumn<MacroEditorData, String> macroTreeTableColumn;
  @FXML private Button macroTreeRefreshButton;
  @FXML private TextField macroTreeFilterTextField;

  @FXML private Label leftStatusLabel;

  @FXML private Font x1;
  @FXML private Color x2;
  @FXML private Font x3;
  @FXML private Color x4;

  @FXML
  private void initialize() {
    assert macroEditorSplitPane != null
        : "fx:id=\"macroEditorSplitPane\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert macroListPane != null
        : "fx:id=\"macroListPane\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert macroEditPane != null
        : "fx:id=\"macroEditPane\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert macroDetailsPane != null
        : "fx:id=\"macroDetailsPane\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert macroTreeTableView != null
        : "fx:id=\"macroTreeTableView\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert macroTreeTableColumn != null
        : "fx:id=\"macroTreeTableColumn\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert macroTreeRefreshButton != null
        : "fx:id=\"macroTreeRefreshButton\" was not injected: check your FXML file 'MacroEditor.fxml'.";
    assert macroTreeFilterTextField != null
        : "fx:id=\"macroTreeFilterTextField\" was not injected: check your FXML file 'MacroEditor.fxml'.";
    assert leftStatusLabel != null
        : "fx:id=\"leftStatusLabel\" was not injected: check your FXML file 'MacroEditor.fxml'.";
    assert x1 != null
        : "fx:id=\"x1\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert x2 != null
        : "fx:id=\"x2\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert x3 != null
        : "fx:id=\"x3\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";
    assert x4 != null
        : "fx:id=\"x4\" was not injected: check your FXML file 'MacroEditor_Controller.fxml'.";

    // Defining cell content
    macroTreeTableColumn.setCellValueFactory(
        (TreeTableColumn.CellDataFeatures<MacroEditorData, String> p) ->
            new ReadOnlyStringWrapper(p.getValue().getValue().getLabel()));

    // Bind text field on change to filter tree
    macroTreeFilterTextField
        .textProperty()
        .addListener((observable, oldValue, newValue) -> filterChanged(newValue));

    macroTreeTableView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> treeTableViewSelected(newValue));

    initMacroTreeView();
    initMacroCommandView();
  }

  @FXML
  private void refreshButton_onAction(ActionEvent event) {
    macroTreeFilterTextField.clear();
    update();
  }

  @FXML
  private void launchWiki() throws URISyntaxException, IOException {
    Desktop.getDesktop().browse(new URI("http://www.lmwcs.com/rptools/wiki/Main_Page"));
  }

  private void treeTableViewSelected(TreeItem<MacroEditorData> selectedItem) {
    if (selectedItem == null) return;

    if (selectedItem.isLeaf()) {
      String command = selectedItem.getValue().getCommand();
      String macroCommandText = toJavaScriptString(command);
      String aceFunctionCall =
          "editor.setValue(" + macroCommandText + ", 0); editor.clearSelection();";

      // log.info("aceFunctionCall: " + aceFunctionCall);

      if (webEngine != null) webEngine.executeScript(aceFunctionCall);
    }
  }

  private static String toJavaScriptString(String value) {
    value =
        value
            .replace("\u0000", "\\0")
            .replace("'", "\\'")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    return "\"" + value + "\"";
  }

  public void update() {
    initMacroTreeView(); // FIXME: split init/update logic
    loadAceEditor();
  }

  private void initMacroCommandView() {
    webEngine = webView.getEngine();

    AnchorPane.setBottomAnchor(webView, 0.0);
    AnchorPane.setLeftAnchor(webView, 0.0);
    AnchorPane.setRightAnchor(webView, 0.0);
    AnchorPane.setTopAnchor(webView, 0.0);

    macroEditPane.getChildren().add(webView);

    loadAceEditor();
  }

  private void loadAceEditor() {
    // File aceEditor = new File("build-resources/ace-builds/editor.html");
    File aceEditor = new File("build-resources/macro-ace-editor.html");
    webEngine.load(aceEditor.toURI().toString());
  }

  private void initMacroTreeView() {
    TreeItem<MacroEditorData> treeItemCampaign = createItemCategory(treeItemRoot, "Campaign");
    TreeItem<MacroEditorData> treeItemLibs = createItemCategory(treeItemRoot, "Libraries");
    TreeItem<MacroEditorData> treeItemNPCs = createItemCategory(treeItemRoot, "NPCs");
    TreeItem<MacroEditorData> treeItemPCs = createItemCategory(treeItemRoot, "PCs");
    TreeItem<MacroEditorData> treeItemGlobal = createItemCategory(treeItemRoot, "Global");

    // Add Campaign Macros
    MapTool.getCampaign()
        .getMacroButtonPropertiesArray()
        .forEach(macroButtonProperties -> createItem(treeItemCampaign, macroButtonProperties));

    // Add Global Macros
    MacroButtonPrefs.getButtonProperties()
        .forEach(macroButtonProperties -> createItem(treeItemGlobal, macroButtonProperties));

    // Add Token Macros
    List<ZoneRenderer> zoneRenderers = MapTool.getFrame().getZoneRenderers();
    for (ZoneRenderer zoneRenderer : zoneRenderers) {
      Zone zone = zoneRenderer.getZone();
      if (zone == null) continue;

      for (Token token : zone.getAllTokens()) {
        // Don't add tokens if they contain no macros
        if (token.getMacroList(true).isEmpty()) continue;

        String tokenName = getFormattedTokenName(token);
        String mapName = zone.getName();

        TreeItem<MacroEditorData> treeItem = new TreeItem<>();

        if (tokenName.matches("(?i)^lib:.*"))
          treeItem = createItemCategory(treeItemLibs, tokenName);
        else if (token.getType() == Token.Type.NPC)
          treeItem = createItemCategory(createItemCategory(treeItemNPCs, mapName), tokenName);
        else if (token.getType() == Token.Type.PC)
          treeItem = createItemCategory(createItemCategory(treeItemPCs, mapName), tokenName);

        addTokenMacros(token, treeItem);
      }
    }

    // Add them all to the root
    //    treeItemRoot
    //        .getChildren()
    //        .setAll(treeItemCampaign, treeItemLibs, treeItemNPCs, treeItemPCs, treeItemGlobal);

    treeItemRoot.getChildren().clear();

    if (!treeItemCampaign.isLeaf()) treeItemRoot.getChildren().add(treeItemCampaign);
    if (!treeItemLibs.isLeaf()) treeItemRoot.getChildren().add(treeItemLibs);
    if (!treeItemNPCs.isLeaf()) treeItemRoot.getChildren().add(treeItemNPCs);
    if (!treeItemPCs.isLeaf()) treeItemRoot.getChildren().add(treeItemPCs);
    if (!treeItemGlobal.isLeaf()) treeItemRoot.getChildren().add(treeItemGlobal);

    // If we decide to make a item expanded on start...
    // treeItemLibs.setExpanded(true);

    macroTreeTableView.setRoot(treeItemRoot);
    macroTreeTableView.setShowRoot(false);
  }

  private void addTokenMacros(Token token, TreeItem<MacroEditorData> tokenGroupTreeItem) {
    for (MacroButtonProperties macroButtonProperties : token.getMacroList(true)) {
      MacroEditorData macroData = new MacroEditorData();
      macroData.setLabel(macroButtonProperties.getLabel());
      macroData.setMacroGroup(macroButtonProperties.getGroup());
      macroData.setCommand(macroButtonProperties.getCommand());

      if (!macroData.getMacroGroup().isEmpty())
        createItem(createItemCategory(tokenGroupTreeItem, macroData.getMacroGroup()), macroData);
      else createItem(tokenGroupTreeItem, macroData);
    }
  }

  private TreeItem<MacroEditorData> createItem(
      TreeItem<MacroEditorData> parent, MacroButtonProperties macroButtonProperties) {

    String group = macroButtonProperties.getGroup();

    MacroEditorData macroEditorData =
        new MacroEditorData(
            macroButtonProperties.getLabel(),
            macroButtonProperties.getGroup(),
            macroButtonProperties.getCommand(),
            "");

    if (!group.isEmpty()) return createItem(createItemCategory(parent, group), macroEditorData);
    else return createItem(parent, macroEditorData);
  }

  private TreeItem<MacroEditorData> createItem(
      TreeItem<MacroEditorData> parent, MacroEditorData macroEditorData) {

    TreeItem<MacroEditorData> treeItem = getTreeViewItem(parent, macroEditorData);

    if (treeItem == null) {
      treeItem = new TreeItem<>();
      treeItem.setValue(macroEditorData);
      // treeItem.setExpanded(true);

      File file =
          new File(
              "D:/Google Drive/Map Tool Resources/! Resources !/Token States/#Macro Buttons/exit.png");

      if (macroEditorData.getLabel().contains("<img"))
        file =
            new File(
                "D:/Google Drive/Map Tool Resources/! Resources !/Token States/#Macro Buttons/breath-weapon.png");

      Image image = new Image(file.toURI().toString(), false);
      ImageView imageView = new ImageView(image);
      imageView.setFitWidth(20);
      imageView.setFitHeight(20);

      treeItem.setGraphic(imageView);

      parent.getChildren().add(treeItem);
    }

    return treeItem;
  }

  private TreeItem<MacroEditorData> createItemCategory(
      TreeItem<MacroEditorData> parent, String treeItemName) {

    MacroEditorData macroEditorData = new MacroEditorData();
    macroEditorData.setLabel(treeItemName);

    // Check if treeItem already exists, if so reuse it
    TreeItem<MacroEditorData> treeItem = getTreeViewItem(parent, macroEditorData);

    if (treeItem == null) {
      treeItem = new TreeItem<>();
      treeItem.setValue(macroEditorData);
      // treeItem.setExpanded(true);

      parent.getChildren().add(treeItem);
    }

    return treeItem;
  }

  protected void addColumn(String label) {
    TreeTableColumn<MacroEditorData, String> column = new TreeTableColumn<>(label);
    column.setPrefWidth(150);
    column.setCellValueFactory(
        (TreeTableColumn.CellDataFeatures<MacroEditorData, String> param) -> {
          ObservableValue<String> result = new ReadOnlyStringWrapper("");
          if (param.getValue().getValue() != null) {
            result = new ReadOnlyStringWrapper("" + param.getValue().getValue().getLabel());
          }

          return result;
        });

    macroTreeTableView.getColumns().add(column);
  }

  private TreeItem getTreeViewItem(TreeItem<MacroEditorData> item, MacroEditorData value) {
    if (item.getValue() == null) return null;

    if (item != null && item.getValue().equals(value)) return item;

    for (TreeItem<MacroEditorData> child : item.getChildren()) {
      TreeItem<MacroEditorData> s = getTreeViewItem(child, value);
      if (s != null) return s;
    }

    return null;
  }

  private void filterChanged(String filter) {
    if (filter.isEmpty()) {
      macroTreeTableView.setRoot(treeItemRoot);
    } else {
      TreeItem<MacroEditorData> filteredRoot = new TreeItem<>();
      filter(treeItemRoot, filter, filteredRoot);
      macroTreeTableView.setRoot(filteredRoot);
    }
  }

  private void filter(
      TreeItem<MacroEditorData> root, String filter, TreeItem<MacroEditorData> filteredRoot) {
    for (TreeItem<MacroEditorData> child : root.getChildren()) {
      TreeItem<MacroEditorData> filteredChild = new TreeItem<>();
      filteredChild.setValue(child.getValue());
      filteredChild.setExpanded(true);
      filter(child, filter, filteredChild);

      if (!filteredChild.getChildren().isEmpty() || isMatch(filteredChild.getValue(), filter)) {
        log.debug(filteredChild.getValue().getLabel() + " matches.");
        filteredRoot.getChildren().add(filteredChild);
      }
    }
  }

  private boolean isMatch(MacroEditorData macroEditorData, String filter) {
    return macroEditorData.getLabel().toLowerCase().contains(filter.toLowerCase());
  }

  private String getFormattedTokenName(Token token) {
    String tokenName = token.getName();
    if (token.getGMName() != null)
      if (!token.getGMName().isEmpty() && token.getGMName() != token.getName())
        tokenName += " (" + token.getGMName() + ")";

    return tokenName;
  }

  private void filterTree() {
    //    ObservableList<YourObjectClass> actualList = ...;
    //    FilteredList<YourObjectClass> filteredList = new FilteredList<>(actualList);
    //
    //    TableView table = ...;
    //    table.setItems(filteredList);
    //
    //// to filter
    //    filteredList.setPredicate(
    //            new Predicate<YourObjectClass>(){
    //              public boolean test(YourObjectClass t){
    //                return false; // or true
    //              }
    //            }
    //    );
  }

  //  // TODO PLACEHOLDER
  //  private void hideSplitPaneTest() {
  //    //    Node splitPane;
  //    //    for (Node node : splitPane.lookupAll(".split-pane-divider")) {
  //    //      node.setVisible(false);
  //    //    }
  //  }

  // For alt info:
  // https://stackoverflow.com/questions/37955425/override-treetablecell-rendering-in-javafx

  //  private static class MacroTreeCell extends TreeCell<MacroEditorData> {
  //    Label label;
  //
  //    public MacroTreeCell() {
  //      label = new Label();
  //    }
  //
  //    @Override
  //    protected void updateItem(MacroButtonProperties item, boolean empty) {
  //      super.updateItem(item, empty);
  //      if (!empty && item != null) {
  //        label.setText(item.getLabel());
  //        setGraphic(label);
  //      }
  //    }
  //  }

  //  private void initMacroCommandViewRSyntax() {
  //    AbstractTokenMakerFactory atmf =
  //            (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
  //    atmf.putMapping(
  //            "text/MapToolScript", "net.rptools.maptool.client.ui.syntax.MapToolScriptSyntax");
  //
  //    // Expanding use of tooltip - already accepts HTML so lets show it
  //    //    getToolTipTextField().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
  //    //    getToolTipTextField().setLineWrap(true);
  //    //    getToolTipTextField().setWrapStyleWord(true);
  //    //    getToolTipTextField().setTabSize(2);
  //
  //    // Macro Editor setup
  //    macroEditorRSyntaxTextArea.setSyntaxEditingStyle("text/MapToolScript");
  //
  //    macroEditorRSyntaxTextArea.setEditable(true);
  //    macroEditorRSyntaxTextArea.setCodeFoldingEnabled(true);
  //    macroEditorRSyntaxTextArea.setLineWrap(true);
  //    macroEditorRSyntaxTextArea.setWrapStyleWord(true);
  //    macroEditorRSyntaxTextArea.setTabSize(4);
  //
  //    FoldParserManager.get().addFoldParserMapping("text/MapToolScript", new CurlyFoldParser());
  //
  //    //
  // https://stackoverflow.com/questions/39613186/how-to-add-keywords-for-rsyntaxtextarea-for-syntax-highlighting
  //    CompletionProvider provider = new MapToolScriptAutoComplete().get();
  //    AutoCompletion ac = new AutoCompletion(provider);
  //    ac.setAutoCompleteEnabled(true);
  //    ac.setAutoActivationEnabled(true);
  //    ac.setAutoActivationDelay(500);
  //    ac.setShowDescWindow(true);
  //    ac.setAutoCompleteSingleChoices(false);
  //    ac.install(macroEditorRSyntaxTextArea);
  //
  //    // Set the color style via Theme
  //    try {
  //      File themeFile =
  //              new File(AppConstants.THEMES_DIR, AppPreferences.getDefaultMacroEditorTheme() +
  // ".xml");
  //      Theme theme = Theme.load(new FileInputStream(themeFile));
  //      theme.apply(macroEditorRSyntaxTextArea);
  //      //      theme.apply(getToolTipTextField());
  //
  //      macroEditorRSyntaxTextArea.revalidate();
  //    } catch (IOException e) {
  //      e.printStackTrace();
  //    }
  //
  //    // Listen for changes in the text
  //    macroEditorRSyntaxTextArea
  //            .getDocument()
  //            .addDocumentListener(
  //                    new DocumentListener() {
  //                      public void changedUpdate(DocumentEvent e) {
  //                        leftStatusLabel.setText("Ready 1");
  //                      }
  //
  //                      public void removeUpdate(DocumentEvent e) {
  //                        leftStatusLabel.setText("Ready 2");
  //                      }
  //
  //                      public void insertUpdate(DocumentEvent e) {
  //                        leftStatusLabel.setText("Ready 3");
  //                      }
  //                    });
  //
  //    csp = new CollapsibleSectionPanel();
  //    //    ((GridView) panel.getComponentByName("macroEditorPanel")).add(csp);
  //
  //    csp.add(new ErrorStrip(macroEditorRSyntaxTextArea), BorderLayout.LINE_END);
  //
  //    RTextScrollPane macroEditorRTextScrollPane = new
  // RTextScrollPane(macroEditorRSyntaxTextArea);
  //    macroEditorRTextScrollPane.setLineNumbersEnabled(true);
  //    // replaceComponent("macroEditorPanel", "macroEditorRTextScrollPane",
  //    // macroEditorRTextScrollPane);
  //
  //    //    csp.add(new ErrorStrip(getToolTipTextField()), BorderLayout.LINE_END);
  //
  //    csp.add(macroEditorRTextScrollPane);
  //
  //    final SwingNode rSyntaxSwingNode = new SwingNode();
  //    createSwingContent(rSyntaxSwingNode, csp);
  //
  //    AnchorPane.setBottomAnchor(rSyntaxSwingNode, 0.0);
  //    AnchorPane.setLeftAnchor(rSyntaxSwingNode, 0.0);
  //    AnchorPane.setRightAnchor(rSyntaxSwingNode, 0.0);
  //    AnchorPane.setTopAnchor(rSyntaxSwingNode, 0.0);
  //
  //    macroEditPane.getChildren().add(rSyntaxSwingNode);
  //  }
  //
  //  private void createSwingContent(SwingNode rSyntaxSwingNode, CollapsibleSectionPanel csp) {
  //    SwingUtilities.invokeLater(() -> rSyntaxSwingNode.setContent(csp));
  //  }

}
