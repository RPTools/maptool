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
package net.rptools.maptool.client.ui.themes;

//
// DO NOT MODIFY
// Generated with com.formdev.flatlaf.demo.intellijthemes.IJThemesClassGenerator
//

import com.formdev.flatlaf.IntelliJTheme;

//
// taken from
// https://github.com/JFormDesigner/FlatLaf/tree/0291dd5416b6bbf8ea2f3d5e4f7a4f22d6368e36/flatlaf-intellij-themes
//
public class FlatDraculaContrastIJTheme extends IntelliJTheme.ThemeLaf {
  public static final String NAME = "Dracula Contrast (Material)";

  public static boolean setup() {
    try {
      return setup(new FlatDraculaContrastIJTheme());
    } catch (RuntimeException ex) {
      return false;
    }
  }

  public static void installLafInfo() {
    installLafInfo(NAME, FlatDraculaContrastIJTheme.class);
  }

  public FlatDraculaContrastIJTheme() {
    super(Utils.loadTheme("Dracula Contrast.theme.json"));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
