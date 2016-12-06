/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
	private static List<LocaleChangeListener> localeListenerList = new CopyOnWriteArrayList<LocaleChangeListener>();

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

	private synchronized static void fireLocaleChanged() {

		for (LocaleChangeListener listener : localeListenerList) {
			listener.localeChanged(locale);
		}
	}

	private synchronized static void updateBundles() {

		bundleList.clear();

		for (String bundleName : bundleNameList) {

			bundleList.add(ResourceBundle.getBundle(bundleName, locale));
		}
	}
}
