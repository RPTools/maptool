package net.rptools.maptool.model.player;

import net.rptools.maptool.api.ApiData;

public record PlayerDatabaseInfo(boolean supportsBlocking, boolean supportsIndividualPasswords,
                                 boolean supportsAsymmetricalKeys,
                                 boolean recordsOnlyConnectedPlayers) {
}
