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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import javax.annotation.Nonnull;
import javax.swing.CellRendererPane;
import javax.swing.text.Style;
import net.rptools.maptool.client.swing.TwoToneTextPane;
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
  private final Rectangle bounds;

  /** Text being painted. */
  private final String text;

  /** The font used to paint the text. */
  private final String font;

  /** The pane used to render the text */
  private transient CellRendererPane renderer;

  /** The text pane used to paint the text. */
  private transient TwoToneTextPane textPane;

  public DrawnLabel(GUID id, String theText, Rectangle theBounds, String aFont) {
    super(id);
    text = theText;
    bounds = theBounds;
    font = aFont;
  }

  public DrawnLabel(DrawnLabel other) {
    super(other);
    this.bounds = new Rectangle(other.bounds);
    this.text = other.text;
    this.font = other.font;
  }

  @Override
  public Drawable copy() {
    return new DrawnLabel(this);
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
      textPane = createTextPane(bounds, font);
      textPane.setText(text);
    }
    renderer.paintComponent(aG, textPane, null, bounds);
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {}

  @Override
  public Rectangle getBounds(Zone zone) {
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
        .setBounds(Mapper.map(bounds))
        .setText(getText())
        .setFont(getFont());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setDrawnLabel(dto).build();
  }

  public static DrawnLabel fromDto(DrawnLabelDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new DrawnLabel(id, dto.getText(), Mapper.map(dto.getBounds()), dto.getFont());
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }

  /**
   * Create a text pane with the passed bounds, pen, and font data
   *
   * @param bounds Bounds of the new text pane
   * @param font Font used to pain the text
   * @return A text pane used to draw text
   */
  private static TwoToneTextPane createTextPane(Rectangle bounds, String font) {
    // Create a text component and place it on the renderer's component
    TwoToneTextPane textPane = new TwoToneTextPane();
    textPane.setBounds(bounds);
    textPane.setOpaque(false);
    textPane.setBackground(new Color(0, 0, 0, 0)); // Transparent

    // Create a style for the component
    Style style = textPane.addStyle("default", null);
    TwoToneTextPane.setFont(style, Font.decode(font));
    textPane.setLogicalStyle(style);
    return textPane;
  }
}
