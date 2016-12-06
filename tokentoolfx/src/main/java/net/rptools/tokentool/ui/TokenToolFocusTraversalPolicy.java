package net.rptools.tokentool.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

import net.rptools.tokentool.TokenTool;

public class TokenToolFocusTraversalPolicy extends FocusTraversalPolicy {

	@Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		return TokenTool.getFrame().getTokenCompositionPanel();
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		return TokenTool.getFrame().getTokenCompositionPanel();
	}

	@Override
	public Component getFirstComponent(Container aContainer) {
		return TokenTool.getFrame().getTokenCompositionPanel();
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		return TokenTool.getFrame().getTokenCompositionPanel();
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		return TokenTool.getFrame().getTokenCompositionPanel();
	}

}
