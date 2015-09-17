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

package net.rptools.maptool.client.swing;

import java.awt.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.functions.MacroLinkFunction;

public class TooltipView extends InlineView {

	private boolean mlToolTips;

	/**
	 * Constructs a new view wrapped on an element.
	 *
	 * @param elem the element
	 */
	public TooltipView(Element elem, boolean macroLinkToolTips) {
		super(elem);
		mlToolTips = macroLinkToolTips;
	}

	@Override
	public String getToolTipText(float x, float y, Shape allocation) {
		AttributeSet att;

		att = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.A);
		if (att != null) {
			String href = att.getAttribute(HTML.Attribute.HREF).toString();
			if (href.startsWith("macro:")) {
				boolean isInsideChat = mlToolTips;
				boolean allowToolTipToShow = !AppPreferences.getSuppressToolTipsForMacroLinks();
				if (isInsideChat && allowToolTipToShow) {
					return MacroLinkFunction.getInstance().macroLinkToolTip(href);
				}
				// if we are not displaying macro link tooltips let if fall through so that any span tooltips will be displayed
			} else {
				return href;
			}
		}

		att = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.SPAN);
		if (att != null)
			return (String) att.getAttribute(HTML.Attribute.TITLE);

		return null;
	}
}
