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

import java.util.ArrayList;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;

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

  private final List<Property> properties = new ArrayList<>();

  public StatSheetContext(Token token, Player player) {

    name = token.getName();
    gmName = player.isGM() ? token.getGMName() : null;
    imageAsset = token.getImageAssetId();
    portraitAsset = token.getPortraitImage();
    label = token.getLabel();
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

                properties.add(
                    new Property(tp.getName(), token.getProperty(tp.getName()), tp.isGMOnly()));
              }
            });
    System.out.println("StatSheetContext property count: " + properties.size());
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

  public List<Property> getProperties() {
    return properties;
  }
}