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
	//	private static final Logger log = Logger.getLogger(InfoTextFieldUI.class.getName());

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
