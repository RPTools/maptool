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
