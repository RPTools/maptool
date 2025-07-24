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

import java.util.Arrays;
import javax.swing.*;

/** Stores the preference value and localised display name for list, combo and menu items. */
public class LocalListItem extends AbstractLocalObject {
  /**
   * New Local List Item
   *
   * @param value the value held by the object
   * @param i18nKeys varArgs for the i18n key and any optional arguments to pass with it.
   */
  public LocalListItem(Object value, String... i18nKeys) {
    super(value, i18nKeys);
  }

  /** Utility method to create a ListModel from an array o LocalisedListItems. */
  @SuppressWarnings("unused")
  public static ListModel<LocalListItem> getLocalisedListModel(LocalListItem[] items) {
    DefaultListModel<LocalListItem> model = new DefaultListModel<>();
    model.addAll(Arrays.stream(items).toList());
    return model;
  }

  /** Utility method to create a ComboBoxModel from an array of LocalisedListItems. */
  @SuppressWarnings("unused")
  public static <T> ComboBoxModel<LocalListItem> getLocalisedComboBoxModel(LocalListItem[] items) {
    return new DefaultComboBoxModel<>(items);
  }

  /**
   * Utility method to create a ComboBoxModel from an array of LocalisedListItems and set the
   * selected item.
   */
  public static ComboBoxModel<LocalListItem> getLocalisedSetComboBoxModel(
      LocalListItem[] items, String selected) {
    ComboBoxModel<LocalListItem> model = getLocalisedComboBoxModel(items);

    try {
      model.setSelectedItem(
          Arrays.stream(items)
              .filter(localisedListItem -> localisedListItem.getValue().equals(selected))
              .toList()
              .getFirst());
    } catch (Exception ignored) {
      try {
        model.setSelectedItem(
            Arrays.stream(items)
                .filter(
                    localisedListItem ->
                        localisedListItem
                            .getValue()
                            .toString()
                            .replaceAll("[_\\W]", "")
                            .equalsIgnoreCase(selected.replaceAll("[_\\W]", "")))
                .toList()
                .getFirst());
      } catch (Exception ignore) {
      }
    }
    return model;
  }
}
