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
package net.rptools.maptool.client.tool;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.TokenUtil;

/** */
public class FacingTool extends DefaultTool {
  private static final long serialVersionUID = -2807604658989763950L;

  // TODO: This shouldn't be necessary, just get it from the renderer
  private Token tokenUnderMouse;
  private Set<GUID> selectedTokenSet;

  public FacingTool() {
    // Non tool-bar tool ... atm
  }

  public void init(Token keyToken, Set<GUID> selectedTokenSet) {
    tokenUnderMouse = keyToken;
    this.selectedTokenSet = selectedTokenSet;
  }

  @Override
  public String getTooltip() {
    return "tool.facing.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.facing.instructions";
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            if (MapTool.confirm(I18N.getText("msg.confirm.removeFacings"))) {
              for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
                Token token = renderer.getZone().getToken(tokenGUID);
                if (token == null) {
                  continue;
                }
                token.setFacing(null);
                renderer.flush(token);
              }
              // Go back to the pointer tool
              resetTool();
            }
          }
        });
  }

  ////
  // MOUSE
  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);

    if (tokenUnderMouse == null || renderer.getTokenBounds(tokenUnderMouse) == null) {
      return;
    }
    Rectangle bounds = renderer.getTokenBounds(tokenUnderMouse).getBounds();

    int x = bounds.x + bounds.width / 2;
    int y = bounds.y + bounds.height / 2;

    double angle = Math.atan2(y - e.getY(), e.getX() - x);

    int degrees = (int) Math.toDegrees(angle);

    if (!SwingUtil.isControlDown(e)) {
      int[] facingAngles = renderer.getZone().getGrid().getFacingAngles();
      degrees = facingAngles[TokenUtil.getIndexNearestTo(facingAngles, degrees)];
    }
    Area visibleArea = null;
    Set<GUID> remoteSelected = new HashSet<GUID>();

    String name = MapTool.getPlayer().getName();
    boolean isGM = MapTool.getPlayer().isGM();
    boolean ownerReveal; // if true, reveal FoW if current player owns the token.
    boolean hasOwnerReveal; // if true, reveal FoW if token has an owner.
    boolean noOwnerReveal; // if true, reveal FoW if token has no owners.
    if (MapTool.isPersonalServer()) {
      ownerReveal =
          hasOwnerReveal = noOwnerReveal = AppPreferences.getAutoRevealVisionOnGMMovement();
    } else {
      ownerReveal = MapTool.getServerPolicy().isAutoRevealOnMovement();
      hasOwnerReveal = isGM && MapTool.getServerPolicy().isAutoRevealOnMovement();
      noOwnerReveal = isGM && MapTool.getServerPolicy().getGmRevealsVisionForUnownedTokens();
    }
    for (GUID tokenGUID : selectedTokenSet) {
      Token token = renderer.getZone().getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      token.setFacing(degrees);

      // Old Logic
      // if (renderer.getZone().hasFog()
      //        && ((AppPreferences.getAutoRevealVisionOnGMMovement() &&
      // MapTool.getPlayer().isGM()))
      //    || MapTool.getServerPolicy().isAutoRevealOnMovement()) {
      //  visibleArea = renderer.getZoneView().getVisibleArea(token);
      //  remoteSelected.add(token.getId());
      //  renderer.getZone().exposeArea(visibleArea, token);
      // }
      boolean revealFog = false;
      if (renderer.getZone().hasFog()) {
        if (ownerReveal && token.isOwner(name)) revealFog = true;
        else if (hasOwnerReveal && token.hasOwners()) revealFog = true;
        else if (noOwnerReveal && !token.hasOwners()) revealFog = true;
      }

      if (revealFog) {
        visibleArea = renderer.getZoneView().getVisibleArea(token);
        remoteSelected.add(token.getId());
        renderer.getZone().exposeArea(visibleArea, token);
      }

      renderer.flushFog();
    }
    // XXX Instead of calling exposeFoW() when visibleArea is null, shouldn't we just skip it?
    MapTool.serverCommand()
        .exposeFoW(
            renderer.getZone().getId(),
            visibleArea == null ? new Area() : visibleArea,
            remoteSelected);
    renderer.repaint(); // TODO: shrink this
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // Commit
    for (GUID tokenGUID : selectedTokenSet) {
      Token token = renderer.getZone().getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      // Send the facing to other players
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setFacing, token.getFacing());
    }
    // Go back to the pointer tool
    resetTool();
  }

  @Override
  protected void resetTool() {
    if (tokenUnderMouse.isStamp()) {
      MapTool.getFrame().getToolbox().setSelectedTool(StampTool.class);
    } else {
      MapTool.getFrame().getToolbox().setSelectedTool(PointerTool.class);
    }
  }
}
