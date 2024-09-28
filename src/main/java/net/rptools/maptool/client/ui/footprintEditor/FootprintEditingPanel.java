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
package net.rptools.maptool.client.ui.footprintEditor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.zones.GridChanged;
import net.rptools.maptool.util.FootPrintToolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FootprintEditingPanel extends JPanel {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private static final CellPoint ORIGIN = new CellPoint(0, 0);
  private static final BufferedImage ORIGIN_MARKER =
      RessourceManager.getImage(Images.ZONE_RENDERER_CELL_WAYPOINT);
  final HexGridHorizontal HV = new HexGridHorizontal();
  final HexGridVertical HH = new HexGridVertical();
  final IsometricGrid ISO_S = new IsometricGrid();
  final HexGridHorizontal ISO_H = new HexGridHorizontal();
  final SquareGrid SQ = new SquareGrid();
  final GridlessGrid GG = new GridlessGrid();
  private ScreenPoint pointUnderMouse = new ScreenPoint(0, 0);
  private final PlayerView playerView = new PlayerView(Player.Role.GM);
  private static volatile Set<CellPoint> cellSet = new HashSet<>();
  private static volatile Grid currentGrid;
  private BufferedImage cellHighlight;
  ZoneRenderer renderer;
  Zone zone;
  static volatile Scale zoneScale;
  static volatile float tokenScale = 1;
  TokenFootprint footprint = FootPrintToolbox.getGlobalDefaultFootprint();

  public FootprintEditingPanel() {
    log.debug("new FootprintEditingPanel");
    this.setFocusable(true);
    this.setEnabled(true);
    this.setBorder(BorderFactory.createLoweredBevelBorder());
    this.setPreferredSize(new Dimension(700, 500));
    zone = ZoneFactory.createZone();
    zone.setHasFog(false);
    zone.setLightingStyle(Zone.LightingStyle.ENVIRONMENTAL);
    zone.setVisionType(Zone.VisionType.DAY);
    zone.setVisible(true);
    zone.setGridColor(Color.WHITE.hashCode());
    renderer = new ZoneRenderer(zone);
    renderer.setSize(this.getSize());
    zoneScale = renderer.getZoneScale();
    zoneScale.setScale(0.7f);
    if (footprint != null) {
      cellSet = footprint.getOccupiedCells(ORIGIN);
    } else {
      cellSet.add(ORIGIN);
    }
    addListeners();
  }

  private void addListeners() {
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            modifyFootprintCells(renderer.getCellAt(pointUnderMouse));
          }
        });
    addMouseMotionListener(
        new MouseAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            pointUnderMouse = new ScreenPoint(e.getX(), e.getY());
          }
        });
  }

  public Set<CellPoint> getCellSet() {
    log.debug("getCellSet");
    return cellSet;
  }

  private void setTokenFootprint(TokenFootprint tokenFootprint) {
    this.footprint = tokenFootprint;
    this.setCellSet(tokenFootprint.getOccupiedCells(ORIGIN));
  }

  public void setTokenFootprint(
      String gridTypeName, TokenFootprint tokenFootprint, Set<CellPoint> cells) {
    log.debug("setTokenFootprint - " + gridTypeName + " : " + tokenFootprint + " : " + cells);
    if (gridTypeName != null) {
      setGrid(gridTypeName);
    }
    setTokenFootprint(tokenFootprint);
    setScale(tokenFootprint.getScale());
    this.footprint.addOffsetTranslator(FootPrintToolbox.createOffsetTranslator(gridTypeName));
    if (cells != null) {
      setCellSet(cells);
    }
    repaint();
  }

  private void setCellSet(Set<CellPoint> cells) {
    if (!cells.equals(cellSet)) {
      footprint =
          new TokenFootprint(footprint.getName(), FootPrintToolbox.cellSetToPointArray(cells));
      cellSet = cells;
      repaint();
    }
  }

  public void setScale(double scale) {
    tokenScale = (float) scale;
    repaint();
  }

  public void setGrid(String gridName) {
    log.debug("setGrid: " + gridName);
    Grid tmpGrid;
    switch (gridName) {
      case "Vertical Hex" -> tmpGrid = HV;
      case "Horizontal Hex" -> tmpGrid = HH;
      case "Isometric Hex" -> tmpGrid = ISO_H;
      case "Isometric" -> tmpGrid = ISO_S;
      case "None" -> tmpGrid = GG;
      case "Square" -> tmpGrid = SQ;
      default -> tmpGrid = null;
    }
    if (tmpGrid == null || tmpGrid == currentGrid) {
      return;
    }
    currentGrid = tmpGrid;
    cellHighlight = currentGrid.getCellHighlight();
    zone.setGrid(currentGrid);
    renderer.flush();
    renderer.setScale(0.7f);
    new MapToolEventBus().getMainEventBus().post(new GridChanged(zone));

    repaint();
    log.debug("grid changed");
  }

  void modifyFootprintCells(CellPoint cp) {
    if (cp.equals(ORIGIN)) {
      return;
    }
    if (cellSet.contains(cp)) {
      cellSet.remove(cp);
      log.debug("modifyFootprintCells: - remove " + cp);
    } else {
      log.debug("modifyFootprintCells: - add " + cp);
      cellSet.add(cp);
    }
    footprint =
        new TokenFootprint(footprint.getName(), FootPrintToolbox.cellSetToPointArray(cellSet));
    repaint();
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g = (Graphics2D) graphics;
    renderer.setSize(this.getSize());
    renderer.centerOn(FootPrintToolbox.zonePointFromCellCentre(currentGrid.getCellCenter(ORIGIN)));
    renderer.renderZone(g, playerView);
    if (currentGrid != null) {
      renderFootprint(g);
    }
    g.dispose();
  }

  public void zoomIn() {
    renderer.zoomIn(0, 0);
    repaint();
  }

  public void zoomOut() {
    renderer.zoomOut(0, 0);
    repaint();
  }

  public void zoomReset() {
    renderer.zoomReset(0, 0);
    repaint();
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if (!aFlag) {
      renderer.flush();
      MapTool.removeZone(zone);
    }
  }

  /**
   * Relevant bits stolen from ZoneRenderer.renderPath
   *
   * @param g graphics
   */
  public void renderFootprint(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Point2D cellCentre;
    ZonePoint zp;
    for (CellPoint p : cellSet) {
      zp = FootPrintToolbox.zonePointFromCellCentre(currentGrid.getCellCenter(p));
      renderer.highlightCell(g, zp, cellHighlight, tokenScale);
    }
    log.debug("renderFootprint - cellSet: " + cellSet.toString());
    cellCentre = currentGrid.getCellCenter(ORIGIN);
    zp = new ZonePoint((int) cellCentre.getX(), (int) cellCentre.getY());
    renderer.highlightCell(g, zp, ORIGIN_MARKER, tokenScale / 3f);
  }
}
