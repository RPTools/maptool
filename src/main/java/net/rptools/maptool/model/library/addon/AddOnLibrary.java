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
package net.rptools.maptool.model.library.addon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.macro.MacroManager.MacroDetails;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryNotValidException;
import net.rptools.maptool.model.library.LibraryNotValidException.Reason;
import net.rptools.maptool.model.library.MTScriptMacroInfo;
import net.rptools.maptool.model.library.data.LibraryData;
import net.rptools.maptool.model.library.proto.*;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryEventsDto;
import net.rptools.maptool.model.library.proto.MTScriptPropertiesDto;
import net.rptools.maptool.model.sheet.stats.StatSheet;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

/** Class that implements add-on libraries. */
public class AddOnLibrary implements Library {

  /** The name of the event for first time initialization. */
  private static final String FIRST_INIT_EVENT = "onFirstInit";

  /** The name of the event for initialization. */
  private static final String INIT_EVENT = "onInit";

  /** The prefix for the name of the JavaScript context for this addon. */
  private static final String JS_CONTEXT_PREFIX = "addon:";

  /** Record used to store information about the MacrScript functions for this library. */
  private record MTScript(String path, boolean autoExecute, String description, MD5Key md5Key) {}

  /** The directory where the files exposed URI are stored. */
  private static final String URL_PUBLIC_DIR = "public/";

  /** The directory where MT MacroScripts are stored. */
  private static final String MTSCRIPT_DIR = "mtscript/";

  /** The directory where public MT MacroScripts are stored. */
  private static final String MTSCRIPT_PUBLIC_DIR = "public/";

  /** Logger instance for this class. */
  private static final Logger logger = LogManager.getLogger(AddOnLibrary.class);

  /** The Asset for the library read me file. */
  private final String readMeFile;

  /** The Asset for the library license file. */
  private final String licenseFile;

  /** The name of the add-on library. */
  private final String name;

  /** The version of the add-on library. */
  private final String version;

  /** The website for the add-on library. */
  private final String website;

  /** The authors for the add-on library. */
  private final String[] authors;

  /** The git url of the add-on library. */
  private final String gitUrl;

  /** The license for the add-on library. */
  private final String license;

  /** The namespace for the add-on library. */
  private final String namespace;

  /** The description for the add-on library. */
  private final String description;

  /** The short description for the add-on library. */
  private final String shortDescription;

  /** if the add-on library allows URI access or not. */
  private final boolean allowsUriAccess;

  /** The mapping between paths and asset information. */
  private final Map<String, Pair<MD5Key, Type>> pathAssetMap;

  /** The mapping between url paths and asset information. */
  private final Map<String, Pair<MD5Key, Type>> urlPathAssetMap;

  /** The mapping between MTScript function paths and asset information. */
  private final Map<String, MTScript> mtsFunctionAssetMap;

  /** The mapping between MTScript function paths and legacy events. */
  private final Map<String, String> legacyEventNameMap = new HashMap<>();

  /** The mapping between MTScript function paths and non legacy events. */
  private final Map<String, String> mtScriptEventNameMap = new HashMap<>();

  /** The mapping between JavaScript script paths and non legacy events. */
  private final Map<String, String> jsEventNameMap = new HashMap<>();

  /** The ID of the asset for the whole of the add-on Library. */
  private final MD5Key assetKey;

  /** The name of the JavaScript context for the add on library. */
  private final String jsContextName;

  /** The Stat Sheets defined by the add-on library. */
  private final Set<StatSheet> statSheets = new HashSet<>();

  /** The mapping between slash command names and slash command details. */
  private final Map<String, MacroDetails> slashCommands = new HashMap<>();

  /** The information about the add-on library. */
  private final LibraryInfo libraryInfo;

  /**
   * Class used to represent Drop In Libraries.
   *
   * @param dto The Drop In Libraries Data Transfer Object.
   * @param mtsDto The MTScript Properties Data Transfer Object.
   * @param eventsDto The MTScript Events Data Transfer Object.
   * @param slashCommandsDto The Slash Commands Data Transfer Object.
   * @param pathAssetMap mapping of paths in the library to {@link MD5Key}s and {@link Type}s.
   */
  private AddOnLibrary(
      MD5Key libraryAssetKey,
      AddOnLibraryDto dto,
      MTScriptPropertiesDto mtsDto,
      AddOnLibraryEventsDto eventsDto,
      AddOnStatSheetsDto statSheetsDto,
      AddonSlashCommandsDto slashCommandsDto,
      Map<String, Pair<MD5Key, Type>> pathAssetMap) {
    Objects.requireNonNull(dto, I18N.getText("library.error.invalidDefinition"));
    name = Objects.requireNonNull(dto.getName(), I18N.getText("library.error.emptyName"));
    version =
        Objects.requireNonNull(dto.getVersion(), I18N.getText("library.error.emptyVersion", name));
    website = Objects.requireNonNullElse(dto.getWebsite(), "");
    authors = dto.getAuthorsList().toArray(String[]::new);
    gitUrl = dto.getGitUrl();
    license = dto.getLicense();
    namespace = dto.getNamespace();
    description = dto.getDescription();
    shortDescription = dto.getShortDescription();
    this.pathAssetMap = Map.copyOf(pathAssetMap);
    allowsUriAccess = dto.getAllowsUriAccess();
    assetKey = libraryAssetKey;

    var urlsMap = new HashMap<String, Pair<MD5Key, Type>>();
    var mtsMap = new HashMap<String, MTScript>();

    var autoExecSet = new HashSet<String>();
    var descriptionMap = new HashMap<String, String>();

    for (var properties : mtsDto.getPropertiesList()) {
      var path = MTSCRIPT_DIR + properties.getFilename();
      if (properties.getAutoExecute()) {
        autoExecSet.add(path);
      }

      descriptionMap.put(path, properties.getDescription());
    }

    for (var s : statSheetsDto.getStatSheetsList()) {
      try {
        statSheets.add(
            new StatSheet(
                s.getName(),
                s.getDescription(),
                new URI("lib://" + namespace + "/" + s.getEntry()).toURL(),
                new HashSet<>(s.getPropertyTypesList()),
                namespace));
      } catch (Exception e) {
        MapTool.showError(I18N.getText("library.error.addOn.sheet", namespace, s.getName()), e);
      }
    }

    eventsDto.getEventsList().stream()
        .filter(e -> !e.getMts().isEmpty())
        .forEach(e -> mtScriptEventNameMap.put(e.getName(), e.getMts()));

    eventsDto.getEventsList().stream()
        .filter(e -> !e.getJs().isEmpty())
        .forEach(e -> jsEventNameMap.put(e.getName(), e.getJs()));

    eventsDto.getLegacyEventsList().stream()
        .filter(e -> !e.getMts().isEmpty())
        .forEach(e -> legacyEventNameMap.put(e.getName(), e.getMts()));

    for (var entry : this.pathAssetMap.entrySet()) {
      String path = entry.getKey();
      if (path.startsWith(URL_PUBLIC_DIR)) {
        urlsMap.put(path.substring(URL_PUBLIC_DIR.length()), entry.getValue());
      } else if (path.startsWith(MTSCRIPT_DIR)) {
        if (path.toLowerCase().endsWith(".mts")) {
          String name = path.substring(MTSCRIPT_DIR.length(), path.length() - 4);
          mtsMap.put(
              name,
              new MTScript(
                  name,
                  autoExecSet.contains(path),
                  descriptionMap.getOrDefault(path, ""),
                  entry.getValue().getValue0()));
        }
      }
    }

    urlPathAssetMap = Collections.unmodifiableMap(urlsMap);
    mtsFunctionAssetMap = Collections.unmodifiableMap(mtsMap);

    licenseFile = dto.getLicenseFile();
    readMeFile = dto.getReadMeFile();

    jsContextName = JS_CONTEXT_PREFIX + namespace;

    libraryInfo =
        new LibraryInfo(
            name,
            namespace,
            version,
            website,
            gitUrl,
            authors,
            license,
            description,
            shortDescription,
            allowsUriAccess,
            readMeFile.isEmpty() ? null : readMeFile,
            licenseFile.isEmpty() ? null : licenseFile);

    for (var s : slashCommandsDto.getSlashCommandsList()) {
      slashCommands.put(
          s.getName(),
          new MacroDetails(
              s.getName(),
              s.getCommand(),
              s.getDescription(),
              MacroManager.Scope.ADDON,
              namespace,
              name));
    }
  }

  /**
   * Creates a new Drop In Library from the given {@link AddOnLibraryDto}, {@link
   * MTScriptPropertiesDto}, and file path assets map.
   *
   * @param dto The Drop In Libraries Data Transfer Object.
   * @param mtsDto The MTScript Properties Data Transfer Object.
   * @param eventsDto The Events Data Transfer Object.
   * @param slashCommandsDto The Slash Commands Data Transfer Object.
   * @param pathAssetMap mapping of paths in the library to {@link MD5Key}s and {@link Type}s.
   * @return the new Add on library.
   */
  public static AddOnLibrary fromDto(
      MD5Key libraryAssetKey,
      AddOnLibraryDto dto,
      MTScriptPropertiesDto mtsDto,
      AddOnLibraryEventsDto eventsDto,
      AddOnStatSheetsDto statSheetsDto,
      AddonSlashCommandsDto slashCommandsDto,
      Map<String, Pair<MD5Key, Type>> pathAssetMap) {

    return new AddOnLibrary(
        libraryAssetKey, dto, mtsDto, eventsDto, statSheetsDto, slashCommandsDto, pathAssetMap);
  }

  @Override
  public CompletableFuture<String> getDescription() {
    return CompletableFuture.completedFuture(description);
  }

  @Override
  public CompletableFuture<String> getShortDescription() {
    return CompletableFuture.completedFuture(shortDescription);
  }

  @Override
  public CompletableFuture<Boolean> allowsUriAccess() {
    return CompletableFuture.completedFuture(allowsUriAccess);
  }

  /**
   * Returns a list of the library tokens.
   *
   * @return list of library tokens
   */
  @Override
  public CompletableFuture<LibraryInfo> getLibraryInfo() {
    return CompletableFuture.completedFuture(libraryInfo);
  }

  /**
   * Return a {@link MTScriptMacroInfo} for the macro.
   *
   * @param macroName The name of the macro.
   * @param macro The macro details.
   * @return The {@link MTScriptMacroInfo} details.
   */
  private CompletableFuture<Optional<MTScriptMacroInfo>> getMacroInfo(
      String macroName, MTScript macro) {
    return CompletableFuture.supplyAsync(
        () -> {
          Asset asset = AssetManager.getAsset(macro.md5Key());
          String command = asset.getDataAsString();
          // Drop In Library Functions are always trusted as only GM can add and no one can edit.
          return Optional.of(
              new MTScriptMacroInfo(
                  macroName,
                  command,
                  true, // Drop In Library Functions are always trusted
                  macro.autoExecute(),
                  macro.description()));
        });
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName) {
    var macro = mtsFunctionAssetMap.get(MTSCRIPT_PUBLIC_DIR + macroName);
    if (macro == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return getMacroInfo(macroName, macro);
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
    var macro = mtsFunctionAssetMap.get(macroName);
    if (macro == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return getMacroInfo(macroName, macro);
  }

  @Override
  public CompletableFuture<List<String>> getAllFiles() {
    return CompletableFuture.completedFuture(new ArrayList<>(pathAssetMap.keySet()));
  }

  @Override
  public CompletableFuture<LibraryData> getLibraryData() {
    return CompletableFuture.completedFuture(
        new AddOnLibraryData(this, this.namespace.toLowerCase()));
  }

  @Override
  public CompletableFuture<Optional<String>> getLegacyEventHandlerName(String eventName) {
    return CompletableFuture.completedFuture(
        Optional.ofNullable(legacyEventNameMap.get(eventName)));
  }

  @Override
  public CompletableFuture<Optional<Token>> getAssociatedToken() {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public boolean canMTScriptAccessPrivate(MapToolMacroContext context) {
    String source = context.getSource().replaceFirst("(?i)^lib:", "");
    return context == null || source.equalsIgnoreCase(namespace);
  }

  @Override
  public CompletableFuture<Optional<Asset>> getReadMeAsset() {
    if (readMeFile.isEmpty()) {
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      return CompletableFuture.supplyAsync(
          () -> {
            var asset = pathAssetMap.get(readMeFile);
            return Optional.of(AssetManager.getAsset(asset.getValue0()));
          });
    }
  }

  @Override
  public CompletableFuture<Optional<Asset>> getLicenseAsset() {
    if (licenseFile.isEmpty()) {
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      return CompletableFuture.supplyAsync(
          () -> {
            var asset = pathAssetMap.get(licenseFile);
            return Optional.of(AssetManager.getAsset(asset.getValue0()));
          });
    }
  }

  @Override
  public void cleanup() {
    // Remove any existing JavaScript context if it exists
    if (JSScriptEngine.hasAddOnContext(jsContextName)) {
      JSScriptEngine.removeAddOnContext(jsContextName);
    }
  }

  @Override
  public Set<MacroDetails> getSlashCommands() {
    return Set.copyOf(slashCommands.values());
  }

  @Override
  public CompletableFuture<String> getVersion() {
    return CompletableFuture.completedFuture(version);
  }

  /**
   * Returns the asset information for the specified location, this will take care of the mapping to
   * public/
   *
   * @param location the URI location passed in.
   * @return the asset information for the path, if there is no asset information at that path then
   *     null is returned.
   */
  private Pair<MD5Key, Asset.Type> getURILocation(URL location) {
    return urlPathAssetMap.get(location.getPath().replaceFirst("^/", ""));
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    if (allowsUriAccess) {
      return CompletableFuture.completedFuture(getURILocation(location) != null);
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.addOn.no.access", name));
    }
  }

  @Override
  public CompletableFuture<Boolean> isAsset(URL location) {
    return CompletableFuture.completedFuture(getURILocation(location) != null);
  }

  @Override
  public CompletableFuture<Optional<MD5Key>> getAssetKey(URL location) {
    var AssetInfo = getURILocation(location);
    if (AssetInfo == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return CompletableFuture.completedFuture(Optional.of(AssetInfo.getValue0()));
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    if (allowsUriAccess) {
      var values = getURILocation(location);
      if (values == null) {
        throw new IOException("Invalid Location");
      }
      if (!values.getValue1().isStringType()) {
        throw new LibraryNotValidException(
            Reason.BAD_CONVERSION,
            I18N.getText("library.error.addOn.notText", values.getValue1().name()));
      }
      return CompletableFuture.supplyAsync(
          () -> {
            Asset asset = AssetManager.getAsset(values.getValue0());
            return asset.getDataAsString();
          });
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.addOn.no.access", name));
    }
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    if (allowsUriAccess) {
      var values = getURILocation(location);
      if (values == null) {
        throw new IOException("Invalid Location");
      }
      return CompletableFuture.supplyAsync(
          () -> {
            Asset asset = AssetManager.getAsset(values.getValue0());
            return new ByteArrayInputStream(asset.getData());
          });
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.addOn.no.access", name));
    }
  }

  @Override
  public CompletableFuture<String> getWebsite() {
    return CompletableFuture.completedFuture(website);
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return CompletableFuture.completedFuture(authors);
  }

  @Override
  public CompletableFuture<String> getGitUrl() {
    return CompletableFuture.completedFuture(gitUrl);
  }

  @Override
  public CompletableFuture<String> getLicense() {
    return CompletableFuture.completedFuture(license);
  }

  @Override
  public CompletableFuture<String> getNamespace() {
    return CompletableFuture.completedFuture(namespace);
  }

  @Override
  public CompletableFuture<String> getName() {
    return CompletableFuture.completedFuture(name);
  }

  /**
   * Returns the asset key for the asset of the add-on library.
   *
   * @return the asset key for the asset of the add-on library./get
   */
  public MD5Key getAssetKey() {
    return assetKey;
  }

  /**
   * Returns the legacy events handled by this add-on library. This method is thread safe.
   *
   * @return the legacy events handled by this add-on library.
   */
  public Set<String> getLegacyEvents() {
    return legacyEventNameMap.keySet();
  }

  /** Run first time initialization of the add-on library. */
  void initialize() {
    registerSheets();
    getLibraryData()
        .thenAccept(
            d -> {
              var data = (AddOnLibraryData) d;
              data.initialize()
                  .thenRun(
                      () -> {
                        data.needsInitialization()
                            .thenAccept(
                                needInit -> {
                                  // First remove any existing JavaScript context if it exists
                                  if (JSScriptEngine.hasAddOnContext(jsContextName)) {
                                    JSScriptEngine.removeAddOnContext(jsContextName);
                                  }
                                  // Then create a new JavaScript context for the add-on library
                                  JSScriptEngine.registerAddOnContext(jsContextName);
                                  if (needInit) {
                                    if (jsEventNameMap.containsKey(FIRST_INIT_EVENT)) {
                                      runJS(jsEventNameMap.get(FIRST_INIT_EVENT));
                                    }
                                    if (mtScriptEventNameMap.containsKey(FIRST_INIT_EVENT)) {
                                      callMTSFunction(mtScriptEventNameMap.get(FIRST_INIT_EVENT))
                                          .join();
                                    }
                                    data.setNeedsToBeInitialized(false).join();
                                  }
                                  if (jsEventNameMap.containsKey(INIT_EVENT)) {
                                    runJS(jsEventNameMap.get(INIT_EVENT));
                                  }
                                  if (mtScriptEventNameMap.containsKey(INIT_EVENT)) {
                                    callMTSFunction(mtScriptEventNameMap.get(INIT_EVENT)).join();
                                  }
                                });
                      });
            })
        .join();
  }

  /** Registers the stat sheets that this add-on defines. */
  public void registerSheets() {
    var statSheetManager = new StatSheetManager();
    statSheetManager.removeNamespace(namespace);
    for (StatSheet sheet : statSheets) {
      try {
        statSheetManager.addStatSheet(sheet, this);
      } catch (IOException e) {
        logger.error(I18N.getText("library.error.addOn.sheet", namespace, sheet.name()));
        MapTool.showError(I18N.getText("library.error.addOn.sheet", namespace, sheet.name()), e);
      }
    }
  }

  /**
   * Call the specified function MacroScript function from the add-on library.
   *
   * @param name the name of the function MacroScript function to call.
   * @return a CompletableFuture that completes when the function MacroScript function has finished
   */
  private CompletableFuture<Void> callMTSFunction(String name) {
    if (SwingUtilities.isEventDispatchThread()) {
      var resolver = new MapToolVariableResolver(null);
      try {
        MapTool.getParser().runMacro(resolver, null, name + "@lib:" + namespace, "");
      } catch (ParserException e) {
        throw new CompletionException(e);
      }
    } else {
      SwingUtilities.invokeLater(
          () -> {
            var resolver = new MapToolVariableResolver(null);
            try {
              MapTool.getParser().runMacro(resolver, null, name + "@lib:" + namespace, "");
            } catch (ParserException e) {
              throw new CompletionException(e);
            }
          });
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Run the specified JavaScript code from the add-on library in the add-on library's JavaScript
   * context.
   *
   * @param file the JavaScript file withing the file to run.
   */
  private void runJS(String file) {
    readFile(file)
        .thenAccept(
            script -> {
              try {
                JSScriptEngine.getJSScriptEngine()
                    .evalScript(jsContextName, script.asAsset().getDataAsString(), true);
              } catch (ParserException | ScriptException e) {
                throw new RuntimeException(e);
              }
            })
        .join();
  }

  CompletableFuture<DataValue> readFile(String path) {
    return CompletableFuture.supplyAsync(
        () -> {
          String filePath = path.replaceFirst("^/", "");
          var val = pathAssetMap.get(filePath);
          if (val == null) {
            return DataValueFactory.undefined(path);
          }
          Asset asset = AssetManager.getAsset(val.getValue0());
          return DataValueFactory.fromAsset(filePath, asset);
        });
  }
}
