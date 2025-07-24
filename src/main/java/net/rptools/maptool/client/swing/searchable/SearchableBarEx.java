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
package net.rptools.maptool.client.swing.searchable;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatPopupMenuSeparator;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import com.formdev.flatlaf.icons.FlatInternalFrameCloseIcon;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.popup.JidePopup;
import com.jidesoft.popup.JidePopupFactory;
import com.jidesoft.swing.*;
import com.jidesoft.swing.event.SearchableEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.localisedObject.LocalObject;

public class SearchableBarEx extends JToolBar implements SearchableProvider {

  private static final ImageIcon ICON_CASE = RessourceManager.getSmallIcon(Icons.ACTION_TEXT_CASE);
  private static final ImageIcon ICON_HIGHLIGHT =
      RessourceManager.getSmallIcon(Icons.ACTION_TEXT_HIGHLIGHT);
  private static final ImageIcon ICON_NEXT =
      RessourceManager.getSmallIcon(Icons.STATUSBAR_RECEIVE_ON);
  private static final ImageIcon ICON_PREV =
      RessourceManager.getSmallIcon(Icons.STATUSBAR_TRANSMIT_ON);
  private static final ImageIcon ICON_RECURSIVE =
      RessourceManager.getSmallIcon(Icons.ACTION_RECURSIVE);
  private static final ImageIcon ICON_REGEX =
      RessourceManager.getSmallIcon(Icons.ACTION_TEXT_REGEX);
  private static final ImageIcon ICON_WORDS = RessourceManager.getSmallIcon(Icons.WINDOW_LIBRARY);

  private final SearchableEx _searchable;
  private int _menuInsertPoint = 0;
  protected JLabel _statusLabel;
  protected JLabel _leadingLabel;
  protected JTextField _searchTextField;
  //     protected JComboBox<String> _comboBox;

  protected AbstractButton _closeButton;
  protected AbstractButton _findPrevButton;
  protected AbstractButton _findNextButton;
  protected AbstractButton _highlightsToggle;
  protected AbstractButton _matchCaseToggle;
  protected AbstractButton _wholeWordsToggle;
  protected AbstractButton _repeatToggle;
  protected AbstractButton _regexToggle;
  protected JideSplitButton _dropDown;

  @SuppressWarnings("unused")
  public static final int SHOW_CLOSE = 1;

  @SuppressWarnings("unused")
  public static final int SHOW_NAVIGATION = 2;

  @SuppressWarnings("unused")
  public static final int SHOW_HIGHLIGHTS = 4;

  @SuppressWarnings({"unused", "SpellCheckingInspection"})
  public static final int SHOW_MATCHCASE = 8;

  @SuppressWarnings("unused")
  public static final int SHOW_REPEATS = 16;

  @SuppressWarnings("unused")
  public static final int SHOW_STATUS = 32;

  @SuppressWarnings("unused")
  public static final int SHOW_WHOLE_WORDS = 64;

  @SuppressWarnings("unused")
  public static final int SHOW_REGEX = 128;

  @SuppressWarnings("unused")
  public static final int SHOW_ALL = -1;

  @SuppressWarnings("unused")
  public static final String PROPERTY_MAX_HISTORY_LENGTH = "maxHistoryLength";

  private int _visibleButtons;
  private boolean _compact;
  private boolean _showMatchCount;
  private JidePopup _messagePopup;
  private MouseMotionListener _mouseMotionListener;
  private KeyListener _keyListener;
  private final List<JMenuItem> _searchHistory = new ArrayList<>();
  private int _maxHistoryLength;
  private int _previousCursor;
  private static final Color DEFAULT_MISMATCH_BACKGROUND =
      ThemeSupport.getThemeColor(ThemeSupport.ThemeColor.YELLOW);
  private Color _mismatchBackground;
  private SearchableBarEx.Installer _installer;

  public SearchableBarEx(SearchableEx searchable) {
    this(searchable, "", false);
  }

  @SuppressWarnings("unused")
  public SearchableBarEx(SearchableEx searchable, boolean compact) {
    this(searchable, "", compact);
  }

  public SearchableBarEx(SearchableEx searchable, String initialText, boolean compact) {
    this._visibleButtons = -17;
    this._showMatchCount = false;
    this._maxHistoryLength = 18;
    this._previousCursor = -1;
    this.setFloatable(false);
    this.setRollover(true);
    this._searchable = searchable;
    setSearchingText(searchable.getSearchingText());
    this._searchable.addSearchableListener(
        e -> {
          if (e.getID() == 3005
              && SearchableBarEx.this._searchable.getSearchingText() != null
              && !SearchableBarEx.this._searchable.getSearchingText().isBlank()) {
            SearchableBarEx.this.highlightAllOrNext();
          }
        });
    this._searchable.setSearchableProvider(this);
    this._compact = compact;
    this.initComponents(initialText);
  }

  private void initComponents(String initialText) {
    final AbstractAction closeAction =
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            if (SearchableBarEx.this.getInstaller() != null) {
              SearchableBarEx.this.getInstaller().closeSearchBar(SearchableBarEx.this);
            }
          }
        };
    final AbstractAction findNextAction =
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            SearchableBarEx.this._highlightsToggle.setSelected(false);
            String text = SearchableBarEx.this.getSearchingText();
            SearchableBarEx.this.addSearchingTextToHistory(text);
            int cursor = SearchableBarEx.this._searchable.getSelectedIndex();
            SearchableBarEx.this._searchable.setCursor(cursor);
            int found = SearchableBarEx.this._searchable.findNext(text);
            if (found == cursor) {
              SearchableBarEx.this.select(found, text, false);
              SearchableBarEx.this.clearStatus();
            } else if (found != -1
                && SearchableBarEx.this._searchable.isRepeats()
                && found <= cursor) {
              SearchableBarEx.this.select(found, text, false);
              SearchableBarEx.this.setStatus(
                  SearchableBarEx.this.getResourceString("SearchableBarEx.reachedBottomRepeat"),
                  SearchableBarEx.this.getImageIcon("icons/repeat.png"));
            } else if (!SearchableBarEx.this._searchable.isRepeats() && found == -1) {
              SearchableBarEx.this.setStatus(
                  SearchableBarEx.this.getResourceString("SearchableBarEx.reachedBottom"),
                  SearchableBarEx.this.getImageIcon("icons/error.png"));
            } else if (found != -1) {
              SearchableBarEx.this.select(found, text, false);
              SearchableBarEx.this.clearStatus();
              if (SearchableBarEx.this._searchable.getSearchingDelay() < 0) {
                SearchableBarEx.this.highlightAllOrNext();
              }
            }
          }
        };
    final AbstractAction findPrevAction =
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            SearchableBarEx.this._highlightsToggle.setSelected(false);
            String text = SearchableBarEx.this.getSearchingText();
            SearchableBarEx.this.addSearchingTextToHistory(text);
            int cursor = SearchableBarEx.this._searchable.getSelectedIndex();
            SearchableBarEx.this._searchable.setCursor(cursor);
            int found = SearchableBarEx.this._searchable.findPrevious(text);
            if (found == cursor) {
              SearchableBarEx.this.select(found, text, false);
              SearchableBarEx.this.clearStatus();
            } else if (found != -1
                && SearchableBarEx.this._searchable.isRepeats()
                && found >= cursor) {
              SearchableBarEx.this.select(found, text, false);
              SearchableBarEx.this.setStatus(
                  SearchableBarEx.this.getResourceString("SearchableBarEx.reachedTopRepeat"),
                  SearchableBarEx.this.getImageIcon("icons/repeat.png"));
            } else if (!SearchableBarEx.this._searchable.isRepeats() && found == -1) {
              SearchableBarEx.this.setStatus(
                  SearchableBarEx.this.getResourceString("SearchableBarEx.reachedTop"),
                  SearchableBarEx.this.getImageIcon("icons/error.png"));
            } else if (found != -1) {
              SearchableBarEx.this.select(found, text, false);
              SearchableBarEx.this.clearStatus();
            }
          }
        };
    this._mouseMotionListener =
        new MouseMotionAdapter() {
          public void mouseMoved(MouseEvent e) {
            SearchableBarEx.this.hideMessage();
          }
        };
    this._keyListener =
        new KeyAdapter() {
          public void keyTyped(KeyEvent e) {
            SearchableBarEx.this.hideMessage();
          }
        };
    this._closeButton = this.createCloseButton(closeAction);
    this._findNextButton = this.createFindNextButton(findNextAction);
    this._findPrevButton = this.createFindPrevButton(findPrevAction);
    this._highlightsToggle = this.createHighlightButton();
    this._matchCaseToggle = this.createMatchCaseButton();
    this._wholeWordsToggle = this.createWholeWordsButton();
    this._repeatToggle = this.createRepeatsButton();
    this._regexToggle = this.createRegexButton();
    this._statusLabel = new JLabel();
    this._dropDown = this.createDropDown();
    this._searchTextField = this.createSearchTextField();
    getSearchTextField()
        .addFocusListener(
            new FocusAdapter() {
              public void focusGained(FocusEvent e) {
                SearchableBarEx.this._searchTextField.selectAll();
              }
            });

    getSearchTextField().setColumns(13);
    //        this._comboBox = this.createComboBox();
    DocumentListener listener =
        new DocumentListener() {
          private final Timer timer =
              new Timer(
                  SearchableBarEx.this._searchable.getSearchingDelay(),
                  e -> SearchableBarEx.this.highlightAllOrNext());

          public void insertUpdate(DocumentEvent e) {
            this.startTimer();
          }

          public void removeUpdate(DocumentEvent e) {
            this.startTimer();
          }

          public void changedUpdate(DocumentEvent e) {
            this.startTimer();
          }

          void startTimer() {
            if (SearchableBarEx.this._searchable.getSearchingDelay() > 0) {
              if (this.timer.isRunning()) {
                this.timer.restart();
              } else {
                this.timer.setRepeats(false);
                this.timer.start();
              }
            } else if (SearchableBarEx.this._searchable.getSearchingDelay() == 0) {
              SearchableBarEx.this.highlightAllOrNext();
            }
          }
        };
    getSearchTextField().getDocument().addDocumentListener(listener);
    getSearchTextField().setText(initialText);
    getSearchTextField().registerKeyboardAction(findNextAction, KeyStroke.getKeyStroke(40, 0), 0);
    getSearchTextField().registerKeyboardAction(findNextAction, KeyStroke.getKeyStroke(10, 0), 0);
    getSearchTextField().registerKeyboardAction(findPrevAction, KeyStroke.getKeyStroke(38, 0), 0);
    getSearchTextField().registerKeyboardAction(closeAction, KeyStroke.getKeyStroke(27, 0), 0);
    if (getSearchTextField() != null) {
      (getSearchTextField()).getDocument().addDocumentListener(listener);
      this.registerKeyboardActions(closeAction, findNextAction, findPrevAction);
    }

    //        this._comboBox.setSelectedItem(initialText);
    //        this._comboBox.setPreferredSize(this._searchTextField.getPreferredSize());
    this.installComponents();
    int found = this._searchable.findFromCursor(this.getSearchingText());
    if (!initialText.isBlank() && found == -1) {
      this.select(found, initialText, false);
    }
  }

  private void registerKeyboardActions(
      AbstractAction closeAction, AbstractAction findNextAction, AbstractAction findPrevAction) {
    (getSearchTextField()).registerKeyboardAction(findNextAction, KeyStroke.getKeyStroke(40, 0), 0);
    (getSearchTextField()).registerKeyboardAction(findNextAction, KeyStroke.getKeyStroke(10, 0), 0);
    (getSearchTextField()).registerKeyboardAction(findPrevAction, KeyStroke.getKeyStroke(38, 0), 0);
    (getSearchTextField()).registerKeyboardAction(closeAction, KeyStroke.getKeyStroke(27, 0), 0);
  }

  private void unregisterKeyboardActions() {
    (getSearchTextField()).unregisterKeyboardAction(KeyStroke.getKeyStroke(40, 0));
    (getSearchTextField()).unregisterKeyboardAction(KeyStroke.getKeyStroke(10, 0));
    (getSearchTextField()).unregisterKeyboardAction(KeyStroke.getKeyStroke(38, 0));
    (getSearchTextField()).unregisterKeyboardAction(KeyStroke.getKeyStroke(27, 0));
  }

  protected JTextField createSearchTextField() {
    FlatTextField searchField = _searchable.getSearchField();
    searchField.setLeadingComponent(_dropDown);
    return searchField;
  }

  protected JTextField getSearchTextField() {
    return this._searchTextField;
  }

  protected JideSplitButton createDropDown() {
    _dropDown = new JideSplitButton(new FlatSearchIcon());
    _dropDown.setEnabled(false);
    _dropDown.setButtonStyle(JideSplitButton.TOOLBOX_STYLE);
    return _dropDown;
  }

  public void addSearchHistoryItem(String entry) {
    for (int i = 0; i < getSearchHistory().size(); i++) {
      JMenuItem mi = _searchHistory.get(i);
      if (mi.getActionCommand().equals(entry)) {
        _searchHistory.remove(mi);
        if (i < _menuInsertPoint) {
          _searchHistory.addFirst(mi);
          mi.setFont(mi.getFont().deriveFont(Font.BOLD));
        } else {
          _searchHistory.add(_menuInsertPoint - 1, mi);
        }
        return;
      } else {
        mi.setFont(mi.getFont().deriveFont(Font.PLAIN));
      }
    }
    JMenuItem menuItem = new JMenuItem(entry);
    menuItem.setActionCommand(entry);
    menuItem.addActionListener(e -> getSearchTextField().setText(e.getActionCommand()));
    _dropDown.add(menuItem, _menuInsertPoint);
    _dropDown.setEnabled(true);
  }

  public void addPresetSearchValues(Set<LocalObject> items) {
    for (LocalObject item : items) {
      addPresetSearchValue(item);
    }
  }

  public void addPresetSearchValue(LocalObject item) {
    JMenuItem menuItem = new JMenuItem(I18N.getText(item.getI18nKey()));
    menuItem.setActionCommand(item.getValue().toString());
    menuItem.addActionListener(e -> getSearchTextField().setText(e.getActionCommand()));
    _dropDown.add(menuItem, 0);
    if (_menuInsertPoint == 0) {
      _dropDown.add(new FlatPopupMenuSeparator());
      _menuInsertPoint++;
    }
    _menuInsertPoint++;
    _dropDown.setEnabled(true);
  }

  public SearchableEx getSearchable() {
    return this._searchable;
  }

  protected AbstractButton createCloseButton(AbstractAction closeAction) {
    FlatButton button = new FlatButton();
    button.setIcon(new FlatInternalFrameCloseIcon());
    button.addActionListener(closeAction);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    return button;
  }

  protected AbstractButton createFindNextButton(AbstractAction findNextAction) {
    FlatButton button = new FlatButton();
    button.setText(isCompact() ? "" : this.getResourceString("Button.findNext"));
    button.setIcon(ICON_NEXT);
    button.setToolTipText(this.getResourceString("SearchableBarEx.findNext.tooltip"));
    button.setMnemonic(this.getResourceString("Button.findNext.mnemonic").charAt(0));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    button.addActionListener(findNextAction);
    button.setEnabled(false);
    return button;
  }

  protected AbstractButton createFindPrevButton(AbstractAction findPrevAction) {
    FlatButton button = new FlatButton();
    button.setText(isCompact() ? "" : this.getResourceString("Button.findPrevious"));
    button.setIcon(ICON_PREV);
    button.setToolTipText(this.getResourceString("Button.findPrevious"));
    button.setMnemonic(this.getResourceString("Button.findPrevious.mnemonic").charAt(0));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    button.addActionListener(findPrevAction);
    button.setEnabled(false);
    return button;
  }

  protected AbstractButton createHighlightButton() {
    FlatToggleButton button = new FlatToggleButton();
    button.setText(isCompact() ? "" : this.getResourceString("Button.showAll"));
    button.setIcon(ICON_HIGHLIGHT);
    button.setButtonType(FlatButton.ButtonType.tab);
    button.setToolTipText(this.getResourceString("Button.showAll"));
    button.setMnemonic(this.getResourceString("Button.showAll.mnemonic").charAt(0));
    //        button.setSelectedIcon(this.getImageIcon("icons/highlightsS.png"));
    //        button.setDisabledIcon(this.getImageIcon("icons/highlightsD.png"));
    //        button.setRolloverIcon(this.getImageIcon("icons/highlightsR.png"));
    //        button.setRolloverSelectedIcon(this.getImageIcon("icons/highlightsRS.png"));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    AbstractAction highlightAllAction =
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            SearchableBarEx.this.addSearchingTextToHistory(SearchableBarEx.this.getSearchingText());
            SearchableBarEx.this.highlightAllOrNext();
          }
        };
    button.addActionListener(highlightAllAction);
    button.setEnabled(false);
    return button;
  }

  protected AbstractButton createRepeatsButton() {
    FlatToggleButton button = new FlatToggleButton();
    button.setButtonType(FlatButton.ButtonType.tab);
    button.setText(isCompact() ? "" : this.getResourceString("Button.showAll"));
    button.setIcon(ICON_RECURSIVE);
    button.setMnemonic(this.getResourceString("Button.showAll.mnemonic").charAt(0));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    button.setSelected(this.getSearchable().isRepeats());
    button.addItemListener(
        e -> {
          if (e.getSource() instanceof AbstractButton) {
            SearchableBarEx.this
                .getSearchable()
                .setRepeats(((AbstractButton) e.getSource()).isSelected());
          }
        });
    button.setOpaque(false);
    return button;
  }

  protected AbstractButton createRegexButton() {
    FlatToggleButton button = new FlatToggleButton();
    button.setButtonType(FlatButton.ButtonType.tab);
    button.setText(isCompact() ? "" : this.getResourceString("Button.regex"));
    button.setIcon(ICON_REGEX);
    button.setMnemonic(this.getResourceString("Button.regex.mnemonic").charAt(0));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    button.setSelected(this.getSearchable().isRepeats());
    button.addItemListener(
        e -> {
          if (e.getSource() instanceof AbstractButton) {
            SearchableBarEx.this
                .getSearchable()
                .setWildcardEnabled(((AbstractButton) e.getSource()).isSelected());
          }
        });
    button.setOpaque(false);
    return button;
  }

  protected AbstractButton createMatchCaseButton() {
    FlatToggleButton button = new FlatToggleButton();
    button.setButtonType(FlatButton.ButtonType.tab);
    button.setText(isCompact() ? "" : this.getResourceString("Button.matchCase"));
    button.setIcon(ICON_CASE);
    button.setMnemonic(this.getResourceString("Button.matchCase.mnemonic").charAt(0));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    button.setSelected(this.getSearchable().isCaseSensitive());
    button.addItemListener(
        e -> {
          if (e.getSource() instanceof AbstractButton) {
            SearchableBarEx.this
                .getSearchable()
                .setCaseSensitive(((AbstractButton) e.getSource()).isSelected());
            SearchableBarEx.this.addSearchingTextToHistory(SearchableBarEx.this.getSearchingText());
            SearchableBarEx.this.highlightAllOrNext();
          }
        });
    button.setOpaque(false);
    return button;
  }

  protected AbstractButton createWholeWordsButton() {
    FlatToggleButton button = new FlatToggleButton();
    button.setButtonType(FlatButton.ButtonType.tab);
    button.setText(isCompact() ? "" : this.getResourceString("Button.wholeWords"));
    button.setIcon(ICON_WORDS);
    button.setMnemonic(this.getResourceString("Button.wholeWords.mnemonic").charAt(0));
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);
    if (this.getSearchable() instanceof WholeWordsSupport) {
      button.setSelected(((WholeWordsSupport) this.getSearchable()).isWholeWords());
    } else {
      button.setSelected(false);
      button.setEnabled(false);
    }

    button.addItemListener(
        e -> {
          if (e.getSource() instanceof AbstractButton
              && SearchableBarEx.this.getSearchable() instanceof WholeWordsSupport) {
            ((WholeWordsSupport) SearchableBarEx.this.getSearchable())
                .setWholeWords(((AbstractButton) e.getSource()).isSelected());
            SearchableBarEx.this.addSearchingTextToHistory(SearchableBarEx.this.getSearchingText());
            SearchableBarEx.this.highlightAllOrNext();
          }
        });
    button.setOpaque(false);
    return button;
  }

  protected void installComponents() {
    this.setBorder(BorderFactory.createEtchedBorder());
    this.setLayout(new JideBoxLayout(this, 0));
    this.add(Box.createHorizontalStrut(4), "fix");
    if ((this._visibleButtons & SHOW_CLOSE) != 0) {
      this.add(this._closeButton);
      this.add(Box.createHorizontalStrut(10));
    }

    this._leadingLabel = new JLabel(this.getResourceString("Button.find"));
    this._leadingLabel.setDisplayedMnemonic(
        this.getResourceString("Button.find.mnemonic").charAt(0));
    this.add(this._leadingLabel);
    this.add(Box.createHorizontalStrut(2), "fix");
    this.add(JideSwingUtilities.createCenterPanel(getSearchTextField()), "fix");
    this._leadingLabel.setLabelFor(getSearchTextField());

    this.add(Box.createHorizontalStrut(2), "fix");
    if ((this._visibleButtons & SHOW_NAVIGATION) != 0) {
      this.add(this._findNextButton);
      this.add(this._findPrevButton);
    }
    if ((this._visibleButtons & SHOW_HIGHLIGHTS) != 0) {
      this.add(this._highlightsToggle);
      this.add(Box.createHorizontalStrut(2));
    }
    if ((this._visibleButtons & SHOW_REPEATS) != 0) {
      this.add(this._repeatToggle);
    }

    if ((this._visibleButtons & SHOW_MATCHCASE) != 0) {
      this.add(this._matchCaseToggle);
      this.add(Box.createHorizontalStrut(2));
    }

    if ((this._visibleButtons & SHOW_WHOLE_WORDS) != 0
        && this.getSearchable() instanceof WholeWordsSupport) {
      this.add(this._wholeWordsToggle);
      this.add(Box.createHorizontalStrut(2));
    }

    if ((this._visibleButtons & SHOW_REGEX) != 0) {
      this.add(this._regexToggle);
      this.add(Box.createHorizontalStrut(2));
    }

    if ((this._visibleButtons & SHOW_STATUS) != 0) {
      this.add(Box.createHorizontalStrut(24));
      this.add(this._statusLabel, "vary");
    }

    this.add(Box.createHorizontalStrut(6), "fix");
  }

  public boolean isHighlightAll() {
    return this._highlightsToggle.isSelected();
  }

  public void setHighlightAll(boolean highlightAll) {
    this._highlightsToggle.setSelected(highlightAll);
  }

  private void highlightAllOrNext() {
    if (this._highlightsToggle.isSelected()) {
      this._previousCursor = this._searchable.getCurrentIndex();
      this.highlightNext();
      this.highlightAll();
    } else {
      if (this._previousCursor >= 0) {
        this._searchable.setCursor(this._previousCursor);
        this._searchable.setSelectedIndex(this._previousCursor, false);
      }

      this.highlightNext();
    }
  }

  private void highlightAll() {
    String text = this.getSearchingText();
    if (text != null && !text.isBlank()) {
      boolean old = this._searchable.isRepeats();
      this._searchable.setRepeats(false);
      int index = this._searchable.findFirst(text);
      if (index != -1) {
        this._searchable.setSelectedIndex(index, false);
        this._searchable.setCursor(index);
        this._findNextButton.setEnabled(true);
        this._findPrevButton.setEnabled(true);
        this._highlightsToggle.setEnabled(true);
        this.clearStatus();
      } else {
        this.select(-1, text, false);
        this._findNextButton.setEnabled(false);
        this._findPrevButton.setEnabled(false);
        this._highlightsToggle.setEnabled(false);
        this.setStatus(
            this.getResourceString("SearchableBarEx.notFound"),
            this.getImageIcon("icons/error.png"));
      }

      this._searchable.highlightAll();
      this._searchable.setRepeats(old);
      this._searchable.setCursor(0);
    } else {
      this._findNextButton.setEnabled(false);
      this._findPrevButton.setEnabled(false);
      this._highlightsToggle.setEnabled(false);
      this.select(-1, "", false);
      this.clearStatus();
    }
  }

  private void highlightNext() {
    this._searchable.cancelHighlightAll();
    String text = this.getSearchingText();
    if (text != null && !text.isBlank()) {
      int found = this._searchable.findFromCursor(text);
      if (found == -1) {
        this.select(-1, "", false);
        this._findNextButton.setEnabled(false);
        this._findPrevButton.setEnabled(false);
        this._highlightsToggle.setEnabled(false);
        this.setStatus(
            this.getResourceString("SearchableBarEx.notFound"),
            this.getImageIcon("icons/error.png"));
      } else {
        this.select(found, text, false);
        this._findNextButton.setEnabled(true);
        this._findPrevButton.setEnabled(true);
        this._highlightsToggle.setEnabled(true);
        this.clearStatus();
      }

    } else {
      this._findNextButton.setEnabled(false);
      this._findPrevButton.setEnabled(false);
      this._highlightsToggle.setEnabled(false);
      this.select(-1, "", false);
      this.clearStatus();
    }
  }

  private void clearStatus() {
    this._statusLabel.setIcon(null);
    getSearchTextField().setBackground(UIDefaultsLookup.getColor("TextField.background"));
    getSearchTextField().setBackground(UIDefaultsLookup.getColor("TextField.background"));
    if (!this.isShowMatchCount()
        || getSearchTextField().getText().length() <= 0
            && (getSearchTextField()).getText().length() <= 0) {
      this._statusLabel.setText("");
    } else {
      this._statusLabel.setText(
          this.getSearchable().getMatchCount()
              + " "
              + this.getResourceString("SearchableBarEx.matches"));
    }
    this.hideMessage();
  }

  private void setStatus(String message, Icon icon) {
    this._statusLabel.setIcon(icon);
    //        this._statusLabel.setText(message);
    //        this._statusLabel.setToolTipText(message);
    //        if (!this._statusLabel.isShowing() || this._statusLabel.getWidth() < 25) {
    this.showMessage(message);
    //        }

  }

  public void focusSearchField() {
    if (getSearchTextField() != null && getSearchTextField().isVisible()) {
      getSearchTextField().requestFocus();
    }
  }

  protected void select(int index, String searchingText, boolean incremental) {
    if (index != -1) {
      this._searchable.setSelectedIndex(index, incremental);
      this._searchable.setCursor(index, incremental);
      getSearchTextField().setBackground(UIDefaultsLookup.getColor("TextField.background"));
      getSearchTextField().setBackground(UIDefaultsLookup.getColor("TextField.background"));
    } else {
      this._searchable.setSelectedIndex(-1, false);
      getSearchTextField().setBackground(this.getMismatchBackground());
      getSearchTextField().setBackground(UIDefaultsLookup.getColor("TextField.background"));
    }

    this._searchable.firePropertyChangeEvent(searchingText);
    if (index != -1) {
      Object element = this._searchable.getElementAt(index);
      this._searchable.fireSearchableEvent(
          new SearchableEvent(
              this._searchable,
              3002,
              searchingText,
              element,
              this._searchable.convertElementToString(element)));
    } else {
      this._searchable.fireSearchableEvent(
          new SearchableEvent(this._searchable, 3003, searchingText));
    }
  }

  public String getSearchingText() {
    if (getSearchTextField() != null && getSearchTextField().isVisible()) {
      return getSearchTextField().getText();
    } else {
      return "";
    }
  }

  @SuppressWarnings("unused")
  public void setSearchingText(String searchingText) {
    if (getSearchTextField() != null && getSearchTextField().isVisible()) {
      getSearchTextField().setText(searchingText);
    }
  }

  public boolean isPassive() {
    return false;
  }

  public void setMismatchForeground(Color mismatchBackground) {
    this._mismatchBackground = mismatchBackground;
  }

  public Color getMismatchBackground() {
    return this._mismatchBackground == null
        ? DEFAULT_MISMATCH_BACKGROUND
        : this._mismatchBackground;
  }

  public List<JMenuItem> getSearchHistory() {
    return this._searchHistory == null ? new ArrayList<>() : this._searchHistory;
  }

  public void setSearchHistory(String[] searchHistory) {
    if (searchHistory != null && searchHistory.length != 0) {
      this._searchHistory.clear();
      for (int i = Math.min(_maxHistoryLength, searchHistory.length) - 1; i >= 0; i--) {
        addSearchHistoryItem(searchHistory[i]);
        if (i == 0) {
          getSearchTextField().setText(searchHistory[i]);
        }
      }
    }
  }

  public int getMaxHistoryLength() {
    return this._maxHistoryLength;
  }

  public void setMaxHistoryLength(int maxHistoryLength) {
    if (this._maxHistoryLength != maxHistoryLength) {
      int old = this._maxHistoryLength;
      this._maxHistoryLength = maxHistoryLength;
      if (this.getMaxHistoryLength() == 0) {
        this._leadingLabel.setLabelFor(getSearchTextField());
        _dropDown.setEnabled(false);
      } else {
        _dropDown.setEnabled(true);
      }
      this.firePropertyChange("maxHistoryLength", old, this._maxHistoryLength);
    }
  }

  public boolean isShowMatchCount() {
    return this._showMatchCount;
  }

  public void setShowMatchCount(boolean showMatchCount) {
    this._showMatchCount = showMatchCount;
    if (this.getSearchable() != null) {
      this.getSearchable().setCountMatch(this.isShowMatchCount());
    }
  }

  public SearchableBarEx.Installer getInstaller() {
    return this._installer;
  }

  public void setInstaller(SearchableBarEx.Installer installer) {
    this._installer = installer;
  }

  public static SearchableBarEx install(
      SearchableEx searchable, KeyStroke keyStroke, SearchableBarEx.Installer installer) {
    final SearchableBarEx searchableBar = new SearchableBarEx(searchable);
    searchableBar.setInstaller(installer);
    ((JComponent) searchable.getComponent())
        .registerKeyboardAction(
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                searchableBar.getInstaller().openSearchBar(searchableBar);
                searchableBar.focusSearchField();
              }
            },
            keyStroke,
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    return searchableBar;
  }

  public void processKeyEvent(KeyEvent e) {}

  public int getVisibleButtons() {
    return this._visibleButtons;
  }

  public void setVisibleButtons(int visibleButtons) {
    this._visibleButtons = visibleButtons;
    this.removeAll();
    this.installComponents();
    this.revalidate();
    this.repaint();
  }

  public boolean isCompact() {
    return this._compact;
  }

  public void setCompact(boolean compact) {
    this._compact = compact;
    this._findNextButton.setText(
        this._compact ? "" : this.getResourceString("SearchableBarEx.findNext"));
    this._highlightsToggle.setText(
        this._compact ? "" : this.getResourceString("SearchableBarEx.highlights"));
    this._findPrevButton.setText(
        this._compact ? "" : this.getResourceString("SearchableBarEx.findPrevious"));
  }

  protected ImageIcon getImageIcon(String name) {
    return SearchableBarIconsFactory.getImageIcon(name);
  }

  protected String getResourceString(String key) {
    return I18N.getText(key);
  }

  private void showMessage(String message) {
    this.hideMessage();
    this._messagePopup = JidePopupFactory.getSharedInstance().createPopup();
    JLabel label = new JLabel(message);
    label.setOpaque(true);
    label.setFont(UIDefaultsLookup.getFont("Label.font").deriveFont(Font.BOLD, 11.0F));
    label.setBackground(new Color(253, 254, 226));
    label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
    label.setForeground(UIDefaultsLookup.getColor("ToolTip.foreground"));
    this._messagePopup.getContentPane().setLayout(new BorderLayout());
    this._messagePopup.getContentPane().add(label);
    if (getSearchTextField() != null && getSearchTextField().isVisible()) {
      this._messagePopup.setOwner(getSearchTextField());
    }

    this._messagePopup.setDefaultMoveOperation(0);
    this._messagePopup.setTransient(true);
    this._messagePopup.showPopup();
    this.addMouseMotionListener(this._mouseMotionListener);
    if (getSearchTextField() != null && getSearchTextField().isVisible()) {
      getSearchTextField().addKeyListener(this._keyListener);
    }
  }

  private void hideMessage() {
    if (this._messagePopup != null) {
      this._messagePopup.hidePopupImmediately();
      this._messagePopup = null;
    }

    if (this._mouseMotionListener != null) {
      this.removeMouseMotionListener(this._mouseMotionListener);
    }

    if (this._keyListener != null) {
      getSearchTextField().removeKeyListener(this._keyListener);
    }
  }

  private void addSearchingTextToHistory(String searchingText) {
    if (searchingText != null && !searchingText.isBlank()) {
      addSearchHistoryItem(searchingText);
    }
  }

  public interface Installer {
    void openSearchBar(SearchableBarEx bar);

    void closeSearchBar(SearchableBarEx bar);
  }
}
