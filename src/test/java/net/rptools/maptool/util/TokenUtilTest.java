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
package net.rptools.maptool.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.model.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenUtilTest {

  @Test
  @DisplayName("Test Guess Token Type")
  void testGuessTokenType() throws Exception {

    // SQUARE
    BufferedImage img = new BufferedImage(100, 100, Transparency.BITMASK);
    Graphics2D g = img.createGraphics();
    g.setColor(Color.blue);
    g.fillRect(0, 0, 100, 100);
    g.dispose();

    assertEquals(Token.TokenShape.SQUARE, TokenUtil.guessTokenType(img));

    img = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/squareToken.gif");

    assertEquals(Token.TokenShape.SQUARE, TokenUtil.guessTokenType(img));

    // CIRCLE
    img = new BufferedImage(100, 100, Transparency.BITMASK);
    g = img.createGraphics();
    g.setColor(Color.red);
    g.fillOval(0, 0, 100, 100);
    g.dispose();

    assertEquals(Token.TokenShape.CIRCLE, TokenUtil.guessTokenType(img));

    img = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/circleToken.png");

    assertEquals(Token.TokenShape.CIRCLE, TokenUtil.guessTokenType(img));

    // TOP DOWN
    img = new BufferedImage(100, 100, Transparency.BITMASK);
    g = img.createGraphics();
    g.setColor(Color.red);
    g.fillOval(0, 0, 10, 10);
    g.fillOval(90, 90, 10, 10);
    g.fillRect(0, 50, 100, 10);
    g.dispose();

    assertEquals(Token.TokenShape.TOP_DOWN, TokenUtil.guessTokenType(img));
  }
}
