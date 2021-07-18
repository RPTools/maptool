package net.rptools.maptool.model.player;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.util.CipherUtil;

public class PersonalServerPlayerDatabase implements PlayerDatabase {

  private final LocalPlayer player;

   public PersonalServerPlayerDatabase() throws NoSuchAlgorithmException, InvalidKeySpecException {
     player = new LocalPlayer(
         AppPreferences.getDefaultUserName(),
         Role.GM,
         ServerConfig.getPersonalServerGMPassword()
     );
  }

  @Override
  public boolean playerExists(String playerName) {
    return true; // Player always exists no matter what the name
  }

  @Override
  public Player getPlayer(String playerName)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    return player;
  }

  @Override
  public Optional<CipherUtil.Key> getPlayerPassword(String playerName) {
    return Optional.of(player.getPassword());
  }

  @Override
  public byte[] getPlayerPasswordSalt(String playerName) {
     return player.getPassword().salt();
  }

  @Override
  public boolean supportsDisabling() {
    return false;
  }

  @Override
  public boolean supportsPlayTimes() {
    return false;
  }

  @Override
  public void setPlayTimes(Player player, Collection<PlayTime> times) throws PasswordDatabaseException {
    throw new PasswordDatabaseException("msg.err.passFile.cantSetPlayTimes");
  }

  @Override
  public Set<PlayTime> getPlayTimes(Player player) {
    return ANY_TIME;
  }

  @Override
  public String getDisabledReason(Player player) {
    return "";
  }

  @Override
  public boolean isDisabled(Player player) {
    return false;
  }

  @Override
  public void disablePlayer(Player player, String reason) throws PasswordDatabaseException {
    throw new PasswordDatabaseException("msg.err.passFile.cantDisablePlayer");
  }

  @Override
  public Optional<CipherUtil.Key> getRolePassword(Player.Role role) {
    if (role == Role.GM) {
       return Optional.of(player.getPassword());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Player getPlayerWithRole(String playerName, Player.Role role)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    return player; // There is no non GM personal server player so just return the GM
  }
}
