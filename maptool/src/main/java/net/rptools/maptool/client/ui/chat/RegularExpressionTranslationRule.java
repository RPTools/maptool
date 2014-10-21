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

package net.rptools.maptool.client.ui.chat;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RegularExpressionTranslationRule extends AbstractChatTranslationRule {
	private static final Logger log = Logger.getLogger(RegularExpressionTranslationRule.class);
	private Pattern pattern;
	private final String replaceWith;

	public RegularExpressionTranslationRule(String pattern, String replaceWith) {
		try {
			this.pattern = Pattern.compile(pattern);
		} catch (Exception e) {
			log.error("Could not parse regex: " + pattern, e);
		}
		this.replaceWith = replaceWith;
	}

	public String translate(String incoming) {
		if (pattern == null) {
			return incoming;
		}
		return pattern.matcher(incoming).replaceAll(replaceWith);
	}
}
