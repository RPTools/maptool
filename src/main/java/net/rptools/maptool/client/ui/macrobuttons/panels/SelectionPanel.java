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
package net.rptools.maptool.client.ui.macrobuttons.panels;

import com.jidesoft.docking.DockableFrame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.MapToolFrame.MTFrame;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SelectionPanel extends AbstractMacroPanel {
  private static final Logger log = LogManager.getLogger(SelectionPanel.class);

  private final List<Token> tokenList = null;
  private List<MacroButtonProperties> commonMacros = new ArrayList<MacroButtonProperties>();
  private CodeTimer timer;

  public SelectionPanel() {
    // TODO: refactoring reminder
    setPanelClass("SelectionPanel");
    init(
        new ArrayList<
            Token>()); // when initially loading MT, the CurrentZoneRenderer isn't ready yet; just
    // send an empty list
  }

  public List<MacroButtonProperties> getCommonMacros() {
    return commonMacros;
  }

  public void setCommonMacros(List<MacroButtonProperties> newCommonMacros) {
    commonMacros = newCommonMacros;
  }

  public void init() {
    MapToolFrame f = MapTool.getFrame();
    ZoneRenderer zr = f.getCurrentZoneRenderer();
    if (zr != null) init(zr.getSelectedTokensList());
  }

  public void init(List<Token> selectedTokenList) {
    boolean panelVisible = true;

    if (MapTool.getFrame() != null) {
      DockableFrame selectionPanel = MapTool.getFrame().getDockingManager().getFrame("SELECTION");
      if (selectionPanel != null)
        panelVisible =
            (selectionPanel.isVisible() && !selectionPanel.isAutohide())
                    || selectionPanel.isAutohideShowing()
                ? true
                : false;
    }
    // Set up a code timer to get some performance data
    timer = new CodeTimer("selectionpanel");
    timer.setEnabled(AppState.isCollectProfilingData() || log.isDebugEnabled());
    timer.setThreshold(10);

    timer.start("painting");

    // paint panel only when it's visible or active
    if (panelVisible) {
      // add the selection panel controls first
      add(new MenuButtonsPanel());

      // draw common group only when there is more than one token selected
      if (selectedTokenList.size() > 1) {
        populateCommonButtons(selectedTokenList);
        if (!commonMacros.isEmpty()) {
          addArea(commonMacros, I18N.getText("component.areaGroup.macro.commonMacros"));
        }
        // add(new ButtonGroup(selectedTokenList, commonMacros, this));
      }
      for (Token token : selectedTokenList) {
        if (!AppUtil.playerOwns(token)) {
          continue;
        }
        addArea(token.getId());
      }
      if (selectedTokenList.size() == 1 && AppUtil.playerOwns(selectedTokenList.get(0))) {
        // if only one token selected, show its image as tab icon
        MapTool.getFrame()
            .getFrame(MTFrame.SELECTION)
            .setFrameIcon(selectedTokenList.get(0).getIcon(16, 16));
      }
    }
    timer.stop("painting");

    if (AppState.isCollectProfilingData() || log.isDebugEnabled()) {
      String results = timer.toString();
      MapTool.getProfilingNoteFrame().addText(results);
      if (log.isDebugEnabled()) log.debug(results);
    }
    MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
  }

  private void populateCommonButtons(List<Token> tokenList) {
    Map<Integer, MacroButtonProperties> uniqueMacros =
        new HashMap<Integer, MacroButtonProperties>();
    Map<Integer, MacroButtonProperties> commonMacros =
        new HashMap<Integer, MacroButtonProperties>();
    for (Token nextToken : tokenList) {
      if (!AppUtil.playerOwns(nextToken)) {
        continue;
      }
      for (MacroButtonProperties nextMacro : nextToken.getMacroList(true)) {
        MacroButtonProperties copiedMacro =
            new MacroButtonProperties(nextMacro.getIndex(), nextMacro);
        int macroKey = copiedMacro.hashCodeForComparison();
        Boolean macroIsInUnique = uniqueMacros.containsKey(copiedMacro.hashCodeForComparison());
        Boolean macroIsInCommon = commonMacros.containsKey(copiedMacro.hashCodeForComparison());
        if (!macroIsInUnique && !macroIsInCommon) {
          uniqueMacros.put(macroKey, copiedMacro);
        } else if (macroIsInUnique && !macroIsInCommon) {
          uniqueMacros.remove(macroKey);
          commonMacros.put(macroKey, copiedMacro);
        } else if (macroIsInUnique && macroIsInCommon) {
          uniqueMacros.remove(macroKey);
        }
      }
    }
    for (MacroButtonProperties nextMacro : commonMacros.values()) {
      nextMacro.setAllowPlayerEdits(true);
      for (Token nextToken : tokenList) {
        if (!AppUtil.playerOwns(nextToken)) {
          continue;
        }
        for (MacroButtonProperties nextTokenMacro : nextToken.getMacroList(true)) {
          if (!nextTokenMacro.getAllowPlayerEdits()) {
            nextMacro.setAllowPlayerEdits(false);
          }
        }
      }
      if (!nextMacro.getCompareApplyToSelectedTokens()) {
        nextMacro.setCompareApplyToSelectedTokens(false);
      }
      if (!nextMacro.getCompareAutoExecute()) {
        nextMacro.setCompareAutoExecute(false);
      }
      if (!nextMacro.getCompareCommand()) {
        nextMacro.setCommand("");
      }
      if (!nextMacro.getCompareGroup()) {
        nextMacro.setGroup("");
      }
      if (!nextMacro.getCompareIncludeLabel()) {
        nextMacro.setIncludeLabel(false);
      }
      if (!nextMacro.getCompareSortPrefix()) {
        nextMacro.setSortby("");
      }
    }
    this.commonMacros = new ArrayList<MacroButtonProperties>(commonMacros.values());
    int indexCount = 0;
    for (MacroButtonProperties nextMacro : this.commonMacros) {
      nextMacro.setIndex(indexCount);
      indexCount++;
    }
    Collections.sort(this.commonMacros);
  }

  @Override
  protected void clear() {
    // reset the tab icon
    MapTool.getFrame()
        .getFrame(MTFrame.SELECTION)
        .setFrameIcon(new ImageIcon(AppStyle.selectionPanelImage));
    super.clear();
  }

  @Override
  public void reset() {
    clear();
    init();
  }
}
