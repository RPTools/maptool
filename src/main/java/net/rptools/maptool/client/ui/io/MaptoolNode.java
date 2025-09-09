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
package net.rptools.maptool.client.ui.io;

import java.util.Objects;

/**
 * This class represents a single node in the tree used for import/export operations.
 *
 * <p>Each object of this type has a <code>String</code> to represent what the user sees in the tree
 * and an <code>Object</code> reference to a related piece of MapTool data. If there is no
 * associated MapTool data the <code>Object</code> contains <code>null</code>.
 *
 * @author crash
 */
public class MaptoolNode {
  /**
   * The name that displays for this node. This <code>String</code> becomes the identifier for this
   * node as well: our <b>hashCode()</b> returns the <b>name</b>'s <b>hashCode()</b>, although our
   * <b>equals()</b> method does more than just compare the two <code>String</code>s.
   */
  private String name;

  /**
   * A <code>null</code> <b>object</b> field is indicative of an entry in the tree that doesn't have
   * a corresponding MapTool object to go with it. This should only be the case for "folder nodes".
   * Note that some folder nodes <b>will</b> have data to go with them and that data will be the
   * overall configuration for everything underneath them. For example, "Campaign/Map" may have
   * default units for the grid size. The default doesn't apply to every map, but since it's a
   * default it has to be stored somewhere. The individual maps have their own sizes.
   */
  private Object object;

  /**
   * The {@code SELECTED} value means that the checkbox is currently selected and should be rendered
   * that way (typically a box with a checkmark inside).
   *
   * <p>The {@code UNSELECTED} value means that the checkbox is unselected and should be rendered
   * empty (typically an empty box).
   *
   * <p>The {@code PARTIAL} value means that children nodes below this one (meaning this on cannot
   * be a leaf) are a mixed set of {@code SELECTED} and {@code UNSELECTED} nodes.
   *
   * @author crash
   */
  enum CheckBoxState {
    SELECTED,
    UNSELECTED,
    PARTIAL
  }

  /** Used to hold the checkbox state when using our own {@code TreeCellRenderer}. */
  private CheckBoxState selected;

  /**
   * This is the normal constructor. It is called with a string that will be used to display the
   * tree node and an object that represents one unit of MapTool application data, such as a
   * MacroButtonProperties or LookupTable object.
   *
   * <p>If a {@code null} object is provided, this node represents a logical folder within the
   * heirarchy of data, but not an actual MapTool object. This is fairly rare except at the upper
   * reaches of the tree, since lower nodes typically hold the defaults for the objects below them.
   * For example, a folder full of maps would still have an object associated with it -- the object
   * would hold the defaults used when creating new maps.
   *
   * @param n the {@code String} used for the visual representation
   * @param o the MapTool {@code Object} to be stored with this node
   */
  public MaptoolNode(String n, Object o) {
    name = n;
    object = o;
  }

  public MaptoolNode(String n) {
    this(n, null);
  }

  public Object getObject() {
    return object;
  }

  public String getName() {
    return name;
  }

  /**
   * This one is unfortunately complicated by the fact that either <code>object</code> could be
   * <code>null</code>. If they're both <code>null</code>, then the two objects are potentially
   * equal. If they're both non-<code>null</code>, then their equal() methods need to be called. And
   * if one is <code>null</code> and the other isn't, they can't be equal.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj instanceof MaptoolNode) {
      MaptoolNode mtn = (MaptoolNode) obj;
      if (!mtn.name.equals(this.name)) return false;
      if (mtn.object == null && this.object == null) return true;
      if (mtn.object != null && this.object != null && mtn.object.equals(this.object)) return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, object);
  }

  /**
   * Eventually I would like this to return not just the string name of this node, but also the
   * number of children under this node. That would cause the JTree display to show quantities,
   * which I think could be useful. But it means moving more state into this class and I don't like
   * that idea. I think it should be kept separately.
   */
  public String toString() {
    return name;
  }
}
