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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeColor;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class GeneSysDice extends AbstractFunction {

  enum ResultType {
    SUCCESS(1, 0, 0, 0, 0, 0, 0, 0, "s"),
    FAILURE(0, 1, 0, 0, 0, 0, 0, 0, "f"),
    ADVANTAGE(0, 0, 1, 0, 0, 0, 0, 0, "a"),
    THREAT(0, 0, 0, 1, 0, 0, 0, 0, "h"),
    TRIUMPH(1, 0, 0, 0, 1, 0, 0, 0, "t"),
    DESPAIR(0, 1, 0, 0, 0, 1, 0, 0, "d"),
    LIGHT(0, 0, 0, 0, 0, 0, 1, 0, "Z"),
    DARK(0, 0, 0, 0, 0, 0, 0, 1, "z"),
    NONE(0, 0, 0, 0, 0, 0, 0, 0, " "),
    SUCCESS_ADVANTAGE(1, 0, 1, 0, 0, 0, 0, 0, "sa"),
    ADVANTAGE_ADVANTAGE(0, 0, 2, 0, 0, 0, 0, 0, "aa"),
    SUCCESS_SUCCESS(2, 0, 0, 0, 0, 0, 0, 0, "ss"),
    FAILURE_THREAT(0, 1, 0, 1, 0, 0, 0, 0, "fh"),
    FAILURE_FAILURE(0, 2, 0, 0, 0, 0, 0, 0, "ff"),
    THREAT_THREAT(0, 0, 0, 2, 0, 0, 0, 0, "hh"),
    LIGHT_LIGHT(0, 0, 0, 0, 0, 0, 2, 0, "ZZ"),
    DARK_DARK(0, 0, 0, 0, 0, 0, 0, 2, "zz");

    private final int success;
    private final int failure;
    private final int advantage;
    private final int threat;
    private final int triumph;
    private final int despair;
    private final int light;
    private final int dark;

    private final String fontCharacters;

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

    public int getSuccess() {
      return success;
    }

    public int getFailure() {
      return failure;
    }

    public int getAdvantage() {
      return advantage;
    }

    public int getThreat() {
      return threat;
    }

    public int getTriumph() {
      return triumph;
    }

    public int getDespair() {
      return despair;
    }

    public int getLight() {
      return light;
    }

    public int getDark() {
      return dark;
    }
  }

  private enum DiceType {
    BOOST(
        "b",
        List.of(
            ResultType.NONE,
            ResultType.NONE,
            ResultType.SUCCESS,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.ADVANTAGE_ADVANTAGE,
            ResultType.ADVANTAGE)),

    SETBACK(
        "s",
        List.of(
            ResultType.NONE,
            ResultType.NONE,
            ResultType.FAILURE,
            ResultType.FAILURE,
            ResultType.THREAT,
            ResultType.THREAT)),
    ABILITY(
        "a",
        List.of(
            ResultType.NONE,
            ResultType.SUCCESS,
            ResultType.SUCCESS,
            ResultType.SUCCESS_SUCCESS,
            ResultType.ADVANTAGE,
            ResultType.ADVANTAGE,
            ResultType.SUCCESS_ADVANTAGE,
            ResultType.ADVANTAGE_ADVANTAGE)),
    DIFFICULTY(
        "d",
        List.of(
            ResultType.NONE,
            ResultType.FAILURE,
            ResultType.FAILURE_FAILURE,
            ResultType.THREAT,
            ResultType.THREAT,
            ResultType.THREAT,
            ResultType.THREAT_THREAT,
            ResultType.FAILURE_THREAT)),
    PROFICIENCY(
        "p",
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
    CHALLENGE(
        "c",
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
    FORCE(
        "f",
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

    private final List<ResultType> sides;
    private final String diePattern;

    DiceType(String diePattern, List<ResultType> sides) {
      this.sides = sides;
      this.diePattern = diePattern;
    }

    public String getDiePattern() {
      return diePattern;
    }

    public int getSides() {
      return sides.size();
    }

    public ResultType roll() {
      return getSide(DiceHelper.rollDice(1, sides.size()) - 1);
    }

    public ResultType getSide(int side) {
      return sides.get(side);
    }
    ;
  }

  private static final Map<String, String> FFG_FONT_NAME_MAP =
      Map.of("swffg", "EotE Symbol", "ffg", "Genesys Glyphs and Dice");

  private static final String BOOST_DIE_PATTERN_NAME = "boost";
  private static final String SETBACK_DIE_PATTERN_NAME = "setback";
  private static final String ABILITY_DIE_PATTERN_NAME = "ability";
  private static final String DIFFICULTY_DIE_PATTERN_NAME = "difficulty";
  private static final String PROFICIENCY_DIE_PATTERN_NAME = "proficiency";
  private static final String CHALLENGE_DIE_PATTERN_NAME = "challenge";
  private static final String FORCE_DIE_PATTERN_NAME = "force";

  public GeneSysDice() {
    super(1, 1, false, "swffg", "ffg");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
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

    var gray = ThemeSupport.getThemeColorHexString(ThemeColor.GREY);

    var sb = new StringBuilder();
    sb.append("<font face='")
        .append(FFG_FONT_NAME_MAP.get(functionName))
        .append("' size ='+1' color='")
        .append(gray)
        .append("'>");
    for (var result : results) {
      sb.append(result.fontCharacters);
    }
    sb.append("</span>");

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

    resolver.setVariable("lastFFGResult", gson);

    return sb.toString();
  }
}
