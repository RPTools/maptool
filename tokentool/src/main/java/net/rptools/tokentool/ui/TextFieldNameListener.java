/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.ui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author cif
 */
class TextFieldNameListener implements DocumentListener {
	public TextFieldNameListener(JTextField field) {
		setTextField(field);

	}

	JTextField _linkedField;

	public void setTextField(JTextField field) {
		_linkedField = field;
	}

	public void insertUpdate(DocumentEvent e) {
		setLinkedFieldToZero();
	}

	public void removeUpdate(DocumentEvent e) {
		setLinkedFieldToZero();
	}

	public void changedUpdate(DocumentEvent e) {
		setLinkedFieldToZero();
	}

	private void setLinkedFieldToZero() {
		// Plain text components do not fire these events
		if (_linkedField != null)
			_linkedField.setText("0");

	}
}