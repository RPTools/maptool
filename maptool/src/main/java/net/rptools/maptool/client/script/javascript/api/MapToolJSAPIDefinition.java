package net.rptools.maptool.client.script.javascript.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.TYPE)
public @interface MapToolJSAPIDefinition {
    String javaScriptVariableName();
}
