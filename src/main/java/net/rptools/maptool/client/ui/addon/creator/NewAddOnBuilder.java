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

public class NewAddOnBuilder {

  private String name;
  private String version;
  private String namespace;
  private String gitURL;
  private String website;
  private String license;

  private String shortDescription;
  private String description;
  private List<String> authors = new ArrayList<>();

  private boolean createEvents;
  private boolean createSlashCommands;
  private boolean createMTSProperties;
  private boolean createUDFs;

  public NewAddOnBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public NewAddOnBuilder setVersion(String version) {
    this.version = version;
    return this;
  }

  public NewAddOnBuilder setNamespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  public NewAddOnBuilder setGitURL(String gitURL) {
    this.gitURL = gitURL;
    return this;
  }

  public NewAddOnBuilder setWebsite(String website) {
    this.website = website;
    return this;
  }

  public NewAddOnBuilder setLicense(String license) {
    this.license = license;
    return this;
  }

  public NewAddOnBuilder setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
    return this;
  }

  public NewAddOnBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  public NewAddOnBuilder setAuthors(List<String> authors) {
    this.authors.clear();
    this.authors.addAll(authors);
    return this;
  }

  public NewAddOnBuilder setCreateEvents(boolean events) {
    this.createEvents = events;
    return this;
  }

  public NewAddOnBuilder setCreateSlashCommands(boolean slashCommands) {
    this.createSlashCommands = slashCommands;
    return this;
  }

  public NewAddOnBuilder setCreateMTSProperties(boolean mtsProperties) {
    this.createMTSProperties = mtsProperties;
    return this;
  }

  public NewAddOnBuilder setCreateUDFs(boolean udfs) {
    this.createUDFs = udfs;
    return this;
  }

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

  private String getReadMe() {
    return I18N.getText("library.dialog.addon.create.readeMe");
  }

  private String getLicenseText() {
    return I18N.getText("library.dialog.addon.create.licenseText");
  }
}
