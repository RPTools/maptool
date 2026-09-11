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
package net.rptools.maptool.client.swing;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.jidesoft.swing.PartialLineBorder;
import com.jidesoft.swing.PartialSide;
import java.awt.*;
import java.awt.event.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.accessibility.*;
import javax.swing.*;
import net.rptools.maptool.client.ui.macrobuttons.dialog.MacroEditorDialog;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;

/**
 *
 *
 * <h1>A component containing a JTextField with a pop-up text area.</h1>
 *
 * <h2>Features</h2>
 *
 * <ul>
 *   <li>A button within the text field to open the text in a larger (modal) {@link
 *       MacroEditorDialog}
 *   <li>[Optional] A label placed before the text field
 *   <li>[Optional] A button to collapse/hide the text field to save space, e.g. for use in a
 *       toolbar
 *   <li>Method to open the text field in a {@link GenericDialog}
 *   <li>Method to get the text field as a {@link DefaultCellEditor}
 * </ul>
 *
 * <p>The {@link #textField} is used as a delegate for values and should be used as the access point
 * for most things, e.g.
 *
 * <pre>
 *     TextFieldEditorWithPopup tf = new TextFieldEditorWithPopup();
 *     tf.getTextField().addActionListener(myListener);
 * </pre>
 */
@SuppressWarnings("unused")
public class TextFieldEditorWithPopup extends Box implements Accessible {
  private static final Icon EDIT_ICON = RessourceManager.getSmallIcon(Icons.ACTION_EDIT_IN_EDITOR);
  private static final FlatSVGIcon RIGHT_TRIANGLE =
      ((FlatSVGIcon) RessourceManager.getSmallIcon(Icons.TRIANGLE_RIGHT)).derive(0.4f);
  private static final FlatSVGIcon LEFT_TRIANGLE =
      ((FlatSVGIcon) RessourceManager.getSmallIcon(Icons.TRIANGLE_LEFT)).derive(0.4f);

  private static final String EDITOR_ACCESSIBLE_NAME_KEY = "textEditorWithPopup.accessibleName";
  private static final String EDITOR_ACCESSIBLE_DESCRIPTION_KEY =
      "textEditorWithPopup.accessibleDescription";
  private static final String EDITOR_BUTTON_ACCESSIBLE_NAME_KEY =
      "textEditorWithPopup.openEditorButton.accessibleName";
  private static final String EDITOR_BUTTON_ACCESSIBLE_DESCRIPTION_KEY =
      "textEditorWithPopup.openEditorButton.accessibleDescription";
  private static final String COLLAPSE_BUTTON_ACCESSIBLE_NAME_KEY =
      "textEditorWithPopup.collapseToggleButton.accessibleName";
  private static final String COLLAPSE_BUTTON_ACCESSIBLE_DESCRIPTION_KEY =
      "textEditorWithPopup.collapseToggleButton.accessibleDescription";
  private static final Color OPEN_EDITOR_BUTTON_BG =
      UIManager.getDefaults().getColor("ToolBar.background");
  private static final Color BUTTON_BORDER_COLOUR =
      UIManager.getDefaults().getColor("Button.borderColor");
  private static final Color BUTTON_HIGHLIGHT =
      UIManager.getDefaults().getColor(FlatIconColors.ACTIONS_YELLOW.key);

  /** Text field displayed for editing. Contains {@link #openEditorButton} at the tail end. */
  private final FlatTextField textField = new FlatTextField();

  /**
   * Label to show if required. Appears before the {@link #textField} and {@link #collapseButton}
   */
  private final JLabel label = new JLabel();

  /** Button inside the {@link #textField} that opens the external editor. */
  private final JButton openEditorButton = new JButton(EDIT_ICON);

  /**
   * Toggle button for collapsing/hiding the text editor. Sits between the {@link #label} and {@link
   * #textField}. Selected == collapsed
   */
  private final JToggleButton collapseButton = new JToggleButton();

  /** Collapsed state; true = text field hidden, false = text area visible */
  private final BooleanProperty collapsible = new SimpleBooleanProperty(false);

  /** Collapsed state; true = text field hidden, false = text area visible */
  private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

  /** Label visibility */
  private final BooleanProperty showLabel = new SimpleBooleanProperty(false);

  /** Used as the title in the external editor pop-up */
  private String externalEditorTitle = I18N.getMessage("Label.editor");

  private ActionListener openEditorActionListener =
      l ->
          MacroEditorDialog.createModalDialog(
                  c -> {
                    if (c != null) {
                      setValue(c);
                    }
                  })
              .show(getExternalEditorTitle(), getValue());

  {
    initLabel();
    initPropertyChangeListeners();
    initTextField();
    initOpenEditorButton();
    initAccessibility();
    initCollapseButton();
  }

  /** Creates a new <code>TextFieldEditorWithPopup</code>. */
  public TextFieldEditorWithPopup(String text) {
    this();
    setValue(text);
  }

  /** Creates a new <code>TextFieldEditorWithPopup</code>. */
  public TextFieldEditorWithPopup() {
    super(BoxLayout.LINE_AXIS);
    super.getAccessibleContext().setAccessibleName(I18N.getMessage(EDITOR_ACCESSIBLE_NAME_KEY));
    super.getAccessibleContext()
        .setAccessibleDescription(I18N.getMessage(EDITOR_ACCESSIBLE_DESCRIPTION_KEY));
    setFocusCycleRoot(false);
    setFocusable(false);
    setRequestFocusEnabled(false);
    add(label);
    add(collapseButton);
    add(textField);
    invalidate();
    setVisible(true);
  }

  /** set up the collapse button */
  private void initCollapseButton() {
    collapseButton.setFocusable(true);
    collapseButton.setFocusTraversalKeysEnabled(true);
    collapseButton.setRequestFocusEnabled(true);
    collapseButton.addPropertyChangeListener(evt -> System.out.println(evt.getPropertyName()));
    // appearance - no background or border unless focussed
    collapseButton.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {
            collapseButton.setContentAreaFilled(true);
          }

          @Override
          public void focusLost(FocusEvent e) {
            collapseButton.setContentAreaFilled(false);
          }
        });
    collapseButton.setIcon(LEFT_TRIANGLE); // collapse
    collapseButton.setSelectedIcon(RIGHT_TRIANGLE); // expand
    collapseButton.putClientProperty("JButton.buttonType", FlatButton.ButtonType.borderless);
    collapseButton.setBackground(BUTTON_HIGHLIGHT);
    collapseButton.setContentAreaFilled(false);
    collapseButton.setSelected(true);
    collapseButton.setVisible(false);
    // update property with state
    collapseButton.addActionListener(e -> setCollapsed(collapseButton.isSelected()));
  }

  /** set accessibility bits */
  private void initAccessibility() {
    openEditorButton
        .getAccessibleContext()
        .setAccessibleName(I18N.getMessage(EDITOR_BUTTON_ACCESSIBLE_NAME_KEY));
    openEditorButton
        .getAccessibleContext()
        .setAccessibleDescription(I18N.getMessage(EDITOR_BUTTON_ACCESSIBLE_DESCRIPTION_KEY));
    openEditorButton.setToolTipText(I18N.getMessage(EDITOR_BUTTON_ACCESSIBLE_DESCRIPTION_KEY));
    collapseButton
        .getAccessibleContext()
        .setAccessibleName(I18N.getMessage(COLLAPSE_BUTTON_ACCESSIBLE_NAME_KEY));
    collapseButton
        .getAccessibleContext()
        .setAccessibleDescription(I18N.getMessage(COLLAPSE_BUTTON_ACCESSIBLE_DESCRIPTION_KEY));
    collapseButton.setToolTipText(I18N.getMessage(COLLAPSE_BUTTON_ACCESSIBLE_DESCRIPTION_KEY));
  }

  /** add listeners for values that influence layout */
  private void initPropertyChangeListeners() {
    collapsible.addListener((observable, oldValue, newValue) -> revalidate());
    collapsed.addListener((observable, oldValue, newValue) -> revalidate());
    showLabel.addListener((observable, oldValue, newValue) -> revalidate());
  }

  private void initLabel() {
    label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 3));
    label.setVisible(false);
    label.setFocusable(false);
    label.setLabelFor(this);
  }

  /** adds openEditor button and listeners to keep enabled state aligned */
  private void initTextField() {
    textField.setFocusable(true);
    textField.setFocusTraversalKeysEnabled(true);
    textField.setRequestFocusEnabled(true);
    // Put the external editor button inside the text field
    textField.setTrailingComponent(openEditorButton);
    // keep external editor button enabled state the same as the text field
    textField.addPropertyChangeListener(
        "editable", evt -> openEditorButton.setEnabled((Boolean) evt.getNewValue()));
    textField.addPropertyChangeListener(
        "enabled", evt -> openEditorButton.setEnabled((Boolean) evt.getNewValue()));
  }

  /**
   * set up the openEditor button appearance and action. FlatTextField does a bad job of
   * differentiating the trailing button or making its focus obvious.
   */
  private void initOpenEditorButton() {
    openEditorButton.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {
            openEditorButton.setBackground(BUTTON_HIGHLIGHT);
          }

          @Override
          public void focusLost(FocusEvent e) {
            openEditorButton.setBackground(OPEN_EDITOR_BUTTON_BG);
          }
        });
    openEditorButton.setFocusable(true);
    openEditorButton.setFocusTraversalKeysEnabled(true);
    openEditorButton.setRequestFocusEnabled(true);
    openEditorButton.setOpaque(true);
    openEditorButton.setBorder(new PartialLineBorder(BUTTON_BORDER_COLOUR, 1, PartialSide.WEST));
    // tends to inherit the caret cursor so we reset it
    openEditorButton.setCursor(Cursor.getDefaultCursor());
    // assign the action to open the external editor
    openEditorButton.addActionListener(openEditorActionListener);
  }

  /**
   * @return text value from the {@link #textField}
   */
  public String getValue() {
    return textField.getText();
  }

  /**
   * @param text value for the {@link #textField}
   */
  public void setValue(String text) {
    textField.setText(text);
  }

  /**
   * Enable/Disable the collapsible functionality
   *
   * @param value enabled
   */
  public void setCollapsible(boolean value) {
    collapsible.set(value);
  }

  /**
   * Sets the collapsed state
   *
   * @param value collapsed
   * @see #collapsed
   */
  public void setCollapsed(boolean value) {
    collapsed.set(value);
    if (collapseButton.isSelected() != value) {
      collapseButton.setSelected(value);
    }
    revalidate();
  }

  /**
   * @return Value for {@link #externalEditorTitle}
   */
  public String getExternalEditorTitle() {
    return externalEditorTitle;
  }

  /**
   * Value for {@link #externalEditorTitle}
   *
   * @param title to use
   */
  public void setExternalEditorTitle(String title) {
    externalEditorTitle = title;
  }

  /**
   * Sets the text on the {@link #openEditorButton} using the i18n lookup key
   *
   * @param text for label
   */
  public void setOpenEditorButtonText(String text) {
    openEditorButton.setText(text);
    invalidate();
  }

  /**
   * Sets the text on the {@link #openEditorButton}
   *
   * @param i18nKey to lookup
   */
  public void setOpenEditorButtonTextKey(String i18nKey) {
    setOpenEditorButtonText(I18N.getMessage(i18nKey));
  }

  /**
   * Set the tooltip on the {@link #openEditorButton} using the i18n lookup key
   *
   * @param i18nKey to lookup
   * @see #openEditorButton
   */
  public void setOpenEditorButtonTooltipTextKey(String i18nKey) {
    openEditorButton.setToolTipText(I18N.getMessage(i18nKey));
  }

  /**
   * Set the tooltip on the {@link #openEditorButton}
   *
   * @param text to display as tooltip
   */
  public void setOpenEditorButtonTooltipText(String text) {
    openEditorButton.setToolTipText(text);
  }

  /**
   * Change the icon on the {@link #openEditorButton}. Removes any text in the button, so do it
   * before {@link #setOpenEditorButtonText}/{@link #setOpenEditorButtonTextKey}
   *
   * @param icon to use
   */
  public void setOpenEditorButtonIcon(Icon icon) {
    openEditorButton.setIcon(icon);
    if (!openEditorButton.getText().isBlank()) {
      openEditorButton.setText(null);
    }
    revalidate();
  }

  /**
   * Replace the listener that opens the external editor.
   *
   * @param openEditorActionListener the replacement
   */
  public void setOpenEditorActionListener(ActionListener openEditorActionListener) {
    openEditorButton.removeActionListener(this.openEditorActionListener);
    this.openEditorActionListener = openEditorActionListener;
    openEditorButton.addActionListener(this.openEditorActionListener);
  }

  /**
   * @return The {@link #label} component
   */
  public JLabel getLabel() {
    return label;
  }

  /**
   * @return The {@link #collapseButton}
   */
  public JToggleButton getCollapseButton() {
    return collapseButton;
  }

  /**
   * @return The {@link #openEditorButton}
   */
  public JButton getOpenEditorButton() {
    return openEditorButton;
  }

  /**
   * @return The {@link #textField}
   */
  public JTextField getTextField() {
    return textField;
  }

  /**
   * Sets the icon on the label preceding the text field
   *
   * @param icon to use
   */
  public void setLabelIcon(Icon icon) {
    label.setIcon(icon);
    setShowLabel(true);
    invalidate();
  }

  /**
   * @return visibility of {@link #label} component
   */
  public boolean isShowLabel() {
    return showLabel.get();
  }

  /**
   * Sets the label as visible. This is set to true automatically when the label is assigned text or
   * an icon.
   *
   * @param value visible state of the label
   */
  public void setShowLabel(boolean value) {
    showLabel.set(value);
    revalidate();
  }

  /**
   * Sets the text on the label preceding the text field
   *
   * @param text for label
   */
  public void setLabelText(String text) {
    label.setText(text);
    setShowLabel(true);
    super.getAccessibleContext().setAccessibleName(text);
    invalidate();
  }

  /**
   * Sets the text on the label preceding the text field using the i18n lookup key.
   *
   * @param i18nKey to lookup
   */
  public void setLabelTextKey(String i18nKey) {
    setLabelText(I18N.getMessage(i18nKey));
  }

  /**
   * Sets the tooltip text on the label preceding the text field.
   *
   * @param text for tooltip
   */
  public void setLabelTooltipText(String text) {
    label.setToolTipText(text);
    super.getAccessibleContext().setAccessibleDescription(text);
  }

  /**
   * Sets the tooltip text on the label preceding the text field using the i18n lookup key.
   *
   * @param i18nKey to lookup
   */
  public void setLabelTooltipTextKey(String i18nKey) {
    setLabelTooltipText(I18N.getMessage(i18nKey));
  }

  /**
   * Applies enabled state to JTextField
   *
   * @param enabled true if this component should be enabled, false otherwise
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    textField.setEnabled(enabled);
  }

  /**
   * Opens the component in a {@link GenericDialog}
   *
   * @param title shown in the JDialog
   */
  public void openInDialog(String title) {
    new GenericDialogFactory()
        .setDialogTitle(title)
        .setCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
        .addButton(ButtonKind.CLOSE)
        .setContent(this)
        .display();
  }

  /**
   * For use in tables, trees, etc.
   *
   * @return a new {@link DefaultCellEditor} with the text field as the editor component
   */
  public DefaultCellEditor getAsCellEditor() {
    return new DefaultCellEditor(textField);
  }

  /** sets various component visibilities before calling super method */
  @Override
  public void revalidate() {
    label.setVisible(showLabel.get());
    collapseButton.setVisible(collapsible.get());
    textField.setVisible(!collapsed.get());
    super.revalidate();
  }
}
