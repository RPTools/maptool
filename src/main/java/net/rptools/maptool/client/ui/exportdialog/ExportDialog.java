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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.*;
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
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.*;
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
public class ExportDialog extends JDialog {

  //
  // Dialog/ UI related vars
  //
  private static final Logger log = LogManager.getLogger(ExportDialog.class);

  /** the modal panel the user uses to select the screenshot options */
  private static AbeillePanel interactPanel;

  /** The place the image will be sent to (file/FTP) */
  private Location exportLocation;

  private final Zone zone;
  private final ZoneRenderer renderer;

  /**
   * This enum is for ALL the radio buttons in the dialog, regardless of their grouping.
   *
   * <p>The names of the enums should be the same as the button names.
   */
  private enum ExportRadioButtons {
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
  private static final class ExportLayers {

    private static AbeillePanel form;
    private static final List<ExportLayers> values;

    private static final ExportLayers LAYER_BOARD;
    private static final ExportLayers LAYER_FOG;
    private static final ExportLayers LAYER_VISIBILITY;

    static {
      values = new ArrayList<>();

      // Include options for all zone layers.
      for (final var layer : Zone.Layer.values()) {
        values.add(new ExportLayers("LAYER_" + layer.name(), layer.isPlayerLayer(), layer));
      }

      // Also control some "pseudo-layers".
      values.add(LAYER_BOARD = new ExportLayers("LAYER_BOARD", false, null));
      values.add(LAYER_FOG = new ExportLayers("LAYER_FOG", false, null));
      values.add(LAYER_VISIBILITY = new ExportLayers("LAYER_VISIBILITY", true, null));
    }

    public static ExportLayers[] values() {
      return values.toArray(ExportLayers[]::new);
    }

    private final String name;
    private final boolean playerCanModify;
    private final @Nullable Zone.Layer associatedZoneLayer;

    private ExportLayers(
        String name, boolean playerCanModify, @Nullable Zone.Layer associatedZoneLayer) {
      this.name = name;
      this.playerCanModify = playerCanModify;
      this.associatedZoneLayer = associatedZoneLayer;
    }

    public String name() {
      return name;
    }

    public String toString() {
      return name();
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
  private static void enforceButtonRules() {
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
        if (layer == ExportLayers.LAYER_VISIBILITY) {
          enabled &= (zone.getVisionType() != Zone.VisionType.OFF);
        }
        if (layer == ExportLayers.LAYER_FOG) {
          enabled &= zone.hasFog();
        }
        layer.setEnabled(enabled);
        if (!enabled) {
          layer.setToDefault();
        }
      }
    }
  }

  public ExportDialog(ZoneRenderer renderer) {
    super(MapTool.getFrame(), I18N.getText("action.exportScreenShot.title"), true);

    this.renderer = renderer;
    this.zone = renderer.getZone();

    // The window uses about 1MB. Disposing frees this, but repeated uses
    // will cause more memory fragmentation.
    // MCL: I figure it's better to save the 1MB for low-mem systems,
    // but it would be even better to HIDE it, and then dispose() it
    // when the user clicks on the memory meter to free memory
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    //
    // Initialize the panel and button actions
    //
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

    // Escape key
    interactPanel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    interactPanel
        .getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                dispose();
              }
            });
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      // Set to interactive mode
      switchToInteractPanel();

      // In case something changed while the dialog was closed...
      enforceButtonRules();

      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
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

  private void browseButtonAction() {
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
   * previously selected by the user.
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
    try {
      // Using a buffer in memory for the whole image
      try (var resetView = restoreZoneState()) {
        if (!interactPanel.getRadioButton("METHOD_BUFFERED_IMAGE").isSelected()
            && !interactPanel.getRadioButton("METHOD_IMAGE_WRITER").isSelected()) {
          throw new Exception("Unknown rendering method!");
        }

        var playerView =
            renderer.makePlayerView(
                ExportRadioButtons.VIEW_PLAYER.isChecked() ? Player.Role.PLAYER : Player.Role.GM,
                false);

        switchToWaitPanel();
        setupZoneLayers();

        if (type == ExportRadioButtons.TYPE_ENTIRE_MAP) {
          setRendererView(playerView);
        }

        doScreenshot(playerView);

        MapTool.getFrame()
            .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaved"));
      } catch (Exception e) {
        MapTool.getFrame()
            .setStatusMessage(I18N.getString("dialog.screenshot.error.failedImageGeneration"));
      }
    } catch (OutOfMemoryError e) {
      MapTool.showError("screenCapture() caught: Out Of Memory", e);
    }
  }

  private void doScreenshot(PlayerView view) throws IOException {
    final ImageWriter pngWriter = ImageIO.getImageWritersByFormatName("png").next();
    MapTool.getFrame()
        .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotStreaming"));

    BufferedImage image;
    if (interactPanel.getRadioButton("METHOD_BUFFERED_IMAGE").isSelected()) {
      image = new BufferedImage(renderer.getWidth(), renderer.getHeight(), Transparency.OPAQUE);
      final Graphics2D g = image.createGraphics();
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
    MapTool.getFrame().setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaving"));
  }

  public Map<String, Boolean> getExportSettings() {
    Map<String, Boolean> settings = new HashMap<>(16);
    for (var component : interactPanel.getAllComponents()) {
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
    for (var component : interactPanel.getAllComponents()) {
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
  private void setupZoneLayers() {
    final Zone zone = renderer.getZone();

    //
    // set according to dialog options
    //
    zone.setHasFog(ExportLayers.LAYER_FOG.isChecked());
    if (!ExportLayers.LAYER_VISIBILITY.isChecked()) {
      zone.setVisionType(Zone.VisionType.OFF);
    }

    if (!ExportLayers.LAYER_BOARD.isChecked()) {
      renderer.disableBoard();
    }
    for (ExportLayers exportLayer : ExportLayers.values()) {
      if (exportLayer.associatedZoneLayer != null && !exportLayer.isChecked()) {
        renderer.disableLayer(exportLayer.associatedZoneLayer);
      }
    }
  }

  private AutoCloseable restoreZoneState() {
    // Preserve settings of the zone and renderer.
    Zone.VisionType savedVision = zone.getVisionType();
    boolean savedFog = zone.hasFog();
    var origBounds = renderer.getBounds();
    var origScale = renderer.getViewModel().getZoneScale();

    return () -> {
      zone.setHasFog(savedFog);
      zone.setVisionType(savedVision);
      renderer.restoreLayers();
      renderer.getViewModel().setZoneScale(origScale);
      renderer.setBounds(origBounds);
    };
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
   */
  private void setRendererView(PlayerView view) throws Exception {
    // Create a place to put the image, and
    // set up the renderer to encompass the whole extents of the map.

    // First, figure out the 'extents' of the canvas
    // This will be later modified by the fog (for players),
    // and by the tiling texture (for re-importing)
    Rectangle extents = zoneExtents(view);
    try {
      // Clip to what the players know about (if applicable).
      // This keeps the player from exporting the map to learn which
      // direction has more 'stuff' in it.
      if (!view.isGMView() && renderer.getZone().hasFog()) {
        Rectangle fogE = renderer.getZone().getExposedArea(view).getBounds();
        if ((fogE.width < 0) || (fogE.height < 0)) {
          // Image is not clipped to show only fog-revealed areas!
          MapTool.showError(I18N.getString("dialog.screenshot.error.negativeFogExtents"));
        } else {
          extents = extents.intersection(fogE);
        }
      }
    } catch (Exception ex) {
      throw new Exception(I18N.getString("dialog.screenshot.error.noArea"), ex);
    }
    if ((extents == null) || (extents.width == 0) || (extents.height == 0)) {
      throw new Exception(I18N.getString("dialog.screenshot.error.noArea"));
    }

    // If output includes the tiling 'board' texture, move the upper-left corner
    // to an integer multiple of the background tile (so it matches up on import).
    // We don't need to move the lower-right corner because it doesn't matter for
    // aligning on importing.

    boolean drawBoard = ExportLayers.LAYER_BOARD.isChecked();
    if (drawBoard
        && renderer.getZone().getBackgroundPaint() instanceof DrawableTexturePaint texturePaint) {
      Image bgTexture = ImageManager.getImage(texturePaint.getAsset().getMD5Key());
      int tileX = bgTexture.getWidth(null);
      int tileY = bgTexture.getHeight(null);
      int x = ((int) Math.floor((float) extents.x / tileX)) * tileX;
      int y = ((int) Math.floor((float) extents.y / tileY)) * tileY;
      extents.width = extents.width + (extents.x - x);
      extents.height = extents.height + (extents.y - y);
      extents.x = x;
      extents.y = y;
    }

    // Rescale the bounds to match the view scale
    Scale originalZoneScale = renderer.getViewModel().getZoneScale();
    double scale = originalZoneScale.getScale();
    extents.setLocation((int) (extents.x * scale), (int) (extents.y * scale));
    extents.setSize((int) (extents.width * scale), (int) (extents.height * scale));

    // Setup the renderer to use the new extents
    Scale s = originalZoneScale.withOffset(-extents.x, -extents.y);
    renderer.getViewModel().setZoneScale(s);
    renderer.setBounds(extents);
  }

  private Rectangle fogExtents() {
    return zone.getExposedArea().getBounds();
  }

  /**
   * Get a bounding box, in Zone coordinates, of all the elements in the zone. This method was
   * created by copying renderZone() and then replacing each bit of rendering with a routine to
   * simply aggregate the extents of the object that would have been rendered.
   *
   * @param view the player view
   * @return a new Rectangle with the bounding box of all the elements in the Zone
   */
  private Rectangle zoneExtents(PlayerView view) {
    // Can't initialize extents to any set x/y values, because
    // we don't know if the actual map contains that x/y.
    // So we need a flag to say extents is 'unset', and the best I
    // could come up with is checking for 'null' on each loop iteration.
    Rectangle extents = null;

    // We don't iterate over the layers in the same order as rendering
    // because its cleaner to group them by type and the order doesn't matter.

    // First background image extents
    if (zone.getMapAssetId() != null) {
      extents =
          new Rectangle(
              zone.getBoardX(),
              zone.getBoardY(),
              ImageManager.getImage(zone.getMapAssetId(), this).getWidth(),
              ImageManager.getImage(zone.getMapAssetId(), this).getHeight());
    }
    // next, extents of drawing objects
    List<DrawnElement> drawableList = zone.getAllDrawnElements();
    for (DrawnElement element : drawableList) {
      if (!view.isGMView() && !element.getDrawable().getLayer().isVisibleToPlayers()) {
        continue;
      }

      Drawable drawable = element.getDrawable();
      Rectangle drawnBounds = new Rectangle(drawable.getBounds(zone));

      // Handle pen size
      // This slightly over-estimates the size of the pen, but we want to
      // make sure to include the anti-aliased edges.
      Pen pen = element.getPen();
      int penSize = (int) Math.ceil((pen.getThickness() / 2) + 1);
      drawnBounds.setBounds(
          drawnBounds.x - penSize,
          drawnBounds.y - penSize,
          drawnBounds.width + (penSize * 2),
          drawnBounds.height + (penSize * 2));

      if (extents == null) {
        extents = drawnBounds;
      } else {
        extents.add(drawnBounds);
      }
    }
    // now, add the stamps/tokens
    // tokens and stamps are the same thing, just treated differently

    // Note: order doesn't matter, so don't need to go back-to-front.
    for (Token element :
        zone.getTokensForLayers(layer -> view.isGMView() || layer.isVisibleToPlayers())) {
      Rectangle drawnBounds = element.getImageBounds(zone);
      if (element.hasFacing()) {
        // Get the facing and do a quick fix to make the math easier: -90 is 'unrotated' for some
        // reason
        int facing = element.getFacing() + 90;
        if (facing > 180) {
          facing -= 360;
        }
        // if 90 degrees, just swap w and h
        // also swap them if rotated more than 90 (optimization for non-90deg rotations)
        if (facing != 0 && facing != 180) {
          if (Math.abs(facing) >= 90) {
            drawnBounds.setSize(drawnBounds.height, drawnBounds.width); // swapping h and w
          }
          // if rotated to non-axis direction, assume the worst case 45 deg
          // also assumes the rectangle rotates around its center
          // This will usually make the bounds bigger than necessary, but its quick.
          // Also, for quickness, we assume its a square token using the larger dimension
          // At 45 deg, the bounds of the square will be sqrt(2) bigger, and the UL corner will
          // shift by 1/2 of the length.
          // The size increase is: (sqrt*(2) - 1) * size ~= 0.42 * size.
          if (facing != 0 && facing != 180 && facing != 90 && facing != -90) {
            int size = Math.max(drawnBounds.width, drawnBounds.height);
            int x = drawnBounds.x - (int) (0.21 * size);
            int y = drawnBounds.y - (int) (0.21 * size);
            int w = drawnBounds.width + (int) (0.42 * size);
            int h = drawnBounds.height + (int) (0.42 * size);
            drawnBounds.setBounds(x, y, w, h);
          }
        }
      }
      if (extents == null) {
        extents = drawnBounds;
      } else {
        extents.add(drawnBounds);
      }
    }
    if (zone.hasFog()) {
      if (extents == null) {
        extents = fogExtents();
      } else {
        extents.add(fogExtents());
      }
    }
    return extents;
  }

  //
  // Panel related functions
  //

  private void switchToWaitPanel() {}

  private void switchToInteractPanel() {}
}
