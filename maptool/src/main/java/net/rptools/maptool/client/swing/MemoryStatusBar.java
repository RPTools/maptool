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
import java.awt.event.MouseAdapter;
import java.text.DecimalFormat;

import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;

import net.rptools.maptool.util.FileUtil;

/**
 */
public class MemoryStatusBar extends JProgressBar {
	private static final long serialVersionUID = 1L;

	private static final Dimension minSize = new Dimension(100, 10);
	private static final DecimalFormat format = new DecimalFormat("#,##0.#");
	private static long largestMemoryUsed = -1;
	private static MemoryStatusBar msb = null;

	public static MemoryStatusBar getInstance() {
		if (msb == null)
			msb = new MemoryStatusBar();
		return msb;
	}

	private MemoryStatusBar() {
		setMinimum(0);
		setStringPainted(true);

		new Thread() {
			@Override
			public void run() {
				while (true) {
					update();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						break;
					}
				}
			}
		}.start();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				System.gc();
				update();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return minSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	public double getLargestMemoryUsed() {
		return largestMemoryUsed;
	}

	private void update() {
		//double totalMegs = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		//double freeMegs = Runtime.getRuntime().freeMemory() / (1024 * 1024);

		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();

		if (totalMemory > largestMemoryUsed)
			largestMemoryUsed = totalMemory;

		setMaximum((int) (totalMemory / (1024 * 1024)));
		setValue((int) ((totalMemory - freeMemory) / (1024 * 1024)));
		//setString(format.format(totalMegs - freeMegs) + "M/" + format.format(totalMegs) + "M");

		setString(FileUtil.byteCountToDisplaySize((totalMemory - freeMemory)) + "/" + FileUtil.byteCountToDisplaySize(totalMemory));

		setToolTipText("Used Memory: " + format.format((totalMemory - freeMemory) / (1024 * 1024))
				+ "M, Total Memory: " + format.format(totalMemory / (1024 * 1024))
				+ "M, Maximum Memory: " + format.format(maxMemory / (1024 * 1024)) + "M");
	}
}
