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
package net.rptools.maptool.client.swing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.htmlframe.HTMLPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Extends FormView to allow for macroLinkText form submissions. */
public class SubmitFormView extends FormView {
  private static final Logger log = LogManager.getLogger(SubmitFormView.class);
  private final HTMLPane htmlPane;

  /**
   * Creates a new SubmitFormView.
   *
   * @param elem The element this is a view for.
   * @param pane The HTMLPane this element resides on.
   */
  public SubmitFormView(Element elem, HTMLPane pane) {
    super(elem);
    htmlPane = pane;
  }

  /**
   * Creates a new SubmitFormView without a HTMLPane.
   *
   * @param elem The element this is a view for.
   */
  public SubmitFormView(Element elem) {
    super(elem);
    htmlPane = null;
  }

  public Logger getLog() {
    return log;
  }

  @Override
  protected void submitData(String data) {

    // Find the form
    Element formElement = null;
    for (Element e = getElement(); e != null; e = e.getParentElement()) {
      if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.FORM) {
        formElement = e;
        break;
      }
    }

    if (formElement != null) {
      AttributeSet att = formElement.getAttributes();
      String action = "";
      if (att.getAttribute(HTML.Attribute.ACTION) != null) {
        action = att.getAttribute(HTML.Attribute.ACTION).toString();
      }
      String method = "get";
      if (att.getAttribute(HTML.Attribute.METHOD) != null) {
        method = att.getAttribute(HTML.Attribute.METHOD).toString().toLowerCase();
      }
      if (method.equals("json")) {
        JsonObject jobj = new JsonObject();
        String[] values = data.split("&"); // Is this safe? What if the data contains an "&"?
        for (String v : values) {
          String[] dataStr = v.split("=");
          if (dataStr.length == 1) {
            jobj.addProperty(URLDecoder.decode(dataStr[0], StandardCharsets.UTF_8), "");
          } else {
            String decodedKey = URLDecoder.decode(dataStr[0], StandardCharsets.UTF_8);
            String decodedValue = URLDecoder.decode(dataStr[1], StandardCharsets.UTF_8);
            try {
              BigDecimal value = new BigDecimal(decodedValue);
              jobj.addProperty(decodedKey, value);
            } catch (NumberFormatException nfe) {
              JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(decodedValue);
              jobj.add(decodedKey, json);
            }
          }
        }
        data = URLEncoder.encode(jobj.toString(), StandardCharsets.UTF_8);
      }
      if (htmlPane != null) {
        // Triggers the submit from the htmlPane
        htmlPane.doSubmit(method, action, data);
      } else {
        // Triggers the macroLink directly
        MacroLinkFunction.runMacroLink(action + data);
      }
    }
  }

  @Override
  protected void imageSubmit(String data) {
    Element formElement = null;
    for (Element e = getElement(); e != null; e = e.getParentElement()) {
      if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.FORM) {
        formElement = e;
        break;
      }
    }

    if (formElement != null) {
      String imageMapName = data.replaceFirst("\\..*", "");

      Map<String, String> fdata = new HashMap<String, String>();
      fdata.putAll(getDataFrom(formElement, imageMapName));
      StringBuilder sb = new StringBuilder();
      for (String s : fdata.keySet()) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append(s).append("=").append(fdata.get(s));
      }
      sb.append("&").append(data);
      submitData(sb.toString());
    } else {
      submitData(data);
    }
  }

  private Map<String, String> getDataFrom(Element ele, String selectedImageMap) {
    Map<String, String> vals = new HashMap<String, String>();

    for (int i = 0; i < ele.getElementCount(); i++) {
      Element e = ele.getElement(i);
      AttributeSet as = e.getAttributes();

      if (as.getAttribute(StyleConstants.ModelAttribute) != null
          || as.getAttribute(HTML.Attribute.TYPE) != null) {
        String type = (String) as.getAttribute(HTML.Attribute.TYPE);
        String name = (String) as.getAttribute(HTML.Attribute.NAME);
        Object model = as.getAttribute(StyleConstants.ModelAttribute);

        if (type == null
            && model instanceof PlainDocument) { // Text area has no HTML.Attribute.TYPE
          PlainDocument pd = (PlainDocument) model;
          try {
            vals.put(name, encode(pd.getText(0, pd.getLength())));
          } catch (BadLocationException e1) {
            getLog().error(e1.getStackTrace());
          }
        } else if (type == null && model instanceof ComboBoxModel) {
          vals.put(name, ((ComboBoxModel) model).getSelectedItem().toString());
        } else if ("text".equals(type)) {
          PlainDocument pd = (PlainDocument) model;
          try {
            vals.put(name, encode(pd.getText(0, pd.getLength())));
          } catch (BadLocationException e1) {
            getLog().error(e1.getStackTrace());
          }
        } else if ("submit".equals(type)) {
          // Ignore
        } else if ("image".equals(type)) {
          if (name != null && name.equals(selectedImageMap)) {
            String val = (String) as.getAttribute(HTML.Attribute.VALUE);
            vals.put(name + ".value", encode(val == null ? "" : val));
          }
        } else if ("radio".equals(type)) {
          if (as.getAttribute(HTML.Attribute.CHECKED) != null) {
            vals.put(name, encode(encode((String) as.getAttribute(HTML.Attribute.VALUE))));
          }
        } else if ("checkbox".equals(type)) {
          if (as.getAttribute(HTML.Attribute.CHECKED) != null) {
            vals.put(name, encode(encode((String) as.getAttribute(HTML.Attribute.VALUE))));
          }
        } else if ("password".equals(type)) {
          PlainDocument pd = (PlainDocument) model;
          try {
            vals.put(name, encode(pd.getText(0, pd.getLength())));
          } catch (BadLocationException e1) {
            getLog().error(e1.getStackTrace());
          }
        } else if ("hidden".equals(type)) {
          vals.put(name, encode(encode((String) as.getAttribute(HTML.Attribute.VALUE))));
        }
      }
      vals.putAll(getDataFrom(e, selectedImageMap));
    }
    return vals;
  }

  private String encode(String str) {
    return URLEncoder.encode(str, StandardCharsets.UTF_8);
  }
}
