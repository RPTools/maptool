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

import java.awt.Label;
import java.util.Locale;

public class LabelLocaleChangeListener implements LocaleChangeListener {

  private Label label;
  private String key;

  public LabelLocaleChangeListener(Label label, String key) {
    this.label = label;
    this.key = key;
  }

  ////
  // LOCALE CHANGE LISTENER
  public void localeChanged(Locale locale) {
    label.setText(I18NManager.getText(key));
  }
}
