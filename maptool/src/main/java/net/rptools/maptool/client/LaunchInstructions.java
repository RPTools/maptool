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

package net.rptools.maptool.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LaunchInstructions {

	private static final String USAGE = "<html><body width=\"400\">You are running MapTool with insufficient memory allocated (%dMB).<br><br>" +
			"You may experience odd behavior, especially when connecting to or hosting a server.<br><br>  " +
			"MapTool will launch anyway, but it is recommended that you use one of the 'Launch' scripts instead.</body></html>";

	public static void main(String[] args) {
		long mem = Runtime.getRuntime().maxMemory();
		String msg = new String(String.format(USAGE, mem / (1024 * 1024)));

		/* 
		 * Asking for 256MB via the -Xmx256M switch doesn't guarantee that the amount 
		 * maxMemory() reports will be 256MB.  The actual amount seems to vary from PC to PC.  
		 * 200MB seems to be a safe value for now.  <Phergus>
		 */
		if (mem < 200 * 1024 * 1024) {
			JOptionPane.showMessageDialog(new JFrame(), msg, "Usage", JOptionPane.INFORMATION_MESSAGE);
		}
		MapTool.main(args);
	}
}
