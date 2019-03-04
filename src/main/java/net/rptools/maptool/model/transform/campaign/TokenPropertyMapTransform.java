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
package net.rptools.maptool.model.transform.campaign;

import java.util.regex.Pattern;
import net.rptools.lib.ModelVersionTransformation;

public class TokenPropertyMapTransform implements ModelVersionTransformation {
  private static final String blockStart = "<propertyMap>\\s*<store";
  private static final String blockEnd = "</propertyMap>";
  private static final String regex = blockStart + "(/?>.*?)" + blockEnd;
  private static final String replacement = "<propertyMapCI><store$1</propertyMapCI>";

  private static final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

  /**
   * Delete the block containing the now-obsolete exportInfo class data, since there is no place to
   * put it (and therefore generates an XStream error)
   */
  public String transform(String xml) {
    // Same as: return xml.replaceAll(regex, replacement);
    // except that we can specify the flag DOTALL
    return pattern.matcher(xml).replaceAll(replacement);
  }
}
