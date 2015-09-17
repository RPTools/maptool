/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.common.expression;

import net.rptools.common.expression.function.*;
import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.transform.RegexpStringTransformer;
import net.rptools.parser.transform.StringLiteralTransformer;

public class ExpressionParser {
	private static String[][] DICE_PATTERNS = new String[][] {
			// Comments
			new String[] { "//.*", "" },

			// Color hex strings #FFF or #FFFFFF or #FFFFFFFF (with alpha)
			new String[] { "(?<![0-9A-Za-z])#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])(?![0-9A-Za-z])", "0x$1$1$2$2$3$3" },
			new String[] { "(?<![0-9A-Za-z])#([0-9A-Fa-f]{6,6}(?:[0-9A-Fa-f]{2,2})?)(?![0-9A-Za-z])", "0x$1" },

			// drop
			new String[] { "\\b(\\d+)[dD](\\d+)[dD](\\d+)\\b", "drop($1, $2, $3)" },
			new String[] { "\\b[dD](\\d+)[dD](\\d+)\\b", "drop(1, $1, $2)" },

			// keep
			new String[] { "\\b(\\d+)[dD](\\d+)[kK](\\d+)\\b", "keep($1, $2, $3)" },
			new String[] { "\\b[dD](\\d+)[kK](\\d+)\\b", "keep(1, $1, $2)" },

			// re-roll
			new String[] { "\\b(\\d+)[dD](\\d+)[rR](\\d+)\\b", "reroll($1, $2, $3)" },
			new String[] { "\\b[dD](\\d+)[rR](\\d+)\\b", "reroll(1, $1, $2)" },

			// count success
			new String[] { "\\b(\\d+)[dD](\\d+)[sS](\\d+)\\b", "success($1, $2, $3)" },
			new String[] { "\\b[dD](\\d+)[sS](\\d+)\\b", "success(1, $1, $2)" },

			// count success while exploding
			new String[] { "\\b(\\d+)[dD](\\d+)[eE][sS](\\d+)\\b", "explodingSuccess($1, $2, $3)" },
			new String[] { "\\b[dD](\\d+)[eE][sS](\\d+)\\b", "explodingSuccess(1, $1, $2)" },
			new String[] { "\\b(\\d+)[eE][sS](\\d+)\\b", "explodingSuccess($1, 6, $2)" },

			// show max while exploding
			new String[] { "\\b(\\d+)[dD](\\d+)[oO]\\b", "openTest($1, $2)" },
			new String[] { "\\b[dD](\\d+)[oO]\\b", "openTest(1, $1)" },
			new String[] { "\\b(\\d+)[oO]\\b", "openTest($1, 6)" },

			// explode
			new String[] { "\\b(\\d+)[dD](\\d+)[eE]\\b", "explode($1, $2)" },
			new String[] { "\\b[dD](\\d+)[eE]\\b", "explode(1, $1)" },

			// hero
			new String[] { "\\b(\\d+[.]\\d+)[dD](\\d+)[hH]\\b", "hero($1, $2)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[hH]\\b", "hero($1, $2)" },
			new String[] { "\\b[dD](\\d+)[hH]\\b", "hero(1, $1)" },

			new String[] { "\\b(\\d+[.]\\d+)[dD](\\d+)[bB]\\b", "herobody($1, $2)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[bB]\\b", "herobody($1, $2)" },
			new String[] { "\\b[dD](\\d+)[bB]\\b", "herobody(1, $1)" },

			// hero killing
			new String[] { "\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling($1, $2, $3)" },
			new String[] { "\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK]\\b", "herokilling($1, $2, 0)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling($1, $2, $3)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[hH][kK]\\b", "herokilling($1, $2, 0)" },
			new String[] { "\\b[dD](\\d+)[hH][kK]([-+]\\d+)\\b", "herokilling(1, $1, $3)" },
			new String[] { "\\b[dD](\\d+)[hH][kK]\\b", "herokilling(1, $1, 0)" },

			// hero killing2
			new String[] { "\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2($1, $2, $3)" },
			new String[] { "\\b(\\d+[.]\\d+)[dD](\\d+)[hH][kK][2]\\b", "herokilling2($1, $2, 0)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2($1, $2, $3)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[hH][kK][2]\\b", "herokilling2($1, $2, 0)" },
			new String[] { "\\b[dD](\\d+)[hH][kK][2]([-+]\\d+)\\b", "herokilling2(1, $1, $3)" },
			new String[] { "\\b[dD](\\d+)[hH][kK][2]\\b", "herokilling2(1, $1, 0)" },

			// hero killing multiplier
			new String[] { "\\b(\\d+)[dD](\\d+)[hH][mM]([-+]\\d+)\\b", "heromultiplier($1, $2, $3)" },
			new String[] { "\\b[dD](\\d+)[hH][mM]([-+]\\d+)\\b", "heromultiplier(1, $1, $2)" },
			new String[] { "\\b(\\d+)[dD](\\d+)[hH][mM]\\b", "heromultiplier($1, $2, 0)" },
			new String[] { "\\b[dD](\\d+)[hH][mM]\\b", "heromultiplier(1, $1, 0)" },
			new String[] { "\\b(\\d+)[hH][mM]\\b", "heromultiplier(0, 0, $1)" },

			// dice
			new String[] { "\\b(\\d+)[dD](\\d+)\\b", "roll($1, $2)" },
			new String[] { "\\b[dD](\\d+)\\b", "roll(1, $1)" },

			// Fudge dice
			new String[] { "\\b(\\d+)[dD][fF]\\b", "fudge($1)" },
			new String[] { "\\b[dD][fF]\\b", "fudge(1)" },

			// Ubiquity dice
			new String[] { "\\b(\\d+)[dD][uU]\\b", "ubiquity($1)" },
			new String[] { "\\b[dD][uU]\\b", "ubiquity(1)" },

			// Shadowrun 4 Edge or Exploding Test
			new String[] { "\\b(\\d+)[sS][rR]4[eE][gG](\\d+)\\b", "sr4e($1, $2)" },
			new String[] { "\\b(\\d+)[sS][rR]4[eE]\\b", "sr4e($1)" },

			// Shadowrun 4 Normal Test
			new String[] { "\\b(\\d+)[sS][rR]4[gG](\\d+)\\b", "sr4($1, $2)" },
			new String[] { "\\b(\\d+)[sS][rR]4\\b", "sr4($1)" },

	};

	private final Parser parser;

	public ExpressionParser() {
		this(DICE_PATTERNS);
	}

	public ExpressionParser(VariableResolver resolver) {
		this(DICE_PATTERNS, resolver);
	}

	public ExpressionParser(String[][] regexpTransforms) {
		this(regexpTransforms, null);
	}

	public ExpressionParser(String[][] regexpTransforms, VariableResolver resolver) {
		parser = new Parser(resolver, true);

		parser.addFunction(new CountSuccessDice());
		parser.addFunction(new DropRoll());
		parser.addFunction(new ExplodeDice());
		parser.addFunction(new KeepRoll());
		parser.addFunction(new RerollDice());
		parser.addFunction(new HeroRoll());
		parser.addFunction(new HeroKillingRoll());
		parser.addFunction(new FudgeRoll());
		parser.addFunction(new UbiquityRoll());
		parser.addFunction(new ShadowRun4Dice());
		parser.addFunction(new ShadowRun4ExplodeDice());
		parser.addFunction(new Roll());
		parser.addFunction(new ExplodingSuccessDice());
		parser.addFunction(new OpenTestDice());

		parser.addFunction(new If());

		StringLiteralTransformer slt = new StringLiteralTransformer();

		parser.addTransformer(slt.getRemoveTransformer());
		parser.addTransformer(new RegexpStringTransformer(regexpTransforms));
		parser.addTransformer(slt.getReplaceTransformer());
	}

	public Parser getParser() {
		return parser;
	}

	public Result evaluate(String expression) throws ParserException {
		Result ret = new Result(expression);
		RunData oldData = RunData.hasCurrent() ? RunData.getCurrent() : null;
		try {
			RunData.setCurrent(new RunData(ret));

			synchronized (parser) {
				Expression xp = parser.parseExpression(expression);
				Expression dxp = xp.getDeterministicExpression();
				ret.setDetailExpression(dxp.format());
				ret.setValue(dxp.evaluate());
			}
		} finally {
			RunData.setCurrent(oldData);
		}

		return ret;
	}
}
