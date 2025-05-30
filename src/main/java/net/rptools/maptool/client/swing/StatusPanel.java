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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.Images;
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
            statusLabel.revalidate();
          }

          @Override
          public void componentMoved(ComponentEvent e) {
            statusLabel.revalidate();
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
  private static class StatusMarquee extends JPanel implements ComponentListener {
    private static final FlatButton BUTTON = new FlatButton();
    private static final JLabel HOVER_LABEL = new JLabel();
    private static final JLabel MARQUEE_LABEL = new JLabel();
    private static final JScrollPane SCROLL_PANE = new FadingScroll();
    private static final String FADE_STRING =
        "   "; // space used at start and end of string where content fades
    private static final Color BG = UIManager.getColor("Panel.background");
    private static final Color CLEAR = new Color(1f, 1f, 1f, 0);
    private static final Color[] GRAD_COLOURS = new Color[] {BG, CLEAR, CLEAR, BG};
    private static final AffineTransform SCROLL_TRANSFORM = new AffineTransform();
    private static int tickInterval = 1000 / AppPreferences.frameRateCap.get();
    private static int startDelay = (int) (1000 * AppPreferences.scrollStatusStartDelay.get());
    private static int endDelay = (int) (1000 * AppPreferences.scrollStatusEndPause.get());
    private static int tempDuration = (int) (1000 * AppPreferences.scrollStatusTempDuration.get());
    private static int textDirection =
        MARQUEE_LABEL.getComponentOrientation().isLeftToRight() ? -1 : 1;
    private static boolean allowScroll = AppPreferences.scrollStatusMessages.get();
    private static double scrollSpeed;
    private static double scrollPosition = 0;
    protected static String labelText = ""; // keep a copy as super truncates string overflow
    private static String oldText;
    private static float overflow; // amount that string overflows container
    private static Timer timer;
    private static Timer tempTimer;
    private static final Function<String, Boolean> TEMP_MESSAGE_CHECK =
        (string) -> {
          // check for similarity because some messages will not be exact,
          // e.g. "Autosave complete. Elapsed time (ms):"
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
        };
    private static final Supplier<Integer> SCROLL_WIDTH =
        () ->
            Math.max(
                0,
                SCROLL_PANE.getWidth()
                    - SCROLL_PANE.getInsets().left
                    - SCROLL_PANE.getInsets().right);

    private static final Consumer<Double> INCREMENT_SCROLL =
        (delta) -> {
          if (overflow != 0) {
            scrollPosition =
                Math.clamp(
                    scrollPosition + delta.floatValue(),
                    textDirection == 1 ? 0 : -1 * (SCROLL_WIDTH.get() + overflow),
                    textDirection == 1 ? SCROLL_WIDTH.get() + overflow : 0);
            SCROLL_TRANSFORM.setToTranslation(scrollPosition, 0);
            Rectangle bounds = statusLabel.getBounds();
            RepaintManager.currentManager(statusLabel)
                .addDirtyRegion(statusLabel, bounds.x, bounds.y, bounds.width, bounds.height);
          } else if (timer.isRunning()) {
            timer.stop();
          }
        };
    private static final Function<String, Integer> STRING_WIDTH =
        string ->
            SwingUtilities.computeStringWidth(
                MARQUEE_LABEL.getFontMetrics(MARQUEE_LABEL.getFont()), string);
    private static final Consumer<String> SET_OVERFLOW =
        string -> {
          int stringWidth = STRING_WIDTH.apply(string);
          int scrollWidth = SCROLL_WIDTH.get();
          if (stringWidth > scrollWidth) {
            stringWidth += 70; // add some space at the end
            MARQUEE_LABEL.setPreferredSize(new Dimension(stringWidth, 1));
            overflow = stringWidth - scrollWidth;
          } else {
            overflow = 0;
            MARQUEE_LABEL.setPreferredSize(new Dimension(SCROLL_WIDTH.get() - 1, -1));
          }
        };

    private static final Supplier<Boolean> PREFERENCE_CHANGED =
        () ->
            1000 / AppPreferences.frameRateCap.get() != tickInterval
                || startDelay != (int) (1000 * AppPreferences.scrollStatusStartDelay.get())
                || endDelay != (int) (1000 * AppPreferences.scrollStatusEndPause.get())
                || Math.abs(scrollSpeed) != AppPreferences.scrollStatusSpeed.get()
                || tempDuration != (int) (1000 * AppPreferences.scrollStatusTempDuration.get())
                || AppPreferences.scrollStatusMessages.get() != allowScroll;

    private static void getPreferences() {
      allowScroll = AppPreferences.scrollStatusMessages.get();

      tickInterval = 1000 / AppPreferences.frameRateCap.get();
      startDelay = (int) (1000 * AppPreferences.scrollStatusStartDelay.get());
      endDelay = (int) (1000 * AppPreferences.scrollStatusEndPause.get());
      tempDuration = (int) (1000 * AppPreferences.scrollStatusTempDuration.get());
      initTimer();

      // Reverse direction for RTL scripts
      textDirection = MARQUEE_LABEL.getComponentOrientation().isLeftToRight() ? -1 : 1;
      scrollSpeed = AppPreferences.scrollStatusSpeed.get() * textDirection;
    }

    private StatusMarquee() {
      super();
      getPreferences();
      BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
      setLayout(layout);
      setBackground(BG);

      HOVER_LABEL.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
      HOVER_LABEL.setIcon(
          ImageUtil.scaleImageIcon(
              new ImageIcon(RessourceManager.getImage(Images.CURSOR_THOUGHT)), 24, 24));
      HOVER_LABEL.setOpaque(false);
      add(HOVER_LABEL);

      setButtonIcon();
      BUTTON.setToolTipText(I18N.getText("StatusBar.button.toggleAutoScroll.tooltip"));
      BUTTON.setSize(getFont().getSize(), getFont().getSize());
      BUTTON.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

      add(BUTTON);

      MARQUEE_LABEL.setText(labelText);
      MARQUEE_LABEL.setDoubleBuffered(allowScroll);
      MARQUEE_LABEL.setPreferredSize(new Dimension(-1, -1));
      MARQUEE_LABEL.setAutoscrolls(true);
      MARQUEE_LABEL.setBackground(getBackground());

      JViewport viewport = new ScrollVP();
      viewport.setView(MARQUEE_LABEL);
      viewport.setBackground(getBackground());

      SCROLL_PANE.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      SCROLL_PANE.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      SCROLL_PANE.setBackground(getBackground());
      SCROLL_PANE.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
      SCROLL_PANE.setViewport(viewport);

      add(SCROLL_PANE);
      validate();

      BUTTON.addMouseListener(LISTEN_BUTTON);
      MARQUEE_LABEL.addMouseListener(LISTEN_MOUSE);
      MARQUEE_LABEL.addMouseWheelListener(LISTEN_WHEEL);
      initTimer();
      addComponentListener(this);
    }

    private static void setButtonIcon() {
      if (allowScroll) {
        BUTTON.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_PAUSE));
      } else {
        BUTTON.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_NEXT));
      }
    }

    private static void resetScrollPosition() {
      scrollPosition = 0;
      INCREMENT_SCROLL.accept(0d); // to trigger repaint
    }

    private static void setText(String text) {
      if (PREFERENCE_CHANGED.get()) {
        getPreferences();
        setText(text);
        return;
      }
      resetScrollPosition();
      if (tempTimer.isRunning()) {
        // if a message comes through before a temporary message has reset.
        labelText = oldText;
        tempTimer.stop();
      }

      oldText = labelText;
      if (text == null || text.isBlank() && oldText != null) {
        text = oldText;
      }
      if (TEMP_MESSAGE_CHECK.apply(text.trim())) {
        tempTimer.start();
      }

      labelText = FADE_STRING + text.trim();
      SET_OVERFLOW.accept(labelText);
      MARQUEE_LABEL.setText(labelText);
      // This will need replacing once auto-wrapping tooltips are implemented
      String tipText =
          "<html><p width="
              + (Toolkit.getDefaultToolkit().getScreenSize().width / 3)
              + ">"
              + labelText
              + "</p></html>";
      HOVER_LABEL.setToolTipText(tipText);
      MARQUEE_LABEL.setToolTipText(tipText);
      startTimer();
    }

    private static void initTimer() {
      if (timer == null) {
        timer = new Timer(tickInterval, LISTEN_TIMER);
        timer.setRepeats(true);
      }
      if (tempTimer == null) {
        tempTimer = new Timer(tempDuration, (e) -> setText(oldText));
      }
      resetTimer();
    }

    private static void resetTimer() {
      timer.setDelay(tickInterval);
      timer.setInitialDelay(startDelay);
      timer.setActionCommand("start_delay");
    }

    private static void startTimer() {
      initTimer(); // just to make sure it exists
      if (allowScroll && overflow > 0 && SCROLL_WIDTH.get() > 0) {
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
              case "scroll" -> INCREMENT_SCROLL.accept(scrollSpeed);
              case "stop" -> {
                timer.stop();
                resetTimer();
                resetScrollPosition();
              }
            }

            if ((textDirection == 1 && scrollPosition > overflow)
                || (textDirection == -1 && scrollPosition < -overflow)) {
              timer.stop();
              timer.setInitialDelay(endDelay); // start end hold period
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
            INCREMENT_SCROLL.accept(direction * lastPt.distance(e.getPoint()));
            lastPt = e.getPoint();
          }
        };
    private static final MouseAdapter LISTEN_MOUSE =
        new MouseAdapter() {
          private String oldActionCommand;

          @Override
          public void mouseClicked(MouseEvent e) {
            // was not a drag initiator - we are toggling pause state
            MARQUEE_LABEL.removeMouseMotionListener(LISTEN_DRAG);
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
            MARQUEE_LABEL.addMouseMotionListener(LISTEN_DRAG);
            // store for click event as drag will set it to "pause"
            oldActionCommand = timer.getActionCommand();
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            MARQUEE_LABEL.removeMouseMotionListener(LISTEN_DRAG);
          }
        };

    private static final MouseWheelListener LISTEN_WHEEL =
        new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(MouseWheelEvent e) {
            timer.setActionCommand("pause");
            INCREMENT_SCROLL.accept(e.getPreciseWheelRotation());
          }
        };

    private void setDisplayable(ComponentEvent e) {
      int availableWidth = e.getComponent().getSize().width;
      if (availableWidth < HOVER_LABEL.getWidth()) {
        setSize(HOVER_LABEL.getWidth() + 2, HOVER_LABEL.getHeight() + 2);
      }
      if (availableWidth < BUTTON.getWidth() + 120) {
        // Hover label only
        HOVER_LABEL.setVisible(true);
        BUTTON.setVisible(false);
        SCROLL_PANE.setVisible(false);
        timer.stop();
      } else {
        HOVER_LABEL.setVisible(false);
        BUTTON.setVisible(true);
        SCROLL_PANE.setVisible(true);
        if (!timer.isRunning()) {
          setText(labelText);
        }
      }
    }

    @Override
    public void componentResized(ComponentEvent e) {
      setDisplayable(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      setDisplayable(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
      setDisplayable(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
      timer.stop();
    }

    /**
     * This paints a linear gradient over the start and end of the scroll pane which looks pretty.
     */
    private static class FadingScroll extends JScrollPane {
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        if (labelText == null || labelText.isBlank() || BG == null) {
          return;
        }
        Graphics2D g2d = (Graphics2D) g;
        Rectangle bounds = getVisibleRect();
        float viewW = (float) bounds.getWidth();
        float fadeFraction =
            Math.clamp(
                (float) g2d.getFontMetrics().getStringBounds(FADE_STRING, g2d).getWidth() / viewW,
                0.01f,
                0.2f);
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
     * The normal scroll pane does not scroll the view when the scroll bar is not visible. Manually
     * setting the view position can only be done in integer increments. When animated using a
     * fractional scale this looks jumpy and jarring. This overcomes the problem by applying a
     * transform to the graphics object before normal painting.
     */
    private static class ScrollVP extends JViewport {
      @Override
      public void paint(Graphics g) {
        if ((labelText == null || labelText.isBlank())) {
          return;
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.transform(SCROLL_TRANSFORM);
        super.paint(g2d);
        g2d.dispose();
      }
    }
  }
}
