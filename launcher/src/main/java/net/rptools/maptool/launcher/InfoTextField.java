/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
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