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

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.server.MapToolServer;

import org.apache.log4j.Logger;

import com.jeta.forms.components.panel.FormPanel;

public class ConnectionInfoDialog extends JDialog {
	private static final Logger log = Logger.getLogger(ConnectionInfoDialog.class);
	private final JTextField externalAddressLabel;

	/**
	 * This is the default constructor
	 */
	public ConnectionInfoDialog(MapToolServer server) {
		super(MapTool.getFrame(), "Server Info", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(275, 200);

		FormPanel panel = new FormPanel("net/rptools/maptool/client/ui/forms/connectionInfoDialog.xml");

		JTextField nameLabel = panel.getTextField("name");
		JTextField localAddressLabel = panel.getTextField("localAddress");
		JTextField portLabel = panel.getTextField("port");
		externalAddressLabel = panel.getTextField("externalAddress");

		String name = server.getConfig().getServerName();
		if (name == null) {
			name = "---";
		}
		String localAddress = "Unknown";
		try {
			InetAddress rptools = InetAddress.getByName("www.rptools.net");
			try {
				InetAddress localAddy = InetAddress.getLocalHost();
				localAddress = localAddy.getHostAddress();
			} catch (IOException e) { // Socket|UnknownHost
				log.warn("Can't resolve 'www.rptools.net' or our own IP address!?", e);
			}
		} catch (UnknownHostException e) {
			log.warn("Can't resolve 'www.rptools.net' or our own IP address!?", e);
		}
		String externalAddress = "Discovering ...";
		String port = MapTool.isPersonalServer() ? "---" : Integer.toString(server.getConfig().getPort());

		nameLabel.setText(name);
		localAddressLabel.setText(localAddress);
		externalAddressLabel.setText(externalAddress);
		portLabel.setText(port);

		JButton okButton = (JButton) panel.getButton("okButton");
		bindOKButtonActions(okButton);

		setLayout(new GridLayout());
		((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(panel);

		new Thread(new ExternalAddressFinder()).start();
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			SwingUtil.centerOver(this, MapTool.getFrame());
		}
		super.setVisible(b);
	}

	/**
	 * This method initializes okButton
	 * 
	 * @return javax.swing.JButton
	 */
	private void bindOKButtonActions(JButton okButton) {
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setVisible(false);
			}
		});
	}

	private class ExternalAddressFinder implements Runnable {
		public void run() {
			String address = "Unknown";
			try {
				address = MapToolRegistry.getAddress();
			} catch (Exception e) {
				// Oh well, might not be connected
			}
			final String addy = address;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					externalAddressLabel.setText(addy);
				}
			});
		}
	}
}
