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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class acts as the basis for various MapTool subsystems to store data in an external file.
 *
 * <p>The idea is that each subsystem will instantiate this class and pass it to the {@code
 * LoadSaveImpl} registry. When the {@code LoadSaveImpl} needs to ask the subsystem for data, it
 * will call the {@code addDataObjects()} method.
 *
 * <p>That method will take the string and data object and add entries to the tree.
 *
 * <p>The subsystem accomplishes this by overriding {@code prepareForDisplay()} and calling {@code
 * addDataObjects()}. There are multiple definitions of the latter method so that the user can pass
 * two parameters and the compiler will figure out which to invoke.
 *
 * <p>On the application side, the user has passed on an instantiated object, so when we're ready,
 * we call {@code prepareForDisplay(UIBuilder.Tree(model)} and pass the tree model, where it is
 * stored inside the object. When the {@code addDataObjects()} is done it will return to {@code
 * prepareForDisplay()}, which then returns to the application. The same object may be used again
 * later.
 *
 * <p>The MapTool object being stored should have a {@code toString()} method that produces the
 * desired display string on the UI. This isn't strictly necessary, because for {@code Map}s the
 * String key is used, but when the data structure is a {@code List} of some type, there is no key.
 * This class copies all of the elements into a {@code Map} using the results of converting the
 * object into a {@c String} as the key. The strings are then sorted and added to the UI.
 *
 * @author crash
 */
public abstract class DataTemplate {

  private static final Logger log = LogManager.getLogger(DataTemplate.class);

  /**
   * This method returns the string used to position the data within the tree displayed by the UI
   * code.
   *
   * <p>Normally this method will be overridden and simply return a hard-coded string. (Although it
   * probably shouldn't be hard-coded, but read from a properties file.)
   *
   * <p>However, if a subsystem has a large number of different data structures to save (such as the
   * {@code CampaignProperties} class) then it won't be practical to use this method -- the string
   * would need to be different for each category of campaign property, meaning a lot of almost
   * identical child classes. :( Instead, this method can be ignored and the version of the {@code
   * addDataObjects()} methods which take a {@code String} parameter can be used instead.
   *
   * @return the string representing the tree path, such as <b>Campaign/Properties</b> or
   *     <b>Campaign/Maps</b>
   */
  public String getTreePath() {
    // The default string makes it clear that some software is misconfigured.
    return "Misconfigured subsystem: getTreePath()";
  }

  public abstract String getSubsystemName();

  private UIBuilder.TreeModel model;

  /**
   * Thie method is invoked only by the UI application code. It starts the process of calling the
   * user-defined {@code prepareForDisplay()} method that will actually fill the data structure.
   *
   * @param m the data model to use to store information into the tree
   */
  void populateModel(UIBuilder.TreeModel m) {
    model = m;
    log.debug("DataTemplate:  processing " + this.getSubsystemName());
    prepareForDisplay();
    model = null;
  }

  /**
   * This method is overridden by the subsystem author when they have data they want to see
   * persisted in an external file. They create this method and use it to invoke {@code
   * addDataObjects()}, passing the data structure to the latter method.
   *
   * <p>This sample code would be used when there are multiple different path strings needed by the
   * subsystem. Ordinarily all of a subsystem's data would be under a single TreePath location in
   * the model.
   *
   * <pre>
   * public void prepareForDisplay() {
   * 	addDataObjects("Campaign/Properties/Token Properties", cmpgn.getTokenTypeMap());
   * 	addDataObjects("Campaign/Properties/Repositories", cmpgn.getRemoteRepositoryList());
   * 	addDataObjects("Campaign/Properties/Sights", cmpgn.getSightTypeMap());
   * 	addDataObjects("Campaign/Properties/Lights", cmpgn.getLightSourcesMap());
   * 	addDataObjects("Campaign/Properties/States", cmpgn.getTokenStatesMap());
   * 	addDataObjects("Campaign/Properties/Bars", cmpgn.getTokenBarsMap());
   * 	addDataObjects("Campaign/Properties/Tables", cmpgn.getLookupTableMap());
   * 	addDataObjects("Campaign/Properties/Macros", cmpgn.getMacroButtonPropertiesArray());
   * }
   * </pre>
   *
   * <p>This sample code would be used when there is but a single path string for a lot of different
   * data objects.
   *
   * <pre>
   * public String getTreePath() {
   * 	return "Campaign/Maps";
   * }
   *
   * public void prepareForDisplay() {
   * 	addDataObjects(Zone.getAllZones());
   * }
   * </pre>
   */
  public abstract void prepareForDisplay();

  /**
   * When the subsystem's data is stored in a <code>Map</code>, use this method or the similar one
   * that doesn't take a <code>String</code>.
   *
   * <p>This method iterates through all elements in the map and adds a <code>MaptoolNode</code> for
   * each one to the model. The keys are sorted alphabetically before being added to the list.
   *
   * @param path the <code>String</code> representation of the TreePath
   * @param data the <code>Map</code> to add to the UI
   */
  protected final void addDataObjects(String path, Map<String, ? extends Object> data) {
    String[] sorted = new String[data.size()];
    data.keySet().toArray(sorted);
    Arrays.sort(sorted);

    for (int i = 0; i < sorted.length; i++) {
      Object one = data.get(sorted[i]);
      model.addNode(path, new MaptoolNode(sorted[i], one));
    }
  }

  /**
   * Calls {@link #addDataObjects(String, Map)} and passes <code>getTreePath()</code> as the first
   * parameter.
   *
   * @param data the <code>Map</code> to add to the UI
   */
  protected final void addDataObjects(Map<String, ? extends Object> data) {
    addDataObjects(getTreePath(), data);
  }

  /**
   * When the subsystem's data is stored in something other than a <code>Map</code>, use this method
   * or the similar one that doesn't take a <code>String</code>.
   *
   * <p>This method iterates through all elements in the collection (usually a <code>List</code>)
   * and adds a <code>MaptoolNode</code> for each one to the model. The keys are sorted
   * alphabetically before being added to the list.
   *
   * @param path the <code>String</code> representation of the TreePath
   * @param data the <code>Collection</code> to add to the UI (usually a <code>List</code>)
   */
  protected final void addDataObjects(String path, Collection<? extends Object> data) {
    Map<String, Object> mymapping = new HashMap<String, Object>(data.size());
    Iterator<? extends Object> iter = data.iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      mymapping.put(o.toString(), o);
    }
    addDataObjects(path, mymapping);
  }

  /**
   * Calls {@link #addDataObjects(String, Map)} and passes <code>getTreePath()</code> as the first
   * parameter.
   *
   * @param data the <code>Collection</code> to add to the UI (usually a <code>List</code>)
   */
  protected final void addDataObjects(Collection<? extends Object> data) {
    addDataObjects(getTreePath(), data);
  }
}
