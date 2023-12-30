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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.drawing.DrawableColorPaint;

public class SightSyntax {
  private static final int DEFAULT_LUMENS = 100;

  public List<SightType> parse(String text) {
    final var sightList = new LinkedList<SightType>();
    final var reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
    String line;
    String toBeParsed = null, errmsg = null;
    List<String> errlog = new LinkedList<>();
    try {
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Blanks
        if (line.isEmpty() || line.indexOf(':') < 1) {
          continue;
        }
        // Parse line
        int split = line.indexOf(':');
        String label = line.substring(0, split).trim();
        String value = line.substring(split + 1).trim();

        if (label.isEmpty()) {
          continue;
        }
        // Parse Details
        double magnifier = 1;
        // If null, no personal light has been defined.
        List<Light> personalLightLights = null;

        String[] args = value.split("\\s+");
        ShapeType shape = ShapeType.CIRCLE;
        boolean scaleWithToken = false;
        double width = 0;
        int arc = 90;
        float range = 0;
        int offset = 0;

        for (String arg : args) {
          assert !arg.isEmpty(); // The split() uses "one or more spaces", removing empty strings
          try {
            shape = ShapeType.valueOf(arg.toUpperCase());
            continue;
          } catch (IllegalArgumentException iae) {
            // Expected when not defining a shape
          }
          // Scale with Token
          if (arg.equalsIgnoreCase("SCALE")) {
            scaleWithToken = true;
            continue;
          }

          try {

            if (arg.startsWith("x")) {
              toBeParsed = arg.substring(1); // Used in the catch block, below
              errmsg = "msg.error.mtprops.sight.multiplier"; // (ditto)
              magnifier = StringUtil.parseDecimal(toBeParsed);
            } else if (arg.startsWith("r")) { // XXX Why not "r=#" instead of "r#"??
              toBeParsed = arg.substring(1);
              errmsg = "msg.error.mtprops.sight.range";

              final var rangeRegex = Pattern.compile("([^#+-]*)(#[0-9a-fA-F]+)?([+-]\\d*)?");
              final var matcher = rangeRegex.matcher(toBeParsed);
              if (matcher.find()) {
                var pLightRange = 0.;
                pLightRange = StringUtil.parseDecimal(matcher.group(1));
                final var colorString = matcher.group(2);
                final var lumensString = matcher.group(3);
                // Note that Color.decode() _wants_ the leading "#", otherwise it might not treat
                // the value as a hex code.
                Color personalLightColor = null;
                if (colorString != null) {
                  personalLightColor = Color.decode(colorString);
                }
                int perRangeLumens = DEFAULT_LUMENS;
                if (lumensString != null) {
                  perRangeLumens = Integer.parseInt(lumensString, 10);
                  if (perRangeLumens == 0) {
                    errlog.add(
                        I18N.getText("msg.error.mtprops.sight.zerolumens", reader.getLineNumber()));
                    perRangeLumens = DEFAULT_LUMENS;
                  }
                }

                if (personalLightLights == null) {
                  personalLightLights = new ArrayList<>();
                }
                personalLightLights.add(
                    new Light(
                        shape,
                        0,
                        pLightRange,
                        width,
                        arc,
                        personalLightColor == null
                            ? null
                            : new DrawableColorPaint(personalLightColor),
                        perRangeLumens,
                        false,
                        false));
              } else {
                throw new ParseException(
                    String.format("Unrecognized personal light syntax: %s", arg), 0);
              }
            } else if (arg.startsWith("width=") && arg.length() > 6) {
              toBeParsed = arg.substring(6);
              errmsg = "msg.error.mtprops.sight.width";
              width = StringUtil.parseInteger(toBeParsed);
            } else if (arg.startsWith("arc=") && arg.length() > 4) {
              toBeParsed = arg.substring(4);
              errmsg = "msg.error.mtprops.sight.arc";
              arc = StringUtil.parseInteger(toBeParsed);
            } else if (arg.startsWith("distance=") && arg.length() > 9) {
              toBeParsed = arg.substring(9);
              errmsg = "msg.error.mtprops.sight.distance";
              range = StringUtil.parseDecimal(toBeParsed).floatValue();
            } else if (arg.startsWith("offset=") && arg.length() > 7) {
              toBeParsed = arg.substring(7);
              errmsg = "msg.error.mtprops.sight.offset";
              offset = StringUtil.parseInteger(toBeParsed);
            } else {
              toBeParsed = arg;
              errmsg =
                  I18N.getText(
                      "msg.error.mtprops.sight.unknownField", reader.getLineNumber(), toBeParsed);
              errlog.add(errmsg);
            }
          } catch (ParseException e) {
            assert errmsg != null;
            errlog.add(I18N.getText(errmsg, reader.getLineNumber(), toBeParsed));
          }
        }

        LightSource personalLight =
            personalLightLights == null
                ? null
                : LightSource.createPersonal(scaleWithToken, false, personalLightLights);
        SightType sight =
            new SightType(
                label, range, magnifier, shape, width, arc, offset, scaleWithToken, personalLight);

        // Store
        sightList.add(sight);
      }
    } catch (IOException ioe) {
      MapTool.showError("msg.error.mtprops.sight.ioexception", ioe);
    }
    if (!errlog.isEmpty()) {
      // Show the user a list of errors so they can (attempt to) correct all of them at once
      MapTool.showFeedback(errlog.toArray());
      errlog.clear();
      throw new IllegalArgumentException(
          "msg.error.mtprops.sight.definition"); // Don't save sights...
    }

    return sightList;
  }

  public String stringify(Map<String, SightType> sightTypeMap) {
    StringBuilder builder = new StringBuilder();
    for (SightType sight : sightTypeMap.values()) {
      builder.append(sight.getName()).append(": ");

      builder.append(sight.getShape().name().toLowerCase()).append(" ");

      switch (sight.getShape()) {
        case SQUARE, CIRCLE, GRID, HEX:
          break;
        case BEAM:
          if (sight.getWidth() != 0) {
            builder.append("width=").append(StringUtil.formatDecimal(sight.getWidth())).append(' ');
          }
          if (sight.getOffset() != 0) {
            builder
                .append("offset=")
                .append(StringUtil.formatDecimal(sight.getOffset()))
                .append(' ');
          }
          break;
        case CONE:
          if (sight.getArc() != 0) {
            builder.append("arc=").append(StringUtil.formatDecimal(sight.getArc())).append(' ');
          }
          if (sight.getOffset() != 0) {
            builder
                .append("offset=")
                .append(StringUtil.formatDecimal(sight.getOffset()))
                .append(' ');
          }
          break;
      }
      if (sight.getDistance() != 0) {
        builder
            .append("distance=")
            .append(StringUtil.formatDecimal(sight.getDistance()))
            .append(' ');
      }

      // Scale with Token
      if (sight.isScaleWithToken()) {
        builder.append("scale ");
      }
      // Multiplier
      if (sight.getMultiplier() != 1 && sight.getMultiplier() != 0) {
        builder.append("x").append(StringUtil.formatDecimal(sight.getMultiplier())).append(' ');
      }
      // Personal light
      if (sight.getPersonalLightSource() != null) {
        LightSource source = sight.getPersonalLightSource();

        for (Light light : source.getLightList()) {
          double range = light.getRadius();

          builder.append("r").append(StringUtil.formatDecimal(range));

          if (light.getPaint() != null && light.getPaint() instanceof DrawableColorPaint) {
            Color color = (Color) light.getPaint().getPaint();
            builder.append(toHex(color));
          }
          final var lumens = light.getLumens();
          if (lumens != DEFAULT_LUMENS) {
            if (lumens >= 0) {
              builder.append('+');
            }
            builder.append(Integer.toString(lumens, 10));
          }
          builder.append(' ');
        }
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  private static String toHex(Color color) {
    return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
  }
}
