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

import com.google.protobuf.util.JsonFormat;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryEventsDto;
import net.rptools.maptool.model.library.proto.AddonSlashCommandsDto;
import net.rptools.maptool.model.library.proto.AddonSlashCommandsDto.AddOnSlashCommand;
import net.rptools.maptool.model.library.proto.MTScriptPropertiesDto;

/** Creates a new add-on directory structure and files. */
public class NewAddOnCreator {

  /** The name of the README file. */
  private static final String README_FILE = "README.md";

  /** The name of the LICENSE file. */
  private static final String LICENSE_FILE = "license.txt";

  /** The add-on to create. */
  private final NewAddOn addOn;

  /** The path to the add-on directory. */
  private final Path addOnPath;

  /** The path to the library directory. */
  private final Path libraryPath;

  /** The path to the mtscript directory. */
  private final Path mtscriptPath;

  /** The path to the public directory. */
  private final Path publicPath;

  /** The path to the mtscript public directory. */
  private final Path mtscriptPublicPath;

  private static final String AUTO_EXEC_EXAMPLE_MACRO = "public/auto_exec.mts";
  private static final String NON_AUTO_EXEC_EXAMPLE_MACRO = "public/non_auto_exec.mts";
  private static final String SLASH_COMMAND_EXAMPLE_MACRO = "testSlash.mts";
  private static final String ON_FIRST_INIT_EXAMPLE_MACRO = "onFirstInit.mts";
  private static final String ON_INIT_EXAMPLE_MACRO = "onInit.mts";
  private static final String ON_INITIATIVE_CHANGE_REQUEST_EXAMPLE_MACRO =
      "onInitiativeChangeRequest.mts";
  private static final String ON_INITIATIVE_CHANGE_EXAMPLE_MACRO = "onInitiativeChange.mts";
  private static final String ON_TOKEN_MOVE_EXAMPLE_MACRO = "onTokenMove.mts";
  private static final String ON_MULTIPLE_TOKENS_MOVE_EXAMPLE_MACRO = "onMultipleTokensMove.mts";

  /**
   * Creates a new add-on creator.
   *
   * @param newAddOn the add-on to create.
   * @param path the path to the add-on directory which will be created.
   */
  public NewAddOnCreator(NewAddOn newAddOn, Path path) {
    addOn = newAddOn;
    addOnPath = path;
    libraryPath = path.resolve(AddOnLibraryImporter.CONTENT_DIRECTORY);
    mtscriptPath = libraryPath.resolve(AddOnLibrary.MTSCRIPT_DIR);
    publicPath = libraryPath.resolve(AddOnLibrary.URL_PUBLIC_DIR);
    mtscriptPublicPath = mtscriptPath.resolve(AddOnLibrary.MTSCRIPT_PUBLIC_DIR);
  }

  /**
   * Creates the add-on directory structure and files.
   *
   * @throws IOException if an error occurs creating the directory or files.
   */
  public void create() throws IOException {
    createAddOnDirectories();
    createLibraryFile();

    if (addOn.createEvents()) {
      createEvents();
    }

    if (addOn.createSlashCommands()) {
      createSlashCommands();
    }
    if (addOn.createMTSProperties()) {
      createMTSProperties();
    }
    createReadmeFile();
    createLicenseFile();
  }

  /**
   * Creates the README file.
   *
   * @throws IOException if an error occurs creating the file.
   */
  private void createLicenseFile() throws IOException {
    try {
      var licensePath = addOnPath.resolve(LICENSE_FILE);
      var writer = new FileWriter(licensePath.toFile());
      writer.write(addOn.licenseText());
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile", LICENSE_FILE), e);
    }
  }

  /**
   * Creates the README file.
   *
   * @throws IOException if an error occurs creating the file.
   */
  private void createReadmeFile() throws IOException {
    try {
      var readmePath = addOnPath.resolve(README_FILE);
      var writer = new FileWriter(readmePath.toFile());
      writer.write(addOn.readme());
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile", README_FILE), e);
    }
  }

  /**
   * Creates the MTS properties file.
   *
   * @throws IOException if an error occurs creating the file.
   */
  private void createMTSProperties() throws IOException {
    var builder = MTScriptPropertiesDto.newBuilder();
    var propertiesBuilderAutoExec = MTScriptPropertiesDto.Property.newBuilder();
    propertiesBuilderAutoExec.setFilename(AUTO_EXEC_EXAMPLE_MACRO);
    propertiesBuilderAutoExec.setAutoExecute(true);
    propertiesBuilderAutoExec.setDescription(I18N.getText("library.dialog.create.autoExecDesc"));

    var propertiesBuilderNoAutoExec = MTScriptPropertiesDto.Property.newBuilder();
    propertiesBuilderNoAutoExec.setFilename(NON_AUTO_EXEC_EXAMPLE_MACRO);
    propertiesBuilderNoAutoExec.setAutoExecute(false);
    propertiesBuilderNoAutoExec.setDescription(
        I18N.getText("library.dialog.create.noAutoExecDesc"));

    builder.addProperties(propertiesBuilderAutoExec);
    builder.addProperties(propertiesBuilderNoAutoExec);

    try {
      var mtsPropertiesPath = addOnPath.resolve(AddOnLibraryImporter.MACROSCRIPT_PROPERTY_FILE);
      var writer = new FileWriter(mtsPropertiesPath.toFile());
      writer.write(JsonFormat.printer().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(
          I18N.getText(
              "library.dialog.failedToCreateFile", AddOnLibraryImporter.MACROSCRIPT_PROPERTY_FILE),
          e);
    }
    writeMacro(mtscriptPath.resolve(AUTO_EXEC_EXAMPLE_MACRO));
    writeMacro(mtscriptPath.resolve(NON_AUTO_EXEC_EXAMPLE_MACRO));
  }

  /**
   * Creates the slash commands file.
   *
   * @throws IOException if an error occurs creating the file.
   */
  private void createSlashCommands() throws IOException {
    var builder = AddonSlashCommandsDto.newBuilder();
    var slashCommandBuilder = AddOnSlashCommand.newBuilder();
    slashCommandBuilder.setName("exampleSlash");
    slashCommandBuilder.setDescription(I18N.getText("library.dialog.create.exampleSlashCmdDesc"));
    slashCommandBuilder.setCommand(SLASH_COMMAND_EXAMPLE_MACRO);
    builder.addSlashCommands(slashCommandBuilder);

    try {
      var slashCommandPath = addOnPath.resolve(AddOnLibraryImporter.SLASH_COMMAND_FILE);
      var writer = new FileWriter(slashCommandPath.toFile());
      writer.write(JsonFormat.printer().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(
          I18N.getText(
              "library.dialog.failedToCreateFile", AddOnLibraryImporter.SLASH_COMMAND_FILE),
          e);
    }

    writeMacro(mtscriptPath.resolve(SLASH_COMMAND_EXAMPLE_MACRO));
  }

  /**
   * Creates the events file.
   *
   * @throws IOException if an error occurs creating the file.
   */
  private void createEvents() throws IOException {
    // Add init events
    var builder = AddOnLibraryEventsDto.newBuilder();
    var onFirstInitMTSBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onFirstInitMTSBuilder.setName("onFirstInit");
    onFirstInitMTSBuilder.setMts(ON_FIRST_INIT_EXAMPLE_MACRO);
    builder.addEvents(onFirstInitMTSBuilder);
    var onInitMTSBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onInitMTSBuilder.setName("onInit");
    onInitMTSBuilder.setMts(ON_INIT_EXAMPLE_MACRO);
    builder.addEvents(onInitMTSBuilder);

    // Add legacy events
    var legacyEventsBuilder = AddOnLibraryEventsDto.newBuilder();
    var onInitiativeChangeRequestBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onInitiativeChangeRequestBuilder.setName("onInitiativeChangeRequest");
    onInitiativeChangeRequestBuilder.setMts(ON_INITIATIVE_CHANGE_REQUEST_EXAMPLE_MACRO);
    var onInitiativeChangeBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onInitiativeChangeBuilder.setName("onInitiativeChange");
    onInitiativeChangeBuilder.setMts(ON_INITIATIVE_CHANGE_EXAMPLE_MACRO);
    legacyEventsBuilder.addLegacyEvents(onInitiativeChangeBuilder);
    var onTokenMoveBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onTokenMoveBuilder.setName("onTokenMove");
    onTokenMoveBuilder.setMts(ON_TOKEN_MOVE_EXAMPLE_MACRO);
    legacyEventsBuilder.addLegacyEvents(onTokenMoveBuilder);
    var onMultipleTokensMoveBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onMultipleTokensMoveBuilder.setName("onMultipleTokensMove");
    onMultipleTokensMoveBuilder.setMts(ON_MULTIPLE_TOKENS_MOVE_EXAMPLE_MACRO);
    legacyEventsBuilder.addLegacyEvents(onMultipleTokensMoveBuilder);

    // Add events to builder
    builder.addAllEvents(legacyEventsBuilder.getLegacyEventsList());
    try {
      var eventsPath = addOnPath.resolve(AddOnLibraryImporter.EVENT_PROPERTY_FILE);
      var writer = new FileWriter(eventsPath.toFile());
      writer.write(JsonFormat.printer().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(
          I18N.getText(
              "library.dialog.failedToCreateFile", AddOnLibraryImporter.EVENT_PROPERTY_FILE),
          e);
    }

    writeMacro(mtscriptPath.resolve(ON_FIRST_INIT_EXAMPLE_MACRO));
    writeMacro(mtscriptPath.resolve(ON_INIT_EXAMPLE_MACRO));
    writeMacro(mtscriptPath.resolve(ON_INITIATIVE_CHANGE_REQUEST_EXAMPLE_MACRO));
    writeMacro(mtscriptPath.resolve(ON_INITIATIVE_CHANGE_EXAMPLE_MACRO));
    writeMacro(mtscriptPath.resolve(ON_TOKEN_MOVE_EXAMPLE_MACRO));
    writeMacro(mtscriptPath.resolve(ON_MULTIPLE_TOKENS_MOVE_EXAMPLE_MACRO));
  }

  /**
   * Creates the library file.
   *
   * @throws IOException if an error occurs creating the file.
   */
  private void createLibraryFile() throws IOException {
    var builder = AddOnLibraryDto.newBuilder();
    builder.setName(addOn.name());
    builder.setVersion(addOn.version());
    builder.setNamespace(addOn.namespace());
    builder.addAllAuthors(addOn.authors());
    if (addOn.gitURL() != null && !addOn.gitURL().isEmpty()) {
      builder.setGitUrl(addOn.gitURL());
    }
    if (addOn.website() != null && !addOn.website().isEmpty()) {
      builder.setWebsite(addOn.website());
    }
    if (addOn.license() != null && !addOn.license().isEmpty()) {
      builder.setLicense(addOn.license());
    }
    builder.setShortDescription(addOn.shortDescription());
    builder.setDescription(addOn.description());
    builder.setLicenseFile(LICENSE_FILE);
    builder.setReadMeFile(README_FILE);
    try {
      var libraryInfoPath = addOnPath.resolve(AddOnLibraryImporter.LIBRARY_INFO_FILE);
      var writer = new FileWriter(libraryInfoPath.toFile());
      writer.write(JsonFormat.printer().includingDefaultValueFields().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(
          I18N.getText("library.dialog.failedToCreateFile", AddOnLibraryImporter.LIBRARY_INFO_FILE),
          e);
    }
  }

  /**
   * Creates the add-on directories.
   *
   * @throws IOException if an error occurs creating the directories.
   */
  private void createAddOnDirectories() throws IOException {
    createAddOnDirectory(addOnPath);
    createAddOnDirectory(libraryPath);
    createAddOnDirectory(mtscriptPath);
    createAddOnDirectory(mtscriptPublicPath);
    createAddOnDirectory(publicPath);
  }

  /**
   * Creates a new directory.
   *
   * @param path the path to the directory to create.
   * @throws IOException if an error occurs creating the directory..
   */
  private void createAddOnDirectory(Path path) throws IOException {
    if (!path.toFile().mkdirs()) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateDir", path.toString()));
    }
  }

  /**
   * Write an example macro to the given path.
   *
   * @param path the path to write the macro to (including filename)
   */
  private void writeMacro(Path path) throws FileNotFoundException {
    var filename = path.getFileName().toString();
    var comment = I18N.getText("library.dialog.addon.create.mtsComment", filename);
    try (var writer = new PrintWriter(path.toFile())) {
      writer.println("<!-- " + comment + " -->");
      writer.println("[h: broadcast('" + filename + "')] ");
    }
  }
}
