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

import net.rptools.lib.ModelVersionTransformation;

public class PCVisionTransform implements ModelVersionTransformation {
  private static final String searchFor = "<tokenType>PC";
  private static final String subField = "<hasSight>";

  public String transform(String xml) {
    int index = 0;
    int start = 0;
    while ((start = xml.indexOf(searchFor, index)) > 0) {
      int sightPos = xml.indexOf(subField, start) + subField.length();
      while (Character.isWhitespace(xml.charAt(sightPos))) sightPos++;
      if (xml.charAt(sightPos) == 'f') {
        String pre = xml.substring(0, sightPos);
        String post = xml.substring(sightPos + "false".length());

        xml = pre + "true" + post;
      }
      index = sightPos;
    }

    return xml;
  }
}
