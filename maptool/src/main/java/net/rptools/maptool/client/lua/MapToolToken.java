/**
 * 
 */
package net.rptools.maptool.client.lua;

import static net.rptools.maptool.client.lua.LuaConverters.isTrue;

import java.util.Locale;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ReadOnlyLuaTable;
import net.rptools.maptool.client.functions.TokenGMNameFunction;
import net.rptools.maptool.client.functions.TokenHaloFunction;
import net.rptools.maptool.client.functions.TokenImage;
import net.rptools.maptool.client.functions.TokenImage.imageType;
import net.rptools.maptool.client.functions.TokenInitFunction;
import net.rptools.maptool.client.functions.TokenInitHoldFunction;
import net.rptools.maptool.client.functions.TokenLabelFunction;
import net.rptools.maptool.client.functions.TokenNameFunction;
import net.rptools.maptool.client.functions.TokenPropertyFunctions;
import net.rptools.maptool.client.functions.TokenVisibleFunction;
import net.rptools.maptool.client.lua.token.AddToInitative;
import net.rptools.maptool.client.lua.token.BringToFront;
import net.rptools.maptool.client.lua.token.CanSee;
import net.rptools.maptool.client.lua.token.ClearLights;
import net.rptools.maptool.client.lua.token.CreateMacro;
import net.rptools.maptool.client.lua.token.Distance;
import net.rptools.maptool.client.lua.token.GetOwners;
import net.rptools.maptool.client.lua.token.HasLights;
import net.rptools.maptool.client.lua.token.ImageFunc;
import net.rptools.maptool.client.lua.token.IsOwnedByAll;
import net.rptools.maptool.client.lua.token.IsOwner;
import net.rptools.maptool.client.lua.token.MatchingProperties;
import net.rptools.maptool.client.lua.token.Move;
import net.rptools.maptool.client.lua.token.SelectToken;
import net.rptools.maptool.client.lua.token.SendToBack;
import net.rptools.maptool.client.lua.token.SetOwner;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.apache.commons.lang.StringUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class MapToolToken extends LuaTable implements IRepresent {
	private static final String ID = "id";
	private static final String LABEL = "label";
	private static final String NAME = "name";
	private static final String NOTES = "notes";
	private static final String GM_NAME = "gm_name";
	private static final String GMNAME = "gmname";
	private static final String GMNOTES = "gmnotes";
	private static final String VISIBLE = "visible";
	private static final String HALO = "halo";
	private static final String INIT = "init";
	private static final String INITIATIVE = "initiative";
	private static final String INIT_HOLD = "inithold";
	private static final String INITIATIVE_HOLD = "initiativehold";
	private static final String INITIATIVE_HOLD2 = "initiative_hold";
	private static final String LAYER = "layer";
	private static final String PROPERTIES = "properties";
	private static final String SIGHT = "sight";
	private static final String SIGHT_TYPE = "sighttype";
	private static final String OWNER_ONLY_VISIBLE = "owneronlyvisible";
	private static final String PROPERTY_TYPE = "propertytype";
	private static final String SIZE = "size";
	private static final String SPEECH = "speech";
	private static final String STATES = "states";
	private static final String SHAPE = "shape";
	private static final String BARS = "bars";
	private static final String DRAW_ORDER = "draworder";
	private static final String PC = "pc";
	private static final String NPC = "npc";
	private static final String TYPE = "type";
	private static final String MACROS = "macros";
	//	private static final String DRAW_ORDER2 = "z";
	//	private static final String X = "x";
	//	private static final String Y = "y";
	private static final String FACING = "facing";
	private static final String IMAGE = "image";
	private static final String HANDOUT = "handout";
	private static final String PORTRAIT = "portrait";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String SNAP_TO_GRID = "snaptogrid";
	private static final String F_BRINGTOFRONT = "bringtofront";
	private static final String F_SENDTOBACK = "sendtoback";
	private static final String F_CANSEE = "cansee";
	private static final String F_SELECT = "select";
	private static final String F_DESELECT = "deselect";
	private static final String F_FORCE_LAYER = "setlayernoforce";
	private static final String LIGHTS = "lights";
	private static final String F_HAS_LIGHTS = "haslights";
	private static final String F_HAS_LIGHTSOURCE = "haslightsource";
	private static final String F_CLEAR_LIGHTS = "clearlights";
	private static final String F_MATCH_PROPS = "getmatchingproperties";
	private static final String F_GET_OWNERS = "getowners";
	private static final String F_SET_OWNERS = "setowner";
	private static final String F_IS_OWNED_BY_ALL = "isownedbyall";
	private static final String F_IS_OWNER = "isowner";
	private static final String F_IMAGE = "getimage";
	private static final String F_HANDOUT = "gethandout";
	private static final String F_PORTRAIT = "getportrait";
	private static final String F_MOVE = "move";
	private static final String F_DISTANCE = "getdistance";
	private static final String F_ADDTOINITIATIVE = "addtoinitiative";
	private static final String F_CREATEMACRO = "createmacro";
	//TODO trusted Macro und so
	private boolean isSelf = false;
	private Token token;
	private String lastfunc = "";
	private LuaTable m_lightsTable;
	private MapToolVariableResolver resolver;

	public MapToolToken(Token t, MapToolVariableResolver resolver) {
		this(t, false, resolver);
	}

	public MapToolToken(Token t, boolean mainToken, MapToolVariableResolver resolver) {
		token = t;
		isSelf = mainToken;
		this.resolver = resolver;
	}

	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	public void set(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(LuaValue key, LuaValue value) {
		try {
			String k = key.tojstring();
			lastfunc = k;
			if (token == null) {
				throw new ParserException(I18N.getText("macro.function.general.noImpersonated", k));
			}
			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
			Zone zone = renderer.getZone();
			switch (k.toLowerCase(Locale.ENGLISH)) {
			case LABEL:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					TokenLabelFunction.getInstance().setLabel(token, value.isnil() ? null : value.tojstring());
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.label"));
				return;
			case NAME:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					String name = value.tojstring();
					if (StringUtils.isEmpty(name)) {
						throw new ParserException(I18N.getText("lineParser.emptyTokenName"));
					}
					TokenNameFunction.getInstance().setName(token, name);
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.name"));
				return;
			case NOTES:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					String note = null;
					if (value.isstring()) {
						note = value.tojstring();
					}
					getToken().setNotes(note);
					MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.name"));
				return;
			case OWNER_ONLY_VISIBLE:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					getToken().setVisibleOnlyToOwner(value.checkboolean());
					MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getOwnerOnlyVisible"));
				return;
			case GMNAME:
			case GM_NAME:
				if (MapTool.getParser().isMacroTrusted()) {
					TokenGMNameFunction.getInstance().setGMName(token, value.isnil() ? null : value.tojstring());
				}
				return;
			case GMNOTES:
				if (MapTool.getParser().isMacroTrusted()) {
					if (token != null) {
						token.setGMNotes(value.isnil() ? null : value.tojstring());
						MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
						return;
					}
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.gmNotes"));
			case HALO:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					TokenHaloFunction.getInstance().setHalo(token, value);
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.halo"));
				return;
			case VISIBLE:
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setVisible"));
				}
				TokenVisibleFunction.getInstance().setVisible(token, isTrue(value));
				return;
			case INIT:
			case INITIATIVE:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					TokenInitFunction.getInstance().setTokenValue(token, value);
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.init"));
				return;
			case INIT_HOLD:
			case INITIATIVE_HOLD:
			case INITIATIVE_HOLD2:
				if (isSelf || MapTool.getParser().isMacroTrusted()) {
					TokenInitHoldFunction.getInstance().setTokenValue(token, isTrue(value));
				} else
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.initHold"));
				return;
			case LAYER:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.layer"));
				}
				setLayer(value, true);
				return;
			case PROPERTY_TYPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getPropertyType"));
				}
				getToken().setPropertyType(value.checkjstring());
				return;
			case SIGHT:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setHasSight"));
				}
				getToken().setHasSight(value.checkboolean());
				return;
			case SIGHT_TYPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSightType"));
				}
				getToken().setSightType(value.checkjstring());
				return;
			case SIZE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSize"));
				}
				TokenPropertyFunctions.setSize(getToken(), value.checkjstring());
				return;
			case STATES:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setState"));
				}
				if (value.checkboolean()) {
					for (String state : MapTool.getCampaign().getCampaignProperties().getTokenStatesMap().keySet()) {
						token.setState(state, Boolean.TRUE);
					}
				} else {
					for (String state : MapTool.getCampaign().getCampaignProperties().getTokenStatesMap().keySet()) {
						token.setState(state, null);
					}
				}
				MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);
				MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
				return;
			case BARS:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setBar"));
				}
				if (value.isnil()) {
					for (String state : MapTool.getCampaign().getCampaignProperties().getTokenBarsMap().keySet()) {
						token.setState(state, null);
					}
				} else {
					Double val = Double.valueOf(value.checkdouble());
					for (String state : MapTool.getCampaign().getCampaignProperties().getTokenBarsMap().keySet()) {
						token.setState(state, val);
					}
				}
				MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);
				MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
				return;
			case DRAW_ORDER:
				//				case DRAW_ORDER2:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setDrawOrder"));
				}
				token.setZOrder(value.checkint());
				zone.putToken(token);
				MapTool.serverCommand().putToken(zone.getId(), token);
				renderer.flushLight();
				return;
			//				case X: move/ getX mit units/cellgrid
			//					if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
			//						throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setX"));
			//					}
			//					token.setX(value.checkint());
			//					zone.putToken(token);
			//					MapTool.serverCommand().putToken(zone.getId(), token);
			//					renderer.flushLight();
			//					return;
			//				case Y:
			//					if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
			//						throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setY"));
			//					}
			//					token.setY(value.checkint());
			//					zone.putToken(token);
			//					MapTool.serverCommand().putToken(zone.getId(), token);
			//					renderer.flushLight();
			//					return;
			case HANDOUT:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.set" + k));
				}
				TokenImage.setHandout(getToken(), value.checkjstring());
				return;
			case PORTRAIT:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.set" + k));
				}
				TokenImage.setPortrait(getToken(), value.checkjstring());
				return;
			case IMAGE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.set" + k));
				}
				TokenImage.setImage(getToken(), value.checkjstring());
				return;
			case SHAPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.set" + k));
				}
				Token.TokenShape newShape = Token.TokenShape.valueOf(value.checkjstring().toUpperCase().trim().replace(" ", "_"));
				token.setShape(newShape);
				MapTool.serverCommand().putToken(zone.getId(), token);
				renderer.flushLight();
				zone.putToken(token);
				return;
			case FACING:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setDrawOrder"));
				}
				if (value.isnil()) {
					token.setFacing(null);
				} else {
					token.setFacing(value.checkint());
				}
				MapTool.serverCommand().putToken(zone.getId(), token);
				renderer.flushLight();
				zone.putToken(token);
				return;
			case WIDTH:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setTokenWidth"));
				}
				token.setSnapToScale(false);
				token.setScaleX(value.checkdouble() / token.getWidth());
				token.setScaleY(token.getBounds(zone).getHeight() / token.getHeight());
				MapTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				return;
			case HEIGHT:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setTokenHeight"));
				}
				token.setSnapToScale(false);
				token.setScaleX(token.getBounds(zone).getWidth() / token.getWidth());
				token.setScaleY(value.checkdouble() / token.getHeight());
				MapTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				return;
			case SNAP_TO_GRID:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSnapToGrid"));
				}
				token.setSnapToGrid(value.checkboolean());
				MapTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				return;
			case PC:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSnapToGrid"));
				}
				if (value.isboolean() && value.checkboolean()) {
					token.setType(Type.PC);
				}
				MapTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				return;
			case NPC:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSnapToGrid"));
				}
				if (value.isboolean() && value.checkboolean()) {
					token.setType(Type.NPC);
				}
				MapTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				return;
			case TYPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSnapToGrid"));
				}
				if (value.isstring() && PC.equalsIgnoreCase(value.checkjstring())) {
					token.setType(Type.PC);
				} else {
					token.setType(Type.NPC);
				}
				MapTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				return;
			default:
				error("table is read-only");
			}
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}

	private static LuaValue valOf(Object text) {
		if (text == null) {
			return LuaValue.NIL;
		}
		return LuaValue.valueOf(text.toString());
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		LuaValue obj = super.rawget(key);
		if (!obj.isnil()) {
			return obj;
		}
		try {
			String k = key.tojstring();
			lastfunc = k;
			if (token == null) {
				throw new ParserException(I18N.getText("macro.function.general.noImpersonated", k));
			}
			switch (k.toLowerCase(Locale.ENGLISH)) {
			case ID:
				return valOf(token.getId());
			case LABEL:
				if (!isSelf && !MapTool.getParser().isMacroTrusted() && !visibleToMe()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.label"));
				}
				return valOf(TokenLabelFunction.getInstance().getLabel(token));
			case NAME:
				if (!isSelf && !MapTool.getParser().isMacroTrusted() && !visibleToMe()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.name"));
				}
				return valOf(TokenNameFunction.getInstance().getName(token));
			case NOTES:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.notes"));
				}
				return valOf(getToken().getNotes());
			case OWNER_ONLY_VISIBLE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getOwnerOnlyVisible"));
				}
				return LuaValue.valueOf(getToken().isVisibleOnlyToOwner());
			case GMNAME:
			case GM_NAME:
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.gmName"));
				}
				return valOf(TokenGMNameFunction.getInstance().getGMName(token));
			case GMNOTES:
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.gmNotes"));
				}
				if (token != null && token.getGMNotes() != null) {
					return valOf(token.getGMNotes());
				}
				return LuaValue.NIL;
			case HALO:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.halo"));
				}
				return LuaValue.valueOf(TokenHaloFunction.getInstance().getHalo(token).toString());
			case VISIBLE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getVisible"));
				}
				return LuaValue.valueOf(TokenVisibleFunction.getInstance().getVisible(token).intValue() > 0);
			case INIT:
			case INITIATIVE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.init"));
				}
				return LuaValue.valueOf(TokenInitFunction.getInstance().getTokenValue(token).toString());
			case INIT_HOLD:
			case INITIATIVE_HOLD2:
			case INITIATIVE_HOLD:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.initHold"));
				}
				Object v = TokenInitHoldFunction.getInstance().getTokenValue(token);
				if (v instanceof Number) {
					return LuaValue.valueOf(((Number) v).intValue() > 0);
				}
				return LuaValue.valueOf(v.toString());
			case LAYER:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.layer"));
				}
				if (token != null && token.getLayer() != null) {
					return valOf(token.getLayer());
				}
				return LuaValue.NIL;
			case LIGHTS:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.initHold"));
				}
				return getLightsTable();
			case PROPERTIES:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.properties"));
				}
				return new TokenProperties(this);
			case PROPERTY_TYPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getPropertyType"));
				}
				return valOf(getToken().getPropertyType());
			case SIGHT:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getHasSight"));
				}
				return LuaValue.valueOf(getToken().getHasSight());
			case SIGHT_TYPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getSightType"));
				}
				return valOf(getToken().getSightType());
			case SIZE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getSize"));
				}
				return valOf(TokenPropertyFunctions.getSize(getToken()));
			case SPEECH:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getSpeechNames"));
				}
				return new TokenSpeech(this);
			case STATES:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getState"));
				}
				return new TokenState(this);
			case BARS:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getBar"));
				}
				return new TokenBar(this);
			case DRAW_ORDER:
				//				case DRAW_ORDER2:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getDrawOrder"));
				}
				return valueOf(token.getZOrder());
			//				case X:
			//					if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
			//						throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getX"));
			//					}
			//					return valueOf(token.getX());
			//				case Y:
			//					if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
			//						throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getY"));
			//					}
			//					return valueOf(token.getY());
			case FACING:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.setDrawOrder"));
				}
				return valOf(token.getFacing());
			case IMAGE:
				if ((isSelf || !MapTool.getParser().isMacroTrusted()) && token != null && token.getImageAssetId() != null) {
					return valOf("asset://" + token.getImageAssetId());
				}
				return valOf("");
			case PORTRAIT:
				if ((isSelf || !MapTool.getParser().isMacroTrusted()) && token != null && token.getPortraitImage() != null) {
					return valOf("asset://" + token.getPortraitImage());
				}
				return valOf("");
			case HANDOUT:
				if ((isSelf || !MapTool.getParser().isMacroTrusted()) && token != null && token.getCharsheetImage() != null) {
					return valOf("asset://" + token.getCharsheetImage());
				}
				return valOf("");
			case SHAPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getTokenShape"));
				}
				return valOf(token.getShape().toString());
			case WIDTH:
				return valOf(token.getBounds(MapTool.getFrame().getCurrentZoneRenderer().getZone()).getWidth());
			case HEIGHT:
				return valOf(token.getBounds(MapTool.getFrame().getCurrentZoneRenderer().getZone()).getHeight());
			case SNAP_TO_GRID:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.isSnapToGrid"));
				}
				return valOf(token.isSnapToGrid());
			case PC:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.isPC"));
				}
				return valueOf(token.getType() == Type.PC);
			case NPC:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.isNPC"));
				}
				return valueOf(token.getType() == Type.NPC);
			case TYPE:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getType"));
				}
				return valOf(token.getType());
			case MACROS:
				if (!isSelf && !MapTool.getParser().isMacroTrusted()) {
					throw new ParserException(I18N.getText("macro.function.general.noPerm", "token.getMacros"));
				}
				return new MapToolMacroIndexes(this, resolver);
			case F_ADDTOINITIATIVE:
				return new AddToInitative(this);
			case F_BRINGTOFRONT:
				return new BringToFront(this);
			case F_SENDTOBACK:
				return new SendToBack(this);
			case F_CANSEE:
				return new CanSee(this);
			case F_SELECT:
				return token == null ? LuaValue.NIL : new SelectToken(false, token.getId());
			case F_DESELECT:
				return token == null ? LuaValue.NIL : new SelectToken(true, token.getId());
			case F_HAS_LIGHTS:
			case F_HAS_LIGHTSOURCE:
				return new HasLights(this);
			case F_CLEAR_LIGHTS:
				return new ClearLights(this);
			case F_MATCH_PROPS:
				return new MatchingProperties(this);
			case F_GET_OWNERS:
				return new GetOwners(this);
			case F_SET_OWNERS:
				return new SetOwner(this);
			case F_IS_OWNER:
				return new IsOwner(this);
			case F_IS_OWNED_BY_ALL:
				return new IsOwnedByAll(this);
			case F_IMAGE:
				return new ImageFunc(this, imageType.TOKEN_IMAGE);
			case F_PORTRAIT:
				return new ImageFunc(this, imageType.TOKEN_PORTRAIT);
			case F_HANDOUT:
				return new ImageFunc(this, imageType.TOKEN_HANDOUT);
			case F_FORCE_LAYER:
				return new OneArgFunction() {
					@Override
					public LuaValue call(LuaValue arg) {
						setLayer(arg, false);
						return LuaValue.NIL;
					}
				};
			case F_MOVE:
				return new Move(this);
			case F_DISTANCE:
				return new Distance(this);
			case F_CREATEMACRO:
				return new CreateMacro(this, resolver);

			}
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		return LuaValue.NIL;
	}

	public boolean visibleToMe() {
		if (token == null) {
			return false;
		}
		if (!token.isVisible()) {
			return false;
		}
		if (token.isVisibleOnlyToOwner()) {
			if (AppUtil.playerOwns(token)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	private LuaTable getLightsTable() {
		if (m_lightsTable != null) {
			return m_lightsTable;
		}
		LuaTable table = new LuaTable();
		for (String cat : MapTool.getCampaign().getLightSourcesMap().keySet()) {
			table.rawset(cat, new TokenLight(this, cat));
		}
		m_lightsTable = new ReadOnlyLuaTable(table, false);
		return m_lightsTable;
	}

	public boolean isSelfOrTrusted() {
		return isSelf || MapTool.getParser().isMacroTrusted();
	}

	public Token getToken() {
		if (token == null) {
			throw new LuaError(I18N.getText("macro.function.general.noImpersonated", lastfunc));
		}
		return token;
	}

	@Override
	public String tojstring() {
		if (token != null)
			return token.getId().toString();
		return super.tojstring();
	}

	@Override
	public LuaValue tostring() {
		if (token != null)
			return LuaString.valueOf(token.getId().toString());
		return super.tostring();
	}

	@Override
	public LuaString checkstring() {
		if (token != null)
			return LuaString.valueOf(token.getId().toString());
		// TODO Auto-generated method stub
		return super.checkstring();
	}

	@Override
	public String toString() {
		if (token != null)
			return token.getId().toString();
		return super.toString();
	}

	public boolean hasToken() {
		return token != null;
	}

	private void setLayer(LuaValue val, boolean force) {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setLayer")));
		}
		try {
			ZoneRenderer zoneR = MapTool.getFrame().getCurrentZoneRenderer();
			Zone zone = zoneR.getZone();
			TokenPropertyFunctions.setLayer(token, val.checkjstring(), force);
			MapTool.serverCommand().putToken(zone.getId(), token);
			zone.putToken(token);
			zoneR.flushLight();
			MapTool.getFrame().updateTokenTree();
		} catch (ParserException pe) {
			throw new LuaError(pe);
		}
	}

	@Override
	public Object export() {
		if (token != null) {
			return token.getId().toString();
		}
		return null;
	}
}