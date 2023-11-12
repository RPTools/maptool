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
package net.rptools.maptool.client.ui.exportdialog;

import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import net.rptools.lib.net.FTPLocation;
import net.rptools.lib.net.LocalLocation;
import net.rptools.lib.net.Location;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.ZoneImageGenerator;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a dialog for performing a screen capture to a PNG file.
 *
 * <p>This uses a modal dialog based on an Abeille form. It creates a PNG file at the resolution of
 * the 'board' image/tile. The file can be saved to disk or sent to an FTP location.
 */
public class ExportDialog extends JDialog implements IIOWriteProgressListener {
  //
  // Dialog/ UI related vars
  //
  private static final Logger log = LogManager.getLogger(ExportDialog.class);

  private static final ExportDialog instance = new ExportDialog();

  /** the modal panel the user uses to select the screenshot options */
  private static AbeillePanel interactPanel;

  /** The modal panel showing screenshot progress */
  private static JLabel progressLabel;

  /** The place the image will be sent to (file/FTP) */
  private Location exportLocation;

  // These are convenience variables, which should be set
  // each time the dialog is shown. It is safe to
  // cache them like this since the dialog is modal.
  // If this dialog is ever not modal, these need to be
  // factored out.
  private static Zone zone;
  private static ZoneRenderer renderer;

  // These are used to preserve zone settings because
  // we'll change the Zone/ZoneRenderer temporarily to take the screenshot.
  // These are static because we don't expect more than
  // a single ExportDialog to ever be instanced.

  // Pseudo-layers
  private static Zone.VisionType savedVision;
  private static boolean savedFog;
  private static boolean savedBoard;
  // for ZoneRenderer preservation
  private static Rectangle origBounds;
  private static Scale origScale;

  /** set by preScreenshot, cleared by postScreenshot */
  private boolean waitingForPostScreenshot = false;

  /** Only doing this because I don't expect more than one instance of this modal dialog */
  private static int instanceCount = 0;

  //
  // Vars for background rendering of the screenshot
  //

  /** 0-100: percentage of pixels written to destination */
  private int renderPercent;

  //
  // TODO: BUG: transparent objects get less transparent with each render?
  // TODO: BUG: stamps disappearing during and after rendering, come back with movement.
  //

  //
  // TODO: Abeille should auto-generate most of this code:
  // 1. We shouldn't have to synchronize the names of variables manually
  // 2. Specifying the name of a button in Abeille is the same as declaring a variable
  // 3. This code is always the same for every form, aside from the var names
  // 4. JAVA doesn't have a way to do abstract enumerated types, so we can't re-use the code except
  // by copy/paste
  // 5. Abeille seems to be abandonded at this point (July 2010). The owner replied as recently as
  // July 2009, but
  // seems not to have followed up.
  //

  /**
   * This enum is for ALL the radio buttons in the dialog, regardless of their grouping.
   *
   * <p>The names of the enums should be the same as the button names.
   */
  public enum ExportRadioButtons {
    // Format of enum declaration:
    // [Abeille Forms Designer button name] (default checked, default enabled)
    // Button Group 1 (not that it matters for this controller)
    TYPE_CURRENT_VIEW,
    TYPE_ENTIRE_MAP,
    // Button Group 2
    VIEW_GM,
    VIEW_PLAYER,
    // Button Group 3
    LAYERS_CURRENT,
    LAYERS_AS_SELECTED;

    private static AbeillePanel form;

    //
    // SetForm stores the form this is attached to
    //
    public static void setForm(AbeillePanel form) {
      ExportRadioButtons.form = form;

      for (ExportRadioButtons button : ExportRadioButtons.values()) {
        try {
          if (form.getRadioButton(button.toString()) == null) {
            throw new Exception("Export Dialog has a mis-matched enum: " + button.toString());
          }
          button.addActionListener(evt -> enforceButtonRules());
        } catch (Exception ex) {
          MapTool.showError("dialog.screenshot.radio.button.uiImplementationError", ex);
        }
      }
    }

    //
    // Generic utility methods
    // NON-Static
    //
    public void setChecked(boolean checked) {
      form.getRadioButton(this.toString()).setSelected(checked);
    }

    public boolean isChecked() {
      return form.getRadioButton(this.toString()).isSelected();
    }

    public void setEnabled(boolean enabled) {
      form.getRadioButton(this.toString()).setEnabled(enabled);
    }

    /**
     * Shortcut to allow clean code and type-checking of invocations of specific buttons
     *
     * @param listener an instance to get callbacks for actions
     */
    public void addActionListener(ActionListener listener) {
      form.getRadioButton(this.toString()).addActionListener(listener);
    }

    /**
     * @return which of the buttons in the Type group is selected
     */
    public static ExportRadioButtons getType() {
      if (ExportRadioButtons.TYPE_CURRENT_VIEW.isChecked()) {
        return TYPE_CURRENT_VIEW;
      } else if (ExportRadioButtons.TYPE_ENTIRE_MAP.isChecked()) {
        return TYPE_ENTIRE_MAP;
      }
      return null;
    }

    /**
     * @return which of the buttons in the View group is selected
     */
    public static ExportRadioButtons getView() {
      if (ExportRadioButtons.VIEW_GM.isChecked()) {
        return VIEW_GM;
      } else if (ExportRadioButtons.VIEW_PLAYER.isChecked()) {
        return VIEW_PLAYER;
      }
      return null;
    }

    /**
     * @return which of the buttons in the Layers group is selected
     */
    public static ExportRadioButtons getLayers() {
      if (ExportRadioButtons.LAYERS_CURRENT.isChecked()) {
        return LAYERS_CURRENT;
      } else if (ExportRadioButtons.LAYERS_AS_SELECTED.isChecked()) {
        return LAYERS_AS_SELECTED;
      }
      return null;
    }
  }

  /**
   * This enum is for all the checkboxes which select layers.
   *
   * <p>The names of the enums should be the same as the button names.
   */
  private enum ExportLayers {
    // enum_val (fieldName as per Abeille Forms Designer, playerCanModify)
    LAYER_TOKEN(true),
    LAYER_HIDDEN(false),
    LAYER_OBJECT(false),
    LAYER_BACKGROUND(false),
    LAYER_BOARD(false),
    LAYER_FOG(false),
    LAYER_VISIBILITY(true);

    private static AbeillePanel form;

    private final boolean playerCanModify;

    /**
     * Constructor, sets rules for export of this layer. 'Player' is in reference to the Role type
     * (Player vs. GM).
     */
    ExportLayers(boolean playerCanModify) {
      this.playerCanModify = playerCanModify;
    }

    /**
     * Stores the form this is attached to, so we don't have to store duplicate data locally (like
     * selected and enabled). Also perform some error checking, since we _are_ duplicating the
     * description of the form itself (like what buttons it has).
     *
     * @param form The FormPanel this dialog is part of.
     */
    public static void setForm(AbeillePanel form) {
      ExportLayers.form = form;
      for (ExportLayers button : ExportLayers.values()) {
        try {
          if (form.getButton(button.toString()) == null) {
            throw new Exception("Export Dialog has a mis-matched enum: " + button.toString());
          }
        } catch (Exception ex) {
          MapTool.showError(
              I18N.getString("dialog.screenshot.layer.button.uiImplementationError"), ex);
        }
      }
    }

    //
    // Misc utility methods
    //

    public void setChecked(boolean checked) {
      form.getButton(this.toString()).setSelected(checked);
    }

    public boolean isChecked() {
      return form.getButton(this.toString()).isSelected();
    }

    public void setEnabled(boolean enabled) {
      form.getButton(this.toString()).setEnabled(enabled);
    }

    /** Sets the layer-selection checkboxes to replicate the "current view". */
    public void setToDefault() {
      final Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      if (this == ExportLayers.LAYER_FOG) {
        ExportLayers.LAYER_FOG.setChecked(zone.hasFog());
      } else if (this == ExportLayers.LAYER_VISIBILITY) {
        ExportLayers.LAYER_VISIBILITY.setChecked(zone.getVisionType() != Zone.VisionType.OFF);
      } else {
        setChecked(true);
      }
    }

    public static void setDefaultChecked() {
      // everything defaults to 'on' since the layers don't really have on/off capability
      // outside of this screenshot code
      for (ExportLayers layer : ExportLayers.values()) {
        layer.setChecked(true);
      }
      // however, some pseudo-layers do have a state, so set that appropriately
      final Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      ExportLayers.LAYER_VISIBILITY.setChecked(zone.getVisionType() != Zone.VisionType.OFF);
      ExportLayers.LAYER_FOG.setChecked(zone.hasFog());
    }

    public static void setDisabled() {
      for (ExportLayers layer : ExportLayers.values()) {
        layer.setEnabled(false);
      }
    }
  }

  /**
   * Ensures that the user can only check/uncheck boxes as appropriate. For example, if "fog" is not
   * enabled on the map, it cannot be enabled for export.
   *
   * <p>This should get called during initialization and whenever the radio buttons change.
   *
   * <p>The GM and Players have different rules, to prevent players from gaining knowledge they
   * should not have using the screenshot (such as revealing things under other things by disabling
   * layers). Players can basically only turn off tokens, to get an 'empty' version of the map.
   */
  public static void enforceButtonRules() {
    if (!MapTool.getPlayer().isGM()) {
      ExportRadioButtons.VIEW_PLAYER.setChecked(true);
      ExportRadioButtons.VIEW_PLAYER.setEnabled(true);
      ExportRadioButtons.VIEW_GM.setEnabled(false);
    }
    if (ExportRadioButtons.LAYERS_CURRENT.isChecked()) {
      // By "current layers" we mean what you see in the editor, which is everything.
      // So disable mucking about with layers.
      interactPanel.getLabel("LAYERS_LABEL").setEnabled(false);
      ExportLayers.setDefaultChecked();
      ExportLayers.setDisabled();
    } else /* if (ExportRadioButtons.LAYERS_AS_SELECTED.isChecked()) */ {
      interactPanel.getLabel("LAYERS_LABEL").setEnabled(true);
      boolean isGM = ExportRadioButtons.VIEW_GM.isChecked();
      final Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

      for (ExportLayers layer : ExportLayers.values()) {
        boolean enabled = isGM || layer.playerCanModify;
        // Regardless of whether it is a player or GM,
        // only enable fog and visibility check-boxes
        // when the map has those things turned on.
        switch (layer) {
          case LAYER_VISIBILITY:
            enabled &= (zone.getVisionType() != Zone.VisionType.OFF);
            break;
          case LAYER_FOG:
            enabled &= zone.hasFog();
            break;
        }
        layer.setEnabled(enabled);
        if (!enabled) {
          layer.setToDefault();
        }
      }
    }
  }

  public static ExportDialog getInstance() {
    return instance;
  }

  private ExportDialog() {
    super(MapTool.getFrame(), I18N.getText("action.exportScreenShot.title"), true);

    // The window uses about 1MB. Disposing frees this, but repeated uses
    // will cause more memory fragmentation.
    // MCL: I figure it's better to save the 1MB for low-mem systems,
    // but it would be even better to HIDE it, and then dispose() it
    // when the user clicks on the memory meter to free memory
    // setDefaultCloseOperation(HIDE_ON_CLOSE);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    //
    // Initialize the panel and button actions
    //
    createWaitPanel();
    interactPanel = new AbeillePanel(new ExportDialogView().getRootComponent());
    setLayout(new GridLayout());
    add(interactPanel);
    getRootPane().setDefaultButton((JButton) interactPanel.getButton("exportButton"));
    pack();

    ExportRadioButtons.setForm(interactPanel);
    ExportLayers.setForm(interactPanel);

    interactPanel.getButton("exportButton").addActionListener(evt -> exportButtonAction());
    interactPanel.getButton("cancelButton").addActionListener(evt -> dispose());
    interactPanel.getButton("browseButton").addActionListener(evt -> browseButtonAction());
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      // Always call this first, since other methods may rely on zone or renderer being set.
      setZone(MapTool.getFrame().getCurrentZoneRenderer().getZone());
      setZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());

      // Set to interactive mode
      switchToInteractPanel();

      // In case something changed while the dialog was closed...
      enforceButtonRules();

      SwingUtil.centerOver(this, MapTool.getFrame());
    } else {
      if (waitingForPostScreenshot) {
        postScreenshot();
      }
    }
    super.setVisible(b);
  }

  //
  // These get/set the convenience variables zone and renderer
  //
  public static void setZone(Zone zone) {
    ExportDialog.zone = zone;
  }

  public static ZoneRenderer getZoneRenderer() {
    return renderer;
  }

  public static void setZoneRenderer(ZoneRenderer renderer) {
    ExportDialog.renderer = renderer;
  }

  public static Zone getZone() {
    return zone;
  }

  private void exportButtonAction() {
    // This block is to allow preservation of existing dialog behavior:
    // Neither button is set when the dialog first appears, so we have to
    // make sure the user picks one. Presumably this is to force the user
    // to pay attention to this choice and not just accept a default.
    if (!(ExportRadioButtons.VIEW_GM.isChecked() || ExportRadioButtons.VIEW_PLAYER.isChecked())) {
      MapTool.showError(I18N.getString("dialog.screenshot.error.mustSelectView"), null);
      return;
    }
    // LOCATION
    // TODO: Show a progress dialog
    // TODO: Make this less fragile
    switch (interactPanel.getTabbedPane("tabs").getSelectedIndex()) {
      case 0:
        File file = new File(interactPanel.getTextField("locationTextField").getText().trim());

        // PNG only supported for now
        if (file.getName().endsWith("/")) {
          MapTool.showError("Filename must not end with a slash ('/')");
          return;
        } else if (!file.getName().toLowerCase().endsWith(".png")) {
          file = new File(file.getAbsolutePath() + ".png");
        }
        exportLocation = new LocalLocation(file);
        break;
      case 1:
        String username = interactPanel.getTextField("username").getText().trim();
        String password = interactPanel.getTextField("password").getText().trim();
        String host = interactPanel.getTextField("host").getText().trim();
        String path = interactPanel.getTextField("path").getText().trim();

        // PNG only supported for now
        if (path.endsWith("/")) {
          MapTool.showError("Path must not end with a slash ('/')");
          return;
        } else if (!path.toLowerCase().endsWith(".png")) {
          path += ".png";
        }
        exportLocation = new FTPLocation(username, password, host, path);
        break;
    }
    try {
      screenCapture();
    } catch (Exception ex) {
      MapTool.showError(I18N.getString("dialog.screenshot.error.failedExportingImage"), ex);
    } finally {
      dispose();
    }
  }

  public void browseButtonAction() {
    JFileChooser chooser = new JFileChooser();
    if (exportLocation instanceof LocalLocation) {
      chooser.setSelectedFile(((LocalLocation) exportLocation).getFile());
    }
    if (chooser.showOpenDialog(ExportDialog.this) == JFileChooser.APPROVE_OPTION) {
      interactPanel
          .getTextField("locationTextField")
          .setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  /**
   * This is the top-level screen-capture routine. It sends the resulting PNG image to the location
   * previously selected by the user. TODO: It currently calls {@link MapTool#takeMapScreenShot} for
   * "normal" screenshots, but that's just until this code is considered stable enough.
   *
   * @throws Exception if unable to take screen capture
   */
  public void screenCapture() throws Exception {
    MapTool.getFrame()
        .setStatusMessage(I18N.getString("dialog.screenshot.msg.GeneratingScreenshot"));
    ExportRadioButtons type = ExportRadioButtons.getType();
    if (type == null) {
      throw new Exception(I18N.getString("dialog.screenshot.error.invalidDialogSettings"));
    }
    Player.Role role;
    try {
      switch (type) {
        case TYPE_CURRENT_VIEW:
          // This uses the original screenshot code: I didn't want to touch it, so I need
          // to pass it the same parameter it took before.
          role = ExportRadioButtons.VIEW_GM.isChecked() ? Player.Role.GM : Player.Role.PLAYER;
          BufferedImage screenCap = MapTool.takeMapScreenShot(renderer.getPlayerView(role));
          // since old screenshot code doesn't throw exceptions, look for null
          if (screenCap == null) {
            throw new Exception(I18N.getString("dialog.screenshot.error.failedImageGeneration"));
          }
          MapTool.getFrame()
              .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotStreaming"));
          try (ByteArrayOutputStream imageOut = new ByteArrayOutputStream()) {
            ImageIO.write(screenCap, "png", imageOut);
            screenCap = null; // Free up the memory as soon as possible
            MapTool.getFrame()
                .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaving"));
            exportLocation.putContent(
                new BufferedInputStream(new ByteArrayInputStream(imageOut.toByteArray())));
          }
          MapTool.getFrame()
              .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaved"));
          break;
        case TYPE_ENTIRE_MAP:
          switchToWaitPanel();
          if (interactPanel.getRadioButton("METHOD_BUFFERED_IMAGE").isSelected()
              || interactPanel.getRadioButton("METHOD_IMAGE_WRITER").isSelected()) {
            // Using a buffer in memory for the whole image
            try {
              final PlayerView view = preScreenshot();
              final ImageWriter pngWriter = ImageIO.getImageWritersByFormatName("png").next();
              MapTool.getFrame()
                  .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotStreaming"));

              BufferedImage image;
              if (interactPanel.getRadioButton("METHOD_BUFFERED_IMAGE").isSelected()) {
                image =
                    new BufferedImage(
                        renderer.getWidth(), renderer.getHeight(), Transparency.OPAQUE);
                final Graphics2D g = image.createGraphics();
                // g.setClip(0, 0, renderer.getWidth(), renderer.getHeight());
                renderer.renderZone(g, view);
                g.dispose();
              } else {
                image = new ZoneImageGenerator(renderer, view);
              }
              // putContent() can consume quite a bit of time; really should have a progress
              // meter of some kind here.
              exportLocation.putContent(pngWriter, image);
              if (image instanceof ZoneImageGenerator) {
                log.debug("ZoneImageGenerator() stats: " + image.toString());
              }
              MapTool.getFrame()
                  .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaving"));
            } catch (Exception e) {
              MapTool.getFrame()
                  .setStatusMessage(
                      I18N.getString("dialog.screenshot.error.failedImageGeneration"));
            } finally {
              postScreenshot();
              MapTool.getFrame()
                  .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaved"));
            }
          } else if (interactPanel.getRadioButton("METHOD_BACKGROUND").isSelected()) {
            // We must call preScreenshot before creating the ZoneImageGenerator, because
            // ZoneImageGenerator uses the ZoneRenderer's bounds to set itself up

            MapTool.showError("This doesn't work! Try one of the other methods.", null);
            if (false) {
              //
              // Note: this implementation is the obvious way, which doesn't work, since
              // ZoneRenderer is part of the Swing component chain, and the threads get deadlocked.
              //
              // The suggested implementation by
              // "Reiger" at http://ubuntuforums.org/archive/index.php/t-1455270.html
              // might work... except that it would have to be part of ZoneRenderer
              //
              // The only way to make this really work is to pull the renderZone function
              // out of ZoneRenderer into a new class: call it ZoneRasterizer. Then make
              // ZoneRenderer create an instance of it, and patch up the code to make it
              // compatible. Then we can create an instance of ZoneRasterizer, and run it
              // in a separate thread, since it won't lock in any of the functions that
              // Swing uses.
              //
              class backscreenRender implements Runnable {
                public void run() {
                  try {
                    PlayerView view = preScreenshot();
                    final ZoneImageGenerator zoneImageGenerator =
                        new ZoneImageGenerator(renderer, view);
                    final ImageWriter pngWriter = ImageIO.getImageWritersByFormatName("png").next();
                    exportLocation.putContent(pngWriter, zoneImageGenerator);
                    // postScreenshot is called by the callback imageComplete()
                  } catch (Exception e) {
                    assert false
                        : "Unhandled Exception in renderOffScreen: '" + e.getMessage() + "'";
                  }
                }
              }
              backscreenRender p = new backscreenRender();
              new Thread(p).start();
              repaint();
            }
          } else {
            throw new Exception("Unknown rendering method!");
          }
          break;
        default:
          throw new Exception(I18N.getString("dialog.screenshot.error.invalidDialogSettings"));
      }
    } catch (OutOfMemoryError e) {
      MapTool.showError("screenCapture() caught: Out Of Memory", e);
    }
  }

  public Map<String, Boolean> getExportSettings() {
    Map<String, Boolean> settings = new HashMap<>(16);
    for (var component : interactPanel.getAllCompoments()) {
      if (component instanceof JToggleButton jtb) {
        settings.put(jtb.getName(), jtb.isSelected());
      }
    }
    return settings;
  }

  /**
   * Turn off all JToggleButtons on the form. We don't care if we turn off fields that are normally
   * turned on, since {@link #enforceButtonRules()} will turn them back on as appropriate.
   */
  private void resetExportSettings() {
    for (var component : interactPanel.getAllCompoments()) {
      if (component instanceof JToggleButton jtb) {
        jtb.setSelected(false);
      }
    }
  }

  public void setExportSettings(Map<String, Boolean> settings) {
    resetExportSettings();
    if (settings != null) {
      for (var entry : settings.entrySet()) {
        JToggleButton jtb = (JToggleButton) interactPanel.getComponent(entry.getKey());
        if (jtb == null) {
          log.warn("GUI component for export setting '" + entry.getKey() + "' not found.");
        } else {
          jtb.setSelected(entry.getValue());
        }
      }
    }
  }

  public Location getExportLocation() {
    return exportLocation;
  }

  public void setExportLocation(Location loc) {
    exportLocation = loc;
  }

  /**
   * This is a preserves the layer settings on the Zone object. It should be followed by
   * restoreZone()
   */
  private static void setupZoneLayers() throws OutOfMemoryError {
    final Zone zone = renderer.getZone();

    //
    // Preserve settings
    //
    // pseudo-layers
    savedVision = zone.getVisionType();
    savedFog = zone.hasFog();
    savedBoard = zone.drawBoard();

    //
    // set according to dialog options
    //
    zone.setHasFog(ExportLayers.LAYER_FOG.isChecked());
    if (!ExportLayers.LAYER_VISIBILITY.isChecked()) zone.setVisionType(Zone.VisionType.OFF);
    zone.setDrawBoard(ExportLayers.LAYER_BOARD.isChecked());

    if (!ExportLayers.LAYER_TOKEN.isChecked()) {
      renderer.disableLayer(Zone.Layer.TOKEN);
    }
    if (!ExportLayers.LAYER_HIDDEN.isChecked()) {
      renderer.disableLayer(Zone.Layer.GM);
    }
    if (!ExportLayers.LAYER_OBJECT.isChecked()) {
      renderer.disableLayer(Zone.Layer.OBJECT);
    }
    if (!ExportLayers.LAYER_BACKGROUND.isChecked()) {
      renderer.disableLayer(Zone.Layer.BACKGROUND);
    }
  }

  /** This restores the layer settings on the Zone object. It should follow setupZoneLayers(). */
  private static void restoreZoneLayers() {
    zone.setHasFog(savedFog);
    zone.setVisionType(savedVision);
    zone.setDrawBoard(savedBoard);
    renderer.restoreLayers();
  }

  /**
   * Finds the extents of the map, sets up zone to be captured. If the user is the GM, the extents
   * include every object and everything that has any area, such as 'fog' and 'visibility' objects.
   *
   * <p>If a background tiling texture is used, the image is aligned to it, so that it can be used
   * on re-import as a new base map image.
   *
   * <p>If the user is a player (or GM posing as a player), the extents only go as far as the
   * revealed fog-of-war.
   *
   * <p>Must be followed by postScreenshot at some point, or the Zone will be messed up.
   *
   * @return the image to be saved
   */
  private PlayerView preScreenshot() throws Exception, OutOfMemoryError {
    assert (!waitingForPostScreenshot) : "preScreenshot() called twice in a row!";

    // Save the original state of the renderer to restore later.
    // Create a place to put the image, and
    // set up the renderer to encompass the whole extents of the map.

    origBounds = renderer.getBounds();
    origScale = renderer.getZoneScale();

    setupZoneLayers();
    boolean viewAsPlayer = ExportRadioButtons.VIEW_PLAYER.isChecked();

    // First, figure out the 'extents' of the canvas
    // This will be later modified by the fog (for players),
    // and by the tiling texture (for re-importing)
    //
    Player.Role viewRole = viewAsPlayer ? Player.Role.PLAYER : Player.Role.GM;
    PlayerView view = renderer.getPlayerView(viewRole, false);
    Rectangle extents = renderer.zoneExtents(view);
    try {
      // Clip to what the players know about (if applicable).
      // This keeps the player from exporting the map to learn which
      // direction has more 'stuff' in it.
      if (viewAsPlayer && renderer.getZone().hasFog()) {
        Rectangle fogE = renderer.getZone().getExposedArea(view).getBounds();
        // MapTool.showError(fogE.x + " " + fogE.y + " " + fogE.width + " " + fogE.height);
        if ((fogE.width < 0) || (fogE.height < 0)) {
          MapTool.showError(
              I18N.getString("dialog.screenshot.error.negativeFogExtents")); // Image is not
          // clipped to
          // show only
          // fog-revealed
          // areas!"));
        } else {
          extents = extents.intersection(fogE);
        }
      }
    } catch (Exception ex) {
      throw (new Exception(I18N.getString("dialog.screenshot.error.noArea"), ex));
    }
    if ((extents == null) || (extents.width == 0) || (extents.height == 0)) {
      throw (new Exception(I18N.getString("dialog.screenshot.error.noArea")));
    }

    // If output includes the tiling 'board' texture, move the upper-left corner
    // to an integer multiple of the background tile (so it matches up on import).
    // We don't need to move the lower-right corner because it doesn't matter for
    // aligning on importing.

    boolean drawBoard = ExportLayers.LAYER_BOARD.isChecked();
    if (drawBoard) {
      DrawablePaint paint = renderer.getZone().getBackgroundPaint();
      DrawableTexturePaint dummy = new DrawableTexturePaint();
      int tileX = 0, tileY = 0;

      if (paint.getClass() == dummy.getClass()) {
        Image bgTexture =
            ImageManager.getImage(((DrawableTexturePaint) paint).getAsset().getMD5Key());
        tileX = bgTexture.getWidth(null);
        tileY = bgTexture.getHeight(null);
        int x = ((int) Math.floor((float) extents.x / tileX)) * tileX;
        int y = ((int) Math.floor((float) extents.y / tileY)) * tileY;
        extents.width = extents.width + (extents.x - x);
        extents.height = extents.height + (extents.y - y);
        extents.x = x;
        extents.y = y;
      }
    }

    // Rescale the bounds to match the view scale
    double scale = renderer.getScale();
    extents.setLocation((int) (extents.x * scale), (int) (extents.y * scale));
    extents.setSize((int) (extents.width * scale), (int) (extents.height * scale));

    // Setup the renderer to use the new extents
    Scale s = new Scale();
    s.setOffset(-extents.x, -extents.y);
    s.setScale(scale);
    renderer.setZoneScale(s);
    renderer.setBounds(extents);

    waitingForPostScreenshot = true;
    return view;
  }

  private void postScreenshot() {
    assert waitingForPostScreenshot : "postScrenshot called without preScreenshot";

    renderer.setBounds(origBounds);
    renderer.setZoneScale(origScale);
    restoreZoneLayers();
    waitingForPostScreenshot = false;
  }

  //
  // Panel related functions
  //

  private void switchToWaitPanel() {
    // remove(interactPanel);
    // add(waitPanel);
    // getRootPane().setDefaultButton(null);
    // pack();
  }

  private void switchToInteractPanel() {
    // remove(waitPanel);
    // add(interactPanel);
    // getRootPane().setDefaultButton((JButton) interactPanel.getButton("exportButton"));
    // pack();
  }

  private void createWaitPanel() {
    progressLabel = new JLabel();
    imageProgress(null, 0);
  }

  //
  // IIOWriteProgressListener Interface
  //

  /** Setup the progress meter. */
  public void imageStarted(ImageWriter source, int imageIndex) {
    renderPercent = 0;
    progressLabel.setText(I18N.getText("exportDialog.msg.renderingWait" + renderPercent + "%"));
    repaint();
  }

  /** Update the progress meter. */
  public void imageProgress(ImageWriter source, float percentageDone) {
    int oldPercent = renderPercent;
    renderPercent = (int) (percentageDone * 100);
    if (renderPercent > oldPercent) {
      progressLabel.setText(I18N.getText("exportDialog.msg.renderingWait" + renderPercent + "%"));
      repaint();
    }
  }

  /** Close this dialog box upon completion of background thread renderer. */
  public void imageComplete(ImageWriter source) {
    postScreenshot();
    dispose();
  }

  public void thumbnailStarted(ImageWriter source, int imageIndex, int thumbnailIndex) {}

  public void thumbnailProgress(ImageWriter source, float percentageDone) {}

  public void thumbnailComplete(ImageWriter source) {}

  public void writeAborted(ImageWriter source) {}
}
