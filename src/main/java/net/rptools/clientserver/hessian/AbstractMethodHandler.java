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
package main.java.net.rptools.clientserver.hessian;

import caucho.hessian.io.HessianInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public abstract class AbstractMethodHandler implements MethodHandler {

  /* (non-Javadoc)
   * @see clientserver.simple.MessageHandler#handleMessage(java.lang.String, byte[])
   */
  public void handleMessage(String id, byte[] message) {
    try {

      HessianInput in = null;
      try {
        in =
            HessianUtils.createSafeHessianInput(
                new GZIPInputStream(new ByteArrayInputStream(message)));
      } catch (IOException ioe) {
        in = HessianUtils.createSafeHessianInput(new ByteArrayInputStream(message));
      }
      in.startCall();
      List<Object> arguments = new ArrayList<Object>();
      while (!in.isEnd()) {
        arguments.add(in.readObject());
      }
      in.completeCall();

      handleMethod(id, in.getMethod(), arguments.toArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
