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
package net.rptools.maptool.model.sheet.stats;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.ImageManager;

public class StatSheetContext {

  static class Property {
    private final String name;
    private final Object value;
    private final boolean gmOnly;

    Property(String name, Object value, boolean gmOnly) {
      this.name = name;
      this.value = value;
      this.gmOnly = gmOnly;
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }

    public boolean getGmOnly() {
      return gmOnly;
    }
  }

  private final String name;
  private final String gmName;

  private final String label;
  private final MD5Key imageAsset;

  private final MD5Key portraitAsset;

  private final int portraitWidth;
  private final int portraitHeight;

  private final String statSheetLocation;

  private final List<Property> properties = new ArrayList<>();

  private final String notes;

  private final String gmNotes;

  private final String speechName;

  private final String tokenType;

  private final boolean gm;

  public StatSheetContext(Token token, Player player, StatSheetLocation location) {

    name = token.getName();
    tokenType = token.getType().name();

    if (player.isGM()) {
      gmName = token.getGMName();
      gmNotes = token.getGMNotes();
      gm = true;
    } else {
      gmName = null;
      gmNotes = null;
      gm = false;
    }
    notes = AppUtil.playerOwns(token) ? token.getNotes() : null;
    speechName = token.getSpeechName();

    if (AppPreferences.getShowPortrait()) {
      imageAsset = token.getImageAssetId();
      portraitAsset = token.getPortraitImage();
    } else {
      imageAsset = null;
      portraitAsset = null;
    }
    label = token.getLabel();
    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    MapTool.getCampaign()
        .getCampaignProperties()
        .getTokenPropertyList(token.getPropertyType())
        .forEach(
            tp -> {
              if (tp.isShowOnStatSheet()) {
                if (tp.isGMOnly() && !MapTool.getPlayer().isGM()) {
                  return;
                }

                if (tp.isOwnerOnly() && !AppUtil.playerOwns(token)) {
                  return;
                }

                Object value = token.getEvaluatedProperty(resolver, tp.getName());
                if (value == null) {
                  return;
                }

                if (value instanceof String svalue) {
                  if (svalue.isBlank()) {
                    return;
                  }
                }
                properties.add(new Property(tp.getName(), value, tp.isGMOnly()));
              }
            });

    Dimension dim;
    if (token.getPortraitImage() != null) {
      var image = ImageManager.getImage(token.getPortraitImage());
      dim = new Dimension(image.getWidth(), image.getHeight());
    } else {
      var image = ImageManager.getImage(token.getImageAssetId());
      dim = new Dimension(image.getWidth(), image.getHeight());
    }
    SwingUtil.constrainTo(dim, AppPreferences.getPortraitSize());
    portraitWidth = dim.width;
    portraitHeight = dim.height;

    System.out.println("StatSheetContext property count: " + properties.size()); // TODO: CDW

    statSheetLocation =
        switch (location) {
          case TOP_LEFT -> "statSheet-topLeft";
          case TOP_RIGHT -> "statSheet-topRight";
          case BOTTOM_LEFT -> "statSheet-bottomLeft";
          case BOTTOM_RIGHT -> "statSheet-bottomRight";
          case TOP -> "statSheet-top";
          case BOTTOM -> "statSheet-bottom";
          case LEFT -> "statSheet-left";
          case RIGHT -> "statSheet-right";
        };
  }

  public String getName() {
    return name;
  }

  public String getGmName() {
    return gmName;
  }

  public String getImage() {
    return imageAsset != null ? "asset://" + imageAsset : null;
  }

  public String getPortrait() {
    return portraitAsset != null ? "asset://" + portraitAsset : null;
  }

  public String getLabel() {
    return label;
  }

  public int getPortraitWidth() {
    return portraitWidth;
  }

  public int getPortraitHeight() {
    return portraitHeight;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public String getStatSheetLocation() {
    return statSheetLocation;
  }

  public String getNotes() {
    return notes;
  }

  public String getGmNotes() {
    return gmNotes;
  }

  public String getSpeechName() {
    return speechName;
  }

  public String getTokenType() {
    return tokenType;
  }

  public boolean isGm() {
    return gm;
  }
}