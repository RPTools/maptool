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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

public interface DisplayName {
  /** The normal name used in code */
  String getName();

  /** The display name presented to users */
  String getDisplayName();

  /** The normal name used in code */
  void setName(String value);

  /** The display name presented to users */
  void setDisplayName(String value);

  interface WithTooltip extends DisplayName {
    /** The descriptive text presented to users, usually in a tooltip */
    String getDescription();

    /** The descriptive text presented to users, usually in a tooltip */
    void setDescription(String value);
  }

  interface WithShortName extends DisplayName {
    /** The abbreviated form, e.g. "STR" for "Strength" */
    String getShortName();

    /** The abbreviated form, e.g. "STR" for "Strength" */
    void setShortName(String value);

    /** An abbreviated form exists, e.g. "STR" for "Strength" */
    boolean hasShortName();
  }
}
