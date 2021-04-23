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

import java.util.*;
import java.util.stream.Collectors;
import net.rptools.common.expression.ExpressionParser;
import net.rptools.common.expression.Result;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.parser.ParserException;

public class LookupTable {

  private static ExpressionParser expressionParser = new ExpressionParser();

  private List<LookupEntry> entryList;
  private String name;
  private String defaultRoll;
  private MD5Key tableImage;
  private Boolean visible;
  private Boolean allowLookup;
  // Flags a table as Pick Once, i.e. each entry can only be chosen once before the
  // table must be reset().
  private Boolean pickOnce = false;

  public static final String NO_PICKS_LEFT = "NO_PICKS_LEFT";

  public LookupTable() {}

  public LookupTable(LookupTable table) {
    name = table.name;
    defaultRoll = table.defaultRoll;
    tableImage = table.tableImage;
    pickOnce = Objects.requireNonNullElse(table.pickOnce, false);

    if (table.entryList != null) {
      getInternalEntryList().addAll(table.entryList);
    }
  }

  public void setRoll(String roll) {
    defaultRoll = roll;
  }

  public void clearEntries() {
    getInternalEntryList().clear();
  }

  public void addEntry(int min, int max, String result, MD5Key imageId) {
    getInternalEntryList().add(new LookupEntry(min, max, result, imageId));
  }

  public LookupEntry getLookup() throws ParserException {
    return getLookup(null);
  }

  public String getRoll() {
    return getDefaultRoll();
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Accepts a string containing a valid dice expression or integer which is evaluated and then the
   * matching entry in the table is returned.
   *
   * @param roll A string containing a dice expression or integer.
   * @return A LookupEntry matching the roll.
   * @throws ParserException if roll can't be parsed as integer or die expression
   */
  public LookupEntry getLookup(String roll) throws ParserException {
    LookupEntry entry;

    if (roll == null) {
      roll = getDefaultRoll();
    }

    if (roll.equals(NO_PICKS_LEFT)) {
      entry = new LookupEntry(0, 0, NO_PICKS_LEFT, null);
      return entry;
    }

    if (getPickOnce()) {
      entry = getPickOnceLookup(roll);
    } else {
      entry = getStandardLookup(roll);
    }

    return entry;
  }

  /**
   * Accepts a string containing a valid dice expression or integer which is evaluated and then the
   * matching entry in the table is returned without filtering for picked entries.
   *
   * @param roll A string containing a dice expression or integer.
   * @return A LookupEntry matching the roll.
   */
  public LookupEntry getLookupDirect(String roll) throws ParserException {
    LookupEntry entry;

    if (roll == null) {
      return (null);
    }

    entry = getStandardLookup(roll);

    return entry;
  }

  private LookupEntry getStandardLookup(String roll) throws ParserException {
    int tableResult = 0;
    LookupEntry retEntry = null;

    try {
      Result result = expressionParser.evaluate(roll);
      tableResult = Integer.parseInt(result.getValue().toString());

      tableResult = constrainRoll(tableResult);

      for (LookupEntry entry : getInternalEntryList()) {
        if (tableResult >= entry.min && tableResult <= entry.max) {
          retEntry = entry;
        }
      }

    } catch (NumberFormatException nfe) {
      throw new ParserException("Error lookup up value: " + tableResult);
    }

    return retEntry;
  }

  private LookupEntry getPickOnceLookup(String roll) throws ParserException {
    try {
      int entryNum = Integer.parseInt(roll);

      if (entryNum < entryList.size()) {
        LookupEntry entry = entryList.get(entryNum);
        entry.setPicked(true);
        entryList.set(entryNum, entry);

        return entry;
      } else {
        return new LookupEntry(0, 0, NO_PICKS_LEFT, null);
      }

    } catch (NumberFormatException nfe) {
      throw new ParserException("Expected integer value for pick once table: " + roll);
    }
  }

  private int constrainRoll(int val) {
    int minmin = Integer.MAX_VALUE;
    int maxmax = Integer.MIN_VALUE;

    for (LookupEntry entry : getInternalEntryList()) {
      if (entry.min < minmin) {
        minmin = entry.min;
      }
      if (entry.max > maxmax) {
        maxmax = entry.max;
      }
    }
    if (val > maxmax) {
      val = maxmax;
    }
    if (val < minmin) {
      val = minmin;
    }
    return val;
  }

  private String getDefaultRoll() {
    if (getPickOnce()) {
      // For Pick Once tables this returns a random pick from those entries in the list that
      // have not been picked.
      List<LookupEntry> le = getInternalEntryList();
      LookupEntry entry;
      int len = le.size();
      List unpicked = new ArrayList<Integer>();
      for (int i = 0; i < len; i++) {
        entry = le.get(i);
        if (!entry.picked) {
          unpicked.add(i);
        }
      }
      if (unpicked.isEmpty()) {
        return (NO_PICKS_LEFT);
      }
      try {
        Result result = expressionParser.evaluate("d" + unpicked.size());
        int index = Integer.parseInt(result.getValue().toString()) - 1;
        return unpicked.get(index).toString();
      } catch (ParserException e) {
        MapTool.showError("Error getting default roll for Pick Once table ", e);
        return (NO_PICKS_LEFT);
      }
    } else {
      if (defaultRoll != null && defaultRoll.length() > 0) {
        return defaultRoll;
      }

      // Find the min and max range
      Integer min = null;
      Integer max = null;

      for (LookupEntry entry : getInternalEntryList()) {
        if (min == null || entry.min < min) {
          min = entry.min;
        }
        if (max == null || entry.max > max) {
          max = entry.max;
        }
      }

      return min != null ? "d" + (max - min + 1) + (min - 1 != 0 ? "+" + (min - 1) : "") : "";
    }
  }

  private List<LookupEntry> getInternalEntryList() {
    if (entryList == null) {
      entryList = new ArrayList<>();
    }
    return entryList;
  }

  /** Sets the picked flag on each table entry to false. */
  public void reset() {
    List<LookupEntry> curList = getInternalEntryList();
    List<LookupEntry> newList = new ArrayList<>();
    for (LookupEntry entry : curList) {
      entry.setPicked(false);
      newList.add(entry);
    }
    entryList = newList;
  }

  /**
   * Reset the picked status of specific entries, allowing them to be picked again in this PickOnce
   * table. Note that this uses the same indexing scheme as {@link #getPickOnceLookup(String)} -
   * these entries are identified by list index (starting at 0), and NOT by any configured range.
   *
   * @param entriesToReset a list of strings representing the integer indices of entries to reset
   * @throws NumberFormatException if any of the string entries cannot be successfully parsed as an
   *     integer.
   */
  public void reset(List<String> entriesToReset) {
    Set<Integer> indicesToReset =
        entriesToReset.stream()
            .map(Integer::parseInt)
            .collect(Collectors.toCollection(HashSet::new));
    List<LookupEntry> curList = getInternalEntryList();
    List<LookupEntry> newList = new ArrayList<>();
    for (int i = 0; i < curList.size(); i++) {
      LookupEntry entry = curList.get(i);
      if (indicesToReset.contains(i)) {
        entry.setPicked(false);
      }
      newList.add(entry);
    }
    entryList = newList;
  }

  /**
   * Get a List of the LookupEntrys for this table.
   *
   * @return List of LookupEntrys
   */
  public List<LookupEntry> getEntryList() {
    return Collections.unmodifiableList(getInternalEntryList());
  }

  /**
   * Get the MD5Key (Asset ID) for the image that represents the table in the Tables Window.
   *
   * @return MD5Key
   */
  public MD5Key getTableImage() {
    return tableImage;
  }

  /**
   * Set an image for the table to be displayed in the Tables Window.
   *
   * @param tableImage The MD5Key (Asset ID) for the image.
   */
  public void setTableImage(MD5Key tableImage) {
    this.tableImage = tableImage;
  }

  /**
   * Gets whether a table is flagged as Pick Once or not.
   *
   * @return Boolean - true if table is Pick Once
   */
  public Boolean getPickOnce() {
    // Older tables won't have it set.
    if (pickOnce == null) {
      pickOnce = false;
    }

    return pickOnce;
  }

  /**
   * Set whether a table as Pick Once (true/false). Automatically resets the pick once status of
   * entries.
   *
   * @param pickOnce - Boolean
   */
  public void setPickOnce(Boolean pickOnce) {
    this.pickOnce = pickOnce;
    this.reset();
  }

  /**
   * Get the number of picks left in a table.
   *
   * <p>Note that for non-PickOnce tables this will be a count of the entries.
   *
   * @return count of the entries in the table that have not been picked.
   */
  public int getPicksLeft() {
    int count = 0;
    for (LookupEntry entry : getInternalEntryList()) {
      if (!entry.picked) {
        count++;
      }
    }
    return count;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    for (LookupEntry entry : getInternalEntryList()) {

      if (entry.min == entry.max) {
        builder.append(entry.min);
      } else {
        builder.append(entry.min).append("-").append(entry.max);
      }
      builder.append("=").append(entry.value).append("\n");
    }

    return builder.toString();
  }

  public static class LookupEntry {

    private int min;
    private int max;
    // For Pick Once tables each entry is flagged as picked (true) or not (false).
    private Boolean picked = false;

    private String value;

    private MD5Key imageId;

    /** @Deprecated here to prevent xstream from breaking b24-b25 */
    private String result;

    public LookupEntry(int min, int max, String result, MD5Key imageId) {
      this.min = min;
      this.max = max;
      this.value = result;
      this.imageId = imageId;
    }

    public MD5Key getImageId() {
      return imageId;
    }

    public void setPicked(Boolean b) {
      picked = b;
    }

    public Boolean getPicked() {
      return picked;
    }

    public int getMax() {
      return max;
    }

    public int getMin() {
      return min;
    }

    public String getValue() {
      // Temporary fix to convert b24 to b25
      if (result != null) {
        value = result;
        result = null;
      }
      return value;
    }
  }

  public Set<MD5Key> getAllAssetIds() {

    Set<MD5Key> assetSet = new HashSet<>();
    if (getTableImage() != null) {
      assetSet.add(getTableImage());
    }
    for (LookupEntry entry : getEntryList()) {
      if (entry.getImageId() != null) {
        assetSet.add(entry.getImageId());
      }
    }
    return assetSet;
  }

  /**
   * Retrieves the visible flag for the LookupTable.
   *
   * @return Boolean -- True indicates that the table will be visible to players. False indicates
   *     that the table will be hidden from players.
   */
  public Boolean getVisible() {
    if (visible == null) {
      visible = true;
    }
    return visible;
  }

  /**
   * Sets the visible flag for the LookupTable.
   *
   * @param value(Boolean) -- True specifies that the table will be visible to players. False
   *     indicates that the table will be hidden from players.
   */
  public void setVisible(Boolean value) {
    visible = value;
  }

  /**
   * Retrieves the allowLookup flag for the LookupTable.
   *
   * @return Boolean -- True indicates that players can call for values from this table. False
   *     indicates that players will be prevented from calling values from this table. GM's can
   *     ALWAYS perform lookups against a table.
   */
  public Boolean getAllowLookup() {
    if (allowLookup == null) {
      allowLookup = true;
    }
    return allowLookup;
  }

  /**
   * Sets the allowLookup flag for the LookupTable.
   *
   * @param value(Boolean) -- True indicates that players can call for values from this table. False
   *     indicates that players will be prevented from calling values from this table. GM's can
   *     ALWAYS perform lookups against a table.
   */
  public void setAllowLookup(Boolean value) {
    allowLookup = value;
  }
}
