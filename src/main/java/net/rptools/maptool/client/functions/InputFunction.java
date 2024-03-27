/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.muntjak.tinylookandfeel.TinyComboBoxButton;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.InputFunction.InputType.OptionException;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.htmlframe.HTMLPane;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.ParameterException;
import org.apache.commons.lang.StringUtils;

// @formatter:off
// Jamz: Had to remove <pre> tags and add formatter:off due to Spotless 3.x error, still not fixed
// as of 6/2/18
// https://github.com/diffplug/spotless/issues/191
/**
 * The input() function prompts the user to input several variable values at once.
 *
 * <p>Each of the string parameters has the following format:
 * "varname|value|prompt|inputType|options"
 *
 * <p>Only the first section is required. varname - the variable name to be assigned value - sets
 * the initial contents of the input field prompt - UI text shown for the variable inputType -
 * specifies the type of input field options - a string of the form "opt1=val1; opt2=val2; ..."
 *
 * <p>The inputType field can be any of the following (defaults to TEXT): TEXT - A text field.
 * "value" sets the initial contents. The return value is the string in the text field. Option:
 * WIDTH=nnn sets the width of the text field (default 16). LIST - An uneditable combo box. "value"
 * populates the list, and has the form "item1,item2,item3..." (trailing empty strings are dropped)
 * The return value is the numeric index of the selected item. Option: SELECT=nnn sets the initial
 * selection (default 0). Option: VALUE=STRING returns the string contents of the selected item
 * (default NUMBER). Option: TEXT=FALSE suppresses the text of the list item (default TRUE). Option:
 * ICON=TRUE causes icon asset URLs to be extracted from the "value" and displayed (default FALSE).
 * Option: ICONSIZE=nnn sets the size of the icons (default 50). CHECK - A checkbox. "value" sets
 * the initial state of the box (anything but "" or "0" checks the box) The return value is 0 or 1.
 * No options. RADIO - A group of radio buttons. "value" is a list "name1, name2, name3, ..." which
 * sets the labels of the buttons. The return value is the index of the selected item. Option:
 * SELECT=nnn sets the initial selection (default 0). Option: ORIENT=H causes the radio buttons to
 * be laid out on one line (default V). Option: VALUE=STRING causes the return value to be the
 * string of the selected item (default NUMBER). LABEL - A label. The "varname" is ignored and no
 * value is assigned to it. Option: TEXT=FALSE, ICON=TRUE, ICONSIZE=nnn, as in the LIST type. PROPS
 * - A sub-panel with multiple text boxes. "value" contains a StrProp of the form "key1=val1;
 * key2=val2; ..." One text box is created for each key, populated with the matching value. Option:
 * SETVARS=SUFFIXED causes variable assignment to each key name, with appended "_" (default NONE).
 * Option: SETVARS=UNSUFFIXED causes variable assignment to each key name. TAB - A tabbed dialog tab
 * is created. Subsequent variables are contained in the tab. Option: SELECT=TRUE causes this tab to
 * be shown at start (default SELECT=FALSE).
 *
 * <p>All inputTypes except TAB accept the option SPAN=TRUE, which causes the prompt to be hidden
 * and the input control to span both columns of the dialog layout (default FALSE).
 *
 * @author knizia.fan
 */
// @formatter:on

public class InputFunction extends AbstractFunction {
  private static final Pattern ASSET_PATTERN =
      Pattern.compile("^(.*)((?:asset|lib|Image):(//)?[0-9a-z-A-Z ./]+)");

  /** The singleton instance. */
  private static final InputFunction instance = new InputFunction();

  private InputFunction() {
    super(1, -1, "input");
  }

  /**
   * Gets the singleton instance.
   *
   * @return the singleton instance for the class.
   */
  public static InputFunction getInstance() {
    return instance;
  }

  /** Enum of input types; also stores their default option values. */
  public enum InputType {
    // The regexp for the option strings is strict: no spaces, and trailing semicolon required.
    // @formatter: off
    TEXT(false, false, "WIDTH=16;SPAN=FALSE;"),
    LIST(
        true,
        false,
        "VALUE=NUMBER;TEXT=TRUE;ICON=FALSE;ICONSIZE=50;SELECT=0;SPAN=FALSE;DELIMITER=,;"),
    CHECK(false, false, "SPAN=FALSE;"),
    RADIO(true, false, "ORIENT=V;VALUE=NUMBER;SELECT=0;SPAN=FALSE;DELIMITER=,;"),
    LABEL(false, false, "TEXT=TRUE;ICON=FALSE;ICONSIZE=50;SPAN=FALSE;"),
    PROPS(false, true, "SETVARS=NONE;SPAN=FALSE;WIDTH=14;TYPE=STRPROP;"),
    TAB(false, true, "SELECT=FALSE;");
    // @formatter: on

    public final OptionMap defaultOptions; // maps option name to default value
    public final boolean isValueComposite; // can "value" section be a list of values?
    public final boolean isControlComposite; // does this control contain sub-controls?

    InputType(boolean isValueComposite, boolean isControlComposite, String nameval) {
      this.isValueComposite = isValueComposite;
      this.isControlComposite = isControlComposite;

      defaultOptions = new OptionMap();
      Pattern pattern =
          Pattern.compile("(\\w+)=([\\w-,]+);"); // no spaces allowed, semicolon required
      Matcher matcher = pattern.matcher(nameval);
      while (matcher.find()) {
        defaultOptions.put(
            matcher.group(1).toUpperCase(Locale.ROOT), matcher.group(2).toUpperCase(Locale.ROOT));
      }
    }

    /**
     * Obtain one of the enum values, or null if <code>strName</code> doesn't match any of them.
     *
     * @param strName the name of the enum values.
     * @return the {@link InputType} matching the passed in name, or {@code null} if there is no
     *     match..
     */
    public static InputType inputTypeFromName(String strName) {
      for (InputType it : InputType.values()) {
        if (strName.equalsIgnoreCase(it.name())) return it;
      }
      return null;
    }

    /**
     * Gets the default value for an option.
     *
     * @param option the name of the option to get the default value for.
     * @return the default value for the passed in option.
     */
    public String getDefault(String option) {
      return defaultOptions.get(option.toUpperCase(Locale.ROOT));
    }

    /**
     * Parses a string and returns a Map of options for the given type. Options not found are set to
     * the default value for the type.
     *
     * @param s the {@code String} to parse.
     * @return the options parsed from the string.
     * @throws OptionException if an error occurs.
     */
    public OptionMap parseOptionString(String s) throws OptionException {
      OptionMap ret = new OptionMap();
      ret.putAll(defaultOptions); // copy the default values first
      Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*=\\s*([^ ]+)\\s*");
      Matcher matcher = pattern.matcher(s);
      while (matcher.find()) {
        String key = matcher.group(1);
        String value = matcher.group(2);
        if (ret.get(key) == null) throw new OptionException(this, key, value);
        if (ret.getNumeric(key, -9998) != -9998) { // minor hack to detect if the option is numeric
          boolean valueIsNumeric;
          try {
            Integer.decode(value);
            valueIsNumeric = true;
          } catch (Exception e) {
            valueIsNumeric = false;
          }
          if (!valueIsNumeric) throw new OptionException(this, key, value);
        }
        ret.put(key, value);
      }
      return ret;
    }

    /** Stores option settings as case-insensitive strings. */
    @SuppressWarnings("serial")
    public final class OptionMap extends HashMap<String, String> {
      /** Case-insensitive put. */
      @Override
      public String put(String key, String value) {
        return super.put(key.toUpperCase(), value.toUpperCase());
      }

      /** Case-insensitive string get. */
      @Override
      public String get(Object key) {
        return super.get(key.toString().toUpperCase());
      }

      /**
       * Case-insensitive numeric get. <br>
       * Returns <code>defaultValue</code> if the option's value is non-numeric. <br>
       * Use when caller wants to override erroneous option settings.
       *
       * @param key the key of the property
       * @param defaultValue the default value, if none is available
       * @return the value or default value
       */
      public int getNumeric(String key, int defaultValue) {
        int ret;
        try {
          ret = Integer.decode(get(key));
        } catch (Exception e) {
          ret = defaultValue;
        }
        return ret;
      }

      /**
       * Case-insensitive numeric get. <br>
       * Returns the default value for the input type if option's value is non-numeric. <br>
       * Use when caller wants to ignore erroneous option settings.
       *
       * @param key the key
       * @return the value of the key
       */
      public int getNumeric(String key) {
        String defstr = getDefault(key);
        int def;
        try {
          def = Integer.decode(defstr);
        } catch (Exception e) {
          def = -1;
          // Should never happen, since the defaults are set in the source code.
        }
        return getNumeric(key, def);
      }

      /**
       * Tests for a given option value.
       *
       * @param key the key to test
       * @param value the value to be tested against
       * @return are the values equal
       */
      public boolean optionEquals(String key, String value) {
        if (get(key) == null) return false;
        return get(key).equalsIgnoreCase(value);
      }
    } ////////////////////////// end of OptionMap class

    /** Thrown when an option value is invalid. */
    @SuppressWarnings("serial")
    public static class OptionException extends Exception {
      public String key, value, type;

      public OptionException(InputType it, String key, String value) {
        super();
        this.key = key;
        this.value = value;
        this.type = it.name();
      }
    }
  } ///////////////////// end of InputType enum

  /** Variable Specifier structure - holds extracted bits of info for a variable. */
  static final class VarSpec {
    public String name, value, prompt;
    public InputType inputType;
    public InputType.OptionMap optionValues;
    public List<String> valueList; // used for types with composite "value" properties

    public VarSpec(String name, String value, String prompt, InputType inputType, String options)
        throws InputType.OptionException {
      initialize(name, value, prompt, inputType, options);
    }

    /** Create a VarSpec from a non-empty specifier string. */
    public VarSpec(String specifier) throws SpecifierException, InputType.OptionException {
      String[] parts = (specifier).split("\\|");
      int numparts = parts.length;

      String name, value, prompt;
      InputType inputType;

      name = (numparts > 0) ? parts[0].trim() : "";
      if (StringUtils.isEmpty(name))
        throw new SpecifierException(
            I18N.getText("macro.function.input.invalidSpecifier", specifier));

      value = (numparts > 1) ? parts[1].trim() : "";
      if (StringUtils.isEmpty(value)) value = "0"; // Avoids having a default value of ""

      prompt = (numparts > 2) ? parts[2].trim() : "";
      if (StringUtils.isEmpty(prompt)) prompt = name;

      String inputTypeStr = (numparts > 3) ? parts[3].trim() : "";
      inputType = InputType.inputTypeFromName(inputTypeStr);
      if (inputType == null) {
        if (StringUtils.isEmpty(inputTypeStr)) {
          inputType = InputType.TEXT; // default
        } else {
          throw new SpecifierException(
              I18N.getText("macro.function.input.invalidType", inputTypeStr, specifier));
        }
      }

      String options = (numparts > 4) ? parts[4].trim() : "";

      initialize(name, value, prompt, inputType, options);
    }

    public void initialize(
        String name, String value, String prompt, InputType inputType, String options)
        throws InputType.OptionException {
      this.name = name;
      this.value = value;
      this.prompt = prompt;
      this.inputType = inputType;
      if (inputType != null) this.optionValues = inputType.parseOptionString(options);

      if (inputType != null && inputType.isValueComposite)
        this.valueList = parseStringList(this.value, this.optionValues.get("DELIMITER"));
    }

    /**
     * Parses a string into a list of values, for composite types. <br>
     * Before calling, the <code>inputType</code> and <code>value</code> must be set. <br>
     * After calling, the <code>listIndex</code> member is adjusted if necessary.
     *
     * @param valueString the string list
     * @param delim the delimiter
     */
    public List<String> parseStringList(String valueString, String delim) {
      List<String> ret = new ArrayList<String>();
      if (valueString != null) {
        if ("json".equalsIgnoreCase(delim)) {
          JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(valueString);
          if (json != null && json.isJsonArray()) {
            for (JsonElement ele : json.getAsJsonArray()) {
              ret.add(ele.getAsString().trim());
            }
          }
        } else {
          String[] values = StringUtil.split(valueString, delim);
          for (String s : values) {
            ret.add(s.trim());
          }
        }
      }
      return ret;
    }

    /** Thrown when a variable specifier string is invalid. */
    @SuppressWarnings("serial")
    public class SpecifierException extends Exception {
      public String msg;

      public SpecifierException(String msg) {
        super();
        this.msg = msg;
      }
    }
  } ///////////////////// end of VarSpec class

  /** Contains input controls, which are arranged in a two-column label + control layout. */
  @SuppressWarnings("serial")
  final class ColumnPanel extends JPanel {
    public VarSpec tabVarSpec; // VarSpec for this subpanel's tab, if any
    public List<VarSpec> varSpecs;
    public List<JComponent> labels; // the labels in the left column
    public List<JComponent>
        inputFields; // the input controls (some may be panels for composite inputs)
    public JComponent lastFocus; // last input field with the focus
    public JComponent onShowFocus; // field to gain focus when shown
    private Insets textInsets = new Insets(0, 2, 0, 2); // used by all text controls
    private GridBagConstraints gbc = new GridBagConstraints();
    private int componentCount;

    public int maxHeightModifier = 0;

    public ColumnPanel() {
      tabVarSpec = null;
      varSpecs = new ArrayList<VarSpec>();
      labels = new ArrayList<JComponent>();
      inputFields = new ArrayList<JComponent>();
      lastFocus = null;
      onShowFocus = null;
      textInsets = new Insets(0, 2, 0, 2); // used by all TEXT controls

      setLayout(new GridBagLayout());
      gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.insets = new Insets(2, 2, 2, 2);

      componentCount = 0;
    }

    public ColumnPanel(List<VarSpec> lvs) {
      this(); // Initialize various member variables
      varSpecs = lvs;

      for (VarSpec vs : varSpecs) {
        addVariable(vs, false);
      }
    }

    /** Adds a row to the ColumnPanel with a label and input field. */
    public void addVariable(VarSpec vs) {
      addVariable(vs, true);
    }

    /**
     * Adds a row to the ColumnPanel with a label and input field. <code>addToVarList</code>
     * controls whether the VarSpec is added to the local listing.
     */
    protected void addVariable(VarSpec vs, boolean addToVarList) {
      if (addToVarList) varSpecs.add(vs);

      gbc.gridy = componentCount;
      gbc.gridwidth = 1;

      // add the label
      gbc.gridx = 0;

      JComponent l;
      Matcher m = Pattern.compile("^\\s*<html>(.*)<\\/html>\\s*$").matcher(vs.prompt);

      if (m.find()) {
        // For HTML values we use a HTMLPane.
        HTMLPane htmlp = new HTMLPane();
        htmlp.setText("<html>" + m.group(1) + ":</html>");
        l = htmlp;
      } else {
        l = new JLabel(vs.prompt + ":");
      }
      labels.add(l);

      if (!vs.optionValues.optionEquals("SPAN", "TRUE")) {
        // if the control is not set to span, we include the prompt label
        add(l, gbc);
      }

      // add the input component
      JComponent inputField = createInputControl(vs);

      if (vs.optionValues.optionEquals("SPAN", "TRUE")) {
        gbc.gridwidth = 2; // the control spans both columns
        inputField.setToolTipText(vs.prompt);
      } else {
        gbc.gridx = 1; // the control lives in the second column
      }

      inputFields.add(inputField);
      add(inputField, gbc);
      componentCount++;
    }

    /** Finds the first focusable control. */
    public JComponent findFirstFocusable() {
      for (JComponent c : inputFields) {
        if (c instanceof ColumnPanel) {
          ColumnPanel cp = (ColumnPanel) c;
          JComponent firstInPanel = cp.findFirstFocusable();
          if (firstInPanel != null) {
            return firstInPanel;
          }
        } else if (!(c instanceof JLabel)) {
          return c;
        }
      }
      return null;
    }

    /** Sets the focus to the control that last had it. */
    public void restoreFocus() {
      // // debugging
      // String s = (onShowFocus instanceof JTextField) ?
      // " (" + ((JTextField)onShowFocus).getText() + ")" : "";
      // String c = (onShowFocus == null) ? "null" : onShowFocus.getClass().getName();
      // System.out.println(" Shown: onShowFocus is " + c + s);

      if (onShowFocus != null) {
        onShowFocus.requestFocusInWindow();
      } else {
        JComponent first = findFirstFocusable();
        if (first != null) first.requestFocusInWindow();
      }
    }

    /** Adjusts the runtime behavior of controls. Called when displayed. */
    public void runtimeFixup() {
      // When first displayed, the focus will go to the first field.
      lastFocus = findFirstFocusable();

      // When a field gains the focus, save it in lastFocus.
      FocusListener listener =
          new FocusListener() {
            public void focusGained(FocusEvent fe) {
              JComponent src = (JComponent) fe.getSource();
              lastFocus = src;
              if (src instanceof JTextField) ((JTextField) src).selectAll();
              // // debugging
              // String s = (src instanceof JTextField) ?
              // " (" + ((JTextField)src).getText() + ")" : "";
              // System.out.println(" Got focus " + src.getClass().getName() + s);
            }

            public void focusLost(FocusEvent arg0) {}
          };

      for (JComponent c : inputFields) {
        // Each control saves itself to lastFocus when it gains focus.
        c.addFocusListener(listener);

        // Implement control-specific adjustments
        if (c instanceof JComboBox) {
          // HACK: to fix a Swing issue.
          // The stupid JComboBox has two subcomponents, BOTH of which accept the focus.
          // Thus it takes two Tab presses to move to the next control, and if you
          // tab once and then hit the down arrow, you can then tab away while the dropdown
          // list remains displayed. (Other comboboxes in MapTool have similar problems.)
          // Since the user is likely to tab between values when inputting, this is a
          // confusing nuisance.
          //
          // The hack used here is to make one of the two components (TinyComboBoxButton)
          // not focusable. We have to do it in a callback like this because the subcomponents
          // don't exist until the dialog is created (I think?). The code has a hardcoded index of
          // 0,
          // which is where the TinyComboBoxButton lives on my Windows box (discovered using the
          // debugger).
          // The code may fail on other OSs, or if a future version of Swing is used.
          // You're not supposed to mess with the internals like this.
          // But the resulting behavior is so much nicer with this fix in place, that I'm keeping it
          // in.
          Component list[] = c.getComponents();
          for (Component component : list)
            if (component instanceof TinyComboBoxButton) component.setFocusable(false); // HACK!
          // } else if (c instanceof JTextField) {
          // // Select all text when the text field gains focus
          // final JTextField textFieldFinal = (JTextField) c;
          // textFieldFinal.addFocusListener(new FocusListener() {
          // public void focusGained(FocusEvent fe) {
          // textFieldFinal.selectAll();
          // }
          //
          // public void focusLost(FocusEvent fe) {
          // }
          // });
        } else if (c instanceof ColumnPanel) {
          ColumnPanel cp = (ColumnPanel) c;
          cp.runtimeFixup();
        }
      }
      if (lastFocus != null) scrollRectToVisible(lastFocus.getBounds());
    }

    /** Creates the appropriate type of input control. */
    public JComponent createInputControl(VarSpec vs) {
      switch (vs.inputType) {
        case TEXT:
          return createTextControl(vs);
        case LIST:
          return createListControl(vs);
        case CHECK:
          return createCheckControl(vs);
        case RADIO:
          return createRadioControl(vs);
        case LABEL:
          return createLabelControl(vs);
        case PROPS:
          return createPropsControl(vs);
        case TAB:
          return null; // should never happen
        default:
          return null;
      }
    }

    /** Creates a text input control. */
    public JComponent createTextControl(VarSpec vs) {
      int width = vs.optionValues.getNumeric("Width");
      JTextField txt = new JTextField(vs.value, width);
      txt.setMargin(textInsets);
      return txt;
    }

    /** Creates a dropdown list control. */
    public JComponent createListControl(VarSpec vs) {
      JComboBox combo;
      boolean showText = vs.optionValues.optionEquals("TEXT", "TRUE");
      boolean showIcons = vs.optionValues.optionEquals("ICON", "TRUE");
      int iconSize = vs.optionValues.getNumeric("ICONSIZE", 0);
      if (iconSize <= 0) showIcons = false;

      // Build the combo box
      for (int j = 0; j < vs.valueList.size(); j++) {
        if (StringUtils.isEmpty(vs.valueList.get(j))) {
          // Using a non-empty string prevents the list entry from having zero height.
          vs.valueList.set(j, " ");
        }
      }
      if (!showIcons) {
        // Swing has an UNBELIEVABLY STUPID BUG when multiple items in a JComboBox compare as equal.
        // The combo box then stops supporting navigation with arrow keys, and
        // no matter which of the identical items is chosen, it returns the index
        // of the first one. Sun closed this bug as "by design" in 1998.
        // A workaround found on the web is to use this alternate string class (defined below)
        // which never reports two items as being equal.
        NoEqualString[] nesValues = new NoEqualString[vs.valueList.size()];
        for (int i = 0; i < nesValues.length; i++)
          nesValues[i] = new NoEqualString(vs.valueList.get(i));
        combo = new JComboBox(nesValues);
      } else {
        combo = new JComboBox();
        combo.setRenderer(new ComboBoxRenderer());
        Pattern pattern = ASSET_PATTERN;

        for (String value : vs.valueList) {
          Matcher matcher = pattern.matcher(value);
          String valueText, assetID;
          Icon icon = null;

          // See if the value string for this item has an image URL inside it
          if (matcher.find()) {
            valueText = matcher.group(1);
            assetID = matcher.group(2);
          } else {
            valueText = value;
            assetID = null;
          }

          // Assemble a JLabel and put it in the list
          UpdatingLabel label = new UpdatingLabel();
          icon = getIcon(assetID, iconSize, label);
          label.setOpaque(true); // needed to make selection highlighting show up
          if (showText) label.setText(valueText);
          if (icon != null) label.setIcon(icon);
          combo.addItem(label);
        }
      }
      int listIndex = vs.optionValues.getNumeric("SELECT");
      if (listIndex < 0 || listIndex >= vs.valueList.size()) listIndex = 0;
      combo.setSelectedIndex(listIndex);
      combo.setMaximumRowCount(20);
      return combo;
    }

    /** Creates a single checkbox control. */
    public JComponent createCheckControl(VarSpec vs) {
      JCheckBox check = new JCheckBox();
      check.setText("    "); // so a focus indicator will appear
      if (vs.value.compareTo("0") != 0) check.setSelected(true);
      return check;
    }

    /** Creates a group of radio buttons. */
    public JComponent createRadioControl(VarSpec vs) {
      int listIndex = vs.optionValues.getNumeric("SELECT");
      if (listIndex < 0 || listIndex >= vs.valueList.size()) listIndex = 0;
      ButtonGroup bg = new ButtonGroup();
      Box box =
          (vs.optionValues.optionEquals("ORIENT", "H"))
              ? Box.createHorizontalBox()
              : Box.createVerticalBox();

      // If the prompt is suppressed by SPAN=TRUE, use it as the border title
      String title = "";
      if (vs.optionValues.optionEquals("SPAN", "TRUE")) title = vs.prompt;
      box.setBorder(new TitledBorder(new EtchedBorder(), title));

      int radioCount = 0;
      for (String value : vs.valueList) {
        JRadioButton radio = new JRadioButton(value, false);
        bg.add(radio);
        box.add(radio);
        if (listIndex == radioCount) radio.setSelected(true);
        radioCount++;
      }
      return box;
    }

    /** Creates a label control, with optional icon. */
    public JComponent createLabelControl(VarSpec vs) {
      boolean hasText = vs.optionValues.optionEquals("TEXT", "TRUE");
      boolean hasIcon = vs.optionValues.optionEquals("ICON", "TRUE");

      // If the string starts with "<html>" then Swing will consider it HTML...
      if (hasText && vs.value.matches("^\\s*<html>")) {
        // For HTML values we use a HTMLPane.
        HTMLPane htmlp = new HTMLPane();
        htmlp.setText(vs.value);
        htmlp.setBackground(Color.decode("0xECE9D8"));
        return htmlp;
      }

      UpdatingLabel label = new UpdatingLabel();

      int iconSize = vs.optionValues.getNumeric("ICONSIZE", 0);
      if (iconSize <= 0) hasIcon = false;
      String valueText = "", assetID = "";
      Icon icon = null;

      // See if the string has an image URL inside it
      Matcher matcher = ASSET_PATTERN.matcher(vs.value);
      if (matcher.find()) {
        valueText = matcher.group(1);
        assetID = matcher.group(2);
      } else {
        hasIcon = false;
        valueText = vs.value;
      }

      // Try to get the icon
      if (hasIcon) {
        icon = getIcon(assetID, iconSize, label);
        if (icon == null) hasIcon = false;
      }

      // Assemble the label
      if (hasText) label.setText(valueText);
      if (hasIcon) label.setIcon(icon);

      return label;
    }

    /** Creates a subpanel with controls for each property. */
    public JComponent createPropsControl(VarSpec vs) {
      Map<String, String> map = new HashMap<String, String>();
      JsonElement jsonElement = JSONMacroFunctions.getInstance().asJsonElement(vs.value);
      List<String> oldKeys = new ArrayList<String>();

      if (jsonElement instanceof JsonObject) {
        // Get the key/value pairs from the JsonObject
        JsonObject jsonObject = (JsonObject) jsonElement;
        for (String key : jsonObject.keySet()) {
          map.put(key.toUpperCase(), jsonObject.get(key).getAsString());
          oldKeys.add(key);
        }
      } else {
        // Get the key/value pairs from the property string
        List<String> oldKeysNormalized = new ArrayList<String>();
        StrPropFunctions.parse(vs.value, map, oldKeys, oldKeysNormalized, ";");
      }

      // Create list of VarSpecs for the subpanel
      List<VarSpec> varSpecs = new ArrayList<VarSpec>();
      InputType it = InputType.TEXT;
      int width = vs.optionValues.getNumeric("WIDTH");
      String options = "WIDTH=" + width;
      for (String key : oldKeys) {
        String name = key;
        String value = map.get(key.toUpperCase());
        String prompt = key;
        VarSpec subvs;
        try {
          subvs = new VarSpec(name, value, prompt, it, options);
        } catch (OptionException e) {
          // Should never happen
          e.printStackTrace();
          subvs = null;
        }
        varSpecs.add(subvs);
      }

      // Create the subpanel
      ColumnPanel cp = new ColumnPanel(varSpecs);

      // If the prompt is suppressed by SPAN=TRUE, use it as the border title
      String title = "";
      if (vs.optionValues.optionEquals("SPAN", "TRUE")) title = vs.prompt;
      cp.setBorder(new TitledBorder(new EtchedBorder(), title));

      return cp;
    }
  } ///////////// end of ColumnPanel class

  /**
   * Wrapper panel provides alignment and scrolling to a ColumnPanel. ColumnPanel uses
   * GridBagLayout, which ignores size requests. Thus, the ColumnPanel is always the size of its
   * container, and is always centered. We wrap the ColumnPanel in a ScrollingPanel to set the
   * alignment and desired maximum size. The ScrollingPanel is wrapped in a JScrollPane to provide
   * scrolling support.
   */
  @SuppressWarnings("serial")
  final class ColumnPanelHost extends JScrollPane {
    ScrollingPanel panel;
    ColumnPanel columnPanel;

    ColumnPanelHost(ColumnPanel cp) {
      // get the width of the vertical scrollbar
      JScrollBar vscroll = getVerticalScrollBar();
      int scrollBarWidth = 20; // default value if we can't find the scrollbar
      if (vscroll != null) {
        Dimension d = vscroll.getMaximumSize();
        scrollBarWidth = (d == null) ? 20 : d.width;
      }

      // Cap the height of the contents at 3/4 of the screen height
      int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

      panel = new ScrollingPanel(cp, scrollBarWidth, screenHeight * 5 / 8);
      setViewportView(panel);
      columnPanel = cp;

      panel.setLayout(new FlowLayout(FlowLayout.LEFT));
      panel.add(columnPanel);

      // Restores focus to the appropriate input field in the nested ColumnPanel.
      ComponentAdapter onPanelShow =
          new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
              columnPanel.restoreFocus();
            }
          };

      setBorder(null);
      addComponentListener(onPanelShow);
    }

    final class ScrollingPanel extends JPanel implements Scrollable {
      ColumnPanel cp;
      int scrollBarWidth;
      int maxHeight;

      ScrollingPanel(ColumnPanel cp, int scrollBarWidth, int maxHeight) {
        this.cp = cp;
        this.scrollBarWidth = scrollBarWidth;
        this.maxHeight = maxHeight;
      }

      // The Scrollable interface methods
      public Dimension getPreferredScrollableViewportSize() {
        Dimension d = cp.getPreferredSize();
        if (d.height > maxHeight
            || (d.height > cp.getParent().getHeight() && cp.getParent().getHeight() > 0)) {
          d.height = maxHeight + cp.maxHeightModifier;
          d.width += scrollBarWidth; // make room for the vertical scrollbar
        }
        return d;
      }

      public int getScrollableBlockIncrement(Rectangle visRect, int orientation, int direction) {
        int retval = visRect.height - 10;
        if (retval < 0) retval = 10;
        return retval;
      }

      public int getScrollableUnitIncrement(Rectangle visRect, int orientation, int direction) {
        return scrollBarWidth;
      }

      public boolean getScrollableTracksViewportHeight() {
        return false;
      }

      public boolean getScrollableTracksViewportWidth() {
        return false;
      }
    }
  }

  @SuppressWarnings("serial")
  final class InputPanel extends JPanel {
    public List<ColumnPanel> columnPanels;
    public JTabbedPane tabPane = null;
    public int initialTab = 0; // Which one is first visible

    InputPanel(List<VarSpec> varSpecs) throws ParameterException {
      ColumnPanel curcp;
      columnPanels = new ArrayList<ColumnPanel>();

      // Only allow tabs if the first item is a TAB specifier
      boolean useTabs = (varSpecs.get(0).inputType == InputType.TAB);
      int nextTabIndex = 0;

      if (useTabs) {
        // The top-level control in the InputPanel is a JTabbedPane
        tabPane = new JTabbedPane();
        add(tabPane);
        curcp = null; // Will get initialized on first step of loop below
      } else {
        // The top-level control is just a single ColumnPanelHost
        curcp = new ColumnPanel();
        columnPanels.add(curcp);
        ColumnPanelHost cph = new ColumnPanelHost(curcp);
        add(cph);
      }

      for (VarSpec vs : varSpecs) {
        if (vs.inputType == InputType.TAB) {
          if (useTabs) {
            curcp = new ColumnPanel();
            curcp.tabVarSpec = vs;
            curcp.setBorder(new EmptyBorder(5, 5, 5, 5));
            columnPanels.add(curcp);
            ColumnPanelHost cph = new ColumnPanelHost(curcp);

            tabPane.addTab(vs.value, null, cph, vs.prompt);
            if (vs.optionValues.optionEquals("SELECT", "TRUE")) {
              initialTab = nextTabIndex;
            }
            nextTabIndex++;
          } else {
            throw new ParameterException(I18N.getText("macro.function.input.invalidTAB"));
          }
        } else {
          // Not a TAB variable, so just add to the current ColumnPanel
          curcp.addVariable(vs);
        }
      }
    }

    /** Returns the first focusable control on the tab which is shown initially. */
    public JComponent findFirstFocusable() {
      ColumnPanel cp = columnPanels.get(initialTab);
      JComponent first = cp.findFirstFocusable();
      return first;
    }

    /** Adjusts the runtime behavior of components, and sets the initial focus. */
    public void runtimeFixup() {
      for (ColumnPanel cp : columnPanels) {
        cp.runtimeFixup();
      }

      // Select the initial tab, if any
      if (tabPane != null) {
        tabPane.setSelectedIndex(initialTab);
      }

      // Start the focus in the first input field, so the user can type immediately
      JComponent compFirst = findFirstFocusable();
      if (compFirst != null) compFirst.requestFocusInWindow();

      // When tab changes, save the last field that had the focus.
      // (The first field in the panel will gain focus before the panel is shown,
      // so we have to save cp.lastFocus before it's overwritten.)
      if (tabPane != null) {
        tabPane.addChangeListener(
            e -> {
              int newTabIndex = tabPane.getSelectedIndex();
              ColumnPanel cp = columnPanels.get(newTabIndex);
              cp.onShowFocus = cp.lastFocus;

              // // debugging
              // JComponent foc = cp.onShowFocus;
              // String s = (foc instanceof JTextField) ?
              // " (" + ((JTextField)foc).getText() + ")" : "";
              // String c = (foc!=null) ? foc.getClass().getName() : "";
              // System.out.println("tabpane foc = " + c + s);
            });
      }
    }

    public void modifyMaxHeightBy(int mod) {
      for (ColumnPanel cpanel : columnPanels) {
        cpanel.maxHeightModifier = mod;
      }
    }
  }

  // The function that does all the work
  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    // Extract the list of specifier strings from the parameters
    // "name | value | prompt | inputType | options"
    List<String> varStrings = new ArrayList<String>();
    for (Object param : parameters) {
      String paramStr = (String) param;
      if (StringUtils.isEmpty(paramStr)) {
        continue;
      }
      // Multiple vars can be packed into a string, separated by "##"
      List<String> substrings = StrListFunctions.toList(paramStr, "##");
      for (String varString : substrings) {
        if (StringUtils.isEmpty(varString)) {
          continue;
        }
        varStrings.add(varString);
      }
    }

    // Create VarSpec objects from each variable's specifier string
    List<VarSpec> varSpecs = new ArrayList<VarSpec>();
    for (String specifier : varStrings) {
      VarSpec vs;
      try {
        vs = new VarSpec(specifier);
      } catch (VarSpec.SpecifierException se) {
        throw new ParameterException(se.msg);
      } catch (InputType.OptionException oe) {
        throw new ParameterException(
            I18N.getText(
                "macro.function.input.invalidOptionType", oe.key, oe.value, oe.type, specifier));
      }
      varSpecs.add(vs);
    }

    // Check if any variables were defined
    if (varSpecs.isEmpty())
      return BigDecimal.ONE; // No work to do, so treat it as a successful invocation.

    // UI step 1 - First, see if a token is in context.
    VariableResolver varRes = resolver;
    Token tokenInContext = null;
    if (varRes instanceof MapToolVariableResolver) {
      tokenInContext = ((MapToolVariableResolver) varRes).getTokenInContext();
    }
    String dialogTitle = "Input Values";
    if (tokenInContext != null) {
      String name = tokenInContext.getName(), gm_name = tokenInContext.getGMName();
      boolean isGM = MapTool.getPlayer().isGM();
      String extra = "";

      if (isGM && gm_name != null && gm_name.compareTo("") != 0) extra = " for " + gm_name;
      else if (name != null && name.compareTo("") != 0) extra = " for " + name;

      dialogTitle = dialogTitle + extra;
    }

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
    JOptionPane jop = new JOptionPane(ip, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    fixLayoutForTabPanes(jop);

    JDialog dlg = jop.createDialog(MapTool.getFrame(), dialogTitle);

    // Set up callbacks needed for desired runtime behavior
    dlg.addComponentListener(new FixupComponentAdapter(ip));

    dlg.setVisible(true);
    int dlgResult = JOptionPane.CLOSED_OPTION;
    try {
      dlgResult = (Integer) jop.getValue();
    } catch (NullPointerException npe) {
    }
    dlg.dispose();

    if (dlgResult == JOptionPane.CANCEL_OPTION || dlgResult == JOptionPane.CLOSED_OPTION)
      return BigDecimal.ZERO;

    // Finally, assign values from the dialog box to the variables
    for (ColumnPanel cp : ip.columnPanels) {
      List<VarSpec> panelVars = cp.varSpecs;
      List<JComponent> panelControls = cp.inputFields;
      int numPanelVars = panelVars.size();
      StringBuilder allAssignments = new StringBuilder(); // holds all values assigned in this tab

      for (int varCount = 0; varCount < numPanelVars; varCount++) {
        VarSpec vs = panelVars.get(varCount);
        JComponent comp = panelControls.get(varCount);
        String newValue = null;
        JsonObject jsonObject = null;
        switch (vs.inputType) {
          case TEXT:
            {
              newValue = ((JTextField) comp).getText();
              break;
            }
          case LIST:
            {
              Integer index = ((JComboBox) comp).getSelectedIndex();
              if (vs.optionValues.optionEquals("VALUE", "STRING")) {
                newValue = vs.valueList.get(index);
              } else { // default is "NUMBER"
                newValue = index.toString();
              }
              break;
            }
          case CHECK:
            {
              int value = ((JCheckBox) comp).isSelected() ? 1 : 0;
              newValue = Integer.toString(value);
              break;
            }
          case RADIO:
            {
              // This code assumes that the Box container returns components
              // in the same order that they were added.
              Component[] comps = comp.getComponents();
              int componentCount = 0;
              int index = 0;
              for (Component c : comps) {
                if (c instanceof JRadioButton) {
                  JRadioButton radio = (JRadioButton) c;
                  if (radio.isSelected()) index = componentCount;
                }
                componentCount++;
              }
              if (vs.optionValues.optionEquals("VALUE", "STRING")) {
                newValue = vs.valueList.get(index);
              } else { // default is "NUMBER"
                newValue = Integer.toString(index);
              }
              break;
            }
          case LABEL:
            {
              newValue = null;
              // The variable name is ignored and not set.
              break;
            }
          case PROPS:
            {
              // Read out and assign all the subvariables.
              // The overall return value is a property string (as in StrPropFunctions.java) with
              // all the new settings.
              Component[] comps = comp.getComponents();
              StringBuilder sb = new StringBuilder();
              jsonObject = new JsonObject();
              int setVars = 0; // "NONE", no assignments made
              if (vs.optionValues.optionEquals("SETVARS", "SUFFIXED")) setVars = 1;
              if (vs.optionValues.optionEquals("SETVARS", "UNSUFFIXED")) setVars = 2;
              if (vs.optionValues.optionEquals("SETVARS", "TRUE"))
                setVars = 2; // for backward compatibility
              for (int compCount = 0; compCount < comps.length; compCount += 2) {
                String key =
                    ((JLabel) comps[compCount]).getText().split("\\:")[0]; // strip trailing colon
                String value = ((JTextField) comps[compCount + 1]).getText();
                sb.append(key);
                sb.append("=");
                sb.append(value);
                sb.append(" ; ");
                if (vs.optionValues.optionEquals("TYPE", "JSON")) {
                  jsonObject.add(
                      key, JSONMacroFunctions.getInstance().convertPrimitiveFromString(value));
                }
                switch (setVars) {
                  case 0:
                    // Do nothing
                    break;
                  case 1:
                    resolver.setVariable(key + "_", value);
                    break;
                  case 2:
                    resolver.setVariable(key, value);
                    break;
                }
              }
              newValue = sb.toString();
              break;
            }
          default:
            // should never happen
            newValue = null;
            break;
        }
        // Set the variable to the value we got from the dialog box.
        if (newValue != null) {
          if (vs.optionValues.optionEquals("TYPE", "JSON")) {
            resolver.setVariable(vs.name, jsonObject);
          } else {
            resolver.setVariable(vs.name, newValue.trim());
          }
          allAssignments.append(vs.name).append("=").append(newValue.trim()).append(" ## ");
        }
      }
      if (cp.tabVarSpec != null) {
        resolver.setVariable(cp.tabVarSpec.name, allAssignments.toString());
      }
    }

    return BigDecimal.ONE; // success

    // for debugging:
    // return debugOutput(varSpecs);
  }

  /**
   * This is a workaround to fix the excessive space introduced when using multiple tabs (see issue
   * #198)
   */
  private void fixLayoutForTabPanes(JOptionPane jop) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;

    GridBagLayout gbl = new GridBagLayout();
    for (Component c : jop.getComponents()) {
      gbl.setConstraints(c, gbc);
      gbc.gridy += 1;
    }
    jop.setLayout(gbl);
  }

  @Override
  public void checkParameters(String functionName, List<Object> parameters)
      throws ParameterException {
    super.checkParameters(functionName, parameters);

    for (Object param : parameters) {
      if (!(param instanceof String))
        throw new ParameterException(
            I18N.getText(
                "macro.function.input.illegalArgumentType",
                param.getClass().getName(),
                String.class.getName()));
    }
  }

  /** Gets icon from the asset manager. Code copied and modified from EditTokenDialog.java */
  private ImageIcon getIcon(String id, int size, ImageObserver io) {
    // Extract the MD5Key from the URL
    if (id == null) {
      return null;
    }
    var assetMD5 = FunctionUtil.getAssetKeyFromString(id);

    // Get the base image && find the new size for the icon
    BufferedImage assetImage = ImageManager.getImage(assetMD5, io);

    // Resize
    if (assetImage.getWidth() > size || assetImage.getHeight() > size) {
      Dimension dim = new Dimension(assetImage.getWidth(), assetImage.getWidth());
      if (dim.height < dim.width) {
        dim.height = (int) ((dim.height / (double) dim.width) * size);
        dim.width = size;
      } else {
        dim.width = (int) ((dim.width / (double) dim.height) * size);
        dim.height = size;
      }
      BufferedImage image = new BufferedImage(dim.width, dim.height, Transparency.BITMASK);
      Graphics2D g = image.createGraphics();
      g.drawImage(assetImage, 0, 0, dim.width, dim.height, null);
      assetImage = image;
    }
    return new ImageIcon(assetImage);
  }

  /** JLabel variant that listens for new image data, and redraws its icon. */
  public static class UpdatingLabel extends JLabel {
    private String macroLink;

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
      Icon curIcon = getIcon();
      int curWidth = curIcon.getIconWidth();
      int curHeight = curIcon.getIconHeight();

      // Are we receiving the final image data?
      int flags = ImageObserver.ALLBITS | ImageObserver.FRAMEBITS;
      if ((infoflags & flags) == 0) return true;

      // Resize
      Dimension dim = new Dimension(curWidth, curHeight);
      BufferedImage sizedImage = new BufferedImage(dim.width, dim.height, Transparency.BITMASK);
      Graphics2D g = sizedImage.createGraphics();
      g.drawImage(img, 0, 0, dim.width, dim.height, null);

      // Update our Icon
      setIcon(new ImageIcon(sizedImage));
      return false;
    }

    public void setMacroLink(String link) {
      macroLink = link;
      this.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              // System.out.println(macroLink);
            }
          });
    }
  }

  /** Custom renderer to display icons and text inside a combo box */
  private static class ComboBoxRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = null;
      if (value instanceof JLabel) {
        label = (JLabel) value;
        if (isSelected) {
          label.setBackground(list.getSelectionBackground());
          label.setForeground(list.getSelectionForeground());
        } else {
          label.setBackground(list.getBackground());
          label.setForeground(list.getForeground());
        }
      }
      return label;
    }
  }

  /** Adjusts the runtime behavior of components */
  public class FixupComponentAdapter extends ComponentAdapter {
    final InputPanel ip;

    public FixupComponentAdapter(InputPanel ip) {
      super();
      this.ip = ip;
    }

    @Override
    public void componentShown(ComponentEvent ce) {
      ip.runtimeFixup();
    }
  }

  /** Class found on web to work around a STUPID SWING BUG with JComboBox */
  public static class NoEqualString {
    private final String text;

    public NoEqualString(String txt) {
      text = txt;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  // Dumps out the parsed input specifications for debugging purposes
  // private String debugOutput(ArrayList<VarSpec> varSpecs) {
  // StringBuilder builder = new StringBuilder();
  // builder.append("<br><table border='1' padding='2px 2px'>");
  // builder.append(
  // "<tr style='font-weight:bold'><td>Name</td><td>Value</td><td>Prompt</td><td>Input
  // Type</td><td>Options</td></tr>"
  // );
  // for (VarSpec vs : varSpecs) {
  // builder.append("<tr>");
  // builder.append("<td>");
  // builder.append(vs.name);
  // builder.append("</td><td>");
  // if (vs.inputType == InputType.LIST) {
  // builder.append("(( ");
  // for (String s : vs.valueList) {
  // builder.append(s);
  // builder.append(",");
  // }
  // builder.append(" ))");
  // } else {
  // builder.append(vs.value);
  // }
  // builder.append("</td><td>");
  // builder.append(vs.prompt);
  // builder.append("</td><td>");
  // builder.append(vs.inputType);
  // builder.append("</td><td>");
  // for (Map.Entry<String, String> entry : vs.optionValues.entrySet())
  // builder.append(entry.getKey() + "=" + entry.getValue() + "<br>");
  // builder.append("</td></tr>");
  // }
  // builder.append("</table>");
  // return builder.toString();
  // }
}

// A sample for the TAB control
/*
 *
 * [h: status = input( "tab0 | Abilities | Enter abilities here | TAB", "blah|Max value is 18|Note|LABEL ## abils|Str=8;Con=8;Dex=8;Int=8;Wis=8;Cha=8|Abilities|PROPS" , "txt0|text on tab 0|",
 * "tab1 | Options | Various options | TAB |select=true", "txt|default text ## ck|1|Toggle me|CHECK ## list|a,b,This is a very long item,d|Pick one|LIST ## foo|foo" )] [h: abort(status)] tab0 is set
 * to [tab0] <br>tab1 is set to [tab1] <br> <br>list is set to [list] <br>get list from the tab1 variable: [getStrProp(tab1,"list","","##")]
 */

// A tall tab control, to demonstrate scrolling
/*
 * [h: props = "a=3;b=bob;c=cow;d=40;e=55;f=33;g=big time;h=hello;i=interesting;j=jack;k=kow;l=leap;m=moon;" ] [h: status = input( "tab0 | Settings | Settings tooltip | TAB", "foo|||CHECK ## bar|bar",
 * "tab1 | Options | Options tooltip | TAB | select=true", "num|a,b,c,d|Pick one|list ## zot|zot ## zam|zam", "tab2 | Options2 | Options2 tooltip | TAB | ", "p | " + props +
 * " | Sample props | PROPS ## p2 | " + props + " | More props | PROPS ## p3 | " + props + " | Even more props | PROPS ## p4 | " + props + " | Still more props | PROPS", "num2|a,b,c,d|Pick one|list ",
 * "num3|after all it's only a listbox here now isn't it dear?,b,c,d|Pick one|list|span=true ## ee|ee ## ff|ff ## gg|gg ## hh|hh ## ii|ii ## jj|jj ## kk|kk" , "tab3 | Empty | nothin' | TAB" )] [h:
 * abort(status)] tab0 is [tab0]<br> foo is [foo]<br> tab1 is [tab1]<br> num is [num]<br> tab2 is [tab2]<br> tab3 is [tab3]<br>
 */

// Here's a sample input to exercise the options
/*
 *
 * Original props = [props = "Name=Longsword +1; Damage=1d8+1; Crit=1d6; Keyword=fire;"] [H: input( "foo", "YourName|George Washington|Your name|TEXT", "Weapon|Axe,Sword,Mace|Choose weapon|LIST",
 * "WarCry|Attack!,No surrender!,I give up!|Pick a war cry|LIST|VALUE=STRING select=1" , "CA || Combat advantage|     CHECK|", "props |"+props+"|Weapon properties|PROPS|setvars=true",
 * "UsePower |1|Use the power|CHECK", "Weight|light,medium,heavy||RADIO|ORIENT=H select=1", "Ambition|Survive today, Defeat my enemies, Rule the world, Become immortal||RADIO|VALUE=STRING" ,
 * "bar | a, b, c, d, e, f, g , h     ,i  j, k   |Radio button test   |  RADIO       | select=5 value = string ; oRiEnT   =h;;;;" )]<br> <i>New values of variables:</i> <br>foo is [foo] <br>YourName
 * is [YourName] <br>Weapon is [Weapon] <br>WarCry is [WarCry] <br>CA is [CA] <br>props is [props] <br>UsePower is [UsePower] <br>Weight is [Weight] <br>Ambition is [Ambition] <br> <br>Name is [Name],
 * Damage is [Damage], Crit is [Crit], Keyword is [Keyword]
 */

// Here's a longer version of that sample, but the 9/14/08 checked in version of MapTool gets
// a stack overflow when this is pasted into chat (due to its length?)
/*
 *
 * Original props = [props = "Name=Longsword +1; Damage=1d8+1; Crit=1d6; Keyword=fire;"] [h: setPropVars = 1] [H: input( "foo", "YourName|George Washington|Your name|TEXT",
 * "Weapon|Axe,Sword,Mace|Choose weapon|LIST", "WarCry|Attack!,No surrender!,I give up!|Pick a war cry|LIST|VALUE=STRING select=1" , "CA || Combat advantage|     CHECK|",
 * "props |"+props+"|Weapon properties|PROPS|setvars=true", "UsePower |1|Use the power|CHECK", "Weight|light,medium,heavy||RADIO|ORIENT=H select=1",
 * "Ambition|Survive today, Defeat my enemies, Rule the world, Become immortal||RADIO|VALUE=STRING" ,
 * "bar | a, b, c, d, e, f, g , h     ,i  j, k   |Radio button test   |  RADIO       | select=5 value = string ; oRiEnT   =h;;;;" )]<br> <i>New values of variables:</i> <table border=0><tr
 * style='font-weight:bold;'><td>Name&nbsp;&nbsp;&nbsp;</td><td>Value</td></tr> <tr><td>foo&nbsp;&nbsp;&nbsp;</td><td>{foo}</td></tr> <tr><td>YourName&nbsp;&nbsp;&nbsp;</td><td>{YourName}</td></tr>
 * <tr><td>Weapon&nbsp;&nbsp;&nbsp;</td><td>{Weapon}</td></tr> <tr><td>WarCry&nbsp;&nbsp;&nbsp;</td><td>{WarCry}</td></tr> <tr><td>CA&nbsp;&nbsp;&nbsp;</td><td>{CA}&nbsp;&nbsp;&nbsp;</td></tr>
 * <tr><td>props&nbsp;&nbsp;&nbsp;</td><td>{props}&nbsp;&nbsp;&nbsp;</td></tr> <tr ><td>UsePower&nbsp;&nbsp;&nbsp;</td><td>{UsePower}&nbsp;&nbsp;&nbsp;</td></ tr>
 * <tr><td>Weight&nbsp;&nbsp;&nbsp;</td><td>{Weight}&nbsp;&nbsp;&nbsp;</td></tr> < tr><td>Ambition&nbsp;&nbsp;&nbsp;</td><td>{Ambition}&nbsp;&nbsp;&nbsp;</td></ tr>
 * <tr><td>&nbsp;&nbsp;&nbsp;</td><td>&nbsp;&nbsp;&nbsp;</td></tr> <tr><td>&nbsp;&nbsp;&nbsp;</td><td>&nbsp;&nbsp;&nbsp;</td></tr> {if (setPropVars,
 * "<tr><td>Name&nbsp;&nbsp;&nbsp;</td><td>"+Name+"&nbsp;&nbsp;&nbsp;</td></tr> < tr><td>Damage&nbsp;&nbsp;&nbsp;</td><td>"+Damage+"&nbsp;&nbsp;&nbsp;</td></tr >
 * <tr><td>Crit&nbsp;&nbsp;&nbsp;</td><td>"+Crit+"&nbsp;&nbsp;&nbsp;</td></tr> < tr><td>Keyword&nbsp;&nbsp;&nbsp;</td><td>"+Keyword+"&nbsp;&nbsp;&nbsp;</td></ tr>", "")} </td></tr> </table> New props
 * = [props]
 */
