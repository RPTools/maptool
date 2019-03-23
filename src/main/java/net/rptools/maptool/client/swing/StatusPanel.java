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
package net.rptools.maptool.client.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/** @author trevor */
public class StatusPanel extends JPanel {

  private JLabel statusLabel;

  public StatusPanel() {

    statusLabel = new JLabel();

    setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.BOTH;

    add(wrap(statusLabel), constraints);
  }

  public void setStatus(String status) {
    statusLabel.setText(status);
  }

  public void addPanel(JComponent component) {

    int nextPos = getComponentCount();

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.fill = GridBagConstraints.BOTH;

    constraints.gridx = nextPos;

    add(wrap(component), constraints);

    invalidate();
    doLayout();
  }

  private JComponent wrap(JComponent component) {

    component.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    return component;
  }
}
