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

import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/*
 * This software copyright by various authors including the RPTools.net development team, and licensed under the LGPL Version 3 or, at your option, any later version.
 *
 * Portions of this software were originally covered under the Apache Software License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */
public class MarkDownFunctions extends AbstractFunction {

  /** The prefix used for MarkDown types in MTS Script. */
  public static final String MARKDOWN_PREFIX = "markdown.type.";

  /** Creates a new {@code MarkDownFunctions} instance. */
  public MarkDownFunctions() {
    super(0, 2, "markdownToHTML");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, args, 1, 2);
    ParserEmulationProfile profile;
    if (args.size() > 1) {
      profile = getParserType(args.get(1).toString());
    } else {
      profile = ParserEmulationProfile.GITHUB_DOC;
    }

    List<Extension> extensions = new ArrayList<>();
    MutableDataHolder options = new MutableDataSet();

    if (profile == ParserEmulationProfile.GITHUB_DOC) {
      extensions.add(TablesExtension.create());
      extensions.add(TaskListExtension.create());
      extensions.add(DefinitionExtension.create());
      extensions.add(TocExtension.create());
      options
          .set(com.vladsch.flexmark.parser.Parser.SPACE_IN_LINK_URLS, true)
          .setFrom(ParserEmulationProfile.GITHUB_DOC)
          .set(TablesExtension.COLUMN_SPANS, false)
          .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
          .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
          .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
          .set(com.vladsch.flexmark.parser.Parser.EXTENSIONS, extensions);
    } else {
      options.setFrom(profile);
    }

    var mdParser = com.vladsch.flexmark.parser.Parser.builder(options).build();
    HtmlRenderer renderer = HtmlRenderer.builder(options).build();

    String markdownText = args.get(0).toString();

    Node document = mdParser.parse(markdownText);

    return renderer.render(document);
  }

  /**
   * Returns a {@link ParserEmulationProfile} based on the value passed from the MTS Parser.
   *
   * @param name The name of the the MarkDown type starting with {@link #MARKDOWN_PREFIX} with the
   *     {@link String} value of {@link ParserEmulationProfile}.
   * @return the {@link ParserEmulationProfile} used to parse the markdown.
   * @throws ParserException if the {@code name} is invalid.
   */
  private ParserEmulationProfile getParserType(String name) throws ParserException {
    String val = name.trim().replace(MARKDOWN_PREFIX, "");
    try {
      return ParserEmulationProfile.valueOf(val);
    } catch (IllegalArgumentException ex) {
      throw new ParserException(I18N.getText("macro.function.markdown.unknownType", val));
    }
  }

  /**
   * Returns a value that can be used in MT Script to specify the type of markdown to parse.
   *
   * @param name The name of the the MarkDown type.
   * @return the {@link ParserEmulationProfile} used to parse the markdown.
   * @throws ParserException if the {@code name} is invalid.
   */
  public Object getMTSTypeLabel(String name) throws ParserException {
    ParserEmulationProfile profile = getParserType(name);
    // If the above was able to convert properly return the string as is, otherwise allow the parser
    // error bubble up
    return name;
  }
}
