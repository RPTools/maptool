package net.rptools.maptool.model.player;

import java.util.Set;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;

public record PlayerInfo(
    String name,
    Role role,
    boolean blocked,
    String blockedReason,
    boolean connected,
    AuthMethod authMethod,
    Set<String> publicKeys,
    boolean persistent
)  { }
