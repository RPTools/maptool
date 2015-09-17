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

import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.language.I18N;

public class ConnectionStatusPanel extends JPanel {
	public enum Status {
		connected, disconnected, server
	}

	public static Icon disconnectedIcon;
	public static Icon connectedIcon;
	public static Icon serverIcon;

	private final JLabel iconLabel = new JLabel();

	static {
		try {
			disconnectedIcon = new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/computer_off.png")); //$NON-NLS-1$
			connectedIcon = new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/computer_on.png")); //$NON-NLS-1$
			serverIcon = new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/computer_server.png")); //$NON-NLS-1$
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public ConnectionStatusPanel() {
		setLayout(new GridLayout(1, 1));
		setStatus(Status.disconnected);
		add(iconLabel);
	}

	public void setStatus(Status status) {
		Icon icon = null;
		String tip = null;
		switch (status) {
		case connected:
			icon = connectedIcon;
			tip = "ConnectionStatusPanel.serverConnected"; //$NON-NLS-1$
			break;
		case server:
			icon = serverIcon;
			tip = "ConnectionStatusPanel.runningServer"; //$NON-NLS-1$
			break;
		default:
			icon = disconnectedIcon;
			tip = "ConnectionStatusPanel.notConnected"; //$NON-NLS-1$
		}
		iconLabel.setIcon(icon);
		setToolTipText(I18N.getString(tip));
	}
}
