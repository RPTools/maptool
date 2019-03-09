package de.jadebringer.maptool.framework;

import java.util.Collection;

import net.rptools.parser.function.Function;

public interface Framework {

	Collection<? extends Function> getFunctions();
}
