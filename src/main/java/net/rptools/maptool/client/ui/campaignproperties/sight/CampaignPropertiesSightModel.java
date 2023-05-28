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
package net.rptools.maptool.client.ui.campaignproperties.sight;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.swing.table.AbstractTableModel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.LightSource.Type;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.drawing.DrawableColorPaint;

/** Table model for the sight types in the campaign properties. */
public class CampaignPropertiesSightModel extends AbstractTableModel {

  /**
   * Copy of the sight types from the campaign properties. This is used so that we can modify them.
   */
  private List<SightType> sightTypes = new ArrayList<>();

  @Override
  public int getRowCount() {
    return sightTypes.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    SightType sightType = sightTypes.get(rowIndex);
    var personalLightSource = sightType.getPersonalLightSource();
    Light light =
        personalLightSource != null && !personalLightSource.getLightList().isEmpty()
            ? personalLightSource.getLightList().get(0)
            : null;
    return switch (columnIndex) {
      case 0 -> sightType.getName();
      case 1 -> sightType.getShape();
      case 2 -> sightType.getDistance() != 0 ? sightType.getDistance() : null;
      case 3 -> sightType.isScaleWithToken();
      case 4 -> sightType.getShape() == ShapeType.CONE ? sightType.getArc() : null;
      case 5 -> sightType.getShape() == ShapeType.CONE ? sightType.getOffset() : null;
      case 6 -> sightType.getMultiplier();
      case 7 -> light == null ? null : light.getRadius();
      case 8 -> {
        if (light != null) {
          if (light.getPaint() != null && light.getPaint() instanceof DrawableColorPaint) {
            yield (Color) light.getPaint().getPaint();
          }
        }
        yield null;
      }
      case 9 -> light == null ? null : light.getLumens();
      default -> null;
    };
  }

  @Override
  public int getColumnCount() {
    return 10;
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> I18N.getString("CampaignPropertiesSite.table.column.name");
      case 1 -> I18N.getString("CampaignPropertiesSite.table.column.shape");
      case 2 -> I18N.getString("CampaignPropertiesSite.table.column.distance");
      case 3 -> I18N.getString("CampaignPropertiesSite.table.column.scaleWithToken");
      case 4 -> I18N.getString("CampaignPropertiesSite.table.column.arc");
      case 5 -> I18N.getString("CampaignPropertiesSite.table.column.offset");
      case 6 -> I18N.getString("CampaignPropertiesSite.table.column.multiplier");
      case 7 -> I18N.getString("CampaignPropertiesSite.table.column.personalLightDistance");
      case 8 -> I18N.getString("CampaignPropertiesSite.table.column.personalLightColor");
      case 9 -> I18N.getString("CampaignPropertiesSite.table.column.personalLightLumens");
      default -> null;
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> String.class;
      case 1 -> ShapeType.class;
      case 2 -> Float.class;
      case 3 -> Boolean.class;
      case 4, 5, 9 -> Integer.class;
      case 6, 7 -> Double.class;
      case 8 -> Color.class;
      default -> null;
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    var sight = sightTypes.get(rowIndex);

    switch (columnIndex) {
      case 0 -> sight.setName((String) aValue);
      case 1 -> {
        sight.setShape((ShapeType) aValue);
        if (sight.getShape() == ShapeType.CONE) {
          if (sight.getArc() == 0) {
            sight.setArc(120);
          }
        } else {
          sight.setArc(0);
          sight.setOffset(0);
        }
      }
      case 2 -> sight.setDistance((Float) aValue);
      case 3 -> sight.setScaleWithToken((Boolean) aValue);
      case 4 -> sight.setArc((Integer) aValue);
      case 5 -> sight.setOffset((Integer) aValue);
      case 6 -> sight.setMultiplier((Double) aValue);
      case 7, 8, 9 -> {
        var personalLightSource = sight.getPersonalLightSource();
        Light light =
            personalLightSource != null && !personalLightSource.getLightList().isEmpty()
                ? personalLightSource.getLightList().get(0)
                : null;
        Color color = null;
        int lumens = 100;
        double radius = 0;
        if (light != null) {
          if (light.getPaint() != null && light.getPaint() instanceof DrawableColorPaint) {
            color = (Color) light.getPaint().getPaint();
            lumens = light.getLumens();
            radius = light.getRadius();
          }
        }
        switch (columnIndex) {
          case 7 -> radius = (Double) aValue;
          case 8 -> color = (Color) aValue;
          case 9 -> lumens = (Integer) aValue;
        }
        sight.setPersonalLightSource(
            createPersonalLightSource(
                sight.getShape(), radius, sight.getArc(), color, lumens, sight.isScaleWithToken()));
      }
    }
  }

  /**
   * Creates a personal light source for the given parameters.
   *
   * @param shape The shape of the light source.
   * @param radius The radius of the light source.
   * @param arcAngle The arc angle of the light source.
   * @param color The color of the light source.
   * @param lumens The lumens of the light source.
   * @param scale Whether the light source should scale with the token.
   * @return The personal light source.
   */
  private LightSource createPersonalLightSource(
      ShapeType shape,
      double radius,
      int arcAngle,
      @Nullable Color color,
      int lumens,
      boolean scale) {
    var lightSource = new LightSource();
    lightSource.setType(Type.NORMAL);
    lightSource.setScaleWithToken(scale);
    var paint = color == null ? null : new DrawableColorPaint(color);
    var light = new Light(shape, 0, radius, arcAngle, paint, lumens, false, false);
    lightSource.add(light);
    return lightSource;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return switch (columnIndex) {
      case 4, 5 -> {
        var sightType = sightTypes.get(rowIndex);
        yield sightType.getShape() == ShapeType.CONE;
      }
      default -> true;
    };
  }

  /**
   * Returns the sight types from the table.
   *
   * @return The sight types.
   */
  public List<SightType> getSightTypes() {
    return sightTypes;
  }

  public void addNewSightType() {
    int seq = 1;
    Set<String> sightNames =
        sightTypes.stream().map(SightType::getName).collect(Collectors.toSet());

    // There are so few sight types that we don't need to do anything fancy to find a unique name.
    String name = I18N.getText("campaignProperties.newSightType", seq);
    while (sightNames.contains(name)) {
      seq++;
      name = I18N.getText("campaignProperties.newSightType", seq);
    }
    var sightType = new SightType(name, 1.0, null);
    sightTypes.add(sightType);
    fireTableRowsInserted(sightTypes.size() - 1, sightTypes.size() - 1);
  }

  public void removeSightType(int selectedRow) {
    if (selectedRow < 0 || selectedRow >= sightTypes.size()) {
      return;
    }
    sightTypes.remove(selectedRow);
    fireTableRowsDeleted(selectedRow, selectedRow);
  }

  public void setSightTypes(Map<String, SightType> sightTypeMap) {
    // Make a copy so that we can modify it
    sightTypes.clear();
    sightTypes.addAll(
        sightTypeMap.values().stream().sorted(Comparator.comparing(SightType::getName)).toList());
    fireTableDataChanged();
  }
}
