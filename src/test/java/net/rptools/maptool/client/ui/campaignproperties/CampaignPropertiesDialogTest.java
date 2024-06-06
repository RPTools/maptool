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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.language.I18N;
import org.junit.jupiter.api.Test;

public class CampaignPropertiesDialogTest {

  @Test
  public void importPredefinedButton() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(
        () -> {
          CampaignPropertiesDialog cpd = new CampaignPropertiesDialog(null);

          JButton button = cpd.getImportPredefinedButton();

          assertEquals(
              button.getText(), I18N.getText("CampaignPropertiesDialog.button.importPredefined"));
        });
  }

  @Test
  public void predefinedPropertiesComboBox_noFiles()
      throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(
        () -> {
          CampaignPropertiesDialog cpd =
              new CampaignPropertiesDialog(null) {
                @Override
                protected File[] getPredefinedPropertyFiles(File propertyDir) {
                  return null;
                }
              };

          JComboBox<String> comboBox = cpd.getPredefinedPropertiesComboBox();

          assertEquals(comboBox.getModel().getSize(), 0);
        });
  }

  @Test
  public void predefinedPropertiesComboBox_twoFiles()
      throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(
        () -> {
          String one = new String("a" + AppConstants.CAMPAIGN_PROPERTIES_FILE_EXTENSION);
          String two = new String("b" + AppConstants.CAMPAIGN_PROPERTIES_FILE_EXTENSION);

          CampaignPropertiesDialog cpd =
              new CampaignPropertiesDialog(null) {
                @Override
                protected File[] getPredefinedPropertyFiles(File propertyDir) {

                  return new File[] {new File(one), new File(two)};
                }
              };

          JComboBox<String> comboBox = cpd.getPredefinedPropertiesComboBox();

          assertEquals(comboBox.getModel().getSize(), 2);
          assertEquals(comboBox.getModel().getElementAt(0), "a");
          assertEquals(comboBox.getModel().getElementAt(1), "b");
        });
  }
}
