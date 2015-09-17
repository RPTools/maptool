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

package net.rptools.maptool.client.ui.htmlframe;

import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

import net.rptools.maptool.client.swing.MessagePanelEditorKit;

@SuppressWarnings("serial")
class HTMLPaneEditorKit extends MessagePanelEditorKit {
	private final HTMLPaneViewFactory viewFactory;

	HTMLPaneEditorKit(HTMLPane htmlPane) {
		setUseMacroLinkToolTips(false);
		viewFactory = new HTMLPaneViewFactory(super.getViewFactory(), htmlPane);
	}

	@Override
	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	@Override
	public HTMLEditorKit.Parser getParser() {
		return super.getParser();
	}
}
