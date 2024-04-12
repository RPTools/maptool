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

import com.google.protobuf.StringValue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import javax.annotation.Nonnull;
import javax.swing.CellRendererPane;
import net.rptools.maptool.client.swing.TwoToneTextPane;
import net.rptools.maptool.client.tool.drawing.DrawnTextTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.DrawnLabelDto;

/**
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class DrawnLabel extends AbstractDrawing {

  /** The bounds of the display rectangle */
  private Rectangle bounds = new Rectangle();

  /** Text being painted. */
  private String text;

  /** The font used to paint the text. */
  private String font;

  /** The pane used to render the text */
  private transient CellRendererPane renderer;

  /** The text pane used to paint the text. */
  private transient TwoToneTextPane textPane;

  /**
   * Create a new drawn label.
   *
   * @param theText Text to be drawn
   * @param theBounds The bounds containing the text.
   * @param aFont The font used to draw the text as a string that can be passed to {@link
   *     Font#decode(java.lang.String)}.
   */
  public DrawnLabel(String theText, Rectangle theBounds, String aFont) {
    text = theText;
    bounds = theBounds;
    font = aFont;
  }

  public DrawnLabel(GUID id, String theText, Rectangle theBounds, String aFont) {
    super(id);
    text = theText;
    bounds = theBounds;
    font = aFont;
  }

  public String getText() {
    return text;
  }

  public String getFont() {
    return font;
  }

  public void draw(Zone zone, Graphics2D aG) {
    if (renderer == null) {
      renderer = new CellRendererPane();
      textPane = DrawnTextTool.createTextPane(bounds, null, font);
      textPane.setText(text);
    }
    renderer.paintComponent(aG, textPane, null, bounds);
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {}

  /**
   * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
   */
  public Rectangle getBounds() {
    return bounds;
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    return new Area();
  }

  @Override
  public DrawableDto toDto() {
    var dto = DrawnLabelDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setBounds(Mapper.map(getBounds()))
        .setText(getText())
        .setFont(getFont());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setDrawnLabel(dto).build();
  }
}
