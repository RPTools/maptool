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

package net.rptools.maptool.client.ui.io;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author crash
 * 
 */
public class ResolveLocalHostname {
	/**
	 * Currently the parameter is unused. This routine there returns the ANY
	 * local address if it can, or the local host address if it can't. It
	 * presumes that ANY is actually "0.0.0.0" but if the underlying platform
	 * says it is, that's when it fallsback to using localhost.
	 * 
	 * @param intendedDestination
	 *            used to determine which NIC MapTool should bind to
	 * @return
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static InetAddress getLocalHost(InetAddress intendedDestination) throws UnknownHostException, SocketException {
		InetAddress inet = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 });
		if (inet.isAnyLocalAddress())
			return inet;
		inet = InetAddress.getLocalHost();
		return inet;
	}
}
