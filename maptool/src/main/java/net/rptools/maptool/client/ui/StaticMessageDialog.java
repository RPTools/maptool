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

package net.rptools.maptool.client.ui;

public class StaticMessageDialog extends MessageDialog {
	private static final long serialVersionUID = 3101164410637883204L;

	private String status;

	public StaticMessageDialog(String status) {
		this.status = status;
	}

	@Override
	protected String getStatus() {
		return status;
	}

	/**
	 * Doesn't work right as it forces a repaint of the GlassPane object which takes a snapshot of the RootPane and then
	 * adds the 'status' message as an overlay. The problem is that the RootPane snapshot includes the previous image
	 * that might have been displayed previously.
	 * 
	 * @param s
	 */
	public void setStatus(String s) {
		this.status = s;
		revalidate();
		repaint();
	}
}
