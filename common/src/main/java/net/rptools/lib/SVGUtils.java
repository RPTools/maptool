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
package net.rptools.lib;

import com.github.weisj.jsvg.geometry.path.BuildHistory;
import com.github.weisj.jsvg.geometry.path.PathCommand;
import com.github.weisj.jsvg.geometry.path.PathParser;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class SVGUtils {
  public static Path2D svgToPath2D(String pathString) {
    PathParser pp = new PathParser(pathString);
    PathCommand[] commands = pp.parsePathCommand();
    Path2D path = new Path2D.Float();
    BuildHistory hist = new BuildHistory();
    for (PathCommand pathCommand : commands) {
      pathCommand.appendPath(path, hist);
    }
    path.trimToSize();
    path =
        new Path2D.Float(
            AffineTransform.getTranslateInstance(
                    -path.getBounds2D().getCenterX(), -path.getBounds2D().getCenterY())
                .createTransformedShape(path));
    return path;
  }
}
