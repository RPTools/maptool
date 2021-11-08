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
package net.rptools.maptool.model.library.token;

import com.google.protobuf.util.JsonFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryEventsDto;
import net.rptools.maptool.model.library.proto.MTScriptPropertiesDto;

public class LibTokenConverter {

  private final Path directory;
  private final Token token;

  private final LibraryToken libraryToken;

  private int fileCounter = 0;

  public LibTokenConverter(File directory, Token token) {
    this.directory = directory.toPath();
    this.token = token;
    this.libraryToken = new LibraryToken(token);
  }

  public void convert() {
    var libraryBuilder = AddOnLibraryDto.newBuilder();
    libraryBuilder.setName(libraryToken.getName().join());
    libraryBuilder.setVersion(libraryToken.getVersion().join());
    libraryBuilder.setWebsite(
        Objects.requireNonNullElse(libraryToken.getWebsite().join(), "unknown"));
    libraryBuilder.setGitUrl(libraryToken.getGitUrl().join());
    libraryBuilder.addAllAuthors(Arrays.asList(libraryToken.getAuthors().join()));
    libraryBuilder.setLicense(libraryToken.getLicense().join());
    libraryBuilder.setDescription(libraryToken.getDescription().join());
    libraryBuilder.setShortDescription(libraryToken.getShortDescription().join());
    libraryBuilder.setNamespace(libraryToken.getNamespace().join());
    libraryBuilder.setAllowsUriAccess(token.getAllowURIAccess());

    var mtsScriptPropBuilder = MTScriptPropertiesDto.newBuilder();

    var eventsBuilder = AddOnLibraryEventsDto.newBuilder();

    Path contentPath = directory.resolve(AddOnLibraryImporter.CONTENT_DIRECTORY);
    Path mtsPropertyPath = directory.resolve(AddOnLibraryImporter.MACROSCRIPT_PROPERTY_FILE);
    Path eventPath = directory.resolve(AddOnLibraryImporter.EVENT_PROPERTY_FILE);
    Path mtsPublicPath = contentPath.resolve("mtscript").resolve("public");
    Path publicPath = contentPath.resolve("public");
    Path propertiesPath = directory.resolve("exported-properties");

    mtsPublicPath.toFile().mkdirs();
    publicPath.toFile().mkdirs();

    var macroScriptNameMap = new HashMap<String, String>();
    var macroDataMap = new HashMap<String, String>();
    var macroAutoExecMap = new HashMap<String, Boolean>();
    var eventMap = new HashMap<String, String>();
    var legacyEventNameMap = new HashMap<String, String>();
    var propertyFilenameMap = new HashMap<String, String>();
    var propertyDataMap = new HashMap<String, String>();
    token
        .getMacroPropertiesMap(false)
        .forEach(
            (key, value) -> {
              var macroName = value.getLabel();
              switch (macroName) {
                case "onInitiativeChangeRequest",
                    "onInitiativeChange",
                    "onTokenMove",
                    "onMultipleTokensMove" -> {
                  macroScriptNameMap.put(macroName, macroName + ".mts");
                  legacyEventNameMap.put(macroName, macroName);
                }
                case "onCampaignLoad" -> {
                  macroScriptNameMap.put(macroName, macroName + ".mts");
                  eventMap.put(macroName, macroName);
                }
                default -> {
                  String fileName = "macro_" + (fileCounter++) + ".mts";
                  macroScriptNameMap.put(macroName, fileName);
                }
              }
              macroAutoExecMap.put(macroName, value.getAutoExecute());
              macroDataMap.put(macroName, value.getCommand());
            });

    token
        .getPropertyNamesRaw()
        .forEach(
            propName -> {
              String fileName = "prop_" + (fileCounter++) + ".txt";
              propertyFilenameMap.put(propName, fileName);
              // String
            });

    String libraryJsonFile = AddOnLibraryImporter.LIBRARY_INFO_FILE;
    try (var writer =
        new BufferedWriter(new FileWriter(directory.resolve(libraryJsonFile).toFile()))) {
      writer.write(
          JsonFormat.printer().includingDefaultValueFields().print(libraryBuilder.build()));
    } catch (IOException e) {
      MapTool.showError(I18N.getText("library.export.errorWriting", libraryJsonFile), e);
      return;
    }

    MapTool.addLocalMessage(I18N.getText("library.export.done", libraryToken.getName(), directory));
  }
}
