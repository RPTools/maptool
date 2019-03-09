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

import junit.framework.TestCase;

public class RegularExpressionTranslationRuleTest extends TestCase {

  public void testIt() throws Exception {

    ChatTranslationRule rule = new RegularExpressionTranslationRule("one", "two");
    assertEquals("two two three", rule.translate("one two three"));

    rule = new RegularExpressionTranslationRule("(t.o)", "*$1*");
    assertEquals("one *two* three", rule.translate("one two three"));
  }
}
