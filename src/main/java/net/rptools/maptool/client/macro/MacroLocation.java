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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.model.Token;

/**
 * Represents the location of a macro. This class is used to identify the source of a macro and
 * provides methods to parse and create macro locations.
 *
 * <p>Macro locations can be one of the following:
 *
 * <ul>
 *   <li>chat
 *   <li>token
 *   <li>gm
 *   <li>campaign
 *   <li>global
 *   <li>library
 *   <li>uri
 *   <li>execFunction
 *   <li>sentryIoLogging
 *   <li>tooltip
 *   <li>macroLink
 *   <li>event
 * </ul>
 */
public class MacroLocation {

  /** The name of the macro. */
  @Nonnull private final String name;

  /** The source of the macro. */
  @Nonnull private final MacroSource source;

  /** The location of the macro. */
  @Nonnull private final String location;

  /** the location of the macro that can be called from macro code using the '@' syntax */
  @Nonnull private final String callableLocation;

  /** The URI of the macro, if applicable. */
  @Nullable private final URI uri;

  /** MacroLocationFactory used to create locations during parsing. */
  private static final MacroLocationFactory factory = MacroLocationFactory.getInstance();

  /**
   * Creates a new MacroSource
   *
   * @param name the name of the macro.
   * @param source the source of the macro.
   * @param location the location of the macro.
   * @param uri the URI of the macro, if applicable.
   */
  MacroLocation(
      @Nonnull String name,
      @Nonnull MacroSource source,
      @Nonnull String location,
      @Nonnull String callableLocation,
      @Nullable URI uri) {
    this.name = name;
    this.source = source;
    this.location = location;
    this.callableLocation = callableLocation;
    this.uri = uri;
  }

  /**
   * Creates a new MacroLocation with the given name and source.
   *
   * @param name the name of the macro.
   * @param source the source of the macro.
   * @param location the location of the macro.
   */
  MacroLocation(
      @Nonnull String name,
      @Nonnull MacroSource source,
      @Nonnull String location,
      @Nonnull String callableLocation) {
    this(name, source, location, callableLocation, null);
  }

  /** Enumeration to represent the source of the macro. */
  public enum MacroSource {
    /** Macros from the chat window. */
    chat("chat", false),
    /** Macros from a token. */
    token("token", true),
    /** Macros from the GM Panel. */
    gm("gm", true),
    /** Macros from the campaign Panel. */
    campaign("campaign", true),
    /** Macros from the global Panel. */
    global("global", true),
    /** Macros from a library. */
    library("library", true),
    /** Macros from a URI. */
    uri("uri", true),
    /** Macros from the execFunction function. */
    execFunction("execFunction", false),
    /** Sentry.io logging. */
    sentryIoLogging("sentryIoLogging", false),
    /** Macros from a tooltip. */
    tooltip("tooltip", true),
    /** Macros from a macro link. */
    macroLink("macroLink", false),
    /** Macros from an event. */
    event("event", false),
    /** Macros from an unknown source. */
    unknown("unknown", false);

    /**
     * Creates a new {code MacroSource} with the given source name.
     *
     * @param sourceName the source name.
     * @param allowsAtThis true if the source allows @this to refer to other macros at the same
     *     location.
     */
    MacroSource(@Nonnull String sourceName, boolean allowsAtThis) {
      this.sourceName = sourceName;
      this.allowsAtThis = allowsAtThis;
    }

    /** The source name. */
    private final String sourceName;

    /** Does this source allow @this to refer to other macros at the same location? */
    private final boolean allowsAtThis;

    /**
     * Returns the source name.
     *
     * @return the source name.
     */
    public String getSourceName() {
      return sourceName;
    }

    /**
     * Returns true if the source allows @this to refer to other macros at the same location.
     *
     * @return true if the source allows @this to refer to other macros at the same location.
     */
    public boolean allowsAtThis() {
      return allowsAtThis;
    }
  }

  /**
   * Parses a macro name and returns a MacroLocation object.
   *
   * @param qMacroName the qualified macro name to parse.
   * @param calledFrom the location that called this macro, if applicable.
   * @param token the token for this macro, if applicable.
   * @return a MacroLocation object representing the parsed macro name.
   */
  public static MacroLocation parseMacroName(
      @Nonnull String qMacroName, @Nullable MacroLocation calledFrom, @Nullable Token token) {
    String qMacroNameLower = qMacroName.toLowerCase();

    if (qMacroNameLower.contains("@campaign")) {
      return factory.createCampaignLocation(qMacroName.substring(0, qMacroName.indexOf("@")));
    }

    if (qMacroNameLower.contains("@gm")) {
      return factory.createGmLocation(qMacroName.substring(0, qMacroName.indexOf("@")));
    }

    if (qMacroNameLower.contains("@global")) {
      return factory.createGlobalLocation(qMacroName.substring(0, qMacroName.indexOf("@")));
    }

    if (qMacroNameLower.contains("@token")) {
      if (token == null) {
        return factory.createUnknownLocation(qMacroName);
      }
      return factory.createTokenLocation(qMacroName.substring(0, qMacroName.indexOf("@")), token);
    }

    if (qMacroNameLower.contains("@lib:")) {
      String macroName = qMacroName.substring(0, qMacroName.indexOf("@"));
      String location = qMacroName.substring(qMacroName.indexOf("@") + 1);
      String namespace = location.replaceFirst("(?i)lib:", "");
      return new MacroLocation(macroName, MacroSource.library, namespace, location);
    }

    if (qMacroNameLower.contains("@this")) {
      var name = qMacroName.substring(0, qMacroName.indexOf("@"));
      var cfrom = calledFrom;
      if (cfrom == null || cfrom.getSource() == MacroSource.tooltip) { // tooltip is special
        if (token != null) {
          cfrom = factory.createTokenLocation(name, token);
        }
        if (cfrom == null || !cfrom.getSource().allowsAtThis()) {
          return factory.createUnknownLocation(qMacroName);
        }
      }
      return new MacroLocation(
          name, cfrom.getSource(), cfrom.getLocation(), cfrom.getCallableLocation());
    }

    // If none of the above then assume it is a URI
    return factory.createUriLocation(
        qMacroName,
        calledFrom != null && calledFrom.getSource() == MacroSource.uri
            ? calledFrom.getUri()
            : null);
  }

  /**
   * Returns the name of the macro.
   *
   * @return the name of the macro.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the source of the macro.
   *
   * @return the source of the macro.
   */
  public MacroSource getSource() {
    return source;
  }

  /**
   * Returns the location of the macro.
   *
   * @return the location of the macro.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Returns the URI of the macro, if applicable.
   *
   * @return the URI of the macro, or null if not applicable.
   */
  public URI getUri() {
    return uri;
  }

  /**
   * Returns the location of the macro that can be called from macro code using the '@' syntax.
   *
   * @return the location of the macro that can be called from other macros.
   */
  @Nonnull
  public String getCallableLocation() {
    return callableLocation;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{name='");
    sb.append(name);
    sb.append(", location='");
    sb.append(location);
    sb.append(", source='");
    sb.append(source);
    if (uri != null) {
      sb.append(", uri='");
      sb.append(uri);
    }
    sb.append("'}");

    return sb.toString();
  }
}
