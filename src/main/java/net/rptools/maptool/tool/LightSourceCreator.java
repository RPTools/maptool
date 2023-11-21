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
import net.rptools.maptool.model.drawing.DrawablePaint;

public class LightSourceCreator {
  private static Light createLight(
      ShapeType shape,
      double radius,
      double arcAngle,
      double offset,
      DrawablePaint paint,
      int lumens,
      boolean isGM,
      boolean ownerOnly) {
    if (arcAngle == 0) arcAngle = 360;
    shape = shape == null ? ShapeType.CIRCLE : shape;
    return new Light(shape, offset, radius, arcAngle, paint, lumens, isGM, ownerOnly);
  }

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

    lightSourceList.add(createCircleLightSource("5", 5, 360));
    lightSourceList.add(createCircleLightSource("15", 15, 360));
    lightSourceList.add(createCircleLightSource("20", 20, 360));
    lightSourceList.add(createCircleLightSource("30", 30, 360));
    lightSourceList.add(createCircleLightSource("40", 40, 360));
    lightSourceList.add(createCircleLightSource("60", 60, 360));

    lightSourcesMap.put("Generic", lightSourceList);

    //    lightSourceList = new ArrayList<LightSource>();
    //
    //    lightSourceList.add(createLightSource("Front", 5, 360));
    //
    //    lightSourcesMap.put("Auras", lightSourceList);

    XStream xstream = FileUtil.getConfiguredXStream();
    System.out.println(xstream.toXML(lightSourcesMap));
  }

  //  private static LightSource createLightSource(String name, double radius, double arcAngle) {
  //  }
  private static LightSource createCircleLightSource(String name, double radius, double arcAngle) {
    return LightSource.createRegular(
        name,
        new GUID(),
        LightSource.Type.NORMAL,
        false,
        List.of(createLight(ShapeType.CIRCLE, radius, arcAngle, 0d, null, 100, false, false)));
  }

  private static LightSource createD20LightSource(String name, double radius, double arcAngle) {
    return LightSource.createRegular(
        name,
        new GUID(),
        LightSource.Type.NORMAL,
        false,
        List.of(
            createLight(ShapeType.CIRCLE, radius, arcAngle, 0d, null, 100, false, false),
            createLight(
                ShapeType.CIRCLE,
                2 * radius,
                arcAngle,
                0d,
                new DrawableColorPaint(new Color(0, 0, 0, 100)),
                100,
                false,
                false)));
  }
}
