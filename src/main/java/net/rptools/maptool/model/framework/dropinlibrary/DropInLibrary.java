package net.rptools.maptool.model.framework.dropinlibrary;

import java.util.Objects;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;

public class DropInLibrary {

  private final String name;
  private final String version;
  private final String website;
  private final String authors;
  private final String gitUrl;
  private final String license;


  private DropInLibrary(DropInLibraryDto dto) {
    Objects.requireNonNull(dto, I18N.getText("library.error.invalidDefinition"));
    name = Objects.requireNonNull(dto.getName(), I18N.getText("library.error.emptyName"));
    version = Objects.requireNonNull(dto.getVersion(), I18N.getText("library.error.emptyVersion",
     name));
    website = Objects.requireNonNullElse(dto.getWebsite(), "");
    authors = Objects.requireNonNullElse(dto.getAuthors(),  "");
    gitUrl = dto.getGitUrl();
    license = dto.getLicense();
  }


  public static DropInLibrary fromDto(DropInLibraryDto dto) {
    return new DropInLibrary(dto);
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getWebsite() {
    return website;
  }

  public String getAuthors() {
    return authors;
  }

  public String getGitUrl() {
    return gitUrl;
  }

  public String getLicense() {
    return license;
  }


}
