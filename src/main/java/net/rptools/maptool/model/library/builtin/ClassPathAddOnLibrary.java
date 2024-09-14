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
package net.rptools.maptool.model.library.builtin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroManager.MacroDetails;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.MTScriptMacroInfo;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.data.LibraryData;

public class ClassPathAddOnLibrary implements BuiltInLibrary {

  private final String resourceFilePath;

  private final AddOnLibrary addOnLibrary;

  /**
   * Creates a new instance of {@link ClassPathAddOnLibrary}.
   *
   * @param resourceFilePath the resource path for the library.
   * @param addOnLibrary the add-on library that was loaded.
   */
  public ClassPathAddOnLibrary(String resourceFilePath, AddOnLibrary addOnLibrary) {
    this.resourceFilePath = resourceFilePath;
    this.addOnLibrary = addOnLibrary;
  }

  /** The directory on the class path for the built in libraries. */
  public static final String BUILTIN_LIB_CLASSPATH_DIR = "net/rptools/maptool/libraries/builtin";

  @Override
  public CompletableFuture<String> getVersion() {
    return addOnLibrary.getVersion();
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    return addOnLibrary.locationExists(location);
  }

  @Override
  public CompletableFuture<Boolean> isAsset(URL location) {
    return addOnLibrary.isAsset(location);
  }

  @Override
  public CompletableFuture<Optional<MD5Key>> getAssetKey(URL location) {
    return addOnLibrary.getAssetKey(location);
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    return addOnLibrary.readAsString(location);
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    return addOnLibrary.read(location);
  }

  @Override
  public CompletableFuture<String> getWebsite() {
    return addOnLibrary.getWebsite();
  }

  @Override
  public CompletableFuture<String> getGitUrl() {
    return addOnLibrary.getGitUrl();
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return addOnLibrary.getAuthors();
  }

  @Override
  public CompletableFuture<String> getLicense() {
    return addOnLibrary.getLicense();
  }

  @Override
  public CompletableFuture<String> getNamespace() {
    return addOnLibrary.getNamespace();
  }

  @Override
  public CompletableFuture<String> getName() {
    return addOnLibrary.getName();
  }

  @Override
  public CompletableFuture<String> getDescription() {
    return addOnLibrary.getDescription();
  }

  @Override
  public CompletableFuture<String> getShortDescription() {
    return addOnLibrary.getShortDescription();
  }

  @Override
  public CompletableFuture<Boolean> allowsUriAccess() {
    return addOnLibrary.allowsUriAccess();
  }

  @Override
  public CompletableFuture<LibraryInfo> getLibraryInfo() {
    return addOnLibrary.getLibraryInfo();
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName) {
    return addOnLibrary.getMTScriptMacroInfo(macroName);
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
    return addOnLibrary.getPrivateMacroInfo(macroName);
  }

  @Override
  public CompletableFuture<List<String>> getAllFiles() {
    return addOnLibrary.getAllFiles();
  }

  @Override
  public CompletableFuture<LibraryData> getLibraryData() {
    return addOnLibrary.getLibraryData();
  }

  @Override
  public CompletableFuture<Optional<String>> getLegacyEventHandlerName(String eventName) {
    return addOnLibrary.getLegacyEventHandlerName(eventName);
  }

  @Override
  public CompletableFuture<Optional<Token>> getAssociatedToken() {
    return addOnLibrary.getAssociatedToken();
  }

  @Override
  public boolean canMTScriptAccessPrivate(MapToolMacroContext context) {
    return addOnLibrary.canMTScriptAccessPrivate(context);
  }

  @Override
  public CompletableFuture<Optional<Asset>> getReadMeAsset() {
    return addOnLibrary.getReadMeAsset();
  }

  @Override
  public CompletableFuture<Optional<Asset>> getLicenseAsset() {
    return addOnLibrary.getLicenseAsset();
  }

  @Override
  public void cleanup() {
    addOnLibrary.cleanup();
  }

  @Override
  public Set<MacroDetails> getSlashCommands() {
    return addOnLibrary.getSlashCommands();
  }

  public String getResourceFilePath() {
    return resourceFilePath;
  }

  void initialize() {
    addOnLibrary.registerSheets();
  }
}
