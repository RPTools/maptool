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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * An association between footprint IDs ({@link GUID}) and {@link TokenFootprint}.
 *
 * <p>Also keeps a reference to a footprint as the <em>default</em> footprint.
 */
public class TokenFootprints implements Iterable<Map.Entry<GUID, TokenFootprint>> {
  private static final Logger log = LogManager.getLogger(TokenFootprints.class);

  private final LinkedHashMap<GUID, TokenFootprint> footprints;
  private final CaseInsensitiveMap<String, GUID> idsByName;
  private final GUID defaultFootprintId;
  private final TokenFootprint defaultFootprint;

  /**
   * Initializes a new set of footprints
   *
   * @param defaultFootprintId Identifies the default footprint.
   * @param footprints All pairs of IDs and footprints to include.
   */
  public TokenFootprints(
      GUID defaultFootprintId, List<Map.Entry<GUID, TokenFootprint>> footprints) {
    this.footprints = new LinkedHashMap<>();
    this.idsByName = new CaseInsensitiveMap<>();
    for (var entry : footprints) {
      GUID id = entry.getKey();
      TokenFootprint footprint = entry.getValue();

      var existing = this.footprints.put(id, footprint);
      if (existing != null) {
        log.error("Found duplicate footprint ID: {}", id);
      }

      var existingId = this.idsByName.put(footprint.getName(), id);
      if (existingId != null) {
        log.error("Found duplicate footprint name: {}", footprint.getName());
      }
    }

    // Validate the default ID.
    var defaultFootprint = this.footprints.get(defaultFootprintId);
    if (defaultFootprint == null) {
      log.error("Invalid default footprint: ID {} not found", defaultFootprintId);
      // Default to the first one.
      var first = footprints.getFirst();
      defaultFootprintId = first.getKey();
      defaultFootprint = first.getValue();
    }

    this.defaultFootprintId = defaultFootprintId;
    this.defaultFootprint = defaultFootprint;
  }

  /**
   * @return The ID of the default footprint.
   */
  public GUID getDefaultId() {
    return defaultFootprintId;
  }

  /**
   * @return The default footprint.
   */
  public TokenFootprint getDefault() {
    return defaultFootprint;
  }

  /**
   * Looks up a footprint from an id.
   *
   * <p>If no matching footprint is found, the default footprint is returned.
   *
   * @param footprintId The ID of the footprint to find.
   * @return The footprint matching {@code footprintId}, or the default footprint if not found.
   */
  public TokenFootprint get(GUID footprintId) {
    return footprints.getOrDefault(footprintId, defaultFootprint);
  }

  /**
   * Case-insensitive name lookup.
   *
   * @param name The footprint name to look up.
   * @return The ID and {@link TokenFootprint} with name {@code name}, or {@link Optional#empty()}
   *     if there is no match.
   */
  public Optional<Map.Entry<GUID, TokenFootprint>> getEntryByName(String name) {
    var id = idsByName.get(name);
    if (id == null) {
      return Optional.empty();
    }

    return Optional.of(Map.entry(id, footprints.get(id)));
  }

  /**
   * @return A read-only iterator over all pairs of IDs and footprints.
   */
  @Override
  public @NotNull Iterator<Map.Entry<GUID, TokenFootprint>> iterator() {
    return Collections.unmodifiableMap(footprints).entrySet().iterator();
  }
}
