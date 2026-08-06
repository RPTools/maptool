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
package net.rptools.maptool.client.functions.exceptions;

import net.rptools.maptool.language.AbstractMessageFactory;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

public class ParserExceptionFactory extends AbstractMessageFactory {
  private Throwable throwable;

  protected ParserExceptionFactory(final String i18nKey) {
    super(i18nKey);
    throwable = null;
  }

  protected ParserExceptionFactory(final Throwable cause) {
    super(null);
    throwable = cause;
  }

  public ParserException exception() {
    if (throwable != null) {
      return new ParserException(throwable);
    } else if (msgKey != null) {
      return new ParserException(I18N.getMessage(msgKey, messageParams));
    } else {
      return new ParserException(I18N.getMessage("macro.function.general.unknownError"));
    }
  }

  public static ParserExceptionFactory forKey(String i18nKey) {
    return new ParserExceptionFactory(i18nKey);
  }

  public ParserExceptionFactory forThrowable(final Throwable cause) {
    throwable = cause;
    return this;
  }

  public ParserExceptionFactory functionName(final String functionName) {
    return (ParserExceptionFactory) namedValue("functionName", functionName);
  }

  public ParserExceptionFactory parameterIndex(final int parameterIndex) {
    return (ParserExceptionFactory) namedValue("parameterIndex", parameterIndex);
  }

  public ParserExceptionFactory parameterValue(Object parameterValue) {
    return (ParserExceptionFactory) namedValue("parameterValue", parameterValue);
  }

  public ParserExceptionFactory results(String results) {
    return (ParserExceptionFactory) namedValue("results", results);
  }

  public ParserExceptionFactory options(String options) {
    return (ParserExceptionFactory) namedValue("options", options);
  }
}
