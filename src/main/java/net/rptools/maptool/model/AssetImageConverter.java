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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AssetImageConverter extends EncodedByteArrayConverter {
  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    // Ignore the image when creating 1.3.b65+ campaigns with assets...
    // System.out.println(context.toString()); // uncomment to set a breakpoint
  }

  // @formatter:off
  /*
  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
  	// But be sure to read them in if they exist.
  	return super.unmarshal(reader, context);
  }
  */
  // @formatter:on

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class type) {
    return true; // Tell XStream that we can convert the image so it uses our methods
  }
}
