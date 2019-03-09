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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashSet;
import javax.swing.AbstractAction;
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
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.swing.preference.WindowPreferences;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
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
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

public class MacroButtonDialog extends JDialog implements SearchListener {

  private static final long serialVersionUID = 8228617911117087993L;
  private FormPanel panel;
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

  private RSyntaxTextArea macroEditorRSyntaxTextArea = new RSyntaxTextArea(2, 2);
  private CollapsibleSectionPanel csp;
  private FindDialog findDialog;
  private ReplaceDialog replaceDialog;
  private FindToolBar findToolBar;
  private ReplaceToolBar replaceToolBar;
  private JLabel status;

  @SuppressWarnings("unchecked")
  private static HashSet<String> openMacroList = new HashSet(4);

  public MacroButtonDialog() {
    super(MapTool.getFrame(), "", true);
    this.setModalityType(ModalityType.MODELESS);

    panel = new FormPanel("net/rptools/maptool/client/ui/forms/macroButtonDialog.xml");
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

    setJMenuBar(createMenuBar());

    panel.getCheckBox("applyToTokensCheckBox").setEnabled(!isTokenMacro);
    panel.getComboBox("hotKey").setEnabled(!isTokenMacro);
    panel
        .getTextField("maxWidth")
        .setEnabled(false); // can't get max-width to work, so temporarily disabling it.
    panel.getCheckBox("allowPlayerEditsCheckBox").setEnabled(MapTool.getPlayer().isGM());

    new WindowPreferences(AppConstants.APP_NAME, "editMacroDialog", this);
    SwingUtil.centerOver(this, MapTool.getFrame());

    // Capture all close events (including red X) so we can maintain the list of open macros
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            updateOpenMacroList(false);
            dispose();
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
      text = "Text found; occurrences marked: " + result.getMarkedCount();
    } else if (type == SearchEvent.Type.MARK_ALL) {
      if (result.getMarkedCount() > 0) {
        text = "Occurrences marked: " + result.getMarkedCount();
      } else {
        text = "";
      }
    } else {
      text = "Text not found";
    }

    status.setText(text);
  }

  @Override
  public String getSelectedText() {
    // TODO Auto-generated method stub
    return null;
  }

  private void installHotKeyCombo() {
    String[] hotkeys = MacroButtonHotKeyManager.HOTKEYS;
    JComboBox combo = panel.getComboBox("hotKey");
    for (int i = 0; i < hotkeys.length; i++) combo.insertItemAt(hotkeys[i], i);
  }

  private void installColorCombo() {
    JComboBox combo = panel.getComboBox("colorComboBox");
    combo.setModel(new DefaultComboBoxModel(MapToolUtil.getColorNames().toArray()));
    combo.insertItemAt("default", 0);
    combo.setSelectedItem("default");
    combo.setRenderer(new ColorComboBoxRenderer());
  }

  private void installFontColorCombo() {
    JComboBox combo = panel.getComboBox("fontColorComboBox");
    combo.setModel(new DefaultComboBoxModel(MacroButtonProperties.getFontColors()));
    // combo.insertItemAt("default", 0);
    combo.setSelectedItem("black");
    combo.setRenderer(new ColorComboBoxRenderer());
  }

  private void installFontSizeCombo() {
    String[] fontSizes = {
      "0.75em", "0.80em", "0.85em", "0.90em", "0.95em", "1.00em", "1.05em", "1.10em", "1.15em",
      "1.20em", "1.25em"
    };
    // String[] fontSizes = { "6pt", "7pt", "8pt", "9pt", "10pt", "11pt", "12pt", "13pt", "14pt",
    // "15pt", "16pt" };
    JComboBox combo = panel.getComboBox("fontSizeComboBox");
    combo.setModel(new DefaultComboBoxModel(fontSizes));
  }

  private void installRunButton() {
    JButton button = (JButton) panel.getButton("runButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            save(false);

            if (properties.getApplyToTokens() || properties.getCommonMacro()) {
              if (MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList().size() > 0) {
                properties.executeMacro(
                    MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList());
              }
            } else {
              properties.executeMacro();
            }
          }
        });
  }

  private void installApplyButton() {
    JButton button = (JButton) panel.getButton("applyButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            save(false);
          }
        });
  }

  private void installOKButton() {
    JButton button = (JButton) panel.getButton("okButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            save(true);
          }
        });

    // getRootPane().setDefaultButton(button);
  }

  private void installCancelButton() {
    JButton button = (JButton) panel.getButton("cancelButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            cancel();
          }
        });
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
    this.isTokenMacro = button.getToken() == null ? false : true;
    this.properties = button.getProperties();
    oldHashCode = properties.hashCodeForComparison();

    if (properties != null) {
      Boolean playerCanEdit = !MapTool.getPlayer().isGM() && properties.getAllowPlayerEdits();
      Boolean onGlobalPanel = properties.getSaveLocation().equals("Global");
      Boolean allowEdits = onGlobalPanel || MapTool.getPlayer().isGM() || playerCanEdit;
      Boolean isCommonMacro =
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
    ac.install(macroEditorRSyntaxTextArea);

    // Set the color style via Theme
    try {
      // Theme theme =
      // Theme.load(getClass().getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/default.xml"));
      // Theme theme =
      // Theme.load(getClass().getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/dark.xml"));
      // Theme theme =
      // Theme.load(getClass().getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/eclipse.xml"));
      // Theme theme =
      // Theme.load(getClass().getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/idea.xml"));
      // Theme theme =
      // Theme.load(getClass().getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/vs.xml"));
      Theme theme =
          Theme.load(
              getClass()
                  .getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/nerps.xml"));
      // Theme theme =
      // Theme.load(getClass().getResourceAsStream("/net/rptools/maptool/client/ui/syntax/themes/nerps-dark.xml"));
      theme.apply(macroEditorRSyntaxTextArea);
      macroEditorRSyntaxTextArea.revalidate();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Listen for changes in the text
    macroEditorRSyntaxTextArea
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              public void changedUpdate(DocumentEvent e) {
                status.setText("Ready");
              }

              public void removeUpdate(DocumentEvent e) {
                status.setText("Ready");
              }

              public void insertUpdate(DocumentEvent e) {
                status.setText("Ready");
              }
            });

    csp = new CollapsibleSectionPanel();
    ((GridView) panel.getComponentByName("macroEditorPanel")).add(csp);

    ErrorStrip errorStrip = new ErrorStrip(macroEditorRSyntaxTextArea);
    csp.add(errorStrip, BorderLayout.LINE_END);

    RTextScrollPane macroEditorRTextScrollPane = new RTextScrollPane(macroEditorRSyntaxTextArea);
    macroEditorRTextScrollPane.setLineNumbersEnabled(true);
    // replaceComponent("macroEditorPanel", "macroEditorRTextScrollPane",
    // macroEditorRTextScrollPane);

    csp.add(macroEditorRTextScrollPane);
  }

  /** Creates our Find and Replace dialogs. */
  public void initSearchDialogs() {
    findDialog = new FindDialog(this, this);
    replaceDialog = new ReplaceDialog(this, this);

    // This ties the properties of the two dialogs together (match case, regex, etc.).
    SearchContext context = findDialog.getSearchContext();
    replaceDialog.setSearchContext(context);

    findToolBar = new FindToolBar(this);
    findToolBar.setSearchContext(context);
    replaceToolBar = new ReplaceToolBar(this);
    replaceToolBar.setSearchContext(context);

    status = (JLabel) panel.getComponentByName("statusBarLabel");
  }

  private JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    JMenu menu = new JMenu("Search");
    menu.add(new JMenuItem(new ShowFindDialogAction(this)));
    menu.add(new JMenuItem(new ShowReplaceDialogAction(this)));
    menu.add(new JMenuItem(new GoToLineAction()));
    menu.addSeparator();

    int ctrl = getToolkit().getMenuShortcutKeyMask();
    int shift = InputEvent.SHIFT_MASK;
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrl | shift);
    Action a = csp.addBottomComponent(ks, findToolBar);
    a.putValue(Action.NAME, "Show Find Search Bar");
    menu.add(new JMenuItem(a));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_H, ctrl | shift);
    a = csp.addBottomComponent(ks, replaceToolBar);
    a.putValue(Action.NAME, "Show Replace Search Bar");
    menu.add(new JMenuItem(a));

    mb.add(menu);

    return mb;
  }

  private class ShowFindDialogAction extends AbstractAction {
    MacroButtonDialog callingDialog;

    public ShowFindDialogAction(MacroButtonDialog macroButtonDialog) {
      super("Find...");
      int c = getToolkit().getMenuShortcutKeyMask();
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
      callingDialog = macroButtonDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (replaceDialog.isVisible()) {
        replaceDialog.setVisible(false);
      }

      // findDialog.setSearchString(macroEditorRSyntaxTextArea.getSelectedText());
      SwingUtil.centerOver(findDialog, callingDialog);
      findDialog.setVisible(true);
    }
  }

  private class ShowReplaceDialogAction extends AbstractAction {
    MacroButtonDialog callingDialog;

    public ShowReplaceDialogAction(MacroButtonDialog macroButtonDialog) {
      super("Replace...");
      int c = getToolkit().getMenuShortcutKeyMask();
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
      callingDialog = macroButtonDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (findDialog.isVisible()) {
        findDialog.setVisible(false);
      }

      // findDialog.setSearchString(macroEditorRSyntaxTextArea.getSelectedText());
      SwingUtil.centerOver(replaceDialog, callingDialog);
      replaceDialog.setVisible(true);
    }
  }

  private class GoToLineAction extends AbstractAction {
    public GoToLineAction() {
      super("Go To Line...");
      int c = getToolkit().getMenuShortcutKeyMask();
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
  // int x = outerWindow.getLocation().x + (outerSize.width - innerSize.width) / 2;
  // int y = outerWindow.getLocation().y + (outerSize.height - innerSize.height) / 2;
  //
  // // Jamz: For multiple monitor's, x & y can be negative values...
  // // innerWindow.setLocation(x < 0 ? 0 : x, y < 0 ? 0 : y);
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

    properties.save();

    if (button.getPanelClass().equals("SelectionPanel")) {
      if (MapTool.getFrame()
          .getSelectionPanel()
          .getCommonMacros()
          .contains(button.getProperties())) {
        Boolean changeAllowPlayerEdits = false;
        Boolean endingAllowPlayerEdits = false;
        if (startingAllowPlayerEdits) {
          if (!properties.getAllowPlayerEdits()) {
            Boolean confirmDisallowPlayerEdits =
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
            Boolean confirmAllowPlayerEdits =
                MapTool.confirm(I18N.getText("confirm.macro.allowPlayerEdits"));
            if (confirmAllowPlayerEdits) {
              changeAllowPlayerEdits = true;
              endingAllowPlayerEdits = true;
            } else {
              properties.setAllowPlayerEdits(false);
            }
          }
        }
        Boolean trusted = true;
        for (Token nextToken :
            MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
          if (AppUtil.playerOwns(nextToken)) {
            trusted = true;
          } else {
            trusted = false;
          }
          boolean isGM = MapTool.getPlayer().isGM();
          for (MacroButtonProperties nextMacro : nextToken.getMacroList(trusted)) {
            if (isGM || (!isGM && nextMacro.getApplyToTokens())) {
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

    if (closeDialog) {
      // setVisible(false);
      updateOpenMacroList(false);
      dispose();
    } else {
      status.setText("Saved");
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

  private JComboBox getHotKeyCombo() {
    return panel.getComboBox("hotKey");
  }

  private JComboBox getColorComboBox() {
    return panel.getComboBox("colorComboBox");
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

  private JComboBox getFontColorComboBox() {
    return panel.getComboBox("fontColorComboBox");
  }

  private JComboBox getFontSizeComboBox() {
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

  private JTextField getToolTipTextField() {
    return panel.getTextField("toolTip");
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
