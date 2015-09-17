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
package net.rptools.common.expression.function;

import net.rptools.common.expression.RunData;
import net.rptools.parser.function.EvaluationException;
import junit.framework.TestCase;

public class DiceHelperTest extends TestCase {
	public void testRollDice() throws Exception {
		RunData.setCurrent(new RunData(null));
		RunData.setSeed(102312L);

		assertEquals(42, DiceHelper.rollDice(10, 6));
	}

	public void testKeepDice() throws Exception {
		RunData.setCurrent(new RunData(null));
		RunData.setSeed(102312L);

		assertEquals(28, DiceHelper.keepDice(10, 6, 5));
	}

	public void testDropDice() throws Exception {
		RunData.setCurrent(new RunData(null));
		RunData.setSeed(102312L);

		assertEquals(28, DiceHelper.dropDice(10, 6, 5));
	}

	public void testRerollDice() throws Exception {
		RunData.setCurrent(new RunData(null));
		RunData.setSeed(102312L);

		assertEquals(50, DiceHelper.rerollDice(10, 6, 2));
	}

	public void testExplodeDice() throws Exception {
		RunData.setCurrent(new RunData(null));
		RunData.setSeed(102312L);

		assertEquals(23, DiceHelper.explodeDice(4, 6));
	}

	public void testExplodeDice_Exception() throws Exception {
		try {
			RunData.setCurrent(new RunData(null));
			RunData.setSeed(102312L);

			assertEquals(23, DiceHelper.explodeDice(4, 1));
			fail();
		} catch (EvaluationException e) {
		}

	}
}
