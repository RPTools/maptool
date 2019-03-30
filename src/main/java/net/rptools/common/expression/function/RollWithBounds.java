package net.rptools.common.expression.function;

import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractNumberFunction;

import java.math.BigDecimal;
import java.util.List;

public class RollWithBounds extends AbstractNumberFunction {

	public RollWithBounds() {
		super(3, 4, false, "rollSubWithLower", "rollWithLower", "rollAddWithUpper", "rollWithUpper");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		int times = 0;
		int sides = 0;
		int mod = 0;
		int lower = Integer.MIN_VALUE;
		int upper = Integer.MAX_VALUE;

		switch (functionName) {
		case "rollSubWithLower":
			times = ((BigDecimal) parameters.get(0)).intValue();
			sides = ((BigDecimal) parameters.get(1)).intValue();
			mod = -((BigDecimal) parameters.get(2)).intValue();
			lower = ((BigDecimal) parameters.get(3)).intValue();
			break;
		case "rollWithLower":
			times = ((BigDecimal) parameters.get(0)).intValue();
			sides = ((BigDecimal) parameters.get(1)).intValue();
			lower = ((BigDecimal) parameters.get(2)).intValue();
			break;
		case "rollAddWithUpper":
			times = ((BigDecimal) parameters.get(0)).intValue();
			sides = ((BigDecimal) parameters.get(1)).intValue();
			mod = ((BigDecimal) parameters.get(2)).intValue();
			upper = ((BigDecimal) parameters.get(3)).intValue();
			break;
		case "rollWithUpper":
			times = ((BigDecimal) parameters.get(0)).intValue();
			sides = ((BigDecimal) parameters.get(1)).intValue();
			upper = ((BigDecimal) parameters.get(2)).intValue();
			break;
		case "rollAddWithLower":
			times = ((BigDecimal) parameters.get(0)).intValue();
			sides = ((BigDecimal) parameters.get(1)).intValue();
			mod = ((BigDecimal) parameters.get(2)).intValue();
			lower = ((BigDecimal) parameters.get(3)).intValue();
			break;
		}
		return DiceHelper.rollModWithBounds(times, sides, mod, lower, upper);
	}
}
