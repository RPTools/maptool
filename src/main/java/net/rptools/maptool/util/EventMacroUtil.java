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
package net.rptools.maptool.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.exceptions.*;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.player.Player;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility class to facilitate macro events like onTokenMove and onInitiativeChange. */
public class EventMacroUtil {
  private static final Logger LOGGER = LogManager.getLogger(EventMacroUtil.class);
  /**
   * Scans all maps to find the first Lib:Token containing a macro that matches the given "callback"
   * string. If more than one token has such a macro, the first one encountered is returned -
   * because this order is unpredictable, this is very much not encouraged.
   *
   * @param macroCallback the macro name to find
   * @return the first Lib:token found that contains the requested macro, or null if none
   */
  public static Token getEventMacroToken(final String macroCallback) {
    return getEventMacroTokens(macroCallback).stream().findFirst().orElse(null);
  }

  /**
   * Scans all maps to find any Lib:Tokens that contain a macro matching the given "callback" label.
   *
   * @param macroCallback the macro name to find
   * @return a (possibly empty) list of Lib:tokens that contain the requested macro
   */
  public static List<Token> getEventMacroTokens(final String macroCallback) {
    List<Token> found = new ArrayList<>();
    List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
    for (ZoneRenderer zr : zrenderers) {
      List<Token> tokenList =
          zr.getZone().getTokensFiltered(t -> t.getName().toLowerCase().startsWith("lib:"));
      for (Token token : tokenList) {
        // If the token is not owned by everyone and all owners are GMs
        // then we are in
        // its a trusted Lib:token so we can run the macro
        if (token != null) {
          if (token.isOwnedByAll()) {
            continue;
          } else {
            Set<String> gmPlayers = new HashSet<String>();
            for (Object o : MapTool.getPlayerList()) {
              Player p = (Player) o;
              if (p.isGM()) {
                gmPlayers.add(p.getName());
              }
            }
            for (String owner : token.getOwners()) {
              if (!gmPlayers.contains(owner)) {
                continue;
              }
            }
          }
        }
        if (token.getMacro(macroCallback, false) != null) {
          found.add(token);
        }
      }
    }
    return found;
  }

  /**
   * Utility method to run a specified macro as a vetoable event handler, reporting whether the
   * handler has vetoed or not.
   *
   * <p>The specified vetoVariable will be initialized to 0, and the event handler indicates a veto
   * by setting the specified vetoVariable to 1. Any other value (or any errors encountered during
   * execution) are interpreted as allowing the event to continue.
   *
   * @param eventName the name of the event being run
   * @param libraryNamespace the namespace of the library containing the event handler
   * @param args the argument string to pass
   * @param tokenInContext token to set as current, if any
   * @param vetoVariable the specific variable to check for a veto
   * @param otherVars any other variables that should be initialized in the macro scope
   * @return true if the handler vetoed the event, false otherwise
   */
  public static boolean pollEventHandlerForVeto(
      final String eventName,
      final String libraryNamespace,
      final String args,
      final Token tokenInContext,
      final String vetoVariable,
      final Map<String, Object> otherVars) {
    Map<String, Object> varsToInitialize =
        (otherVars != null) ? new HashMap<>(otherVars) : new HashMap<>(1);
    varsToInitialize.put(vetoVariable, 0);

    boolean isVetoed = false;
    try {
      MapToolVariableResolver resolver =
          callEventHandler(eventName, libraryNamespace, args, tokenInContext, varsToInitialize);
      BigDecimal vetoValue = BigDecimal.ZERO;
      if (resolver.getVariable(vetoVariable) instanceof BigDecimal) {
        vetoValue = (BigDecimal) resolver.getVariable(vetoVariable);
      }
      isVetoed = (vetoValue != null && vetoValue.intValue() == 1);
    } catch (ParserException e) {
      MapTool.addLocalMessage(
          I18N.getText(
              "library.error.errorParsingVotableEvent",
              eventName,
              libraryNamespace,
              e.getMessage()));
      LOGGER.debug(
          I18N.getText(
              "library.error.errorParsingVotableEvent",
              eventName,
              libraryNamespace,
              e.getMessage()),
          e);
    }
    return isVetoed;
  }

  /**
   * Utility wrapper for running a specified macro as an event handler, getting back the variable
   * resolver instance that can be checked for any particular outputs.
   *
   * <p>Called macros will output to chat as normal - to suppress, see {@link
   * #callEventHandler(String, String, String, Token, Map, boolean)}
   *
   * @param eventName the name of the event being run
   * @param libraryNamespace the namespace of the library containing the event handler
   * @param args the argument string to pass
   * @param tokenInContext token to set as current, if any
   * @param varsToSet any variables that should be initialized in the macro scope
   * @return the variable resolver containing the resulting variable states
   */
  public static MapToolVariableResolver callEventHandler(
      final String eventName,
      final String libraryNamespace,
      final String args,
      final Token tokenInContext,
      Map<String, Object> varsToSet) {

    return callEventHandler(eventName, libraryNamespace, args, tokenInContext, varsToSet, false);
  }

  /**
   * Utility wrapper for running a specified macro as an event handler, getting back the variable
   * resolver instance that can be checked for any particular outputs.
   *
   * <p>Optionally suppresses chat output.
   *
   * @param eventName the name of the event being run
   * @param libraryNamespace the namespace of the library containing the event handler
   * @param args the argument string to pass
   * @param tokenInContext token to set as current, if any
   * @param varsToSet any variables that should be initialized in the macro scope
   * @param suppressChatOutput whether normal macro chat output should be suppressed
   * @return the variable resolver containing the resulting variable states
   */
  public static MapToolVariableResolver callEventHandler(
      final String eventName,
      final String libraryNamespace,
      final String args,
      final Token tokenInContext,
      Map<String, Object> varsToSet,
      boolean suppressChatOutput) {
    if (varsToSet == null) {
      varsToSet = Collections.emptyMap();
    }
    MapToolVariableResolver newResolver = new MapToolVariableResolver(tokenInContext);
    try {
      for (Map.Entry<String, Object> entry : varsToSet.entrySet()) {
        newResolver.setVariable(entry.getKey(), entry.getValue());
      }

      var library =
          new LibraryManager()
              .getLibrary(libraryNamespace)
              .orElseThrow(
                  () ->
                      new ParserException(
                          I18N.getText("library.error.notFound", libraryNamespace)));
      var eventTarget =
          library
              .getLegacyEventHandlerName(eventName)
              .get()
              .orElseThrow(
                  () ->
                      new ParserException(
                          I18N.getText(
                              "library.error.noEventHandler", eventName, libraryNamespace)));
      String macroTarget = eventTarget + "@lib:" + libraryNamespace;

      String resultVal =
          MapTool.getParser().runMacro(newResolver, tokenInContext, macroTarget, args, false);
      if (!suppressChatOutput && resultVal != null && !resultVal.equals("")) {
        MapTool.addMessage(
            new TextMessage(
                TextMessage.Channel.SAY, null, MapTool.getPlayer().getName(), resultVal, null));
      }
    } catch (AbortFunctionException afe) {
      // Do nothing
    } catch (AssertFunctionException e) {
      MapTool.addLocalMessage(e.getMessage());
    } catch (ParserException | ExecutionException | InterruptedException e) {
      MapTool.addLocalMessage(
          I18N.getText(
              "library.error.errorRunningEvent", eventName, libraryNamespace, e.getMessage()));
      LOGGER.debug(
          I18N.getText(
              "library.error.errorRunningEvent", eventName, libraryNamespace, e.getMessage()),
          e);
    }
    return newResolver;
  }

  /**
   * Utility wrapper for running a specified macro as an event handler, getting back the variable
   * resolver instance that can be checked for any particular outputs.
   *
   * <p>Called macros will output to chat as normal - to suppress, see {@link
   * #callEventHandlerOld(String, String, Token, Map, boolean)}
   *
   * @param macroTarget the fully-qualified macro name
   * @param args the argument string to pass
   * @param tokenInContext token to set as current, if any
   * @param varsToSet any variables that should be initialized in the macro scope
   * @return the variable resolver containing the resulting variable states
   */
  public static MapToolVariableResolver callEventHandlerOld(
      final String macroTarget,
      final String args,
      final Token tokenInContext,
      Map<String, Object> varsToSet) {
    return callEventHandlerOld(macroTarget, args, tokenInContext, varsToSet, false);
  }

  /**
   * Utility wrapper for running a specified macro as an event handler, getting back the variable
   * resolver instance that can be checked for any particular outputs.
   *
   * <p>Optionally suppresses chat output.
   *
   * @param macroTarget the fully-qualified macro name
   * @param args the argument string to pass
   * @param tokenInContext token to set as current, if any
   * @param varsToSet any variables that should be initialized in the macro scope
   * @param suppressChatOutput whether normal macro chat output should be suppressed
   * @return the variable resolver containing the resulting variable states
   */
  public static MapToolVariableResolver callEventHandlerOld(
      final String macroTarget,
      final String args,
      final Token tokenInContext,
      Map<String, Object> varsToSet,
      boolean suppressChatOutput) {
    if (varsToSet == null) varsToSet = Collections.emptyMap();
    MapToolVariableResolver newResolver = new MapToolVariableResolver(tokenInContext);
    try {
      for (Map.Entry<String, Object> entry : varsToSet.entrySet()) {
        newResolver.setVariable(entry.getKey(), entry.getValue());
      }
      String resultVal =
          MapTool.getParser().runMacro(newResolver, tokenInContext, macroTarget, args, false);
      if (!suppressChatOutput && resultVal != null && !resultVal.equals("")) {
        MapTool.addMessage(
            new TextMessage(
                TextMessage.Channel.SAY, null, MapTool.getPlayer().getName(), resultVal, null));
      }
    } catch (AbortFunctionException afe) {
      // Do nothing
    } catch (AssertFunctionException e) {
      MapTool.addLocalMessage(e.getMessage());
    } catch (ParserException e) {
      MapTool.addLocalMessage(
          "Event continuing after error running " + macroTarget + ": " + e.getMessage());
      LOGGER.debug("error running {}: {}", macroTarget, e.getMessage(), e);
    }
    return newResolver;
  }
}
