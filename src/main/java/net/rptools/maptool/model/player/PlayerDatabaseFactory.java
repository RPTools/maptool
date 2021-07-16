package net.rptools.maptool.model.player;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.server.ServerConfig;

public class PlayerDatabaseFactory {

  public enum PlayerDatabaseType {
    LOCAL_PLAYER,
    DEFAULT,
    PASSWORD_FILE
  }


  private static final Map<PlayerDatabaseType, PlayerDatabase> playerDatabaseMap =
      new ConcurrentHashMap<>();

  private static final ReentrantLock lock = new ReentrantLock();

  private static final File PASSWORD_FILE =
      AppUtil.getAppHome("config").toPath().resolve("passwords.json").toFile();
  private static final File PASSWORD_ADDITION_FILE =
      AppUtil.getAppHome("config").toPath().resolve("passwords_add.json").toFile();
  private static ServerConfig serverConfig;



  public static void setServerConfig(ServerConfig config) {
    try {
      lock.lock();
      serverConfig = config;
    } finally {
      lock.unlock();
    }
  }

  private static ServerConfig getServerConfig() {
    try {
      lock.lock();
      return serverConfig;
    } finally {
      lock.unlock();
    }
  }

  public static PlayerDatabase getPlayerDatabase(PlayerDatabaseType databaseType) {
      switch (databaseType) {
        case LOCAL_PLAYER:
        case PASSWORD_FILE:
          return playerDatabaseMap.computeIfAbsent(databaseType,
              PlayerDatabaseFactory::createPlayerDatabase);
        default:
          return createPlayerDatabase(databaseType);
      }
  }


  private static PlayerDatabase createPlayerDatabase(PlayerDatabaseType databaseType) {
    try {
      switch (databaseType) {
        case LOCAL_PLAYER:
          return new LocalPlayerDatabase();
        case PASSWORD_FILE:
          return new PasswordFilePlayerDatabase(PASSWORD_FILE, PASSWORD_ADDITION_FILE);
        default:
          ServerConfig config = getServerConfig();
          return new DefaultPlayerDatabase(
              config.getPlayerPassword(),
              config.getGmPassword()
          );
      }
    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException(e);
    }
  }

}
