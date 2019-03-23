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
package net.rptools.lib.i18n;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Locale;

public class WindowLocaleChangeListener implements LocaleChangeListener {

  private Window window;
  private String key;

  public WindowLocaleChangeListener(Window window, String key) {
    this.window = window;
    this.key = key;
  }

  ////
  // LOCALE CHANGE LISTENER
  public void localeChanged(Locale locale) {

    if (window instanceof Dialog) {
      ((Dialog) window).setTitle(I18NManager.getText(key));
    }
    if (window instanceof Frame) {
      ((Frame) window).setTitle(I18NManager.getText(key));
    }
  }
}
