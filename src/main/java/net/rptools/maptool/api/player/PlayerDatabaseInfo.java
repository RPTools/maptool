package net.rptools.maptool.api.player;

import net.rptools.maptool.api.ApiData;

public record PlayerDatabaseInfo(boolean supportsBlocking, boolean supportsIndividualPasswords,
                                 boolean supportsAsymmetricalKeys) implements ApiData {
}
