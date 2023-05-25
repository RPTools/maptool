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
package net.rptools.maptool.client.ui.misc;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.rptools.maptool.util.I18nName;

/**
 * Class that implements the Combo Box renderer for enums.
 *
 * @param <T> The enum type.
 */
class EnumComboBoxRenderer<T extends Enum<T>> extends BasicComboBoxRenderer {

  /** Creates a new instance of the renderer. */
  public EnumComboBoxRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getListCellRendererComponent(
      JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (value instanceof I18nName i18nName) {
      setText(i18nName.i18nName());
    } else {
      setText(value.toString());
    }
    return this;
  }
}
