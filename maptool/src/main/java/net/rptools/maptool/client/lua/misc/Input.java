/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ReadOnlyLuaTable;
import net.rptools.maptool.client.functions.InputFunction;
import net.rptools.maptool.client.functions.InputFunction.ColumnPanel;
import net.rptools.maptool.client.functions.InputFunction.FixupComponentAdapter;
import net.rptools.maptool.client.functions.InputFunction.InputPanel;
import net.rptools.maptool.client.functions.InputFunction.InputType;
import net.rptools.maptool.client.functions.InputFunction.VarSpec;
import net.rptools.maptool.client.functions.InputFunction.InputType.OptionMap;
import net.rptools.maptool.client.functions.StrListFunctions;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.EvaluationException;
import net.rptools.parser.function.ParameterException;

import org.apache.commons.lang.StringUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 * 
 */
public class Input extends VarArgFunction {

	private final Token tokenInContext;

	public Input(Token tokenInContext) {
		super();
		this.tokenInContext = tokenInContext;
	}

	public static List<VarSpec> createVarSpecs(Varargs parameters)
			throws EvaluationException, ParserException {
		int count = 0;
		List<LuaValue> varStrings = new ArrayList<LuaValue>();
		for (int i = 1; i <= parameters.narg(); i++) {
			LuaValue param = parameters.arg(i);
			if (parameters.isstring(i)) {
				String paramStr = parameters.checkjstring(i);
				if (StringUtils.isEmpty(paramStr)) {
					continue;
				}
				// Multiple vars can be packed into a string, separated by "##"
				List<String> substrings = new ArrayList<String>();
				StrListFunctions.parse(paramStr, substrings, "##");
				for (String varString : substrings) {
					if (StringUtils.isEmpty(paramStr)) {
						continue;
					}
					varStrings.add(valueOf(varString));
				}
			} else if (param.istable()) {
				LuaTable t = param.checktable();
				Varargs n = t.inext(LuaValue.valueOf(0));
				if (n.isnil(1) || !t.get(valueOf("name")).isnil()) {
					varStrings.add(param);
				} else {
					for (; !n.arg1().isnil(); n = t.inext(n.arg1())) {
						varStrings.add(n.arg(2));
					}
				}
			} else {
				varStrings.add(param);
			}
		}

		// Create VarSpec objects from each variable's specifier string
		List<VarSpec> varSpecs = new ArrayList<VarSpec>();
		for (LuaValue specifier : varStrings) {
			count++;
			try {
				if (specifier.isstring()) {
					varSpecs.add(new VarSpec(specifier.checkjstring()));
				} else if (specifier.istable()) {
					LuaTable table = specifier.checktable();
					LuaValue name = table.get(valueOf("name"));
					if (!name.isstring() || name.isnil())
						name = valueOf("var" + count);
					LuaValue type = table.get(valueOf("type"));
					InputType inputType = InputType.TEXT;
					if (type.isstring()) {
						String t = type.checkjstring();
						for (InputType it : InputType.values()) {
							if (it.name().equalsIgnoreCase(t)) {
								inputType = it;
								break;
							}
						}
					}
					LuaValue values = table.get(valueOf("content"));
					List<String> v = new ArrayList<String>();
					List<Object> o = new ArrayList<Object>();
					OptionMap options = inputType.parseOptionString("");
					boolean token = false;
					if (values.istable()) {
						for (LuaValue val : LuaConverters.arrayIterate(values.checktable())) {
							if (val instanceof MapToolToken) {
								if (!token) {
									token = true;
									options.put("ICON", "TRUE");
								}
								MapToolToken t = (MapToolToken)val;
								v.add(t.rawget("name").toString() + t.rawget("image").toString());
								o.add(val);
							} else {
								o.add(val);
								v.add(val.toString());
							}
						}
					} else if (values.isnil()) {
						v.add(null);
						o.add(NIL);
					} else {
						o.add(values);
						v.add(values.toString());
					}
					LuaValue prompt = table.get(valueOf("prompt"));
					for (Varargs n = table.next(LuaValue.NIL); !n.arg1()
							.isnil(); n = table.next(n.arg1())) {
						options.put(n.arg1().toString(), n.arg(2).toString());
					}
					if (v.isEmpty()) {
						varSpecs.add(new VarSpec(inputType, name.checkjstring(), "", prompt.isnil() ? name.checkjstring() : prompt.toString(), options));
					} else {
						varSpecs.add(new VarSpec(inputType, name.checkjstring(), v, prompt.isnil() ? name.checkjstring() : prompt.toString(), options, o));
					}
					
				}
			} catch (VarSpec.SpecifierException se) {
				throw new ParameterException(se.msg);
			} catch (InputType.OptionException oe) {
				throw new ParameterException(I18N.getText(
						"macro.function.input.invalidOptionType", oe.key,
						oe.value, oe.type, specifier));
			}
		}
		return varSpecs;
	}

	@Override
	public Varargs invoke(Varargs args) {
		try {
			List<VarSpec> varSpecs = createVarSpecs(args);
			// UI step 2 - build the panel with the input fields
			InputPanel ip = new InputPanel(varSpecs);

			// Calculate the height
			// TODO: remove this workaround
			int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
			int maxHeight = screenHeight * 3 / 4;
			Dimension ipPreferredDim = ip.getPreferredSize();
			if (maxHeight < ipPreferredDim.height) {
				ip.modifyMaxHeightBy(maxHeight - ipPreferredDim.height);
			}

			// UI step 3 - show the dialog
			JOptionPane jop = new JOptionPane(ip, JOptionPane.PLAIN_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			JDialog dlg = jop.createDialog(MapTool.getFrame(),
					InputFunction.createTitle(tokenInContext));

			// Set up callbacks needed for desired runtime behavior
			dlg.addComponentListener(new FixupComponentAdapter(ip));

			dlg.setVisible(true);
			int dlgResult = JOptionPane.CLOSED_OPTION;
			try {
				dlgResult = (Integer) jop.getValue();
			} catch (NullPointerException npe) {
			}
			dlg.dispose();

			if (dlgResult == JOptionPane.CANCEL_OPTION
					|| dlgResult == JOptionPane.CLOSED_OPTION)
				return NONE;

			LuaTable result = new LuaTable();
			LuaTable tabs = new LuaTable();
			// Finally, assign values from the dialog box to the variables
			for (ColumnPanel cp : ip.columnPanels) {
				List<VarSpec> panelVars = cp.varSpecs;
				List<JComponent> panelControls = cp.inputFields;
				int numPanelVars = panelVars.size();
				LuaTable allAssignments = new LuaTable();
				for (int varCount = 0; varCount < numPanelVars; varCount++) {
					VarSpec vs = panelVars.get(varCount);
					JComponent comp = panelControls.get(varCount);
					LuaValue newValue = null;
					switch (vs.inputType) {
					case TEXT: {
						newValue = valueOf(((JTextField) comp).getText().trim());
						break;
					}
					case LIST: {
						Integer index = ((JComboBox) comp).getSelectedIndex();
						if (vs.optionValues.optionEquals("VALUE", "OBJECT")) {
							if (vs.objectList != null) {
								newValue = (LuaValue)vs.objectList.get(index);
							} else {
								newValue = valueOf(vs.valueList.get(index).trim());
							}
						} else if (vs.optionValues.optionEquals("VALUE", "STRING")) {
							newValue = valueOf(vs.valueList.get(index).trim());
						} else { // default is "NUMBER"
							newValue = valueOf(index);
						}
						break;
					}
					case CHECK: {
						Integer value = ((JCheckBox) comp).isSelected() ? 1 : 0;
						newValue = valueOf(value);
						break;
					}
					case RADIO: {
						// This code assumes that the Box container returns
						// components
						// in the same order that they were added.
						Component[] comps = ((Box) comp).getComponents();
						int componentCount = 0;
						Integer index = 0;
						for (Component c : comps) {
							if (c instanceof JRadioButton) {
								JRadioButton radio = (JRadioButton) c;
								if (radio.isSelected())
									index = componentCount;
							}
							componentCount++;
						}
						if (vs.optionValues.optionEquals("VALUE", "OBJECT")) {
							if (vs.objectList != null) {
								newValue = (LuaValue)vs.objectList.get(index);
							} else {
								newValue = valueOf(vs.valueList.get(index).trim());
							}
						} else if (vs.optionValues.optionEquals("VALUE", "STRING")) {
							newValue = valueOf(vs.valueList.get(index).trim());
						} else { // default is "NUMBER"
							newValue = valueOf(index);
						}
						break;
					}
					case LABEL: {
						newValue = null;
						// The variable name is ignored and not set.
						break;
					}
					case PROPS: {
						// Read out and assign all the subvariables.
						// The overall return value is a property string (as in
						// StrPropFunctions.java) with all the new settings.
						Component[] comps = ((JPanel) comp).getComponents();
						StringBuilder sb = new StringBuilder();
						int setVars = 0; // "NONE", no assignments made
						if (vs.optionValues.optionEquals("SETVARS", "SUFFIXED"))
							setVars = 1;
						if (vs.optionValues.optionEquals("SETVARS",
								"UNSUFFIXED"))
							setVars = 2;
						if (vs.optionValues.optionEquals("SETVARS", "TRUE"))
							setVars = 2; // for backward compatibility
						for (int compCount = 0; compCount < comps.length; compCount += 2) {
							String key = ((JLabel) comps[compCount]).getText()
									.split("\\:")[0]; // strip trailing colon
							String value = ((JTextField) comps[compCount + 1])
									.getText();
							sb.append(key);
							sb.append("=");
							sb.append(value);
							sb.append(" ; ");
							switch (setVars) {
							case 0:
								// Do nothing
								break;
							case 1:
								allAssignments.rawset(key + "_", value);
								result.rawset(key + "_", value);
								break;
							case 2:
								allAssignments.rawset(key, value);
								result.rawset(key, value);
								break;
							}
						}
						newValue = valueOf(sb.toString().trim());
						break;
					}
					default:
						// should never happen
						newValue = null;
						break;
					}
					// Set the variable to the value we got from the dialog box.
					if (newValue != null) {
						result.rawset(vs.name, newValue);
						allAssignments.rawset(vs.name, newValue);
					}
				}
				if (cp.tabVarSpec != null) {
					result.rawset(cp.tabVarSpec.name, allAssignments);
					tabs.rawset(cp.tabVarSpec.name, allAssignments);
				}
			}
			return varargsOf(result, tabs);
		} catch (Exception e) {
			throw new LuaError(e);
		}
	}
}
