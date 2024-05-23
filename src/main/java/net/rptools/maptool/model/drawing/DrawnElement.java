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
package net.rptools.maptool.model.drawing;

import net.rptools.maptool.server.proto.drawing.DrawnElementDto;

/** */
public class DrawnElement {

  private Drawable drawable;
  private Pen pen;

  public DrawnElement(Drawable drawable, Pen pen) {
    this.drawable = drawable;
    this.pen = pen;
  }

  public DrawnElement(DrawnElement other) {
    this(other.drawable.copy(), new Pen(other.pen));
  }

  public Drawable getDrawable() {
    return drawable;
  }

  public Pen getPen() {
    return pen;
  }

  public void setPen(Pen pen) {
    this.pen = pen;
  }

  public static DrawnElement fromDto(DrawnElementDto dto) {
    return new DrawnElement(Drawable.fromDto(dto.getDrawable()), Pen.fromDto(dto.getPen()));
  }

  public DrawnElementDto toDto() {
    return DrawnElementDto.newBuilder()
        .setDrawable(getDrawable().toDto())
        .setPen(getPen().toDto())
        .build();
  }
}
