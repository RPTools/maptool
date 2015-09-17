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

import java.util.List;

public interface MapToolRegistryService {

	public static final int CODE_UNKNOWN = 0;
	public static final int CODE_OK = 1;
	public static final int CODE_COULD_CONNECT_BACK = 2;
	public static final int CODE_ID_IN_USE = 3;

	public int registerInstance(String id, int port, String version);

	public void unregisterInstance(int port);

	public String findInstance(String id);

	public List<String> findAllInstances();

	public boolean testConnection(int port);

	public void heartBeat(int port);

	public String getAddress();
}
