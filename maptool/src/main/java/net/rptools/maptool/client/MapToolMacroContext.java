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

public class MapToolMacroContext {
	/** The name of the macro being executed. */
	private final String name;

	/** Where the macro comes from. */
	private final String source;

	/** Is the macro trusted or not. */
	private final boolean trusted;

	/** The index of the button that was clicked on to fire of this macro*/
	private int macroButtonIndex;

	/**
	 * Creates a new Macro Context.
	 * @param name The name of the macro.
	 * @param source The source location of the macro.
	 * @param trusted Is the macro trusted or not.
	 */
	public MapToolMacroContext(String name, String source, boolean trusted) {
		this(name, source, trusted, -1);
	}

	/**
	 * Creates a new Macro Context.
	 * @param name The name of the macro.
	 * @param source The source location of the macro.
	 * @param trusted Is the macro trusted or not.
	 * @param macroButtonIndex The index of the button that ran this command.
	 */
	public MapToolMacroContext(String name, String source, boolean trusted, int macroButtonIndex) {
		this.name = name;
		this.source = source;
		this.trusted = trusted;
		this.macroButtonIndex = macroButtonIndex;
	}

	/**
	 * Gets the name of the macro context.
	 * @return the name of the macro context.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the source location of the macro context.
	 * @return the source location of the macro context.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Gets if the macro context is trusted or not.
	 * @return if the macro context is trusted or not.
	 */
	public boolean isTrusted() {
		return trusted;
	}

	/**
	 * Gets the index of the macro button that this macro is in
	 * @return the index of the macro button.
	 */
	public int getMacroButtonIndex() {
		return macroButtonIndex;
	}
}
