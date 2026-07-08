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
package net.rptools.maptool.client.ui.token;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.rptools.lib.AwtUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.FlowImageTokenOverlayDto;
import net.rptools.maptool.util.ImageManager;

/**
 * An overlay that allows multiple images to be placed on the token so that they do not interfere
 * with any tokens on the same grid.
 *
 * @author Jay
 */
public final class FlowImageTokenOverlay extends BooleanTokenOverlay {
  private static final int DEFAULT_GRID_SIZE = 3;

  /** ID of the image displayed in the overlay. */
  private MD5Key assetId;

  /** Size of the grid used to place a token with this state. */
  private int grid;

  /** Flow used to define position of states */
  private transient TokenOverlayFlow flow;

  /**
   * Create the image overlay flow for the name, asset and grid
   *
   * @param name Name of the new state
   * @param assetId Asset displayed for the state
   * @param gridSize Size of the overlay grid for this state. All states with the same grid size
   *     share the same overlay.
   */
  public FlowImageTokenOverlay(String name, MD5Key assetId, int gridSize) {
    super(name);

    this.assetId = assetId;

    if (gridSize <= 0) {
      gridSize = DEFAULT_GRID_SIZE;
    }
    this.grid = gridSize;
  }

  public FlowImageTokenOverlay(FlowImageTokenOverlay other) {
    super(other);
    this.assetId = other.assetId;
    this.grid = other.grid;
  }

  @Override
  public FlowImageTokenOverlay clone() {
    return new FlowImageTokenOverlay(this);
  }

  public MD5Key getAssetId() {
    return assetId;
  }

  /**
   * Get the flow used to position the states.
   *
   * @return Flow used to position the states
   */
  private TokenOverlayFlow getFlow() {
    if (flow == null) {
      flow = TokenOverlayFlow.getInstance(grid);
    }
    return flow;
  }

  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds) {
    BufferedImage image = ImageManager.getImageAndWait(assetId);

    var imageBounds = new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight());
    AwtUtil.fitInto(imageBounds, bounds);

    var gridCellBounds = getFlow().getStateBounds2D(imageBounds, token, getName());

    // Paint it at the right location
    int width = (int) gridCellBounds.getWidth();
    int height = (int) gridCellBounds.getHeight();
    int x = (int) gridCellBounds.getMinX();
    int y = (int) gridCellBounds.getMinY();

    g.drawImage(image, x, y, width, height, null);
  }

  /**
   * @return Getter for grid
   */
  public int getGrid() {
    return grid;
  }

  public FlowImageTokenOverlayDto toFlowImageDto() {
    var dto = FlowImageTokenOverlayDto.newBuilder();
    dto.setAssetId(assetId.toString());
    dto.setGridSize(grid);
    return dto.build();
  }

  public static FlowImageTokenOverlay fromDto(FlowImageTokenOverlayDto dto) {
    var assetId = new MD5Key(dto.getAssetId());
    var gridSize = dto.getGridSize();
    return new FlowImageTokenOverlay(DEFAULT_STATE_NAME, assetId, gridSize);
  }
}
