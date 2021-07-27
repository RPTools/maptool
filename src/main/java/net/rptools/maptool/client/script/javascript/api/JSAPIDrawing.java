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
package net.rptools.maptool.client.script.javascript.api;

import java.util.ArrayList;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.DrawnElement;
import org.graalvm.polyglot.*;

public class JSAPIDrawing implements MapToolJSAPIInterface {
  @Override
  public String serializeToString() {
    return guid.toString();
  }

  @HostAccess.Export public final AbstractDrawing drawing;

  @HostAccess.Export public final GUID guid;
  private boolean dead = false;

  @HostAccess.Export
  public JSAPIDrawing(String gid) {
    guid = new GUID(gid);
    AbstractDrawing target = null;
    for (DrawnElement e :
        MapTool.getFrame().getCurrentZoneRenderer().getZone().getAllDrawnElements()) {
      if (e.getDrawable().getId().equals(guid)) {
        target = (AbstractDrawing) e.getDrawable();
        break;
      }
    }
    drawing = target;
    if (drawing == null) {
      dead = true;
    }
  }

  public JSAPIDrawing(AbstractDrawing d) {
    drawing = d;
    guid = d.getId();
  }

  @HostAccess.Export
  public void removeDrawable() {
    if (!dead) {
      //            AbstractDrawing target = null;
      //            for (DrawnElement e:
      // MapTool.getFrame().getCurrentZoneRenderer().getZone().getAllDrawnElements()) {
      //                if (e.getDrawable().getId().equals(guid)) {
      //                    target = (AbstractDrawing) e.getDrawable();
      //                    break;
      //                }
      //            }
      MapTool.getFrame().getCurrentZoneRenderer().getZone().removeDrawable(guid);
    }
    dead = true;
  }

  @HostAccess.Export
  public static ArrayList<Object> getAllDrawings() {
    ArrayList<Object> out = new ArrayList<>();
    for (DrawnElement e :
        MapTool.getFrame().getCurrentZoneRenderer().getZone().getAllDrawnElements()) {
      out.add(new JSAPIDrawing((AbstractDrawing) e.getDrawable()));
    }
    return out;
  }
}
