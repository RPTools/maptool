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
package net.rptools.maptool.model.library.builtin.themecss;

import javax.swing.JLabel;
import javax.swing.UIDefaults;

public class ThemeHeader {

  public static class Header {
    private final String fontFamily;
    private final String fontSize;

    public Header(String fontFamily, String fontSize) {
      this.fontFamily = fontFamily;
      this.fontSize = fontSize;
    }

    public String getFontFamily() {
      return fontFamily;
    }

    public String getFontSize() {
      return fontSize;
    }
  }

  private final Header h1;
  private final Header h2;

  private final Header h3;

  private final Header h4;

  private final Header h5;

  private final Header h6;

  public ThemeHeader(UIDefaults uiDef) {
    JLabel h1Label = new JLabel();
    h1Label.putClientProperty("FlatLaf.styleClass", "h1");
    h1 = new Header(h1Label.getFont().getFamily(), h1Label.getFont().getSize() + "px");

    JLabel h2Label = new JLabel();
    h2Label.putClientProperty("FlatLaf.styleClass", "h2");
    h2 = new Header(h2Label.getFont().getFamily(), h2Label.getFont().getSize() + "px");

    JLabel h3Label = new JLabel();
    h3Label.putClientProperty("FlatLaf.styleClass", "h3");
    h3 = new Header(h3Label.getFont().getFamily(), h3Label.getFont().getSize() + "px");

    JLabel h4Label = new JLabel();
    h4Label.putClientProperty("FlatLaf.styleClass", "h4");
    h4 = new Header(h4Label.getFont().getFamily(), h4Label.getFont().getSize() + "px");

    JLabel h5Label = new JLabel();
    h5Label.putClientProperty("FlatLaf.styleClass", "h5");
    h5 = new Header(h5Label.getFont().getFamily(), h5Label.getFont().getSize() + "px");

    JLabel h6Label = new JLabel();
    h6Label.putClientProperty("FlatLaf.styleClass", "h6");
    h6 = new Header(h6Label.getFont().getFamily(), h6Label.getFont().getSize() + "px");
  }

  public Header getH1() {
    return h1;
  }

  public Header getH2() {
    return h2;
  }

  public Header getH3() {
    return h3;
  }

  public Header getH4() {
    return h4;
  }

  public Header getH5() {
    return h5;
  }

  public Header getH6() {
    return h6;
  }
}
