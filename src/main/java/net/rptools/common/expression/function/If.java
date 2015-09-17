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
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.EvaluationException;

public class If extends AbstractFunction {

	public If() {
		super(3, 3, false, "if");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws EvaluationException {
		if (BigDecimal.ZERO.equals((BigDecimal) parameters.get(0)))
			return parameters.get(2);

		return parameters.get(1);
	}

}
