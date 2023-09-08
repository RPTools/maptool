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
package net.rptools.dicelib.expression.function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeColor;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** This class implements the dice rolling functions for genesys / starwars genesys systems. */
public class GeneSysDice extends AbstractFunction {

  /** Enumeration of the possible results of a die roll. */
  enum ResultType {
    /** A single success. */
    SUCCESS(1, 0, 0, 0, 0, 0, 0, 0, "s"),
    /** A single failure. */
    FAILURE(0, 1, 0, 0, 0, 0, 0, 0, "f"),
    /** A single advantage. */
    ADVANTAGE(0, 0, 1, 0, 0, 0, 0, 0, "a"),
    /** A single threat. */
    THREAT(0, 0, 0, 1, 0, 0, 0, 0, "h"),
    /** A single triumph. */
    TRIUMPH(1, 0, 0, 0, 1, 0, 0, 0, "t"),
    /** A single despair. */
    DESPAIR(0, 1, 0, 0, 0, 1, 0, 0, "d"),
    /* A single light side point. */
    LIGHT(0, 0, 0, 0, 0, 0, 1, 0, "Z"),
    /** A single dark side point. */
    DARK(0, 0, 0, 0, 0, 0, 0, 1, "z"),
    /** No result. */
    NONE(0, 0, 0, 0, 0, 0, 0, 0, " "),
    /** A single success and a single advantage. */
    SUCCESS_ADVANTAGE(1, 0, 1, 0, 0, 0, 0, 0, "sa"),
    /** Two Advantages. */
    ADVANTAGE_ADVANTAGE(0, 0, 2, 0, 0, 0, 0, 0, "aa"),
    /** Two Successes. */
    SUCCESS_SUCCESS(2, 0, 0, 0, 0, 0, 0, 0, "ss"),
    /* A single failure and a single threat. */
    FAILURE_THREAT(0, 1, 0, 1, 0, 0, 0, 0, "fh"),
    /** Two Failures. */
    FAILURE_FAILURE(0, 2, 0, 0, 0, 0, 0, 0, "ff"),
    /** Two Threats. */
    THREAT_THREAT(0, 0, 0, 2, 0, 0, 0, 0, "hh"),
    /** Two Light Side Points. */
    LIGHT_LIGHT(0, 0, 0, 0, 0, 0, 2, 0, "ZZ"),
    /** Two Dark Side Points. */
    DARK_DARK(0, 0, 0, 0, 0, 0, 0, 2, "zz");

    /** The number of successes this result represents. */
    private final int success;
    /** The number of failures this result represents. */
    private final int failure;
    /** The number of advantages this result represents. */
    private final int advantage;
    /** The number of threats this result represents. */
    private final int threat;
    /** The number of triumphs this result represents. */
    private final int triumph;
    /** The number of despairs this result represents. */
    private final int despair;
    /** The number of light side points this result represents. */
    private final int light;
    /** The number of dark side points this result represents. */
    private final int dark;

    /** The font characters that represent this result. */
    private final String fontCharacters;

    /**
     * Constructor.
     *
     * @param success the number of successes this result represents.
     * @param failure the number of failures this result represents.
     * @param advantage the number of advantages this result represents.
     * @param threat the number of threats this result represents.
     * @param triumph the number of triumphs this result represents.
     * @param despair the number of despairs this result represents.
     * @param light the number of light side points this result represents.
     * @param dark the number of dark side points this result represents.
     * @param fontCharacters the font characters that represent this result.
     */
    ResultType(
        int success,
        int failure,
        int advantage,
        int threat,
        int triumph,
        int despair,
        int light,
        int dark,
        String fontCharacters) {
      this.success = success;
      this.failure = failure;
      this.advantage = advantage;
      this.threat = threat;
      this.triumph = triumph;
      this.despair = despair;
      this.light = light;
      this.dark = dark;
      this.fontCharacters = fontCharacters;
    }

    /**
     * Get the number of successes this result represents.
     *
     * @return the number of successes this result represents.
     */
    public int getSuccess() {
      return success;
    }

    /**
     * Get the number of failures this result represents.
     *
     * @return the number of failures this result represents.
     */
    public int getFailure() {
      return failure;
    }

    /**
     * Get the number of advantages this result represents.
     *
     * @return the number of advantages this result represents.
     */
    public int getAdvantage() {
      return advantage;
    }

    /**
     * Get the number of threats this result represents.
     *
     * @return the number of threats this result represents.
     */
    public int getThreat() {
      return threat;
    }

    /**
     * Get the number of triumphs this result represents.
     *
     * @return the number of triumphs this result represents.
     */
    public int getTriumph() {
      return triumph;
    }

    /**
     * Get the number of despairs this result represents.
     *
     * @return the number of despairs this result represents.
     */
    public int getDespair() {
      return despair;
    }

    /**
     * Get the number of light side points this result represents.
     *
     * @return the number of light side points this result represents.
     */
    public int getLight() {
      return light;
    }

    /**
     * Get the number of dark side points this result represents.
     *
     * @return the number of dark side points this result represents.
     */
    public int getDark() {
      return dark;
    }
  }

  /** Enumeration of the possible dice types. */
  private enum DiceType {
    /** The boost die. */
    BOOST(
        "b",
        0,
        List.of(
            ResultType.NONE,
            ResultType.NONE,
            ResultType.SUCCESS,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.ADVANTAGE_ADVANTAGE,
            ResultType.ADVANTAGE)),

    /** The setback die. */
    SETBACK(
        "s",
        1,
        List.of(
            ResultType.NONE,
            ResultType.NONE,
            ResultType.FAILURE,
            ResultType.FAILURE,
            ResultType.THREAT,
            ResultType.THREAT)),
    /** The ability die. */
    ABILITY(
        "a",
        2,
        List.of(
            ResultType.NONE,
            ResultType.SUCCESS,
            ResultType.SUCCESS,
            ResultType.SUCCESS_SUCCESS,
            ResultType.ADVANTAGE,
            ResultType.ADVANTAGE,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.ADVANTAGE_ADVANTAGE)),
    /** The difficulty die. */
    DIFFICULTY(
        "d",
        3,
        List.of(
            ResultType.NONE,
            ResultType.FAILURE,
            ResultType.FAILURE_FAILURE,
            ResultType.THREAT,
            ResultType.THREAT,
            ResultType.THREAT,
            ResultType.THREAT_THREAT,
            ResultType.FAILURE_THREAT)),
    /** The proficiency die. */
    PROFICIENCY(
        "p",
        4,
        List.of(
            ResultType.NONE,
            ResultType.SUCCESS,
            ResultType.SUCCESS,
            ResultType.SUCCESS_SUCCESS,
            ResultType.SUCCESS_SUCCESS,
            ResultType.ADVANTAGE,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.ADVANTAGE_ADVANTAGE,
            ResultType.ADVANTAGE_ADVANTAGE,
            ResultType.TRIUMPH)),
    /* The challenge die. */
    CHALLENGE(
        "c",
        5,
        List.of(
            ResultType.NONE,
            ResultType.FAILURE,
            ResultType.FAILURE,
            ResultType.FAILURE_FAILURE,
            ResultType.FAILURE_FAILURE,
            ResultType.THREAT,
            ResultType.THREAT,
            ResultType.FAILURE_THREAT,
            ResultType.FAILURE_THREAT,
            ResultType.THREAT_THREAT,
            ResultType.THREAT_THREAT,
            ResultType.DESPAIR)),
    /** The force die. */
    FORCE(
        "f",
        6,
        List.of(
            ResultType.DARK,
            ResultType.DARK,
            ResultType.DARK,
            ResultType.DARK,
            ResultType.DARK,
            ResultType.DARK,
            ResultType.DARK_DARK,
            ResultType.LIGHT,
            ResultType.LIGHT,
            ResultType.LIGHT_LIGHT,
            ResultType.LIGHT_LIGHT,
            ResultType.LIGHT_LIGHT));

    /** The sides of the die. */
    private final List<ResultType> sides;

    /** The pattern used to represent this die. */
    private final String diePattern;

    /** The sort order of the die when grouped, */
    private final int groupSort;

    /**
     * Constructor.
     *
     * @param diePattern the pattern used to represent this die.
     * @param groupSort the sort order of the die when grouped.
     * @param sides the sides of the die.
     */
    DiceType(String diePattern, int groupSort, List<ResultType> sides) {
      this.sides = sides;
      this.groupSort = groupSort;
      this.diePattern = diePattern;
    }

    /**
     * Get the pattern used to represent this die.
     *
     * @return the pattern used to represent this die.
     */
    public String getDiePattern() {
      return diePattern;
    }

    /**
     * Get the number of sides on this die.
     *
     * @return the number of sides on this die.
     */
    public int getSides() {
      return sides.size();
    }

    /**
     * Roll the die.
     *
     * @return the result of the roll.
     */
    public ResultType roll() {
      return getSide(DiceHelper.rollDice(1, sides.size()) - 1);
    }

    /**
     * Get the result of a roll of the die.
     *
     * @param side the side of the die to get the result for.
     * @return the result of the roll.
     */
    public ResultType getSide(int side) {
      return sides.get(side);
    }

    /**
     * Get the sort order of the die when grouped.
     *
     * @return the sort order of the die when grouped.
     */
    public int getGroupSort() {
      return groupSort;
    }
  }

  /** Map of font names for the different systems. */
  private static final Map<String, String> GS_FONT_NAME_MAP =
      Map.of("swgenesys", "EotE Symbol", "genesys", "Genesys Glyphs and Dice");

  /** Map of variable names for the different systems. */
  private static final Map<String, String> ROLL_VARIABLE_MAP =
      Map.of("swgenesys", "#lastSWResult", "genesys", "#lastGSResult");

  /** Constructor. */
  public GeneSysDice() {
    super(
        0,
        1,
        false,
        "swgenesys",
        "genesys",
        "swgenesyslastdetails",
        "genesyslastdetails",
        "swgenesyslastrolls",
        "genesyslastrolls",
        "swgenesyslastgrouped",
        "genesyslastgrouped");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    return switch (functionName.toLowerCase()) {
      case "swgenesys", "genesys" -> performRoll(functionName, resolver, parameters);
      case "swgenesyslastdetails", "genesyslastdetails" -> returnDetails(
          functionName.toLowerCase().replace("lastdetails", ""), resolver);
      case "swgenesyslastrolls", "genesyslastrolls" -> renderRolls(
          functionName.toLowerCase().replace("lastrolls", ""), resolver);
      case "swgenesyslastgrouped", "genesyslastgrouped" -> renderGrouped(
          functionName.toLowerCase().replace("lastgrouped", ""), resolver);
      default -> // Should never happen
      throw new ParserException("Invalid function name: " + functionName);
    };
  }

  /**
   * Render the results of a roll.
   *
   * @param functionName the name of the function.
   * @param resolver the variable resolver.
   * @return the rendered results.
   * @throws ParserException if an error occurs.
   */
  private String renderGrouped(String functionName, VariableResolver resolver)
      throws ParserException {
    var rollResults = (JsonObject) resolver.getVariable(ROLL_VARIABLE_MAP.get(functionName));
    var rollsMap = new HashMap<String, List<String>>();
    for (var entry : rollResults.get("rolls").getAsJsonObject().entrySet()) {
      var dieName = entry.getKey();
      var rolls =
          StreamSupport.stream(entry.getValue().getAsJsonArray().spliterator(), false)
              .map(r -> r.getAsString().toLowerCase())
              .toList();
      rollsMap.put(dieName, rolls);
    }
    return renderGrouped(functionName, rollsMap);
  }

  /**
   * Render the results of a roll to a string.
   *
   * @param functionName the name of the function.
   * @param rolls the rolls to render.
   * @return the rendered results.
   */
  private String renderGrouped(String functionName, Map<String, List<String>> rolls) {
    var gray = ThemeSupport.getThemeColorHexString(ThemeColor.GREY);
    var sb = new StringBuilder();
    sb.append("<span style='color:").append(gray).append("'>");
    var dieTypes =
        Arrays.stream(DiceType.values())
            .sorted(Comparator.comparingInt(DiceType::getGroupSort))
            .toList();
    boolean first = true;
    for (var dt : dieTypes) {
      var dieName = dt.name().toLowerCase();
      if (rolls.containsKey(dieName)) {
        if (!first) {
          sb.append(", ");
        }
        first = false;
        sb.append(dieName).append(": ");
        var dieRolls =
            rolls.get(dieName).stream().map(r -> ResultType.valueOf(r.toUpperCase())).toList();
        sb.append(renderRolls(functionName, dieRolls));
      }
      sb.append("</span>");
    }
    return sb.toString();
  }

  /**
   * Render the results of a roll to a string.
   *
   * @param functionName the name of the function.
   * @param resolver the variable resolver.
   * @return the rendered results.
   * @throws ParserException if an error occurs.
   */
  private String renderRolls(String functionName, VariableResolver resolver)
      throws ParserException {
    var rollResults = (JsonObject) resolver.getVariable(ROLL_VARIABLE_MAP.get(functionName));
    var rolls =
        StreamSupport.stream(
                rollResults
                    .get("rolls")
                    .getAsJsonObject()
                    .get("all")
                    .getAsJsonArray()
                    .spliterator(),
                false)
            .map(r -> ResultType.valueOf(r.getAsString().toUpperCase()))
            .toList();
    return renderRolls(functionName, rolls);
  }

  /**
   * Return the details of the last roll.
   *
   * @param functionName the name of the function.
   * @param resolver the variable resolver.
   * @return the details of the last roll.
   * @throws ParserException if an error occurs.
   */
  private JsonObject returnDetails(String functionName, VariableResolver resolver)
      throws ParserException {
    return (JsonObject) resolver.getVariable(ROLL_VARIABLE_MAP.get(functionName));
  }

  /**
   * Perform a roll.
   *
   * @param functionName the name of the function.
   * @param resolver the variable resolver.
   * @param parameters the parameters to the function.
   * @return the rendered results.
   * @throws ParserException if an error occurs.
   */
  private String performRoll(
      String functionName, VariableResolver resolver, List<Object> parameters)
      throws ParserException {

    var diceString = parameters.get(0).toString().toLowerCase();

    var pattern = Pattern.compile("(\\S)(\\d+)");
    var matcher = pattern.matcher(diceString);
    var results = new ArrayList<ResultType>();
    var individualResults = new HashMap<String, List<String>>();

    while (matcher.find()) {
      var dieType = matcher.group(1);
      var dieCount = Integer.parseInt(matcher.group(2));
      var die =
          Arrays.stream(DiceType.values())
              .filter(dt -> dt.diePattern.equalsIgnoreCase(dieType))
              .findFirst()
              .get();
      for (int i = 0; i < dieCount; i++) {
        var roll = die.roll();
        results.add(roll);
        var dieName = die.name().toLowerCase();
        individualResults.putIfAbsent(dieName, new ArrayList<>());
        individualResults.get(dieName).add(roll.name().toLowerCase());
      }
    }

    individualResults.put("all", results.stream().map(r -> r.name().toLowerCase()).toList());

    // Set variable with result
    var resultMap = new HashMap<String, Integer>();
    for (var result : results) {
      var resultString = result.toString().toLowerCase();
      resultMap.put(resultString, resultMap.getOrDefault(resultString, 0) + 1);
    }

    int successCount = 0;
    int failureCount = 0;
    int advantageCount = 0;
    int threatCount = 0;
    int triumphCount = 0;
    int despairCount = 0;
    int lightCount = 0;
    int darkCount = 0;

    for (var result : results) {
      successCount += result.getSuccess();
      failureCount += result.getFailure();
      advantageCount += result.getAdvantage();
      threatCount += result.getThreat();
      triumphCount += result.getTriumph();
      despairCount += result.getDespair();
      lightCount += result.getLight();
      darkCount += result.getDark();
    }

    var counters = new HashMap<String, Integer>();
    counters.put("success", successCount);
    counters.put("failure", failureCount);
    counters.put("advantage", advantageCount);
    counters.put("threat", threatCount);
    counters.put("triumph", triumphCount);
    counters.put("despair", despairCount);
    if (lightCount > 0) {
      counters.put("light", lightCount);
    }
    if (darkCount > 0) {
      counters.put("dark", darkCount);
    }

    var result = new HashMap<String, Integer>();
    result.put("success", successCount - failureCount);
    result.put("advantage", advantageCount - threatCount);
    result.put("triumph", triumphCount);
    result.put("despair", despairCount);
    if (lightCount > 0 || darkCount > 0) {
      result.put("force", lightCount - darkCount);
    }

    var gson = new Gson().toJsonTree(resultMap).getAsJsonObject();
    gson.add("rolls", new Gson().toJsonTree(individualResults).getAsJsonObject());
    gson.add("counters", new Gson().toJsonTree(counters).getAsJsonObject());
    gson.add("result", new Gson().toJsonTree(result).getAsJsonObject());

    resolver.setVariable(ROLL_VARIABLE_MAP.get(functionName), gson);

    return renderRolls(functionName, results);
  }

  /**
   * Render the results of a roll to a string.
   *
   * @param functionName the name of the function.
   * @param results the results to render.
   * @return the rendered results.
   */
  private String renderRolls(String functionName, List<ResultType> results) {
    var gray = ThemeSupport.getThemeColorHexString(ThemeColor.GREY);

    var sb = new StringBuilder();
    sb.append("<font face='")
        .append(GS_FONT_NAME_MAP.get(functionName))
        .append("' size ='+1' color='")
        .append(gray)
        .append("'>");
    for (var result : results) {
      sb.append(result.fontCharacters);
    }
    sb.append("</font>");
    return sb.toString();
  }
}
