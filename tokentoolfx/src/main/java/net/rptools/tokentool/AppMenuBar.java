/*
 * The MIT License
 * 
 * Copyright (c) 2005 David Rice, Trevor Croft
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.rptools.tokentool;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class AppMenuBar extends JMenuBar {

	public AppMenuBar() {

		add(createFileMenu());
		add(createToolMenu());
		add(createHelpMenu());
	}

	protected JMenu createToolMenu() {
		JMenu menu = new JMenu("Tool");

		menu.add(new JMenuItem(AppActions.SCREEN_CAP));
		menu.addSeparator();
		menu.add(new JMenuItem(AppActions.COPY_CLIPBOARD));
		menu.add(new JMenuItem(AppActions.PASTE_CLIPBOARD));

		return menu;
	}

	protected JMenu createHelpMenu() {

		JMenu menu = new JMenu("Help");

		menu.add(new JMenuItem(AppActions.SHOW_ABOUT));

		return menu;
	}

	protected JMenu createFileMenu() {

		JMenu menu = new JMenu("File");

		menu.add(new JMenuItem(AppActions.SHOW_OVERLAY_MANAGEMENT_DIALOG));
		menu.add(new JMenuItem(AppActions.SAVE_TOKEN));
		menu.add(new JMenuItem(AppActions.SAVE_TOKEN_IMAGE));
		menu.addSeparator();
		menu.add(new JMenuItem(AppActions.EXIT_APP));

		return menu;
	}
}
