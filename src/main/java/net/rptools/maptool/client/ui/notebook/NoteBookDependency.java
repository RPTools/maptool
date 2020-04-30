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
package net.rptools.maptool.client.ui.notebook;

public class NoteBookDependency {
  /** The namespace of the {@link net.rptools.maptool.model.notebook.NoteBook}. */
  private final String namespace;

  /** The version of the {@link net.rptools.maptool.model.notebook.NoteBook}. */
  private final String version;

  /**
   * Creates a new {@code NoteBookDependency} object.
   *
   * @param namespace The namespace of the {@link net.rptools.maptool.model.notebook.NoteBook}.
   * @param version The version of the {@link net.rptools.maptool.model.notebook.NoteBook}.
   */
  public NoteBookDependency(String namespace, String version) {
    this.namespace = namespace;
    this.version = version;
  }

  /**
   * Returns the namespace of the dependency.
   *
   * @return the namespace of the dependency.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Returns the version of the dependency.
   *
   * @return the version of the dependency.
   */
  public String getVersion() {
    return version;
  }

}
