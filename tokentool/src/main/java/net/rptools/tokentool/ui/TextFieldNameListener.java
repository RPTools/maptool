/**
 * 
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