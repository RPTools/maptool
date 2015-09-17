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

package net.rptools.maptool.client.script.api;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.script.api.proxy.TokenProxy;
import net.rptools.maptool.model.Token;

public class TokenApi {
	public TokenProxy current() {
		Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());

		return new TokenProxy(token);
	}

	//    public TokenProxy find() {
	//        return null;
	//    }
}
