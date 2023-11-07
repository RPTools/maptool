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
package net.rptools.maptool.client.ui.campaignproperties;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.swing.*;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.StaticMessageDialog;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.StringUtil;

public class CampaignPropertiesDialog extends JDialog {
  public enum Status {
    OK,
    CANCEL
  }

  private TokenPropertiesManagementPanel tokenPropertiesPanel;
  private TokenStatesController tokenStatesController;
  private TokenBarController tokenBarController;

  private Status status;
  private AbeillePanel formPanel;
  private Campaign campaign;

  public CampaignPropertiesDialog(JFrame owner) {
    super(owner, I18N.getText("CampaignPropertiesDialog.label.title"), true);

    initialize();

    pack(); // FJE
  }

  public Status getStatus() {
    return status;
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    } else {
      MapTool.getFrame().repaint();
    }
    super.setVisible(b);
  }

  private void initialize() {
    setLayout(new GridLayout());
    formPanel = new AbeillePanel(new CampaignPropertiesDialogView().getRootComponent());

    initTokenPropertiesDialog(formPanel);
    tokenStatesController = new TokenStatesController(formPanel);
    tokenBarController = new TokenBarController(formPanel);
    tokenBarController.setNames(tokenStatesController.getNames());

    initHelp();
    initOKButton();
    initCancelButton();
    initAddRepoButton();
    //    initAddGalleryIndexButton();
    initDeleteRepoButton();

    initImportButton();
    initExportButton();
    initImportPredefinedButton();
    initPredefinedPropertiesComboBox();

    add(formPanel);

    // Escape key
    formPanel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    formPanel
        .getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                cancel();
              }
            });
    getRootPane().setDefaultButton(getOKButton());
  }

  private void initTokenPropertiesDialog(AbeillePanel panel) {
    tokenPropertiesPanel = new TokenPropertiesManagementPanel();
    panel.replaceComponent("propertiesPanel", "tokenPropertiesPanel", tokenPropertiesPanel);
  }

  public JTextField getNewServerTextField() {
    return formPanel.getTextField("newServer");
  }

  private void initHelp() {
    JEditorPane lightHelp = (JEditorPane) formPanel.getComponent("lightHelp");
    lightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    lightHelp.setText(I18N.getString("CampaignPropertiesDialog.label.light"));
    lightHelp.setCaretPosition(0);

    JEditorPane sightHelp = (JEditorPane) formPanel.getComponent("sightHelp");
    sightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    sightHelp.setText(I18N.getString("CampaignPropertiesDialog.label.sight"));
    sightHelp.setCaretPosition(0);
  }

  private void initAddRepoButton() {
    JButton button = (JButton) formPanel.getButton("addRepoButton");
    button.addActionListener(
        e -> {
          String newRepo = getNewServerTextField().getText();
          if (newRepo == null || newRepo.length() == 0) {
            return;
          }
          // TODO: Check for uniqueness
          ((DefaultListModel) getRepositoryList().getModel()).addElement(newRepo);
        });
  }

  //  private void initAddGalleryIndexButton() {
  //    JButton button = (JButton) formPanel.getButton("addGalleryIndexButton");
  //    button.addActionListener(
  //        new ActionListener() {
  //          public void actionPerformed(ActionEvent e) {
  //            // TODO: Check for uniqueness
  //            ((DefaultListModel) getRepositoryList().getModel())
  //                .addElement("http://www.rptools.net/image-indexes/gallery.rpax.gz");
  //          }
  //        });
  //  }

  public void initDeleteRepoButton() {
    JButton button = (JButton) formPanel.getButton("deleteRepoButton");
    button.addActionListener(
        e -> {
          int[] selectedRows = getRepositoryList().getSelectedIndices();
          Arrays.sort(selectedRows);
          for (int i = selectedRows.length - 1; i >= 0; i--) {
            ((DefaultListModel) getRepositoryList().getModel()).remove(selectedRows[i]);
          }
        });
  }

  private void cancel() {
    status = Status.CANCEL;
    setVisible(false);
  }

  private void accept() {
    try {
      MapTool.getFrame()
          .showFilledGlassPane(
              new StaticMessageDialog("campaignPropertiesDialog.tokenTypeNameRename"));
      tokenPropertiesPanel
          .getRenameTypes()
          .forEach(
              (o, n) -> {
                campaign.renameTokenTypes(o, n);
              });
      MapTool.getFrame().hideGlassPane();
      copyUIToCampaign();
      AssetManager.updateRepositoryList();
      status = Status.OK;
      setVisible(false);
    } catch (IllegalArgumentException iae) {
      MapTool.showError(iae.getMessage());
    }
  }

  public void setCampaign(Campaign campaign) {
    this.campaign = campaign;
    copyCampaignToUI(campaign.getCampaignProperties());
  }

  private void copyCampaignToUI(CampaignProperties campaignProperties) {

    tokenPropertiesPanel.copyCampaignToUI(campaignProperties);
    updateRepositoryList(campaignProperties);

    String text;
    text = updateSightPanel(campaignProperties.getSightTypeMap());
    getSightPanel().setText(text);
    getSightPanel().setCaretPosition(0);

    text = updateLightPanel(campaignProperties.getLightSourcesMap());
    getLightPanel().setText(text);
    getLightPanel().setCaretPosition(0);

    tokenStatesController.copyCampaignToUI(campaignProperties);
    tokenBarController.copyCampaignToUI(campaignProperties);
    // updateTableList();
  }

  private String updateSightPanel(Map<String, SightType> sightTypeMap) {
    StringBuilder builder = new StringBuilder();
    for (SightType sight : sightTypeMap.values()) {
      builder.append(sight.getName()).append(": ");

      switch (sight.getShape()) {
        case SQUARE:
          builder.append("square ");
          if (sight.getDistance() != 0)
            builder
                .append("distance=")
                .append(StringUtil.formatDecimal(sight.getDistance()))
                .append(' ');
          break;
        case CIRCLE:
          builder.append("circle ");
          if (sight.getDistance() != 0)
            builder
                .append("distance=")
                .append(StringUtil.formatDecimal(sight.getDistance()))
                .append(' ');
          break;
        case GRID:
          builder.append("grid ");
          if (sight.getDistance() != 0)
            builder
                .append("distance=")
                .append(StringUtil.formatDecimal(sight.getDistance()))
                .append(' ');
          break;
        case HEX:
          builder.append("hex ");
          if (sight.getDistance() != 0)
            builder
                .append("distance=")
                .append(StringUtil.formatDecimal(sight.getDistance()))
                .append(' ');
          break;
        case CONE:
          builder.append("cone ");
          if (sight.getArc() != 0)
            builder.append("arc=").append(StringUtil.formatDecimal(sight.getArc())).append(' ');
          if (sight.getOffset() != 0)
            builder
                .append("offset=")
                .append(StringUtil.formatDecimal(sight.getOffset()))
                .append(' ');
          if (sight.getDistance() != 0)
            builder
                .append("distance=")
                .append(StringUtil.formatDecimal(sight.getDistance()))
                .append(' ');
          break;
        default:
          throw new IllegalArgumentException("Invalid shape?!");
      }
      // Scale with Token
      if (sight.isScaleWithToken()) {
        builder.append("scale ");
      }
      // Multiplier
      if (sight.getMultiplier() != 1 && sight.getMultiplier() != 0) {
        builder.append("x").append(StringUtil.formatDecimal(sight.getMultiplier())).append(' ');
      }
      // Personal light
      if (sight.getPersonalLightSource() != null) {
        LightSource source = sight.getPersonalLightSource();

        if (source.getLightList() != null) {
          for (Light light : source.getLightList()) {
            double range = light.getRadius();

            builder.append("r").append(StringUtil.formatDecimal(range));

            if (light.getPaint() != null && light.getPaint() instanceof DrawableColorPaint) {
              Color color = (Color) light.getPaint().getPaint();
              builder.append(toHex(color));
            }
            final var lumens = light.getLumens();
            if (lumens >= 0) {
              builder.append('+');
            }
            builder.append(Integer.toString(lumens, 10));
            builder.append(' ');
          }
        }
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  private String updateLightPanel(Map<String, Map<GUID, LightSource>> lightSources) {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, Map<GUID, LightSource>> entry : lightSources.entrySet()) {
      builder.append(entry.getKey());
      builder.append("\n----\n");

      for (LightSource lightSource : entry.getValue().values()) {
        builder.append(lightSource.getName()).append(":");

        if (lightSource.getType() != LightSource.Type.NORMAL) {
          builder.append(' ').append(lightSource.getType().name().toLowerCase());
        }
        if (lightSource.isScaleWithToken()) {
          builder.append(" scale");
        }

        String lastShape = ""; // this forces 'circle' to be printed
        double lastArc = 90;
        boolean lastGM = false;
        boolean lastOwner = false;
        for (Light light : lightSource.getLightList()) {
          String shape;
          // TODO: This HAS to change, the lights need to be auto describing, this hard wiring sucks
          if (lightSource.getType() == LightSource.Type.AURA) {
            // Currently these are mutually exclusive but perhaps not in the future?
            if (light.isGM() && light.isGM() != lastGM) builder.append(" GM");
            if (light.isOwnerOnly() && light.isOwnerOnly() != lastOwner) builder.append(" OWNER");
            lastGM = light.isGM();
            lastOwner = light.isOwnerOnly();
          }
          if (light.getShape() != null) {
            switch (light.getShape()) {
              default:
                throw new RuntimeException(
                    "Unrecognized shape: " + light.getShape().toString().toLowerCase());
              case SQUARE:
              case GRID:
              case CIRCLE:
              case HEX:
                // TODO: Make this a preference
                shape = light.getShape().toString().toLowerCase();
                break;
              case CONE:
                // if (light.getArcAngle() != 0 && light.getArcAngle() != 90 && light.getArcAngle()
                // != lastArc)
                {
                  lastArc = light.getArcAngle();
                  shape = "cone arc=" + StringUtil.formatDecimal(lastArc);
                  if (light.getFacingOffset() != 0)
                    builder
                        .append(" offset=")
                        .append(StringUtil.formatDecimal(light.getFacingOffset()))
                        .append(' ');
                }
                break;
            }
            if (!lastShape.equals(shape)) builder.append(' ').append(shape);
            lastShape = shape;
          }
          builder.append(' ').append(StringUtil.formatDecimal(light.getRadius()));
          if (light.getPaint() instanceof DrawableColorPaint) {
            Color color = (Color) light.getPaint().getPaint();
            builder.append(toHex(color));
          }
          if (lightSource.getType() == LightSource.Type.NORMAL) {
            final var lumens = light.getLumens();
            if (lumens >= 0) {
              builder.append('+');
            }
            builder.append(Integer.toString(lumens, 10));
          }
        }
        builder.append('\n');
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  private String toHex(Color color) {
    StringBuilder builder = new StringBuilder("#");

    builder.append(padLeft(Integer.toHexString(color.getRed()), '0', 2));
    builder.append(padLeft(Integer.toHexString(color.getGreen()), '0', 2));
    builder.append(padLeft(Integer.toHexString(color.getBlue()), '0', 2));

    return builder.toString();
  }

  private String padLeft(String str, char padChar, int length) {
    while (str.length() < length) {
      str = padChar + str;
    }
    return str;
  }

  private void updateRepositoryList(CampaignProperties properties) {
    DefaultListModel model = new DefaultListModel();
    for (String repo : properties.getRemoteRepositoryList()) {
      model.addElement(repo);
    }
    getRepositoryList().setModel(model);
  }

  public JList getRepositoryList() {
    return formPanel.getList("repoList");
  }

  private void copyUIToCampaign() {
    tokenPropertiesPanel.copyUIToCampaign(campaign);

    campaign.getRemoteRepositoryList().clear();
    for (int i = 0; i < getRepositoryList().getModel().getSize(); i++) {
      String repo = (String) getRepositoryList().getModel().getElementAt(i);
      campaign.getRemoteRepositoryList().add(repo);
    }
    Map<String, Map<GUID, LightSource>> lightMap;
    lightMap = commitLightMap(getLightPanel().getText(), campaign.getLightSourcesMap());
    campaign.getLightSourcesMap().clear();
    campaign.getLightSourcesMap().putAll(lightMap);

    commitSightMap(getSightPanel().getText());
    tokenStatesController.copyUIToCampaign(campaign);
    tokenBarController.copyUIToCampaign(campaign);

    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    if (zr != null) {
      zr.getZoneView().flush();
      zr.flushFog();
      zr.flushLight();
      MapTool.getFrame().refresh();
    }
  }

  private void commitSightMap(final String text) {
    List<SightType> sightList = new LinkedList<SightType>();
    LineNumberReader reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
    String line = null;
    String toBeParsed = null, errmsg = null;
    List<String> errlog = new LinkedList<String>();
    try {
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Blanks
        if (line.length() == 0 || line.indexOf(':') < 1) {
          continue;
        }
        // Parse line
        int split = line.indexOf(':');
        String label = line.substring(0, split).trim();
        String value = line.substring(split + 1).trim();

        if (label.length() == 0) {
          continue;
        }
        // Parse Details
        double magnifier = 1;
        // If null, no personal light has been defined.
        List<Light> personalLightLights = null;

        String[] args = value.split("\\s+");
        ShapeType shape = ShapeType.CIRCLE;
        boolean scaleWithToken = false;
        int arc = 90;
        float range = 0;
        int offset = 0;
        double pLightRange = 0;

        for (String arg : args) {
          assert arg.length() > 0; // The split() uses "one or more spaces", removing empty strings
          try {
            shape = ShapeType.valueOf(arg.toUpperCase());
            continue;
          } catch (IllegalArgumentException iae) {
            // Expected when not defining a shape
          }
          // Scale with Token
          if (arg.equalsIgnoreCase("SCALE")) {
            scaleWithToken = true;
            continue;
          }
          try {

            if (arg.startsWith("x")) {
              toBeParsed = arg.substring(1); // Used in the catch block, below
              errmsg = "msg.error.mtprops.sight.multiplier"; // (ditto)
              magnifier = StringUtil.parseDecimal(toBeParsed);
            } else if (arg.startsWith("r")) { // XXX Why not "r=#" instead of "r#"??
              toBeParsed = arg.substring(1);
              errmsg = "msg.error.mtprops.sight.range";

              final var rangeRegex = Pattern.compile("([^#+-]*)(#[0-9a-fA-F]+)?([+-]\\d*)?");
              final var matcher = rangeRegex.matcher(toBeParsed);
              if (matcher.find()) {
                pLightRange = StringUtil.parseDecimal(matcher.group(1));
                final var colorString = matcher.group(2);
                final var lumensString = matcher.group(3);
                // Note that Color.decode() _wants_ the leading "#", otherwise it might not treat
                // the value as a hex code.
                Color personalLightColor = null;
                if (colorString != null) {
                  personalLightColor = Color.decode(colorString);
                }
                int perRangeLumens = 100;
                if (lumensString != null) {
                  perRangeLumens = Integer.parseInt(lumensString, 10);
                  if (perRangeLumens == 0) {
                    errlog.add(
                        I18N.getText("msg.error.mtprops.sight.zerolumens", reader.getLineNumber()));
                    perRangeLumens = 100;
                  }
                }

                if (personalLightLights == null) {
                  personalLightLights = new ArrayList<>();
                }
                personalLightLights.add(
                    new Light(
                        shape,
                        0,
                        pLightRange,
                        arc,
                        personalLightColor == null
                            ? null
                            : new DrawableColorPaint(personalLightColor),
                        perRangeLumens,
                        false,
                        false));
              } else {
                throw new ParseException(
                    String.format("Unrecognized personal light syntax: %s", arg), 0);
              }
            } else if (arg.startsWith("arc=") && arg.length() > 4) {
              toBeParsed = arg.substring(4);
              errmsg = "msg.error.mtprops.sight.arc";
              arc = StringUtil.parseInteger(toBeParsed);
            } else if (arg.startsWith("distance=") && arg.length() > 9) {
              toBeParsed = arg.substring(9);
              errmsg = "msg.error.mtprops.sight.distance";
              range = StringUtil.parseDecimal(toBeParsed).floatValue();
            } else if (arg.startsWith("offset=") && arg.length() > 7) {
              toBeParsed = arg.substring(7);
              errmsg = "msg.error.mtprops.sight.offset";
              offset = StringUtil.parseInteger(toBeParsed);
            } else {
              toBeParsed = arg;
              errmsg =
                  I18N.getText(
                      "msg.error.mtprops.sight.unknownField", reader.getLineNumber(), toBeParsed);
              errlog.add(errmsg);
            }
          } catch (ParseException e) {
            assert errmsg != null;
            errlog.add(I18N.getText(errmsg, reader.getLineNumber(), toBeParsed));
          }
        }

        LightSource personalLight =
            personalLightLights == null
                ? null
                : LightSource.createPersonal(scaleWithToken, personalLightLights);
        SightType sight =
            new SightType(label, magnifier, personalLight, shape, arc, scaleWithToken);
        sight.setDistance(range);
        sight.setOffset(offset);

        // Store
        sightList.add(sight);
      }
    } catch (IOException ioe) {
      MapTool.showError("msg.error.mtprops.sight.ioexception", ioe);
    }
    if (!errlog.isEmpty()) {
      // Show the user a list of errors so they can (attempt to) correct all of them at once
      MapTool.showFeedback(errlog.toArray());
      errlog.clear();
      throw new IllegalArgumentException(
          "msg.error.mtprops.sight.definition"); // Don't save sights...
    }
    campaign.setSightTypes(sightList);
  }

  /**
   * Converts the string stored in <code>getLightPanel().getText()</code> into a Map that relates a
   * group of light sources to a Map of GUID and LightSource.
   *
   * <p>The format for the text is as follows:
   *
   * <ol>
   *   <li>Any line starting with a dash ("-") is a comment and is ignored.
   *   <li>Blank lines (those containing only zero or more spaces) are group separators.
   *   <li>The first line of a sequence is the group name.
   *   <li>Within a group, any line without a colon (":") is ignored.
   *   <li>Remaining lines are of the following format:
   *       <p><b> <code>
   *       [Gm | Owner] [Circle+ | Square | Cone] [Normal+ | Aura] [Arc=angle] [Offset=angle] distance [#rrggbb]
   *       </code> </b>
   *       <p>Brackets indicate optional components. A plus sign follows any default value for a
   *       given field. Fields starting with an uppercase letter are literal text (although they are
   *       case-insensitive). Fields that do not start with an uppercase letter represent
   *       user-supplied values, typically numbers (such as <code>angle</code>, <code>distance
   *       </code>, and <code>#rrggbb</code>). The <code>GM</code>/<code>Owner</code> field is only
   *       valid for Auras.
   * </ol>
   */
  private Map<String, Map<GUID, LightSource>> commitLightMap(
      final String text, final Map<String, Map<GUID, LightSource>> originalLightSourcesMap) {
    Map<String, Map<GUID, LightSource>> lightMap = new TreeMap<String, Map<GUID, LightSource>>();
    LineNumberReader reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
    String line = null;
    List<String> errlog = new LinkedList<String>();

    try {
      String currentGroupName = null;
      Map<GUID, LightSource> lightSourceMap = null;

      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Comments
        if (line.length() > 0 && line.charAt(0) == '-') {
          continue;
        }
        // Blank lines
        if (line.length() == 0) {
          if (currentGroupName != null) {
            lightMap.put(currentGroupName, lightSourceMap);
          }
          currentGroupName = null;
          continue;
        }
        // New group
        if (currentGroupName == null) {
          currentGroupName = line;
          lightSourceMap = new HashMap<GUID, LightSource>();
          continue;
        }
        // Item
        int split = line.indexOf(':');
        if (split < 1) {
          continue;
        }

        // region Light source properties.
        String name = line.substring(0, split).trim();
        GUID id = new GUID();
        LightSource.Type type = LightSource.Type.NORMAL;
        boolean scaleWithToken = false;
        List<Light> lights = new ArrayList<>();
        // endregion
        // region Individual light properties
        ShapeType shape = ShapeType.CIRCLE; // TODO: Make a preference for default shape
        double arc = 0;
        double offset = 0;
        boolean gmOnly = false;
        boolean owner = false;
        String distance = null;
        // endregion

        for (String arg : line.substring(split + 1).split("\\s+")) {
          arg = arg.trim();
          if (arg.length() == 0) {
            continue;
          }
          if (arg.equalsIgnoreCase("GM")) {
            gmOnly = true;
            owner = false;
            continue;
          }
          if (arg.equalsIgnoreCase("OWNER")) {
            gmOnly = false;
            owner = true;
            continue;
          }
          // Scale with token designation
          if (arg.equalsIgnoreCase("SCALE")) {
            scaleWithToken = true;
            continue;
          }
          // Shape designation ?
          try {
            shape = ShapeType.valueOf(arg.toUpperCase());
            continue;
          } catch (IllegalArgumentException iae) {
            // Expected when not defining a shape
          }

          // Type designation ?
          try {
            type = LightSource.Type.valueOf(arg.toUpperCase());
            continue;
          } catch (IllegalArgumentException iae) {
            // Expected when not defining a shape
          }

          // Facing offset designation
          if (arg.toUpperCase().startsWith("OFFSET=")) {
            try {
              offset = Integer.parseInt(arg.substring(7));
              continue;
            } catch (NullPointerException noe) {
              errlog.add(
                  I18N.getText("msg.error.mtprops.light.offset", reader.getLineNumber(), arg));
            }
          }

          // Parameters
          split = arg.indexOf('=');
          if (split > 0) {
            String key = arg.substring(0, split);
            String value = arg.substring(split + 1);

            // TODO: Make this a generic map to pass instead of 'arc'
            if ("arc".equalsIgnoreCase(key)) {
              try {
                arc = StringUtil.parseDecimal(value);
                shape = ShapeType.CONE; // If the user specifies an arc, force the shape to CONE
              } catch (ParseException pe) {
                errlog.add(
                    I18N.getText("msg.error.mtprops.light.arc", reader.getLineNumber(), value));
              }
            }
            continue;
          }

          Color color = null;
          int perRangeLumens = 100;
          distance = arg;

          final var rangeRegex = Pattern.compile("([^#+-]*)(#[0-9a-fA-F]+)?([+-]\\d*)?");
          final var matcher = rangeRegex.matcher(arg);
          if (matcher.find()) {
            distance = matcher.group(1);
            final var colorString = matcher.group(2);
            final var lumensString = matcher.group(3);
            // Note that Color.decode() _wants_ the leading "#", otherwise it might not treat the
            // value as a hex code.
            if (colorString != null) {
              color = Color.decode(colorString);
            }
            if (lumensString != null) {
              perRangeLumens = Integer.parseInt(lumensString, 10);
              if (perRangeLumens == 0) {
                errlog.add(
                    I18N.getText("msg.error.mtprops.light.zerolumens", reader.getLineNumber()));
                perRangeLumens = 100;
              }
            }
          }

          boolean isAura = type == LightSource.Type.AURA;
          if (!isAura && (gmOnly || owner)) {
            errlog.add(I18N.getText("msg.error.mtprops.light.gmOrOwner", reader.getLineNumber()));
            gmOnly = false;
            owner = false;
          }
          owner = gmOnly ? false : owner;
          try {
            Light t =
                new Light(
                    shape,
                    offset,
                    StringUtil.parseDecimal(distance),
                    arc,
                    color == null ? null : new DrawableColorPaint(color),
                    perRangeLumens,
                    gmOnly,
                    owner);
            lights.add(t);
          } catch (ParseException pe) {
            errlog.add(
                I18N.getText("msg.error.mtprops.light.distance", reader.getLineNumber(), distance));
          }
        }
        // Keep ID the same if modifying existing light. This avoids tokens losing their lights when
        // the light definition is modified.
        if (originalLightSourcesMap.containsKey(currentGroupName)) {
          for (LightSource ls : originalLightSourcesMap.get(currentGroupName).values()) {
            if (ls.getName().equalsIgnoreCase(name)) {
              assert ls.getId() != null;
              id = ls.getId();
              break;
            }
          }
        }

        final var source = LightSource.createRegular(name, id, type, scaleWithToken, lights);
        lightSourceMap.put(source.getId(), source);
      }
      // Last group
      if (currentGroupName != null) {
        lightMap.put(currentGroupName, lightSourceMap);
      }
    } catch (IOException ioe) {
      MapTool.showError("msg.error.mtprops.light.ioexception", ioe);
    }
    if (!errlog.isEmpty()) {
      MapTool.showFeedback(errlog.toArray());
      errlog.clear();
      throw new IllegalArgumentException(
          "msg.error.mtprops.light.definition"); // Don't save lights...
    }
    return lightMap;
  }

  public JEditorPane getLightPanel() {
    return (JEditorPane) formPanel.getTextComponent("lightPanel");
  }

  public JEditorPane getSightPanel() {
    return (JEditorPane) formPanel.getTextComponent("sightPanel");
  }

  public JTextArea getTokenPropertiesTextArea() {
    return (JTextArea) formPanel.getTextComponent("tokenProperties");
  }

  public JButton getOKButton() {
    return (JButton) formPanel.getButton("okButton");
  }

  private void initOKButton() {
    getOKButton().addActionListener(e -> accept());
  }

  public JButton getCancelButton() {
    return (JButton) formPanel.getButton("cancelButton");
  }

  public JButton getImportButton() {
    return (JButton) formPanel.getButton("importButton");
  }

  public JButton getExportButton() {
    return (JButton) formPanel.getButton("exportButton");
  }

  public JButton getImportPredefinedButton() {
    return (JButton) formPanel.getButton("importPredefinedButton");
  }

  public JComboBox<String> getPredefinedPropertiesComboBox() {
    return (JComboBox<String>) formPanel.getComboBox("predefinedPropertiesComboBox");
  }

  private void initCancelButton() {
    getCancelButton()
        .addActionListener(
            e -> {
              status = Status.CANCEL;
              setVisible(false);
            });
  }

  private void initImportButton() {
    getImportButton()
        .addActionListener(
            e -> {
              JFileChooser chooser = MapTool.getFrame().getLoadPropsFileChooser();

              if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) return;

              final File selectedFile = chooser.getSelectedFile();
              EventQueue.invokeLater(
                  () -> {
                    CampaignProperties properties =
                        PersistenceUtil.loadCampaignProperties(selectedFile);
                    // TODO: Allow specifying whether it is a replace or merge
                    if (properties != null) {
                      MapTool.getCampaign().mergeCampaignProperties(properties);
                      copyCampaignToUI(properties);
                    }
                  });
            });
  }

  private void initExportButton() {
    getExportButton()
        .addActionListener(
            e -> {
              // TODO: Remove this hack. Specifically, make the export use a properties object
              // composed of the current dialog entries instead of directly from the campaign
              copyUIToCampaign();
              // END HACK

              JFileChooser chooser = MapTool.getFrame().getSavePropsFileChooser();
              boolean tryAgain = true;
              while (tryAgain) {
                if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                  return;
                }
                var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
                var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
                if (saveDir.startsWith(installDir)) {
                  MapTool.showWarning("msg.warning.savePropToInstallDir");
                } else {
                  tryAgain = false;
                }
              }

              File selectedFile = chooser.getSelectedFile();
              if (selectedFile.exists()) {
                if (selectedFile.getName().endsWith(".rpgame")) {
                  if (!MapTool.confirm("Import into game settings file?")) {
                    return;
                  }
                } else if (!MapTool.confirm("Overwrite existing file?")) {
                  return;
                }
              }
              try {
                PersistenceUtil.saveCampaignProperties(campaign, chooser.getSelectedFile());
                MapTool.showInformation("Properties Saved.");
              } catch (IOException ioe) {
                MapTool.showError("Could not save properties: ", ioe);
              }
            });
  }

  private void initImportPredefinedButton() {
    getImportPredefinedButton()
        .addActionListener(
            new ActionListener() {

              private File getSelectedPropertyFile() {
                String property = (String) getPredefinedPropertiesComboBox().getSelectedItem();
                return new File(
                    AppConstants.CAMPAIGN_PROPERTIES_DIR,
                    property + AppConstants.CAMPAIGN_PROPERTIES_FILE_EXTENSION);
              }

              @Override
              public void actionPerformed(ActionEvent e) {
                File selectedFile = getSelectedPropertyFile();
                EventQueue.invokeLater(
                    () -> {
                      CampaignProperties properties =
                          PersistenceUtil.loadCampaignProperties(selectedFile);
                      if (properties != null) {
                        MapTool.getCampaign().mergeCampaignProperties(properties);
                        copyCampaignToUI(properties);
                      }
                    });
              }
            });
  }

  private void initPredefinedPropertiesComboBox() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
    for (File f : getPredefinedProperty()) {

      model.addElement(FileUtil.getNameWithoutExtension(f));
    }
    getPredefinedPropertiesComboBox().setModel(model);
  }

  private List<File> getPredefinedProperty() {
    File[] result = getPredefinedPropertyFiles(AppConstants.CAMPAIGN_PROPERTIES_DIR);
    if (result == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(result);
  }

  protected File[] getPredefinedPropertyFiles(File propertyDir) {
    return propertyDir.listFiles(AppConstants.CAMPAIGN_PROPERTIES_FILE_FILTER);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Testing campaign properties dialog syntax-specific fields");
    CampaignPropertiesDialog cpd = new CampaignPropertiesDialog(frame);
    // @formatter:off
    String lights =
        "D20\n"
            + "----\n"
            + "Lantern, Bullseye - 60 : cone arc=60 60#f0f0f0 120#330000\n"
            + "Lantern, Hooded - 30 : circle 30 60#330000 arc=60 120#f0f0f0\n"
            + "Torch - 20 : circle 20 40#330000\n"
            + "\n"
            + "Aura\n"
            + "----\n"
            + "Arc 120deg OWNERonly - 20 : owner aura arc=120 22.5#115511\n"
            + "Arc 60deg - 60 : aura cone arc=60 facing=15 62.5#77ffaa\n"
            + "Circle - 20 : aura circle 22.5#220000\n"
            + "Circle GM+Owner : aura circle GM Owner 62.5#ff8080\n"
            + "Circle GMonly : aura circle GM 62.5#ff8080\n"
            + "Fancy - 30/60/120 : aura GM circle 30 60#330000 owner arc=60 120#f0f0f0\n"
            + "\n";
    // @formatter:on
    System.out.print(lights);

    Map<String, Map<GUID, LightSource>> originalLightSourcesMap =
        new HashMap<String, Map<GUID, LightSource>>();
    Map<String, Map<GUID, LightSource>> lightMap = new HashMap<String, Map<GUID, LightSource>>();
    try {
      lightMap = cpd.commitLightMap(lights, originalLightSourcesMap);
    } catch (Exception e) {
    }

    String text = cpd.updateLightPanel(lightMap);
    System.out.print(text);

    // keySet() might be empty if an exception occurred.
    for (String string : lightMap.keySet()) {
      System.out.println("\nGroup Name: " + string);
      System.out.println("-------------");
      for (GUID guid : lightMap.get(string).keySet()) {
        LightSource ls = lightMap.get(string).get(guid);
        System.out.println(ls.getType() + ", " + ls.getName() + ":");
        for (Light light : ls.getLightList()) {
          System.out.print("  [shape=" + light.getShape());
          if (light.getShape() == ShapeType.CONE) {
            System.out.print(", arc=" + light.getArcAngle());
            System.out.print(", facing=" + light.getFacingOffset());
          }
          System.out.print(", gm=" + light.isGM());
          System.out.print(", owner=" + light.isOwnerOnly());
          System.out.print(", radius=" + light.getRadius());
          System.out.print(", color=" + light.getPaint() + "]\n");
        }
      }
    }
    System.exit(1);
  }
}
