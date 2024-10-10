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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/**
 * New class extending AbstractFunction to create new "Macro Functions" exportData and
 * getEnvironmentVariable
 *
 * <p>These functions is to allow for easier data exports to external files.
 *
 * <p>exportData(FilePath file, String data, boolean appendToFile) :: Saves string data to external
 * file. getEnvironmentVariable(String name) Returns the value stored in the Environment Variable.
 * Useful to store local directory paths for saving data
 */
public class ExportDataFunctions extends AbstractFunction {

  private static final ExportDataFunctions instance = new ExportDataFunctions();

  private ExportDataFunctions() {
    super(1, 3, "exportData", "getEnvironmentVariable");
  }

  public static ExportDataFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted())
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

    if (!AppPreferences.allowExternalMacroAccess.get())
      throw new ParserException(I18N.getText("macro.function.general.accessDenied", functionName));

    // New function to save data to an external file.
    if (functionName.equalsIgnoreCase("exportData")) {
      if (parameters.size() != 3)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

      File file = new File(parameters.get(0).toString());
      String data = parameters.get(1).toString();
      boolean appendToFile = (new BigDecimal(parameters.get(2).toString()).equals(BigDecimal.ONE));

      try {
        // if file doesn't exists, then create it
        if (!file.exists()) {
          file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile(), appendToFile);
        BufferedWriter bw = new BufferedWriter(fw);

        String[] words = data.split("\\\\r");
        for (String word : words) {
          bw.write(word.replaceAll("\\\\t", "\t"));
          bw.newLine();
        }

        bw.close();
      } catch (Exception e) {
        System.out.println("Error in exportData during file write!");
        e.printStackTrace();
        return BigDecimal.ZERO;
      }

      return BigDecimal.ONE;
    }

    if (functionName.equalsIgnoreCase("getEnvironmentVariable")) {
      if (parameters.size() != 1)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

      String envName = parameters.get(0).toString();
      String value = System.getenv(envName);

      // System.out.format("%s=%s%n", envName, value);
      // System.out.format("%s is not assigned.%n", envName);
      return Objects.requireNonNullElse(value, "");
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }
}
