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
package net.rptools.maptool.model.localisedObject;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Map;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferenceEnums;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.util.preferences.Preference;
import org.jetbrains.annotations.NotNull;

/**
 * Stores the localised display name and preference value String for menu items that don't have a
 * corresponding enum.
 */
public class LocalEnumListItem<E extends Enum<?>> extends LocalListItem implements LocalObject {
  /** Stores the localized display name and preference value for list, combo and menu items */
  public interface EnumPreferenceItem<E extends Enum<E>> extends LocalObject {
    /**
     * @return the actual value used by the application.
     */
    @Override
    @NotNull
    E getValue();

    void updatePreference(Enum<?> newValue);
  }

  /**
   * Creates a localised item.
   *
   * @param enumConstant the constant value from the enum.
   * @param i18nKey the i18n key to use for the display name.
   */
  public LocalEnumListItem(E enumConstant, String i18nKey) {
    super(enumConstant, i18nKey);
  }

  static Map<AppPreferenceEnums.GridType, Icon> gridIconMap =
      Map.of(
          AppPreferenceEnums.GridType.NONE, RessourceManager.getSmallIcon(Icons.GRID_NONE),
          AppPreferenceEnums.GridType.HEX_VERT,
              RessourceManager.getSmallIcon(Icons.GRID_HEX_VERTICAL),
          AppPreferenceEnums.GridType.HEX_HORI,
              RessourceManager.getSmallIcon(Icons.GRID_HEX_HORIZONTAL),
          AppPreferenceEnums.GridType.ISOMETRIC,
              RessourceManager.getSmallIcon(Icons.GRID_ISOMETRIC),
          AppPreferenceEnums.GridType.SQUARE, RessourceManager.getSmallIcon(Icons.GRID_SQUARE));
  private static final ListCellRenderer<?> cellRendererWithGridIcon =
      new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          JLabel lbl =
              (JLabel)
                  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          lbl.setIcon(gridIconMap.get(((AppPreferenceEnums.GridType) value)));
          return lbl;
        }
      };

  /** Utility method to create and set the selected item for preference enum combo box models. */
  public static JComboBox<Enum<?>> createComboBox(Preference<?> p) {
    JComboBox<Enum<?>> comboBox = new JComboBox<>((Enum<?>[]) p.getValueClass().getEnumConstants());
    if (p.equals(AppPreferences.defaultGridType)) {
      comboBox.setRenderer((ListCellRenderer<? super Enum<?>>) cellRendererWithGridIcon);
    }
    comboBox.setSelectedItem(p.get());
    comboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            try {
              if (comboBox.getSelectedItem()
                  instanceof LocalEnumListItem.EnumPreferenceItem<?> selected) {
                selected.updatePreference(selected.getValue());
              }
            } catch (Exception ex) {
              System.out.println("listener error: " + ex);
            }
          }
        });
    return comboBox;
  }
}
