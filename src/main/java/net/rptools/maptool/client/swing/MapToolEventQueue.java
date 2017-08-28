/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.swing;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jidesoft.dialog.JideOptionPane;

public class MapToolEventQueue extends EventQueue {
	private static final Logger log = LogManager.getLogger(MapToolEventQueue.class);
	private static final JideOptionPane optionPane = new JideOptionPane(I18N.getString("MapToolEventQueue.details"), JOptionPane.ERROR_MESSAGE, JideOptionPane.CLOSE_OPTION); //$NON-NLS-1$

	@Override
	protected void dispatchEvent(AWTEvent event) {
		try {
			super.dispatchEvent(event);
		} catch (StackOverflowError soe) {
			log.error(soe, soe);
			optionPane.setTitle(I18N.getString("MapToolEventQueue.stackOverflow.title")); //$NON-NLS-1$
			optionPane.setDetails(I18N.getString("MapToolEventQueue.stackOverflow"));
			displayPopup();
		} catch (Throwable t) {
			log.error(t, t);
			optionPane.setTitle(I18N.getString("MapToolEventQueue.unexpectedError")); //$NON-NLS-1$
			optionPane.setDetails(toString(t));
			try {
				displayPopup();
			} catch (Throwable thrown) {
				// Displaying the error message using the JideOptionPane has just failed.
				// Fallback to standard swing dialog.
				log.error(thrown, thrown);
				JOptionPane.showMessageDialog(null, toString(thrown),
						I18N.getString("MapToolEventQueue.unexpectedError"), JOptionPane.ERROR_MESSAGE);
			} finally {
				System.out.println("INSERT SENTRY LOG HERE!");
				log.debug("SENTRY TEST :: Debug message");
				log.info("SENTRY TEST :: Info message");
				log.warn("SENTRY TEST :: Warn message");
			}
		}
	}

	private static void displayPopup() {
		optionPane.setDetailsVisible(true);
		JDialog dialog = optionPane.createDialog(MapTool.getFrame(), I18N.getString("MapToolEventQueue.warning.title")); //$NON-NLS-1$
		dialog.setResizable(true);
		dialog.pack();
		dialog.setVisible(true);
	}

	private static String toString(Throwable t) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(out);
		t.printStackTrace(ps);
		ps.close();
		return out.toString();
	}
}
