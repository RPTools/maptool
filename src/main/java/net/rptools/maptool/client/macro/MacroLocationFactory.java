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
package net.rptools.maptool.client.macro;

import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.client.macro.MacroLocation.MacroSource;
import net.rptools.maptool.model.Token;

/**
 * Creates a {@link MacroLocation} objecjt. This is a singleton class that provides methods to
 * create {@link MacroLocation} objects for different sources such as global, campaign, token, etc.
 */
public class MacroLocationFactory {

  /** Private constructor to prevent instantiation. */
  private MacroLocationFactory() {}

  /** The singleton instance of the factory. */
  private static MacroLocationFactory instance = new MacroLocationFactory();

  /**
   * Returns the singleton instance of the factory.
   *
   * @return the singleton instance of the factory.
   */
  public static MacroLocationFactory getInstance() {
    return instance;
  }

  /**
   * Creates a new {@link MacroLocation} object for an unknown location.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for an unknown location.
   */
  public MacroLocation createUnknownLocation(@Nonnull String name) {
    return new MacroLocation(name, MacroSource.unknown, "");
  }

  /**
   * Creates a new {@link MacroLocation} object for a global Panel.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for a global Panel.
   */
  public MacroLocation createGlobalLocation(@Nonnull String name) {
    return new MacroLocation(name, MacroSource.global, MacroSource.global.getSourceName());
  }

  /**
   * Creates a new {@link MacroLocation} object for a campaign Panel.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for a campaign Panel.
   */
  public MacroLocation createCampaignLocation(@Nonnull String name) {
    return new MacroLocation(name, MacroSource.campaign, MacroSource.campaign.getSourceName());
  }

  /**
   * Creates a new {@link MacroLocation} object for a token.
   *
   * @param name the name of the macro.
   * @param token the token associated with the macro.
   * @return a new {@link MacroLocation} object for a token.
   */
  public MacroLocation createTokenLocation(@Nonnull String name, @Nonnull Token token) {
    return new MacroLocation(name, MacroSource.token, token.getName(), null, token);
  }

  /**
   * Creates a new {@link MacroLocation} object for a library token.
   *
   * @param name the name of the macro.
   * @param libToken the library token associated with the macro.
   * @return a new {@link MacroLocation} object for a library token.
   */
  public MacroLocation createLibTokenLocation(@Nonnull String name, @Nonnull Token libToken) {
    return new MacroLocation(
        name, MacroSource.library, libToken.getName().substring(4), null, libToken);
  }

  /**
   * Creates a new {@link MacroLocation} object for a GM Panel.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for a GM Panel.
   */
  public MacroLocation createGmLocation(@Nonnull String name) {
    return new MacroLocation(name, MacroSource.gm, MacroSource.gm.getSourceName());
  }

  /**
   * Creates a new {@link MacroLocation} object for a library location.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for a library location.
   */
  public MacroLocation createExecFunctionLocation(@Nonnull String functionName) {
    return new MacroLocation(
        MacroSource.execFunction.getSourceName(), MacroSource.execFunction, functionName);
  }

  /**
   * Creates a new {@link MacroLocation} object for a macro link.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for a macro link.
   */
  public MacroLocation createMacroLinkLocation(@Nonnull String name) {
    return new MacroLocation(
        MacroSource.macroLink.getSourceName(),
        MacroSource.macroLink,
        MacroSource.macroLink.getSourceName());
  }

  /**
   * Creates a new {@link MacroLocation} object for an event.
   *
   * @param name the name of the macro.
   * @return a new {@link MacroLocation} object for an event.
   */
  public MacroLocation createEventLocation(@Nonnull String name) {
    return new MacroLocation(MacroSource.event.getSourceName(), MacroSource.event, name);
  }

  public MacroLocation createSentryIoLoggingLocation() {
    return new MacroLocation(
        MacroSource.sentryIoLogging.getSourceName(),
        MacroSource.sentryIoLogging,
        MacroSource.sentryIoLogging.getSourceName());
  }

  /**
   * Creates a new {@link MacroLocation} object for a URI location.
   *
   * @param name the name of the macro.
   * @param calledFrom the URI that called this macro.
   * @return a new {@link MacroLocation} object for a URI location.
   */
  public MacroLocation createUriLocation(@Nonnull String name, @Nullable URI calledFrom) {
    try {
      var uri = new URI(name);
      if (uri.getScheme() == null) {
        if (calledFrom == null) {
          return createUnknownLocation(name);
        }
        uri = calledFrom.resolve(uri);
      }
      return new MacroLocation(uri.getPath(), MacroSource.uri, uri.getHost(), uri, null);
    } catch (URISyntaxException e) {
      return createUnknownLocation(name);
    }
  }

  /**
   * Creates a new {@link MacroLocation} object for the chat box.
   *
   * @param token the token associated with the chat box.
   * @return a new {@link MacroLocation} object for a the chat box.
   */
  public MacroLocation createChatLocation() {
    return new MacroLocation(
        MacroSource.chat.getSourceName(), MacroSource.chat, MacroSource.chat.getSourceName());
  }

  /**
   * Creates a new {@link MacroLocation} object for a tooltip.
   *
   * @param token the token associated with the tooltip.
   * @return a new {@link MacroLocation} object for a tooltip.
   */
  public MacroLocation createToolTipLocation(@Nullable Token token) {
    return new MacroLocation(
        MacroSource.tooltip.getSourceName(),
        MacroSource.tooltip,
        token != null ? token.getName() : "",
        null,
        token);
  }
}
