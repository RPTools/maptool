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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 * Extension that provides the ability to show/hide one of the panels
 * in the split pane
 */
@SuppressWarnings("serial")
public class JSplitPaneEx extends JSplitPane {

	private int lastVisibleSize;

	private static final int HORIZONTAL = 0;
	private static final int VERITICAL = 1;

	private static final int LEFT = 0;
	private static final int RIGHT = 1;

	private boolean isEitherHidden;

	private int dividerSize;

	public JSplitPaneEx() {
		setContinuousLayout(true);
	}

	public void setInitialDividerPosition(int position) {
		lastVisibleSize = position;
		setDividerLocation(position);
	}

	public int getDividerLocation() {
		return getDividerSize() > 0 ? super.getDividerLocation() : lastVisibleSize;
	}

	public boolean isTopHidden() {
		return !getTopComponent().isVisible();
	}

	public boolean isLeftHidden() {
		return !getLeftComponent().isVisible();
	}

	public boolean isBottomHidden() {
		return !getBottomComponent().isVisible();
	}

	public boolean isRightHidden() {
		return !getRightComponent().isVisible();
	}

	public void hideLeft() {
		hideInternal((JComponent) getLeftComponent(), LEFT, HORIZONTAL);
	}

	public void hideTop() {
		hideInternal((JComponent) getLeftComponent(), LEFT, VERITICAL);
	}

	public void showLeft() {
		showInternal((JComponent) getLeftComponent(), LEFT, HORIZONTAL);
	}

	public void showTop() {
		showInternal((JComponent) getLeftComponent(), LEFT, VERITICAL);
	}

	public void hideRight() {
		hideInternal((JComponent) getRightComponent(), RIGHT, HORIZONTAL);
	}

	public void hideBottom() {
		hideInternal((JComponent) getRightComponent(), RIGHT, VERITICAL);
	}

	public void showRight() {
		showInternal((JComponent) getRightComponent(), RIGHT, HORIZONTAL);
	}

	public void showBottom() {
		showInternal((JComponent) getRightComponent(), RIGHT, VERITICAL);
	}

	public int getLastVisibleSize() {
		return lastVisibleSize;
	}

	private synchronized void hideInternal(JComponent component, int which, int orientation) {
		if (isEitherHidden) {
			return;
		}

		Dimension componentSize = component.getSize();
		Dimension mySize = getSize();

		lastVisibleSize = orientation == HORIZONTAL ? componentSize.width : componentSize.height;
		if (which == RIGHT) {
			lastVisibleSize = (orientation == HORIZONTAL ? mySize.width : mySize.height) - lastVisibleSize - getDividerSize();
		}
		component.setVisible(false);
		dividerSize = getDividerSize();

		setDividerSize(0);

		isEitherHidden = true;
	}

	private synchronized void showInternal(JComponent component, int which, int orientation) {
		if (!isEitherHidden) {
			return;
		}

		if (lastVisibleSize == 0) {

			Dimension mySize = getSize();
			Dimension preferredSize = component.getSize();

			if (preferredSize.width == 0 && preferredSize.height == 0) {
				preferredSize = component.getMinimumSize();
			}

			lastVisibleSize = orientation == HORIZONTAL ? preferredSize.width : preferredSize.height;
			if (which == RIGHT) {
				lastVisibleSize = (orientation == HORIZONTAL ? mySize.width : mySize.height) - lastVisibleSize;
			}
		}
		setDividerSize(dividerSize);
		setDividerLocation(lastVisibleSize);

		component.setVisible(true);
		isEitherHidden = false;
	}

}
