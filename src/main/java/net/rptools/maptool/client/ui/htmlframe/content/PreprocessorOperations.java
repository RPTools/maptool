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
package net.rptools.maptool.client.ui.htmlframe.content;

import javax.annotation.Nonnull;
import org.jsoup.nodes.Document;

/**
 * This interface defines a contract for preprocessor operations that can be applied to HTML
 * documents.
 */
public interface PreprocessorOperations {

  /**
   * Applies the preprocessor operation to the given document.
   *
   * @param document The document to which the preprocessor operation will be applied.
   */
  void apply(@Nonnull Document document);

  /**
   * Returns priority of the preprocessor operation. The priority determines the order in which
   * operations are applied, lower values get applied first. If there is no specific priority,
   * return 0.
   */
  public int getPriority();
}
