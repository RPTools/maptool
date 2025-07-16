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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Action;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarVertHexEuclideanWalker;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;

/*
 * @formatter:off
 * Vertical Hex grids produce columns of hexes
 * and have their points at the side
 *  \_/ \
 *  / \_/
 *  \_/ \
 *  / \_/
 *  \_/ \
 *
 * @formatter:on
 */
public class HexGridVertical extends HexGrid {
  private static final OffsetTranslator OFFSET_TRANSLATOR =
      (originPoint, offsetPoint) -> {
        if (Math.abs(originPoint.x) % 2 == 1 && Math.abs(offsetPoint.x) % 2 == 0) {
          offsetPoint.y++;
        }
      };
  private static final Map<Integer, Area> gridShapeCache = new ConcurrentHashMap<>();

  @Override
  protected List<TokenFootprint> createFootprints() {
    return List.of(
        new TokenFootprint(new GUID("C0A80F0E0CB9FB560100000040A8090C"), "1/6", false, .408),
        new TokenFootprint(new GUID("C0A80F0E0CB9FB560200000040A8090C"), "1/4", false, .500),
        new TokenFootprint(new GUID("C0A80F0E0CB9FB560300000040A8090C"), "1/3", false, .577),
        new TokenFootprint(new GUID("C0A80F0E0CB9FB560400000040A8090C"), "1/2", false, .707),
        new TokenFootprint(new GUID("C0A80F0E0CB9FB560500000040A8090C"), "2/3", false, .816),
        new TokenFootprint(
            new GUID("7F00010109655C380100000038000101"),
            "Medium",
            I18N.getString("TokenFootprint.name.medium"),
            true,
            1.0),
        new TokenFootprint(
            new GUID("7F00010109655C380200000038000101"),
            "Large",
            I18N.getString("TokenFootprint.name.large"),
            new Point(0, 1),
            new Point(1, 0)),
        new TokenFootprint(
            new GUID("7F00010109655C380300000038000101"),
            "Huge",
            I18N.getString("TokenFootprint.name.huge"),
            new Point(-1, -1),
            new Point(-1, 0),
            new Point(0, -1),
            new Point(0, 1),
            new Point(1, -1),
            new Point(1, 0)),
        new TokenFootprint(
            new GUID("C0A80F464BAAC1B10900000080800A42"),
            "Humongous",
            I18N.getString("TokenFootprint.name.humongous"),
            new Point(-2, -1),
            new Point(-2, 0),
            new Point(-2, 1),
            new Point(-1, -2),
            new Point(-1, -1),
            new Point(-1, 0),
            new Point(-1, 1),
            new Point(0, -2),
            new Point(0, -1),
            new Point(0, 1),
            new Point(0, 2),
            new Point(1, -2),
            new Point(1, -1),
            new Point(1, 0),
            new Point(1, 1),
            new Point(2, -1),
            new Point(2, 0),
            new Point(2, 1)));
  }

  @Override
  protected synchronized Map<Integer, Area> getGridShapeCache() {
    return gridShapeCache;
  }

  @Override
  public double cellDistance(CellPoint cellA, CellPoint cellB, WalkerMetric wmetric) {
    int x1 = cellA.x;
    int x2 = cellB.x;
    int y1 = cellA.y - (int) Math.floor(x1 / 2.0); // convert to 60-degree angle coordinates
    int y2 = cellB.y - (int) Math.floor(x2 / 2.0);

    int dx = x2 - x1;
    int dy = y2 - y1;

    if (Integer.signum(dx) == Integer.signum(dy)) {
      return Math.abs(dx + dy);
    } else {
      return Math.max(Math.abs(dx), Math.abs(dy));
    }
  }

  protected int snapFacingInternal(
      int facing, boolean faceEdges, boolean faceVertices, int addedSteps) {

    if (!faceEdges && !faceVertices) {
      // Facing not support. Return a default answer.
      return 90;
    }

    // Work in range (0, 360], it's easier. Will convert back to (-180,180] at the end.
    facing = Math.floorMod(facing - 1, 360) + 1;

    /* The number of degrees between each standard facing. */
    int step = (faceEdges && faceVertices) ? 30 : 60;
    /* The position of the first standard facing CCW from zero. */
    int base = (!faceEdges && faceVertices) ? 0 : 30;
    /* A modification applied to facing to get the nearest answer, not a modulo/int div answer. */
    int diff = (step - 1) / 2;

    int stepsFromBase = Math.floorDiv(facing + diff - base, step) + addedSteps;
    return stepsFromBase * step + base;
  }

  /*
   * For a horizontal hex grid we want the following layout:
   * @formatter:off
   *
   *		7	8	9
   *	-		5		-
   *		1	2	3
   *
   * @formatter:on
   * (non-Javadoc)
   * @see net.rptools.maptool.model.Grid#installMovementKeys(net.rptools.maptool.client.tool.PointerTool, java.util.Map)
   */
  @Override
  public void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap) {
    if (movementKeys == null) {
      movementKeys = new HashMap<KeyStroke, Action>(16); // parameter is 9/0.75 (load factor)
      Rectangle r = getCellShape().getBounds();
      double w = r.width * 0.707;
      double h = r.height * 0.707;
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), new MovementKey(callback, -w, -h));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new MovementKey(callback, 0, -h));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), new MovementKey(callback, w, -h));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), new MovementKey(callback, -w, h));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), new MovementKey(callback, 0, h));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), new MovementKey(callback, w, h));
      movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new MovementKey(callback, 0, -h));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new MovementKey(callback, 0, h));
    }
    actionMap.putAll(movementKeys);
  }

  @Override
  public void uninstallMovementKeys(Map<KeyStroke, Action> actionMap) {
    if (movementKeys != null) {
      for (KeyStroke key : movementKeys.keySet()) {
        actionMap.remove(key);
      }
    }
  }

  @Override
  public BufferedImage getCellHighlight() {
    return pathHighlight;
  }

  @Override
  public double getCellHeight() {
    return getVRadius() * 2;
  }

  @Override
  public double getCellWidth() {
    return getURadius() * 2;
  }

  @Override
  public Dimension getCellOffset() {
    return new Dimension((int) getCellOffsetU(), (int) getCellOffsetV());
  }

  @Override
  public ZoneWalker createZoneWalker() {
    return new AStarVertHexEuclideanWalker(getZone());
  }

  @Override
  protected void setGridDrawTranslation(Graphics2D g, double U, double V) {
    g.translate(U, V);
  }

  @Override
  public double getRendererSizeV(ZoneRenderer renderer) {
    return renderer.getSize().getHeight();
  }

  @Override
  public double getRendererSizeU(ZoneRenderer renderer) {
    return renderer.getSize().getWidth();
  }

  @Override
  public int getOffV(ZoneRenderer renderer) {
    return (int) (renderer.getViewOffsetY() + getOffsetY() * renderer.getScale());
  }

  @Override
  public int getOffU(ZoneRenderer renderer) {
    return (int) (renderer.getViewOffsetX() + getOffsetX() * renderer.getScale());
  }

  @Override
  public CellPoint convert(ZonePoint zp) {
    return convertZP(zp.x, zp.y);
  }

  @Override
  protected int getOffsetU() {
    return getOffsetX();
  }

  @Override
  protected int getOffsetV() {
    return getOffsetY();
  }

  @Override
  public ZonePoint convert(CellPoint cp) {
    return convertCP(cp.x, cp.y);
  }

  @Override
  protected OffsetTranslator getOffsetTranslator() {
    return OFFSET_TRANSLATOR;
  }

  /** Returns the cell centre as well as nearest vertex */
  @Override
  public ZonePoint getNearestVertex(ZonePoint point) {
    double heightHalf = getURadius() / 2;
    //
    double isoY =
        ((point.y - getOffsetY()) / getVRadius() + (point.x - getOffsetX()) / heightHalf) / 2;
    double isoX =
        ((point.x - getOffsetX()) / heightHalf - (point.y - getOffsetY()) / getVRadius()) / 2;
    int newX = (int) Math.floor(isoX);
    int newY = (int) Math.floor(isoY);
    //
    double mapY = (newY - newX) * getVRadius();
    double mapX = ((newX + newY) * heightHalf) + heightHalf;
    return new ZonePoint((int) (mapX) + getOffsetX(), (int) (mapY) + getOffsetY());
  }

  @Override
  protected AffineTransform getGridOffset(Token token) {
    // Adjust to grid if token is an even number of grid cells
    double footprintHeight = token.getFootprint(this).getBounds(this).getHeight();

    final AffineTransform at = new AffineTransform();

    if ((footprintHeight / getSize()) % 2 == 0) {
      double coordinateOffsetV = getCellOffsetV();
      double coordinateOffsetU = -0.5 * (edgeProjection + edgeLength);
      at.translate(coordinateOffsetU, coordinateOffsetV);
    }

    return at;
  }
}
