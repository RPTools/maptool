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
package net.rptools.maptool.client.ui.chat;

import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegularExpressionTranslationRule extends AbstractChatTranslationRule {
  private static final Logger log = LogManager.getLogger(RegularExpressionTranslationRule.class);
  private Pattern pattern;
  private final String replaceWith;

  public RegularExpressionTranslationRule(String pattern, String replaceWith) {
    try {
      this.pattern = Pattern.compile(pattern);
    } catch (Exception e) {
      log.error("Could not parse regex: " + pattern, e);
    }
    this.replaceWith = replaceWith;
  }

  public String translate(String incoming) {
    if (pattern == null) {
      return incoming;
    }
    return pattern.matcher(incoming).replaceAll(replaceWith);
  }
}
