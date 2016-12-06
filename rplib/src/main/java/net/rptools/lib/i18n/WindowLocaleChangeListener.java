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
