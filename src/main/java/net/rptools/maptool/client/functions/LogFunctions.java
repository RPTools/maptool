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

import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Log functions to dynamically set log levels and configurations
 *
 * <p>TBD
 */
public class LogFunctions extends AbstractFunction {
  private static final Logger log = LogManager.getLogger(LogFunctions.class);
  //  private static final Logger logger = LogManager.getLogger("My Logger");

  private static final LogFunctions instance = new LogFunctions();

  private LogFunctions() {
    super(0, 2, "log.setLevel", "log.getLoggers", "log.openConsole");
  }

  public static LogFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    if (!MapTool.getParser().isMacroPathTrusted())
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

    if (functionName.equalsIgnoreCase("log.setLevel")) return setLogLevel(functionName, parameters);
    else if (functionName.equalsIgnoreCase("log.getLoggers"))
      return getLoggers(functionName, parameters);
    else if (functionName.equalsIgnoreCase("log.openConsole"))
      return openConsole(functionName, parameters);
    else
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Dynamically set the log level for macros
   *
   * @param functionName
   * @param parameters include URL, headers (optional) and if full response is requested (optional)
   * @return HTTP response as JSON (if full response) or server response, usually JSON but can be
   *     XML, HTML, or other formats.
   * @throws ParserException
   */
  private Object setLogLevel(String functionName, List<Object> parameters) throws ParserException {
    checkParameters(functionName, parameters, 2, 3);

    log.debug("Macro function setLogLevel called with " + parameters.toString());

    String loggerName = parameters.get(0).toString();

    // Convert the parameter string to a logger Level and return false if no match is found
    Level newLevel = Level.getLevel(parameters.get(1).toString());
    if (newLevel == null) return BigDecimal.ZERO;

    if (parameters.size() == 3) {
      String logPattern = parameters.get(2).toString();

      //      ConfigurationBuilder<BuiltConfiguration> builder =
      // ConfigurationBuilderFactory.newConfigurationBuilder();
      //      builder.setStatusLevel(Level.ERROR);
      //      builder.setConfigurationName("BuilderTest");
      //      builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT,
      // Filter.Result.NEUTRAL).addAttribute("level", Level.DEBUG));
      //      builder.get
      //      AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout",
      // "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
      //      appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d
      // Thread:[%t] %-5level: MSG: %msg%n%throwable"));
      //      appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY,
      // Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
      //
      //      builder.add(appenderBuilder);
      //      builder.add(builder.newLogger("org.apache.logging.log4j",
      // Level.DEBUG).add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
      //      builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout")));
      //      Configurator.initialize(builder.build());
      //
      //      appenderBuilder.add(
      //          builder
      //              .newLayout("PatternLayout")
      //              .addAttribute("pattern", logPattern));

    }

    Configurator.setLevel(loggerName, newLevel);

    return newLevel.toString();
  }

  private Object getLoggers(String functionName, List<Object> parameters) throws ParserException {
    checkParameters(functionName, parameters, 0, 0);

    LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
    //    Map<String, String> loggerLevels = new HashMap<>();
    List<LoggerResponse> loggerLevels = new ArrayList<>();

    for (Logger logger : logContext.getLoggers())
      try {
        loggerLevels.add(new LoggerResponse(logger.getName(), logger.getLevel().name()));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    Gson gson = new Gson();
    String jsonResponse = gson.toJson(loggerLevels);

    log.debug(jsonResponse);

    return jsonResponse;
  }

  private Object openConsole(String functionName, List<Object> parameters) {

    return null;
  }

  /**
   * @param function's name
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

  /*
   * A POJO to hold an Logger Config to marshal as a nice JSON object
   */
  private final class LoggerResponse {
    private final String name;
    private final String level;

    public LoggerResponse(String name, String level) throws IOException {
      this.name = name;
      this.level = level;
    }
  }
}
