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

import com.google.protobuf.StringValue;
import net.rptools.dicelib.expression.ExpressionParser;
import net.rptools.dicelib.expression.Result;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.server.proto.LookupEntryDto;
import net.rptools.maptool.server.proto.LookupTableDto;
import net.rptools.parser.ParserException;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LookupTable represents a table of die roll ranges and a String value (with an optional
 * asset ID).
 * <p>
 * The general idea is that random picks from a list are often a die roll, such as "2d6",
 * that is then used to perform a lookup on a table.  The typical random encounter table,
 * for example, might look something like this:
 * <table><thead>
 *     <tr><th colspan="2">Die range (2d6)</th><th>Value</th><th>Table image</th></tr>
 * </thead>
 * <tbody>
 *     <tr><td>2</td><td>4</td><td>Orcs, group of 1d4</td><td><b>null</b></td></tr>
 *     <tr><td>5</td><td>6</td><td>Goblins, group of 2d4+2</td><td><b>null</b></td></tr>
 *     <tr><td>7</td><td></td><td>Hobgoblins, family unit of 3</td><td><b>null</b></td></tr>
 *     <tr><td>8</td><td>9</td><td>Bandits, brother/sister team</td><td><b>null</b></td></tr>
 *     <tr><td>10</td><td>12</td><td>City guard, squad of 4</td><td><b>null</b></td></tr>
 * </tbody></table>
 * <p>
 * If the <i>Die range</i> contains only a single value, then only that value will select that row.<br/>
 * The <i>default roll</i> is the "2d6" shown in the table header (column one).<br/>
 * The <i>Value</i> column is the String returned for a given lookup.<br/>
 * The <i>Table image</i> column contains either <code>null</code> or an {@link MD5Key} for an asset.
 * </p>
 */
public class LookupTable {

  private static final ExpressionParser expressionParser = new ExpressionParser();

  /**
   * The complete list of entries in the table.
   * (Future implementation changes will likely be sorted so that binary searches
   * can find entries more quickly.)
   */
  private @Nonnull List<LookupEntry> entryList = new ArrayList<>();
  private @Nullable String name;
  /**
   * The die expression to use when one isn't provided when retrieving values.
   * <p>
   * If no value is set, the default implementation will choose a row randomly,
   * ignoring the min/max values (so each row has an equal chance of being selected).
   */
  private @Nullable String defaultRoll;
  private @Nullable MD5Key tableImage;
  /**
   * Whether the table is visible for players in the Table panel.
   */
  private @Nonnull Boolean visible = true;
  /**
   * Whether players can read elements from the table.
   */
  private @Nonnull Boolean allowLookup = true;
  /**
   * Flags a table as "Pick Once", i.e. each entry can only be chosen once
   * before the table must be reset().
   */
  private @Nonnull Boolean pickOnce = false;

  /**
   * Unique string returned when all picks from a Pick Once table have
   * been selected.
   * <p>
   * DO NOT use this string as a table value! 😁
   */
  public static final String NO_PICKS_LEFT = "NO_PICKS_LEFT";

  public LookupTable() {}

  public LookupTable(LookupTable table) {
    name = table.name;
    defaultRoll = table.defaultRoll;
    tableImage = table.tableImage;
    pickOnce = table.pickOnce;
    visible = table.visible;
    allowLookup = table.allowLookup;
    entryList.addAll(table.entryList);
  }

  /**
   * Sets the <code>defaultRoll</code> field that is used to choose a random element
   * from the table when no die roll expression is provided.  See {@link #getLookup()}.
   *
   * @param roll String expression representing default roll
   */
  public void setRoll(String roll) {
    defaultRoll = roll;
  }

  /**
   * Removes all entries from the table, but does not reset the default roll
   * or other fields.  (Note that for Pick Once tables, the pick status is
   * part of each entry, so those are cleared by this method as well.)
   */
  public void clearEntries() {
    entryList.clear();
  }

  /**
   * Adds a single row to the table using the specified parameters.
   *
   * @param min lower bound of the die roll
   * @param max upper bound of the die roll
   * @param result a non-<code>null</code> String to store into the table
   * @param imageId the asset ID (may be <code>null</code>)
   */
  public void addEntry(int min, int max, String result, MD5Key imageId) {
    entryList.add(new LookupEntry(min, max, result, imageId));
  }

  /**
   * Calls {@link #getLookup(String)} and passes it a <code>null</code> parameter.
   *
   * @return the result of calling {@link #getLookup(String)}
   * @throws ParserException thrown when the default roll expression isn't valid
   */
  public LookupEntry getLookup() throws ParserException {
    return getLookup(null);
  }

//  public String getRoll() {
//    return getDefaultRoll();
//  }

  /**
   * Set the table name.
   * <p>
   * This method should not be called after the table is added to
   * the campaign properties, as the <code>Map<></code> therein relies on the
   * name for the hash code.
   *
   * @param name name of the table
   */
  public void setName(@org.jetbrains.annotations.Nullable String name) {
    this.name = name;
  }

  /**
   * Retrieves the name of this table.
   *
   * @return name of the table
   */
  public @org.jetbrains.annotations.Nullable String getName() {
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

  /**
   * Evaluates the <code>roll</code> expression and returns the appropriate table entry.
   * The {@link #getLookup(String)} method delegates here for non-Pick Once tables.
   *
   * @param roll dice expression
   * @return selected table entry; returns <code>null</code> if there's no matching range
   * @throws ParserException thrown if the dice expression is invalid
   */
  private LookupEntry getStandardLookup(String roll) throws ParserException {
    int tableResult = 0;
    LookupEntry retEntry = null;

    try {
      Result result = expressionParser.evaluate(roll);
      tableResult = Integer.parseInt(result.getValue().toString());

      tableResult = constrainRoll(tableResult);

      for (LookupEntry entry : entryList) {
        if (tableResult >= entry.min && tableResult <= entry.max) {
          retEntry = entry;
        }
      }

    } catch (NumberFormatException nfe) {
      throw new ParserException("Error lookup up value: " + tableResult);
    }

    return retEntry;
  }

  /**
   * Returns an entry from a Pick Once table that hasn't already been selected.
   * The {@link #getLookup(String)} method delegates here for Pick Once tables.
   * <p>
   * For this method, the <code>roll</code> parameter must be a string that
   * can be converted to integer via {@link Integer#parseInt(String)} -- a
   * dice expression is not allowed.
   *
   * @param roll String representing which entry to retrieve
   * @return the table entry corresponding to the <code>roll</code> parameter
   * @throws ParserException thrown if <code>roll</code> cannot be parsed as an integer
   */
  private LookupEntry getPickOnceLookup(String roll) throws ParserException {
    try {
      int entryNum = Integer.parseInt(roll);

      if (entryNum < entryList.size()) {
        LookupEntry entry = entryList.get(entryNum);
        entry.setPicked(true);
        entryList.set(entryNum, entry); // TODO Isn't this redundant??
        return entry;
      } else {
        return new LookupEntry(0, 0, NO_PICKS_LEFT, null);
      }

    } catch (NumberFormatException nfe) {
      throw new ParserException("Expected integer value for Pick Once table: " + roll);
    }
  }

  /**
   * Constrains the parameter to be within the range of values depicted by the table entries.
   * Values less than the lowest minimum entry are set the minimum, while values larger than
   * the largest maximum entry are set to the maximum.
   * <p>
   * There is no check to determine whether <code>val</code> actually identifies a specific
   * table entry per the min/max values, so the return value (if unmodified) may not
   * represent an actual table entry.
   *
   * @param val value to be constrained
   * @return the constrained value
   */
  private int constrainRoll(int val) {
    int minmin = Integer.MAX_VALUE;
    int maxmax = Integer.MIN_VALUE;

    for (LookupEntry entry : entryList) {
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

  /**
   * Has two modes of operation.
   * <ul>
   *     <li>For standard tables, it retrieves the {@link #defaultRoll}
   * if it has been set, or calculates a dice expression to use and returns that.
   *     <li>For Pick Once tables, it ignores the {@link #defaultRoll}
   * and instead rolls a random number which indexes into a subset of the table made up only
   * of entries which have not been previously selected.  The <code>value</code> of that
   * entry is returned.
   * </ul>
   *
   * @return the <code>value</code> field from the table for the associated entry; Pick Once
   * tables also have their <code>picked</code> flag set so that they are not picked again
   */
  public String getDefaultRoll() {
    if (getPickOnce()) {
      // For Pick Once tables this returns a random pick from those entries in the list that
      // have not been picked.
      LookupEntry[] unpicked = entryList.stream().filter(e -> !e.picked).toArray(LookupEntry[]::new);
      try {
        if (unpicked.length != 0) {
          Result result = expressionParser.evaluate("d" + unpicked.length);
          int index = Integer.parseInt(result.getValue().toString()) - 1;
          unpicked[index].picked = true;
          return unpicked[index].getValue();
        }
      } catch (ParserException e) {
        MapTool.showError("Error getting default roll for Pick Once table: " + name, e);
      }
      return (NO_PICKS_LEFT);
    } else {
      // If the defaultRoll hasn't been set or is an empty String, return it.
      if (defaultRoll != null && !defaultRoll.isEmpty()) {
        return defaultRoll;
      }

      // Find the min and max range
      Integer min = null;
      Integer max = null;

      for (LookupEntry entry : entryList) {
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

  /**
   * Sets the picked flag on each table entry to false.
   * <p>
   * This method returns immediately if the table is not a Pick Once table.
   */
  public void reset() {
    if (pickOnce) {
      List<LookupEntry> curList = entryList;
      List<LookupEntry> newList = new ArrayList<>();
      for (LookupEntry entry : curList) {
        entry.setPicked(false);
        newList.add(entry);
      }
      entryList = newList;
    }
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
    List<LookupEntry> curList = entryList;
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
   * @return unmodifiable list of LookupEntrys
   */
  public List<LookupEntry> getEntryList() {
    return Collections.unmodifiableList(entryList);
  }

  /**
   * Get the MD5Key (Asset ID) for the image that represents the table in the Tables Window.
   *
   * @return MD5Key
   */
  public @org.jetbrains.annotations.Nullable MD5Key getTableImage() {
    return tableImage;
  }

  /**
   * Set an image for the table to be displayed in the Tables Window.
   *
   * @param tableImage The MD5Key (Asset ID) for the image.
   */
  public void setTableImage(@org.jetbrains.annotations.Nullable MD5Key tableImage) {
    this.tableImage = tableImage;
  }

  /**
   * Gets whether a table is flagged as Pick Once or not.
   *
   * @return true if table is Pick Once
   */
  public boolean getPickOnce() {
    return pickOnce;
  }

  /**
   * Set whether a table is Pick Once (true/false). Automatically resets the pick once status of
   * entries.
   *
   * @param pickOnce <code>true</code> if the table should be treated as Pick Once
   */
  public void setPickOnce(boolean pickOnce) {
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
    for (LookupEntry entry : entryList) {
      if (!entry.picked) {
        count++;
      }
    }
    return count;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    for (LookupEntry entry : entryList) {

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

    private final int min;
    private final int max;
    // For Pick Once tables each entry is flagged as picked (true) or not (false).
    private @Nonnull Boolean picked = false;
    private @Nullable String value;
    private final @Nullable MD5Key imageId;

    /**
     * @deprecated here to prevent xstream from breaking b24-b25
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated private @Nullable String result;

    public LookupEntry(int min, int max,
                       @org.jetbrains.annotations.Nullable String value,
                       @org.jetbrains.annotations.Nullable MD5Key imageId) {
      this.min = min;
      this.max = max;
      this.value = value;
      this.imageId = imageId;
    }

    @Contract(value = " -> this")
    @SuppressWarnings({"ConstantValue", "unused"})
    private Object readResolve() {
      if (picked == null) {
        picked = false;
      }
      // Temporary fix to convert b24 to b25
      if (result != null) {
        value = result;
        result = null;
      }

      return this;
    }

    public @org.jetbrains.annotations.Nullable MD5Key getImageId() {
      return imageId;
    }

    public void setPicked(boolean b) {
      picked = b;
    }

    public boolean getPicked() {
      return picked;
    }

    public int getMax() {
      return max;
    }

    public int getMin() {
      return min;
    }

    public @org.jetbrains.annotations.Nullable String getValue() {
      return value;
    }

    public static LookupEntry fromDto(LookupEntryDto dto) {
      var entry =
          new LookupEntry(
              dto.getMin(),
              dto.getMax(),
              dto.hasValue() ? dto.getValue().getValue() : null,
              dto.hasImageId() ? new MD5Key(dto.getImageId().getValue()) : null);
      entry.picked = dto.getPicked();
      return entry;
    }

    public LookupEntryDto toDto() {
      var dto = LookupEntryDto.newBuilder();
      dto.setMin(min);
      dto.setMax(max);
      dto.setPicked(picked);
      if (value != null) {
        dto.setValue(StringValue.of(value));
      }
      if (imageId != null) {
        dto.setImageId(StringValue.of(imageId.toString()));
      }
      return dto.build();
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
   * @return <code>true</code> indicates that the table will be visible to players.
   * Otherwise, the table will be hidden from players.
   */
  public boolean getVisible() {
    return visible;
  }

  /**
   * Sets the visible flag for the LookupTable.
   *
   * @param value <code>true</code> specifies that the table will be visible to players.
   *     Otherwise, the table will be hidden from players.
   */
  public void setVisible(boolean value) {
    visible = value;
  }

  /**
   * Retrieves the allowLookup flag for the LookupTable.
   *
   * @return <code>true</code> indicates that players can call for values from this table.
   * Otherwise, players will be prevented from calling values from this table.
   * GM's can ALWAYS perform lookups against a table.
   */
  public boolean getAllowLookup() {
    return allowLookup;
  }

  /**
   * Sets the allowLookup flag for the LookupTable.
   *
   * @param value <code>true</code> indicates that players can call for values from this table.
   * Otherwise, players will be prevented from calling values from this table.
   * GM's can ALWAYS perform lookups against a table.
   */
  public void setAllowLookup(boolean value) {
    allowLookup = value;
  }

  @Contract(value = " -> this")
  @SuppressWarnings({"ConstantValue", "unused"})
  private Object readResolve() {
    if (visible == null) {
      visible = true;
    }
    if (pickOnce == null) {
      pickOnce = false;
    }
    if (allowLookup == null) {
      allowLookup = true;
    }
    if (entryList == null) {
      entryList = new ArrayList<>();
    }
    return this;
  }

  public static LookupTable fromDto(LookupTableDto dto) {
    var table = new LookupTable();
    table.name = dto.hasName() ? dto.getName().getValue() : null;
    table.entryList =
        dto.getEntriesList().stream().map(LookupEntry::fromDto).collect(Collectors.toList());
    table.defaultRoll = dto.hasDefaultRoll() ? dto.getDefaultRoll().getValue() : null;
    table.tableImage = dto.hasTableImage() ? new MD5Key(dto.getTableImage().getValue()) : null;
    table.setVisible(dto.getVisible());
    table.setAllowLookup(dto.getAllowLookup());
    table.setPickOnce(dto.getPickOnce());
    return table;
  }

  public LookupTableDto toDto() {
    var dto = LookupTableDto.newBuilder();
    dto.addAllEntries(entryList.stream().map(LookupEntry::toDto).collect(Collectors.toList()));
    if (name != null) {
      dto.setName(StringValue.of(name));
    }
    if (defaultRoll != null) {
      dto.setDefaultRoll(StringValue.of(defaultRoll));
    }
    if (tableImage != null) {
      dto.setTableImage(StringValue.of(tableImage.toString()));
    }
    dto.setVisible(visible);
    dto.setAllowLookup(allowLookup);
    dto.setPickOnce(pickOnce);
    return dto.build();
  }
}
