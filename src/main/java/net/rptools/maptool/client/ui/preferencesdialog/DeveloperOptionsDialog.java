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
package net.rptools.maptool.client.ui.preferencesdialog;

import java.awt.*;
import javax.swing.*;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.GenericDialogFactory;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.preferences.Preference;
import org.apache.commons.text.WordUtils;

public class DeveloperOptionsDialog {
  private static class DeveloperToggleModel extends DefaultButtonModel {
    private final Preference<Boolean> option;

    public DeveloperToggleModel(Preference<Boolean> option) {
      this.option = option;
    }

    @Override
    public boolean isSelected() {
      return option.get();
    }

    @Override
    public void setSelected(boolean b) {
      option.set(b);
      super.setSelected(b);
    }
  }

  GenericDialogFactory dialogFactory =
      GenericDialog.getFactory()
          .setDialogTitle("Preferences.tab.developer")
          .makeModal(true)
          .addButton(ButtonKind.CLOSE);

  public DeveloperOptionsDialog() {
    final var developerOptionToggles = new JPanel(new GridBagLayout());

    final var headingConstraints = new GridBagConstraints();
    headingConstraints.insets = new Insets(6, 6, 3, 6);
    headingConstraints.gridx = 0;
    headingConstraints.gridy = 0;
    headingConstraints.gridwidth = 2;
    headingConstraints.gridheight = 2;
    headingConstraints.anchor = GridBagConstraints.CENTER;
    headingConstraints.fill = GridBagConstraints.VERTICAL;

    JLabel heading =
        new JLabel(
            "<html>"
                + WordUtils.wrap(
                    I18N.getText("Preferences.tab.developer.warning"), 50, "<br/>", false)
                + "</html>",
            RessourceManager.getBigIcon(Icons.WARNING),
            SwingConstants.CENTER);
    heading.setIconTextGap(12);
    heading.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                UIManager.getColor("Component.warning.borderColor"), 4, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
    developerOptionToggles.add(heading, headingConstraints);

    final var labelConstraints = new GridBagConstraints();
    labelConstraints.insets = new Insets(6, 0, 6, 5);
    labelConstraints.gridx = 0;
    labelConstraints.gridy = 2;
    labelConstraints.weightx = 0.;
    labelConstraints.weighty = 1.;
    labelConstraints.fill = GridBagConstraints.HORIZONTAL;

    final var checkboxConstraints = new GridBagConstraints();
    checkboxConstraints.insets = new Insets(6, 5, 6, 0);
    checkboxConstraints.gridx = 1;
    checkboxConstraints.gridy = 2;
    checkboxConstraints.weightx = 0.;
    checkboxConstraints.weighty = 1.;
    checkboxConstraints.fill = GridBagConstraints.HORIZONTAL;

    for (final var option : DeveloperOptions.Toggle.getOptions()) {
      labelConstraints.gridy += 1;
      checkboxConstraints.gridy += 1;

      final var label = new JLabel(option.getLabel());
      label.setToolTipText(option.getTooltip());
      label.setHorizontalAlignment(SwingConstants.LEADING);
      label.setHorizontalTextPosition(SwingConstants.TRAILING);

      final var checkbox = new JCheckBox();
      checkbox.setModel(new DeveloperToggleModel(option));
      checkbox.addActionListener(e -> option.set(!checkbox.isSelected()));

      label.setLabelFor(checkbox);

      developerOptionToggles.add(label, labelConstraints);
      developerOptionToggles.add(checkbox, checkboxConstraints);
    }
    dialogFactory.setContent(developerOptionToggles);
  }

  public void closeDialog() {
    dialogFactory.closeDialog();
  }

  public void showDialog() {
    dialogFactory.display();
  }
}
