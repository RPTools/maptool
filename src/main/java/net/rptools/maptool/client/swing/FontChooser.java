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

import com.formdev.flatlaf.extras.components.*;
import com.formdev.flatlaf.util.UIScale;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.theme.ThemeFontTools;
import net.rptools.maptool.language.I18N;

public class FontChooser extends JComponent {
  private static final String uiClassID = "RootPaneUI";

  public FontChooser(Option... args) {
    super();
    addOptions(Objects.requireNonNullElse(args, DEFAULT_OPTIONS));
  }

  public enum Option {
    BOLD,
    DELETE,
    ITALIC,
    PREFIX,
    RELATIVE_SIZE,
    FONT_LOGICAL,
    FONT_NAME,
    FONT_FAMILY
  }

  public static final Option[] DEFAULT_OPTIONS =
      new Option[] {Option.BOLD, Option.ITALIC, Option.RELATIVE_SIZE, Option.FONT_LOGICAL};

  private boolean deleted = false;
  private String prefix;
  private String fontName;

  private Font fontIn = Font.decode("");
  private final SimpleObjectProperty<Font> fontOut = new SimpleObjectProperty<>(fontIn);

  private final SimpleBooleanProperty bold = new SimpleBooleanProperty(false);
  private final SimpleBooleanProperty italic = new SimpleBooleanProperty(false);
  private final SimpleIntegerProperty referenceSize =
      new SimpleIntegerProperty(ThemeFontTools.OS_DEFAULT_FONT_SIZE);
  private final SimpleIntegerProperty relativeSize =
      new SimpleIntegerProperty(fontIn.getSize() - referenceSize.get());

  private final LayoutManager layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
  private final FlatButton deleteButton = new FlatButton();
  private final FlatSpinner sizeSpinner = new FlatSpinner();
  private final FlatToggleButton italicToggle = new FlatToggleButton();
  private final FlatToggleButton boldToggle = new FlatToggleButton();
  private final FlatComboBox<Font> fontCombo = new FlatComboBox<>();
  private FlatLabel exemplar;
  private SpinnerNumberModel spinnerNumberModel;

  private List<Font> fontList = new ArrayList<>();
  private final List<Option> optionList = new ArrayList<>();

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser useFont(Font font) {
    fontIn = font;
    fontOut.set(font);
    setBold(font.isBold());
    setItalic(font.isItalic());
    setRelativeSize(font.getSize() - getReferenceSize());
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser addOption(Option option) {
    optionList.add(option);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser addOptions(Option... args) {
    for (Option option : args) {
      addOption(option);
    }
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser removeOption(Option option) {
    optionList.remove(option);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser create() {
    setLayout(layout);
    setupComponents();

    if (optionList.contains(Option.DELETE)) {
      add(deleteButton);
      deleteButton.addActionListener(
          action -> {
            setEnabled(false);
            deleted = true;
            this.removeAll();
            referenceSize.set(0);
            relativeSize.set(0);
            bold.set(false);
            italic.set(false);
            fontIn = null;
            fontOut.set(null);
          });
    }
    if (optionList.contains(Option.RELATIVE_SIZE)) {
      add(sizeSpinner);
    }
    if (optionList.contains(Option.BOLD)) {
      add(boldToggle);
    }
    if (optionList.contains(Option.ITALIC)) {
      add(italicToggle);
    }
    if (optionList.contains(Option.FONT_LOGICAL)
        || optionList.contains(Option.FONT_NAME)
        || optionList.contains(Option.FONT_FAMILY)) {
      add(fontCombo);
    }

    for (Component c : getComponents()) {
      c.setEnabled(this.isEnabled());
    }

    ChangeListener<Object> changeListener =
        (obs, was, now) -> {
          updateFont();
          firePropertyChange("font", was, now);
        };

    bold.addListener(changeListener);
    italic.addListener(changeListener);
    fontOut.addListener(changeListener);
    referenceSize.addListener(changeListener);
    relativeSize.addListener(changeListener);

    bold.addListener((evt, was, now) -> boldToggle.setSelected(now));
    italic.addListener((evt, was, now) -> italicToggle.setSelected(now));

    boldToggle.addActionListener(evt -> setBold(((FlatToggleButton) evt.getSource()).isSelected()));
    italicToggle.addActionListener(
        evt -> setItalic(((FlatToggleButton) evt.getSource()).isSelected()));

    validate();
    return this;
  }

  private void setupComponents() {
    if (optionList.contains(Option.DELETE)) {
      deleteButton.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_DELETE));
    }
    if (optionList.contains(Option.BOLD)) {
      setupToggle(Option.BOLD);
    }
    if (optionList.contains(Option.ITALIC)) {
      setupToggle(Option.ITALIC);
    }
    if (optionList.contains(Option.RELATIVE_SIZE)) {
      int side = UIManager.getDefaults().getFont("defaultFont").getSize();
      sizeSpinner.setMaximumSize(new Dimension(side * 6, side * 3));
      spinnerNumberModel =
          new SpinnerNumberModel(
              getRelativeSize(), Math.min(getRelativeSize(), 1 - getReferenceSize()), 200, 1);
      sizeSpinner.setModel(spinnerNumberModel);
      referenceSize.addListener(
          (obs, old, now) -> {
            spinnerNumberModel.setMinimum(1 - now.intValue());
            spinnerNumberModel.setValue(
                Math.max(
                    ((Number) spinnerNumberModel.getMinimum()).intValue(),
                    ((Number) spinnerNumberModel.getValue()).intValue()));
          });
      sizeSpinner.addChangeListener(
          e -> relativeSize.set(spinnerNumberModel.getNumber().intValue()));
    }
    if (optionList.contains(Option.FONT_NAME)
        || optionList.contains(Option.FONT_FAMILY)
        || optionList.contains(Option.FONT_LOGICAL)) {
      setupCombo();
    }

    relativeSize.addListener((ob, o, n) -> updateFont());
  }

  private void setupToggle(Option option) {
    FlatToggleButton toggle;
    switch (option) {
      case BOLD -> {
        toggle = boldToggle;
        toggle.setText(I18N.getText("Button.text.bold.short"));
        toggle.setToolTipText(I18N.getText("Button.text.bold"));
      }
      case ITALIC -> {
        toggle = italicToggle;
        toggle.setText(I18N.getText("Button.text.italic.short"));
        toggle.setToolTipText(I18N.getText("Button.text.italic"));
      }
      default -> {
        return;
      }
    }
    toggle.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Button.border"), 1),
            BorderFactory.createEmptyBorder(0, 0, 2, 0)));
    toggle.setFont(
        Font.decode(Font.SERIF)
            .deriveFont(UIManager.getDefaults().getFont("defaultFont").getSize2D() + 2f));
    if (option.equals(Option.BOLD)) {
      toggle.setFont(toggle.getFont().deriveFont(Font.BOLD));
    }
    toggle.setSquareSize(true);
    toggle.setButtonType(FlatButton.ButtonType.tab);
  }

  private void setupCombo() {
    fontList.addAll(ThemeFontTools.FONT_LIST);
    int maxWidth;
    Option option;
    if (optionList.contains(Option.FONT_FAMILY)) {
      option = Option.FONT_FAMILY;
      maxWidth = ThemeFontTools.FONT_MAX_NAME_WIDTHS.get("familyName");
      fontList =
          fontList.stream().sorted(Comparator.comparing(Font::getFamily)).distinct().toList();
    } else if (optionList.contains(Option.FONT_LOGICAL)) {
      option = Option.FONT_LOGICAL;
      maxWidth = ThemeFontTools.FONT_MAX_NAME_WIDTHS.get("name");
      fontList = fontList.stream().sorted(Comparator.comparing(Font::getName)).distinct().toList();
    } else if (optionList.contains(Option.FONT_NAME)) {
      option = Option.FONT_NAME;
      maxWidth = ThemeFontTools.FONT_MAX_NAME_WIDTHS.get("fontName");
      fontList =
          fontList.stream().sorted(Comparator.comparing(Font::getFontName)).distinct().toList();
    } else {
      fontCombo.setVisible(false);
      return;
    }
    fontCombo.setRenderer(fontComboBoxRenderer);
    fontCombo.setMaximumSize(new Dimension(UIScale.scale(maxWidth), 400));
    fontCombo.setModel(new JComboBox<>(fontList.toArray(new Font[0])).getModel());
    fontCombo.addItemListener(
        item -> {
          Font font = (Font) Objects.requireNonNull(fontCombo.getModel().getSelectedItem());
          setBold(font.isBold());
          setItalic(font.isItalic());
          font.deriveFont(
              isBold() ? Font.BOLD : Font.PLAIN + (isItalic() ? Font.ITALIC : Font.PLAIN),
              getReferenceSize() + getRelativeSize());
          fontOut.set(font);

          fontCombo.setFont(
              ((Font) fontCombo.getModel().getSelectedItem())
                  .deriveFont(UIManager.getDefaults().getFont("defaultFont").getSize2D()));
          switch (option) {
            case FONT_FAMILY -> fontName = fontCombo.getFont().getFamily();
            case FONT_LOGICAL -> fontName = fontCombo.getFont().getName();
            case FONT_NAME -> fontName = fontCombo.getFont().getFontName();
          }
          updateFont();
        });
    fontCombo.getModel().setSelectedItem(fontIn);
  }

  public void updateFont() {
    Font font = (Font) fontCombo.getSelectedItem();
    if (font == null) {
      font = fontIn;
    }
    fontOut.set(
        font.deriveFont(
            (isBold() ? Font.BOLD : Font.PLAIN) + (isItalic() ? Font.ITALIC : Font.PLAIN),
            referenceSize.floatValue() + relativeSize.floatValue()));

    if (exemplar != null) {
      if (optionList.contains(Option.PREFIX)) {
        exemplar.setText(MessageFormat.format("{0} > {1}", prefix, fontName));
      }
      exemplar.setFont(getFontOut().deriveFont(getFontSize2D()));
    }
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser setBold(boolean b) {
    bold.set(b);
    boldToggle.setSelected(b);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser setItalic(boolean i) {
    italic.set(i);
    italicToggle.setSelected(i);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser setReferenceSize(int ref) {
    referenceSize.set(ref);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser setLabel(FlatLabel label) {
    exemplar = label;
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser setLabelPrefix(String text) {
    prefix = text;
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FontChooser setRelativeSize(int size) {
    relativeSize.set(size);
    return this;
  }

  public int getRelativeSize() {
    return relativeSize.get();
  }

  public Font getFontOut() {
    return fontOut.get();
  }

  public boolean isItalic() {
    return italic.get();
  }

  public boolean isBold() {
    return bold.get();
  }

  public int getReferenceSize() {
    return referenceSize.intValue();
  }

  public float getFontSize2D() {
    return fontOut.get().getSize2D();
  }

  public int getFontSize() {
    return fontOut.get().getSize();
  }

  @SuppressWarnings("unused")
  public Font getChosenFont() {
    return fontOut.get();
  }

  public SpinnerNumberModel getSpinnerNumberModel() {
    return spinnerNumberModel;
  }

  private final BasicComboBoxRenderer fontComboBoxRenderer =
      new BasicComboBoxRenderer.UIResource() {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
          } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
          }
          if (value == null) {
            setText("---");
          } else if (value instanceof Font font) {
            if (optionList.contains(Option.FONT_FAMILY)) {
              setText(font.getFamily());
            } else if (optionList.contains(Option.FONT_LOGICAL)) {
              setText(font.getName());
            } else if (optionList.contains(Option.FONT_NAME)) {
              setText(font.getFontName());
            }
            setFont(font.deriveFont(ThemeFontTools.OS_DEFAULT_FONT_SIZE + 1f));
          }
          return this;
        }
      };

  @Override
  public Component add(Component c) {
    super.add(Box.createHorizontalStrut(3));
    return super.add(c);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    for (Component c : getComponents()) {
      c.setEnabled(enabled);
    }
    if (!enabled && fontIn != null) {
      useFont(fontIn);
      fontCombo.getModel().setSelectedItem(fontIn);
    }
    if (deleted && enabled) {
      super.setEnabled(false);
    }
  }

  public String getUIClassID() {
    return uiClassID;
  }

  public String toFlatPropertyString() {
    if (!isEnabled()) {
      return "";
    }
    DecimalFormat nf = new DecimalFormat("+0;-0");
    if (ThemeFontTools.FONT_STYLE_FAMILIES.contains(this.getName().replace(".chooser", ""))) {
      return MessageFormat.format(
          "{0}{1}{2}{3}",
          nf.format(getRelativeSize()),
          isBold() ? " bold" : "",
          isItalic() ? " italic" : "",
          fontName == null ? "" : " \"" + fontName + "\"");
    } else if (ThemeFontTools.GENERAL_FONT_KEYS.contains(this.getName().replace(".chooser", ""))) {
      return MessageFormat.format(
          "{0}{1}{2}",
          nf.format(getRelativeSize()), isBold() ? " bold" : "", isItalic() ? " italic" : "");
    }
    return "";
  }
}
