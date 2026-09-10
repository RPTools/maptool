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
package net.rptools.maptool.client.swing.table;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

/* cell renderer for properties table */
public class WordWrapCellRenderer extends RSyntaxTextArea implements TableCellRenderer {
  private static final Logger log = LogManager.getLogger();

  public WordWrapCellRenderer() {
    setLineWrap(false);
    setWrapStyleWord(true);

    /* Set the color style via Theme */
    try {
      File themeFile =
          new File(AppConstants.THEMES_DIR, AppPreferences.defaultMacroEditorTheme.get() + ".xml");
      Theme theme = Theme.load(new FileInputStream(themeFile));
      theme.apply(this);
      setFont(FlatUIUtils.nonUIResource(UIManager.getFont("monospaced.font")));
      revalidate();
    } catch (IOException e) {
      log.error("Error while loading theme", e);
    }
  }

  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value == null) {
      value = "";
    }
    setText(value.toString());
    setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
    if (table.getRowHeight(row) != getPreferredSize().height) {
      table.setRowHeight(row, getPreferredSize().height);
    }
    return this;
  }
}
