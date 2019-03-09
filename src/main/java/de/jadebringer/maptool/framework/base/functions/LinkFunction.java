/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.framework.base.functions;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jadebringer.maptool.framework.FunctionCaller;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.ObservableList;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.Function;
import net.sf.json.JSONObject;

/**
 * 
 * @author oliver.szymanski
 */
public class LinkFunction extends AbstractFunction {
	public LinkFunction() {
		super(1, UNLIMITED_PARAMETERS, "jb_execLink", "jb_unpackArgs", "jb_createLink");
	}

	private final static LinkFunction instance = new LinkFunction();

	private final static String[] SPECIAL_OUTPUT_CHANNELS = {"self", "gm", "all", "none", "gm-self" };
	
	private static final Pattern macroLink = Pattern.compile("(?s)([^:]*)://([^/]*)/([^/]*)/([^?]*)(?:\\?(.*))?");

	private static enum OutputTo {
		SELF, NONE, GM, ALL, SELF_AND_GM, LIST
	}

	public static LinkFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		
		if ("jb_execLink".equals(functionName)) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
			}

			boolean defer = FunctionCaller.getParam(parameters, 1, false);
			Object message = FunctionCaller.getParam(parameters, 0);
			execLink((String)message, false, parser, defer);
			return "";
		} if ("jb_unpackArgs".equals(functionName)) {
			Object message = FunctionCaller.getParam(parameters, 0);
			return JSONObject.fromObject(message).get("args");
		} else if ("jb_createLink".equals(functionName)) {
			return createLink(parser, parameters);
		}
		
		return "";
	}

	public Object createLink(Parser parser, List<Object> parameters)
			throws ParserException {
		String linkTo = FunctionCaller.getParam(parameters, 0);
		String who = FunctionCaller.getParam(parameters, 1);
		String args = FunctionCaller.getParam(parameters, 2);
		String target = FunctionCaller.getParam(parameters, 3);		
		return createLink(parser, linkTo, who, args, target);
	}
	
	public Object createLink(Parser parser, String linkTo, String who, String args, String target)
			throws ParserException {
		Function macro = MacroLinkFunction.getInstance();
		Function json = JSONMacroFunctions.getInstance();
		
		if (linkTo == null) who = "ping";
		if (who == null) who = "GM";
		if (target == null) target = "impersonated";
		
		boolean specialChannel = false;
		for(String channel : SPECIAL_OUTPUT_CHANNELS) {
			if (channel.equals(who.toLowerCase())) specialChannel = true;
		}
		
		String message = "{}";
		String toWho = who;
		if (!specialChannel) {
			toWho = "list";
			boolean isArray = "ARRAY".equals(FunctionCaller.callFunction("json.type", json, parser, who));
			if (isArray) {
				who = ((JSONObject)FunctionCaller.callFunction("json.fromList", json, parser, who)).toString();
			}
			message = ((JSONObject)FunctionCaller.callFunction("json.set", json, parser, message, "mlOutputList", who)).toString();
		}
		
		if (args != null) {
			message = ((JSONObject)FunctionCaller.callFunction("json.set", json, parser, message, "args", args)).toString();
		}
		
		if (args != null) {
			return FunctionCaller.callFunction("macroLinkText", macro, parser, linkTo, toWho, message, target);
		} else {
			return FunctionCaller.callFunction("macroLinkText", macro, parser, linkTo, toWho);				
		}
	}

	public void execLink(String link, boolean setVars, Parser parser, boolean defer) {
		if (defer) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					execLink(link, setVars, parser);
				}
			});
		} else {
			this.execLink(link, setVars, parser);
		}
	}
	
	/**
	 * Runs the macro specified by the link.
	 * 
	 * @param link
	 *            the link to the macro.
	 * @param setVars
	 *            should the variables be set in the macro context as well as passed in as macro.args.
	 */
	@SuppressWarnings("unchecked")
	public void execLink(String link, boolean setVars, Parser parser) {
		if (link == null || link.length() == 0) {
			return;
		}
		Matcher m = macroLink.matcher(link);

		if (m.matches()) {
			OutputTo outputTo;
			String macroName = "";
			String args = "";
			Set<String> outputToPlayers = new HashSet<String>();

			if (m.group(1).equalsIgnoreCase("macro")) {

				String who = m.group(3);
				if (who.equalsIgnoreCase("self")) {
					outputTo = OutputTo.SELF;
				} else if (who.equalsIgnoreCase("gm")) {
					outputTo = OutputTo.GM;
				} else if (who.equalsIgnoreCase("none")) {
					outputTo = OutputTo.NONE;
				} else if (who.equalsIgnoreCase("all") || who.equalsIgnoreCase("say")) {
					outputTo = OutputTo.ALL;
				} else if (who.equalsIgnoreCase("gm-self") || who.equalsIgnoreCase("gmself")) {
					outputTo = OutputTo.SELF_AND_GM;
				} else if (who.equalsIgnoreCase("list")) {
					outputTo = OutputTo.LIST;
				} else {
					outputTo = OutputTo.NONE;
				}
				macroName = m.group(2);

				String val = m.group(5);
				if (val != null) {
					try {
						Double.parseDouble(val);
						// Do nothing as its a number
					} catch (NumberFormatException e) {
						try {
							val = argsToStrPropList(val);
						} catch (ParserException e1) {
							MapTool.addLocalMessage("Error running macro link: " + e1.getMessage());
						}
					}
					args = val;
					try {
						JSONObject jobj = JSONObject.fromObject(args);
						if (jobj.containsKey("mlOutputList")) {
							outputToPlayers.addAll(jobj.getJSONArray("mlOutputList"));
						}
					} catch (Exception e) {
						// Do nothing as we just dont populate the list.
					}
				}

				String[] targets = m.group(4).split(",");
				Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

				try {
					for (String t : targets) {
						if (t.equalsIgnoreCase("impersonated")) {
							Token token;
							GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
							if (guid != null)
								token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
							else
								token = zone.resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());
							createAndDoOutput(parser, outputTo, macroName, args, outputToPlayers, token);
						} else if (t.equalsIgnoreCase("selected")) {
							for (GUID id : MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet()) {
								Token token = zone.getToken(id);
								createAndDoOutput(parser, outputTo, macroName, args, outputToPlayers, token);
							}
						} else {
							Token token = zone.resolveToken(t);
							createAndDoOutput(parser, outputTo, macroName, args, outputToPlayers, token);
						}
					}
				} catch (Exception e) {
					MapTool.addLocalMessage(e.getMessage());
				}
			}
		}
	}

	private void createAndDoOutput(Parser parser, OutputTo outputTo, String macroName, String args,
			Set<String> outputToPlayers, Token token) throws ParserException {
		MapToolVariableResolver resolver = new MapToolVariableResolver(token);
		String output = null;
		if (macroName.indexOf("@") < 0) {
			output = FunctionCaller.callFunction(macroName, parser, (Object)args).toString();
		} else {
			MapTool.getParser().runMacro(resolver, token, macroName, args);
		}
		doOutput(token, outputTo, output, outputToPlayers);
	}

	/**
	 * Converts a URL argument string into a property list.
	 * 
	 * @param args
	 *            the URL argument string.
	 * @return a property list representation of the arguments.
	 * @throws ParserException
	 *             if the argument encoding is incorrect.
	 */
	public String argsToStrPropList(String args) throws ParserException {
		String vals[] = args.split("&");
		StringBuilder propList = new StringBuilder();

		try {
			for (String s : vals) {
				String decoded = URLDecoder.decode(s, "utf-8");
				decoded = decoded.replaceAll(";", "&#59");
				if (propList.length() == 0) {
					propList.append(decoded);
				} else {
					propList.append(" ; ");
					propList.append(decoded);
				}
			}
			return propList.toString();
		} catch (UnsupportedEncodingException e) {
			throw new ParserException(e);
		}
	}
	
	private void doOutput(Token token, OutputTo outputTo, String line, Set<String> playerList) {
		/*
		 * First we check our player list to make sure we are not sending things out multiple times or the wrong way. This looks a little ugly, but all it is doing is searching for the strings "say",
		 * "gm", or "gmself", and if it contains no other strings changes it to a more appropriate for such as /togm, /self, etc. If it contains other names then gm, self etc will be replaced with
		 * player names.
		 */
		if (outputTo == OutputTo.LIST) {
			if (playerList == null) {
				outputTo = OutputTo.NONE;
			} else if (playerList.contains("all") || playerList.contains("say")) {
				outputTo = OutputTo.ALL;
			} else if (playerList.contains("gmself") || playerList.contains("gm-self")) {
				playerList.remove("gmself");
				playerList.remove("gm-self");
				if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
					outputTo = OutputTo.SELF_AND_GM;
				} else {
					playerList.addAll(MapTool.getGMs());
					playerList.add(getSelf());
				}
			} else if (playerList.contains("gm") && playerList.contains("self")) {
				playerList.remove("gm");
				playerList.remove("self");
				if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
					outputTo = OutputTo.SELF_AND_GM;
				} else {
					playerList.addAll(MapTool.getGMs());
					playerList.add(getSelf());
				}
			} else if (playerList.contains("gm")) {
				playerList.remove("gm");
				if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
					outputTo = OutputTo.GM;
				} else {
					playerList.addAll(MapTool.getGMs());
					playerList.add(getSelf());
				}
			} else if (playerList.contains("self")) {
				playerList.remove("self");
				if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
					outputTo = OutputTo.SELF;
				} else {
					playerList.add(getSelf());
				}
			}
		}

		switch (outputTo) {
		case SELF:
			MapTool.addLocalMessage(line);
			break;
		case SELF_AND_GM:
			MapTool.addMessage(new TextMessage(TextMessage.Channel.ME, null, MapTool.getPlayer().getName(), I18N.getText("togm.self", line), null));
			// Intentionally falls through
		case GM:
			MapTool.addMessage(new TextMessage(TextMessage.Channel.GM, null, MapTool.getPlayer().getName(), I18N.getText("togm.saysToGM", MapTool.getPlayer().getName()) + " " + line, null));
			break;
		case ALL:
			doSay(line, token, false, "");
			break;
		case LIST:
			StringBuilder sb = new StringBuilder();
			for (String name : playerList) {
				doWhisper(line, token, name);
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(name);
			}
			MapTool.addMessage(new TextMessage(TextMessage.Channel.ME, null, MapTool.getPlayer().getName(), "<span class='whisper' style='color:blue'>"
					+ I18N.getText("whisper.you.string", sb.toString(), line) + "</span>", null));

			break;
		case NONE:
			// Do nothing with the output.
			break;
		}

	}

	private void doWhisper(String message, Token token, String playerName) {
		ObservableList<Player> playerList = MapTool.getPlayerList();
		List<String> players = new ArrayList<String>();
		for (int count = 0; count < playerList.size(); count++) {
			Player p = playerList.get(count);
			String thePlayer = p.getName();
			players.add(thePlayer);
		}
		String playerNameMatch = StringUtil.findMatch(playerName, players);
		playerName = (!playerNameMatch.equals("")) ? playerNameMatch : playerName;

		// Validate
		if (!MapTool.isPlayerConnected(playerName)) {
			MapTool.addMessage(new TextMessage(TextMessage.Channel.ME, null, MapTool.getPlayer().getName(), I18N.getText("msg.error.playerNotConnected", playerName), null));
		}
		if (MapTool.getPlayer().getName().equalsIgnoreCase(playerName)) {
			return;
		}

		// Send
		MapTool.addMessage(new TextMessage(TextMessage.Channel.WHISPER, playerName, MapTool.getPlayer().getName(), "<span class='whisper' style='color:blue'>"
				+ "<span class='whisper' style='color:blue'>" + I18N.getText("whisper.string", MapTool.getFrame().getCommandPanel().getIdentity(), message) + "</span>", null));

	}

	private void doSay(String msg, Token token, boolean trusted, String macroName) {
		StringBuilder sb = new StringBuilder();

		String identity = token == null ? MapTool.getPlayer().getName() : token.getName();

		sb.append("<table cellpadding=0><tr>");

		if (token != null && AppPreferences.getShowAvatarInChat()) {
			if (token != null) {
				MD5Key imageId = token.getPortraitImage();
				if (imageId == null) {
					imageId = token.getImageAssetId();
				}
				sb.append("<td valign=top width=40 style=\"padding-right:5px\"><img src=\"asset://").append(imageId).append("-40\" ></td>");
			}
		}

		sb.append("<td valign=top style=\"margin-right: 5px\">");
		if (trusted && !MapTool.getPlayer().isGM()) {
			sb.append("<span style='background-color: #C9F7AD' ").append("title='").append(macroName).append("'>");
		}
		sb.append(identity).append(": ");
		if (trusted && !MapTool.getPlayer().isGM()) {
			sb.append("</span>");
		}

		sb.append("</td><td valign=top>");

		Color color = MapTool.getFrame().getCommandPanel().getTextColorWell().getColor();
		if (color != null) {
			sb.append("<span style='color:#").append(String.format("%06X", (color.getRGB() & 0xFFFFFF))).append("'>");
		}
		sb.append(msg);
		if (color != null) {
			sb.append("</span>");

			sb.append("</td>");

			sb.append("</tr></table>");

			MacroContext context = new MacroContext();
			context.addTransform(msg);

			MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), sb.toString()));
		}
	}
	
	private String getSelf() {
		return MapTool.getPlayer().getName();
	}

}
