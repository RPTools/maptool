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
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.event.ActionEvent;

/** Class holding nested classes for ActionEvent related to HTML. */
public class HTMLActionEvent {
  /** Action event for changing title of the container. */
  public static class ChangeTitleActionEvent extends ActionEvent {
    private final String newTitle;

    ChangeTitleActionEvent(Object source, String title) {
      super(source, 0, "changeTitle");
      newTitle = title;
    }

    /** @return the new title. */
    String getNewTitle() {
      return newTitle;
    }
  }

  public static class MetaTagActionEvent extends ActionEvent {
    private final String name;
    private final String content;

    MetaTagActionEvent(Object source, String name, String content) {
      super(source, 0, "metaTag");
      this.name = name;
      this.content = content;
    }

    /**
     * Gets the name of the meta tag.
     *
     * @return the name of the meta tag.
     */
    public String getName() {
      return name;
    }

    /**
     * Gets the content for the meta tag.
     *
     * @return the content of the meta tag.
     */
    public String getContent() {
      return content;
    }
  }

  /** Action event for registering a macro */
  public static class RegisterMacroActionEvent extends ActionEvent {
    private final String type;
    private final String macro;

    RegisterMacroActionEvent(Object source, String type, String macro) {
      super(source, 0, "registerMacro");
      this.type = type;
      this.macro = macro;
    }

    /**
     * Gets the type of macro to register.
     *
     * @return the type of macro.
     */
    public String getType() {
      return type;
    }

    /**
     * Gets the link to the macro.
     *
     * @return the link to the macro.
     */
    public String getMacro() {
      return macro;
    }
  }

  /** Class that listens for form events. */
  public static class FormActionEvent extends ActionEvent {
    private final String method;
    private final String action;
    private final String data;

    FormActionEvent(Object source, String method, String action, String data) {
      super(source, 0, "submit");

      this.method = method;
      this.action = action;
      if (method.equals("json")) {
        this.data = data;
      } else {
        this.data = data.replace("%0A", "%20"); // String properties can not handle \n in strings.
        // XXX Shouldn't we warn the MTscript programmer somehow?
      }
    }

    public String getMethod() {
      return method;
    }

    public String getAction() {
      return action;
    }

    public String getData() {
      return data;
    }
  }
}
