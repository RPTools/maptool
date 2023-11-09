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
package net.rptools.maptool.client.ui.macrobuttons.buttongroups;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.TokenPopupMenu;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.client.ui.macrobuttons.panels.AbstractMacroPanel;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

public abstract class AbstractButtonGroup extends JPanel
    implements DropTargetListener, MouseListener {
  protected DropTarget dt;
  private GUID tokenId;
  private List<Token> tokenList;
  private List<MacroButtonProperties> propertiesList;
  private AbstractMacroPanel panel;
  private String panelClass = "";
  private String groupLabel = "";
  private String groupClass = "";
  private String macroGroup = "";
  private int spacerHeight = 0;
  private AreaGroup area;

  public void dragEnter(DropTargetDragEvent event) {
    // System.out.println("BG: drag enter");
  }

  public void dragOver(DropTargetDragEvent event) {
    // System.out.println("BG: drag over");
  }

  public void dropActionChanged(DropTargetDragEvent event) {
    // System.out.println("BG: drag action changed");
  }

  public void dragExit(DropTargetEvent event) {
    // System.out.println("BG: drag exit");
  }

  public void drop(DropTargetDropEvent event) {
    // System.out.println("BG: drop!");
  }

  public Token getToken() {
    if (tokenId == null) {
      return null;
    } else {
      return MapTool.getFrame().getCurrentZoneRenderer() != null
          ? MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId)
          : null;
    }
  }

  public GUID getTokenId() {
    return this.tokenId;
  }

  public void setTokenId(GUID tokenId) {
    this.tokenId = tokenId;
  }

  public void setTokenId(Token token) {
    if (token == null) {
      this.tokenId = null;
    } else {
      this.tokenId = token.getId();
    }
  }

  public List<Token> getTokenList() {
    return tokenList;
  }

  public void setTokenList(List<Token> tokenList) {
    this.tokenList = tokenList;
  }

  public String getGroupClass() {
    return groupClass;
  }

  public void setGroupClass(String groupClass) {
    this.groupClass = groupClass;
  }

  public String getGroupLabel() {
    return groupLabel;
  }

  public void setGroupLabel(String label) {
    this.groupLabel = label;
  }

  public AbstractMacroPanel getPanel() {
    return panel;
  }

  public void setPanel(AbstractMacroPanel panel) {
    this.panel = panel;
  }

  public String getPanelClass() {
    return panelClass;
  }

  public void setPanelClass(String panelClass) {
    this.panelClass = panelClass;
  }

  public List<MacroButtonProperties> getPropertiesList() {
    return propertiesList;
  }

  public void setPropertiesList(List<MacroButtonProperties> propertiesList) {
    this.propertiesList = propertiesList;
  }

  public String getMacroGroup() {
    return macroGroup;
  }

  public void setMacroGroup(String group) {
    this.macroGroup = group;
  }

  public void setSpacerHeight(int height) {
    this.spacerHeight = height;
  }

  public AreaGroup getArea() {
    return area;
  }

  public void setArea(AreaGroup newArea) {
    area = newArea;
  }

  protected String getTokenName(Token token) {
    // if a token has a GM name, put that to button title too
    if (token.getGMName() != null && token.getGMName().trim().length() > 0) {
      return token.getName() + " (" + token.getGMName() + ")";
    } else {
      return token.getName();
    }
  }

  // Override these mouse events in subclasses to specify component specific behavior.
  public void mouseClicked(MouseEvent event) {}

  public void mousePressed(MouseEvent event) {}

  public void mouseReleased(MouseEvent event) {
    Token token = getToken();
    if (SwingUtilities.isRightMouseButton(event)) {
      if (getPanelClass().equals("CampaignPanel") && !MapTool.getPlayer().isGM()) {
        return;
      }
      // open button group menu
      new ButtonGroupPopupMenu(getPanelClass(), area, getMacroGroup(), token)
          .show(this, event.getX(), event.getY());
    }
  }

  public void mouseEntered(MouseEvent event) {}

  public void mouseExited(MouseEvent event) {}

  protected ThumbnailedBorder createBorder(String label) {
    if (getToken() != null) {
      ImageIcon i = new ImageIcon(ImageManager.getImageAndWait(getToken().getImageAssetId()));
      Image icon = i.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT);
      return new ThumbnailedBorder(icon, label);
    } else {
      return new ThumbnailedBorder(null, label);
    }
  }

  protected class ThumbnailedBorder extends AbstractBorder {

    private Image image;
    private String label;
    private Rectangle imageBounds;

    // private final int X_OFFSET = 5;

    public ThumbnailedBorder(Image image, String label) {
      this.image = image;
      this.label = label;

      addMouseListener(new MouseHandler());
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      // ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
      // RenderingHints.VALUE_RENDER_QUALITY);

      // TODO: change magic numbers to final fields
      // match line color to default titledborder line color
      g.setColor(new Color(165, 163, 151));

      if (image == null && label == null) {
        g.drawRoundRect(2, 2, c.getWidth() - 3, c.getHeight() - 3, 6, 6);
      } else {
        g.drawRoundRect(2, 12, c.getWidth() - 5, c.getHeight() - 13, 6, 6);
        // clear the left and right handside of the image to show space between border line and
        // image
        g.setColor(c.getBackground());
        g.fillRect(8, 0, 24, 20);
        g.drawImage(image, 10, 2, null);

        int strx = image != null ? 30 : 5;

        // clear the left and right of the label
        FontMetrics metrics = g.getFontMetrics();
        int stringHeight = metrics.getHeight();
        int stringWidth = metrics.stringWidth(label);
        g.fillRect(strx, 0, stringWidth + 5, stringHeight);

        // set the area for mouse listener
        if (image != null) {
          imageBounds =
              new Rectangle(10, 2, image.getWidth(null) + stringWidth, image.getHeight(null));
          // display impersonated image if impersonated
          if (getToken() != null && getToken().isBeingImpersonated()) {
            var impersonatedImage =
                RessourceManager.getSmallIcon(Icons.WINDOW_IMPERSONATED_MACROS).getImage();
            g.drawImage(impersonatedImage, (int) imageBounds.getMaxX() + 5, 4, null);
          }
        }

        g.setColor(UIManager.getColor("TitledBorder.titleColor"));
        g.drawString(label, strx + 3, (20 - stringHeight) / 2 + stringHeight - 2);
      }
    }

    public Insets getBorderInsets(Component component) {
      return new Insets(5, 5, 5, 5);
    }

    public boolean isBorderOpaque() {
      return true;
    }

    private class MouseHandler extends MouseAdapter {
      public void mouseReleased(MouseEvent event) {
        Token token = getToken();
        if (imageBounds != null && imageBounds.contains(event.getPoint())) {
          if (SwingUtilities.isLeftMouseButton(event)
              && event.getClickCount() == 2
              && !SwingUtil.isShiftDown(event)) {
            // open edit token dialog
            MapTool.getFrame()
                .showTokenPropertiesDialog(token, MapTool.getFrame().getCurrentZoneRenderer());
          } else if (SwingUtilities.isRightMouseButton(event)) {
            // open token popup menu
            Set<GUID> GUIDSet = new HashSet<GUID>();
            GUIDSet.add(tokenId);
            ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
            new TokenPopupMenu(GUIDSet, event.getX(), event.getY(), renderer, token)
                .showPopup(AbstractButtonGroup.this);
          } else if (SwingUtilities.isLeftMouseButton(event) && SwingUtil.isShiftDown(event)) {
            // impersonate token toggle
            if (token.isBeingImpersonated()) {
              MapTool.getFrame().getCommandPanel().commitCommand("/im");
            } else {
              MapTool.getFrame().getCommandPanel().commitCommand("/im " + tokenId);
            }
          }
        }
      }
    }

    public MouseAdapter getMouseAdapter() {
      return new MouseHandler();
    }
  }

  public static void clearHotkeys(AbstractMacroPanel panel, String macroGroup) {
    for (int areaGroupCount = 0; areaGroupCount < panel.getComponentCount(); areaGroupCount++) {
      AreaGroup area = (AreaGroup) panel.getComponent(areaGroupCount);
      for (ButtonGroup group : area.getButtonGroups()) {
        if (macroGroup.equals(group.getMacroGroup())) {
          for (MacroButton nextButton : group.getButtons()) {
            nextButton.clearHotkey();
          }
        }
      }
    }
  }
}
