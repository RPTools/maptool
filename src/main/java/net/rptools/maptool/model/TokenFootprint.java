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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.stream.Collectors;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.TokenFootPrintDto;

/**
 * This class represents the set of cells a token occupies based on its size. Each token is assumed
 * to take up at least one cell, additional cells are indicated by cell offsets assuming the
 * occupied cell is at 0, 0
 */
public class TokenFootprint {
  private final Set<Point> cellSet = new HashSet<Point>();

  private String name;
  private String localizedName = null;
  private GUID id;
  private boolean isDefault;
  private double scale = 1;
  private boolean localizeName = false;

  private transient List<OffsetTranslator> translatorList = new LinkedList<OffsetTranslator>();

  public TokenFootprint() {
    // for serialization
  }

  public TokenFootprint(String name, boolean isDefault, double scale, Point... points) {
    this.name = name;
    id = new GUID();
    this.isDefault = isDefault;
    this.scale = scale;
    cellSet.addAll(Arrays.asList(points));
  }

  public static TokenFootprint fromDto(TokenFootPrintDto dto) {
    var footPrint = new TokenFootprint();
    footPrint.cellSet.addAll(
        dto.getCellSetList().stream().map(p -> Mapper.map(p)).collect(Collectors.toList()));
    footPrint.name = dto.getName();
    footPrint.id = GUID.valueOf(dto.getId());
    footPrint.isDefault = dto.getIsDefault();
    footPrint.scale = dto.getScale();
    footPrint.localizedName = dto.getLocalizedName();
    return footPrint;
  }

  public TokenFootPrintDto toDto() {
    var dto = TokenFootPrintDto.newBuilder();
    dto.addAllCellSet(cellSet.stream().map(Mapper::map).collect(Collectors.toList()));
    dto.setName(name);
    dto.setId(id.toString());
    dto.setIsDefault(isDefault);
    dto.setScale(scale);
    if (localizedName != null) {
      dto.setLocalizedName(localizedName);
    }
    return dto.build();
  }

  @Override
  public String toString() {
    return getLocalizedName();
  }

  public JsonObject toJson() {
    JsonObject jsonRep = new JsonObject();
    JsonArray occupiedString = new JsonArray();
    var cellArray = getOccupiedCells(new CellPoint(0, 0)).toArray();
    for (int j = 0; j < cellArray.length; j++) {
      CellPoint currentCell = (CellPoint) cellArray[j];
      JsonObject jsonPoint = new JsonObject();
      jsonPoint.addProperty("x", currentCell.x);
      jsonPoint.addProperty("y", currentCell.y);
      occupiedString.add(jsonPoint);
    }
    jsonRep.addProperty("name", name);
    jsonRep.add("cells", occupiedString);
    jsonRep.addProperty("scale", scale);
    jsonRep.addProperty("localizedName", localizedName);
    return jsonRep;
  }

  public void addOffsetTranslator(OffsetTranslator translator) {
    translatorList.add(translator);
  }

  public Set<CellPoint> getOccupiedCells(CellPoint centerPoint) {
    Set<CellPoint> occupiedSet = new HashSet<CellPoint>();

    // Implied
    // Not a centre point on square grid, in fact is top-left??
    occupiedSet.add(centerPoint);

    // Relative
    for (Point offset : cellSet) {
      CellPoint cp = new CellPoint(centerPoint.x + offset.x, centerPoint.y + offset.y);
      for (OffsetTranslator translator : translatorList) {
        translator.translate(centerPoint, cp);
      }
      occupiedSet.add(cp);
    }
    return occupiedSet;
  }

  public TokenFootprint(String name, Point... points) {
    this(name, false, 1, points);
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public GUID getId() {
    return id;
  }

  /** Returns the English name of the footprint */
  public String getName() {
    return name;
  }

  /** Returns the localized name of the footprint */
  public String getLocalizedName() {
    if (localizeName && localizedName == null) {
      localizedName = I18N.getString("TokenFootprint.name." + name.toLowerCase());
      return localizedName;
    } else if (localizedName != null) {
      return localizedName;
    }
    return name;
  }

  public void setLocalizedName(String text) {
    localizedName = text;
  }

  public Rectangle getBounds(Grid grid) {
    return getBounds(grid, null);
  }

  public double getScale() {
    return scale;
  }

  /**
   * Return a rectangle that exactly bounds the footprint, values are in {@link ZonePoint} space.
   *
   * @param grid the {@link Grid} that the footprint corresponds to
   * @param cell origin cell of this footprint; <code>null</code> means that <code>(0,0)</code> will
   *     be used
   * @return the bounding rectangle that bounds the footprint
   */
  public Rectangle getBounds(Grid grid, CellPoint cell) {
    cell = cell != null ? cell : new CellPoint(0, 0);
    Rectangle bounds = new Rectangle(grid.getBounds(cell));

    for (CellPoint cp : getOccupiedCells(cell)) {
      bounds.add(grid.getBounds(cp));
    }

    return bounds;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TokenFootprint)) {
      return false;
    }
    return ((TokenFootprint) obj).id.equals(id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  private Object readResolve() {
    translatorList = new LinkedList<OffsetTranslator>();
    return this;
  }

  public static interface OffsetTranslator {
    public void translate(CellPoint originPoint, CellPoint offsetPoint);
  }
}
