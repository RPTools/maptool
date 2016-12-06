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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GradientPanel extends JPanel {

	private Color c1;
	private Color c2;

	public GradientPanel(Color c1, Color c2) {
		this(c1, c2, new FlowLayout());
	}

	public GradientPanel(Color c1, Color c2, LayoutManager layout) {
		super(layout);

		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		Paint p = new GradientPaint(0, 0, c1, getSize().width, 0, c2);
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(p);
		g2d.fillRect(0, 0, getSize().width, getSize().height);
		g2d.setPaint(oldPaint);
	}
}
