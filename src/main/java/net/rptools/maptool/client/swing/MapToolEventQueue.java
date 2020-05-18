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
package net.rptools.maptool.client.swing;

import com.jidesoft.dialog.JideOptionPane;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.functions.getInfoFunction;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.util.MapToolSysInfoProvider;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapToolEventQueue extends EventQueue {
  private static final Logger log = LogManager.getLogger(MapToolEventQueue.class);
  private static final JideOptionPane optionPane =
      new JideOptionPane(
          I18N.getString("MapToolEventQueue.details"), // $NON-NLS-1$
          JOptionPane.ERROR_MESSAGE,
          JideOptionPane.CLOSE_OPTION);

  @Override
  protected void dispatchEvent(AWTEvent event) {
    try {
      if (event instanceof MouseWheelEvent) {
        MouseWheelEvent mwe = (MouseWheelEvent) event;
        if (AppUtil.MAC_OS_X && mwe.isShiftDown()) {
          // issue 1317: ignore ALL horizontal movement on macOS, *even if* the physical Shift is
          // held down.
          return;
        }
      }
      super.dispatchEvent(event);
    } catch (StackOverflowError soe) {
      log.error(soe, soe);
      optionPane.setTitle(I18N.getString("MapToolEventQueue.stackOverflow.title")); // $NON-NLS-1$
      optionPane.setDetails(I18N.getString("MapToolEventQueue.stackOverflow"));
      displayPopup();
      reportToSentryIO(soe);
    } catch (Throwable t) {
      log.error(t, t);
      optionPane.setTitle(I18N.getString("MapToolEventQueue.unexpectedError")); // $NON-NLS-1$
      optionPane.setDetails(toString(t));
      try {
        displayPopup();
        reportToSentryIO(t);
      } catch (Throwable thrown) {
        // Displaying the error message using the JideOptionPane has just failed. Fallback to
        // standard swing
        // dialog.
        log.error(thrown, thrown);
        JOptionPane.showMessageDialog(
            null,
            toString(thrown),
            I18N.getString("MapToolEventQueue.unexpectedError"),
            JOptionPane.ERROR_MESSAGE);
        reportToSentryIO(thrown);
      }
    }
  }

  private static void displayPopup() {
    optionPane.setDetailsVisible(true);
    JDialog dialog =
        optionPane.createDialog(
            MapTool.getFrame(), I18N.getString("MapToolEventQueue.warning.title")); // $NON-NLS-1$
    dialog.setResizable(true);
    dialog.pack();
    dialog.setVisible(true);
  }

  private static String toString(Throwable t) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    t.printStackTrace(ps);
    ps.close();
    return out.toString();
  }

  private static void reportToSentryIO(Throwable thrown) {
    if (Sentry.getStoredClient().getEnvironment().equalsIgnoreCase("development")) {
      log.info("Sentry.IO stacktrace logging skipped in development environment.");
      return;
    }

    // Note that all fields set on the context are optional. Context data is copied onto all future
    // events in the
    // current context (until the context is cleared).

    // Record a breadcrumb in the current context. By default the last 100 breadcrumbs are kept.
    // TODO: We could use this to record user actions to get a hint on what user was doing before
    // exception was
    // thrown...
    // Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("User made an
    // action").build());

    UserBuilder user = new UserBuilder();
    Player player = MapTool.getPlayer();
    if (player != null) {
      user.setUsername(player.getName());
      user.setId(MapTool.getClientId());
      user.setEmail(
          player.getName().replaceAll(" ", "_") + "@rptools.net"); // Lets prompt for this?
    } else {
      user.setUsername("Unknown");
      user.setId("Unknown");
      user.setEmail("Unknown");
    }

    // Set the user in the current context.
    Sentry.getContext().setUser(user.build());

    Sentry.getContext().addTag("role", player != null ? player.getRole().toString() : null);
    boolean hostingServer = MapTool.isHostingServer();
    Sentry.getContext().addTag("hosting", String.valueOf(MapTool.isHostingServer()));

    Sentry.getContext().addExtra("System Info", new MapToolSysInfoProvider().getSysInfoJSON());

    addGetInfoToSentry("campaign");

    if (hostingServer) {
      addGetInfoToSentry("server");
      Sentry.getContext().addExtra("Server Policy", MapTool.getServerPolicy().toJSON());
    }

    // Send the event!
    Sentry.capture(thrown);
  }

  private static void addGetInfoToSentry(String command) {
    Object campaign;
    try {
      MapToolMacroContext sentryContext = new MapToolMacroContext(command, "sentryIOLogging", true);
      MapTool.getParser().enterContext(sentryContext);
      campaign =
          getInfoFunction
              .getInstance()
              .childEvaluate(null, null, Collections.singletonList(command));
      MapTool.getParser().exitContext();
    } catch (ParserException e) {
      campaign = "Can't call getInfo(\"" + command + "\"), it threw " + e.getMessage();
    }
    Sentry.getContext().addExtra("getinfo(\"" + command + "\")", campaign);
  }
}
