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
package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/*
 * This software Copyright by the RPTools.net development team, and licensed under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this source Code. If not, see <http://www.gnu.org/licenses/>
 */
public class MathFunctions extends AbstractFunction {

  private static MathFunctions instance = new MathFunctions();

  private MathFunctions() {
    super(
        0,
        UNLIMITED_PARAMETERS,
        "math.abs",
        "math.ceil",
        "math.floor",
        "math.cos",
        "math.cos_r",
        "math.sin",
        "math.sin_r",
        "math.tan",
        "math.tan_r",
        "math.acos",
        "math.acos_r",
        "math.asin",
        "math.asin_r",
        "math.atan",
        "math.atan_r",
        "math.atan2",
        "math.atan2_r",
        "math.cbrt",
        "math.cuberoot",
        "math.hypot",
        "math.hypotenuse",
        "math.log",
        "math.log10",
        "math.min",
        "math.max",
        "math.mod",
        "math.pow",
        "math.sqrt",
        "math.squareroot",
        "math.toRadians",
        "math.toDegrees",
        "math.pi",
        "math.e",
        "math.isOdd",
        "math.isEven",
        "math.isInt");
  }

  public static MathFunctions getInstance() {
    return instance;
  }

  private List<BigDecimal> getNumericParams(
      List<Object> param, int minParams, int maxParams, String functionName)
      throws ParserException {
    if (minParams == maxParams) {
      if (param.size() != minParams) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, minParams, param.size()));
      }
    } else if (param.size() < minParams) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, minParams, param.size()));
    } else if (maxParams != UNLIMITED_PARAMETERS && param.size() > maxParams) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, maxParams, param.size()));
    }

    int i = 0;
    List<BigDecimal> outVals = new ArrayList<>();
    for (Object o : param) {
      if (o instanceof BigDecimal) {
        outVals.add((BigDecimal) o);
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeN", functionName, i, o.toString()));
      }
    }

    return outVals;
  }

  private boolean isInteger(BigDecimal num) {
    // Quick check first
    if (num.scale() <= 0) {
      return true;
    }

    // Otherwise we have to go for a slower check
    return num.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> param)
      throws ParserException {
    if ("math.abs".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.abs(nparam.get(0).doubleValue()));
    } else if ("math.ceil".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.ceil(nparam.get(0).doubleValue()));
    } else if ("math.floor".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.floor(nparam.get(0).doubleValue()));
    } else if ("math.cos".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.cos(Math.toRadians(nparam.get(0).doubleValue())));
    } else if ("math.cos_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.cos(nparam.get(0).doubleValue()));
    } else if ("math.sin".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.sin(Math.toRadians(nparam.get(0).doubleValue())));
    } else if ("math.sin_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.sin(nparam.get(0).doubleValue()));
    } else if ("math.tan".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.tan(Math.toRadians(nparam.get(0).doubleValue())));
    } else if ("math.tan_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.tan(nparam.get(0).doubleValue()));
    } else if ("math.acos".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.toDegrees(Math.acos(nparam.get(0).doubleValue())));
    } else if ("math.acos_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.acos(nparam.get(0).doubleValue()));
    } else if ("math.asin".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.toDegrees(Math.asin(nparam.get(0).doubleValue())));
    } else if ("math.asin_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.asin(nparam.get(0).doubleValue()));
    } else if ("math.atan".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.toDegrees(Math.atan(nparam.get(0).doubleValue())));
    } else if ("math.atan_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.atan(nparam.get(0).doubleValue()));
    } else if ("math.atan2".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, 2, functionName);
      return BigDecimal.valueOf(
          Math.toDegrees(Math.atan2(nparam.get(0).doubleValue(), nparam.get(1).doubleValue())));
    } else if ("math.atan2_r".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, 2, functionName);
      return BigDecimal.valueOf(
          Math.atan2(nparam.get(0).doubleValue(), nparam.get(1).doubleValue()));
    } else if ("math.cbrt".equals(functionName) || "math.cuberoot".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.cbrt(nparam.get(0).doubleValue()));
    } else if ("math.hypot".equals(functionName) || "math.hypotenuse".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, 2, functionName);
      return BigDecimal.valueOf(
          Math.hypot(nparam.get(0).doubleValue(), nparam.get(1).doubleValue()));
    } else if ("math.log".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.log(nparam.get(0).doubleValue()));
    } else if ("math.log10".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.log10(nparam.get(0).doubleValue()));
    } else if ("math.min".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, UNLIMITED_PARAMETERS, functionName);
      double minVal = nparam.get(0).doubleValue();
      for (BigDecimal n : nparam) {
        double val = n.doubleValue();
        if (val < minVal) {
          minVal = val;
        }
      }
      return BigDecimal.valueOf(minVal);
    } else if ("math.max".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, UNLIMITED_PARAMETERS, functionName);
      double maxVal = nparam.get(0).doubleValue();
      for (BigDecimal n : nparam) {
        double val = n.doubleValue();
        if (val > maxVal) {
          maxVal = val;
        }
      }
      return BigDecimal.valueOf(maxVal);
    } else if ("math.mod".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, 2, functionName);
      if (!isInteger(nparam.get(0))) {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeI", functionName, 1, nparam.get(0)));
      }

      if (!isInteger(nparam.get(1))) {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeI", functionName, 2, nparam.get(1)));
      }

      return BigDecimal.valueOf(nparam.get(0).intValue() % nparam.get(1).intValue());
    } else if ("math.pow".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 2, 2, functionName);
      return BigDecimal.valueOf(Math.pow(nparam.get(0).doubleValue(), nparam.get(1).doubleValue()));
    } else if ("math.random".equals(functionName)) {
      if (param.size() > 0) {
        throw new ParserException(
            I18N.getText("macro.function.general.wrongNumParam", functionName, 0, param.size()));
      }
      return BigDecimal.valueOf(Math.random());
    } else if ("math.sqrt".equals(functionName) || "math.squareroot".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.sqrt(nparam.get(0).doubleValue()));
    } else if ("math.toRadians".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.toRadians(nparam.get(0).doubleValue()));
    } else if ("math.toRadians".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.toRadians(nparam.get(0).doubleValue()));
    } else if ("math.toDegrees".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return BigDecimal.valueOf(Math.toDegrees(nparam.get(0).doubleValue()));
    } else if ("math.pi".equals(functionName)) {
      if (param.size() > 0) {
        throw new ParserException(
            I18N.getText("macro.function.general.wrongNumParam", functionName, 0, param.size()));
      }
      return BigDecimal.valueOf(Math.PI);
    } else if ("math.e".equals(functionName)) {
      if (param.size() > 0) {
        throw new ParserException(
            I18N.getText("macro.function.general.wrongNumParam", functionName, 0, param.size()));
      }
      return BigDecimal.valueOf(Math.E);
    } else if ("math.isEven".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      if (!isInteger(nparam.get(0))) {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeI", functionName, 1, nparam.get(0)));
      }

      return nparam.get(0).remainder(BigDecimal.valueOf(2)).equals(BigDecimal.ZERO)
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    } else if ("math.isOdd".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      if (!isInteger(nparam.get(0))) {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeI", functionName, 1, nparam.get(0)));
      }

      return nparam.get(0).remainder(BigDecimal.valueOf(2)).equals(BigDecimal.ZERO)
          ? BigDecimal.ZERO
          : BigDecimal.ONE;
    } else if ("math.isInt".equals(functionName)) {
      List<BigDecimal> nparam = getNumericParams(param, 1, 1, functionName);
      return isInteger(nparam.get(0)) ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    return "";
  }
}
