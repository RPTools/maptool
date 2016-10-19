/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ReadOnlyLuaTable;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;

/**
 * @author Maluku
 *
 */
public class MapToolGlobals extends Globals {
	MapToolVariableResolver resolver;
	private LuaValue m_campaignProps = null;

//	private class CompilerUndumper implements Undumper {
//		@Override
//		public Prototype undump(InputStream stream, String chunkname)
//				throws IOException {
//			return compiler.compile(stream, chunkname);
//		}
//		
//	}
	
	private class SimpleLoader implements Loader {

		@Override
		public LuaFunction load(Prototype prototype, String chunkname, LuaValue env) throws IOException {
			return new LuaClosure(prototype, env);
		}
	}
	
	/**
	 * @param res 
	 * 
	 */
	public MapToolGlobals(MapToolVariableResolver res) {
		resolver = res;
		undumper = LoadState.instance;
		loader = new SimpleLoader();
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		LuaValue obj = super.rawget(key);
		if (!obj.isnil()) {
			return obj;
		}
		if (key.tojstring().equals("campaign")) {
			if (m_campaignProps != null) {
				return m_campaignProps;
			}
			return m_campaignProps = makeCampaignProps(MapTool.getCampaign().getCampaignProperties());
		}
		try {
			Object o = resolver.getVariable(key.checkjstring());
			if (o instanceof BigDecimal) {
				BigDecimal bd = (BigDecimal) o;
				if (isIntegerValue(bd)) {
					return LuaValue.valueOf(bd.intValue());
				}
				return LuaValue.valueOf(bd.doubleValue());
			}
			return LuaValue.valueOf(ObjectUtils.toString(o));
		} catch (ParserException e) {
			throw new LuaError(e);
		}

	}

	private LuaValue makeCampaignProps(CampaignProperties cp) {
		boolean trusted = MapTool.getParser().isMacroTrusted();
		LuaTable tokenprops = new LuaTable();
		LuaTable tokenpropsall = new LuaTable();
		Map<String, List<TokenProperty>> propmap = cp.getTokenTypeMap();
		for (String group : propmap.keySet()) {
			LuaTable tab = new LuaTable();
			for (TokenProperty p : propmap.get(group)) {
				LuaValue val = formatProp(p, trusted);
				tab.insert(0, val);
				tokenpropsall.insert(0, val);
			}
			tokenprops.rawset(group, tab);
		}

		LuaTable campaign = new LuaTable();
		campaign.rawset("allTokenProperties", tokenpropsall);
		campaign.rawset("tokenProperties", tokenprops);

		LuaTable states = new LuaTable();
		LuaTable statesall = new LuaTable();
		Map<String, BooleanTokenOverlay> statemap = cp.getTokenStatesMap();
		for (BooleanTokenOverlay state : statemap.values()) {
			String group = state.getGroup();
			if (group == null) {
				group = "No Group";
			}
			LuaValue tab = states.rawget(group);
			if (tab == null || tab == LuaValue.NIL) {
				tab = new LuaTable();
				states.rawset(group, tab);
			}
			LuaTable s = overlay(state, trusted);
			tab.rawset(state.getName(), s);
			statesall.rawset(state.getName(), s);

		}
		campaign.rawset("allStates", statesall);
		campaign.rawset("states", states);

		campaign.rawset("lights", makeCampaignLights(cp.getLightSourcesMap(), trusted));
		if (trusted) {
			LuaTable bars = new LuaTable();
			LuaTable barsall = new LuaTable();
			Map<String, BarTokenOverlay> barmap = cp.getTokenBarsMap();

			for (BarTokenOverlay bar : barmap.values()) {
				String group = bar.getGroup();
				if (group == null) {
					group = "No Group";
				}
				LuaValue tab = bars.rawget(group);
				if (tab == null || tab == LuaValue.NIL) {
					tab = new LuaTable();
					bars.rawset(group, tab);
				}
				LuaTable s = overlay(bar, trusted);
				tab.rawset(bar.getName(), s);
				barsall.rawset(bar.getName(), s);

			}
			campaign.rawset("allBars", barsall);
			campaign.rawset("bars", bars);
			campaign.rawset("sight", makeCampaignSight(cp.getSightTypeMap()));
			Campaign c = MapTool.getCampaign();
			campaign.rawset("id", valOf(c.getId()));
			campaign.rawset("initiativeMovementLocked", LuaValue.valueOf(cp.isInitiativeMovementLock()));
			campaign.rawset("initiativeOwnerPermissions", LuaValue.valueOf(cp.isInitiativeOwnerPermissions()));
			LuaTable zinfo = new LuaTable();
			for (Zone z : c.getZones()) {
				zinfo.rawset(z.getName(), z.getId().toString());
			}
			campaign.rawset("zones", zinfo);

			LuaTable tinfo = new LuaTable();
			for (LookupTable table : c.getLookupTableMap().values()) {
				tinfo.insert(0, LuaValue.valueOf(table.getName()));
			}
			campaign.rawset("tables", tinfo);

			LuaTable rinfo = new LuaTable();
			for (String rr : c.getRemoteRepositoryList()) {
				rinfo.insert(0, LuaValue.valueOf(rr));
			}
			campaign.rawset("remoteRepository", rinfo);

		}

		return new ReadOnlyLuaTable(campaign);
	}
	
	private LuaTable formatProp(TokenProperty p, boolean trusted) {
		LuaTable o = new LuaTable();
		o.rawset("name", valOf(p.getName()));
		o.rawset("short", valOf(p.getShortName()));
		o.rawset("showOnSheet", valOf(p.isShowOnStatSheet()));
		o.rawset("default", valOf(p.getDefaultValue()));
		if (trusted) {
			o.rawset("showGM", LuaValue.valueOf(p.isGMOnly()));
			o.rawset("showOwner", LuaValue.valueOf(p.isOwnerOnly()));
		}
		return o;
	}

	private LuaTable overlay(AbstractTokenOverlay over, boolean trusted) {
		LuaTable o = new LuaTable();
		o.rawset("name", valOf(over.getName()));
		o.rawset("group", valOf(over.getGroup()));
		if (trusted) {
			o.rawset("order", LuaValue.valueOf(over.getOrder()));
			o.rawset("mouseover", LuaValue.valueOf(over.isMouseover()));
			o.rawset("opacity", LuaValue.valueOf(over.getOpacity()));
			o.rawset("showGM", LuaValue.valueOf(over.isShowGM()));
			o.rawset("showOwner", LuaValue.valueOf(over.isShowOwner()));
			o.rawset("showOthers", LuaValue.valueOf(over.isShowOthers()));
			if (over instanceof BarTokenOverlay) {
				o.rawset("increments", LuaValue.valueOf(((BarTokenOverlay) over).getIncrements()));
				o.rawset("side", valOf(((BarTokenOverlay) over).getSide()));
			}
		}
		return o;
	}

	private LuaTable makeCampaignSight(Map<String, SightType> sightTypeMap) {
		LuaTable sight = new LuaTable();
		for (SightType st : sightTypeMap.values()) {
			LuaTable s = new LuaTable();
			s.rawset("name", valOf(st.getName()));
			s.rawset("distance", LuaValue.valueOf(st.getDistance()));
			s.rawset("multiplier", LuaValue.valueOf(st.getMultiplier()));
			s.rawset("arc", LuaValue.valueOf(st.getArc()));
			s.rawset("shape", valOf(st.getShape()));
			s.rawset("personalLightSource", lightsource(st.getPersonalLightSource(), true));
			sight.rawset(st.getName(), s);
		}
		return sight;
	}

	private static LuaValue valOf(Object text) {
		if (text == null) {
			return LuaValue.NIL;
		}
		return LuaValue.valueOf(text.toString());
	}

	private LuaValue lightsource(LightSource ls, boolean trusted) {
		if (ls != null) {
			LuaTable lightsource = new LuaTable();
			lightsource.rawset("name", valOf(ls.getName()));
			lightsource.rawset("maxRange", LuaValue.valueOf(ls.getMaxRange()));
			lightsource.rawset("type", valOf(ls.getType()));
			if (trusted) {
				LuaTable lights = new LuaTable();
				for (Light l : ls.getLightList()) {
					LuaTable light = new LuaTable();
					//private double facingOffset;
					//				private double radius;
					//				private double arcAngle;
					//				private ShapeType shape;
					//				private boolean isGM;
					//				private boolean ownerOnly;
					light.rawset("facingOffset", LuaValue.valueOf(l.getFacingOffset()));
					light.rawset("radius", LuaValue.valueOf(l.getRadius()));
					light.rawset("arcAngle", LuaValue.valueOf(l.getArcAngle()));
					light.rawset("shape", valOf(l.getShape()));
					light.rawset("isGM", LuaValue.valueOf(l.isGM()));
					light.rawset("ownerOnly", LuaValue.valueOf(l.isOwnerOnly()));
					lights.insert(0, light);
				}
				lightsource.rawset("lights", lights);
			}
		}
		return LuaValue.NIL;
	}

	private LuaTable makeCampaignLights(Map<String, Map<GUID, LightSource>> lightSourcesMap, boolean trusted) {
		LuaTable lights = new LuaTable();
		for (java.util.Map.Entry<String, Map<GUID, LightSource>> e : lightSourcesMap.entrySet()) {
			LuaTable lightcat = new LuaTable();
			for (LightSource ls : e.getValue().values()) {
				lightcat.rawset(LuaValue.valueOf(ls.getName()), lightsource(ls, trusted));
			}
			lights.rawset(LuaValue.valueOf(e.getKey()), lightcat);
		}
		return lights;
	}

	private boolean isIntegerValue(BigDecimal bd) {
		return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
	}

	@Override
	public void rawset(LuaValue key, LuaValue value) {
		if (key.isstring() && key.tojstring().equals("token")) {
			if (value instanceof MapToolToken) {
				MapToolToken mtt = (MapToolToken) value;
				if (!mtt.isSelfOrTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "switchToken")));
				}
				if (mtt.hasToken()) {
					resolver.setTokenIncontext(mtt.getToken());
				} else {
					resolver.setTokenIncontext(null);
				}
			} else
				throw new LuaError("token must be a maptool token");
		}
		super.rawset(key, value);
	}

}
