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
package net.rptools.maptool.client.ui.htmlframe;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Zone.Event;
import net.rptools.parser.ParserException;

public class HTMLFrameFactory {
  private HTMLFrameFactory() {}

  public enum FrameType {
    FRAME,
    DIALOG,
    OVERLAY
  }

  private static HTMLFrameFactory.Listener listener;

  /**
   * Shows a dialog or frame based on the options.
   *
   * @param name The name of the dialog or frame.
   * @param frameType The type of the frame.
   * @param isHTML5 Does it use HTML5 (JavaFX) or HTML 3.2 (Swing).
   * @param properties The properties that determine the attributes of the frame or dialog.
   * @param html The html contents of frame or dialog.
   * @throws ParserException if zorder is not numerical
   */
  public static void show(
      String name, FrameType frameType, boolean isHTML5, String properties, String html)
      throws ParserException {
    if (listener == null) {
      listener = new HTMLFrameFactory.Listener();
    }
    boolean input = false;
    boolean temporary = false;
    int width = -1;
    int height = -1;
    int zOrder = 0;
    String title = name;
    String tabTitle = null;
    Object frameValue = null;
    boolean hasFrame = true;
    boolean closeButton = true;
    boolean scrollReset = false;

    if (properties != null && !properties.isEmpty()) {
      String[] opts = properties.split(";");
      for (String opt : opts) {
        String[] vals = opt.split("=");
        String key = vals[0].trim();
        String value = vals.length > 1 ? vals[1].trim() : "";
        String keyLC = key.toLowerCase();
        if (keyLC.equals("input")) {
          try {
            int v = Integer.parseInt(value);
            if (v != 0) {
              input = true;
              closeButton = !input; // disable button by default
            }
          } catch (NumberFormatException e) {
            // Ignoring the value; shouldn't we warn the user?
          }
        } else if (keyLC.equals("temporary")
            || keyLC.equals("undecorated")
            || keyLC.equals("temp")) {
          try {
            int v = Integer.parseInt(value);
            if (v != 0) {
              temporary = true; // undecorated is temporary by default
            }
          } catch (NumberFormatException e) {
            // Ignoring the value; shouldn't we warn the user?
          }
        } else if (keyLC.equals("width")) {
          try {
            width = Integer.parseInt(value);
          } catch (NumberFormatException e) {
            // Ignoring the value; shouldn't we warn the user?
          }
        } else if (keyLC.equals("height")) {
          try {
            height = Integer.parseInt(value);
          } catch (NumberFormatException e) {
            // Ignoring the value; shouldn't we warn the user?
          }
        } else if (keyLC.equals("zorder")) {
          try {
            zOrder = Integer.parseInt(value);
          } catch (NumberFormatException e) {
            String funcName = frameType.toString().toLowerCase();
            String msg = I18N.getText("macro.function.general.argumentKeyTypeI", funcName, keyLC);
            throw new ParserException(msg);
          }
        } else if (keyLC.equals("title")) {
          title = value;
        } else if (keyLC.equals("noframe")) {
          try {
            int v = Integer.parseInt(value);
            if (v != 0) {
              hasFrame = false;
            }
          } catch (NumberFormatException e) {
            // Ignoring the value; shouldn't we warn the user?
          }
        } else if (keyLC.equals("closebutton")) {
          try {
            int v = Integer.parseInt(value);
            if (v == 0) {
              closeButton = false;
            }
          } catch (NumberFormatException e) {
            // Ignoring the value; shouldn't we warn the user?
          }
        } else if (keyLC.equals("scrollreset")) {
          int v = Integer.parseInt(value);
          if (v != 0) {
            scrollReset = true;
          }
        } else if (keyLC.equals("value")) {
          frameValue = value;
        } else if (keyLC.equals("tabtitle")) {
          tabTitle = value;
        }
      }
    }
    if (tabTitle == null) tabTitle = title; // if tabTitle not set, make it same as title
    if (frameType == FrameType.FRAME) {
      HTMLFrame.showFrame(
          name, title, tabTitle, width, height, temporary, scrollReset, isHTML5, frameValue, html);
    } else if (frameType == FrameType.DIALOG) {
      HTMLDialog.showDialog(
          name,
          title,
          width,
          height,
          hasFrame,
          input,
          temporary,
          closeButton,
          scrollReset,
          isHTML5,
          frameValue,
          html);
    } else if (frameType == FrameType.OVERLAY) {
      MapTool.getFrame().getOverlayPanel().showOverlay(name, zOrder, html);
    }
  }

  /** The list of selected tokens changed. */
  public static void selectedListChanged() {
    HTMLFrame.doSelectedChanged();
    HTMLDialog.doSelectedChanged();
    MapTool.getFrame().getOverlayPanel().doSelectedChanged();
  }

  /** A new token has been impersonated or cleared. */
  public static void impersonateToken() {
    HTMLFrame.doImpersonatedChanged();
    HTMLDialog.doImpersonatedChanged();
    MapTool.getFrame().getOverlayPanel().doImpersonatedChanged();
  }

  /**
   * One of the tokens has changed.
   *
   * @param token the token that have changed
   */
  public static void tokenChanged(Token token) {
    HTMLFrame.doTokenChanged(token);
    HTMLDialog.doTokenChanged(token);
    MapTool.getFrame().getOverlayPanel().doTokenChanged(token);
  }

  public static class Listener implements ModelChangeListener, AppEventListener {
    public Listener() {
      MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
      MapTool.getFrame().getCurrentZoneRenderer().getZone().addModelChangeListener(this);
    }

    public void modelChanged(ModelChangeEvent event) {
      if (event.eventType == Event.TOKEN_CHANGED) {
        final CommandPanel cpanel = MapTool.getFrame().getCommandPanel();

        List<Token> tokens; // could be receiving a list from putTokens()
        if (event.getArg() instanceof Token) {
          tokens = Collections.singletonList((Token) event.getArg());
        } else tokens = (List<Token>) event.getArg();
        Set<GUID> selectedTokens =
            MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
        boolean selectedChange = false;
        for (Token token : tokens) {
          if (selectedTokens.contains(token.getId())) {
            selectedChange = true;
          }
          if (token.getName().equals(cpanel.getIdentity())
              || token.getId().equals(cpanel.getIdentityGUID())) {
            impersonateToken();
          }
          tokenChanged(token);
        }
        if (selectedChange) {
          selectedListChanged();
        }
      }
    }

    public void handleAppEvent(AppEvent event) {
      Zone oldZone = (Zone) event.getOldValue();
      Zone newZone = (Zone) event.getNewValue();

      if (oldZone != null) {
        oldZone.removeModelChangeListener(this);
      }
      newZone.addModelChangeListener(this);
    }
  }

  /**
   * Return the visibility of the container.
   *
   * @param isFrame is it a frame or a container?
   * @param name the name of the container.
   * @return is it visible?
   */
  public static boolean isVisible(boolean isFrame, String name) {
    if (isFrame) {
      return HTMLFrame.isVisible(name);
    } else {
      return HTMLDialog.isVisible(name);
    }
  }

  /**
   * Close a container.
   *
   * @param isFrame is it a frame or a container?
   * @param name the name of the container.
   */
  public static void close(boolean isFrame, String name) {
    if (isFrame) {
      HTMLFrame.close(name);
    } else {
      HTMLDialog.close(name);
    }
  }
}
