package net.rptools.maptool.client.ui.addon.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import net.rptools.maptool.language.I18N;

public class NewAddOnCreator {

  private static final String LIBRARY_FILE = "library.json";
  private static final String EVENTS_FILE = "events.json";
  private static final String SLASH_COMMAND_FILE = "slash_commands.json";
  private static final String MTS_PROPERTIES_FILE = "mts_properties.json";
  private static final String UDF_FILE = "functions.json";
  private static final String README_FILE = "README.md";
  private static final String LICENSE_FILE = "license.txt";

  private static final String LIBRARY_DIRECTORY = "library";
  private static final String MTSCRIPT_DIRECTORY = "mtscript";
  private static final String PUBLIC_DIRECTORY = "public";


  private final NewAddOn addOn;

  public NewAddOnCreator(NewAddOn newAddOn) {
    addOn = newAddOn;
  }


  public void create(Path path) throws IOException {
    createAddOnDirectories(path);
    createLibraryFile(path);
    if (addOn.createEvents()) {
      createEvents(path);
    }
    if (addOn.createSlashCommands()) {
      createSlashCommands(path);
    }
    if (addOn.createMTSProperties()) {
      createMTSProperties(path);
    }
    if (addOn.createUDFs()) {
      createUDFs(path);
    }
    createReadmeFile(path);
    createLicenseFile(path);
  }

  private void createEvents(Path path) {
    
  }

  private void createLibraryFile(Path path) throws IOException {
    var libraryJson = new JsonObject();
    libraryJson.addProperty("name", addOn.name());
    libraryJson.addProperty("version", addOn.version());
    libraryJson.addProperty("namespace", addOn.namespace());
    if (addOn.gitURL() != null && !addOn.gitURL().isEmpty()) {
      libraryJson.addProperty("gitURL", addOn.gitURL());
    }
    if (addOn.website() != null && !addOn.website().isEmpty()) {
      libraryJson.addProperty("website", addOn.website());
    }
    var jsonAuthors = new JsonArray();
    addOn.authors().forEach(jsonAuthors::add);
    libraryJson.add("authors", jsonAuthors);
    if (addOn.license() != null && !addOn.license().isEmpty()) {
      libraryJson.addProperty("license", addOn.license());
    }
    libraryJson.addProperty("shortDescription", addOn.shortDescription());
    libraryJson.addProperty("description", addOn.description());
    try {
      var libraryPath = path.resolve(LIBRARY_FILE);
      var writer = new FileWriter(libraryPath.toFile());
      writer.write(libraryJson.toString());
      writer.close();
    } catch (IOException e) {
      throw new IOException(I18N.getText("library.dialog.failedToCreateFile", LIBRARY_FILE), e);
    }

  }

  private void createAddOnDirectories(Path path) throws IOException {
    Path libraryPath = path.resolve(LIBRARY_DIRECTORY);
    Path mtscriptPath = libraryPath.resolve(MTSCRIPT_DIRECTORY);
    Path publicPath = libraryPath.resolve(PUBLIC_DIRECTORY);
    Path mtscriptPublicPath = mtscriptPath.resolve(PUBLIC_DIRECTORY);

    createAddOnDirectory(path);
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
