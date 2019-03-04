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
package net.rptools.maptool.client;

public class MapToolMacroContext {
  /** The name of the macro being executed. */
  private final String name;

  /** Where the macro comes from. */
  private final String source;

  /** Is the macro trusted or not. */
  private final boolean trusted;

  /** The index of the button that was clicked on to fire of this macro */
  private int macroButtonIndex;

  /**
   * Creates a new Macro Context.
   *
   * @param name The name of the macro.
   * @param source The source location of the macro.
   * @param trusted Is the macro trusted or not.
   */
  public MapToolMacroContext(String name, String source, boolean trusted) {
    this(name, source, trusted, -1);
  }

  /**
   * Creates a new Macro Context.
   *
   * @param name The name of the macro.
   * @param source The source location of the macro.
   * @param trusted Is the macro trusted or not.
   * @param macroButtonIndex The index of the button that ran this command.
   */
  public MapToolMacroContext(String name, String source, boolean trusted, int macroButtonIndex) {
    this.name = name;
    this.source = source;
    this.trusted = trusted;
    this.macroButtonIndex = macroButtonIndex;
  }

  /**
   * Gets the name of the macro context.
   *
   * @return the name of the macro context.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the source location of the macro context.
   *
   * @return the source location of the macro context.
   */
  public String getSource() {
    return source;
  }

  /**
   * Gets if the macro context is trusted or not.
   *
   * @return if the macro context is trusted or not.
   */
  public boolean isTrusted() {
    return trusted;
  }

  /**
   * Gets the index of the macro button that this macro is in
   *
   * @return the index of the macro button.
   */
  public int getMacroButtonIndex() {
    return macroButtonIndex;
  }
}
