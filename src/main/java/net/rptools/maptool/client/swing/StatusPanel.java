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

import com.formdev.flatlaf.extras.components.FlatButton;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import org.apache.commons.text.similarity.JaroWinklerDistance;

/**
 * @author trevor
 */
public class StatusPanel extends JPanel {
  private static final StatusMarquee statusLabel = new StatusMarquee();
  // messages that will only be displayed for a short time before the previous message is restored.
  private static final List<String> TEMP_STRINGS = new ArrayList<>();

  /**
   * Add the provided key to the list of messages treated as temporary. They will only replace the
   * current status for a short time until the previous message is restored.
   *
   * @param i18nKey The translation key to look up
   */
  public static void addTempMessageString(String i18nKey) {
    TEMP_STRINGS.add(I18N.getText(i18nKey));
  }

  static {
    addTempMessageString("StatusBar.helpText");
    addTempMessageString("AutoSaveManager.status.autoSaveComplete");
    addTempMessageString("AutoSaveManager.status.autoSaving");
    addTempMessageString("AutoSaveManager.status.lockFailed");
    addTempMessageString("ChatAutoSave.status.chatAutosave");
    addTempMessageString("Zone.status.optimizing");
  }

  public StatusPanel() {
    statusLabel.setMinimumSize(new Dimension(0, 0));

    setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.BOTH;

    add(wrap(statusLabel), constraints);
    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            setStatus(StatusMarquee.labelText);
          }
        });
  }

  /**
   * Set the status-bar message. If you only want it to be displayed for a short time, add the
   * translation string key to the list of temporary messages with {@link
   * StatusPanel#addTempMessageString(String) StatusPanel.addTempMessageString()}
   *
   * @param status The message to display
   */
  public void setStatus(String status) {
    StatusMarquee.setText(status);
  }

  public void addPanel(JComponent component) {
    int nextPos = getComponentCount();

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = nextPos;
    add(wrap(component), constraints);

    validate();
  }

  private JComponent wrap(JComponent component) {
    component.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    return component;
  }

  /**
   * Container for the status message with bells and whistles. When a message exceeds the available
   * space the message can be scrolled (automatically or manually) with an adjustable scroll speed
   * and various delays.
   */
  private static class StatusMarquee extends JPanel {
    private static final Color BG = UIManager.getColor("Panel.background");
    private static boolean allowScroll = AppPreferences.scrollStatusMessages.get();
    private static final int TICK_INTERVAL = 1000 / AppPreferences.frameRateCap.get();
    private static int textDirection;
    private static double scrollSpeed;
    private static double scrollPosition = 0;
    protected static String labelText = ""; // keep a copy as super truncates string overflow
    private static String oldText;
    private static float overflow; // amount that string overflows container
    private static Timer timer;
    private static Timer tempTimer;
    private static FlatButton button;
    private static JLabel marqueeText;
    private static JScrollPane scrollPane;
    private static final AffineTransform SCROLL_TRANSFORM = new AffineTransform();
    private static final String FADE_STRING =
        "   "; // space used at start and end of string where content fades

    private StatusMarquee() {
      super();
      setLayout(new BorderLayout(1, 0));
      setBackground(BG);

      // Reverse direction for RTL scripts
      textDirection = this.getComponentOrientation().isLeftToRight() ? -1 : 1;
      scrollSpeed = textDirection * AppPreferences.scrollStatusSpeed.get();

      button = new FlatButton();
      setButtonIcon();
      button.setToolTipText(I18N.getText("StatusBar.button.toggleAutoScroll.tooltip"));
      button.setSize(getFont().getSize(), getFont().getSize());
      button.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

      add(button, BorderLayout.WEST);

      marqueeText = new JLabel(labelText);
      marqueeText.setDoubleBuffered(allowScroll);
      marqueeText.setPreferredSize(new Dimension(-1, -1));
      marqueeText.setAutoscrolls(true);
      marqueeText.setBackground(getBackground());

      JViewport viewport = new ScrollVP();
      viewport.setView(marqueeText);
      viewport.setBackground(getBackground());

      scrollPane = new FadingScroll();
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      scrollPane.setBackground(getBackground());
      scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
      scrollPane.setViewport(viewport);

      add(scrollPane, BorderLayout.CENTER);
      validate();
      viewport.setDoubleBuffered(true);

      button.addMouseListener(LISTEN_BUTTON);
      marqueeText.addMouseListener(LISTEN_MOUSE);
      marqueeText.addMouseWheelListener(LISTEN_WHEEL);
      initTimer();
    }

    private static void setButtonIcon() {
      if (allowScroll) {
        button.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_PAUSE));
      } else {
        button.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_NEXT));
      }
    }

    private static void resetScrollPosition() {
      scrollPosition = 0;
      incrementScrollPosition(0); // to trigger repaint
    }

    private static void incrementScrollPosition(double delta) {
      if (overflow != 0) {
        scrollPosition =
            Math.clamp(
                scrollPosition + (float) delta,
                textDirection == 1 ? 0 : -1 * (getScrollWidth() + overflow),
                textDirection == 1 ? getScrollWidth() + overflow : 0);
        SCROLL_TRANSFORM.setToTranslation(scrollPosition, 0);
        RepaintManager.currentManager(statusLabel)
            .addDirtyRegion(
                statusLabel,
                statusLabel.getX() + button.getWidth(),
                scrollPane.getY(),
                statusLabel.getWidth() - button.getWidth(),
                scrollPane.getHeight());
      } else if (timer.isRunning()) {
        timer.stop();
      }
    }

    private static int getScrollWidth() {
      if (scrollPane == null) {
        return 0;
      } else {
        return Math.max(
            0, scrollPane.getWidth() - scrollPane.getInsets().left - scrollPane.getInsets().right);
      }
    }

    private static boolean isTempMessage(String string) {
      // we check for similarity because some messages will not be exact, e.g. "Autosave complete.
      // Elapsed time (ms):"
      JaroWinklerDistance winklerDistance = new JaroWinklerDistance();
      double threshhold = 0.15;
      for (String s : TEMP_STRINGS) {
        if (s != null) {
          if (winklerDistance.apply(string, s.trim()) < threshhold) {
            return true;
          }
        }
      }
      return false;
    }

    private static void setText(String text) {
      resetScrollPosition();
      if (tempTimer.isRunning()) {
        // if a message comes through before a temporary message has reset.
        labelText = oldText;
        tempTimer.stop();
      }

      oldText = labelText;
      if (text == null || text.isBlank()) {
        text = oldText;
      }
      if (isTempMessage(text.trim())) {
        tempTimer.start();
      }

      labelText = FADE_STRING + text.trim();
      int stringWidth =
          SwingUtilities.computeStringWidth(
              marqueeText.getFontMetrics(marqueeText.getFont()), labelText);

      if (stringWidth > getScrollWidth()) {
        stringWidth += 70;
        marqueeText.setPreferredSize(new Dimension(stringWidth, -1));
        overflow = stringWidth - getScrollWidth();
      } else {
        overflow = 0;
        marqueeText.setPreferredSize(new Dimension(getScrollWidth() - 1, -1));
      }
      marqueeText.setText(labelText);
      // This will need replacing once auto-wrapping tooltips are implemented
      marqueeText.setToolTipText(
          "<html><p width="
              + (Toolkit.getDefaultToolkit().getScreenSize().width / 3)
              + ">"
              + labelText
              + "</p></html>");
      startTimer();
    }

    private static void initTimer() {
      if (timer == null) {
        timer = new Timer(TICK_INTERVAL, LISTEN_TIMER);
        timer.setRepeats(true);
      }
      if (tempTimer == null) {
        tempTimer =
            new Timer(
                (int) (1000d * AppPreferences.scrollStatusTempDuration.get()),
                (e) -> setText(oldText));
      }
      resetTimer();
    }

    private static void resetTimer() {
      timer.setDelay(TICK_INTERVAL);
      timer.setInitialDelay((int) (1000 * AppPreferences.scrollStatusStartDelay.get()));
      timer.setActionCommand("start_delay");
    }

    private static void startTimer() {
      initTimer(); // just to make sure it exists
      if (allowScroll && overflow > 0 && getScrollWidth() > 0) {
        timer.start();
      } else if (timer.isRunning()) {
        timer.setActionCommand("stop");
        resetScrollPosition();
      }
    }

    private static final ActionListener LISTEN_TIMER =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
              case "start_delay" -> timer.setActionCommand("scroll"); // initial delay completed
              case "end_hold" -> { // end hold period completed
                timer.stop();
                resetTimer();
                resetScrollPosition();
                timer.start();
              }
              case "pause" -> {
                timer.setInitialDelay(0); // start scrolling immediately on resume
                timer.setActionCommand("scroll");
                timer.stop();
              }
              case "scroll" -> incrementScrollPosition(scrollSpeed);
              case "stop" -> {
                timer.stop();
                resetTimer();
                resetScrollPosition();
              }
            }

            if ((textDirection == 1 && scrollPosition > overflow)
                || (textDirection == -1 && scrollPosition < -overflow)) {
              timer.stop();
              timer.setInitialDelay(
                  (int)
                      (1000 * AppPreferences.scrollStatusEndPause.get())); // start end hold period
              timer.setActionCommand("end_hold");
              timer.start();
            }
          }
        };

    private static final MouseAdapter LISTEN_BUTTON =
        new MouseAdapter() {
          // Right-clicking replaces the text with a deliberately long string and scrolls it as an
          // example
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            // toggle scrolling preference
            allowScroll = !allowScroll;
            AppPreferences.scrollStatusMessages.set(allowScroll);
            if (SwingUtilities.isRightMouseButton(e)) {
              oldText = labelText;
              if (timer.isRunning()) {
                timer.stop();
              }
              resetTimer();
              setText(I18N.getText("StatusBar.helpText"));
              if (!timer.isRunning()) {
                timer.start();
              }
            } else {
              setButtonIcon();
              if (!allowScroll && timer.isRunning()) {
                // stop scrolling
                timer.setActionCommand("stop");
              } else if (allowScroll && !timer.isRunning()) {
                // restart scrolling if required
                startTimer();
              }
            }
          }
        };
    private static Point lastPt = new Point(); // for calculating drag distance
    private static final MouseMotionListener LISTEN_DRAG =
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            timer.setActionCommand("pause");
            int direction = lastPt.getX() > e.getPoint().getX() ? -1 : 1;
            incrementScrollPosition(direction * lastPt.distance(e.getPoint()));
            lastPt = e.getPoint();
          }
        };
    private static final MouseAdapter LISTEN_MOUSE =
        new MouseAdapter() {
          private String oldActionCommand;

          @Override
          public void mouseClicked(MouseEvent e) {
            // was not a drag initiator - we are toggling pause state
            marqueeText.removeMouseMotionListener(LISTEN_DRAG);
            if (oldActionCommand.equals("pause")) {
              timer.setActionCommand("scroll");
              timer.start();
            } else {
              timer.setActionCommand("pause");
            }
          }

          @Override
          public void mousePressed(MouseEvent e) {
            lastPt = e.getPoint();
            marqueeText.addMouseMotionListener(LISTEN_DRAG);
            // store for click event as drag will set it to "pause"
            oldActionCommand = timer.getActionCommand();
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            marqueeText.removeMouseMotionListener(LISTEN_DRAG);
          }
        };
    private static final MouseWheelListener LISTEN_WHEEL =
        new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(MouseWheelEvent e) {
            timer.setActionCommand("pause");
            incrementScrollPosition(e.getPreciseWheelRotation());
          }
        };

    /**
     * This paints a linear gradient over the start and end of the scroll pane which looks pretty.
     */
    private static class FadingScroll extends JScrollPane {
      private static final Color CLEAR = new Color(1f, 1f, 1f, 0);
      private static final Color[] GRAD_COLOURS = new Color[] {BG, CLEAR, CLEAR, BG};

      @Override
      public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        Rectangle bounds = getVisibleRect();
        float viewW = (float) bounds.getWidth();
        float fadeFraction =
            (float) g2d.getFontMetrics().getStringBounds(FADE_STRING, g2d).getWidth() / viewW;
        g2d.setPaint(
            new LinearGradientPaint(
                new Point2D.Float(0f, 0f),
                new Point2D.Float(viewW, 0f),
                new float[] {0f, fadeFraction, 1 - fadeFraction, 1f},
                GRAD_COLOURS));
        g2d.fill(bounds);
      }
    }

    /**
     * The normal scroll pane does not scroll the view if the scroll bar is not visible and manually
     * setting the view position can only be done in integer increments. When animated using a
     * fractional scale this looks jumpy and jarring. This overcomes the problem by applying a
     * transform to the graphics object before normal painting.
     */
    private static class ScrollVP extends JViewport {
      @Override
      public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        GraphicsConfiguration gc = g2d.getDeviceConfiguration();
        AffineTransform at = gc.getNormalizingTransform();
        at.concatenate(SCROLL_TRANSFORM);
        g2d.transform(at);
        super.paint(g2d);
        g2d.dispose();
      }
    }
  }
}
