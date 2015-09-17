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

import java.util.LinkedList;
import java.util.List;

public class ChatTranslationRuleGroup {
	private final String name;
	private final List<ChatTranslationRule> translationRuleList = new LinkedList<ChatTranslationRule>();

	public ChatTranslationRuleGroup(String name) {
		this(name, null);
	}

	public ChatTranslationRuleGroup(String name, List<ChatTranslationRule> translationRuleList) {
		this.name = name;
		if (translationRuleList != null) {
			this.translationRuleList.addAll(translationRuleList);
		}
	}

	public void addRule(ChatTranslationRule rule) {
		translationRuleList.add(rule);
	}

	public boolean isEnabled() {
		return true;
	}

	public String getName() {
		return name;
	}

	public String translate(String incoming) {
		if (incoming == null) {
			return null;
		}
		for (ChatTranslationRule rule : translationRuleList) {
			incoming = rule.translate(incoming);
		}
		return incoming;
	}
}
