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

import com.caucho.hessian.io.HessianFactory;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

/** @author drice */
public class HessianUtils {

  public static HessianInput createSafeHessianInput(InputStream is) {
    HessianFactory hessianFactory = new HessianFactory();
    HessianSecurity hessianSecurity = new HessianSecurity();
    HessianInput in = hessianFactory.createHessianInput(is);
    in.getSerializerFactory().setAllowNonSerializable(true);
    in.getSerializerFactory().getClassFactory().setWhitelist(true);
    hessianSecurity.getAllowed().forEach(a -> in.getSerializerFactory().getClassFactory().allow(a));
    hessianSecurity.getDenied().forEach(d -> in.getSerializerFactory().getClassFactory().allow(d));
    return in;
  }

  public static final byte[] methodToBytes(String method, Object... parameters) {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    HessianOutput hout = new HessianOutput(bout);
    hout.getSerializerFactory().setAllowNonSerializable(true);

    try {
      hout.call(method, parameters);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bout.toByteArray();
  }

  public static final byte[] methodToBytesGZ(String method, Object... parameters) {

    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    try {

      ByteArrayOutputStream hessianBytes = new ByteArrayOutputStream();
      HessianOutput hout = new HessianOutput(hessianBytes);
      hout.getSerializerFactory().setAllowNonSerializable(true);
      hout.call(method, parameters);

      GZIPOutputStream gzip = new GZIPOutputStream(bout);
      gzip.write(hessianBytes.toByteArray());
      gzip.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    return bout.toByteArray();
  }
}
