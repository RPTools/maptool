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

package net.rptools.maptool.client.macro.impl;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.macro.Macro;

public abstract class AbstractMacro implements Macro {
	protected String processText(String incoming) {
		return "\002" + MapTool.getFrame().getCommandPanel().getChatProcessor().process(incoming) + "\003";
	}

	//	public static void main(String[] args) {
	//		new AbstractMacro(){
	//			public void execute(String macro) {
	//
	//				System.out.println(getWords(macro));
	//			}
	//		}.execute("one \"two three\" \"four five\"");
	//	}
}
