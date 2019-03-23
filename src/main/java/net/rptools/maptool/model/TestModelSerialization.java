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
package net.rptools.maptool.model;

/** @author drice */
public class TestModelSerialization {
  //
  // private static final Zone generateZone() {
  // Zone z = new Zone("FOOBAR".getBytes());
  // z.setGridScale(107);
  //
  // return z;
  // }
  //
  // public static void main(String[] args) throws IOException {
  // ByteArrayOutputStream bout = new ByteArrayOutputStream();
  // HessianOutput hout = new HessianOutput(bout);
  //
  // try {
  // hout.call("test", new Object[] { generateZone() });
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  //
  // byte[] data = bout.toByteArray();
  //
  // HessianInput in = new HessianInput(new ByteArrayInputStream(data));
  // in.startCall();
  // List<Object> arguments = new ArrayList<Object>();
  // while (!in.isEnd()) {
  // arguments.add(in.readObject());
  // }
  // in.completeCall();
  //
  // Zone z = (Zone) arguments.get(0);
  //
  // System.out.println("background: " + new String(z.getBackground()));
  //
  // System.out.println(z.getGridScale());
  //
  // }
}
