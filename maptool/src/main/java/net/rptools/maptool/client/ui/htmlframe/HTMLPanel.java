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

package net.rptools.maptool.client.ui.htmlframe;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import net.rptools.maptool.client.swing.MessagePanelEditorKit;

public class HTMLPanel extends JPanel {
	private static final long serialVersionUID = -2574631956909778786L;

	private final HTMLPane pane = new HTMLPane();
	private final JPanel closePanel = new JPanel();

	/**
	 * Creates a new HTMLPanel.
	 * 
	 * @param container
	 *            The container that will hold the HTML panel.
	 * @param closeButton
	 *            If the panel has a close button.
	 * @param scrollBar
	 *            Should panel have scroll bars or not.
	 */
	HTMLPanel(final HTMLPanelContainer container, boolean closeButton, boolean scrollBar) {
		setLayout(new BorderLayout());

		JButton jcloseButton = new JButton("Close");
		jcloseButton.setActionCommand("Close");
		jcloseButton.addActionListener(container);
		closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
		closePanel.add(Box.createHorizontalGlue());
		closePanel.add(jcloseButton);
		closePanel.add(Box.createHorizontalGlue());

		if (scrollBar) {
			add(new JScrollPane(pane), BorderLayout.CENTER);
		} else {
			add(pane, BorderLayout.CENTER);
		}
		updateContents("", closeButton);

		// ESCAPE closes the window
		pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		pane.getActionMap().put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				container.closeRequest();
			}
		});
		// Add an action listener so we can get notified about form events.
		pane.addActionListener(container);
	}

	/**
	 * Update the contents of the panel.
	 * 
	 * @param html
	 *            The HTML to display.
	 * @param closeButton
	 *            If the panel has a close button.
	 */
	public void updateContents(final String html, boolean closeButton) {
		if (closeButton) {
			add(closePanel, BorderLayout.SOUTH);
		} else {
			remove(closePanel);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				((MessagePanelEditorKit) pane.getEditorKit()).flush();
				pane.setText(html);
				pane.setCaretPosition(0);
			}
		});
	}

	/**
	 * Flushes any caching for the panel.
	 */
	public void flush() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				((MessagePanelEditorKit) pane.getEditorKit()).flush();
			}
		});
	}

	/**
	 * Updates if this panel is an input panel or not.
	 * 
	 * @param input
	 *            is this panel has a close button or not.
	 */
	void updateContents(boolean closeButton) {
		if (closeButton) {
			add(closePanel, BorderLayout.SOUTH);
		} else {
			remove(closePanel);
		}
		revalidate();
	}
}
