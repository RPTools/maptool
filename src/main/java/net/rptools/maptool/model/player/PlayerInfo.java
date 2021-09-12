package net.rptools.maptool.model.player;

import net.rptools.maptool.model.player.Player.Role;

public record PlayerInfo(
    String name,
    Role role,
    boolean blocked,
    String blockedReason,
    boolean connected
)  {
}
