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
package net.rptools.maptool.client.ui.theme;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatLabel;
import java.awt.*;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.FontChooser;
import net.rptools.maptool.language.I18N;

@SuppressWarnings("rawtypes")
public class ThemeFontPreferences extends AbeillePanel {
  public ThemeFontPreferences() {
    super(new ThemeFontPreferencesPanel().getRootComponent());
    panelInit();
    validate();
    for (FontChooser chooser : FONT_CHOOSER_MAP.values()) {
      chooser.addPropertyChangeListener(changeListener);
    }
  }

  private static final LookAndFeel CURRENT_LAF = UIManager.getLookAndFeel();
  private static final UIDefaults LAF_DEFAULTS = CURRENT_LAF.getDefaults();

  private static final Map<String, FontChooser> FONT_CHOOSER_MAP = new HashMap<>();
  private static final String DF = "defaultFont";

  private final boolean prefAtLoad = AppPreferences.useCustomThemeFontProperties.get();
  private boolean stateChanged;
  private final PropertyChangeListener changeListener =
      new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          stateChanged =
              stateChanged
                  | evt.getPropertyName().equals("font")
                  | evt.getPropertyName().equals("enabled");
        }
      };

  @SuppressWarnings("unused")
  public void initComponents() {
    stateChanged = false;
    // app preference checkbox to use alternate font settings
    JCheckBox useCustomUIProperties = getCheckBox("useCustomUIProperties");
    useCustomUIProperties.setSelected(AppPreferences.useCustomThemeFontProperties.get());
    useCustomUIProperties.addChangeListener(
        change ->
            AppPreferences.useCustomThemeFontProperties.set(useCustomUIProperties.isSelected()));

    // everything is relative to "defaultFont" so we pull that out and grab some values
    Font defaultFont = LAF_DEFAULTS.getFont(DF);
    int referenceSize = defaultFont.getSize();
    int defaultFontRelativeSizeValue;
    String defaultFontProperty;
    Font referenceFont = null;
    if (ThemeFontTools.USER_FLAT_PROPERTY_NAMES.contains(DF)) {
      defaultFontProperty = ThemeFontTools.USER_FLAT_PROPERTIES.getProperty(DF);
      referenceFont = ThemeFontTools.parseFlatPropertyString(defaultFontProperty);
    }
    if (referenceFont == null) {
      defaultFontRelativeSizeValue = referenceSize - ThemeFontTools.OS_DEFAULT_FONT_SIZE;
    } else {
      defaultFontRelativeSizeValue = referenceFont.getSize();
    }

    // Keys for general font sizes as well as generic font styles
    List<String> allKeys =
        Stream.of(ThemeFontTools.GENERAL_FONT_KEYS, ThemeFontTools.FONT_STYLE_FAMILIES)
            .flatMap(Collection::stream)
            .distinct()
            .toList();
    for (String key : allKeys) {
      boolean enabled = ThemeFontTools.USER_FLAT_PROPERTIES.containsKey(key);
      JCheckBox cb = getCheckBox(key + ".enable");
      cb.setSelected(enabled);
      cb.addItemListener(enableListener);
      cb.addItemListener(
          item -> {
            if (((JCheckBox) item.getSource()).isSelected()) {
              useCustomUIProperties.setSelected(true);
            }
          });
      // set up the labels to display the various fonts and sizes
      FlatLabel label = new FlatLabel();
      label.setText(((JLabel) getComponent(key + ".label")).getText());
      replaceComponent(getComponent(key + ".label").getParent().getName(), key + ".label", label);
      label.setName(key + ".label");

      if (ThemeFontTools.FONT_STYLE_FAMILIES.contains(key)) {
        if (key.equalsIgnoreCase(DF)) {
          // defaultFont breaks the naming syntax so it gets its own fork
          label.setLabelType(FlatLabel.LabelType.regular);
        } else {
          Font lafFont = LAF_DEFAULTS.getFont(key);
          label.setFont(lafFont);
        }
        // as one of the four principal fonts, we want a little more in our exemplar
        label.setText(
            MessageFormat.format(
                "{0} > {1}",
                I18N.getText("Preferences.label." + key), LAF_DEFAULTS.getFont(key).getFamily()));
      } else {
        // all the generic sizes plus headings
        label.setLabelType(
            FlatLabel.LabelType.valueOf(key.substring(0, key.indexOf(".")).toLowerCase()));
      }

      Component placeHolder = getLabel(key + ".chooser");

      FontChooser fontChooser =
          new FontChooser()
              .setLabel(label)
              .useFont(label.getFont())
              .addOptions(
                  FontChooser.Option.FONT_LOGICAL,
                  FontChooser.Option.RELATIVE_SIZE,
                  FontChooser.Option.BOLD,
                  FontChooser.Option.ITALIC,
                  FontChooser.Option.DELETE);
      fontChooser.addPropertyChangeListener(
          pc -> {
            if (pc.getPropertyName().equalsIgnoreCase("enabled")) {
              cb.setSelected((Boolean) pc.getNewValue());
            }
          });
      if (key.equalsIgnoreCase(DF)) {
        fontChooser.setReferenceSize(referenceSize - defaultFontRelativeSizeValue);
        fontChooser.setRelativeSize(defaultFontRelativeSizeValue);
      } else {
        fontChooser.setReferenceSize(referenceSize);
        String userProperty = ThemeFontTools.USER_FLAT_PROPERTIES.getProperty(key);
        String modelProperty = ThemeFontTools.MODEL_FLAT_PROPERTIES.getProperty(key);
        try {
          fontChooser.setRelativeSize(Integer.parseInt(userProperty.split(" ")[0]));
        } catch (NullPointerException | NumberFormatException ignored) {
          try {
            fontChooser.setRelativeSize(Integer.parseInt(modelProperty.split(" ")[0]));
          } catch (NullPointerException ignore) {
            fontChooser.setRelativeSize(0);
          }
        }
      }
      fontChooser.setName(key + ".chooser");
      fontChooser.setEnabled(enabled);

      if (ThemeFontTools.FONT_STYLE_FAMILIES.contains(key)) {
        // if it is one of the four principal fonts, add label prefix
        fontChooser =
            fontChooser
                .addOption(FontChooser.Option.PREFIX)
                .setLabelPrefix(I18N.getText("Preferences.label." + key));
        // if it is one of the four principal fonts, we only want size on defaultFont
        if (!key.equalsIgnoreCase(DF)) {
          fontChooser.removeOption(FontChooser.Option.RELATIVE_SIZE);
        }
      }
      // unless it is the default font, for the general styles we do not want the combo-box
      if (ThemeFontTools.GENERAL_FONT_KEYS.contains(key) && !key.equalsIgnoreCase(DF)) {
        fontChooser.removeOption(FontChooser.Option.FONT_LOGICAL);
      }

      FONT_CHOOSER_MAP.put(key, fontChooser.create());
      replaceComponent(placeHolder.getParent().getName(), placeHolder.getName(), fontChooser);
    }
    FONT_CHOOSER_MAP.get(DF).getSpinnerNumberModel().addChangeListener(e -> changeReferenceSize());
  }

  private void changeReferenceSize() {
    int value = FONT_CHOOSER_MAP.get(DF).getFontSize();
    for (String key : FONT_CHOOSER_MAP.keySet()) {
      if (!key.equalsIgnoreCase(DF)) {
        FONT_CHOOSER_MAP.get(key).setReferenceSize(value);
      }
    }
  }

  private final ItemListener enableListener =
      e -> {
        JCheckBox cb = (JCheckBox) e.getSource();
        boolean enabled = cb.isSelected();
        int end = cb.getName().lastIndexOf(".");
        end = end == -1 ? cb.getName().length() : end;
        String key = cb.getName().substring(0, end);

        FONT_CHOOSER_MAP.get(key).setEnabled(enabled);

        if (key.equalsIgnoreCase(DF)) {
          changeReferenceSize();
        }
      };

  @Override
  public boolean commit() {
    if (prefAtLoad == AppPreferences.useCustomThemeFontProperties.get() && !stateChanged) {
      return false;
    }
    String[] keys = FONT_CHOOSER_MAP.keySet().toArray(new String[0]);

    for (int i = keys.length - 1; i > -1; i--) {
      if (!(FONT_CHOOSER_MAP.get(keys[i]).isEnabled()
          && AppPreferences.useCustomThemeFontProperties.get())) {
        FONT_CHOOSER_MAP.remove(keys[i]);
      }
    }
    boolean write = ThemeFontTools.writeCustomProperties(FONT_CHOOSER_MAP);
    if (write) {
      FlatLaf.updateUI();
      FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
    }
    return write;
  }
}
