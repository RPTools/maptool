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
package net.rptools.dicelib.expression;

import java.util.List;
import java.util.regex.Pattern;
import net.rptools.dicelib.expression.function.ArsMagicaStress;
import net.rptools.dicelib.expression.function.CountSuccessDice;
import net.rptools.dicelib.expression.function.DropHighestRoll;
import net.rptools.dicelib.expression.function.DropRoll;
import net.rptools.dicelib.expression.function.ExplodeDice;
import net.rptools.dicelib.expression.function.ExplodingSuccessDice;
import net.rptools.dicelib.expression.function.FudgeRoll;
import net.rptools.dicelib.expression.function.HeroKillingRoll;
import net.rptools.dicelib.expression.function.HeroRoll;
import net.rptools.dicelib.expression.function.If;
import net.rptools.dicelib.expression.function.KeepLowestRoll;
import net.rptools.dicelib.expression.function.KeepRoll;
import net.rptools.dicelib.expression.function.OpenTestDice;
import net.rptools.dicelib.expression.function.RerollDice;
import net.rptools.dicelib.expression.function.RerollDiceOnce;
import net.rptools.dicelib.expression.function.Roll;
import net.rptools.dicelib.expression.function.RollWithBounds;
import net.rptools.dicelib.expression.function.ShadowRun4Dice;
import net.rptools.dicelib.expression.function.ShadowRun4ExplodeDice;
import net.rptools.dicelib.expression.function.ShadowRun5Dice;
import net.rptools.dicelib.expression.function.ShadowRun5ExplodeDice;
import net.rptools.dicelib.expression.function.UbiquityRoll;
import net.rptools.dicelib.expression.function.advanced.AdvancedDiceRolls;
import net.rptools.parser.*;
import net.rptools.parser.transform.RegexpStringTransformer;
import net.rptools.parser.transform.StringLiteralTransformer;
import org.javatuples.Pair;

public class ExpressionParser {
  private static String[][] DICE_PATTERNS =
      new String[][] {
        // Comments
        new String[] {"//.*", ""},

        // Color hex strings #FFF or #FFFFFF or #FFFFFFFF (with alpha)
        new String[] {
          "(?<![0-9A-Za-z])#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])(?![0-9A-Za-z])",
          "0x$1$1$2$2$3$3"
        },
        new String[] {
          "(?<![0-9A-Za-z])#([0-9A-Fa-f]{6,6}(?:[0-9A-Fa-f]{2,2})?)(?![0-9A-Za-z])", "0x$1"
        },

        // drop
        new String[] {"\\b(\\d+)[dD](\\d+)[dD](\\d+)\\b", "drop($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[dD](\\d+)\\b", "drop(1, $1, $2)"},

        // drop highest
        new String[] {"\\b(\\d+)[dD](\\d+)[dD][hH](\\d+)\\b", "dropHighest($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[dD][hH](\\d+)\\b", "dropHighest(1, $1, $2)"},

        // keep
        new String[] {"\\b(\\d+)[dD](\\d+)[kK](\\d+)\\b", "keep($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[kK](\\d+)\\b", "keep(1, $1, $2)"},

        // keep lowest
        new String[] {"\\b(\\d+)[dD](\\d+)[kK][lL](\\d+)\\b", "keepLowest($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[kK][lL](\\d+)\\b", "keepLowest(1, $1, $2)"},

        // re-roll
        new String[] {"\\b(\\d+)[dD](\\d+)[rR](\\d+)\\b", "reroll($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[rR](\\d+)\\b", "reroll(1, $1, $2)"},

        // re-roll once and keep the new value
        new String[] {"\\b(\\d+)[dD](\\d+)[rR][kK](\\d+)\\b", "rerollOnce($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[rR][kK](\\d+)\\b", "rerollOnce(1, $1, $2)"},

        // re-roll once and choose the higher value
        new String[] {"\\b(\\d+)[dD](\\d+)[rR][cC](\\d+)\\b", "rerollOnce($1, $2, $3, true)"},
        new String[] {"\\b[dD](\\d+)[rR][cC](\\d+)\\b", "rerollOnce(1, $1, $2, true)"},

        // count success
        new String[] {"\\b(\\d+)[dD](\\d+)[sS](\\d+)\\b", "success($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[sS](\\d+)\\b", "success(1, $1, $2)"},

        // count success while exploding
        new String[] {"\\b(\\d+)[dD](\\d+)[eE][sS](\\d+)\\b", "explodingSuccess($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[eE][sS](\\d+)\\b", "explodingSuccess(1, $1, $2)"},
        new String[] {"\\b(\\d+)[eE][sS](\\d+)\\b", "explodingSuccess($1, 6, $2)"},
        new String[] {
          "\\b(\\d+)[dD](\\d+)[eE](\\d+)[sS](\\d+)\\b", "explodingSuccess($1, $2, $4, $3)"
        },
        new String[] {"\\b[dD](\\d+)[eE](\\d+)[sS](\\d+)\\b", "explodingSuccess(1, $1, $3, $2)"},
        new String[] {"\\b(\\d+)[eE](\\d+)[sS](\\d+)\\b", "explodingSuccess($1, 6, $3, $2)"},

        // show max while exploding
        new String[] {"\\b(\\d+)[dD](\\d+)[oO]\\b", "openTest($1, $2)"},
        new String[] {"\\b[dD](\\d+)[oO]\\b", "openTest(1, $1)"},
        new String[] {"\\b(\\d+)[oO]\\b", "openTest($1, 6)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[oO](\\d+)\\b", "openTest($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[oO](\\d+)\\b", "openTest(1, $1, $2)"},
        new String[] {"\\b(\\d+)[oO](\\d+)\\b", "openTest($1, 6, $2)"},

        // explode
        new String[] {"\\b(\\d+)[dD](\\d+)[eE]\\b", "explode($1, $2)"},
        new String[] {"\\b[dD](\\d+)[eE]\\b", "explode(1, $1)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[eE](\\d+)\\b", "explode($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[eE](\\d+)\\b", "explode(1, $1, $2)"},

        // hero
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH]\\b", "hero($1, $2)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH]\\b", "hero($1, $2)"},
        new String[] {"\\b[dD](\\d+)[hH]\\b", "hero(1, $1)"},
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[bB]\\b", "herobody($1, $2)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[bB]\\b", "herobody($1, $2)"},
        new String[] {"\\b[dD](\\d+)[bB]\\b", "herobody(1, $1)"},

        // hero killing
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling($1, $2, $3)"},
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK]\\b", "herokilling($1, $2, 0)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling($1, $2, $3)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK]\\b", "herokilling($1, $2, 0)"},
        new String[] {"\\b[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling(1, $1, $3)"},
        new String[] {"\\b[dD](\\d+)[hH][kK]\\b", "herokilling(1, $1, 0)"},

        // hero killing2
        new String[] {
          "\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2($1, $2, $3)"
        },
        new String[] {"\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK][2]\\b", "herokilling2($1, $2, 0)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2($1, $2, $3)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][kK][2]\\b", "herokilling2($1, $2, 0)"},
        new String[] {"\\b[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2(1, $1, $3)"},
        new String[] {"\\b[dD](\\d+)[hH][kK][2]\\b", "herokilling2(1, $1, 0)"},

        // hero killing multiplier
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][mM]([-+]\\d+)\\b", "heromultiplier($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[hH][mM]([-+]\\d+)\\b", "heromultiplier(1, $1, $2)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[hH][mM]\\b", "heromultiplier($1, $2, 0)"},
        new String[] {"\\b[dD](\\d+)[hH][mM]\\b", "heromultiplier(1, $1, 0)"},
        new String[] {"\\b(\\d+)[hH][mM]\\b", "heromultiplier(0, 0, $1)"},

        // dice
        new String[] {"\\b(\\d+)[dD](\\d+)\\b", "roll($1, $2)"},
        new String[] {"\\b[dD](\\d+)\\b", "roll(1, $1)"},

        // Fudge dice
        new String[] {"\\b(\\d+)[dD][fF]\\b", "fudge($1)"},
        new String[] {"\\b[dD][fF]\\b", "fudge(1)"},

        // Ubiquity dice
        new String[] {"\\b(\\d+)[dD][uU]\\b", "ubiquity($1)"},
        new String[] {"\\b[dD][uU]\\b", "ubiquity(1)"},

        // Shadowrun 4 Edge or Exploding Test
        new String[] {"\\b(\\d+)[sS][rR]4[eE][gG](\\d+)\\b", "sr4e($1, $2)"},
        new String[] {"\\b(\\d+)[sS][rR]4[eE]\\b", "sr4e($1)"},

        // Shadowrun 4 Normal Test
        new String[] {"\\b(\\d+)[sS][rR]4[gG](\\d+)\\b", "sr4($1, $2)"},
        new String[] {"\\b(\\d+)[sS][rR]4\\b", "sr4($1)"},

        // Shadowrun 5 Edge or Exploding Test
        new String[] {"\\b(\\d+)[sS][rR]5[eE][gG](\\d+)\\b", "sr5e($1, $2)"},
        new String[] {"\\b(\\d+)[sS][rR]5[eE]\\b", "sr5e($1)"},

        // Shadowrun 5 Normal Test
        new String[] {"\\b(\\d+)[sS][rR]5[gG](\\d+)\\b", "sr5($1, $2)"},
        new String[] {"\\b(\\d+)[sS][rR]5\\b", "sr5($1)"},

        // Add X, apply a maximum of Y
        new String[] {
          "\\b(\\d+)[dD](\\d+)[aA](\\d+)[uU](\\d+)\\b", "rollAddWithUpper($1, $2, $3, $4)"
        },
        new String[] {"\\b[dD](\\d+)[aA](\\d+)[uU](\\d+)\\b", "rollAddWithUpper(1, $1, $2, $3)"},

        // Add X, apply a minimum of Y
        new String[] {
          "\\b(\\d+)[dD](\\d+)[aA](\\d+)[lL](\\d+)\\b", "rollAddWithLower($1, $2, $3, $4)"
        },
        new String[] {"\\b[dD](\\d+)[aA](\\d+)[lL](\\d+)\\b", "rollAddWithLower(1, $1, $2, $3)"},

        // Subtract X, apply a maximum of Y
        new String[] {
          "\\b(\\d+)[dD](\\d+)[sS](\\d+)[uU](\\d+)\\b", "rollSubWithUpper($1, $2, $3, $4)"
        },
        new String[] {"\\b[dD](\\d+)[sS](\\d+)[uU](\\d+)\\b", "rollSubWithUpper(1, $1, $2, $3)"},

        // Subtract X, apply a minimum of Y
        new String[] {
          "\\b(\\d+)[dD](\\d+)[sS](\\d+)[lL](\\d+)\\b", "rollSubWithLower($1, $2, $3, $4)"
        },
        new String[] {"\\b[dD](\\d+)[sS](\\d+)[lL](\\d+)\\b", "rollSubWithLower(1, $1, $2, $3)"},

        // Roll with a minimum value per roll (e.g. treat 1s as 2s)
        new String[] {"\\b(\\d+)[dD](\\d+)[lL](\\d+)\\b", "rollWithLower($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[lL](\\d+)\\b", "rollWithLower(1, $1, $2)"},

        // Roll with a maximum value per roll (e.g. treat 6s as 5s)
        new String[] {"\\b(\\d+)[dD](\\d+)[uU](\\d+)\\b", "rollWithUpper($1, $2, $3)"},
        new String[] {"\\b[dD](\\d+)[uU](\\d+)\\b", "rollWithUpper(1, $1, $2)"},

        // Dragon Quest
        new String[] {"\\b(\\d+)[dD](\\d+)[qQ]#([+-]?\\d+)\\b", "rollAddWithLower($1, $2, $3, 1)"},
        new String[] {"\\b[dD](\\d+)[qQ]#([+-]?\\d+)\\b", "rollAddWithLower(1, $1, $2, 1)"},
        new String[] {"\\b(\\d+)[dD](\\d+)[qQ]\\b", "rollAddWithLower($1, $2, 0, 1)"},
        new String[] {"\\b[dD](\\d+)[qQ]\\b", "rollAddWithLower(1, $1, 0, 1)"},

        // Ars Magica Stress Die
        new String[] {"\\b[aA][sS](\\d+)\\b", "arsMagicaStress($1, 0)"},
        new String[] {"\\b[aA][sS](\\d+)[bB]#([+-]?\\d+)\\b", "arsMagicaStress($1, $2)"},
        new String[] {"\\b[aA][nN][sS](\\d+)\\b", "arsMagicaStressNum($1, 0)"},
        new String[] {"\\b[aA][nN][sS](\\d+)[bB]#([+-]?\\d+)\\b", "arsMagicaStressNum($1, $2)"},
      };

  private final Parser parser;

  private final List<Pair<Pattern, String>> preprocessPatterns =
      List.of(
          new Pair<>(Pattern.compile("^([A-z]+)!\"([^\"]*)\"$"), "advancedRoll('$1', " + "'$2')"),
          new Pair<>(Pattern.compile("^([A-z]+)!'([^']*)'$"), "advancedRoll('$1', " + "'$2')"));

  public ExpressionParser() {
    this(DICE_PATTERNS);
  }

  public ExpressionParser(String[][] regexpTransforms) {

    parser = createParser();

    parser.addFunction(new CountSuccessDice());
    parser.addFunction(new DropRoll());
    parser.addFunction(new ExplodeDice());
    parser.addFunction(new KeepRoll());
    parser.addFunction(new RerollDice());
    parser.addFunction(new RerollDiceOnce());
    parser.addFunction(new HeroRoll());
    parser.addFunction(new HeroKillingRoll());
    parser.addFunction(new FudgeRoll());
    parser.addFunction(new UbiquityRoll());
    parser.addFunction(new ShadowRun4Dice());
    parser.addFunction(new ShadowRun4ExplodeDice());
    parser.addFunction(new ShadowRun5Dice());
    parser.addFunction(new ShadowRun5ExplodeDice());
    parser.addFunction(new Roll());
    parser.addFunction(new ExplodingSuccessDice());
    parser.addFunction(new OpenTestDice());
    parser.addFunction(new RollWithBounds());
    parser.addFunction(new DropHighestRoll());
    parser.addFunction(new KeepLowestRoll());
    parser.addFunction(new ArsMagicaStress());
    parser.addFunction(new AdvancedDiceRolls());

    parser.addFunction(new If());

    StringLiteralTransformer slt = new StringLiteralTransformer();

    parser.addTransformer(slt.getRemoveTransformer());
    parser.addTransformer(new RegexpStringTransformer(regexpTransforms));
    parser.addTransformer(slt.getReplaceTransformer());
  }

  protected Parser createParser() {
    return new Parser();
  }

  public Parser getParser() {
    return parser;
  }

  public Result evaluate(String expression) throws ParserException {
    return evaluate(expression, new MapVariableResolver(), true);
  }

  public Result evaluate(String expression, VariableResolver resolver) throws ParserException {
    return evaluate(expression, resolver, true);
  }

  public Result evaluate(String expression, VariableResolver resolver, boolean makeDeterministic)
      throws ParserException {
    Result ret = new Result(expression);
    RunData oldData = RunData.hasCurrent() ? RunData.getCurrent() : null;
    try {
      RunData newRunData;
      if (oldData != null) {
        newRunData = oldData.createChildRunData(ret);
      } else {
        newRunData = new RunData(ret);
      }
      RunData.setCurrent(newRunData);

      // Some patterns need pre-processing before the parser is called otherwise the parser
      // creation will fail
      expression = preProcess(expression);
      synchronized (parser) {
        final Expression xp =
            makeDeterministic
                ? parser.parseExpression(expression).getDeterministicExpression(resolver)
                : parser.parseExpression(expression);
        ret.setDetailExpression(() -> xp.format());
        ret.setValue(xp.evaluate(resolver));
        ret.setRolled(newRunData.getRolled());
      }
    } finally {
      RunData.setCurrent(oldData);
    }

    return ret;
  }

  /**
   * Pre-process the expression before it is parsed. This is used to convert some patterns into
   * function calls that the parser can handle.
   *
   * @param expression The expression to pre-process
   * @return The pre-processed expression
   */
  private String preProcess(String expression) {
    var trimmed = expression.trim();
    for (Pair<Pattern, String> p : preprocessPatterns) {
      if (p.getValue0().matcher(trimmed).find()) {
        return p.getValue0().matcher(trimmed).replaceAll(p.getValue1());
      }
    }
    return expression;
  }
}
