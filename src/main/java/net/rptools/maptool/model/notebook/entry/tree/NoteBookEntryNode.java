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
package net.rptools.maptool.model.notebook.entry.tree;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.rptools.maptool.model.notebook.entry.DirectoryEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;

public class NoteBookEntryNode {

  private final NoteBookEntry entry;

  private final Set<NoteBookEntryNode> children = new HashSet<>();

  public NoteBookEntryNode(NoteBookEntry entry) {
    this.entry = Objects.requireNonNull(entry, "Entry for NoteBookEntryNode cannot be null");
  }

  public void addChild(NoteBookEntryNode child) {
    if (!isDirectory()) {
      throw new IllegalStateException("Cannot add a child to a non directory NoteBookEntryNode");
    }
    Objects.requireNonNull(child, "Child to add to NoteBookEntryNode cannot be null");
    children.add(child);
  }

  public void removeChild(NoteBookEntryNode child) {
    Objects.requireNonNull(child, "Child to remove from NoteBookEntryNode cannot be null");
    children.remove(child);
  }

  public Set<NoteBookEntryNode> getChildren() {
    return children;
  }

  public boolean isDirectory() {
    return entry instanceof DirectoryEntry;
  }

  public NoteBookEntry getEntry() {
    return entry;
  }

  public NoteBookEntryNode duplicate() {
    NoteBookEntryNode node = new NoteBookEntryNode(entry);
    children.stream().map(NoteBookEntryNode::duplicate).forEach(node::addChild);

    return node;
  };
}
