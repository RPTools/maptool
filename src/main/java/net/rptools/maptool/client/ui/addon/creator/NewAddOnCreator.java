package net.rptools.maptool.client.ui.addon.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.util.JsonFormat;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryEventsDto;
import net.rptools.maptool.model.library.proto.AddonSlashCommandsDto;
import net.rptools.maptool.model.library.proto.AddonSlashCommandsDto.AddOnSlashCommand;
import net.rptools.maptool.model.library.proto.MTScriptPropertiesDto;

public class NewAddOnCreator {

  private static final String README_FILE = "README.md";
  private static final String LICENSE_FILE = "license.txt";



  private final NewAddOn addOn;

  private Path addOnPath;
  private Path libraryPath;
  private Path mtscriptPath;
  private Path publicPath;
  private Path mtscriptPublicPath;

  public NewAddOnCreator(NewAddOn newAddOn) {
    addOn = newAddOn;
  }


  public void create(Path path) throws IOException {
    addOnPath = path;
    libraryPath = path.resolve(AddOnLibraryImporter.LIBRARY_INFO_FILE);
    mtscriptPath = libraryPath.resolve(AddOnLibrary.MTSCRIPT_DIR);
    publicPath = libraryPath.resolve(AddOnLibrary.URL_PUBLIC_DIR);
    mtscriptPublicPath = mtscriptPath.resolve(AddOnLibrary.MTSCRIPT_PUBLIC_DIR);
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

  private void createLicenseFile() throws IOException {
    try {
      var licensePath = addOnPath.resolve(LICENSE_FILE);
      var writer = new FileWriter(licensePath.toFile());
      writer.write(addOn.licenseText());
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile",
          LICENSE_FILE), e);
    }
  }

  private void createReadmeFile() throws IOException {
    try {
      var readmePath = addOnPath.resolve(README_FILE);
      var writer = new FileWriter(readmePath.toFile());
      writer.write(addOn.readme());
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile",
          README_FILE), e);
    }
  }

  private void createMTSProperties() throws IOException {
    var builder = MTScriptPropertiesDto.newBuilder();
    var propertiesBuilderAutoExec = MTScriptPropertiesDto.Property.newBuilder();
    propertiesBuilderAutoExec.setFilename("public/auto_exec.mts");
    propertiesBuilderAutoExec.setAutoExecute(true);
    propertiesBuilderAutoExec.setDescription(I18N.getText("library.dialog.create.autoExecDesc"));

    var propertiesBuilderNoAutoExec = MTScriptPropertiesDto.Property.newBuilder();
    propertiesBuilderNoAutoExec.setFilename("public/no_auto_exec.mts");
    propertiesBuilderNoAutoExec.setAutoExecute(false);
    propertiesBuilderNoAutoExec.setDescription(I18N.getText("library.dialog.create.noAutoExecDesc"));

    builder.addProperties(propertiesBuilderAutoExec);
    builder.addProperties(propertiesBuilderNoAutoExec);

    try {
      var mtsPropertiesPath = mtscriptPath.resolve(AddOnLibraryImporter.MACROSCRIPT_PROPERTY_FILE);
      var writer = new FileWriter(mtsPropertiesPath.toFile());
      writer.write(JsonFormat.printer().includingDefaultValueFields().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile",
          AddOnLibraryImporter.MACROSCRIPT_PROPERTY_FILE), e);
    }
    // TODO: CDW Create MTS properties macros
  }

  private void createSlashCommands() throws IOException {
    var builder = AddonSlashCommandsDto.newBuilder();
    var slashCommandBuilder = AddOnSlashCommand.newBuilder();
    slashCommandBuilder.setName("exampleSlash");
    slashCommandBuilder.setDescription(I18N.getText("library.dialog.create.exampleSlashCmdDesc"));
    slashCommandBuilder.setCommand("testSlash.mts");
    builder.addSlashCommands(slashCommandBuilder);

    try {
      var slashCommandPath = mtscriptPath.resolve(AddOnLibraryImporter.SLASH_COMMAND_FILE);
      var writer = new FileWriter(slashCommandPath.toFile());
      writer.write(JsonFormat.printer().includingDefaultValueFields().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile",
          AddOnLibraryImporter.SLASH_COMMAND_FILE), e);
    }
    
    // TODO: CDW Create Slash command macro
  }

  private void createEvents() throws IOException {
    // Add init events
    var builder = AddOnLibraryEventsDto.newBuilder();
    var onFirstInitMTSBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onFirstInitMTSBuilder.setName("onFirstInit");
    onFirstInitMTSBuilder.setMts("onFirstInit.mts");
    builder.addEvents(onFirstInitMTSBuilder);
    var onInitMTSBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onInitMTSBuilder.setName("onInit");
    onInitMTSBuilder.setMts("onInit.mts");
    builder.addEvents(onInitMTSBuilder);

    // Add legacy events
    var legacyEventsBuilder = AddOnLibraryEventsDto.newBuilder();
    var onInitiativeChangeRequestBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onInitiativeChangeRequestBuilder.setName("onInitiativeChangeRequest");
    onInitiativeChangeRequestBuilder.setMts("onInitiativeChangeRequest.mts");
    var onInitiativeChangeBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onInitiativeChangeBuilder.setName("onInitiativeChange");
    onInitiativeChangeBuilder.setMts("onInitiativeChange.mts");
    legacyEventsBuilder.addLegacyEvents(onInitiativeChangeBuilder);
    var onTokenMoveBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onTokenMoveBuilder.setName("onTokenMove");
    onTokenMoveBuilder.setMts("onTokenMove.mts");
    legacyEventsBuilder.addLegacyEvents(onTokenMoveBuilder);
    var onMultipleTokensMoveBuilder = AddOnLibraryEventsDto.Events.newBuilder();
    onMultipleTokensMoveBuilder.setName("onMultipleTokensMove");
    onMultipleTokensMoveBuilder.setMts("onMultipleTokensMove.mts");
    legacyEventsBuilder.addLegacyEvents(onMultipleTokensMoveBuilder);

    // Add events to builder
    builder.addAllEvents(legacyEventsBuilder.getLegacyEventsList());
    try {
      var eventsPath = mtscriptPath.resolve(AddOnLibraryImporter.EVENT_PROPERTY_FILE);
      var writer = new FileWriter(eventsPath.toFile());
      writer.write(JsonFormat.printer().includingDefaultValueFields().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile",
          AddOnLibraryImporter.EVENT_PROPERTY_FILE), e);
    }

    // TODO: CDW Create event macros
    
  }

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
      var libraryPath = addOnPath.resolve(AddOnLibraryImporter.LIBRARY_INFO_FILE);
      var writer = new FileWriter(libraryPath.toFile());
      writer.write(JsonFormat.printer().includingDefaultValueFields().print(builder));
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile",
          AddOnLibraryImporter.LIBRARY_INFO_FILE), e);
    }

  }

  private void createAddOnDirectories() throws IOException {
    createAddOnDirectory(addOnPath);
    createAddOnDirectory(libraryPath);
    createAddOnDirectory(mtscriptPath);
    createAddOnDirectory(mtscriptPublicPath);
    createAddOnDirectory(publicPath);
  }


  private void createAddOnDirectory(Path path) throws IOException {
    if (!path.toFile().mkdirs()) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateDir", path.toString()));
    }
  }
}
