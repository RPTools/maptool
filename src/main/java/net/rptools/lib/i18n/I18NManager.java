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

public final class I18NManager {

  private static final List<String> BUNDLE_NAME_LIST = new CopyOnWriteArrayList<String>();
  private static final List<ResourceBundle> BUNDLE_LIST = new ArrayList<ResourceBundle>();
  private static Locale locale = Locale.US;
  private static final List<LocaleChangeListener> LOCALE_LISTENER_LIST =
      new CopyOnWriteArrayList<LocaleChangeListener>();

  private I18NManager() {}

  public static void addBundle(String bundleName) {
    BUNDLE_NAME_LIST.add(bundleName);
    updateBundles();
  }

  public static void removeBundle(String bundleName) {
    BUNDLE_NAME_LIST.remove(bundleName);
    updateBundles();
  }

  public static void setLocale(Locale locale) {
    I18NManager.locale = locale;
    updateBundles();
    fireLocaleChanged();
  }

  public static String getText(String key) {

    return BUNDLE_LIST.stream().findFirst().map(bundle -> bundle.getString(key)).orElse(key);
  }

  private static synchronized void fireLocaleChanged() {

    for (LocaleChangeListener listener : LOCALE_LISTENER_LIST) {
      listener.localeChanged(locale);
    }
  }

  private static synchronized void updateBundles() {

    BUNDLE_LIST.clear();

    for (String bundleName : BUNDLE_NAME_LIST) {
      BUNDLE_LIST.add(ResourceBundle.getBundle(bundleName, locale));
    }
  }
}
