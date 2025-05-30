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
package net.rptools.maptool.client.ui.campaignproperties;

import java.awt.*;
import javax.swing.*;

public class CampaignPropertiesDialogView {

  private JPanel mainPanel;
  private JEditorPane sightPanel;
  private JEditorPane lightPanel;
  private JEditorPane auraPanel;
  private JEditorPane auraHelp;
  private JEditorPane lightHelp;
  private JEditorPane sightHelp;

  public JComponent getRootComponent() {
    return mainPanel;
  }

  public JEditorPane getSightPanel() {
    return sightPanel;
  }

  public JEditorPane getSightHelp() {
    return sightHelp;
  }

  public JEditorPane getLightPanel() {
    return lightPanel;
  }

  public JEditorPane getLightHelp() {
    return lightHelp;
  }

  public JEditorPane getAuraPanel() {
    return auraPanel;
  }

  public JEditorPane getAuraHelp() {
    return auraHelp;
  }
}
