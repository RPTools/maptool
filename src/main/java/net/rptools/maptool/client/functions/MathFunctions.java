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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.ParameterException;

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
        "math.isInt",
        "math.arraySum",
        "math.arrayMin",
        "math.arrayMax",
        "math.arrayMean",
        "math.arrayMedian",
        "math.arrayProduct",
        "math.listSum",
        "math.listMin",
        "math.listMax",
        "math.listMean",
        "math.listMedian",
        "math.listProduct");
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

    List<BigDecimal> outVals = new ArrayList<>();
    for (int i = 0; i < param.size(); i++) {
      Object o = param.get(i);
      if (o instanceof BigDecimal) {
        outVals.add((BigDecimal) o);
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", functionName, i + 1, o.toString()));
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

  /**
   * Convert a single function parameter containing a non-empty JSON Array into a parallel stream of
   * numeric values.
   *
   * @param functionName the function name to use in error reporting
   * @param param the list of parameters, which should contain a single non-empty JSON array
   * @return a parallel stream of numeric values
   * @throws ParserException if too few/many params are present, if the provided param cannot be
   *     parsed as a JSON array, or if the JSON array is empty
   * @throws NumberFormatException if any element of the json array is non-numeric
   */
  static Stream<BigDecimal> getNumericValuesFromArrayParam(String functionName, List<Object> param)
      throws ParserException {
    checkParamNumber(functionName, param, 1, 1);
    JsonArray jsonArray = FunctionUtil.paramAsJsonArray(functionName, param, 0);
    if (jsonArray.size() == 0) {
      throw new ParserException(
          I18N.getText("macro.function.json.arrayCannotBeEmpty", functionName, 1));
    }
    return StreamSupport.stream(jsonArray.spliterator(), true).map(JsonElement::getAsBigDecimal);
  }

  /**
   * Convert a non-empty string list and optional delim parameter into a parallel stream of numeric
   * values.
   *
   * @param functionName the function name to use in error reporting
   * @param param the list of parameters, which should contain a non-empty string list and an
   *     optional delimiter
   * @return a parallel stream of numeric values
   * @throws ParserException if too few/many params are present, if the provided param cannot be
   *     parsed as a string list with the given delimiter, or if the string list is empty
   * @throws NumberFormatException if any element of the string list is non-numeric
   */
  static Stream<BigDecimal> getNumericValuesFromStrListParam(
      String functionName, List<Object> param) throws ParserException {
    checkParamNumber(functionName, param, 1, 2);
    String delim = (param.size() > 1) ? param.get(1).toString() : ",";
    List<String> stringList = StrListFunctions.toList(param.get(0).toString(), delim);
    if (stringList.size() == 0) {
      throw new ParserException(
          I18N.getText("macro.function.general.listCannotBeEmpty", functionName, 1));
    }
    return stringList.parallelStream().map(BigDecimal::new);
  }

  /**
   * Check the number of provided parameters, throwing a ParserException if an unacceptable number
   * is found
   *
   * @param functionName the function name to use in error reporting
   * @param param the list of parameters
   * @param minParameters the minimum number of acceptable parameters
   * @param maxParameters the maximum number of acceptable parameters
   * @throws ParserException if too few or too many parameters are present
   */
  static void checkParamNumber(
      String functionName, List<Object> param, int minParameters, int maxParameters)
      throws ParserException {
    int pCount = param == null ? 0 : param.size();
    if (minParameters == maxParameters) {
      if (pCount != maxParameters)
        throw new ParameterException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, minParameters, pCount));
    } else {
      if (pCount < minParameters)
        throw new ParameterException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, minParameters, pCount));
      if (maxParameters != UNLIMITED_PARAMETERS && pCount > maxParameters)
        throw new ParameterException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, maxParameters, pCount));
    }
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> param)
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
    } else if ("math.arraySum".equals(functionName)) {
      return streamSum(getNumericValuesFromArrayParam(functionName, param));
    } else if ("math.arrayProduct".equals(functionName)) {
      return streamProduct(getNumericValuesFromArrayParam(functionName, param));
    } else if ("math.arrayMin".equals(functionName)) {
      return streamMin(getNumericValuesFromArrayParam(functionName, param));
    } else if ("math.arrayMax".equals(functionName)) {
      return streamMax(getNumericValuesFromArrayParam(functionName, param));
    } else if ("math.arrayMean".equals(functionName)) {
      List<BigDecimal> theValues =
          getNumericValuesFromArrayParam(functionName, param).collect(Collectors.toList());
      return getMean(theValues);
    } else if ("math.arrayMedian".equals(functionName)) {
      List<BigDecimal> theValues =
          getNumericValuesFromArrayParam(functionName, param).collect(Collectors.toList());
      return getMedian(theValues);
    } else if ("math.listSum".equals(functionName)) {
      return streamSum(getNumericValuesFromStrListParam(functionName, param));
    } else if ("math.listProduct".equals(functionName)) {
      return streamProduct(getNumericValuesFromStrListParam(functionName, param));
    } else if ("math.listMin".equals(functionName)) {
      return streamMin(getNumericValuesFromStrListParam(functionName, param));
    } else if ("math.listMax".equals(functionName)) {
      return streamMax(getNumericValuesFromStrListParam(functionName, param));
    } else if ("math.listMean".equals(functionName)) {
      List<BigDecimal> theValues =
          getNumericValuesFromStrListParam(functionName, param).collect(Collectors.toList());
      return getMean(theValues);
    } else if ("math.listMedian".equals(functionName)) {
      List<BigDecimal> theValues =
          getNumericValuesFromStrListParam(functionName, param).collect(Collectors.toList());
      return getMedian(theValues);
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Calculate the sum of a stream of numbers
   *
   * @param stream a potentially parallel stream of numbers
   * @return the sum
   */
  static BigDecimal streamSum(Stream<BigDecimal> stream) {
    return stream.reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculate the product of a stream of numbers
   *
   * @param stream a potentially parallel stream of numbers
   * @return the product
   */
  static BigDecimal streamProduct(Stream<BigDecimal> stream) {
    return stream.reduce(BigDecimal.ONE, BigDecimal::multiply);
  }

  /**
   * Find the minimum value in a stream of numbers
   *
   * @param stream a potentially parallel stream of numbers
   * @return the minimum value found
   * @throws java.util.NoSuchElementException if stream contained no elements
   */
  static BigDecimal streamMin(Stream<BigDecimal> stream) {
    return stream.min(BigDecimal::compareTo).orElseThrow();
  }

  /**
   * Find the maximum value in a stream of numbers
   *
   * @param stream a potentially parallel stream of numbers
   * @return the maximum value found
   * @throws java.util.NoSuchElementException if stream contained no elements
   */
  static BigDecimal streamMax(Stream<BigDecimal> stream) {
    return stream.max(BigDecimal::compareTo).orElseThrow();
  }

  /**
   * Compute the arithmetic mean of a collection of numbers
   *
   * @param theValues collection of numbers
   * @return the mean, computed to Decimal128 precision
   * @throws ArithmeticException if the provided collection is empty
   */
  static BigDecimal getMean(Collection<BigDecimal> theValues) {
    BigDecimal total = streamSum(theValues.parallelStream());
    return total.divide(new BigDecimal(theValues.size()), MathContext.DECIMAL128);
  }

  /**
   * Compute the median value of a collection of numbers
   *
   * @param theValues collection of numbers
   * @return the median, computed to Decimal128 precision
   * @throws IndexOutOfBoundsException if the provided list is empty
   */
  static BigDecimal getMedian(List<BigDecimal> theValues) {
    Collections.sort(theValues);
    if (theValues.size() % 2 == 0) {
      // even number, median is between the 2 middle numbers
      BigDecimal d1 = theValues.get(theValues.size() / 2 - 1);
      BigDecimal d2 = theValues.get(theValues.size() / 2);
      return d1.add(d2).divide(new BigDecimal(2), MathContext.DECIMAL128);
    } else {
      // odd number, select the middle one
      return theValues.get((theValues.size() - 1) / 2);
    }
  }
}
