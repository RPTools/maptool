package net.rptools.maptool.model.notebook.entry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;

public final class DirectoryEntry extends AbstractNoteBookEntry implements NoteBookEntry {

  private final Set<NoteBookEntry> children = new HashSet<>();

  public DirectoryEntry(String name) {
    this(null, name);
  }

  public DirectoryEntry(UUID id, String name) {
    super(null, name, null, EntryZoneRequirements.ZONE_IGNORED);
  }

  @Override
  public UUID getId() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Optional<GUID> getZoneId() {
    return Optional.empty();
  }

  @Override
  public Collection<MD5Key> getAssetKeys() {
    return Collections.emptySet();
  }

  @Override
  public NoteBookEntry setName(String name) {
    Objects.requireNonNull(name, "Name passed to setName cannot be null");
    if (name.equals(getName())) {
      return this;
    } else {
      return new DirectoryEntry(getId(), name);
    }
  }

  @Override
  public NoteBookEntry setZoneId(GUID zoneId) {
    return this; // Zone can never change as its ignored for directories.
  }

  /**
   * Returns the children in this directory.
   * @return the children in this directory.
   */
  public synchronized Set<NoteBookEntry> getChildren() {
    return new HashSet<>(children);
  }

  public synchronized void removeChild(NoteBookEntry entry) {
    children.remove(entry);
  }


  public synchronized void addChild(NoteBookEntry entry) {
    children.remove(entry);
    children.add(entry);
  }

  public synchronized boolean containsChild(NoteBookEntry entry) {
    return children.contains(entry);
  }
}
