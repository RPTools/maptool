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
package net.rptools.maptool.client.ui.syntax;

import java.util.ResourceBundle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.DefinesSpecialVariables;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

public class MapToolScriptAutoComplete {
  private static final Logger log = LogManager.getLogger(MapToolScriptAutoComplete.class);

  private static final ResourceBundle MACRO_DESCRIPTIONS_BUNDLE =
      ResourceBundle.getBundle("net.rptools.maptool.language.macro_descriptions.i18n");
  private static final String I18N_SHORT_DESCRIPTION = ".description";
  private static final String I18N_SUMMARY = ".summary";
  private static final String I18N_PLACEHOLDER_TEXT = "TBD";

  DefaultCompletionProvider provider =
      new DefaultCompletionProvider() {
        @Override
        protected boolean isValidChar(char ch) {
          return super.isValidChar(ch) || ch == '.';
        }
      };

  public MapToolScriptAutoComplete() {
    for (String macro : MapTool.getParser().listAllMacroFunctions())
      provider.addCompletion(
          new BasicCompletion(provider, macro, getShortDescription(macro), getSummary(macro)));

    // Add "Special Variables" as Data Type
    for (String dataType : MapToolScriptSyntax.DATA_TYPES)
      provider.addCompletion(
          new BasicCompletion(
              provider, dataType, getShortDescription(dataType), getSummary(dataType)));

    // Add "Roll Options" as Reserved word
    for (String reservedWord : MapToolScriptSyntax.RESERVED_WORDS)
      provider.addCompletion(
          new BasicCompletion(
              provider, reservedWord, getShortDescription(reservedWord), getSummary(reservedWord)));

    // Add "Events" as Reserved Word 2
    for (String reservedWord : MapToolScriptSyntax.RESERVED_WORDS_2)
      provider.addCompletion(
          new BasicCompletion(
              provider, reservedWord, getShortDescription(reservedWord), getSummary(reservedWord)));

    for (Function function : MapTool.getParser().getMacroFunctions()) {
      if (function instanceof DefinesSpecialVariables) {
        for (String specialVariable : ((DefinesSpecialVariables) function).getSpecialVariables()) {
          provider.addCompletion(
              new BasicCompletion(
                  provider,
                  specialVariable,
                  getShortDescription(specialVariable),
                  getSummary(specialVariable)));
        }
      }
    }

    // FIXME: TERRIBLE! But I'm tired and running out of time, need to add to a .properties
    // file!
    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "if",
            "[h, if(condition), CODE: {\r\n\t[h: true_body]\r\n};{\r\n\t[h: false_body]\r\n}]",
            "A complex hidden IF/THEN statement"));
    provider.addCompletion(
        new ShorthandCompletion(
            provider, "if", "[h, if(condition): true_body]", "A basic hidden IF statement")); // Can
    // also
    // add
    // a
    // full
    // HTML
    // description
    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "if",
            "[h, if(condition): true_body; false_body]",
            "A basic hidden IF/THEN statement"));

    provider.addCompletion(
        new ShorthandCompletion(
            provider, "for", "[h, for(var, start, end): body]", "A basic hidden FOR loop"));
    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "for",
            "[h, for(var, start, end, stepsize): body]",
            "A basic hidden FOR loop with steps"));
    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "for",
            "[h, for(var, start, end, stepsize, separator): body]",
            "A basic hidden FOR loop with seps and seperator"));

    provider.addCompletion(
        new ShorthandCompletion(
            provider, "foreach", "[h, FOREACH(var, list): body]", "A basic hidden FOREACH loop"));
    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "foreach",
            "[h, FOREACH(var, list, output_separator): body]",
            "A basic hidden FOREACH loop with steps"));
    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "foreach",
            "[h, FOREACH(var, list, output_separator, list_separator): body]",
            "A basic hidden FOREACH loop with seps and seperator"));

    provider.addCompletion(
        new ShorthandCompletion(
            provider,
            "foreach",
            "[h, FOREACH(var, list), CODE: {\r\n\t[h: do_stuff]\r\n}]",
            "A complex hidden FOREACH loop"));
  }

  private String getShortDescription(String macro) {
    String shortDesc = I18N.getString(macro + I18N_SHORT_DESCRIPTION, MACRO_DESCRIPTIONS_BUNDLE);

    if (shortDesc == null) return null;

    if (shortDesc.equals(I18N_PLACEHOLDER_TEXT)) return null;
    else return shortDesc;
  }

  private String getSummary(String macro) {
    String summary = I18N.getString(macro + I18N_SUMMARY, MACRO_DESCRIPTIONS_BUNDLE);

    if (summary == null) return null;

    if (summary.equals(I18N_PLACEHOLDER_TEXT)) return null;
    else return summary;
  }

  public CompletionProvider get() {
    return provider;
  }
}
