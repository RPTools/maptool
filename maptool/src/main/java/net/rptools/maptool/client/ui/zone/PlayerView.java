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

package net.rptools.maptool.client.ui.zone;

import java.util.List;

import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Token;

public class PlayerView {
	private final Player.Role role;
	private final List<Token> tokens; // Optional

	// Optimization
	private final String hash;

	public PlayerView(Player.Role role) {
		this(role, null);
	}

	public PlayerView(Player.Role role, List<Token> tokens) {
		this.role = role;
		this.tokens = tokens != null && !tokens.isEmpty() ? tokens : null;
		hash = calculateHashcode();
	}

	public Player.Role getRole() {
		return role;
	}

	public boolean isGMView() {
		return role == Player.Role.GM;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public boolean isUsingTokenView() {
		return tokens != null;
	}

	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PlayerView)) {
			return false;
		}
		PlayerView other = (PlayerView) obj;
		return hash.equals(other.hash);
	}

	private String calculateHashcode() {
		StringBuilder builder = new StringBuilder();
		builder.append(role);
		if (tokens != null) {
			for (Token token : tokens) {
				builder.append(token.getId());
			}
		}
		return builder.toString();
	}
}
