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
package net.rptools.maptool.client.ui;

import com.jeta.forms.components.panel.FormPanel;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import net.rptools.lib.net.FTPLocation;
import net.rptools.lib.net.LocalLocation;
import net.rptools.lib.net.Location;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.ImageManager;

/**
 * Creates a dialog for performing a screen capture to a PNG file.
 *
 * <p>This uses a modal dialog based on an Abeille form. It creates a PNG file at the resolution of
 * the 'board' image/tile. The file can be saved to disk or sent to an FTP location.
 */
@SuppressWarnings("serial")
public class ExportDialogOld extends JDialog {

  private static FormPanel formPanel;
  private static Location exportLocation;

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
  public static enum ExportRadioButtons {
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

    private static FormPanel form;

    //
    // SetForm stores the form this is attached to
    //
    public static void setForm(FormPanel form) {
      ExportRadioButtons.form = form;

      for (ExportRadioButtons button : ExportRadioButtons.values()) {
        try {
          if (form.getRadioButton(button.toString()) == null) {
            throw new Exception("Export Dialog has a mis-matched enum: " + button.toString());
          }
          button.addActionListener(
              new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                  enforceButtonRules();
                }
              });
        } catch (Exception ex) {
          MapTool.showError(
              I18N.getString("dialog.screenshot.radio.button.uiImplementationError"), ex);
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

    /** Shortcut to allow clean code and type-checking of invocations of specific buttons */
    public void addActionListener(ActionListener listener) {
      form.getRadioButton(this.toString()).addActionListener(listener);
    }

    /** @return which of the buttons in the Type group is selected */
    public static ExportRadioButtons getType() {
      if (ExportRadioButtons.TYPE_CURRENT_VIEW.isChecked()) {
        return TYPE_CURRENT_VIEW;
      } else if (ExportRadioButtons.TYPE_ENTIRE_MAP.isChecked()) {
        return TYPE_ENTIRE_MAP;
      }
      return null;
    }

    /** @return which of the buttons in the View group is selected */
    public static ExportRadioButtons getView() {
      if (ExportRadioButtons.VIEW_GM.isChecked()) {
        return VIEW_GM;
      } else if (ExportRadioButtons.VIEW_PLAYER.isChecked()) {
        return VIEW_PLAYER;
      }
      return null;
    }

    /** @return which of the buttons in the Layers group is selected */
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
  private static enum ExportLayers {
    // enum_val (fieldName as per Abeille Forms Designer, playerCanModify)
    LAYER_TOKEN(true),
    LAYER_HIDDEN(false),
    LAYER_OBJECT(false),
    LAYER_BACKGROUND(false),
    LAYER_BOARD(false),
    LAYER_FOG(false),
    LAYER_VISIBILITY(true);

    private static FormPanel form;

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
    public static void setForm(FormPanel form) {
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
      // however, some psuedo-layers do have a state, so set that appropriately
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
      formPanel.getLabel("LAYERS_LABEL").setEnabled(false);
      ExportLayers.setDefaultChecked();
      ExportLayers.setDisabled();
    } else /* if (ExportRadioButtons.LAYERS_AS_SELECTED.isChecked()) */ {
      formPanel.getLabel("LAYERS_LABEL").setEnabled(true);
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

  public ExportDialogOld() {
    super(MapTool.getFrame(), "Export Screenshot", true);
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
    formPanel = new FormPanel("net/rptools/maptool/client/ui/forms/exportDialog.xml");
    setLayout(new GridLayout());
    add(formPanel);
    getRootPane().setDefaultButton((JButton) formPanel.getButton("exportButton"));
    pack();

    ExportRadioButtons.setForm(formPanel);
    ExportLayers.setForm(formPanel);

    formPanel
        .getButton("exportButton")
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                exportButtonAction();
              }
            });

    formPanel
        .getButton("cancelButton")
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                dispose();
              }
            });

    formPanel
        .getButton("browseButton")
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                browseButtonAction();
              }
            });

    // Run this once to make sure the dialog is in a good starting state.
    ExportLayers.setDefaultChecked();
    enforceButtonRules();
  }

  @Override
  public void setVisible(boolean b) {
    // In case something changed while the dialog was closed...
    enforceButtonRules();

    if (b) {
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
    // TODO: Show a progress dialog
    // TODO: Make this less fragile
    switch (formPanel.getTabbedPane("tabs").getSelectedIndex()) {
      case 0:
        File file = new File(formPanel.getText("locationTextField"));

        // PNG only supported for now
        if (!file.getName().toLowerCase().endsWith(".png")) {
          file = new File(file.getAbsolutePath() + ".png");
        }

        exportLocation = new LocalLocation(file);
        break;
      case 1:
        String username = formPanel.getText("username");
        String password = formPanel.getText("password");
        String host = formPanel.getText("host");
        String path = formPanel.getText("path");

        // PNG only supported for now
        if (!path.toLowerCase().endsWith(".png")) {
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

    if (chooser.showOpenDialog(ExportDialogOld.this) == JFileChooser.APPROVE_OPTION) {
      formPanel.setText("locationTextField", chooser.getSelectedFile().getAbsolutePath());
    }
  }

  /**
   * This is the top-level screen-capture routine. It sends the resulting PNG image to the location
   * previously selected by the user. TODO: It currently calls {@link MapTool#takeMapScreenShot} for
   * "normal" screenshots, but that's just until this code is considered stable enough.
   *
   * @throws Exception
   */
  public void screenCapture() throws Exception {

    BufferedImage screenCap = null;

    MapTool.getFrame()
        .setStatusMessage(I18N.getString("dialog.screenshot.msg.GeneratingScreenshot"));
    ExportRadioButtons type = ExportRadioButtons.getType();
    try {
      switch (type) {
        case TYPE_CURRENT_VIEW:
          // This uses the original screenshot code: I didn't want to touch it, so I need
          // to pass it the same parameter it took before.
          Player.Role role =
              ExportRadioButtons.VIEW_GM.isChecked() ? Player.Role.GM : Player.Role.PLAYER;
          screenCap = MapTool.takeMapScreenShot(new PlayerView(role));
          // since old screenshot code doesn't throw exceptions, look for null
          if (screenCap == null) {
            throw new Exception(I18N.getString("dialog.screenshot.error.failedImageGeneration"));
          }
          break;
        case TYPE_ENTIRE_MAP:
          screenCap = entireMapScreenShotWithLayers();
          break;
        default:
          throw new Exception(I18N.getString("dialog.screenshot.error.invalidDialogSettings"));
      }
      MapTool.getFrame()
          .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotStreaming"));
      ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
      try {
        ImageIO.write(screenCap, "png", imageOut);
        screenCap = null; // Free up the memory as soon as possible

        MapTool.getFrame()
            .setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaving"));
        exportLocation.putContent(
            new BufferedInputStream(new ByteArrayInputStream(imageOut.toByteArray())));
      } finally {
        if (imageOut != null) {
          imageOut.close();
        }
      }

      MapTool.getFrame().setStatusMessage(I18N.getString("dialog.screenshot.msg.screenshotSaved"));
    } catch (OutOfMemoryError e) {
      MapTool.showError("Out Of Memory", e);
    } catch (Exception ex) {
      MapTool.showError("screenCapture()", ex);
    }
  }

  public static Location getExportLocation() {
    return exportLocation;
  }

  public static void setExportLocation(Location loc) {
    exportLocation = loc;
  }

  /**
   * This is a wrapper that preserves the layer settings on the Zone object. It calls {@link
   * this#takeEntireMapScreenShot} to to the real work.
   *
   * @return the image to be saved to a file
   */
  private static BufferedImage entireMapScreenShotWithLayers() throws Exception, OutOfMemoryError {
    final Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

    //
    // Preserve settings
    //
    // psuedo-layers
    final Zone.VisionType savedVision = zone.getVisionType();
    final boolean savedFog = zone.hasFog();
    final boolean savedBoard = zone.drawBoard();
    // real layers
    final boolean savedToken = Zone.Layer.TOKEN.isEnabled();
    final boolean savedHidden = Zone.Layer.GM.isEnabled();
    final boolean savedObject = Zone.Layer.OBJECT.isEnabled();
    final boolean savedBackground = Zone.Layer.BACKGROUND.isEnabled();

    //
    // set according to dialog options
    //
    zone.setHasFog(ExportLayers.LAYER_FOG.isChecked());
    if (!ExportLayers.LAYER_VISIBILITY.isChecked()) zone.setVisionType(Zone.VisionType.OFF);
    zone.setDrawBoard(ExportLayers.LAYER_BOARD.isChecked());
    Zone.Layer.TOKEN.setEnabled(ExportLayers.LAYER_TOKEN.isChecked());
    Zone.Layer.GM.setEnabled(ExportLayers.LAYER_HIDDEN.isChecked());
    Zone.Layer.OBJECT.setEnabled(ExportLayers.LAYER_OBJECT.isChecked());
    Zone.Layer.BACKGROUND.setEnabled(ExportLayers.LAYER_BACKGROUND.isChecked());
    // This 'cache invalidation' is handled by setZone inside takeEntireMapScreenShot()
    // but it should be more robust.
    // MapTool.getFrame().getCurrentZoneRenderer().invalidateCurrentViewCache();

    //
    // screenshot!
    // MCL: NOTE: while turning off Fog, there is a possibility the players
    // may see the map flash for a second with fog turned off-- need to look into
    // whether this is true.
    BufferedImage image = null;
    try {
      image = takeEntireMapScreenShot();
    } finally {
      //
      // Restore settings
      //
      zone.setHasFog(savedFog);
      zone.setVisionType(savedVision);
      zone.setDrawBoard(savedBoard);
      Zone.Layer.TOKEN.setEnabled(savedToken);
      Zone.Layer.GM.setEnabled(savedHidden);
      Zone.Layer.OBJECT.setEnabled(savedObject);
      Zone.Layer.BACKGROUND.setEnabled(savedBackground);
      // MapTool.getFrame().getCurrentZoneRenderer().invalidateCurrentViewCache();
    }

    return image;
  }

  /**
   * Finds the extents of the map, then takes a 'screenshot' of that area. If the user is the GM,
   * the extents include every object and everything that has any area, such as 'fog' and
   * 'visibility' objects.
   *
   * <p>If a background tiling texture is used, the image is aligned to it, so that it can be used
   * on re-import as a new base map image.
   *
   * <p>If the user is a player (or GM posing as a player), the extents only go as far as the
   * revealed fog-of-war.
   *
   * @return the image to be saved
   */
  private static BufferedImage takeEntireMapScreenShot() throws Exception, OutOfMemoryError {
    final ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    if (renderer == null) {
      throw (new Exception("renderer = NULL"));
    }

    boolean viewAsPlayer = ExportRadioButtons.VIEW_PLAYER.isChecked();

    //
    // First, figure out the 'extents' of the canvas
    // This will be later modified by the fog (for players),
    // and by the tiling texture (for re-importing)
    //
    final PlayerView view = new PlayerView(viewAsPlayer ? Player.Role.PLAYER : Player.Role.GM);
    Rectangle extents = renderer.zoneExtents(view);
    try {
      // Clip to what the players know about (if applicable).
      // This keeps the player from exporting the map to learn which
      // direction has more 'stuff' in it.

      if (viewAsPlayer) {
        Rectangle fogE = renderer.fogExtents();
        // MapTool.showError(fogE.x + " " + fogE.y + " " + fogE.width + " " + fogE.height);
        if ((fogE.width < 0) || (fogE.height < 0)) {
          MapTool.showError(
              I18N.getString(
                  "dialog.screenshot.error.negativeFogExtents")); // Image is not clipped to show
          // only fog-revealed areas!"));
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
      Integer tileX = 0, tileY = 0;

      if (paint.getClass() == dummy.getClass()) {
        Image bgTexture = ImageManager.getImage(((DrawableTexturePaint) paint).getAsset().getId());
        tileX = bgTexture.getWidth(null);
        tileY = bgTexture.getHeight(null);
        Integer x = ((int) Math.floor((float) extents.x / tileX)) * tileX;
        Integer y = ((int) Math.floor((float) extents.y / tileY)) * tileY;
        extents.width = extents.width + (extents.x - x);
        extents.height = extents.height + (extents.y - y);
        extents.x = x;
        extents.y = y;
      }
    }

    // Save the original state of the renderer to restore later.
    // Create a place to put the image, and
    // set up the renderer to encompass the whole extents of the map.

    Rectangle origBounds = renderer.getBounds();
    Scale origScale = renderer.getZoneScale();
    Dimension origSize = renderer.getSize();

    BufferedImage image = null;
    try {
      image = new BufferedImage(extents.width, extents.height, Transparency.OPAQUE);
    } catch (OutOfMemoryError me) {
      throw new OutOfMemoryError("image size = " + extents.width + " x " + extents.height);
    } catch (Exception e) {
      throw new Exception("image size = " + extents.width + " x " + extents.height, e);
    }

    final Graphics2D g = image.createGraphics();
    g.setClip(0, 0, extents.width, extents.height);
    Scale s = new Scale();
    s.setOffset(-extents.x, -extents.y);

    // Finally, draw the image.
    // Copied this thread concept from the original screenshot code in MapTool.
    // Have to do this on the EDT so that there aren't any odd side effects
    // of rendering using a renderer that's on screen.

    try {
      renderer.setZoneScale(s);
      renderer.setBounds(extents);
      renderer.setSize(extents.getSize());
      if (!EventQueue.isDispatchThread()) {
        EventQueue.invokeAndWait(
            new Runnable() {
              public void run() {
                renderer.renderZone(g, view);
              }
            });
      } else {
        renderer.renderZone(g, view);
      }
      return image;
    } catch (OutOfMemoryError me) {
      throw new OutOfMemoryError("image size = " + extents.width + " x " + extents.height);
    } catch (InterruptedException ie) {
      MapTool.showError("While creating snapshot", ie);
    } catch (InvocationTargetException ite) {
      MapTool.showError("While creating snapshot", ite);
    } catch (Exception e) {
      throw new Exception("image size = " + extents.width + " x " + extents.height, e);
    } finally {
      g.dispose();
      // Restore original state
      renderer.setBounds(origBounds);
      renderer.setZoneScale(origScale);
      renderer.setSize(origSize);
    }
    // This is just to avoid the compiler error: it should be unreachable
    return null;
  }
}
