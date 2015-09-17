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

import java.math.BigDecimal;
import java.util.List;

import net.rptools.parser.Parser;
import net.rptools.parser.function.AbstractNumberFunction;

/** ubiquity(range)
 * 
 * Generate a random number form 1 to <code>sides</code>, <code>times</code>
 * number of times.  If <code>times</code> is not supplied it defaults to 1.
 * 
 * Example:
 * 	ubiquity(4) = 4dU (0..4 successes)
 */
public class UbiquityRoll extends AbstractNumberFunction {

	public UbiquityRoll() {
		super(1, 1, false, "u", "ubiquity");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) {
		int n = 0;

		int times = 1;
		if (parameters.size() == 1)
			times = ((BigDecimal) parameters.get(n++)).intValue();

		return new BigDecimal(DiceHelper.ubiquityDice(times));
	}
}
