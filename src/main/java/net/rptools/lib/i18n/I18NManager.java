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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

public class I18NManager {

  private static List<String> bundleNameList = new CopyOnWriteArrayList<String>();
  private static List<ResourceBundle> bundleList = new ArrayList<ResourceBundle>();
  private static Locale locale = Locale.US;
  private static List<LocaleChangeListener> localeListenerList =
      new CopyOnWriteArrayList<LocaleChangeListener>();

  public static void addBundle(String bundleName) {
    bundleNameList.add(bundleName);
    updateBundles();
  }

  public static void removeBundle(String bundleName) {
    bundleNameList.remove(bundleName);
    updateBundles();
  }

  public static void setLocale(Locale locale) {
    I18NManager.locale = locale;
    updateBundles();
    fireLocaleChanged();
  }

  public static String getText(String key) {

    for (ResourceBundle bundle : bundleList) {

      String value = bundle.getString(key);
      if (value != null) {
        return value;
      }
    }

    return key;
  }

  private static synchronized void fireLocaleChanged() {

    for (LocaleChangeListener listener : localeListenerList) {
      listener.localeChanged(locale);
    }
  }

  private static synchronized void updateBundles() {

    bundleList.clear();

    for (String bundleName : bundleNameList) {

      bundleList.add(ResourceBundle.getBundle(bundleName, locale));
    }
  }
}
