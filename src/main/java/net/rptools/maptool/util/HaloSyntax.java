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
package net.rptools.maptool.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import net.rptools.lib.StringUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CategorizedHalos;
import net.rptools.maptool.model.CategorizedHalos.Category;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Halo;
import net.rptools.maptool.model.HaloPart;
import net.rptools.maptool.model.Halos;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HaloSyntax {

  private static final Logger log = LogManager.getLogger(HaloSyntax.class);

  public Halos parseHalos(String text, Halos original) {
    final var halos = new Halos();
    final var reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
    List<String> errlog = new LinkedList<>();

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        var halo = parseHaloLine(line, reader.getLineNumber(), original, errlog);
        if (halo != null) {
          halos.add(halo);
        }
      }
    } catch (IOException ioe) {
      MapTool.showError("msg.error.mtprops.halo.ioexception", ioe);
    }

    if (!errlog.isEmpty()) {
      MapTool.showFeedback(errlog.toArray());
      errlog.clear();
      throw new IllegalArgumentException(
          "msg.error.mtprops.halo.definition"); // Don't save halos...
    }

    return halos;
  }

  public CategorizedHalos parseCategorizedHalos(String text, CategorizedHalos originalHaloMap) {
    final var categorized = new CategorizedHalos();
    final var reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
    List<String> errlog = new LinkedList<>();

    try {
      Halos currentGroupOriginalHalos = new Halos();
      Category currentGroup = null;

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Blank lines
        if (line.isEmpty()) {
          if (currentGroup != null) {
            categorized.addAllToCategory(currentGroup.name(), currentGroup.halos());
          }
          currentGroup = null;
          continue;
        }
        // New group
        if (currentGroup == null) {
          currentGroup = new Category(line, new Halos());
          currentGroupOriginalHalos =
              originalHaloMap
                  .getCategory(currentGroup.name())
                  .map(Category::halos)
                  .orElse(new Halos());
          continue;
        }

        var halo = parseHaloLine(line, reader.getLineNumber(), currentGroupOriginalHalos, errlog);
        if (halo != null) {
          currentGroup.halos().add(halo);
        }
      }

      if (currentGroup != null) {
        categorized.addAllToCategory(currentGroup.name(), currentGroup.halos());
      }
    } catch (IOException ioe) {
      MapTool.showError("msg.error.mtprops.halo.ioexception", ioe);
    }

    if (!errlog.isEmpty()) {
      MapTool.showFeedback(errlog.toArray());
      errlog.clear();
      throw new IllegalArgumentException(
          "msg.error.mtprops.halo.definition"); // Don't save halos...
    }

    return categorized;
  }

  /**
   * Get a list of halos as a string.
   *
   * @param halos the erm... halos
   * @return the halos as a string
   */
  public String stringifyHalos(Halos halos) {
    StringBuilder builder = new StringBuilder();
    writeHaloLines(builder, halos);
    return builder.toString();
  }

  /**
   * Get a list of categorized halos as a string.
   *
   * @param halos the erm... halos
   * @return the categorized halos as a string
   */
  public String stringifyCategorizedHalos(CategorizedHalos halos) {
    StringBuilder builder = new StringBuilder();
    for (var category : halos.getCategories()) {
      builder.append(category.name());
      builder.append("\n----\n");

      writeHaloLines(builder, category.halos());
      builder.append('\n');
    }
    return builder.toString();
  }

  /**
   * Stringify the halos line by line into a list.
   *
   * @param builder the erm... builder
   * @param halos the halos within a category
   */
  private void writeHaloLines(StringBuilder builder, Halos halos) {
    for (Halo halo : halos) {

      // region 1 write halo properties
      builder.append(halo.getName()).append(":");

      if (halo.isGMOnly()) {
        builder.append(" GM");
      }
      if (halo.isOwnerOnly()) {
        builder.append(" OWNER");
      }
      if (halo.isInner()) {
        builder.append(" INNER");
      }
      if (halo.isFacingWithToken()) {
        builder.append(" FACING");
      }
      if (halo.isFlipWithToken()) {
        builder.append(" FLIP");
      }
      if (halo.isScaleWithToken()) {
        builder.append(" SCALE");
      }
      // endregion

      // region 2 write haloPart properties
      final var lastParameters = new LinkedHashMap<String, Object>();
      lastParameters.put("", null);
      lastParameters.put("fill", false);
      lastParameters.put("flipH", false);
      lastParameters.put("flipV", false);
      lastParameters.put("angle", 0.);
      lastParameters.put("offset", 0.);
      lastParameters.put("rotate", 0.);
      lastParameters.put("scaleX", 1d);
      lastParameters.put("scaleY", 1d);
      lastParameters.put("vertices", 0);
      lastParameters.put("mini", 0);
      lastParameters.put("miniStart", 0);
      lastParameters.put("miniStop", 0);
      lastParameters.put("miniRotate", 0.);
      lastParameters.put("miniSpin", 1d);

      for (HaloPart haloPart : halo.getHaloParts()) {
        final var parameters = new HashMap<>();

        parameters.put("fill", haloPart.getFill());

        HaloPart.HaloShapeType haloShapeType = haloPart.getHaloShapeType();
        if (haloShapeType != null) {
          if (!haloShapeType.equals(HaloPart.DEFAULT_HALO_SHAPE_TYPE)) {
            parameters.put("", haloShapeType.name().toLowerCase());
          }

          if (haloShapeType.isTransformable()) {
            if (haloPart.getFlipHorizontal()) {
              parameters.put("flipH", true);
            }
            if (haloPart.getFlipVertical()) {
              parameters.put("flipV", true);
            }
            parameters.put("offset", haloPart.getOffset());
            parameters.put("rotate", haloPart.getRotate());
            parameters.put("scaleX", haloPart.getScaleX());
            parameters.put("scaleY", haloPart.getScaleY());
            parameters.put("mini", haloPart.getMini());
          }

          if (haloShapeType.isPolygonal()) {
            parameters.put("vertices", haloPart.getVertices());
          }

          if (haloShapeType.isAngleBased()) {
            parameters.put("angle", haloPart.getAngle());
          }
          if (haloPart.getMini() > 0) {
            parameters.put("miniStart", haloPart.getMiniStart());
            parameters.put("miniStop", haloPart.getMiniStop());
            parameters.put("miniRotate", haloPart.getMiniRotate());
            parameters.put("miniSpin", haloPart.getMiniSpin());
          }
        }

        for (final var parameterEntry : lastParameters.entrySet()) {
          final var key = parameterEntry.getKey();
          final var oldValue = parameterEntry.getValue();
          final var newValue = parameters.get(key);

          if (newValue != null && !newValue.equals(oldValue)) {
            lastParameters.put(key, newValue);

            // Special case: booleans are flags that are either present or not.
            if (newValue instanceof Boolean b) {
              if (b) {
                builder.append(" ").append(key);
              }
            } else {
              builder.append(" ");
              if (!"".equals(key)) {
                // Special case: don't include a key= for shapes.
                builder.append(key).append("=");
              }
              builder.append(
                  switch (newValue) {
                    case Double d -> StringUtil.formatDecimal(d);
                    default -> newValue.toString();
                  });
            }
          }
        }
        // endregion

        // region 3 write haloPart properties: width, color, and pattern
        DecimalFormat decimalFormat =
            new DecimalFormat(
                "0.###"); // do not add thousand separators and remove trailing d.p. zeros
        if (haloPart.getWidth() != null) {
          builder.append(' ').append(decimalFormat.format(haloPart.getWidth()));
        } else {
          builder.append(' ');
        }
        if (haloPart.getPaint() instanceof DrawableColorPaint dcp) {
          builder.append(colorToHexRGBA(dcp));
        }
        if (haloPart.getDashedPattern() != null) {
          if (!haloPart.getDashedPattern().isEmpty()) {
            builder.append('(');
            int i = 0;
            for (Float dashOrGap : haloPart.getDashedPattern()) {
              i = i + 1;
              builder.append(decimalFormat.format(dashOrGap));
              if (i < haloPart.getDashedPattern().size()) {
                builder.append(',');
              }
            }
            builder.append(')');
          }
        }
        // endregion
      }
      builder.append('\n');
    }
  }

  private Halo parseHaloLine(
      String line, int lineNumber, Halos originalInCategory, List<String> errlog) {
    // Blank lines, comments
    if (line.isEmpty() || line.charAt(0) == '-') {
      return null;
    }

    // Item
    int split = line.indexOf(':');
    if (split < 1) {
      return null;
    }

    String name = line.substring(0, split).trim();
    GUID id = new GUID();

    // region halo properties
    boolean gmOnly = false;
    boolean ownerOnly = false;
    boolean inner = false;
    boolean facingWithToken = false;
    boolean flipWithToken = false;
    boolean scaleWithToken = false;
    List<HaloPart> haloParts = new ArrayList<>();
    // endregion

    // region haloPart properties
    HaloPart.HaloShapeType shape = null;
    boolean fill = false;
    boolean flipHorizontal = false;
    boolean flipVertical = false;
    double angle = 0;
    double offset = 0;
    double rotate = 0;
    double scaleX = 1d;
    double scaleY = 1d;
    int vertices = 0;
    int mini = 0;
    int miniStart = 0;
    int miniStop = 0;
    double miniRotate = 0.;
    double miniSpin = 1d;
    // endregion

    for (String arg : line.substring(split + 1).split("\\s+")) {
      arg = arg.trim();
      if (arg.isEmpty()) {
        continue;
      }

      // region halo properties designations
      // Visible to GM only designation
      if (arg.equalsIgnoreCase("GM")) {
        gmOnly = true;
        continue;
      }
      // Visible to token owner only designation
      if (arg.equalsIgnoreCase("OWNER")) {
        ownerOnly = true;
        continue;
      }
      // Inner token designation
      if (arg.equalsIgnoreCase("INNER")) {
        inner = true;
        continue;
      }
      // Facing with token designation
      if (arg.equalsIgnoreCase("FACING")) {
        facingWithToken = true;
        continue;
      }
      // Facing with token designation
      if (arg.equalsIgnoreCase("FLIP")) {
        flipWithToken = true;
        continue;
      }
      // Scale with token designation
      if (arg.equalsIgnoreCase("SCALE")) {
        scaleWithToken = true;
        continue;
      }
      // endregion

      // region haloPart properties designations
      if (arg.equalsIgnoreCase("FILL")) {
        fill = true;
        continue;
      }
      if (arg.equalsIgnoreCase("FLIPH")) {
        flipHorizontal = true;
        continue;
      }
      if (arg.equalsIgnoreCase("FLIPV")) {
        flipVertical = true;
        continue;
      }

      // Angle designation
      if (arg.toUpperCase().startsWith("ANGLE=")) {
        try {
          angle = Double.parseDouble(arg.substring(6));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.angle", lineNumber, arg));
        }
      }

      // Offset designation
      if (arg.toUpperCase().startsWith("OFFSET=")) {
        try {
          offset = Double.parseDouble(arg.substring(7));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.offset", lineNumber, arg));
        }
      }

      // Rotate angle designation
      if (arg.toUpperCase().startsWith("ROTATE=")) {
        try {
          rotate = Double.parseDouble(arg.substring(7));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.rotate", lineNumber, arg));
        }
      }

      // ScaleX angle designation
      if (arg.toUpperCase().startsWith("SCALEX=")) {
        try {
          scaleX = Double.parseDouble(arg.substring(7));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.scaleX", lineNumber, arg));
        }
      }

      // ScaleY angle designation
      if (arg.toUpperCase().startsWith("SCALEY=")) {
        try {
          scaleY = Double.parseDouble(arg.substring(7));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.scaleY", lineNumber, arg));
        }
      }

      // Vertices designation, used for Polygons and Stars
      if (arg.toUpperCase().startsWith("VERTICES=")) {
        try {
          vertices = Integer.parseInt(arg.substring(9));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.vertices", lineNumber, arg));
        }
      }

      // Mini designation
      if (arg.toUpperCase().startsWith("MINI=")) {
        try {
          mini = Integer.parseInt(arg.substring(5));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.mini", lineNumber, arg));
        }
      }

      // Mini start designation
      if (arg.toUpperCase().startsWith("MINISTART=")) {
        try {
          miniStart = Integer.parseInt(arg.substring(10));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.miniStart", lineNumber, arg));
        }
      }

      // Mini stop designation
      if (arg.toUpperCase().startsWith("MINISTOP=")) {
        try {
          miniStop = Integer.parseInt(arg.substring(9));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.miniStop", lineNumber, arg));
        }
      }

      // Mini rotate angle designation
      if (arg.toUpperCase().startsWith("MINIROTATE=")) {
        try {
          miniRotate = Double.parseDouble(arg.substring(11));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.miniRotate", lineNumber, arg));
        }
      }

      // Mini speed of rotation designation
      if (arg.toUpperCase().startsWith("MINISPIN=")) {
        try {
          miniSpin = Double.parseDouble(arg.substring(9));
          continue;
        } catch (NullPointerException noe) {
          errlog.add(I18N.getText("msg.error.mtprops.halo.miniSpin", lineNumber, arg));
        }
      }

      // Shape designation
      try {
        shape = HaloPart.HaloShapeType.valueOf(arg.toUpperCase());
        continue;
      } catch (IllegalArgumentException iae) {
        // Expected when not defining a shape
      }
      // endregion

      // region haloPart properties: width, color, and pattern
      Integer width = null;
      Color color = null;
      ArrayList<Float> dashedPattern = new ArrayList<>();

      final var rangeRegex = Pattern.compile("^(?:\\s*)([^#+-]*)(#[0-9a-fA-F]+)?(?:\\((.+?)\\))?");
      // e.g. #123456, #12345678, 5#123456, 5#baaaa2(1,5), 10#baaaa2(1,5,1)
      final var matcher = rangeRegex.matcher(arg);
      if (matcher.find()) {
        final String widthString = matcher.group(1);
        final String colorString = matcher.group(2);
        final String dashedPatternString = matcher.group(3);
        if (widthString != null) {
          if (!widthString.isEmpty()) {
            try {
              width = Integer.parseInt(matcher.group(1));
            } catch (Exception e) {
              errlog.add(I18N.getText("msg.error.mtprops.halo.width", lineNumber, widthString));
            }
          }
        }

        // Note that Color.decode() _wants_ the leading "#", otherwise it might not treat the
        // value as a hex code.
        if (colorString != null) {
          try {
            // color = Color.decode(colorString);
            color = hexRGBAToColor(colorString);
          } catch (Exception e) {
            log.info("Exception :: {} :: {}", e.toString(), e.getMessage());
            errlog.add(I18N.getText("msg.error.mtprops.halo.color", lineNumber, colorString));
          }
        }

        // a supplied dash pattern is to be used for the BasicStroke's float[] dash array
        if (dashedPatternString != null) {
          for (String i : StringUtil.split(dashedPatternString, ",")) {
            try {
              dashedPattern.add(Float.parseFloat(i));
            } catch (NumberFormatException e) {

              errlog.add(
                  I18N.getText(
                      "msg.error.mtprops.halo.dashedPattern", lineNumber, dashedPatternString));
            }
          }
          // dashed pattern values are in pairs, however if an odd number > 2 use the last value
          // as the dash phase, however if just one number is given, be helpful and add another
          // number otherwise later on the BasicStroke will complain about having nulls in the dash
          // array
          if (dashedPattern.size() == 1) {
            dashedPattern.add(1.0f);
          }
        }
      }
      // endregion

      try {
        HaloPart t =
            new HaloPart(
                new DrawableColorPaint(color),
                shape,
                width,
                dashedPattern,
                fill,
                flipHorizontal,
                flipVertical,
                angle,
                offset,
                rotate,
                scaleX,
                scaleY,
                vertices,
                mini,
                miniStart,
                miniStop,
                miniRotate,
                miniSpin);
        haloParts.add(t);
      } catch (Exception e) {
        errlog.add(I18N.getText("msg.error.mtprops.halo.haloPart", lineNumber, name));
      }
    }

    // Keep ID the same if modifying existing halos. This avoids tokens losing their halos
    // when the halo definition is modified.
    for (Halo h : originalInCategory) {
      if (name.equalsIgnoreCase(h.getName())) {
        assert h.getId() != null;
        id = h.getId();
        break;
      }
    }

    ownerOnly = !gmOnly && ownerOnly;
    return new Halo(
        id,
        name,
        gmOnly,
        ownerOnly,
        inner,
        facingWithToken,
        flipWithToken,
        scaleWithToken,
        haloParts);
  }

  /**
   * Convert a color to a Hex string, with or without the Alpha channel as needed.
   *
   * <p>N.B. For color alpha/transparency, java {@link Color} handles this the format ARGB, but for
   * user input/display the #RGBA format is more intuitive and has more widespread adaption, so we
   * also need to convert between the two.
   *
   * @param dcp the color object
   * @return hex string representation of the color either in the format #000000 for RGB or
   *     #000000AA for RGBA
   */
  private String colorToHexRGBA(DrawableColorPaint dcp) {
    String hexRGBA;
    if ((dcp.getColor() & 0xFF000000) == 0xFF000000) {
      // no transparency, show as an RGB hex color
      hexRGBA = String.format("#%06x", dcp.getColor() & 0x00FFFFFF).toLowerCase();
    } else {
      // has transparency, get ARGB hex color
      String hexARGB = String.format("#%08x", dcp.getColor()).toUpperCase();
      // ARGB to RGBA
      hexRGBA = hexARGB.charAt(0) + hexARGB.substring(3, 9) + hexARGB.substring(1, 3);
    }
    return hexRGBA;
  }

  /**
   * Convert an RGB (or RGBA) hex string into a color.
   *
   * <p>N.B. For color alpha/transparency, java {@link Color} handles this the format ARGB, but for
   * user input/display the #RGBA format is more intuitive and has more widespread adaption, so we
   * also need to convert between the two.
   *
   * @param hexRGBA in the format #000000 for RGB or #000000AA for RGBA
   * @return a color with or without Alpha
   */
  private Color hexRGBAToColor(String hexRGBA) {
    Color color;
    try {
      // assume some transparency specified
      color =
          new Color(
              Integer.parseInt(hexRGBA.substring(1, 3), 16), // Red
              Integer.parseInt(hexRGBA.substring(3, 5), 16), // Green
              Integer.parseInt(hexRGBA.substring(5, 7), 16), // Blue
              Integer.parseInt(hexRGBA.substring(7, 9), 16)); // Alpha
    } catch (Exception e) {
      // no transparency specified
      color = Color.decode(hexRGBA);
    }
    return color;
  }
}
