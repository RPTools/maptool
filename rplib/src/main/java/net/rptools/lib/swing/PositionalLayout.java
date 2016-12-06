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
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 */
public class PositionalLayout implements LayoutManager2 {

	public enum Position {

		NW, N, NE, W, CENTER, E, SW, S, SE
	}

	private int padding = 0;

	private Map<Component, Position> compPositionMap = new HashMap<Component, Position>();

	public PositionalLayout() {
	}

	public PositionalLayout(int edgePadding) {
		padding = edgePadding;
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		if (!(constraints instanceof Position)) {
			return;
		}

		compPositionMap.put(comp, (Position) constraints);
	}

	public void addLayoutComponent(String name, Component comp) {
		throw new IllegalArgumentException("Use add(comp, Position)");
	}

	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	public void invalidateLayout(Container target) {
		// Nothing to do right now
	}

	public void layoutContainer(Container parent) {

		Dimension size = parent.getSize();

		Component[] compArray = parent.getComponents();
		for (Component comp : compArray) {

			Position pos = compPositionMap.get(comp);
			Dimension compSize = comp.getSize();

			int x = 0;
			int y = 0;

			switch (pos) {
			case NW: {
				x = padding;
				y = padding;
				break;
			}
			case N: {
				x = center(size.width, compSize.width);
				y = padding;
				break;
			}
			case NE: {
				x = size.width - compSize.width - padding;
				y = padding;
				break;
			}
			case W: {
				x = padding;
				y = center(size.height, compSize.height);
				break;
			}
			case E: {
				x = size.width - compSize.width - padding;
				y = center(size.height, compSize.height);
				break;
			}
			case SW: {
				x = padding;
				y = size.height - compSize.height - padding;
				break;
			}
			case S: {
				x = center(size.width, compSize.width);
				y = size.height - compSize.height - padding;
				break;
			}
			case SE: {
				x = size.width - compSize.width - padding;
				y = size.height - compSize.height - padding;
				break;
			}
			case CENTER: {
				x = 0;
				y = 0;

				// Fill available space
				comp.setSize(size);
			}
			}

			comp.setLocation(x, y);
		}
	}

	private int center(int outsideWidth, int insideWidth) {
		return (outsideWidth - insideWidth) / 2;
	}

	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(0, 0);
	}

	public void removeLayoutComponent(Component comp) {

		compPositionMap.remove(comp);
	}

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new PositionalPanel();

		panel.add(createButton("NW"), Position.NW);
		panel.add(createButton("N"), Position.N);
		panel.add(createButton("NE"), Position.NE);
		panel.add(createButton("W"), Position.W);
		panel.add(createButton("E"), Position.E);
		panel.add(createButton("SW"), Position.SW);
		panel.add(createButton("S"), Position.S);
		panel.add(createButton("SE"), Position.SE);
		panel.add(createButton("CENTER"), Position.CENTER);

		frame.setContentPane(panel);

		frame.setSize(200, 200);
		frame.setVisible(true);
	}

	private static JButton createButton(String label) {
		JButton button = new JButton(label);
		button.setSize(button.getMinimumSize());

		return button;
	}
}
