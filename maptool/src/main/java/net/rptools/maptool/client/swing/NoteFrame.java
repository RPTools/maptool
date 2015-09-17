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

package net.rptools.maptool.client.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public class NoteFrame extends JFrame {
	private JTextComponent noteArea;
	private JButton clearButton;
	private JButton closeButton;

	public NoteFrame() {
		setPreferredSize(new Dimension(300, 300));
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, new JScrollPane(getNoteArea()));
		add(BorderLayout.SOUTH, createButtonBar());
	}

	private JTextComponent getNoteArea() {
		if (noteArea == null) {
			noteArea = new JTextArea();
			noteArea.setBorder(BorderFactory.createLineBorder(Color.black));
		}
		return noteArea;
	}

	private JPanel createButtonBar() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		panel.add(BorderLayout.WEST, getClearButton());
		panel.add(BorderLayout.EAST, getCloseButton());
		return panel;
	}

	private JButton getClearButton() {
		if (clearButton == null) {
			clearButton = new JButton("Clear");
			clearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getNoteArea().setText("");
				}
			});
		}
		return clearButton;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					processWindowEvent(new WindowEvent(NoteFrame.this, WindowEvent.WINDOW_CLOSING));
				}
			});
		}
		return closeButton;
	}

	public synchronized void addText(String text) {
		getNoteArea().setText(getNoteArea().getText() + text + "\n");
		getNoteArea().setCaretPosition(getNoteArea().getText().length());
	}
}
