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
package net.rptools.maptool.model;

import java.awt.Color;

public class Label {
  private final GUID id;
  private String label;
  private int x, y;
  private boolean showBackground;
  private int foregroundColor;

  public Label() {
    this("");
  }

  public Label(String label) {
    this(label, 0, 0);
  }

  public Label(String label, int x, int y) {
    id = new GUID();
    this.label = label;
    this.x = x;
    this.y = y;
    showBackground = true;
  }

  public Label(Label label) {
    this(label.label, label.x, label.y);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public GUID getId() {
    return id;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean isShowBackground() {
    return showBackground;
  }

  public void setShowBackground(boolean showBackground) {
    this.showBackground = showBackground;
  }

  public Color getForegroundColor() {
    return new Color(foregroundColor);
  }

  public void setForegroundColor(Color foregroundColor) {
    this.foregroundColor = foregroundColor.getRGB();
  }
}
