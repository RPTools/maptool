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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
/*
 * $Id: TaskPanelGroup.java 5381 2010-09-07 17:17:26Z azhrei_fje $
 *
 * Copyright (C) 2005, Digital Motorworks LP, a wholly owned subsidiary of ADP.
 * The contents of this file are protected under the copyright laws of the
 * United States of America with all rights reserved. This document is
 * confidential and contains proprietary information. Any unauthorized use or
 * disclosure is expressly prohibited.
 */

@SuppressWarnings("serial")
public class TaskPanelGroup extends JPanel {

	private int gap = 0;

	public static final String TASK_PANEL_LIST = "taskPanelGroup.panelList";

	private List<TaskPanel> taskPanelList = new ArrayList<TaskPanel>();

	public TaskPanelGroup() {
		this(0);
	}

	public TaskPanelGroup(int gap) {
		this.gap = gap;
		setLayout(new TaskPanelGroupLayout());
	}

	public TaskPanel getTaskPanel(String title) {

		for (TaskPanel taskPanel : taskPanelList) {
			if (title.equals(taskPanel.getTitle())) {
				return taskPanel;
			}
		}

		return null;
	}

	@Override
	public Component add(String name, Component comp) {
		add(comp, name);
		return comp;
	}

	@Override
	public void add(Component comp, Object title) {
		if (!(title instanceof String)) {
			// LATER: Title should be able to handle any component
			throw new IllegalArgumentException("Must supply a string title");
		}

		TaskPanel taskPanel = new TaskPanel((String) title, comp);
		firePropertyChange(TASK_PANEL_LIST, null, taskPanel);

		taskPanelList.add(taskPanel);
		add(taskPanel);
	}

	public List<TaskPanel> getTaskPanels() {
		return Collections.unmodifiableList(taskPanelList);
	}

	private class TaskPanelGroupLayout implements LayoutManager {

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {

			Dimension size = getSize();
			Insets insets = getInsets();

			int heightRemaining = size.height - insets.bottom - insets.top;
			int openPanelCount = 0;

			TaskPanel lastPanel = null;
			for (Component comp : getComponents()) {
				if (!(comp instanceof TaskPanel)) {
					continue;
				}

				TaskPanel panel = (TaskPanel) comp;

				if (!panel.isOpen()) {
					heightRemaining -= panel.getPreferredSize().height;
				} else {
					openPanelCount++;
					heightRemaining -= gap;
				}

				lastPanel = panel;
			}

			// Add back the last gap
			if (lastPanel != null && lastPanel.isOpen()) {
				heightRemaining += gap;
			}

			int openSize = openPanelCount > 0 ? heightRemaining / openPanelCount : 0;
			int currentHeight = insets.top;

			for (Component comp : getComponents()) {
				if (!(comp instanceof TaskPanel)) {
					continue;
				}

				TaskPanel panel = (TaskPanel) comp;
				int height = panel.isOpen() ? openSize : panel.getPreferredSize().height;
				panel.setSize(size.width - insets.left - insets.right, height);
				panel.setLocation(insets.left, currentHeight);

				currentHeight += height;
				if (panel.isOpen()) {
					currentHeight += gap;
				}
			}
		}

		public Dimension minimumLayoutSize(Container parent) {

			int width = 0;
			int height = 0;

			for (Component comp : getComponents()) {

				Dimension size = comp.getMinimumSize();
				if (size.width > width) {
					width = size.width;
				}

				height += size.height;
			}

			return new Dimension(width, height);
		}

		public Dimension preferredLayoutSize(Container parent) {
			int width = 0;
			int height = 0;

			for (Component comp : getComponents()) {

				Dimension size = comp.getPreferredSize();
				if (size.width > width) {
					width = size.width;
				}

				height += size.height;
			}

			return new Dimension(width, height);
		}

		public void removeLayoutComponent(Component comp) {
		}
	}

	public static void main(String[] args) {

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TaskPanelGroup group = new TaskPanelGroup(5);
		group.add(new TaskPanel("Testing", new JButton("Hello World")));
		group.add(Box.createHorizontalGlue());
		group.add(new TaskPanel("Testing 2", new JButton("Hello World2")));
		group.add(Box.createHorizontalGlue());
		group.add(new TaskPanel("Testing 2", new JButton("Hello World2")));
		group.add(Box.createHorizontalGlue());

		((JComponent) f.getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		f.add(group);
		f.pack();
		SwingUtil.centerOnScreen(f);

		f.setVisible(true);

	}
}
