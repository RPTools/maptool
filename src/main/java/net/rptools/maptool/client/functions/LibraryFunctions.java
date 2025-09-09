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
package net.rptools.maptool.client.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.LibraryType;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Class that implements player macro functions. */
public class LibraryFunctions extends AbstractFunction {

  /** Creates a new {@code PlayerFunctions} object. */
  public LibraryFunctions() {
    super(
        0,
        1,
        "library.listAddOnLibraries",
        "library.getInfo",
        "library.listTokenLibraries",
        "library.getContents");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    String fName = functionName.toLowerCase();
    try {
      var libraryManager = new LibraryManager();

      switch (fName) {
        case "library.listaddonlibraries" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
          return librariesAsJson(libraryManager.getLibraries(LibraryType.ADD_ON));
        }
        case "library.getinfo" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
          String namespace = parameters.get(0).toString();
          Optional<LibraryInfo> libraryInfo = libraryManager.getLibraryInfo(namespace);
          if (libraryInfo.isPresent()) {
            return libraryAsJson(libraryInfo.get());
          } else {
            return "";
          }
        }
        case "library.listtokenlibraries" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
          return librariesAsJson(libraryManager.getLibraries(LibraryType.TOKEN));
        }

        case "library.getcontents" -> {
          FunctionUtil.blockUntrustedMacro(functionName);
          FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
          String namespace = parameters.get(0).toString();
          Optional<Library> library = libraryManager.getLibrary(namespace);
          if (library.isPresent()) {
            return listLibraryContents(library.get());
          } else {
            return "";
          }
        }

        default ->
            throw new ParserException(
                I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException(e);
    }
  }

  private JsonArray listLibraryContents(Library library)
      throws ExecutionException, InterruptedException {
    JsonArray json = new JsonArray();
    library
        .getAllFiles()
        .thenAccept(
            l -> {
              l.forEach(json::add);
            })
        .get();

    return json;
  }

  /**
   * Returns the list of {@link LibraryInfo} records as a json list.
   *
   * @param libraries the {@link LibraryInfo} list to convert to json.
   * @return the json list.
   */
  private JsonArray librariesAsJson(List<LibraryInfo> libraries) {
    JsonArray librariesJson = new JsonArray();
    libraries.stream().map(this::libraryAsJson).forEach(librariesJson::add);
    return librariesJson;
  }

  /**
   * Returns the json representation of a {@link LibraryInfo} object.
   *
   * @param library the {@link LibraryInfo} to convert to json.
   * @return the json representation.
   */
  private JsonObject libraryAsJson(LibraryInfo library) {
    JsonObject libraryJson = new JsonObject();
    JsonArray authors = new JsonArray();
    for (String author : library.authors()) {
      authors.add(author);
    }
    libraryJson.addProperty("name", library.name());
    libraryJson.addProperty("namespace", library.namespace());
    libraryJson.addProperty("version", library.version());
    libraryJson.addProperty("website", library.website());
    libraryJson.addProperty("gitUrl", library.gitUrl());
    libraryJson.add("authors", authors);
    libraryJson.addProperty("license", library.license());
    libraryJson.addProperty("description", library.description());
    libraryJson.addProperty("shortDescription", library.shortDescription());
    libraryJson.addProperty("allowsUrlAccess", library.allowsUrlAccess());

    return libraryJson;
  }
}
