/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
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
