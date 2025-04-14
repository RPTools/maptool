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
import javax.annotation.Nullable;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.ImageManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZoneFactory {
  private static final Logger log = LogManager.getLogger(ZoneFactory.class);

  public static final String DEFAULT_MAP_NAME = "Grasslands";
  private static final DrawableColorPaint fallbackBackgroundPaint =
      new DrawableColorPaint(Color.LIGHT_GRAY);
  private static @Nullable MD5Key defaultImageId;

  private static DrawablePaint getDefaultBackgroundPaint() {
    if (defaultImageId != null) {
      return new DrawableTexturePaint(defaultImageId);
    }

    File grassImage =
        new File(AppUtil.getAppHome("resource/Default/Textures").getAbsolutePath() + "/Grass.png");
    if (!grassImage.exists()) {
      log.warn(
          "Unable to load the default background texture: file {} does not exist",
          grassImage.getPath());
      return fallbackBackgroundPaint;
    }

    MD5Key imageId;
    try {
      Asset asset =
          Asset.createImageAsset(DEFAULT_MAP_NAME, FileUtils.readFileToByteArray(grassImage));
      imageId = asset.getMD5Key();

      // Make sure the image is loaded to avoid a flash screen when it becomes visible
      ImageManager.getImageAndWait(asset.getMD5Key());
    } catch (IOException ioe) {
      log.error("Error reading default background image", ioe);
      return fallbackBackgroundPaint;
    }

    defaultImageId = imageId;
    return new DrawableTexturePaint(defaultImageId);
  }

  public static Zone createZone() {
    Zone zone = new Zone();

    zone.setName(DEFAULT_MAP_NAME);

    var backgroundPaint = getDefaultBackgroundPaint();
    zone.setBackgroundPaint(backgroundPaint);

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
