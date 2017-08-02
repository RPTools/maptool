/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.launcher;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class InfoTextField extends JTextField implements FocusListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String info;

	private static Font focusedFont;
	private static Font unfocusedFont;

	static {
		focusedFont = new Font("SanSerif", Font.BOLD, 12); //$NON-NLS-1$
		unfocusedFont = new Font("SanSerif", Font.ITALIC | Font.BOLD, 12); //$NON-NLS-1$
	}

	public InfoTextField() {
		this(""); //$NON-NLS-1$
		setFont(unfocusedFont);
	}

	public InfoTextField(final String info) {
		setInfo(info);
		addFocusListener(this);
	}

	public void setInfo(String i) {
		info = i;
		setUI(new InfoTextFieldUI(info, true));
	}

	@Override
	public void focusGained(FocusEvent e) {
		selectAll();
		setFont(focusedFont);
		if (getText().length() == 0) {
			super.setText(""); //$NON-NLS-1$
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		setFont(unfocusedFont);
		if (getText().length() == 0) {
			setInfo(info);
		}
	}

	@Override
	public String getText() {
		final String typed = super.getText();
		return typed.equals(info) ? "" : typed; //$NON-NLS-1$
	}
}