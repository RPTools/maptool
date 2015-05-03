/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.rptools.common.expression.function;

import java.math.BigDecimal;

import net.rptools.common.expression.RunData;
import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.EvaluationException;
import net.rptools.parser.function.ParameterException;
import junit.framework.TestCase;

public class DropRollTest extends TestCase {
	public void testEvaluateRoll() throws ParserException, EvaluationException,
			ParameterException {
		
		Parser p = new Parser();
		p.addFunction(new DropRoll());

		try {
            RunData.setCurrent(new RunData(null));
		    RunData.setSeed(10423L);

			Expression xp = p.parseExpression("drop(4,6,1)");

			long result = ((BigDecimal) xp.evaluate()).longValueExact();

			assertEquals(12L, result);
		} finally {
			RunData.setCurrent(null);
		}
	}
}
