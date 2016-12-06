/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.rptools.lib.image.ImageUtil;
/*
 * $Id: TaskPanel.java 5381 2010-09-07 17:17:26Z azhrei_fje $
 *
 * Copyright (C) 2005, Digital Motorworks LP, a wholly owned subsidiary of ADP.
 * The contents of this file are protected under the copyright laws of the
 * United States of America with all rights reserved. This document is
 * confidential and contains proprietary information. Any unauthorized use or
 * disclosure is expressly prohibited.
 */

public class TaskPanel extends JPanel {

	private static final Color DEFAULT_TOP_COLOR = new Color(191, 197, 255);
	private static final Color DEFAULT_BOTTOM_COLOR = new Color(175, 185, 255);

	private Color topColor = DEFAULT_TOP_COLOR;
	private Color bottomColor = DEFAULT_BOTTOM_COLOR;

	public enum State {
		OPEN, CLOSED
	}

	public void setTopColor(Color color) {
		topColor = color;
		repaint();
	}

	public void setBottomColor(Color color) {
		bottomColor = color;
		repaint();
	}

	public static final String TASK_PANEL_STATE = "taskPanel.state";

	private static Icon closeIcon;
	private static Icon openIcon;

	static {
		try {
			closeIcon = new ImageIcon(ImageUtil.getImage("net/rptools/lib/swing/image/collapse.png"));
			openIcon = new ImageIcon(ImageUtil.getImage("net/rptools/lib/swing/image/expand.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private State state;
	private JPanel contentPanel;
	private JLabel toggleButton;
	private String title;

	public TaskPanel(String title, Component component) {

		this.title = title;

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, createTitlePanel(title));
		add(BorderLayout.CENTER, getContentPanel(component));

		setState(State.OPEN);
	}

	public String getTitle() {
		return title;
	}

	public void toggleState() {
		setState(state == State.OPEN ? State.CLOSED : State.OPEN);
	}

	public void setState(State state) {
		State oldState = this.state;
		this.state = state;
		contentPanel.setVisible(state == State.OPEN);
		toggleButton.setIcon(state != State.OPEN ? openIcon : closeIcon);

		firePropertyChange(TASK_PANEL_STATE, oldState, state);

		doLayout();
		invalidate();
		repaint();
	}

	public boolean isOpen() {
		return state == State.OPEN;
	}

	public State getState() {
		return state;
	}

	private JPanel getContentPanel(Component component) {

		if (contentPanel == null) {
			contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(BorderLayout.CENTER, component);
			//contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		}

		return contentPanel;
	}

	private JPanel createTitlePanel(String title) {

		//JPanel panel = new GradientPanel(new Color(0, 0, 100), Color.lightGray, new GridBagLayout()) {
		JPanel panel = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Dimension size = getSize();

				g.setColor(topColor);
				g.fillRect(0, 0, size.width, size.height / 2);

				//((Graphics2D)g).setPaint(new GradientPaint(0, size.height/2, TOP_COLOR, 0, size.height, BOTTOM_COLOR));
				g.setColor(bottomColor);
				g.fillRect(0, size.height / 2, size.width, size.height / 2);

				g.setColor(Color.gray);
				g.drawLine(0, 0, size.width, 0);
				g.drawLine(0, 0, 0, size.height);
				g.drawLine(size.width - 1, 0, size.width - 1, size.height);
				g.drawLine(0, size.height - 1, size.width - 1, size.height - 1);

			}
		};
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 1;
		panel.add(createButtonPanel(), constraints);

		constraints.gridx = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;

		JLabel titleLabel = new JLabel(title);
		panel.add(titleLabel, constraints);

		return panel;
	}

	private JPanel createButtonPanel() {

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.add(getToggleButton());

		return panel;
	}

	private JLabel getToggleButton() {
		if (toggleButton == null) {
			toggleButton = new JLabel(closeIcon);
			toggleButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					toggleState();
				}
			});
		}

		return toggleButton;
	}
}
