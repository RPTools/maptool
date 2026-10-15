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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Changing an initiative list when no entry has the initiative. */
class InitiativeListTest {
  /** Collects the errors logged anywhere while a test runs. */
  private static class ErrorCollector extends AbstractAppender {
    final List<String> errors = new ArrayList<>();

    ErrorCollector() {
      super("InitiativeListTestErrors", null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      if (event.getLevel().isMoreSpecificThan(Level.ERROR)) {
        errors.add(event.getMessage().getFormattedMessage());
      }
    }
  }

  private ErrorCollector collector;

  @BeforeEach
  void collectErrors() {
    collector = new ErrorCollector();
    collector.start();
    var config = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
    config.getRootLogger().addAppender(collector, Level.ERROR, null);
    ((LoggerContext) LogManager.getContext(false)).updateLoggers();
  }

  @AfterEach
  void stopCollecting() {
    var config = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
    config.getRootLogger().removeAppender(collector.getName());
    ((LoggerContext) LogManager.getContext(false)).updateLoggers();
    collector.stop();
  }

  @Test
  void changingAListWithNoCurrentEntryLogsNoError() {
    var list = new InitiativeList(null);
    var first = new Token();
    var second = new Token();

    list.insertToken(-1, first);
    list.insertToken(-1, second);
    list.moveToken(0, 2);

    assertEquals(List.of(), collector.errors);
    assertEquals(-1, list.getCurrent());
    assertEquals(2, list.getSize());
  }

  @Test
  void theCurrentEntryKeepsTheInitiativeWhenTheListChanges() {
    var list = new InitiativeList(null);
    var first = new Token();
    var second = new Token();
    var firstEntry = list.insertToken(-1, first);
    list.setCurrent(0);

    list.insertToken(0, second);

    assertEquals(1, list.getCurrent());
    assertSame(firstEntry, list.getTokenInitiative(list.getCurrent()));
    assertEquals(List.of(), collector.errors);
  }
}
