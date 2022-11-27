/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.tokens;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.Zone.Event;
import net.rptools.maptool.model.zones.ZoneAdded;
import net.rptools.maptool.model.zones.ZoneRemoved;

/**
 * This class listens to the legacy token addition events and forwards them to the main MapTool
 * event bus.
 *
 * @see MapToolEventBus#getMainEventBus()
 */
public class TokenEventBusBridge {
  private final TokenModelChangeListener modelChangeListener = new TokenModelChangeListener();

  @Subscribe
  void onZoneAdded(ZoneAdded event) {
    final var zone = event.zone();
    addTokenChangeListener(zone);
    // Now we have fire off adding the tokens in the zone
    var tokens =
        zone.getTokens().stream()
            .map(token -> new TokenInfo(token.getName(), token.getId(), token))
            .collect(Collectors.toSet());
    new MapToolEventBus().getMainEventBus().post(new TokensAddedEvent(tokens));
  }

  @Subscribe
  void onZoneRemoved(ZoneRemoved event) {
    final var zone = event.zone();
    removeTokenChangeListener(zone);
    // Now we have fire off removing tokens that are in the zone
    var tokens =
        zone.getTokens().stream()
            .map(token -> new TokenInfo(token.getName(), token.getId(), token))
            .collect(Collectors.toSet());
    new MapToolEventBus().getMainEventBus().post(new TokensRemovedEvent(tokens));
  }

  private static class TokenModelChangeListener implements ModelChangeListener {

    @Override
    public void modelChanged(ModelChangeEvent event) {
      if (event.eventType == Zone.Event.TOKEN_ADDED) {
        SwingUtilities.invokeLater(
            () -> {
              List<Token> tokenList = new ArrayList<>();
              if (event.getArg() instanceof Token t) {
                tokenList.add(t);
              } else if (event.getArg() instanceof List<?> lt) {
                tokenList.addAll((List<Token>) lt);
              }
              var tokens =
                  tokenList.stream()
                      .map(token -> new TokenInfo(token.getName(), token.getId(), token))
                      .collect(Collectors.toSet());
              var added = new TokensAddedEvent(tokens);
              new MapToolEventBus().getMainEventBus().post(added);
            });
      } else if (event.eventType == Event.TOKEN_REMOVED) {
        SwingUtilities.invokeLater(
            () -> {
              List<Token> tokenList = new ArrayList<>();
              if (event.getArg() instanceof Token t) {
                tokenList.add(t);
              } else if (event.getArg() instanceof List<?> lt) {
                tokenList.addAll((List<Token>) lt);
              }
              var tokens =
                  tokenList.stream()
                      .map(token -> new TokenInfo(token.getName(), token.getId(), token))
                      .collect(Collectors.toSet());
              var removed = new TokensRemovedEvent(tokens);
              new MapToolEventBus().getMainEventBus().post(removed);
            });
      } else if (event.eventType == Event.TOKEN_CHANGED || event.eventType == Event.TOKEN_EDITED) {
        SwingUtilities.invokeLater(
            () -> {
              var tokens = new HashSet<TokenInfo>();
              if (event.getArg() instanceof Token t) {
                tokens.add(new TokenInfo(t.getName(), t.getId(), t));
              } else if (event.getArg() instanceof List<?> lt) {
                tokens.addAll(
                    lt.stream()
                        .map(token -> ((Token) token))
                        .map(token -> new TokenInfo(token.getName(), token.getId(), token))
                        .collect(Collectors.toSet()));
              }
              new MapToolEventBus().getMainEventBus().post(new TokensChangedEvent(tokens));
            });
      }
    }
  }

  /** The singleton instance of this class. */
  private static final TokenEventBusBridge instance = new TokenEventBusBridge();

  /** Has the bridge been initialized? */
  private boolean initialized = false;

  /**
   * Returns the singleton instance of this class.
   *
   * @return the singleton instance of this class.
   */
  public static TokenEventBusBridge getInstance() {
    boolean needsInit;
    synchronized (instance) {
      needsInit = !instance.initialized;
      instance.initialized = true;
    }
    if (needsInit) {
      instance.init();
    }
    return instance;
  }

  /**
   * Initializes the bridge, you must call this method after creating a new instance of this class.
   */
  private void init() {
    for (Zone zone : MapTool.getCampaign().getZones()) {
      addTokenChangeListener(zone);
    }
  }

  private void removeTokenChangeListener(Zone zone) {
    zone.removeModelChangeListener(modelChangeListener);
  }

  private void addTokenChangeListener(Zone zone) {
    zone.addModelChangeListener(modelChangeListener);
  }
}
