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

    Path contentPath = directory.resolve(AddOnLibraryImporter.CONTENT_DIRECTORY);
    Path libraryJsonPath = directory.resolve(AddOnLibraryImporter.LIBRARY_INFO_FILE);
    Path mtsPropertyPath = directory.resolve(AddOnLibraryImporter.MACROSCRIPT_PROPERTY_FILE);
    Path eventPath = directory.resolve(AddOnLibraryImporter.EVENT_PROPERTY_FILE);
    Path mtsPublicPath = contentPath.resolve("mtscript").resolve("public");
    Path publicPath = contentPath.resolve("public");
    Path propertiesPath = directory.resolve("property");
    Path macroDetails = directory.resolve("macro_script_map.txt");
    Path propertyDetails = propertiesPath.resolve("prop_file_map.txt");

    mtsPublicPath.toFile().mkdirs();
    publicPath.toFile().mkdirs();

    var macroScriptNameMap = new HashMap<String, String>();
    var macroDataMap = new HashMap<String, String>();
    var macroAutoExecMap = new HashMap<String, Boolean>();
    var eventNameMap = new HashMap<String, String>();
    var legacyEventNameMap = new HashMap<String, String>();
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
                  eventNameMap.put(macroName, macroName);
                }
                default -> {
                  String fileName = "macro_" + (fileCounter++) + ".mts";
                  macroScriptNameMap.put(macroName, fileName);
                }
              }
              macroAutoExecMap.put(macroName, value.getAutoExecute());
              macroDataMap.put(macroName, value.getCommand());
            });

    var propertyFilenameMap = new HashMap<String, String>();
    var propertyDataMap = new HashMap<String, String>();
    token
        .getPropertyNamesRaw()
        .forEach(
            propName -> {
              String fileName = "prop_" + (fileCounter++) + ".txt";
              propertyFilenameMap.put(propName, fileName);
              propertyDataMap.put(
                  propName, Objects.requireNonNullElse(token.getProperty(propName), "").toString());
            });

    var eventsBuilder = AddOnLibraryEventsDto.newBuilder();
    legacyEventNameMap.forEach(
        (key, value) -> {
          var eventBuilder = AddOnLibraryEventsDto.Events.newBuilder();
          eventBuilder.setName(key);
          eventBuilder.setMts(key);
          eventsBuilder.addLegacyEvents(eventBuilder);
        });

    eventNameMap.forEach(
        (key, value) -> {
          var eventBuilder = AddOnLibraryEventsDto.Events.newBuilder();
          eventBuilder.setName(key);
          eventBuilder.setMts(key);
          eventsBuilder.addEvents(eventBuilder);
        });

    var mtsScriptPropBuilder = MTScriptPropertiesDto.newBuilder();
    macroScriptNameMap.forEach(
        (key, value) -> {
          var propertyBuilder = MTScriptPropertiesDto.Property.newBuilder();
          propertyBuilder.setFilename("public/" + value);
          propertyBuilder.setAutoExecute(macroAutoExecMap.get(key));
          mtsScriptPropBuilder.addProperties(propertyBuilder);
        });

    try (var writer = new BufferedWriter(new FileWriter(libraryJsonPath.toFile()))) {
      writer.write(
          JsonFormat.printer().includingDefaultValueFields().print(libraryBuilder.build()));
    } catch (IOException e) {
      MapTool.showError(I18N.getText("library.export.errorWriting", libraryJsonPath), e);
      return;
    }

    try (var writer = new BufferedWriter(new FileWriter(mtsPropertyPath.toFile()))) {
      writer.write(JsonFormat.printer().print(mtsScriptPropBuilder.build()));
    } catch (IOException e) {
      MapTool.showError(I18N.getText("library.export.errorWriting", mtsPropertyPath), e);
      return;
    }

    try (var writer = new BufferedWriter(new FileWriter(eventPath.toFile()))) {
      writer.write(JsonFormat.printer().print(eventsBuilder.build()));
    } catch (IOException e) {
      MapTool.showError(I18N.getText("library.export.errorWriting", eventPath), e);
      return;
    }

    mtsPublicPath.toFile().mkdirs();
    macroDataMap.forEach(
        (key, value) -> {
          String filename = macroScriptNameMap.get(key);
          try (var writer =
              new BufferedWriter(new FileWriter(mtsPublicPath.resolve(filename).toFile()))) {
            writer.write(value);
          } catch (IOException e) {
            MapTool.showError(I18N.getText("library.export.errorWriting", filename), e);
            return;
          }
        });

    propertiesPath.toFile().mkdirs();
    propertyDataMap.forEach(
        (key, value) -> {
          String filename = propertyFilenameMap.get(key);
          try (var writer =
              new BufferedWriter(new FileWriter(propertiesPath.resolve(filename).toFile()))) {
            writer.write(value);
          } catch (IOException e) {
            MapTool.showError(I18N.getText("library.export.errorWriting", filename), e);
            return;
          }
        });

    try (var writer = new BufferedWriter(new FileWriter(macroDetails.toFile()))) {
      for (var entry : macroScriptNameMap.entrySet()) {
        writer.write(entry.getValue() + " => " + entry.getKey() + "\n");
      }
    } catch (IOException e) {
      MapTool.showError(I18N.getText("library.export.errorWriting", macroDetails), e);
      return;
    }

    try (var writer = new BufferedWriter(new FileWriter(propertyDetails.toFile()))) {
      for (var entry : propertyFilenameMap.entrySet()) {
        writer.write(entry.getValue() + " => " + entry.getKey() + "\n");
      }
    } catch (IOException e) {
      MapTool.showError(I18N.getText("library.export.errorWriting", propertyDetails), e);
      return;
    }

    MapTool.addLocalMessage(I18N.getText("library.export.done", libraryToken.getName(), directory));
  }
}
