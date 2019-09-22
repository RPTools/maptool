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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base64 functions to encode/decode strings.
 *
 * <p>Functions take a string parameter and returns a string.
 *
 * <p>RESTful API's often use Base64 encoding in the payload for file contents and as such may be
 * useful to compliment the RESTful macro functions. GitHub API is one such example.
 */
public class Base64Functions extends AbstractFunction {
  private static final Logger log = LogManager.getLogger(Base64Functions.class);

  private static final Base64Functions instance = new Base64Functions();

  private Base64Functions() {
    super(1, 1, "base64.encode", "base64.decode");
  }

  public static Base64Functions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    checkParameters(functionName, parameters, 1, 1);

    if (functionName.equalsIgnoreCase("base64.encode"))
      return base64Encode(functionName, parameters);

    if (functionName.equalsIgnoreCase("base64.decode"))
      return base64Decode(functionName, parameters);
    else
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Encodes passed in string to Base64
   *
   * @param functionName
   * @param parameters a list, with the message as the first element
   * @return Base64 encoded string
   * @throws ParserException
   */
  private Object base64Encode(String functionName, List<Object> parameters) throws ParserException {
    byte[] message = parameters.get(0).toString().getBytes(StandardCharsets.UTF_8);

    return Base64.getEncoder().encodeToString(message);
  }

  /**
   * Decodes a passed in string from Base64
   *
   * @param functionName
   * @param parameters a list of parameters with string to decode as first element.
   * @return String decoded from a Base64 encoded string
   * @throws ParserException
   */
  private Object base64Decode(String functionName, List<Object> parameters) throws ParserException {
    byte[] decoded = Base64.getDecoder().decode(parameters.get(0).toString());

    return new String(decoded, StandardCharsets.UTF_8);
  }

  /**
   * @param functionName the name of the function
   * @param parameters passed into the function call
   * @param min number of parameters required
   * @param max number of parameters required
   * @throws ParserException
   */
  private void checkParameters(String functionName, List<Object> parameters, int min, int max)
      throws ParserException {

    if (min == max) {
      if (parameters.size() != max)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, max, parameters.size()));

    } else {
      if (parameters.size() < min)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, min, parameters.size()));

      if (parameters.size() > max)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, max, parameters.size()));
    }
  }
}
