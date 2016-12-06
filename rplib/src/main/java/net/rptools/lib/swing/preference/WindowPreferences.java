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
package net.rptools.lib.swing.preference;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Automatically keeps track of and restores frame size when opening/closing
 * the application.
 * 
 * To use, simply add a line like this to you frame's constructor:
 * 
 *      new WindowPreferences(appName, identifier, this);
 */
public class WindowPreferences extends WindowAdapter {
	private final Preferences prefs;

	private static final String KEY_X = "x";
	private static final String KEY_Y = "y";
	private static final String KEY_WIDTH = "width";
	private static final String KEY_HEIGHT = "height";

	private static int DEFAULT_X;
	private static int DEFAULT_Y;
	private static int DEFAULT_WIDTH;
	private static int DEFAULT_HEIGHT;

	/**
	 * Creates an object that holds the window boundary information after storing it into
	 * {@link Preferences#userRoot()}.  This object also registers a <code>WindowListener</code> on the
	 * passed in <code>Window</code> object so that it is notified when the window is closed,
	 * allowing this object to save the final window boundary into <code>Preferences</code> again.
	 * 
	 * @param appName top-level name to use in the Preferences
	 * @param controlName bottom level name to use
	 * @param window the window whose boundary information is being recorded
	 */
	public WindowPreferences(String appName, String controlName, Window window) {
		prefs = Preferences.userRoot().node(appName + "/control/" + controlName);

		DEFAULT_X = window.getLocation().x;
		DEFAULT_Y = window.getLocation().y;
		DEFAULT_WIDTH = window.getSize().width;
		DEFAULT_HEIGHT = window.getSize().height;

		restorePreferences(window);
		window.addWindowListener(this);
	}

	/**
	 * Clear out window preferences from the user's system
	 */
	public void clear() {
		try {
			prefs.clear();
		} catch (BackingStoreException bse) {
			// This error shouldn't matter, really,
			// since it is an asthetic action
			bse.printStackTrace();
		}
	}

	////
	// Preferences

	protected int getX() {
		return prefs.getInt(KEY_X, DEFAULT_X);
	}

	protected void setX(int x) {
		prefs.putInt(KEY_X, x);
	}

	protected int getY() {
		return prefs.getInt(KEY_Y, DEFAULT_Y);
	}

	protected void setY(int y) {
		prefs.putInt(KEY_Y, y);
	}

	protected int getWidth() {
		return prefs.getInt(KEY_WIDTH, DEFAULT_WIDTH);
	}

	protected void setWidth(int width) {
		prefs.putInt(KEY_WIDTH, width);
	}

	protected int getHeight() {
		return prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT);
	}

	protected void setHeight(int height) {
		prefs.putInt(KEY_HEIGHT, height);
	}

	protected void storePreferences(Window frame) {
		setX(frame.getLocation().x);
		setY(frame.getLocation().y);

		setWidth(frame.getSize().width);
		setHeight(frame.getSize().height);
	}

	protected void restorePreferences(Window frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int x = Math.max(Math.min(getX(), screenSize.width - getWidth()), 0);
		int y = Math.max(Math.min(getY(), screenSize.height - getHeight()), 0);

		frame.setSize(getWidth(), getHeight());
		frame.setLocation(x, y);
	}

	////
	// WINDOW ADAPTER
	@Override
	public final void windowClosing(WindowEvent e) {
		storePreferences((Window) e.getSource());
	}
}
