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
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.helper.AssignHelper;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.NumberHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import java.io.IOException;
import java.util.Arrays;
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

  /**
   * Creates a new instance of the utility class.
   *
   * @param stringTemplate The template to compile.
   * @throws IOException If there is an error compiling the template.
   */
  public HandlebarsUtil(String stringTemplate) throws IOException {
    try {
      Handlebars handlebars = new Handlebars();
      StringHelpers.register(handlebars);
      Arrays.stream(ConditionalHelpers.values())
          .forEach(h -> handlebars.registerHelper(h.name(), h));
      NumberHelper.register(handlebars);
      handlebars.registerHelper(AssignHelper.NAME, AssignHelper.INSTANCE);

      template = handlebars.compileInline(stringTemplate);
    } catch (IOException e) {
      log.error("Handlebars Error: {}", e.getMessage());
      throw e;
    }
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

      Handlebars handlebars = new Handlebars();
      var context = Context.newBuilder(bean).resolver(JavaBeanValueResolver.INSTANCE).build();
      return template.apply(context);
    } catch (IOException e) {
      log.error("Handlebars Error: {}", e.getMessage());
      throw e;
    }
  }
}
