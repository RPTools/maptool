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
package net.rptools.maptool.tool;

import com.thoughtworks.xstream.XStream;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.drawing.DrawableColorPaint;

public class LightSourceCreator {
  public static void main(String[] args) {
    Map<String, List<LightSource>> lightSourcesMap = new HashMap<String, List<LightSource>>();

    List<LightSource> lightSourceList = new ArrayList<LightSource>();

    lightSourceList.add(createD20LightSource("Candle - 5", 5, 360));
    lightSourceList.add(createD20LightSource("Lamp - 15", 15, 360));
    lightSourceList.add(createD20LightSource("Torch - 20", 20, 360));
    lightSourceList.add(createD20LightSource("Everburning - 20", 20, 360));
    lightSourceList.add(createD20LightSource("Lantern, Hooded - 30", 30, 360));
    lightSourceList.add(createD20LightSource("Sunrod - 30", 30, 360));

    lightSourcesMap.put("D20", lightSourceList);

    lightSourceList = new ArrayList<LightSource>();

    lightSourceList.add(createLightSource("5", 5, 360));
    lightSourceList.add(createLightSource("15", 15, 360));
    lightSourceList.add(createLightSource("20", 20, 360));
    lightSourceList.add(createLightSource("30", 30, 360));
    lightSourceList.add(createLightSource("40", 40, 360));
    lightSourceList.add(createLightSource("60", 60, 360));

    lightSourcesMap.put("Generic", lightSourceList);

    XStream xstream = FileUtil.getConfiguredXStream();
    System.out.println(xstream.toXML(lightSourcesMap));
  }

  private static LightSource createLightSource(String name, double radius, double arcAngle) {
    return LightSource.createRegular(
        name,
        new GUID(),
        LightSource.Type.NORMAL,
        false,
        false,
        List.of(new Light(ShapeType.CIRCLE, 0, radius, 0, arcAngle, null, 100, false, false)));
  }

  private static LightSource createD20LightSource(String name, double radius, double arcAngle) {
    return LightSource.createRegular(
        name,
        new GUID(),
        LightSource.Type.NORMAL,
        false,
        false,
        List.of(
            new Light(ShapeType.CIRCLE, 0, radius, 0, arcAngle, null, 100, false, false),
            new Light(
                ShapeType.CIRCLE,
                0,
                radius * 2,
                0,
                arcAngle,
                new DrawableColorPaint(new Color(0, 0, 0, 100)),
                100,
                false,
                false)));
  }
}
