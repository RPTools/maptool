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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;
import net.rptools.maptool.server.proto.GridTypeDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum GridType {
  Square(
      OffsetTranslator.IDENTITY,
      new GUID("7F000101CF65152A050000002A000101"),
      List.of(
          new TokenFootprint(
              new GUID("7F000101CD65152A010000002A000101"),
              "Fine",
              I18N.getString("TokenFootprint.name.fine"),
              0.5),
          new TokenFootprint(
              new GUID("7F000101CD65152A020000002A000101"),
              "Diminutive",
              I18N.getString("TokenFootprint.name.diminutive"),
              0.5),
          new TokenFootprint(
              new GUID("7F000101CE65152A030000002A000100"),
              "Tiny",
              I18N.getString("TokenFootprint.name.tiny"),
              0.5),
          new TokenFootprint(
              new GUID("7F000101CE65152A040000002A000100"),
              "Small",
              I18N.getString("TokenFootprint.name.small"),
              0.75),
          new TokenFootprint(
              new GUID("7F000101CF65152A050000002A000101"),
              "Medium",
              I18N.getString("TokenFootprint.name.medium"),
              1.0),
          new TokenFootprint(
              new GUID("7F000101D065152A060000002A000100"),
              "Large",
              I18N.getString("TokenFootprint.name.large"),
              1.0,
              new Point(1, 0),
              new Point(0, 1),
              new Point(1, 1)),
          new TokenFootprint(
              new GUID("7F000101D065152A070000002A000100"),
              "Huge",
              I18N.getString("TokenFootprint.name.huge"),
              1.0,
              new Point(1, 0),
              new Point(2, 0),
              new Point(0, 1),
              new Point(1, 1),
              new Point(2, 1),
              new Point(0, 2),
              new Point(1, 2),
              new Point(2, 2)),
          new TokenFootprint(
              new GUID("7F000101D165152A080000002A000101"),
              "Gargantuan",
              I18N.getString("TokenFootprint.name.gargantuan"),
              1.0,
              new Point(1, 0),
              new Point(2, 0),
              new Point(3, 0),
              new Point(0, 1),
              new Point(1, 1),
              new Point(2, 1),
              new Point(3, 1),
              new Point(0, 2),
              new Point(1, 2),
              new Point(2, 2),
              new Point(3, 2),
              new Point(0, 3),
              new Point(1, 3),
              new Point(2, 3),
              new Point(3, 3)),
          new TokenFootprint(
              new GUID("7F000101E165152A090000002A000101"),
              "Colossal",
              I18N.getString("TokenFootprint.name.colossal"),
              1.0,
              new Point(1, 0),
              new Point(2, 0),
              new Point(3, 0),
              new Point(4, 0),
              new Point(5, 0),
              new Point(0, 1),
              new Point(1, 1),
              new Point(2, 1),
              new Point(3, 1),
              new Point(4, 1),
              new Point(5, 1),
              new Point(0, 2),
              new Point(1, 2),
              new Point(2, 2),
              new Point(3, 2),
              new Point(4, 2),
              new Point(5, 2),
              new Point(0, 3),
              new Point(1, 3),
              new Point(2, 3),
              new Point(3, 3),
              new Point(4, 3),
              new Point(5, 3),
              new Point(0, 4),
              new Point(1, 4),
              new Point(2, 4),
              new Point(3, 4),
              new Point(4, 4),
              new Point(5, 4),
              new Point(0, 5),
              new Point(1, 5),
              new Point(2, 5),
              new Point(3, 5),
              new Point(4, 5),
              new Point(5, 5)))),
  Isometric(
      OffsetTranslator.IDENTITY,
      new GUID("7F000101CF65152A050000002A000101"),
      List.of(
          new TokenFootprint(
              new GUID("7F000101CD65152A010000002A000101"),
              "Fine",
              I18N.getString("TokenFootprint.name.fine"),
              0.5),
          new TokenFootprint(
              new GUID("7F000101CD65152A020000002A000101"),
              "Diminutive",
              I18N.getString("TokenFootprint.name.diminutive"),
              0.5),
          new TokenFootprint(
              new GUID("7F000101CE65152A030000002A000100"),
              "Tiny",
              I18N.getString("TokenFootprint.name.tiny"),
              0.5),
          new TokenFootprint(
              new GUID("7F000101CE65152A040000002A000100"),
              "Small",
              I18N.getString("TokenFootprint.name.small"),
              0.75),
          new TokenFootprint(
              new GUID("7F000101CF65152A050000002A000101"),
              "Medium",
              I18N.getString("TokenFootprint.name.medium"),
              1.0),
          new TokenFootprint(
              new GUID("7F000101D065152A060000002A000100"),
              "Large",
              I18N.getString("TokenFootprint.name.large"),
              1.0,
              new Point(1, 0),
              new Point(0, 1),
              new Point(1, 1)),
          new TokenFootprint(
              new GUID("7F000101D065152A070000002A000100"),
              "Huge",
              I18N.getString("TokenFootprint.name.huge"),
              1.0,
              new Point(1, 0),
              new Point(2, 0),
              new Point(0, 1),
              new Point(1, 1),
              new Point(2, 1),
              new Point(0, 2),
              new Point(1, 2),
              new Point(2, 2)),
          new TokenFootprint(
              new GUID("7F000101D165152A080000002A000101"),
              "Gargantuan",
              I18N.getString("TokenFootprint.name.gargantuan"),
              1.0,
              new Point(1, 0),
              new Point(2, 0),
              new Point(3, 0),
              new Point(0, 1),
              new Point(1, 1),
              new Point(2, 1),
              new Point(3, 1),
              new Point(0, 2),
              new Point(1, 2),
              new Point(2, 2),
              new Point(3, 2),
              new Point(0, 3),
              new Point(1, 3),
              new Point(2, 3),
              new Point(3, 3)),
          new TokenFootprint(
              new GUID("7F000101E165152A090000002A000101"),
              "Colossal",
              I18N.getString("TokenFootprint.name.colossal"),
              1.0,
              new Point(1, 0),
              new Point(2, 0),
              new Point(3, 0),
              new Point(4, 0),
              new Point(5, 0),
              new Point(0, 1),
              new Point(1, 1),
              new Point(2, 1),
              new Point(3, 1),
              new Point(4, 1),
              new Point(5, 1),
              new Point(0, 2),
              new Point(1, 2),
              new Point(2, 2),
              new Point(3, 2),
              new Point(4, 2),
              new Point(5, 2),
              new Point(0, 3),
              new Point(1, 3),
              new Point(2, 3),
              new Point(3, 3),
              new Point(4, 3),
              new Point(5, 3),
              new Point(0, 4),
              new Point(1, 4),
              new Point(2, 4),
              new Point(3, 4),
              new Point(4, 4),
              new Point(5, 4),
              new Point(0, 5),
              new Point(1, 5),
              new Point(2, 5),
              new Point(3, 5),
              new Point(4, 5),
              new Point(5, 5)))),
  HexVertical(
      OffsetTranslator.HEX_VERTICAL,
      new GUID("7F00010109655C380100000038000101"),
      List.of(
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560100000040A8090C"), "1/6", "1/6", .408),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560200000040A8090C"), "1/4", "1/4", .500),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560300000040A8090C"), "1/3", "1/3", .577),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560400000040A8090C"), "1/2", "1/2", .707),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560500000040A8090C"), "2/3", "2/3", .816),
          new TokenFootprint(
              new GUID("7F00010109655C380100000038000101"),
              "Medium",
              I18N.getString("TokenFootprint.name.medium"),
              1.0),
          new TokenFootprint(
              new GUID("7F00010109655C380200000038000101"),
              "Large",
              I18N.getString("TokenFootprint.name.large"),
              1.0,
              new Point(0, 1),
              new Point(1, 0)),
          new TokenFootprint(
              new GUID("7F00010109655C380300000038000101"),
              "Huge",
              I18N.getString("TokenFootprint.name.huge"),
              1.0,
              new Point(-1, -1),
              new Point(-1, 0),
              new Point(0, -1),
              new Point(0, 1),
              new Point(1, -1),
              new Point(1, 0)),
          new TokenFootprint(
              new GUID("C0A80F464BAAC1B10900000080800A42"),
              "Humongous",
              I18N.getString("TokenFootprint.name.humongous"),
              1.0,
              new Point(-2, -1),
              new Point(-2, 0),
              new Point(-2, 1),
              new Point(-1, -2),
              new Point(-1, -1),
              new Point(-1, 0),
              new Point(-1, 1),
              new Point(0, -2),
              new Point(0, -1),
              new Point(0, 1),
              new Point(0, 2),
              new Point(1, -2),
              new Point(1, -1),
              new Point(1, 0),
              new Point(1, 1),
              new Point(2, -1),
              new Point(2, 0),
              new Point(2, 1)))),
  HexHorizontal(
      OffsetTranslator.HEX_HORIZONTAL,
      new GUID("7F00010109655C380100000038000101"),
      List.of(
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560100000040A8090C"), "1/6", "1/6", .408),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560200000040A8090C"), "1/4", "1/4", .500),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560300000040A8090C"), "1/3", "1/3", .577),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560400000040A8090C"), "1/2", "1/2", .707),
          new TokenFootprint(new GUID("C0A80F0E0CB9FB560500000040A8090C"), "2/3", "2/3", .816),
          new TokenFootprint(
              new GUID("7F00010109655C380100000038000101"),
              "Medium",
              I18N.getString("TokenFootprint.name.medium"),
              1.0),
          new TokenFootprint(
              new GUID("7F00010109655C380200000038000101"),
              "Large",
              I18N.getString("TokenFootprint.name.large"),
              1.0,
              new Point(1, 0),
              new Point(0, 1)),
          new TokenFootprint(
              new GUID("7F00010109655C380300000038000101"),
              "Huge",
              I18N.getString("TokenFootprint.name.huge"),
              1.0,
              new Point(0, 1),
              new Point(1, 0),
              new Point(-1, 0),
              new Point(-1, -1),
              new Point(0, -1),
              new Point(-1, 1)),
          new TokenFootprint(
              new GUID("C0A80F464BAAC1B10900000080800A42"),
              "Humongous",
              I18N.getString("TokenFootprint.name.humongous"),
              1.0,
              new Point(-1, -2),
              new Point(0, -2),
              new Point(1, -2),
              new Point(-2, -1),
              new Point(-1, -1),
              new Point(0, -1),
              new Point(1, -1),
              new Point(-2, 0),
              new Point(-1, 0),
              new Point(1, 0),
              new Point(2, 0),
              new Point(-2, 1),
              new Point(-1, 1),
              new Point(0, 1),
              new Point(1, 1),
              new Point(-1, 2),
              new Point(0, 2),
              new Point(1, 2)))),
  None(
      OffsetTranslator.IDENTITY,
      new GUID("C0A80F0EBEFED0A20C00000080800E0E"),
      List.of(
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20100000080800E0E"), "-11", "-11", 0.086),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20200000080800E0E"), "-10", "-10", 0.107),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20300000080800E0E"), "-9", "-9", 0.134),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20400000080800E0E"), "-8", "-8", 0.168),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20500000080800E0E"), "-7", "-7", 0.210),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20600000080800E0E"), "-6", "-6", 0.262),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20700000080800E0E"), "-5", "-5", 0.328),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20800000080800E0E"), "-4", "-4", 0.410),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20900000080800E0E"), "-3", "-3", 0.512),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20A00000080800E0E"), "-2", "-2", 0.640),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20B00000080800E0E"), "-1", "-1", 0.800),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20C00000080800E0E"), "0", "0", 1.000),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20D00000080800E0E"), "1", "1", 1.200),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20E00000080800E0E"), "2", "2", 1.440),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A20F00000080800E0E"), "3", "3", 1.728),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21000000080800E0E"), "4", "4", 2.074),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21100000080800E0E"), "5", "5", 2.488),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21200000080800E0E"), "6", "6", 2.986),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21300000080800E0E"), "7", "7", 3.583),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21400000080800E0E"), "8", "8", 4.300),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21500000080800E0E"), "9", "9", 5.160),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21600000080800E0E"), "10", "10", 6.192),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21700000080800E0E"), "11", "11", 7.430),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21800000080800E0E"), "12", "12", 8.916),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21900000080800E0E"), "13", "13", 10.699),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21A00000080800E0E"), "14", "14", 12.839),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21B00000080800E0E"), "15", "15", 15.407),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21C00000080800E0E"), "16", "16", 18.488),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21D00000080800E0E"), "17", "17", 22.186),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21E00000080800E0E"), "18", "18", 26.623),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A21F00000080800E0E"), "19", "19", 31.948),
          new TokenFootprint(new GUID("C0A80F0EBEFED0A22000000080800E0E"), "20", "20", 38.338)));

  private final OffsetTranslator offsetTranslator;
  private final LinkedHashMap<GUID, TokenFootprint> footprints;
  private final TokenFootprint defaultFootprint;

  GridType(
      OffsetTranslator offsetTranslator, GUID defaultFootprintId, List<TokenFootprint> footprints) {
    final var log = LogManager.getLogger(GridType.class);

    this.offsetTranslator = offsetTranslator;
    this.footprints = new LinkedHashMap<>();
    for (var footprint : footprints) {
      var existing = this.footprints.put(footprint.getId(), footprint);
      if (existing != null) {
        log.error("Found duplicate footprint ID: {}", footprint.getId());
      }
    }

    var defaultFootprint = this.footprints.get(defaultFootprintId);
    if (defaultFootprint == null) {
      log.error("Invalid default footprint: ID {} not found", defaultFootprintId);
      // Default to the first one.
      defaultFootprint = footprints.getFirst();
    }
    this.defaultFootprint = defaultFootprint;
  }

  public OffsetTranslator getOffsetTranslator() {
    return offsetTranslator;
  }

  public SequencedCollection<TokenFootprint> getFootprints() {
    return Collections.unmodifiableSequencedCollection(footprints.sequencedValues());
  }

  public TokenFootprint getDefaultFootprint() {
    return defaultFootprint;
  }

  public TokenFootprint getFootprint(GUID guid) {
    return footprints.getOrDefault(guid, defaultFootprint);
  }

  private static final Logger log = LogManager.getLogger(GridType.class);

  public boolean isHex() {
    return this == HexVertical || this == HexHorizontal;
  }

  public boolean isIsometric() {
    return this == Isometric;
  }

  public boolean isNone() {
    return this == None;
  }

  public Shape createGridShape(double size) {
    return switch (this) {
      case Square -> {
        var path = new Path2D.Double();
        path.moveTo(size / 2, size / 2);
        path.lineTo(-size / 2, size / 2);
        path.lineTo(-size / 2, -size / 2);
        path.lineTo(size / 2, -size / 2);
        path.closePath();
        yield path;
      }
      case Isometric -> {
        var path = new Path2D.Double();
        path.moveTo(size, 0.);
        path.lineTo(0., size / 2);
        path.lineTo(-size, 0.);
        path.lineTo(0, -size / 2);
        path.closePath();
        yield path;
      }
      case HexHorizontal -> {
        final double l = size / Math.sqrt(3.);
        var path = new Path2D.Double();
        path.moveTo(size / 2, l / 2);
        path.lineTo(0, l);
        path.lineTo(-size / 2, l / 2);
        path.lineTo(-size / 2, -l / 2);
        path.lineTo(0, -l);
        path.lineTo(size / 2, -l / 2);
        path.closePath();
        yield path;
      }
      case HexVertical -> {
        final double l = size / Math.sqrt(3.);
        var path = new Path2D.Double();
        path.moveTo(l, 0);
        path.lineTo(l / 2, size / 2);
        path.lineTo(-l / 2, size / 2);
        path.lineTo(-l, 0);
        path.lineTo(-l / 2, -size / 2);
        path.lineTo(l / 2, -size / 2);
        path.closePath();
        yield path;
      }
      case None -> new Ellipse2D.Double(-size / 2, -size / 2, size, size);
    };
  }

  public String toString() {
    return switch (this) {
      case Square -> "Square";
      case Isometric -> "Isometric";
      case HexVertical -> "Vertical Hex";
      case HexHorizontal -> "Horizontal Hex";
      case None -> "None";
    };
  }

  public static GridType fromString(String string) {
    return switch (string) {
      case "Square" -> Square;
      case "Isometric" -> Isometric;
      case "Vertical Hex" -> HexVertical;
      case "Horizontal Hex" -> HexHorizontal;
      case "None" -> None;
      default ->
          throw new IllegalArgumentException(
              String.format("\"%s\" is not a valid grid type", string));
    };
  }

  public GridTypeDto toDto() {
    return switch (this) {
      case Square -> GridTypeDto.Square;
      case Isometric -> GridTypeDto.Isometric;
      case HexVertical -> GridTypeDto.HexVertical;
      case HexHorizontal -> GridTypeDto.HexHorizontal;
      case None -> GridTypeDto.Gridless;
    };
  }

  public static GridType fromDto(GridTypeDto dto) {
    return switch (dto) {
      case Square -> Square;
      case Gridless -> None;
      case HexVertical -> HexVertical;
      case HexHorizontal -> HexHorizontal;
      case Isometric -> Isometric;
      case UNRECOGNIZED -> {
        log.error("Unrecognized grid. Defaulting to {}", Square);
        yield Square;
      }
    };
  }
}
