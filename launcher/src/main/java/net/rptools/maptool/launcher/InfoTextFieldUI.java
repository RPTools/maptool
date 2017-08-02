/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.launcher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/**
 * @author frank
 */
public class InfoTextFieldUI extends BasicTextFieldUI implements FocusListener {
	// private static final Logger log = Logger.getLogger(InfoTextFieldUI.class.getName());

	private final String info;
	private final boolean hideOnFocus;
	private final Color color;

	private void repaint() {
		if (getComponent() != null) {
			getComponent().repaint();
		}
	}

	public InfoTextFieldUI(String info, boolean hideOnFocus) {
		this(info, hideOnFocus, null);
	}

	public InfoTextFieldUI(String info, boolean hideOnFocus, Color color) {
		this.info = info;
		this.hideOnFocus = hideOnFocus;
		this.color = color;
	}

	@Override
	protected void paintSafely(Graphics g) {
		super.paintSafely(g);
		final JTextComponent comp = getComponent();
		if (info != null && comp.getText().length() == 0 && (!(hideOnFocus && comp.hasFocus()))) {
			if (color != null) {
				g.setColor(color);
			} else {
				g.setColor(Color.gray);
			}
			final int padding = (comp.getHeight() - comp.getFont().getSize()) / 2;
			g.drawString(info, 5, comp.getHeight() - padding - 1);
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (hideOnFocus) {
			repaint();
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (hideOnFocus) {
			repaint();
		}
	}

	@Override
	protected void installListeners() {
		super.installListeners();
		getComponent().addFocusListener(this);
	}

	@Override
	protected void uninstallListeners() {
		super.uninstallListeners();
		getComponent().removeFocusListener(this);
	}
}
