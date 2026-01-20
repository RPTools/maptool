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
package net.rptools.maptool.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
import com.github.jknack.handlebars.cache.TemplateCache;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.sheet.stats.StatSheetContext;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBDebugUtil {
  /** Object for logging messages. */
  private static final Logger log = LoggerFactory.getLogger(HBDebugUtil.class);

  private static final Set<Path> LOG_FILES = new HashSet<>();
  private static final String RESOURCE_PATH = "/net/rptools/maptool/library/builtin/debugTemplates";
  private static final List<String> templateNames = List.of("debug-template", "_data-as-json");
  private Template template;
  private URI resourceUri;
  private static final Map<String, Object> CONTEXT_MAP = new HashMap<>();

  private static final Path LOG_FOLDER = AppUtil.getAppHome("logs").getAbsoluteFile().toPath();

  public HBDebugUtil() {
    if (DeveloperOptions.Toggle.EnableHandlebarsDebugging.get()) {
      try {
        TemplateCache cache = new ConcurrentMapTemplateCache();
        TemplateLoader loader = new ClassPathTemplateLoader(RESOURCE_PATH);
        Handlebars handlebars =
            HandlebarsUtil.getHandlebarsInstance(loader)
                .with(cache)
                .setCharset(StandardCharsets.ISO_8859_1)
                .prettyPrint(true);

        for (String fileName : templateNames) {
          Template t = handlebars.compile(loader.sourceAt(fileName));
          if (fileName.endsWith(templateNames.getFirst())) {
            template = t;
          }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::retract));

      } catch (IOException e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }
  }

  public void publish(
      StatSheetContext statSheetContext, Token token, String content, URL entry, String HTMLout) {
    String day =
        LocalDateTime.now()
            .getDayOfWeek()
            .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
    String sheetName = entry.toString();
    sheetName = sheetName.substring(sheetName.lastIndexOf('/') + 1).replace(".hbs", "");
    String filePrefix = String.format("%s-%s-%s", day, sheetName, token.getName());

    Path tempFileHbs = LOG_FOLDER.resolve(filePrefix + ".hbs");
    Path tempFileHtml = LOG_FOLDER.resolve(filePrefix + ".html");
    Path tempFileInfo = LOG_FOLDER.resolve(filePrefix + "-info.html");

    try {
      for (Path tmpFile : new Path[] {tempFileHbs, tempFileHtml, tempFileInfo}) {
        Files.deleteIfExists(tmpFile);
        LOG_FILES.remove(tmpFile);
      }

      CONTEXT_MAP.clear();
      CONTEXT_MAP.put("token", token);
      CONTEXT_MAP.put("sheetName", sheetName);
      CONTEXT_MAP.put("templateString", content);
      CONTEXT_MAP.put("templateURL", tempFileHbs.getFileName());
      CONTEXT_MAP.put("htmlOut", tempFileHtml.getFileName());
      Context context = Context.newBuilder(statSheetContext).combine(CONTEXT_MAP).build();

      IOUtils.write(
          template.apply(context),
          Files.newOutputStream(tempFileInfo, StandardOpenOption.CREATE),
          StandardCharsets.ISO_8859_1);
      LOG_FILES.add(tempFileInfo);
      IOUtils.write(
          content,
          Files.newOutputStream(tempFileHbs, StandardOpenOption.CREATE),
          StandardCharsets.ISO_8859_1);
      LOG_FILES.add(tempFileHbs);
      IOUtils.write(
          HTMLout,
          Files.newOutputStream(tempFileHtml, StandardOpenOption.CREATE),
          StandardCharsets.ISO_8859_1);
      LOG_FILES.add(tempFileHtml);

      log.info("Handlebars debug output written to {}", LOG_FOLDER);
    } catch (IOException e) {
      log.error(e.getLocalizedMessage(), e);
    }
  }

  private void retract() {
    for (Path p : LOG_FILES) {
      try {
        Files.deleteIfExists(p);
      } catch (IOException e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }
  }
}
