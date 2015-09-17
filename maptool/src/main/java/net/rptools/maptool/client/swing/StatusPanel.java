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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * @author trevor
 */
public class StatusPanel extends JPanel {

	private JLabel statusLabel;

	public StatusPanel() {

		statusLabel = new JLabel();

		setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;

		add(wrap(statusLabel), constraints);
	}

	public void setStatus(String status) {
		statusLabel.setText(status);
	}

	public void addPanel(JComponent component) {

		int nextPos = getComponentCount();

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.BOTH;

		constraints.gridx = nextPos;

		add(wrap(component), constraints);

		invalidate();
		doLayout();
	}

	private JComponent wrap(JComponent component) {

		component.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		return component;
	}
}
