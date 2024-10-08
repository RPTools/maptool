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
package net.rptools.maptool.model;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.ImageManager;
import org.apache.commons.io.FileUtils;

public class ZoneFactory {

  public static final String DEFAULT_MAP_NAME = "Grasslands";
  public static MD5Key defaultImageId;

  static {
    // TODO: I really don't like this being hard wired this way, need to make it a preference or
    // something
    File grassImage =
        new File(AppUtil.getAppHome("resource/Default/Textures").getAbsolutePath() + "/Grass.png");
    if (grassImage.exists()) {
      try {
        Asset asset =
            Asset.createImageAsset(DEFAULT_MAP_NAME, FileUtils.readFileToByteArray(grassImage));
        defaultImageId = asset.getMD5Key();

        // Make sure the image is loaded to avoid a flash screen when it becomes visible
        ImageManager.getImageAndWait(asset.getMD5Key());
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  public static Zone createZone() {

    Zone zone = new Zone();

    zone.setName(DEFAULT_MAP_NAME);
    zone.setBackgroundPaint(new DrawableTexturePaint(defaultImageId));
    zone.setFogPaint(new DrawableColorPaint(Color.black));

    zone.setVisible(AppPreferences.newMapsVisible.get());
    zone.setHasFog(AppPreferences.newMapsHaveFow.get());
    zone.setUnitsPerCell(AppPreferences.defaultUnitsPerCell.get());
    zone.setTokenVisionDistance(AppPreferences.defaultVisionDistance.get());
    zone.setVisionType(AppPreferences.defaultVisionType.get());

    zone.setGrid(GridFactory.createGrid(AppPreferences.defaultGridType.get()));
    zone.setGridColor(AppPreferences.defaultGridColor.get().getRGB());
    zone.getGrid().setSize(AppPreferences.defaultGridSize.get());
    zone.getGrid().setOffset(0, 0);

    return zone;
  }
}
