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
package net.rptools.maptool.client.ui;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.GridView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.swing.preference.WindowPreferences;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.FormPanelI18N;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.client.ui.syntax.MapToolScriptAutoComplete;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

public class MacroButtonDialog extends JDialog implements SearchListener {

  public static final String DEFAULT_COLOR_NAME = "default";

  private static final long serialVersionUID = 8228617911117087993L;
  private final FormPanel panel;
  MacroButton button;
  MacroButtonProperties properties;
  boolean isTokenMacro = false;
  int oldHashCode = 0;
  Boolean startingCompareGroup;
  Boolean startingCompareSortPrefix;
  Boolean startingCompareCommand;
  Boolean startingCompareIncludeLabel;
  Boolean startingCompareAutoExecute;
  Boolean startingCompareApplyToSelectedTokens;
  Boolean startingAllowPlayerEdits;

  private final RSyntaxTextArea macroEditorRSyntaxTextArea = new RSyntaxTextArea(2, 2);
  private CollapsibleSectionPanel csp;
  private FindDialog findDialog;
  private ReplaceDialog replaceDialog;
  private FindToolBar findToolBar;
  private ReplaceToolBar replaceToolBar;
  private JLabel status;
  private static final String READY = I18N.getText("Label.ready");
  private static final String SAVED = I18N.getText("Label.saved");

  private static final Set<String> openMacroList = new HashSet<String>(4);

  public MacroButtonDialog() {
    super(MapTool.getFrame(), "", true);
    this.setModalityType(ModalityType.MODELESS);

    panel = new FormPanelI18N("net/rptools/maptool/client/ui/forms/macroButtonDialog.xml");
    setContentPane(panel);
    setSize(700, 400);

    installRunButton();
    installApplyButton();
    installOKButton();
    installCancelButton();
    installHotKeyCombo();
    installColorCombo();
    installFontColorCombo();
    installFontSizeCombo();

    initCommandTextArea();
    initSearchDialogs();

    createMenuBar();

    panel.getCheckBox("applyToTokensCheckBox").setEnabled(!isTokenMacro);
    panel.getComboBox("hotKey").setEnabled(!isTokenMacro);
    // can't get max-width to work, so temporarily disabling it.
    panel.getTextField("maxWidth").setEnabled(false);
    panel.getCheckBox("allowPlayerEditsCheckBox").setEnabled(MapTool.getPlayer().isGM());

    new WindowPreferences(AppConstants.APP_NAME, "editMacroDialog", this);
    SwingUtil.centerOver(this, MapTool.getFrame());

    // Capture all close events (including red X) so we can maintain the
    // list of open macros
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            destroyMenuBar();
            updateOpenMacroList(false);
            // FJE fix for macOS pinwheel
            SwingUtilities.invokeLater(() -> dispose());
          }
        });
  }

  /** Listens for events from our search dialogs and actually does the dirty work. */
  @Override
  public void searchEvent(SearchEvent e) {
    SearchEvent.Type type = e.getType();
    SearchContext context = e.getSearchContext();
    SearchResult result = null;

    switch (type) {
      default: // Prevent FindBugs warning later
      case MARK_ALL:
        result = SearchEngine.markAll(macroEditorRSyntaxTextArea, context);
        break;
      case FIND:
        result = SearchEngine.find(macroEditorRSyntaxTextArea, context);
        if (!result.wasFound()) {
          UIManager.getLookAndFeel().provideErrorFeedback(macroEditorRSyntaxTextArea);
        }
        break;
      case REPLACE:
        result = SearchEngine.replace(macroEditorRSyntaxTextArea, context);
        if (!result.wasFound()) {
          UIManager.getLookAndFeel().provideErrorFeedback(macroEditorRSyntaxTextArea);
        }
        break;
      case REPLACE_ALL:
        result = SearchEngine.replaceAll(macroEditorRSyntaxTextArea, context);
        JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
        break;
    }

    String text = null;
    if (result.wasFound()) {
      text = I18N.getText("component.msg.macro.textfound", result.getMarkedCount());
    } else if (type == SearchEvent.Type.MARK_ALL) {
      if (result.getMarkedCount() > 0) {
        text = I18N.getText("component.msg.macro.occurrences", result.getMarkedCount());
      } else {
        text = "";
      }
    } else {
      text = I18N.getText("component.msg.macro.notfound");
    }

    status.setText(text);
  }

  @Override
  public String getSelectedText() {
    return macroEditorRSyntaxTextArea.getSelectedText();
  }

  /**
   * @param id the id to look for
   * @return whether the macro dialog is already opened.
   */
  public static boolean isMacroDialogOpen(String id) {
    return openMacroList.contains(id);
  }

  private void installHotKeyCombo() {
    String[] hotkeys = MacroButtonHotKeyManager.HOTKEYS;
    JComboBox<String> combo = getHotKeyCombo();
    for (int i = 0; i < hotkeys.length; i++) combo.insertItemAt(hotkeys[i], i);
  }

  private void installColorCombo() {
    JComboBox<String> combo = getColorComboBox();
    var colorNamesSet = MapToolUtil.getColorNames();
    var colorNamesArrayList = new ArrayList<String>();
    colorNamesArrayList.add(DEFAULT_COLOR_NAME);
    colorNamesArrayList.addAll(colorNamesSet);
    String[] colorNamesArray = colorNamesArrayList.toArray(new String[0]);
    combo.setModel(new DefaultComboBoxModel<>(colorNamesArray));
    combo.setSelectedItem(DEFAULT_COLOR_NAME);
    combo.setRenderer(
        new ColorComboBoxRenderer(
            DEFAULT_COLOR_NAME,
            UIManager.getColor("Button" + ".background"),
            UIManager.getColor("Button.foreground")));
  }

  private void installFontColorCombo() {
    JComboBox<String> combo = getFontColorComboBox();
    var colorNamesArrayList = new ArrayList<String>();
    colorNamesArrayList.add(DEFAULT_COLOR_NAME);
    colorNamesArrayList.addAll(Arrays.asList(MacroButtonProperties.getFontColors()));
    var colorNamesArray = colorNamesArrayList.toArray(new String[0]);
    combo.setModel(new DefaultComboBoxModel<>(colorNamesArray));
    combo.setSelectedItem(DEFAULT_COLOR_NAME);
    combo.setRenderer(
        new ColorComboBoxRenderer(
            DEFAULT_COLOR_NAME,
            UIManager.getColor("Button" + ".foreground"),
            UIManager.getColor("Button.background")));
  }

  private void installFontSizeCombo() {
    String[] fontSizes = {
      "0.75em", "0.80em", "0.85em", "0.90em", "0.95em", "1.00em", "1.05em", "1.10em", "1.15em",
      "1.20em", "1.25em"
    };
    // String[] fontSizes = { "6pt", "7pt", "8pt", "9pt", "10pt", "11pt",
    // "12pt", "13pt", "14pt", "15pt", "16pt" };
    JComboBox<String> combo = getFontSizeComboBox();
    combo.setModel(new DefaultComboBoxModel<String>(fontSizes));
  }

  private void installRunButton() {
    JButton button = (JButton) panel.getButton("runButton");
    button.addActionListener(
        e -> {
          save(false);

          if (properties.getApplyToTokens() || properties.getCommonMacro()) {
            if (MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList().size() > 0) {
              properties.executeMacro(
                  MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList());
            }
          } else {
            properties.executeMacro();
          }
        });
  }

  private void installApplyButton() {
    JButton button = (JButton) panel.getButton("applyButton");
    button.addActionListener(e -> save(false));
  }

  private void installOKButton() {
    JButton button = (JButton) panel.getButton("okButton");
    button.addActionListener(e -> save(true));

    // getRootPane().setDefaultButton(button);
  }

  private void installCancelButton() {
    JButton button = (JButton) panel.getButton("cancelButton");
    button.addActionListener(e -> cancel());
  }

  private void updateOpenMacroList(boolean addToList) {
    String id = button.getProperties().getMacroUUID();

    if (addToList) {
      openMacroList.add(id);
    } else {
      openMacroList.remove(id);
    }
  }

  public void show(MacroButton button) {
    String id = button.getProperties().getMacroUUID();

    if (openMacroList.contains(id)) {
      return;
    }

    initI18NSupport();
    this.button = button;
    updateOpenMacroList(true);
    this.isTokenMacro = button.getToken() != null;
    this.properties = button.getProperties();

    if (properties != null) {
      oldHashCode = properties.hashCodeForComparison();
      Boolean playerCanEdit = !MapTool.getPlayer().isGM() && properties.getAllowPlayerEdits();
      boolean onGlobalPanel = properties.getSaveLocation().equals("Global");
      boolean allowEdits = onGlobalPanel || MapTool.getPlayer().isGM() || playerCanEdit;
      boolean isCommonMacro =
          button.getPanelClass().equals("SelectionPanel")
              && MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(properties);
      if (allowEdits) {
        this.setTitle(
            properties.getLabel()
                + " - ["
                + I18N.getText("component.dialogTitle.macro.macroID")
                + ": "
                + id
                + "]");

        getColorComboBox().setSelectedItem(properties.getColorKey());
        getHotKeyCombo().setSelectedItem(properties.getHotKey());
        getLabelTextField().setText(properties.getLabel());
        getGroupTextField().setText(properties.getGroup());
        getSortbyTextField().setText(properties.getSortby());
        getCommandTextArea().setText(properties.getCommand());
        getCommandTextArea().setCaretPosition(0);
        getCommandTextArea().discardAllEdits();
        getAutoExecuteCheckBox().setSelected(properties.getAutoExecute());
        getIncludeLabelCheckBox().setSelected(properties.getIncludeLabel());
        getApplyToTokensCheckBox().setSelected(properties.getApplyToTokens());
        getFontColorComboBox().setSelectedItem(properties.getFontColorKey());
        getFontSizeComboBox().setSelectedItem(properties.getFontSize());
        getMinWidthTextField().setText(properties.getMinWidth());
        getMaxWidthTextField().setText(properties.getMaxWidth());
        getCompareGroupCheckBox().setSelected(properties.getCompareGroup());
        getCompareSortPrefixCheckBox().setSelected(properties.getCompareSortPrefix());
        getCompareCommandCheckBox().setSelected(properties.getCompareCommand());
        getCompareIncludeLabelCheckBox().setSelected(properties.getCompareIncludeLabel());
        getCompareAutoExecuteCheckBox().setSelected(properties.getCompareAutoExecute());
        getCompareApplyToSelectedTokensCheckBox()
            .setSelected(properties.getCompareApplyToSelectedTokens());
        getAllowPlayerEditsCheckBox().setSelected(properties.getAllowPlayerEdits());
        getToolTipTextField().setText(properties.getToolTip());
        getDisplayHotkeyCheckBox().setSelected(properties.getDisplayHotKey());

        if (isCommonMacro) {
          getColorComboBox().setEnabled(false);
          getHotKeyCombo().setEnabled(false);
          getGroupTextField().setEnabled(properties.getCompareGroup());
          getSortbyTextField().setEnabled(properties.getCompareSortPrefix());
          getCommandTextArea().setEnabled(properties.getCompareCommand());
          getAutoExecuteCheckBox().setEnabled(properties.getCompareAutoExecute());
          getIncludeLabelCheckBox().setEnabled(properties.getCompareIncludeLabel());
          getApplyToTokensCheckBox().setEnabled(properties.getCompareApplyToSelectedTokens());
          getFontColorComboBox().setEnabled(false);
          getFontSizeComboBox().setEnabled(false);
          getMinWidthTextField().setEnabled(false);
          getMaxWidthTextField().setEnabled(false);
        }
        startingCompareGroup = properties.getCompareGroup();
        startingCompareSortPrefix = properties.getCompareSortPrefix();
        startingCompareCommand = properties.getCompareCommand();
        startingCompareAutoExecute = properties.getCompareAutoExecute();
        startingCompareIncludeLabel = properties.getCompareIncludeLabel();
        startingCompareApplyToSelectedTokens = properties.getCompareApplyToSelectedTokens();
        startingAllowPlayerEdits = properties.getAllowPlayerEdits();

        setVisible(true);
      } else {
        MapTool.showWarning(I18N.getText("msg.warning.macro.playerChangesNotAllowed"));
      }
    } else {
      MapTool.showError(I18N.getText("msg.error.macro.buttonPropsAreNull"));
    }
  }

  private void initCommandTextArea() {
    AbstractTokenMakerFactory atmf =
        (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
    atmf.putMapping(
        "text/MapToolScript", "net.rptools.maptool.client.ui.syntax.MapToolScriptSyntax");

    // Expanding use of tooltip - already accepts HTML so lets show it
    getToolTipTextField().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
    getToolTipTextField().setLineWrap(true);
    getToolTipTextField().setWrapStyleWord(true);
    getToolTipTextField().setTabSize(2);

    // Macro Editor setup
    macroEditorRSyntaxTextArea.setSyntaxEditingStyle("text/MapToolScript");

    macroEditorRSyntaxTextArea.setEditable(true);
    macroEditorRSyntaxTextArea.setCodeFoldingEnabled(true);
    macroEditorRSyntaxTextArea.setLineWrap(true);
    macroEditorRSyntaxTextArea.setWrapStyleWord(true);
    macroEditorRSyntaxTextArea.setTabSize(4);

    FoldParserManager.get().addFoldParserMapping("text/MapToolScript", new CurlyFoldParser());

    // https://stackoverflow.com/questions/39613186/how-to-add-keywords-for-rsyntaxtextarea-for-syntax-highlighting
    CompletionProvider provider = new MapToolScriptAutoComplete().get();
    AutoCompletion ac = new AutoCompletion(provider);
    ac.setAutoCompleteEnabled(true);
    ac.setAutoActivationEnabled(true);
    ac.setAutoActivationDelay(500);
    ac.setShowDescWindow(true);
    ac.setAutoCompleteSingleChoices(false);
    ac.install(macroEditorRSyntaxTextArea);

    // Set the color style via Theme
    try {
      File themeFile =
          new File(AppConstants.THEMES_DIR, AppPreferences.getDefaultMacroEditorTheme() + ".xml");
      Theme theme = Theme.load(new FileInputStream(themeFile));
      theme.apply(macroEditorRSyntaxTextArea);
      theme.apply(getToolTipTextField());

      macroEditorRSyntaxTextArea.revalidate();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Listen for changes in the text
    macroEditorRSyntaxTextArea
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void changedUpdate(DocumentEvent e) {
                status.setText(READY);
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                status.setText(READY);
              }

              @Override
              public void insertUpdate(DocumentEvent e) {
                status.setText(READY);
              }
            });

    csp = new CollapsibleSectionPanel();
    ((GridView) panel.getComponentByName("macroEditorPanel")).add(csp);

    csp.add(new ErrorStrip(macroEditorRSyntaxTextArea), BorderLayout.LINE_END);

    RTextScrollPane macroEditorRTextScrollPane = new RTextScrollPane(macroEditorRSyntaxTextArea);
    macroEditorRTextScrollPane.setLineNumbersEnabled(true);
    // replaceComponent("macroEditorPanel", "macroEditorRTextScrollPane",
    // macroEditorRTextScrollPane);

    csp.add(new ErrorStrip(getToolTipTextField()), BorderLayout.LINE_END);

    //    RTextScrollPane macroEditorRTextScrollPane = new
    // RTextScrollPane(macroEditorRSyntaxTextArea);
    //    macroEditorRTextScrollPane.setLineNumbersEnabled(true);

    csp.add(macroEditorRTextScrollPane);
  }

  /** Creates our Find and Replace dialogs. */
  public void initSearchDialogs() {
    findDialog = new FindDialog(this, this);
    replaceDialog = new ReplaceDialog(this, this);

    // This ties the properties of the two dialogs together (match case,
    // regex, etc.).
    SearchContext context = findDialog.getSearchContext();
    replaceDialog.setSearchContext(context);

    findToolBar = new FindToolBar(this);
    findToolBar.setSearchContext(context);
    replaceToolBar = new ReplaceToolBar(this);
    replaceToolBar.setSearchContext(context);

    status = (JLabel) panel.getComponentByName("statusBarLabel");
  }

  /**
   * Creates new menu items that are added to the "Edit" top-level menu.
   *
   * <p>This is trickier than it sounds as macOS doesn't automatically merge the per-window menu bar
   * that this method used to create to the top-of-the-screen menu bar that is the stock in trade on
   * macOS. So in order to create a portable method that works everywhere, we retrieve the "Edit"
   * JMenu and add our elements to it. When the dialog closes, we call {@link #destroyMenuBar()} to
   * remove these newly added menu items.
   *
   * @return
   */
  private void createMenuBar() {
    JMenuBar mb = MapTool.getFrame().getJMenuBar();
    for (int i = 0; i < mb.getMenuCount(); i++) {
      JMenu menu = mb.getMenu(i);
      if (menu.getText().equalsIgnoreCase(I18N.getText("menu.edit"))) {
        // This is the menu we want to add onto...
        addMenuItems(menu);
        return;
      }
    }
    throw new RuntimeException("Didn't find the 'Edit' menu?");
  }

  /**
   * Is a utility method that adds all of the menu items.
   *
   * @param menu
   */
  private void addMenuItems(JMenu menu) {
    menu.addSeparator();
    menu.add(new JMenuItem(new ShowFindDialogAction(this)));
    menu.add(new JMenuItem(new ShowReplaceDialogAction(this)));
    menu.add(new JMenuItem(new GoToLineAction()));
    menu.addSeparator();

    menu.add(bottomComponent("action.macroEditor.showFindSearchBar", findToolBar)); // shift
    // f
    menu.add(bottomComponent("action.macroEditor.showReplaceSearchBar", replaceToolBar)); // shift
    // h
  }

  /**
   * Creates the slide-up panel at the bottom of the macro editor dialog panel.
   *
   * @param key string key to lookup in the properties file (used to call {@link I18N#getKeystroke}
   *     and {@link I18N#getText}
   * @param tb the toolbar that is meant to slide up
   * @return new JMenuItem containing the new {@link Action}
   */
  private JMenuItem bottomComponent(String key, FindToolBar tb) {
    KeyStroke k = I18N.getKeystroke(key);
    Action a = csp.addBottomComponent(k, tb);
    a.putValue(Action.NAME, I18N.getText(key));
    return new JMenuItem(a);
  }

  /** Is called when the dialog is about to disappear. We use it to do any menu cleanup. */
  private void destroyMenuBar() {
    JMenuBar mb = MapTool.getFrame().getJMenuBar();
    if (mb == null) return;
    for (int i = 0; i < mb.getMenuCount(); i++) {
      JMenu menu = mb.getMenu(i);
      if (menu.getText().equalsIgnoreCase(I18N.getText("menu.edit"))) {
        // This is the menu we want to cleanup...
        removeMenuItems(menu, menu.getItemCount());
        return;
      }
    }
    throw new RuntimeException("Didn't find the 'Edit' menu?");
  }

  /**
   * Removes all menu items added when the macro editor dialog is opened.
   *
   * @param menu menu containing the items to be removed
   * @param max current number of items on the menu.
   */
  private void removeMenuItems(JMenu menu, int max) {
    // Start at the end and delete items until we find the first Action we
    // added.
    // We then delete _one more_ because we put a separator above it.
    while (max-- > 0) {
      JMenuItem mi = menu.getItem(max);
      menu.remove(max);
      if (mi != null) {
        Action a = mi.getAction();
        if (a instanceof ShowFindDialogAction) {
          // When we find the one we want, delete the separator above
          // it, and we're done.
          menu.remove(max - 1);
          return;
        }
      }
    }
  }

  private class ShowFindDialogAction extends AppActions.DefaultClientAction {
    MacroButtonDialog callingDialog;

    public ShowFindDialogAction(MacroButtonDialog macroButtonDialog) {
      init("action.macroEditor.searchFind");
      callingDialog = macroButtonDialog;
    }

    @Override
    protected void executeAction() {
      if (replaceDialog.isVisible()) {
        replaceDialog.setVisible(false);
      }
      // findDialog.setSearchString(macroEditorRSyntaxTextArea.getSelectedText());
      SwingUtil.centerOver(findDialog, callingDialog);
      findDialog.setVisible(true);
    }
  }

  private class ShowReplaceDialogAction extends AppActions.DefaultClientAction {
    MacroButtonDialog callingDialog;

    public ShowReplaceDialogAction(MacroButtonDialog macroButtonDialog) {
      init("action.macroEditor.searchReplace");
      callingDialog = macroButtonDialog;
    }

    @Override
    protected void executeAction() {
      if (findDialog.isVisible()) {
        findDialog.setVisible(false);
      }
      // findDialog.setSearchString(macroEditorRSyntaxTextArea.getSelectedText());
      SwingUtil.centerOver(replaceDialog, callingDialog);
      replaceDialog.setVisible(true);
    }
  }

  private class GoToLineAction extends AppActions.DefaultClientAction {
    public GoToLineAction() {
      init("action.macroEditor.gotoLine");
    }

    @Override
    protected void executeAction() {
      if (findDialog.isVisible()) {
        findDialog.setVisible(false);
      }
      if (replaceDialog.isVisible()) {
        replaceDialog.setVisible(false);
      }
      GoToDialog dialog = new GoToDialog(MacroButtonDialog.this);
      dialog.setMaxLineNumberAllowed(macroEditorRSyntaxTextArea.getLineCount());
      dialog.setVisible(true);
      int line = dialog.getLineNumber();
      if (line > 0) {
        try {
          macroEditorRSyntaxTextArea.setCaretPosition(
              macroEditorRSyntaxTextArea.getLineStartOffset(line - 1));
        } catch (BadLocationException ble) { // Never happens
          UIManager.getLookAndFeel().provideErrorFeedback(macroEditorRSyntaxTextArea);
          ble.printStackTrace();
        }
      }
    }
  }

  // Jamz: maybe later...
  // public void rememberLastWindowLocation() {
  // Dimension windowSize = this.getSize();
  //
  // int x = outerWindow.getLocation().x + (outerSize.width - innerSize.width)
  // / 2;
  // int y = outerWindow.getLocation().y + (outerSize.height -
  // innerSize.height) / 2;
  //
  // Jamz: For multiple monitor's, x & y can be negative values...
  // innerWindow.setLocation(x < 0 ? 0 : x, y < 0 ? 0 : y);
  // innerWindow.setLocation(x, y);
  // }

  private void save(boolean closeDialog) {
    String hotKey = getHotKeyCombo().getSelectedItem().toString();
    button.getHotKeyManager().assignKeyStroke(hotKey);
    button.setColor(getColorComboBox().getSelectedItem().toString());
    button.setText(this.button.getButtonText());
    properties.setHotKey(hotKey);
    properties.setColorKey(getColorComboBox().getSelectedItem().toString());
    properties.setLabel(getLabelTextField().getText());
    properties.setGroup(getGroupTextField().getText());
    properties.setSortby(getSortbyTextField().getText());
    properties.setCommand(getCommandTextArea().getText());
    properties.setAutoExecute(getAutoExecuteCheckBox().isSelected());
    properties.setIncludeLabel(getIncludeLabelCheckBox().isSelected());
    properties.setApplyToTokens(getApplyToTokensCheckBox().isSelected());
    properties.setFontColorKey(getFontColorComboBox().getSelectedItem().toString());
    properties.setFontSize(getFontSizeComboBox().getSelectedItem().toString());
    properties.setMinWidth(getMinWidthTextField().getText());
    properties.setMaxWidth(getMaxWidthTextField().getText());
    properties.setCompareGroup(getCompareGroupCheckBox().isSelected());
    properties.setCompareSortPrefix(getCompareSortPrefixCheckBox().isSelected());
    properties.setCompareCommand(getCompareCommandCheckBox().isSelected());
    properties.setCompareIncludeLabel(getCompareIncludeLabelCheckBox().isSelected());
    properties.setCompareAutoExecute(getCompareAutoExecuteCheckBox().isSelected());
    properties.setCompareApplyToSelectedTokens(
        getCompareApplyToSelectedTokensCheckBox().isSelected());
    properties.setAllowPlayerEdits(getAllowPlayerEditsCheckBox().isSelected());
    properties.setToolTip(getToolTipTextField().getText());
    properties.setDisplayHotKey(getDisplayHotkeyCheckBox().isSelected());

    properties.save();

    if (button.getPanelClass().equals("SelectionPanel")) {
      if (MapTool.getFrame()
          .getSelectionPanel()
          .getCommonMacros()
          .contains(button.getProperties())) {
        boolean changeAllowPlayerEdits = false;
        boolean endingAllowPlayerEdits = false;
        if (startingAllowPlayerEdits) {
          if (!properties.getAllowPlayerEdits()) {
            boolean confirmDisallowPlayerEdits =
                MapTool.confirm(I18N.getText("confirm.macro.disallowPlayerEdits"));
            if (confirmDisallowPlayerEdits) {
              changeAllowPlayerEdits = true;
              endingAllowPlayerEdits = false;
            } else {
              properties.setAllowPlayerEdits(true);
            }
          }
        } else {
          if (properties.getAllowPlayerEdits()) {
            boolean confirmAllowPlayerEdits =
                MapTool.confirm(I18N.getText("confirm.macro.allowPlayerEdits"));
            if (confirmAllowPlayerEdits) {
              changeAllowPlayerEdits = true;
              endingAllowPlayerEdits = true;
            } else {
              properties.setAllowPlayerEdits(false);
            }
          }
        }
        boolean trusted = true;
        for (Token nextToken :
            MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
          trusted = AppUtil.playerOwns(nextToken);
          boolean isGM = MapTool.getPlayer().isGM();
          for (MacroButtonProperties nextMacro : nextToken.getMacroList(trusted)) {
            if (isGM || nextMacro.getApplyToTokens()) {
              if (nextMacro.hashCodeForComparison() == oldHashCode) {
                nextMacro.setLabel(properties.getLabel());
                if (properties.getCompareGroup() && startingCompareGroup) {
                  nextMacro.setGroup(properties.getGroup());
                }
                if (properties.getCompareSortPrefix() && startingCompareSortPrefix) {
                  nextMacro.setSortby(properties.getSortby());
                }
                if (properties.getCompareCommand() && startingCompareCommand) {
                  nextMacro.setCommand(properties.getCommand());
                }
                if (properties.getCompareAutoExecute() && startingCompareAutoExecute) {
                  nextMacro.setAutoExecute(properties.getAutoExecute());
                }
                if (properties.getCompareIncludeLabel() && startingCompareIncludeLabel) {
                  nextMacro.setIncludeLabel(properties.getIncludeLabel());
                }
                if (properties.getCompareApplyToSelectedTokens()
                    && startingCompareApplyToSelectedTokens) {
                  nextMacro.setApplyToTokens(properties.getApplyToTokens());
                }
                if (changeAllowPlayerEdits) {
                  nextMacro.setAllowPlayerEdits(endingAllowPlayerEdits);
                }
                nextMacro.setCompareGroup(properties.getCompareGroup());
                nextMacro.setCompareSortPrefix(properties.getCompareSortPrefix());
                nextMacro.setCompareCommand(properties.getCompareCommand());
                nextMacro.setCompareAutoExecute(properties.getCompareAutoExecute());
                nextMacro.setCompareIncludeLabel(properties.getCompareIncludeLabel());
                nextMacro.setCompareApplyToSelectedTokens(
                    properties.getCompareApplyToSelectedTokens());
                nextMacro.save();
              }
            }
          }
        }
      }
      MapTool.getFrame().getSelectionPanel().reset();
    }
    if (button.getPanelClass().equals("CampaignPanel")) {
      MapTool.serverCommand()
          .updateCampaignMacros(MapTool.getCampaign().getMacroButtonPropertiesArray());
      MapTool.getFrame().getCampaignPanel().reset();
    }

    if (button.getPanelClass().equals("GmPanel")) {
      MapTool.serverCommand()
          .updateGmMacros(MapTool.getCampaign().getGmMacroButtonPropertiesArray());
      MapTool.getFrame().getGmPanel().reset();
    }

    if (closeDialog) {
      // setVisible(false);
      updateOpenMacroList(false);
      dispose();
    } else {
      status.setText(SAVED);
    }
  }

  private void cancel() {
    // setVisible(false);
    updateOpenMacroList(false);
    dispose();
  }

  private JCheckBox getAutoExecuteCheckBox() {
    return panel.getCheckBox("autoExecuteCheckBox");
  }

  private JCheckBox getIncludeLabelCheckBox() {
    return panel.getCheckBox("includeLabelCheckBox");
  }

  private JCheckBox getApplyToTokensCheckBox() {
    return panel.getCheckBox("applyToTokensCheckBox");
  }

  private JComboBox<String> getHotKeyCombo() {
    return panel.getComboBox("hotKey");
  }

  @SuppressWarnings("unchecked")
  private JComboBox<String> getColorComboBox() {
    var v = panel.getComboBox("colorComboBox");
    return v;
    // return panel.getComboBox("colorComboBox");
  }

  private JTextField getLabelTextField() {
    return panel.getTextField("label");
  }

  private JTextField getGroupTextField() {
    return panel.getTextField("group");
  }

  private JTextField getSortbyTextField() {
    return panel.getTextField("sortby");
  }

  private RSyntaxTextArea getCommandTextArea() {
    // return (JTextArea) panel.getTextComponent("command");
    return macroEditorRSyntaxTextArea;
  }

  private JComboBox<String> getFontColorComboBox() {
    return panel.getComboBox("fontColorComboBox");
  }

  private JComboBox<String> getFontSizeComboBox() {
    return panel.getComboBox("fontSizeComboBox");
  }

  private JTextField getMinWidthTextField() {
    return panel.getTextField("minWidth");
  }

  private JTextField getMaxWidthTextField() {
    return panel.getTextField("maxWidth");
  }

  private JCheckBox getAllowPlayerEditsCheckBox() {
    return panel.getCheckBox("allowPlayerEditsCheckBox");
  }

  private JCheckBox getDisplayHotkeyCheckBox() {
    return panel.getCheckBox("displayHotKeyCheckBox");
  }

  private RSyntaxTextArea getToolTipTextField() {
    return (RSyntaxTextArea) panel.getComponentByName("toolTip");
  }

  // Begin comparison customization

  private JCheckBox getCompareIncludeLabelCheckBox() {
    return panel.getCheckBox("commonUseIncludeLabel");
  }

  private JCheckBox getCompareAutoExecuteCheckBox() {
    return panel.getCheckBox("commonUseAutoExecute");
  }

  private JCheckBox getCompareApplyToSelectedTokensCheckBox() {
    return panel.getCheckBox("commonUseApplyToSelectedTokens");
  }

  private JCheckBox getCompareGroupCheckBox() {
    return panel.getCheckBox("commonUseGroup");
  }

  private JCheckBox getCompareSortPrefixCheckBox() {
    return panel.getCheckBox("commonUseSortPrefix");
  }

  private JCheckBox getCompareCommandCheckBox() {
    return panel.getCheckBox("commonUseCommand");
  }

  // End comparison customization

  protected void replaceComponent(String panelName, String name, Component component) {
    panel.getFormAccessor(panelName).replaceBean(name, component);
    panel.reset();
  }

  private void initI18NSupport() {
    panel.getTabbedPane("macroTabs").setTitleAt(0, I18N.getText("component.tab.macro.editor"));
    panel.getTabbedPane("macroTabs").setTitleAt(1, I18N.getText("component.tab.macro.details"));
    panel.getTabbedPane("macroTabs").setTitleAt(2, I18N.getText("component.tab.macro.options"));
    panel.getLabel("macroLabelLabel").setText(I18N.getText("component.label.macro.label") + ":");
    getLabelTextField().setToolTipText(I18N.getText("component.tooltip.macro.label"));
    panel.getLabel("macroGroupLabel").setText(I18N.getText("component.label.macro.group") + ":");
    getGroupTextField().setToolTipText(I18N.getText("component.tooltip.macro.group"));
    panel
        .getLabel("macroSortPrefixLabel")
        .setText(I18N.getText("component.label.macro.sortPrefix") + ":");
    getSortbyTextField().setToolTipText(I18N.getText("component.tooltip.macro.sortPrefix"));
    panel.getLabel("macroHotKeyLabel").setText(I18N.getText("component.label.macro.hotKey") + ":");
    getHotKeyCombo().setToolTipText(I18N.getText("component.tooltip.macro.hotKey"));
    // Jamz: FIXME need to edit border text for gridview
    // panel.getLabel("macroCommandLabel").setText(I18N.getText("component.label.macro.command"));
    panel
        .getLabel("macroButtonColorLabel")
        .setText(I18N.getText("component.label.macro.buttonColor") + ":");
    getColorComboBox().setToolTipText(I18N.getText("component.tooltip.macro.buttonColor"));
    panel
        .getLabel("macroFontColorLabel")
        .setText(I18N.getText("component.label.macro.fontColor") + ":");
    getFontColorComboBox().setToolTipText(I18N.getText("component.tooltip.macro.fontColor"));
    panel
        .getLabel("macroFontSizeLabel")
        .setText(I18N.getText("component.label.macro.fontSize") + ":");
    getFontSizeComboBox().setToolTipText(I18N.getText("component.tooltip.macro.fontSize"));
    panel
        .getLabel("macroMinWidthLabel")
        .setText(I18N.getText("component.label.macro.minWidth") + ":");
    getMinWidthTextField().setToolTipText(I18N.getText("component.tooltip.macro.minWidth"));
    panel
        .getLabel("macroMaxWidthLabel")
        .setText(I18N.getText("component.label.macro.maxWidth") + ":");
    getMaxWidthTextField().setToolTipText(I18N.getText("component.tooltip.macro.maxWidth"));
    panel
        .getLabel("macroToolTipLabel")
        .setText(I18N.getText("component.label.macro.toolTip") + ":");
    getToolTipTextField().setToolTipText(I18N.getText("component.tooltip.macro.tooltip"));
    getIncludeLabelCheckBox().setText(I18N.getText("component.label.macro.includeLabel"));
    getIncludeLabelCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.includeLabel"));
    getAutoExecuteCheckBox().setText(I18N.getText("component.label.macro.autoExecute"));
    getAutoExecuteCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.autoExecute"));
    getApplyToTokensCheckBox().setText(I18N.getText("component.label.macro.applyToSelected"));
    getApplyToTokensCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.applyToSelected"));
    getAllowPlayerEditsCheckBox().setText(I18N.getText("component.label.macro.allowPlayerEdits"));
    getAllowPlayerEditsCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.allowPlayerEdits"));
    getDisplayHotkeyCheckBox().setText(I18N.getText("component.label.macro.displayHotKey"));
    getDisplayHotkeyCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.displayHotKey"));
    ((TitledBorder) ((GridView) panel.getComponentByName("macroComparisonGridView")).getBorder())
        .setTitle(I18N.getText("component.label.macro.macroCommonality"));
    getCompareIncludeLabelCheckBox()
        .setText(I18N.getText("component.label.macro.compareUseIncludeLabel"));
    getCompareIncludeLabelCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.compareUseIncludeLabel"));
    getCompareAutoExecuteCheckBox()
        .setText(I18N.getText("component.label.macro.compareUseAutoExecute"));
    getCompareAutoExecuteCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.compareUseAutoExecute"));
    getCompareApplyToSelectedTokensCheckBox()
        .setText(I18N.getText("component.label.macro.compareApplyToSelected"));
    getCompareApplyToSelectedTokensCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.compareUseApplyToSelected"));
    getCompareGroupCheckBox().setText(I18N.getText("component.label.macro.compareUseGroup"));
    getCompareGroupCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.compareUseGroup"));
    getCompareSortPrefixCheckBox()
        .setText(I18N.getText("component.label.macro.compareUseSortPrefix"));
    getCompareSortPrefixCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.compareUseSortPrefix"));
    getCompareCommandCheckBox().setText(I18N.getText("component.label.macro.compareUseCommand"));
    getCompareCommandCheckBox()
        .setToolTipText(I18N.getText("component.tooltip.macro.compareUseCommand"));
  }
}
