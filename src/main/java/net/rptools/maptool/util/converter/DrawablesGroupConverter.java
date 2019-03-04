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
package net.rptools.maptool.util.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import net.rptools.maptool.model.drawing.DrawablesGroup;

/**
 * This is a null converter to tell Xstream to not serialize the class. Note: Custom content could
 * be written/read using such converters if needed.
 *
 * @author Jamz
 */
public class DrawablesGroupConverter implements Converter {
  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {}

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return null;
  }

  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(DrawablesGroup.class);
  }
}
