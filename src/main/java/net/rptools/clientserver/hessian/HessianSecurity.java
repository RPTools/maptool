package net.rptools.clientserver.hessian;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HessianSecurity {
    private final Set<String> allowed;
    private final Set<String> denied;


    public HessianSecurity() {

        Set<String> allow = new HashSet<>();
        // Safe java.lang stuff
        allow.add("java.lang.Boolean");
        allow.add("java.lang.Byte");
        allow.add("java.lang.Character");
        allow.add("java.lang.Double");
        allow.add("java.lang.Float");
        allow.add("java.lang.Long");
        allow.add("java.lang.Short");
        allow.add("java.lang.String");


        allow.add("java.awt.geom.*");
        allow.add("sun.awt.geom.*");
        allow.add("java.awt.BasicStroke");

        allow.add("net.rptools.maptool.client.walker.*");
        allow.add("net.rptools.maptool.common.*");
        allow.add("net.rptools.maptool.model.*");


        allow.add("net.rptools.lib.MD5Key");

        Set<String> deny = new HashSet<>();
        deny.add("*");

        allowed = Collections.unmodifiableSet(allow);

        denied = Collections.unmodifiableSet(deny);
    }

    public Collection<String> getAllowed() {
        return allowed;
    }

    public Collection<String> getDenied() {
        return denied;
    }
}
