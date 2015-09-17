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

import java.awt.Dimension;

import javax.swing.JProgressBar;

/**
 */
public class ProgressStatusBar extends JProgressBar {

	private static final Dimension minSize = new Dimension(75, 10);

	int indeterminateCount = 0;
	int determinateCount = 0;
	int totalWork = 0;
	int currentWork = 0;

	public ProgressStatusBar() {
		setMinimum(0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize() {
		return minSize;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	public void startIndeterminate() {
		indeterminateCount++;
		setIndeterminate(true);
	}

	public void endIndeterminate() {
		indeterminateCount--;
		if (indeterminateCount < 1) {
			setIndeterminate(false);

			indeterminateCount = 0;
		}
	}

	public void startDeterminate(int totalWork) {
		determinateCount++;
		this.totalWork += totalWork;

		setMaximum(this.totalWork);
	}

	public void updateDeterminateProgress(int additionalWorkCompleted) {
		currentWork += additionalWorkCompleted;
		setValue(currentWork);
	}

	public void endDeterminate() {
		determinateCount--;
		if (determinateCount == 0) {
			totalWork = 0;
			currentWork = 0;

			setMaximum(0);
			setValue(0);
		}
	}

}
