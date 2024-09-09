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
package net.rptools.maptool.client.ui.addon.creator;

import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.language.I18N;

/**
 * Builder for creating a {@link NewAddOn}.
 */
public class NewAddOnBuilder {

  /** The name of the add-on. */
  private String name;

  /** The version of the add-on. */
  private String version;

  /** The namespace of the add-on. */
  private String namespace;

  /** The git URL of the add-on. */
  private String gitURL;

  /** The website of the add-on. */
  private String website;

  /** The license of the add-on. */
  private String license;

  /** The short description of the add-on. */
  private String shortDescription;

  /** The description of the add-on. */
  private String description;

  /** The authors of the add-on. */
  private final List<String> authors = new ArrayList<>();

  /** Whether to create events. */
  private boolean createEvents;

  /** Whether to create slash commands. */
  private boolean createSlashCommands;

  /** Whether to create MTS properties. */
  private boolean createMTSProperties;

  /** Whether to create UDFs. (not yet implemented) */
  private boolean createUDFs;

  /**
   * Sets the name of the add-on.
   * @param name the name of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the version of the add-on.
   * @param version the version of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setVersion(String version) {
    this.version = version;
    return this;
  }

  /**
   * Sets the namespace of the add-on.
   * @param namespace the namespace of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setNamespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Sets the git URL of the add-on.
   * @param gitURL the git URL of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setGitURL(String gitURL) {
    this.gitURL = gitURL;
    return this;
  }

  /**
   * Sets the website of the add-on.
   * @param website the website of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setWebsite(String website) {
    this.website = website;
    return this;
  }

  /**
   * Sets the license of the add-on.
   * @param license the license of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setLicense(String license) {
    this.license = license;
    return this;
  }

  /**
   * Sets the short description of the add-on.
   * @param shortDescription the short description of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
    return this;
  }

  /**
   * Sets the description of the add-on.
   * @param description the description of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the authors of the add-on.
   * @param authors the authors of the add-on
   * @return this builder.
   */
  public NewAddOnBuilder setAuthors(List<String> authors) {
    this.authors.clear();
    this.authors.addAll(authors);
    return this;
  }

  /**
   * Sets whether to create events.
   * @param events whether to create events.
   * @return this builder.
   */
  public NewAddOnBuilder setCreateEvents(boolean events) {
    this.createEvents = events;
    return this;
  }

  /**
   * Sets whether to create slash commands.
   * @param slashCommands whether to create slash commands.
   * @return this builder.
   */
  public NewAddOnBuilder setCreateSlashCommands(boolean slashCommands) {
    this.createSlashCommands = slashCommands;
    return this;
  }

  /**
   * Sets whether to create MTS properties.
   * @param mtsProperties whether to create MTS properties.
   * @return this builder.
   */
  public NewAddOnBuilder setCreateMTSProperties(boolean mtsProperties) {
    this.createMTSProperties = mtsProperties;
    return this;
  }

  /**
   * Sets whether to create UDFs.
   * @param udfs whether to create UDFs.
   * @return this builder.
   */
  public NewAddOnBuilder setCreateUDFs(boolean udfs) {
    this.createUDFs = udfs;
    return this;
  }

  /**
   * Builds a new {@link NewAddOn} from the current state of the builder.
   * @return a new {@link NewAddOn}.
   */
  public NewAddOn build() {
    return new NewAddOn(
        name,
        version,
        namespace,
        gitURL,
        website,
        license,
        shortDescription,
        description,
        authors,
        createEvents,
        createSlashCommands,
        createMTSProperties,
        createUDFs,
        getReadMe(),
        getLicenseText());
  }

  /**
   * Gets the default README text.
   * @return the default README text.
   */
  private String getReadMe() {
    return I18N.getText("library.dialog.addon.create.readeMe");
  }

  /**
   * Gets the default license text.
   * @return the default license text.
   */
  private String getLicenseText() {
    return I18N.getText("library.dialog.addon.create.licenseText");
  }
}
