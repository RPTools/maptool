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
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.helper.AssignHelper;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.IncludeHelper;
import com.github.jknack.handlebars.helper.NumberHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.URLTemplateLoader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to apply a Handlebars template given a bean.
 *
 * @param <T> The type of the bean to apply the template to.
 */
public class HandlebarsUtil<T> {

  /** The compiled template. */
  private final Template template;

  /** Logging class instance. */
  private static final Logger log = LogManager.getLogger(Token.class);

  /** Handlebars partial template loader that uses Add-On Library URIs */
  private static class LibraryTemplateLoader extends URLTemplateLoader {
    /** Path to template being resolved, relative paths are resolved relative to its parent. */
    Path current;

    private LibraryTemplateLoader(String current, String prefix, String suffix) {
      if (!current.startsWith("/")) {
        current = "/" + current;
      }
      this.current = new File(current).toPath();
      setPrefix(prefix);
      setSuffix(suffix);
    }

    private LibraryTemplateLoader(String current, String prefix) {
      this(current, prefix, DEFAULT_SUFFIX);
    }

    private LibraryTemplateLoader(String current) {
      this(current, DEFAULT_PREFIX, DEFAULT_SUFFIX);
    }

    /** Normalize locations by removing redundant path components */
    @Override
    protected String normalize(final String location) {
      return new File(location).toPath().normalize().toString();
    }

    /** Resolve possibly relative uri relative to current rooted below prefix */
    @Override
    public String resolve(final String uri) {
      var location = current.resolveSibling(uri).normalize().toString();
      if (location.startsWith("/")) {
        location = location.substring(1);
      }
      return getPrefix() + location + getSuffix();
    }

    @Override
    protected URL getResource(String location) throws IOException {
      if (location.startsWith("/")) {
        location = location.substring(1);
      }
      return new URL("lib://" + location);
    }
  }

  private static enum MapToolHelpers implements Helper<Object> {
    /**
     * Turns the textual form of the value into a base64-encoded string. For example:
     *
     * <pre>
     * &lt;script type="application/json;base64" id="jsonProperty"&gt;
     *   {{ base64Encode properties[0].value }}
     * &lt;/script&gt;
     * &lt;script type="application/javascript"&gt;
     * const jsonProperty = JSON.parse(atob(document.getElementById("jsonProperty").innerText));
     * &lt;/script&gt;
     * </pre>
     */
    base64Encode {
      @Override
      public Object apply(final Object context, final Options options) {
        byte[] message = context.toString().getBytes(StandardCharsets.UTF_8);

        return new Handlebars.SafeString(Base64.getUrlEncoder().encodeToString(message));
      }
    }
  }

  /**
   * Creates a new instance of the utility class.
   *
   * @param stringTemplate The template to compile.
   * @param loader The template loader for loading included partial templates
   * @throws IOException If there is an error compiling the template.
   */
  private HandlebarsUtil(String stringTemplate, TemplateLoader loader) throws IOException {
    try {
      Handlebars handlebars = new Handlebars(loader);
      StringHelpers.register(handlebars);
      Arrays.stream(ConditionalHelpers.values())
          .forEach(h -> handlebars.registerHelper(h.name(), h));
      NumberHelper.register(handlebars);
      handlebars.registerHelper(AssignHelper.NAME, AssignHelper.INSTANCE);
      handlebars.registerHelper(IncludeHelper.NAME, IncludeHelper.INSTANCE);
      Arrays.stream(MapToolHelpers.values()).forEach(h -> handlebars.registerHelper(h.name(), h));

      template = handlebars.compileInline(stringTemplate);
    } catch (IOException e) {
      log.error("Handlebars Error: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Creates a new instance of the utility class.
   *
   * @param stringTemplate The template to compile.
   * @param entry The lib:// URL of the template to load partial templates relative to
   * @throws IOException If there is an error compiling the template.
   */
  public HandlebarsUtil(String stringTemplate, URL entry) throws IOException {
    this(stringTemplate, new LibraryTemplateLoader(entry.getHost() + entry.getPath()));
  }

  /**
   * Creates a new instance of the utility class.
   *
   * @param stringTemplate The template to compile.
   * @throws IOException If there is an error compiling the template.
   */
  public HandlebarsUtil(String stringTemplate) throws IOException {
    this(stringTemplate, new ClassPathTemplateLoader());
  }

  /**
   * Applies the template to the given bean.
   *
   * @param bean The bean to apply the template to.
   * @return The result of applying the template to the bean.
   * @throws IOException If there is an error applying the template.
   */
  public String apply(T bean) throws IOException {
    try {
      var context = Context.newBuilder(bean).resolver(JavaBeanValueResolver.INSTANCE).build();
      return template.apply(context);
    } catch (IOException e) {
      log.error("Handlebars Error: {}", e.getMessage());
      throw e;
    }
  }
}
