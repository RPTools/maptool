/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.common.expression.Result;
import net.rptools.common.expression.RunData;
import net.rptools.common.expression.function.DiceHelper;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.EvaluationException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 * 
 */
public class DiceLib extends TwoArgFunction {

	public static LuaValue roll(LuaValue arg, LuaValue arg2) {
		return LuaValue.valueOf(DiceHelper.rollDice(arg.checkint(), arg2.checkint()));
	}
	
	public static LuaValue fudge(LuaValue arg) {
		return LuaValue.valueOf(DiceHelper.fudgeDice(arg.checkint()));
	}
	
	public static LuaValue ubiquity(LuaValue arg) {
		return LuaValue.valueOf(DiceHelper.ubiquityDice(arg.checkint()));
	}
	
	public static LuaValue hero(LuaValue arg, LuaValue arg2) {
		LuaTable result = new LuaTable();
		int body = 0;
		double times = arg.checkdouble();
		int sides = arg2.checkint();

		RunData runData = RunData.getCurrent();

		int stun = 0;
		double half = times - Math.floor(times);
		for (int i = 0; i < Math.floor(times); i++) {
			int die = runData.randomInt(sides);
			/*
			 * Keep track of the body generated. In theory Hero System only uses
			 * 6-sided where a 1 is 0 body, 2-5 is 1 body and 6 is 2 body but I
			 * left the sides unbounded just in case.
			 */
			if (die > 1)
				body++;
			if (die == sides)
				body++;

			stun += die;
		}

		if (half >= 0.5) {
			/*
			 * Roll a half dice. In theory Hero System only uses 6-sided and for
			 * half dice 1 & 2 = 1 Stun 0 body 3 = 2 stun 0 body 4 = 2 stun 1
			 * body 5 & 6 = 3 stun 1 body
			 */
			int die = runData.randomInt(sides);
			if (die * 2 > sides)
				body++;

			stun += (die + 1) / 2;
		}
		result.set("stun", LuaValue.valueOf(stun));
		result.set("body", LuaValue.valueOf(body));
		return result;
		
	}
	
	public static LuaValue herokilling(LuaValue arg, LuaValue arg2, LuaValue mult, LuaValue extraarg, boolean killing2) {
		LuaTable result = new LuaTable();
		double times = arg.checkdouble();
		int multiplier = mult.checkint();
		int sides = arg2.checkint();
		double half = times - Math.floor(times);
		int extra = 0;
		if (!extraarg.isnil())
			extra = extraarg.checkint();
		RunData runData = RunData.getCurrent();
		int body = 0;
		if (!killing2) {
			body = DiceHelper.rollDice((int) times, sides);
			body = body + extra;
			/*
			 * If value half or more roll a half die
			 */
			if (half >= 0.5) {
				/*
				 * Roll a half dice.
				 */
				int die = runData.randomInt(sides);
				body += (die + 1) / 2;
			} else if (half >= 0.2) {
				/*
				 * Add a single pip
				 */
				body++;
			}
			result.set("body", body);
		} else {
			body = DiceHelper.rollDice((int) times, sides);
			body = body + extra;
			/*
			 * If value half or more roll a die -1. minimum value of 1.
			 */
			if (half >= 0.5) {
				/*
				 * Roll a half dice.
				 */
				int die = runData.randomInt(sides);
				if (die > 1)
					die = die - 1;
				body += die;
			} else if (half >= 0.2) {
				/*
				 * Add a single pip
				 */
				body++;
			}
			result.set("body", body);
		}
		int multi = DiceHelper.rollDice(multiplier, sides);
		multi = multi + extra;
		if (multi < 1)
			multi = 1;
		result.set("multiplied", body * multi);
		return result;
	}
	
	
	public static LuaValue sr4(LuaValue arg, LuaValue arg2, boolean asplode) {
		return LuaValue.valueOf(DiceHelper.countShadowRun4(arg.checkint(), arg2.checkint(), asplode));
	}
	
	public static LuaValue explode(LuaValue arg, LuaValue arg2) {
		try {
			return LuaValue.valueOf(DiceHelper.explodeDice(arg.checkint(), arg2.checkint()));
		} catch (EvaluationException e) {
			throw new LuaError(e);
		}
	}
	
	public static LuaValue success(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		return LuaValue.valueOf(DiceHelper.countSuccessDice(arg.checkint(), arg2.checkint(), arg3.checkint()));
	}
	public static LuaValue drop(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		try {
			return LuaValue.valueOf(DiceHelper.dropDice(arg.checkint(), arg2.checkint(), arg3.checkint()));
		} catch (EvaluationException e) {
			throw new LuaError(e);
		}
	}
	public static LuaValue explodingSuccess(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		try {
			return LuaValue.valueOf(DiceHelper.explodingSuccessDice(arg.checkint(), arg2.checkint(), arg3.checkint()));
		} catch (EvaluationException e) {
			throw new LuaError(e);
		}
	}
	public static LuaValue keep(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		try {
			return LuaValue.valueOf(DiceHelper.keepDice(arg.checkint(), arg2.checkint(), arg3.checkint()));
		} catch (EvaluationException e) {
			throw new LuaError(e);
		}
	}
	public static LuaValue reroll(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		try {
			return LuaValue.valueOf(DiceHelper.rerollDice(arg.checkint(), arg2.checkint(), arg3.checkint()));
		} catch (EvaluationException e) {
			throw new LuaError(e);
		}
	}
	public static final class Dice1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			if (!RunData.hasCurrent()) {
				RunData.setCurrent(new RunData(new Result("")));
			}
			switch (opcode) {
				case 0:
				case 1:
					return fudge(arg);
				case 2:
				case 3:
					return ubiquity(arg);
				}
				return NIL;
		}
	}
	
	public static final class Dice2 extends TwoArgFunction {
		public LuaValue call(LuaValue arg, LuaValue arg2) {
			if (!RunData.hasCurrent()) {
				RunData.setCurrent(new RunData(new Result("")));
			}
			switch (opcode) {
				case 0:
				case 1:
				case 2:
					return roll(arg, arg2);
				case 3:
					return explode(arg, arg2);
				case 4:
					return hero(arg, arg2);
				case 5:
					return sr4(arg, arg2, false);
				case 6:
					return sr4(arg, arg2, true);
			}
			return NIL;
		}
	}
	
	public static final class Dice3 extends ThreeArgFunction {
		public LuaValue call(LuaValue arg, LuaValue arg2, LuaValue arg3) {
			if (!RunData.hasCurrent()) {
				RunData.setCurrent(new RunData(new Result("")));
			}
			switch (opcode) {
				case 0:
				case 1:
				case 2:
					return success(arg, arg2, arg3);
				case 3:
				case 4:
					return drop(arg, arg2, arg3);
				case 5:
				case 6:
					return explodingSuccess(arg, arg2, arg3);
				case 7:
				case 8:
					return keep(arg, arg2, arg3);
				case 9:
				case 10:
					return reroll(arg, arg2, arg3);
			}
			return NIL;
		}
	}
	
	public static final class Dice4 extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			if (!RunData.hasCurrent()) {
				RunData.setCurrent(new RunData(new Result("")));
			}
			switch (opcode) {
				case 0:
					return herokilling(args.arg(1), args.arg(2), args.arg(3), args.arg(4), false);
				case 1:
					return herokilling(args.arg(1), args.arg(2), args.arg(3), args.arg(4), true);
			}
			return NIL;
		}
	}

	Globals globals;

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable t = new LuaTable();
		bind(t, Dice1.class, new String[] { "f", "fudge",  "u", "ubiquity"});
		bind(t, Dice2.class, new String[] { "dice", "d", "roll",  "explode",  "hero",  "sr4",  "sr4e"});
		bind(t, Dice3.class, new String[] { "countsuccess", "countSuccess", "success",  "drop", "dd",  "explodingSuccess", "explodingsuccess",  "keep", "k",  "reroll", "rr"});
		bind(t, Dice4.class, new String[] {"herokilling", "herokilling2",});
		env.set("dice", t);
		env.get("package").get("loaded").set("dice", t);
		return t;
	}

	

	public static void checkTrusted(String cls) {
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new LuaError(new ParserException(I18N.getText(
						"macro.function.general.noPerm", "dice." + cls)));
			}
		}

	}
}
