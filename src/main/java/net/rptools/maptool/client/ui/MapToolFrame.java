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

import com.google.common.eventbus.Subscribe;
import com.jidesoft.docking.DefaultDockableHolder;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.ParserConfigurationException;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.AppActions.ClientAction;
import net.rptools.maptool.client.events.ZoneActivated;
import net.rptools.maptool.client.events.ZoneDeactivated;
import net.rptools.maptool.client.events.ZoneLoading;
import net.rptools.maptool.client.swing.AboutDialog;
import net.rptools.maptool.client.swing.AppHomeDiskSpaceStatusBar;
import net.rptools.maptool.client.swing.AssetCacheStatusBar;
import net.rptools.maptool.client.swing.CoordinateStatusBar;
import net.rptools.maptool.client.swing.DragImageGlassPane;
import net.rptools.maptool.client.swing.GlassPane;
import net.rptools.maptool.client.swing.ImageCacheStatusBar;
import net.rptools.maptool.client.swing.ImageChooserDialog;
import net.rptools.maptool.client.swing.MemoryStatusBar;
import net.rptools.maptool.client.swing.PlayersLoadingStatusBar;
import net.rptools.maptool.client.swing.PositionalLayout;
import net.rptools.maptool.client.swing.ProgressStatusBar;
import net.rptools.maptool.client.swing.SpacerStatusBar;
import net.rptools.maptool.client.swing.StatusPanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.swing.TopologyModeSelectionPanel;
import net.rptools.maptool.client.swing.ZoomStatusBar;
import net.rptools.maptool.client.swing.colorpicker.ColorPicker;
import net.rptools.maptool.client.swing.preference.WindowPreferences;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.tool.Toolbox;
import net.rptools.maptool.client.ui.assetpanel.AssetDirectory;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.client.ui.connections.ClientConnectionPanel;
import net.rptools.maptool.client.ui.docking.MapToolDockingManager;
import net.rptools.maptool.client.ui.drawpanel.DrawPanelPopupMenu;
import net.rptools.maptool.client.ui.drawpanel.DrawPanelTreeCellRenderer;
import net.rptools.maptool.client.ui.drawpanel.DrawPanelTreeModel;
import net.rptools.maptool.client.ui.drawpanel.DrawablesPanel;
import net.rptools.maptool.client.ui.htmlframe.HTMLOverlayPanel;
import net.rptools.maptool.client.ui.lookuptable.LookupTablePanel;
import net.rptools.maptool.client.ui.macrobuttons.MacroButtonHotKeyManager;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.client.ui.macrobuttons.panels.*;
import net.rptools.maptool.client.ui.mappropertiesdialog.MapPropertiesDialog;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.dialog.edit.EditTokenDialog;
import net.rptools.maptool.client.ui.tokenpanel.InitiativePanel;
import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeCellRenderer;
import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeModel;
import net.rptools.maptool.client.ui.zone.PointerOverlay;
import net.rptools.maptool.client.ui.zone.PointerToolOverlay;
import net.rptools.maptool.client.ui.zone.ZoneMiniMapPanel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZoneFactory;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.util.ImageManager;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/** */
public class MapToolFrame extends DefaultDockableHolder implements WindowListener {
  private static final Logger log = LogManager.getLogger(MapToolFrame.class);
  private static final String INITIAL_LAYOUT_XML = "net/rptools/maptool/client/ui/ilayout.xml";
  private static final String CREDITS_HTML = "net/rptools/maptool/client/credits.html";
  private static final long serialVersionUID = 3905523813025329458L;
  private static final String DOCKING_PROFILE_NAME = "maptoolDocking";

  private static final int WINDOW_WIDTH = 800;
  private static final int WINDOW_HEIGHT = 600;

  private final Pen pen = new Pen(Pen.DEFAULT);
  private final Map<MTFrame, DockableFrame> frameMap = new HashMap<MTFrame, DockableFrame>();

  /** Are the drawing measurements being painted? */
  private boolean paintDrawingMeasurement = true;

  private ImageChooserDialog imageChooserDialog;
  private ZoneRenderer currentRenderer;

  // Components
  private final AssetPanel assetPanel;
  private final ClientConnectionPanel connectionPanel;

  /** The panel showing the initiative order. */
  private final InitiativePanel initiativePanel;

  /** The HTML pane showing the map overlay. */
  private HTMLOverlayPanel overlayPanel;

  private final PointerOverlay pointerOverlay;
  private final CommandPanel commandPanel;
  private final AboutDialog aboutDialog;
  private final ColorPicker colorPicker;
  private final Toolbox toolbox;
  private final ToolbarPanel toolbarPanel;
  private final ZoneMiniMapPanel zoneMiniMapPanel;

  /** Contains the zoneRenderer, as well as all overlays. */
  private final JPanel zoneRendererPanel;

  /** Contains the overlays that should be displayed in front of everything else. */
  private final PointerToolOverlay pointerToolOverlay;

  private JPanel visibleControlPanel;
  private FullScreenFrame fullScreenFrame;
  private JButton fullsZoneButton;
  private JPanel fullScreenToolPanel;
  private final JPanel rendererBorderPanel;
  private final List<ZoneRenderer> zoneRendererList;
  private final JMenuBar menuBar;
  private final StatusPanel statusPanel;
  private String statusMessage = "";
  private final ActivityMonitorPanel activityMonitor = new ActivityMonitorPanel();
  private final ProgressStatusBar progressBar = new ProgressStatusBar();
  private final ConnectionStatusPanel connectionStatusPanel = new ConnectionStatusPanel();
  private CoordinateStatusBar coordinateStatusBar;
  private AssetCacheStatusBar assetCacheStatusBar;
  private ImageCacheStatusBar imageCacheStatusBar;
  private AppHomeDiskSpaceStatusBar appHomeDiskSpaceStatusBar;
  private PlayersLoadingStatusBar playersLoadingStatusBar;
  private ZoomStatusBar zoomStatusBar;
  private JLabel chatActionLabel;
  private boolean fullScreenToolsShown;

  private Color chatTypingLabelColor;
  private ChatTypingNotification chatTypingPanel;
  private Timer chatTimer;
  private long chatNotifyDuration;
  private final ChatNotificationTimers chatTyperTimers;
  private GUID PreRemoveRenderGUID = null;

  private final GlassPane glassPane;

  /** Model for the token tree panel of the map explorer. */
  private TokenPanelTreeModel tokenPanelTreeModel;

  private DrawPanelTreeModel drawPanelTreeModel;
  private DrawablesPanel drawablesPanel;
  private final TextureChooserPanel textureChooserPanel;
  private LookupTablePanel lookupTablePanel;

  // External filename support
  private JFileChooser loadPropsFileChooser;
  private JFileChooser loadFileChooser;
  private JFileChooser saveCmpgnFileChooser;
  private JFileChooser savePropsFileChooser;
  private JFileChooser saveFileChooser;
  private JFileChooser saveMapFileChooser;
  private JFileChooser saveTokenFileChooser;

  private final FileFilter campaignFilter =
      new MTFileFilter(I18N.getText("file.ext.cmpgn"), "cmpgn");
  private final FileFilter mapFilter = new MTFileFilter(I18N.getText("file.ext.rpmap"), "rpmap");
  private final FileFilter propertiesFilter =
      new MTFileFilter(I18N.getText("file.ext.mtprops"), "mtprops");
  private final FileFilter macroFilter =
      new MTFileFilter(I18N.getText("file.ext.mtmacro"), "mtmacro");
  private final FileFilter macroSetFilter =
      new MTFileFilter(I18N.getText("file.ext.mtmacset"), "mtmacset");
  private final FileFilter tableFilter =
      new MTFileFilter(I18N.getText("file.ext.mttable"), "mttable");
  private final FileFilter dungeonDraftFilter =
      new MTFileFilter(I18N.getText("file.ext.dungeondraft"), "dd2vtt", "df2vtt", "uvtt");
  private EditTokenDialog tokenPropertiesDialog;

  private final CampaignPanel campaignPanel = new CampaignPanel();
  private final GmPanel gmPanel = new GmPanel();
  private final GlobalPanel globalPanel = new GlobalPanel();
  private final SelectionPanel selectionPanel = new SelectionPanel();
  private final ImpersonatePanel impersonatePanel = new ImpersonatePanel();

  private final DragImageGlassPane dragImageGlassPane = new DragImageGlassPane();

  private final class KeyListenerDeleteDraw implements KeyListener {
    private final JTree tree;

    private KeyListenerDeleteDraw(JTree tree) {
      this.tree = tree;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_DELETE) {
        EventQueue.invokeLater(
            () -> {
              TreePath[] selection = tree.getSelectionPaths();
              Set<GUID> selectedDrawSet = new HashSet<GUID>();
              if (selection != null) {
                for (TreePath path : selection) {
                  if (path.getLastPathComponent() instanceof DrawnElement) {
                    DrawnElement de = (DrawnElement) path.getLastPathComponent();
                    selectedDrawSet.add(de.getDrawable().getId());
                  }
                }
              }
              if (selectedDrawSet.isEmpty()) return;
              // check to see if this is the required action
              if (!MapTool.confirmDrawDelete()) {
                return;
              }
              for (GUID id : selectedDrawSet) {
                MapTool.serverCommand().undoDraw(getCurrentZoneRenderer().getZone().getId(), id);
              }
              getCurrentZoneRenderer().repaint();
              MapTool.getFrame().updateDrawTree();
              MapTool.getFrame().refresh();
            });
      }
    }

    @Override
    public void keyPressed(KeyEvent e) {}
  }

  private final class KeyListenerDeleteToken implements KeyListener {
    private final JTree tree;

    private KeyListenerDeleteToken(JTree tree) {
      this.tree = tree;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_DELETE) {
        EventQueue.invokeLater(
            () -> {
              // check to see if this is the required action
              if (!MapTool.confirmTokenDelete()) {
                return;
              }
              ZoneRenderer zr = getCurrentZoneRenderer();
              AppActions.deleteTokens(zr.getZone(), zr.getSelectedTokenSet());
            });
      }
    }

    @Override
    public void keyPressed(KeyEvent e) {}
  }

  public class ChatNotificationTimers {
    private final LinkedMap<String, Long> chatTypingNotificationTimers;

    public synchronized void setChatTyper(final String playerName) {
      if (AppPreferences.getTypingNotificationDuration() == 0) {
        turnOffUpdates();
        chatTypingNotificationTimers.clear();
      } else {
        MapTool.getFrame().getChatTimer().start();
        MapTool.getFrame().getChatTypingPanel().setVisible(true);
        chatTypingNotificationTimers.put(playerName, System.currentTimeMillis());
        updatePanel();
      }
    }

    private void updatePanel() {
      chatTypingPanel.invalidate();
      chatTypingPanel.repaint();
    }

    private void turnOffUpdates() {
      MapTool.getFrame().getChatTimer().stop();
      MapTool.getFrame().getChatTypingPanel().setVisible(false);
    }

    public synchronized void removeChatTyper(final String playerName) {
      chatTypingNotificationTimers.remove(playerName);
      if (chatTypingNotificationTimers.isEmpty()) turnOffUpdates();
      updatePanel();
    }

    public synchronized LinkedMap<String, Long> getChatTypers() {
      return new LinkedMap<>(chatTypingNotificationTimers);
    }

    public ChatNotificationTimers() {
      chatTypingNotificationTimers = new LinkedMap<>();
    }
  }

  public MapToolFrame(JMenuBar menuBar) {
    // Set up the frame
    super(AppConstants.APP_NAME);

    this.menuBar = menuBar;

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(this);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    SwingUtil.centerOnScreen(this);
    setFocusTraversalPolicy(new MapToolFocusTraversalPolicy());

    setIconImage(RessourceManager.getImage(Images.MAPTOOL_LOGO_MINI));
    // Notify duration
    initializeNotifyDuration();

    // Components
    glassPane = new GlassPane();
    assetPanel = createAssetPanel();
    connectionPanel = createConnectionPanel();
    toolbox = new Toolbox();
    initiativePanel = new InitiativePanel();
    overlayPanel = new HTMLOverlayPanel();

    zoneRendererList = new CopyOnWriteArrayList<ZoneRenderer>();
    pointerOverlay = new PointerOverlay();
    colorPicker = new ColorPicker(this);
    textureChooserPanel =
        new TextureChooserPanel(
            colorPicker.getPaintChooser(), assetPanel.getModel(), "imageExplorerTextureChooser");
    colorPicker.getPaintChooser().addPaintChooser(textureChooserPanel);

    String credits = "";
    String version = "";
    Image logo = null;
    try {
      credits = new String(FileUtil.loadResource(CREDITS_HTML), StandardCharsets.UTF_8);
      version = MapTool.getVersion();
      credits = credits.replace("%VERSION%", version);
      logo = RessourceManager.getImage(Images.MAPTOOL_LOGO);
    } catch (Exception ioe) {
      log.error(I18N.getText("msg.error.credits"), ioe);
      ioe.printStackTrace();
    }
    aboutDialog = new AboutDialog(this, logo, credits);
    aboutDialog.setSize(354, 400);

    statusPanel = new StatusPanel();

    statusPanel.addPanel(getAssetCacheStatusBar());
    statusPanel.addPanel(getImageCacheStatusBar());
    statusPanel.addPanel(getAppHomeDiskSpaceStatusBar());
    statusPanel.addPanel(getCoordinateStatusBar());
    statusPanel.addPanel(getZoomStatusBar());
    statusPanel.addPanel(getPlayersLoadingStatusBar());
    statusPanel.addPanel(MemoryStatusBar.getInstance());
    // statusPanel.addPanel(progressBar);
    statusPanel.addPanel(connectionStatusPanel);
    statusPanel.addPanel(activityMonitor);
    statusPanel.addPanel(new SpacerStatusBar(25));

    zoneMiniMapPanel = new ZoneMiniMapPanel();
    // zoneMiniMapPanel.setSize(100, 100);

    zoneRendererPanel = new JPanel(new PositionalLayout(5));
    zoneRendererPanel.setBackground(Color.black);
    // zoneRendererPanel.add(zoneMiniMapPanel, PositionalLayout.Position.SE);
    // zoneRendererPanel.add(getChatTypingLabel(), PositionalLayout.Position.NW);
    zoneRendererPanel.add(getChatTypingPanel(), PositionalLayout.Position.NW);
    zoneRendererPanel.add(getChatActionLabel(), PositionalLayout.Position.SW);

    commandPanel = new CommandPanel();

    rendererBorderPanel = new JPanel(new GridLayout());
    rendererBorderPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
    rendererBorderPanel.add(zoneRendererPanel);
    toolbarPanel = new ToolbarPanel(toolbox);

    zoneRendererPanel.add(overlayPanel, PositionalLayout.Position.CENTER, 0);
    overlayPanel.setVisible(false); // disabled by default

    pointerToolOverlay = new PointerToolOverlay();
    zoneRendererPanel.add(pointerToolOverlay, PositionalLayout.Position.CENTER, 0);

    // Put it all together
    setJMenuBar(menuBar);
    add(BorderLayout.NORTH, toolbarPanel);
    add(BorderLayout.SOUTH, statusPanel);

    JLayeredPane glassPaneComposite = new JLayeredPane();
    glassPaneComposite.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1;
    constraints.weighty = 1;

    glassPaneComposite.add(glassPane, constraints);
    glassPaneComposite.add(dragImageGlassPane, constraints);

    setGlassPane(glassPane);
    // setGlassPane(glassPaneComposite);

    glassPaneComposite.setVisible(true);

    if (!AppUtil.MAC_OS_X) removeWindowsF10();
    else registerForMacOSXEvents();

    new MapToolEventBus().getMainEventBus().register(this);

    restorePreferences();
    updateKeyStrokes();

    // This will cause the frame to be set to visible (BAD jide, BAD! No cookie for you!)
    configureDocking();

    new WindowPreferences(AppConstants.APP_NAME, "mainFrame", this);
    chatTyperTimers = new ChatNotificationTimers();
    chatTimer = getChatTimer();
    setChatTypingLabelColor(AppPreferences.getChatNotificationColor());
  }

  public ChatNotificationTimers getChatNotificationTimers() {
    return chatTyperTimers;
  }

  public void registerForMacOSXEvents() {
    try {
      Desktop.getDesktop()
          .setQuitHandler(
              (arg0, arg1) -> {
                ((ClientAction) AppActions.EXIT).execute(null);
                /*
                 * Always tell the OS to cancel the quit operation -- we're doing it ourselves. Unfortunately, if the user was trying to logout, the logout operation is now cancelled, too! We
                 * can't use performQuit() because that is documented to call System.exit(0) and we may not be done with what we're doing. That just leaves not calling either one -- that may turn
                 * out to be the best option in the long run.
                 */
                arg1.cancelQuit();
              });
      Desktop.getDesktop()
          .setAboutHandler(arg0 -> ((ClientAction) AppActions.SHOW_ABOUT).execute(null));
      Desktop.getDesktop()
          .setPreferencesHandler(
              arg0 -> ((ClientAction) AppActions.SHOW_PREFERENCES).execute(null));
    } catch (Exception e) {
      String msg = "Error while configuring Desktop interaction";
      log.error(msg, e);
      System.err.println(msg);
    }
  }

  public DragImageGlassPane getDragImageGlassPane() {
    return dragImageGlassPane;
  }

  public ImageChooserDialog getImageChooserDialog() {
    if (imageChooserDialog == null) {
      imageChooserDialog = new ImageChooserDialog(this);
    }
    return imageChooserDialog;
  }

  public enum MTFrame {
    /*
     * These enums should be specified using references to the properties file. However, a simple toString() method is used later to determine what to display on the various panels. So if I
     * convert the propName into the value from the properties file and return it, parts of the code later on use that string to do a properties file lookup! That means that any code using MTFrame
     * enums that are converted to Strings need to be checked so that when the return value is used as the NAME of an Action, the property name is retrieved instead. Ugh. :(
     *
     * We'll need two additional methods: getPropName() and getDisplayName(). Perhaps toString() could call getDisplayName(), but it might be much simpler to debug if toString() weren't used. In
     * that case, there's no reason to use an enum either ... may as well use a class with static final objects in it. Sigh.
     */
    // @formatter:off
    CONNECTIONS("Connections"),
    TOKEN_TREE("MapExplorer"),
    DRAW_TREE("DrawExplorer"),
    INITIATIVE("Initiative"),
    IMAGE_EXPLORER("Library"),
    CHAT("Chat"),
    LOOKUP_TABLES("Tables"),
    GLOBAL("Global"),
    CAMPAIGN("Campaign"),
    GM("Gm"),
    SELECTION("Selected"),
    IMPERSONATED("Impersonate");
    // @formatter:on

    private String displayName;

    MTFrame(String dispName) {
      displayName = dispName;
    }

    @Override
    public String toString() {
      return displayName;
    }

    public String getPropertyName() {
      return "panel." + displayName;
    }
  }

  private void configureDocking() {
    initializeFrames();

    getDockingManager().setProfileKey(DOCKING_PROFILE_NAME);
    getDockingManager().setOutlineMode(com.jidesoft.docking.DockingManager.PARTIAL_OUTLINE_MODE);
    getDockingManager().setUsePref(false);

    getDockingManager().getWorkspace().setAcceptDockableFrame(false);

    // Main panel
    getDockingManager().getWorkspace().add(rendererBorderPanel);

    // Add all dockable frames
    for (DockableFrame frame : frameMap.values()) {
      getDockingManager().addFrame(frame);
    }

    /* Issue #2485
     * Layout data is only retained for frames that already exist, so to work around this
     * create some placeholder frames using the names saved to frames.dat before loading
     * the layout
     */
    String[] frameNames = null;
    try {
      Path path = Paths.get(AppUtil.getAppHome("config").getAbsolutePath() + "/frames.dat");
      String data = Files.readString(path, StandardCharsets.UTF_8);

      if (!data.isEmpty()) {
        frameNames = data.split("\0");

        for (String name : frameNames) {
          if (name.isBlank()) continue;
          getDockingManager().addFrame(new DockableFrame(name));
        }
      }
    } catch (NoSuchFileException nsfe) {
      // Do nothing
    } catch (IOException ioe) {
      log.error("Unable to load frames.dat", ioe);
    }
    /* /Issue #2485 */

    try {
      getDockingManager()
          .loadInitialLayout(
              MapToolFrame.class.getClassLoader().getResourceAsStream(INITIAL_LAYOUT_XML));
    } catch (ParserConfigurationException | SAXException | IOException e) {
      MapTool.showError("msg.error.layoutInitial", e);
    }
    try {
      getDockingManager()
          .loadLayoutDataFromFile(AppUtil.getAppHome("config").getAbsolutePath() + "/layout.dat");
    } catch (IllegalArgumentException e) {
      // This error sometimes comes up when using three monitors due to a bug in the java jdk
      // incorrectly
      // reporting screen size as zero.
      MapTool.showError("msg.error.layoutParse", e);
    }

    // Update frame titles to their localized names. This needs to be done after the layout is
    // loaded otherwise the titles default to English
    for (MTFrame mtFrame : frameMap.keySet()) {
      setFrameTitle(mtFrame, I18N.getText(mtFrame.getPropertyName()));
    }

    /* Issue #2485 */
    if (frameNames != null) {
      for (String name : frameNames) {
        if (name.isBlank()) continue;
        getDockingManager().hideFrame(name);
      }
    }
    /* /Issue #2485 */
  }

  public DockableFrame getFrame(MTFrame frame) {
    return frameMap.get(frame);
  }

  @Override
  protected DockingManager createDockingManager(Container container) {
    return new MapToolDockingManager(this, container);
  }

  private void initializeFrames() {
    frameMap.put(
        MTFrame.CONNECTIONS,
        createDockingFrame(
            MTFrame.CONNECTIONS,
            connectionPanel,
            RessourceManager.getSmallIcon(Icons.WINDOW_CONNECTIONS)));
    frameMap.put(
        MTFrame.TOKEN_TREE,
        createDockingFrame(
            MTFrame.TOKEN_TREE,
            new JScrollPane(createTokenTreePanel()),
            RessourceManager.getSmallIcon(Icons.WINDOW_MAP_EXPLORER)));
    frameMap.put(
        MTFrame.IMAGE_EXPLORER,
        createDockingFrame(
            MTFrame.IMAGE_EXPLORER,
            assetPanel,
            RessourceManager.getSmallIcon(Icons.WINDOW_LIBRARY)));
    frameMap.put(
        MTFrame.DRAW_TREE,
        createDockingFrame(
            MTFrame.DRAW_TREE,
            new JScrollPane(createDrawTreePanel()),
            RessourceManager.getSmallIcon(Icons.WINDOW_DRAW_EXPLORER)));
    frameMap.put(
        MTFrame.CHAT,
        createDockingFrame(
            MTFrame.CHAT, commandPanel, RessourceManager.getSmallIcon(Icons.WINDOW_CHAT)));
    frameMap.put(
        MTFrame.LOOKUP_TABLES,
        createDockingFrame(
            MTFrame.LOOKUP_TABLES,
            getLookupTablePanel(),
            RessourceManager.getSmallIcon(Icons.WINDOW_TABLES)));
    frameMap.put(
        MTFrame.INITIATIVE,
        createDockingFrame(
            MTFrame.INITIATIVE,
            initiativePanel,
            RessourceManager.getSmallIcon(Icons.WINDOW_INITIATIVE)));

    JScrollPane campaign = scrollPaneFactory(campaignPanel);
    JScrollPane gm = scrollPaneFactory(gmPanel);
    JScrollPane global = scrollPaneFactory(globalPanel);

    JPanel selection = new JPanel(new BorderLayout());
    selection.add(scrollPaneFactory(selectionPanel), BorderLayout.CENTER);
    selection.add(new MenuButtonsPanel(), BorderLayout.NORTH);

    JScrollPane impersonate = scrollPaneFactory(impersonatePanel);
    frameMap.put(
        MTFrame.GLOBAL,
        createDockingFrame(
            MTFrame.GLOBAL, global, RessourceManager.getSmallIcon(Icons.WINDOW_GLOBAL_MACROS)));
    frameMap.put(
        MTFrame.CAMPAIGN,
        createDockingFrame(
            MTFrame.CAMPAIGN,
            campaign,
            RessourceManager.getSmallIcon(Icons.WINDOW_CAMPAIGN_MACROS)));
    frameMap.put(
        MTFrame.GM,
        createDockingFrame(MTFrame.GM, gm, RessourceManager.getSmallIcon(Icons.WINDOW_GM_MACROS)));
    frameMap.put(
        MTFrame.SELECTION,
        createDockingFrame(
            MTFrame.SELECTION,
            selection,
            RessourceManager.getSmallIcon(Icons.WINDOW_SELECTED_TOKEN)));
    frameMap.put(
        MTFrame.IMPERSONATED,
        createDockingFrame(
            MTFrame.IMPERSONATED,
            impersonate,
            RessourceManager.getSmallIcon(Icons.WINDOW_IMPERSONATED_MACROS)));
  }

  private JScrollPane scrollPaneFactory(JPanel panel) {
    JScrollPane pane =
        new JScrollPane(
            panel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    pane.getViewport().setBorder(null);
    return pane;
  }

  private static DockableFrame createDockingFrame(MTFrame mtFrame, Component component, Icon icon) {
    DockableFrame frame = new DockableFrame(mtFrame.name(), icon);
    frame.add(component);
    frame.addDockableFrameListener(new MapToolDockListener());
    return frame;
  }

  /**
   * Updates the window title, tab title, and side title of the given frame
   *
   * @param mtFrame The frame to set the title of
   * @param title The new title
   */
  public void setFrameTitle(MTFrame mtFrame, String title) {
    DockableFrame frame = getFrame(mtFrame);
    if (frame != null) {
      frame.setTitle(title);
      frame.setTabTitle(title);
      frame.setSideTitle(title);
    }
  }

  public LookupTablePanel getLookupTablePanel() {
    if (lookupTablePanel == null) {
      lookupTablePanel = new LookupTablePanel();
    }
    return lookupTablePanel;
  }

  /**
   * Shows the token properties dialog, and saves the token.
   *
   * @param token the token to edit
   * @param zr the ZoneRenderer of the token
   */
  public void showTokenPropertiesDialog(Token token, ZoneRenderer zr) {
    if (token != null && zr != null) {
      if (MapTool.getPlayer().isGM() || !MapTool.getServerPolicy().isTokenEditorLocked()) {
        EditTokenDialog dialog = MapTool.getFrame().getTokenPropertiesDialog();
        dialog.showDialog(token);
        if (dialog.isTokenSaved()) {
          // Checks if the map still exists. Fixes #1646.
          if (getZoneRenderers().contains(zr) && zr.getZone().getToken(token.getId()) != null) {
            MapTool.serverCommand().editToken(zr.getZone().getId(), token);
          }
        }
      }
    }
  }

  private EditTokenDialog getTokenPropertiesDialog() {
    if (tokenPropertiesDialog == null) {
      tokenPropertiesDialog = new EditTokenDialog();
    }
    return tokenPropertiesDialog;
  }

  /** Repaints the current ZoneRenderer, if it is not null. */
  public void refresh() {
    if (getCurrentZoneRenderer() != null) {
      getCurrentZoneRenderer().repaint();
    }
  }

  /** Accepts 1 or more file extensions */
  private static class MTFileFilter extends FileFilter {
    private final String[] extensions;
    private final String description;

    MTFileFilter(String desc, String... extens) {
      super();
      extensions = extens;
      description = desc;
    }

    /** Accept directories and files matching any of the provided extensions */
    @Override
    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }
      String fext = getExtension(f);
      for (String ext : extensions) {
        if (fext != null && fext.equals(ext)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String getDescription() {
      return description;
    }

    public String getExtension(File f) {
      String ext = null;
      String s = f.getName();
      int i = s.lastIndexOf('.');

      if (i > 0 && i < s.length() - 1) {
        ext = s.substring(i + 1).toLowerCase();
      }
      return ext;
    }
  }

  public FileFilter getCmpgnFileFilter() {
    return campaignFilter;
  }

  public FileFilter getMapFileFilter() {
    return mapFilter;
  }

  /**
   * Returns the {@link FileFilter} for Univerasl VTT export files.
   *
   * @return the {@link FileFilter} for Universal VTT export files.
   */
  public FileFilter getDungeonDraftFilter() {
    return dungeonDraftFilter;
  }

  public JFileChooser getLoadPropsFileChooser() {
    if (loadPropsFileChooser == null) {
      loadPropsFileChooser = new JFileChooser();
      loadPropsFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
      loadPropsFileChooser.addChoosableFileFilter(propertiesFilter);
      loadPropsFileChooser.setDialogTitle(I18N.getText("msg.title.importProperties"));
    }
    loadPropsFileChooser.setFileFilter(propertiesFilter);
    return loadPropsFileChooser;
  }

  public JFileChooser getLoadFileChooser() {
    if (loadFileChooser == null) {
      loadFileChooser = new JFileChooser();
      loadFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
    }
    return loadFileChooser;
  }

  public JFileChooser getSaveCmpgnFileChooser() {
    if (saveCmpgnFileChooser == null) {
      saveCmpgnFileChooser = new JFileChooser();
      saveCmpgnFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
      saveCmpgnFileChooser.addChoosableFileFilter(campaignFilter);
      saveCmpgnFileChooser.setDialogTitle(I18N.getText("msg.title.saveCampaign"));
    }
    saveCmpgnFileChooser.setAcceptAllFileFilterUsed(true);
    return saveCmpgnFileChooser;
  }

  public JFileChooser getSavePropsFileChooser() {
    if (savePropsFileChooser == null) {
      savePropsFileChooser = new JFileChooser();
      savePropsFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
      savePropsFileChooser.addChoosableFileFilter(propertiesFilter);
      savePropsFileChooser.setDialogTitle(I18N.getText("msg.title.exportProperties"));
    }
    savePropsFileChooser.setAcceptAllFileFilterUsed(true);
    return savePropsFileChooser;
  }

  public JFileChooser getSaveTokenFileChooser() {
    if (saveTokenFileChooser == null) {
      saveTokenFileChooser = new JFileChooser();
      saveTokenFileChooser.setCurrentDirectory(AppPreferences.getSaveTokenDir());
    }
    return saveTokenFileChooser;
  }

  public JFileChooser getSaveMapFileChooser() {
    if (saveMapFileChooser == null) {
      saveMapFileChooser = new JFileChooser();
      saveMapFileChooser.setCurrentDirectory(AppPreferences.getSaveMapDir());
    }
    return saveMapFileChooser;
  }

  public JFileChooser getSaveFileChooser() {
    if (saveFileChooser == null) {
      saveFileChooser = new JFileChooser();
      saveFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
    }
    return saveFileChooser;
  }

  /**
   * Show the control panel. If panels is not empty then the control panel's contents are replaced
   * with the passed contents. If it is empty (no arguments) then it will restore the control panel
   * if it is hidden.
   *
   * @param panels The panels to add to control panel, or empty to restore hidden control panel.
   * @see #hideControlPanel()
   */
  public void showControlPanel(JPanel... panels) {
    if (panels.length == 0) {
      if (visibleControlPanel != null) {
        visibleControlPanel.setVisible(true);
      }
      return;
    }

    JPanel layoutPanel = new JPanel(new GridBagLayout());
    layoutPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

    int i = 0;
    for (JPanel panel : panels) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 1;
      gbc.gridy = i;
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.BOTH;
      layoutPanel.add(panel, gbc);
      i++;
    }

    layoutPanel.setSize(layoutPanel.getPreferredSize());
    zoneRendererPanel.add(layoutPanel, PositionalLayout.Position.NE);
    zoneRendererPanel.setComponentZOrder(layoutPanel, 0);
    zoneRendererPanel.revalidate();
    zoneRendererPanel.repaint();
    visibleControlPanel = layoutPanel;
  }

  public PlayersLoadingStatusBar getPlayersLoadingStatusBar() {
    if (playersLoadingStatusBar == null) {
      playersLoadingStatusBar = new PlayersLoadingStatusBar();
    }
    return playersLoadingStatusBar;
  }

  public ZoomStatusBar getZoomStatusBar() {
    if (zoomStatusBar == null) {
      zoomStatusBar = new ZoomStatusBar();
    }
    return zoomStatusBar;
  }

  public AssetCacheStatusBar getAssetCacheStatusBar() {
    if (assetCacheStatusBar == null) {
      assetCacheStatusBar = new AssetCacheStatusBar();
    }
    return assetCacheStatusBar;
  }

  public ImageCacheStatusBar getImageCacheStatusBar() {
    if (imageCacheStatusBar == null) {
      imageCacheStatusBar = new ImageCacheStatusBar();
    }
    return imageCacheStatusBar;
  }

  public AppHomeDiskSpaceStatusBar getAppHomeDiskSpaceStatusBar() {
    if (appHomeDiskSpaceStatusBar == null) {
      appHomeDiskSpaceStatusBar = new AppHomeDiskSpaceStatusBar();
    }
    return appHomeDiskSpaceStatusBar;
  }

  public CoordinateStatusBar getCoordinateStatusBar() {
    if (coordinateStatusBar == null) {
      coordinateStatusBar = new CoordinateStatusBar();
    }
    return coordinateStatusBar;
  }

  /**
   * Removes the control panel. IF you want to temporarily hide the control panel use {@link
   * #hideControlPanel()}.
   *
   * @see #hideControlPanel()
   * @see #showControlPanel(JPanel...)
   */
  public void removeControlPanel() {
    if (visibleControlPanel != null) {
      if (zoneRendererPanel != null) {
        zoneRendererPanel.remove(visibleControlPanel);
      }
      visibleControlPanel = null;
      refresh();
    }
  }

  /**
   * Hides but does not remove the current control panel. To restore the control panel use {@link
   * #showControlPanel(JPanel...)} with an empty argument list.
   *
   * @see #showControlPanel(JPanel...)
   * @see #removeControlPanel()
   */
  public void hideControlPanel() {
    if (visibleControlPanel != null) {
      visibleControlPanel.setVisible(false);
    }
  }

  public void showNonModalGlassPane(JComponent component, int x, int y) {
    showGlassPane(component, x, y, false);
  }

  public void showModalGlassPane(JComponent component, int x, int y) {
    showGlassPane(component, x, y, true);
  }

  private void showGlassPane(JComponent component, int x, int y, boolean modal) {
    glassPane.removeAll();
    component.setSize(component.getPreferredSize());
    component.setLocation(x, y);
    glassPane.setLayout(null);
    glassPane.add(component);
    glassPane.setModel(modal);
    glassPane.setVisible(true);
  }

  public void showFilledGlassPane(JComponent component) {
    glassPane.removeAll();
    glassPane.setLayout(new GridLayout());
    glassPane.add(component);
    // glassPane.setActionMap(null);
    // glassPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
    glassPane.setVisible(true);
  }

  public void hideGlassPane() {
    glassPane.removeAll();
    glassPane.setVisible(false);
  }

  public ChatTypingNotification getChatTypingPanel() {
    if (chatTypingPanel == null) {
      chatTypingPanel = new ChatTypingNotification();
      chatTypingPanel.setOpaque(false);
      chatTypingPanel.setSize(220, 100); // FIXME change to variable width
      chatTypingPanel.setEnabled(true);
      chatTypingPanel.setVisible(false); // Only visible when there are notifications to display
    }
    return chatTypingPanel;
  }

  public Color getChatTypingLabelColor() {
    if (chatTypingLabelColor == null) {
      chatTypingLabelColor = Color.BLACK;
    }
    return chatTypingLabelColor;
  }

  public void setChatTypingLabelColor(Color color) {
    if (color != null) {
      chatTypingLabelColor = color;
    }
  }

  public void setChatNotifyDuration(int duration) {
    chatNotifyDuration = duration;
  }

  private void initializeNotifyDuration() {
    chatNotifyDuration = AppPreferences.getTypingNotificationDuration();
  }

  public JLabel getChatActionLabel() {
    if (chatActionLabel == null) {
      chatActionLabel = new JLabel(RessourceManager.getSmallIcon(Icons.CHAT_NOTIFICATION));
      chatActionLabel.setSize(chatActionLabel.getPreferredSize());
      chatActionLabel.setVisible(false);
      chatActionLabel.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              showCommandPanel();
            }
          });
    }
    return chatActionLabel;
  }

  public boolean isCommandPanelVisible() {
    return getFrame(MTFrame.CHAT).isShowing();
  }

  public void showCommandPanel() {
    chatActionLabel.setVisible(false);
    getDockingManager().showFrame(MTFrame.CHAT.name());
    commandPanel.requestFocus();
  }

  public void hideCommandPanel() {
    getDockingManager().hideFrame(MTFrame.CHAT.name());
  }

  public ColorPicker getColorPicker() {
    return colorPicker;
  }

  public void showAboutDialog() {
    aboutDialog.setVisible(true);
  }

  public ConnectionStatusPanel getConnectionStatusPanel() {
    return connectionStatusPanel;
  }

  private void restorePreferences() {
    Set<File> assetRootList = AppPreferences.getAssetRoots();
    for (File file : assetRootList) {
      addAssetRoot(file);
    }
  }

  private JComponent createDrawTreePanel() {
    final JTree tree = new JTree();
    drawablesPanel = new DrawablesPanel();
    drawPanelTreeModel = new DrawPanelTreeModel(tree);
    tree.setModel(drawPanelTreeModel);
    tree.setCellRenderer(new DrawPanelTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setContinuousLayout(true);

    splitPane.setTopComponent(new JScrollPane(tree));
    splitPane.setBottomComponent(drawablesPanel);
    splitPane.setDividerLocation(100);
    // Add tree selection listener
    tree.addTreeSelectionListener(
        e -> {
          TreePath path = e.getPath();
          if (path == null) {
            return;
          }
          int[] treeRows = tree.getSelectionRows();
          java.util.Arrays.sort(treeRows);
          drawablesPanel.clearSelectedIds();
          for (int treeRow : treeRows) {
            TreePath p = tree.getPathForRow(treeRow);
            if (p.getLastPathComponent() instanceof DrawnElement) {
              DrawnElement de = (DrawnElement) p.getLastPathComponent();
              drawablesPanel.addSelectedId(de.getDrawable().getId());
            }
          }
        });

    tree.addKeyListener(new KeyListenerDeleteDraw(tree));

    // Add mouse Event for right click menu
    tree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path == null) {
              return;
            }
            Object row = path.getLastPathComponent();
            int rowIndex = tree.getRowForLocation(e.getX(), e.getY());
            if (SwingUtilities.isLeftMouseButton(e)) {
              if (!SwingUtil.isShiftDown(e) && !SwingUtil.isControlDown(e)) {
                tree.clearSelection();
              }
              tree.addSelectionInterval(rowIndex, rowIndex);
              if (row instanceof DrawnElement && e.getClickCount() == 2) {
                DrawnElement de = (DrawnElement) row;
                getCurrentZoneRenderer()
                    .centerOn(
                        new ZonePoint(
                            (int) de.getDrawable().getBounds().getCenterX(),
                            (int) de.getDrawable().getBounds().getCenterY()));
              }
              /*
               * int[] treeRows = tree.getSelectionRows(); java.util.Arrays.sort(treeRows); drawablesPanel.clearSelectedIds(); for (int i = 0; i < treeRows.length; i++) { TreePath p =
               * tree.getPathForRow(treeRows[i]); if (p.getLastPathComponent() instanceof DrawnElement) { DrawnElement de = (DrawnElement) p.getLastPathComponent();
               * drawablesPanel.addSelectedId(de.getDrawable().getId()); } }
               */
            }
            if (SwingUtilities.isRightMouseButton(e)) {
              if (!isRowSelected(tree.getSelectionRows(), rowIndex) && !SwingUtil.isShiftDown(e)) {
                tree.clearSelection();
                tree.addSelectionInterval(rowIndex, rowIndex);
                drawablesPanel.clearSelectedIds();
              }
              final int x = e.getX();
              final int y = e.getY();
              EventQueue.invokeLater(
                  () -> {
                    DrawnElement firstElement = null;
                    Set<GUID> selectedDrawSet = new HashSet<GUID>();
                    boolean topLevelOnly = true;
                    for (TreePath path1 : tree.getSelectionPaths()) {
                      if (path1.getPathCount() != 3) topLevelOnly = false;
                      if (path1.getLastPathComponent() instanceof DrawnElement) {
                        DrawnElement de = (DrawnElement) path1.getLastPathComponent();
                        if (firstElement == null) {
                          firstElement = de;
                        }
                        selectedDrawSet.add(de.getDrawable().getId());
                      }
                    }
                    if (!selectedDrawSet.isEmpty()) {
                      try {
                        new DrawPanelPopupMenu(
                                selectedDrawSet,
                                x,
                                y,
                                getCurrentZoneRenderer(),
                                firstElement,
                                topLevelOnly)
                            .showPopup(tree);
                      } catch (IllegalComponentStateException icse) {
                        log.info(tree.toString(), icse);
                      }
                    }
                  });
            }
          }
        });
    return splitPane;
  }

  // Used to redraw the Draw Tree Panel after actions have been called
  public void updateDrawTree() {
    if (drawPanelTreeModel != null) {
      drawPanelTreeModel.update();
      drawablesPanel.clearSelectedIds();
    }
  }

  /** Create the token tree panel for the map explorer */
  private JComponent createTokenTreePanel() {
    final JTree tree = new JTree();
    tokenPanelTreeModel = new TokenPanelTreeModel(tree);
    tree.setModel(tokenPanelTreeModel);
    tree.setCellRenderer(new TokenPanelTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    tree.addKeyListener(new KeyListenerDeleteToken(tree));

    tree.addMouseListener(
        new MouseAdapter() {
          // TODO: Make this a handler class, not an aic
          @Override
          public void mousePressed(MouseEvent e) {
            // tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path == null) {
              return;
            }
            Object row = path.getLastPathComponent();
            int rowIndex = tree.getRowForLocation(e.getX(), e.getY());
            if (SwingUtilities.isLeftMouseButton(e)) {
              if (!SwingUtil.isShiftDown(e) && !SwingUtil.isControlDown(e)) {
                tree.clearSelection();
              }
              tree.addSelectionInterval(rowIndex, rowIndex);

              if (row instanceof Token && e.getClickCount() == 2) {
                Token token = (Token) row;
                getCurrentZoneRenderer().centerOnAndSetSelected(token);
              }
            }
            if (SwingUtilities.isRightMouseButton(e)) {
              if (!isRowSelected(tree.getSelectionRows(), rowIndex) && !SwingUtil.isShiftDown(e)) {
                tree.clearSelection();
                tree.addSelectionInterval(rowIndex, rowIndex);
              }
              final int x = e.getX();
              final int y = e.getY();
              EventQueue.invokeLater(
                  () -> {
                    Token firstToken = null;
                    Set<GUID> selectedTokenSet = new HashSet<GUID>();
                    for (TreePath path1 : tree.getSelectionPaths()) {
                      if (path1.getLastPathComponent() instanceof Token) {
                        Token token = (Token) path1.getLastPathComponent();
                        if (firstToken == null) {
                          firstToken = token;
                        }
                        if (AppUtil.playerOwns(token)) {
                          selectedTokenSet.add(token.getId());
                        }
                      }
                    }
                    if (!selectedTokenSet.isEmpty()) {
                      try {
                        if (firstToken.getLayer().isStampLayer()) {
                          new StampPopupMenu(
                                  selectedTokenSet, x, y, getCurrentZoneRenderer(), firstToken)
                              .showPopup(tree);
                        } else {
                          new TokenPopupMenu(
                                  selectedTokenSet, x, y, getCurrentZoneRenderer(), firstToken)
                              .showPopup(tree);
                        }
                      } catch (IllegalComponentStateException icse) {
                        log.info(tree.toString(), icse);
                      }
                    }
                  });
            }
          }
        });
    return tree;
  }

  public void clearTokenTree() {
    if (tokenPanelTreeModel != null) {
      tokenPanelTreeModel.setZone(null);
    }
  }

  /** Update tokenPanelTreeModel and the initiativePanel. */
  public void updateTokenTree() {
    if (tokenPanelTreeModel != null) {
      tokenPanelTreeModel.update();
    }
    if (initiativePanel != null) {
      initiativePanel.update();
    }
  }

  private boolean isRowSelected(int[] selectedRows, int row) {
    if (selectedRows == null) {
      return false;
    }
    for (int selectedRow : selectedRows) {
      if (row == selectedRow) {
        return true;
      }
    }
    return false;
  }

  private ClientConnectionPanel createConnectionPanel() {
    ClientConnectionPanel panel = new ClientConnectionPanel();
    return panel;
  }

  private AssetPanel createAssetPanel() {
    final AssetPanel panel = new AssetPanel("mainAssetPanel");
    panel.addImagePanelMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent e) {
            // TODO use for real popup logic
            // if (SwingUtilities.isLeftMouseButton(e)) {
            // if (e.getClickCount() == 2) {
            //
            // List<Object> idList = panel.getSelectedIds();
            // if (idList == null || idList.size() == 0) {
            // return;
            // }
            //
            // final int index = (Integer) idList.get(0);
            // createZone(panel.getAsset(index));
            // }
            // }
            if (SwingUtilities.isRightMouseButton(e) && MapTool.getPlayer().isGM()) {
              List<Object> idList = panel.getSelectedIds();
              if (idList == null || idList.size() == 0) {
                return;
              }
              final int index = (Integer) idList.get(0);

              JPopupMenu menu = new JPopupMenu();
              menu.add(
                  new JMenuItem(
                      new AbstractAction() {
                        {
                          putValue(NAME, I18N.getText("action.newMap"));
                        }

                        public void actionPerformed(ActionEvent e) {
                          createZone(panel.getAsset(index));
                        }
                      }));
              panel.showImagePanelPopup(menu, e.getX(), e.getY());
            }
          }

          private void createZone(Asset asset) {
            Zone zone = ZoneFactory.createZone();
            zone.setName(asset.getName());
            BufferedImage image = ImageManager.getImageAndWait(asset.getMD5Key());
            if (image.getWidth() < 200 || image.getHeight() < 200) {
              zone.setBackgroundPaint(new DrawableTexturePaint(asset));
              zone.setBackgroundAsset(asset.getMD5Key());
            } else {
              zone.setMapAsset(asset.getMD5Key());
              zone.setBackgroundPaint(new DrawableColorPaint(Color.black));
              zone.setBackgroundAsset(asset.getMD5Key());
            }
            MapPropertiesDialog newMapDialog =
                MapPropertiesDialog.createMapPropertiesDialog(MapTool.getFrame());
            newMapDialog.setZone(zone);
            newMapDialog.setVisible(true);

            if (newMapDialog.getStatus() == MapPropertiesDialog.Status.OK) {
              MapTool.addZone(zone);
            }
          }
        });
    return panel;
  }

  public PointerOverlay getPointerOverlay() {
    return pointerOverlay;
  }

  public void setStatusMessage(final String message) {
    statusMessage = message;
    SwingUtilities.invokeLater(() -> statusPanel.setStatus("  " + message));
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public ActivityMonitorPanel getActivityMonitor() {
    return activityMonitor;
  }

  public void startIndeterminateAction() {
    progressBar.startIndeterminate();
  }

  public void endIndeterminateAction() {
    progressBar.endIndeterminate();
  }

  public void startDeterminateAction(int totalWork) {
    progressBar.startDeterminate(totalWork);
  }

  public void updateDeterminateActionProgress(int additionalWorkCompleted) {
    progressBar.updateDeterminateProgress(additionalWorkCompleted);
  }

  public void endDeterminateAction() {
    progressBar.endDeterminate();
  }

  public ZoneMiniMapPanel getZoneMiniMapPanel() {
    return zoneMiniMapPanel;
  }

  // /////////////////////////////////////////////////////////////////////////
  // static methods
  // /////////////////////////////////////////////////////////////////////////

  public CommandPanel getCommandPanel() {
    return commandPanel;
  }

  public ClientConnectionPanel getConnectionPanel() {
    return connectionPanel;
  }

  public AssetPanel getAssetPanel() {
    return assetPanel;
  }

  public DrawablesPanel getDrawablesPanel() {
    return drawablesPanel;
  }

  public void addAssetRoot(File rootDir) {
    assetPanel.addAssetRoot(new AssetDirectory(rootDir, AppConstants.IMAGE_FILE_FILTER));
  }

  public Pen getPen() {
    pen.setPaint(DrawablePaint.convertPaint(colorPicker.getForegroundPaint()));
    pen.setBackgroundPaint(DrawablePaint.convertPaint(colorPicker.getBackgroundPaint()));
    pen.setThickness(colorPicker.getStrokeWidth());
    pen.setOpacity(colorPicker.getOpacity());
    pen.setThickness(colorPicker.getStrokeWidth());
    return pen;
  }

  public List<ZoneRenderer> getZoneRenderers() {
    // TODO: This should prob be immutable
    return zoneRendererList;
  }

  public ZoneRenderer getCurrentZoneRenderer() {
    return currentRenderer;
  }

  /**
   * @return the HTML Overlay Panel
   */
  public HTMLOverlayPanel getOverlayPanel() {
    return overlayPanel;
  }

  public void addZoneRenderer(ZoneRenderer renderer) {
    zoneRendererList.add(renderer);
    if (renderer.getZone().getId().equals(this.PreRemoveRenderGUID)) {
      if (MapTool.getPlayer().isGM() || renderer.getZone().isVisible()) {
        this.PreRemoveRenderGUID = null;
        setCurrentZoneRenderer(renderer);
      } else {
        this.PreRemoveRenderGUID = null;
      }
    } else {
      this.PreRemoveRenderGUID = null;
    }
  }

  /**
   * Remove the ZoneRenderer. If it's the current ZoneRenderer, set a new current ZoneRenderer.
   * Flush zoneMiniMapPanel.
   *
   * @param renderer the ZoneRenderer to remove.
   */
  public void removeZoneRenderer(ZoneRenderer renderer) {
    boolean isCurrent = renderer == getCurrentZoneRenderer();
    this.PreRemoveRenderGUID = getCurrentZoneRenderer().getZone().getId();
    zoneRendererList.remove(renderer);
    if (isCurrent) {
      boolean rendererSet = false;
      for (ZoneRenderer currRenderer : zoneRendererList) {
        if (MapTool.getPlayer().isGM() || currRenderer.getZone().isVisible()) {
          setCurrentZoneRenderer(currRenderer);
          rendererSet = true;
          break;
        }
      }
      if (!rendererSet) {
        setCurrentZoneRenderer(null);
      }
    }
    zoneMiniMapPanel.flush();
    zoneMiniMapPanel.repaint();
  }

  public void clearZoneRendererList() {
    zoneRendererList.clear();
    zoneMiniMapPanel.flush();
    zoneMiniMapPanel.repaint();
  }

  /** Stop the drag of the token, if any is being dragged. */
  private void stopTokenDrag() {
    Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
    if (tool instanceof PointerTool) {
      PointerTool pointer = (PointerTool) tool;
      if (pointer.isDraggingToken()) pointer.stopTokenDrag();
    }
  }

  /**
   * Set the current ZoneRenderer
   *
   * @param renderer the ZoneRenderer
   */
  public void setCurrentZoneRenderer(ZoneRenderer renderer) {
    // Flush first so that the new zone renderer can inject the newly needed images
    if (renderer != null) {
      new MapToolEventBus().getMainEventBus().post(new ZoneLoading(renderer.getZone()));

      ImageManager.flush(renderer.getZone().getAllAssetIds());
    } else {
      ImageManager.flush();
      // zoneRendererList.remove(currentRenderer);
    }
    // Handle new renderers
    // TODO: should this be here ?
    if (renderer != null && !zoneRendererList.contains(renderer)) {
      zoneRendererList.add(renderer);
    }
    Zone oldZone = null;
    if (currentRenderer != null) {
      // Check if the zone still exists. Fix #1568
      if (MapTool.getFrame().getZoneRenderers().contains(currentRenderer)) {
        stopTokenDrag(); // if a token is being dragged, stop the drag
      }
      oldZone = currentRenderer.getZone();
      currentRenderer.flush();
      zoneRendererPanel.remove(currentRenderer);
    }
    if (renderer != null) {
      zoneRendererPanel.add(
          renderer, PositionalLayout.Position.CENTER, zoneRendererPanel.getComponentCount() - 1);
      zoneRendererPanel.doLayout();
    }
    currentRenderer = renderer;
    initiativePanel.update();
    toolbox.setTargetRenderer(renderer);

    if (renderer != null) {
      // Previous zone must be passed for the listeners to be properly removed. Fix #1670.

      final var eventBus = new MapToolEventBus().getMainEventBus();
      if (oldZone != null) {
        eventBus.post(new ZoneDeactivated(oldZone));
      }
      eventBus.post(new ZoneActivated(renderer.getZone()));

      renderer.requestFocusInWindow();
      // Updates the VBL/MBL button. Fixes #1642.
      TopologyModeSelectionPanel.getInstance().setMode(renderer.getZone().getTopologyTypes());
    }
    AppActions.updateActions();
    repaint();

    setTitleViaRenderer(renderer);
    getZoomStatusBar().update();
  }

  /**
   * Set the MapTool title bar. The title includes the name of the app, the player name, the
   * campaign name and the name of the specified zone.
   *
   * @param renderer the ZoneRenderer of the zone.
   */
  public void setTitleViaRenderer(ZoneRenderer renderer) {
    String campaignName = " - [" + MapTool.getCampaign().getName() + "]";
    String versionString =
        MapTool.getVersion().equals("unspecified") ? "Development" : "v" + MapTool.getVersion();
    var title =
        AppConstants.APP_NAME + " " + versionString + " - " + MapTool.getPlayer() + campaignName;

    if (renderer != null) {
      title += "-" + renderer.getZone().toString();
    }
    setTitle(title);
  }

  /**
   * Set the MapTool title bar. The title includes the name of the app, the player name, the
   * campaign name and the current zone name.
   */
  public void setTitle() {
    setTitleViaRenderer(MapTool.getFrame().getCurrentZoneRenderer());
  }

  public Toolbox getToolbox() {
    return toolbox;
  }

  public ToolbarPanel getToolbarPanel() {
    return toolbarPanel;
  }

  /**
   * Return the first ZoneRender for which the zone is the same as the passed zone (should be only
   * one).
   *
   * @param zone the zone.
   * @return the ZoneRenderer.
   */
  public ZoneRenderer getZoneRenderer(Zone zone) {
    for (ZoneRenderer renderer : zoneRendererList) {
      if (zone == renderer.getZone()) {
        return renderer;
      }
    }
    return null;
  }

  /**
   * Return the first ZoneRender for which the zone has the zoneGUID (should be only one).
   *
   * @param zoneGUID the zoneGUID of the zone.
   * @return the ZoneRenderer.
   */
  public ZoneRenderer getZoneRenderer(GUID zoneGUID) {
    for (ZoneRenderer renderer : zoneRendererList) {
      if (zoneGUID.equals(renderer.getZone().getId())) {
        return renderer;
      }
    }
    return null;
  }

  /**
   * Return the first ZoneRender for which the zone has the zoneName (could be multiples).
   *
   * @param zoneName the name of the zone.
   * @return the ZoneRenderer.
   */
  public ZoneRenderer getZoneRenderer(final String zoneName) {
    for (ZoneRenderer renderer : zoneRendererList) {
      if (zoneName.equals(renderer.getZone().getName())) {
        return renderer;
      }
    }
    return null;
  }

  /**
   * Get the paintDrawingMeasurements for this MapToolClient.
   *
   * @return Returns the current value of paintDrawingMeasurements.
   */
  public boolean isPaintDrawingMeasurement() {
    return paintDrawingMeasurement;
  }

  /**
   * Set the value of paintDrawingMeasurements for this MapToolClient.
   *
   * @param aPaintDrawingMeasurements The paintDrawingMeasurements to set.
   */
  public void setPaintDrawingMeasurement(boolean aPaintDrawingMeasurements) {
    paintDrawingMeasurement = aPaintDrawingMeasurements;
  }

  public JButton getFullsZoneButton() {
    return fullsZoneButton;
  }

  public void showFullScreen() {
    GraphicsConfiguration graphicsConfig = getGraphicsConfiguration();
    Rectangle bounds = graphicsConfig.getBounds();

    fullScreenFrame = new FullScreenFrame();
    fullScreenFrame.add(zoneRendererPanel);

    // Under mac os x this does not properly hide the menu bar so adjust top and height
    // so menu bar does not overlay screen.
    if (AppUtil.MAC_OS_X) {
      fullScreenFrame.setBounds(bounds.x, bounds.y + 21, bounds.width, bounds.height - 21);
    } else {
      fullScreenFrame.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    fullScreenFrame.setJMenuBar(menuBar);
    // Menu bar is visible anyways on MAC so leave menu items on it
    if (!AppUtil.MAC_OS_X) menuBar.setVisible(false);

    fullScreenFrame.setVisible(true);
    showFullScreenTools();
    this.setVisible(false);
  }

  public void showFullScreenTools() {
    if (!AppState.isFullScreenUIEnabled()) return;

    fullScreenToolPanel = new JPanel();
    fullScreenToolPanel.setLayout(new BoxLayout(fullScreenToolPanel, BoxLayout.LINE_AXIS));
    fullScreenToolPanel.setOpaque(false);
    fullScreenToolPanel.add(toolbarPanel.getPointerGroupButton());
    fullScreenToolPanel.add(toolbarPanel.getDrawButton());
    fullScreenToolPanel.add(toolbarPanel.getTemplateButton());
    fullScreenToolPanel.add(toolbarPanel.getFogButton());
    fullScreenToolPanel.add(toolbarPanel.getTopologyButton());

    var btn = toolbarPanel.getPointerGroupButton();
    fullsZoneButton = toolbarPanel.createZoneSelectionButton();
    fullsZoneButton.setBorder(btn.getBorder());
    fullsZoneButton.setVisible(MapTool.getFrame().getToolbarPanel().getMapselect().isVisible());
    fullScreenToolPanel.add(fullsZoneButton);

    JButton initiativeButton = new JButton(RessourceManager.getBigIcon(Icons.WINDOW_INITIATIVE));

    initiativeButton.addActionListener(
        (e) -> {
          if (initiativePanel.isVisible()) initiativePanel.setVisible(false);
          else initiativePanel.setVisible(true);
        });

    initiativeButton.setBorder(btn.getBorder());
    initiativeButton.setToolTipText(I18N.getText("tools.initiative.tooltip"));
    fullScreenToolPanel.add(initiativeButton);

    // set buttons to uniform size
    boolean first = true;
    Dimension size = null;
    for (var component : fullScreenToolPanel.getComponents()) {
      if (!(component instanceof AbstractButton)) continue;

      var abstractButton = (AbstractButton) component;
      if (first) {
        first = false;
        size = abstractButton.getSize();
      } else abstractButton.setPreferredSize(size);

      abstractButton.setText(null);
    }
    fullScreenToolPanel.setSize(fullScreenToolPanel.getPreferredSize());
    zoneRendererPanel.add(fullScreenToolPanel, PositionalLayout.Position.NW);
    zoneRendererPanel.setComponentZOrder(fullScreenToolPanel, 0);

    var optionPanel = toolbarPanel.getOptionPanel();
    // set size of optionpanel to only necessary size
    for (Component comp : optionPanel.getComponents()) {
      if (comp.isVisible()) {
        optionPanel.setSize(comp.getPreferredSize());
      }
    }

    zoneRendererPanel.add(optionPanel, PositionalLayout.Position.N);
    zoneRendererPanel.setComponentZOrder(optionPanel, 0);

    size = initiativePanel.getPreferredSize();
    size.height = zoneRendererPanel.getHeight() / 2;

    initiativePanel.setSize(size);

    initiativePanel.setVisible(false);

    zoneRendererPanel.add(initiativePanel, PositionalLayout.Position.SE);
    zoneRendererPanel.setComponentZOrder(initiativePanel, 0);

    zoneRendererPanel.revalidate();
    zoneRendererPanel.repaint();

    fullScreenToolsShown = true;
  }

  public void hideFullScreenTools() {
    if (!fullScreenToolsShown) return;

    toolbarPanel.add(toolbarPanel.getOptionPanel(), toolbarPanel.getOptionsPanelIndex());

    JToggleButton buttons[] = {
      toolbarPanel.getTopologyButton(), toolbarPanel.getFogButton(),
      toolbarPanel.getTemplateButton(), toolbarPanel.getDrawButton(),
      toolbarPanel.getPointerGroupButton()
    };

    for (var button : buttons) {
      button.setPreferredSize(null);
      toolbarPanel.add(button, 0);
    }

    zoneRendererPanel.remove(fullScreenToolPanel);
    fullScreenToolPanel = null;
    var initiativeFrame = frameMap.get(MTFrame.INITIATIVE);
    initiativePanel.setVisible(true);
    initiativeFrame.add(initiativePanel);

    fullScreenToolsShown = false;
  }

  public boolean isFullScreen() {
    return fullScreenFrame != null;
  }

  public boolean areFullScreenToolsShown() {
    return fullScreenToolsShown;
  }

  public void showWindowed() {
    if (fullScreenFrame == null) {
      return;
    }
    hideFullScreenTools();

    rendererBorderPanel.add(zoneRendererPanel);
    setJMenuBar(menuBar);
    menuBar.setVisible(true);
    this.setVisible(true);

    fullScreenFrame.dispose();
    fullScreenFrame = null;
  }

  public static class FullScreenFrame extends JFrame {
    public FullScreenFrame() {
      setUndecorated(true);
    }
  }

  @Subscribe
  void onZoneActivated(ZoneActivated event) {
    final var zone = event.zone();

    if (drawPanelTreeModel != null) {
      drawPanelTreeModel.setZone(zone);
    }
    if (tokenPanelTreeModel != null) {
      tokenPanelTreeModel.setZone(zone);
    }
    initiativePanel.setZone(zone);

    // Let's add all the assets, starting with the backgrounds and working up.
    final var layers = Zone.Layer.values();
    ArrayUtils.reverse(layers);
    for (Zone.Layer layer : layers) {
      for (Token token : zone.getTokensOnLayer(layer)) {
        MD5Key key = token.getImageAssetId();
        ImageManager.getImage(key);
      }
    }
  }

  // WINDOW LISTENER
  public void windowOpened(WindowEvent e) {}

  public void windowClosing(WindowEvent e) {
    if (!confirmClose()) {
      return;
    }
    closingMaintenance();
  }

  public boolean confirmClose() {
    return !MapTool.isHostingServer() || MapTool.confirm("msg.confirm.hostingDisconnect");
  }

  public void closingMaintenance() {
    if (AppPreferences.getSaveReminder() && MapTool.isCampaignDirty()) {
      if (MapTool.getPlayer().isGM()) {
        int result =
            MapTool.confirmImpl(
                I18N.getText("msg.title.saveCampaign"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                "msg.confirm.saveCampaign",
                (Object[]) null);
        // int result = JOptionPane.showConfirmDialog(MapTool.getFrame(),
        // I18N.getText("msg.confirm.saveCampaign"), I18N.getText("msg.title.saveCampaign"),
        // JOptionPane.YES_NO_CANCEL_OPTION);

        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
          return;
        }
        if (result == JOptionPane.YES_OPTION) {
          AppActions.doSaveCampaign(() -> MapTool.getFrame().close());
          return;
        }
      } else {
        if (!MapTool.confirm("msg.confirm.disconnecting")) {
          return;
        }
      }
    }
    close();
  }

  public void close() {
    ServerDisconnectHandler.disconnectExpected = true;
    MapTool.disconnect();

    getDockingManager()
        .saveLayoutDataToFile(AppUtil.getAppHome("config").getAbsolutePath() + "/layout.dat");

    /* Issue #2485
     * Write the name of macro created frames to frames.dat so they can be used to create
     * placeholders the next time Maptool is launched
     */
    try {
      List<String> mtFrameNames = Stream.of(MapToolFrame.MTFrame.values()).map(Enum::name).toList();
      List<String> namesToSave =
          getDockingManager().getAllFrames().stream()
              .filter(frame -> !mtFrameNames.contains(frame))
              .toList();

      Path path = Paths.get(AppUtil.getAppHome("config").getAbsolutePath() + "/frames.dat");
      Files.writeString(path, String.join("\0", namesToSave), StandardCharsets.UTF_8);
    } catch (IOException ioe) {
      log.error("Unable to write to frames.dat", ioe);
    }
    /* /Issue #2485 */

    // If closing cleanly, remove the autosave file
    MapTool.getAutoSaveManager().purge();
    setVisible(false);

    EventQueue.invokeLater(this::dispose);
  }

  public void windowClosed(WindowEvent e) {
    System.exit(0);
  }

  public void windowIconified(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowActivated(WindowEvent e) {}

  public void windowDeactivated(WindowEvent e) {}

  // Windows OS defaults F10 to the menu bar, noooooo!! We want for macro buttons.
  // XXX Shouldn't this keystroke be configurable via the properties file anyway?
  // XXX Doesn't work for Mac OSX and isn't called in that case.
  private void removeWindowsF10() {
    InputMap imap = menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    Object action = imap.get(KeyStroke.getKeyStroke("F10"));
    log.info(
        "Removing the F10 key from the menuBar's InputMap; it {} exist",
        (action == null ? "did not" : "did"));
    ActionMap amap = menuBar.getActionMap();
    amap.getParent().remove(action);
  }

  public void updateKeyStrokes() {
    updateKeyStrokes(menuBar);
  }

  public Timer getChatTimer() {
    if (chatTimer == null) {
      chatTimer = newChatTimer();
    }
    return chatTimer;
  }

  public void setChatTimer(Timer timer) {
    chatTimer = timer;
  }

  private Timer newChatTimer() {
    // Set up the Chat timer to listen for changes
    Timer tm =
        new Timer(
            500,
            ae -> {
              long currentTime = System.currentTimeMillis();
              LinkedMap chatTimers = chatTyperTimers.getChatTypers();
              List<String> removeThese = new ArrayList<String>(chatTimers.size());

              @SuppressWarnings("unchecked")
              Set<String> playerTimers = chatTimers.keySet();
              for (String player : playerTimers) {
                long playerTime = (Long) chatTimers.get(player);
                if (currentTime - playerTime >= (chatNotifyDuration * 1000)) {
                  // set up a temp place and remove them after the loop
                  removeThese.add(player);
                }
              }
              for (String remove : removeThese) {
                chatTyperTimers.removeChatTyper(remove);
              }
            });
    tm.start();
    return tm;
  }

  private void updateKeyStrokes(JComponent c) {
    c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
    Map<KeyStroke, MacroButton> keyStrokeMap = MacroButtonHotKeyManager.getKeyStrokeMap();

    if (c.getActionMap().keys() != null) {
      for (Object o : c.getActionMap().keys()) {
        // We're looking for MacroButton here, but we're adding AbstractActions below... Is this
        // right? XXX
        if (o instanceof MacroButton) {
          log.debug("Removing MacroButton {}", ((MacroButton) o).getButtonText());
          c.getActionMap().remove(o);
        }
      }
    }
    for (var entry : keyStrokeMap.entrySet()) {
      final KeyStroke keyStroke = entry.getKey();
      final MacroButton button = entry.getValue();
      if (button != null) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, button);
        c.getActionMap().put(button, new MTButtonHotKeyAction(button));
      } else {
        // This shouldn't be possible...
        log.error("No MacroButton found for keyStroke " + keyStroke.toString());
      }
    }
  }

  public CampaignPanel getCampaignPanel() {
    return campaignPanel;
  }

  public GmPanel getGmPanel() {
    return gmPanel;
  }

  public GlobalPanel getGlobalPanel() {
    return globalPanel;
  }

  public ImpersonatePanel getImpersonatePanel() {
    return impersonatePanel;
  }

  public SelectionPanel getSelectionPanel() {
    return selectionPanel;
  }

  /** Reset the impersonatePanel and the selectionPanel. */
  public void resetTokenPanels() {
    impersonatePanel.reset();
    selectionPanel.reset();
  }

  /** Reset the macro panels. Currently only used after loading a campaign. */
  public void resetPanels() {
    MacroButtonHotKeyManager.clearKeyStrokes();
    campaignPanel.reset();
    gmPanel.reset();
    globalPanel.reset();
    impersonatePanel.reset();
    selectionPanel.reset();
    updateKeyStrokes();
  }

  /**
   * @return Getter for initiativePanel
   */
  public InitiativePanel getInitiativePanel() {
    return initiativePanel;
  }

  private JFileChooser saveMacroFileChooser;
  private JFileChooser saveMacroSetFileChooser;

  public JFileChooser getSaveMacroFileChooser() {
    if (saveMacroFileChooser == null) {
      saveMacroFileChooser = new JFileChooser();
      saveMacroFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
      saveMacroFileChooser.addChoosableFileFilter(macroFilter);
      saveMacroFileChooser.setDialogTitle(I18N.getText("msg.title.exportMacro"));
    }
    saveMacroFileChooser.setAcceptAllFileFilterUsed(true);
    return saveMacroFileChooser;
  }

  public JFileChooser getSaveMacroSetFileChooser() {
    if (saveMacroSetFileChooser == null) {
      saveMacroSetFileChooser = new JFileChooser();
      saveMacroSetFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
      saveMacroSetFileChooser.addChoosableFileFilter(macroSetFilter);
      saveMacroSetFileChooser.setDialogTitle(I18N.getText("msg.title.exportMacroSet"));
    }
    saveMacroSetFileChooser.setAcceptAllFileFilterUsed(true);
    return saveMacroSetFileChooser;
  }

  private JFileChooser loadMacroFileChooser;
  private JFileChooser loadMacroSetFileChooser;

  public JFileChooser getLoadMacroFileChooser() {
    if (loadMacroFileChooser == null) {
      loadMacroFileChooser = new JFileChooser();
      loadMacroFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
      loadMacroFileChooser.addChoosableFileFilter(macroFilter);
      loadMacroFileChooser.setDialogTitle(I18N.getText("msg.title.importMacro"));
    }
    loadMacroFileChooser.setFileFilter(macroFilter);
    return loadMacroFileChooser;
  }

  public JFileChooser getLoadMacroSetFileChooser() {
    if (loadMacroSetFileChooser == null) {
      loadMacroSetFileChooser = new JFileChooser();
      loadMacroSetFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
      loadMacroSetFileChooser.addChoosableFileFilter(macroSetFilter);
      loadMacroSetFileChooser.setDialogTitle(I18N.getText("msg.title.importMacroSet"));
    }
    loadMacroSetFileChooser.setFileFilter(macroSetFilter);
    return loadMacroSetFileChooser;
  }

  // end of Macro import/export support

  private JFileChooser saveTableFileChooser;
  private JFileChooser loadTableFileChooser;

  public JFileChooser getSaveTableFileChooser() {
    if (saveTableFileChooser == null) {
      saveTableFileChooser = new JFileChooser();
      saveTableFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
      saveTableFileChooser.addChoosableFileFilter(tableFilter);
      saveTableFileChooser.setDialogTitle(I18N.getText("Label.table.export"));
    }
    saveTableFileChooser.setAcceptAllFileFilterUsed(true);
    return saveTableFileChooser;
  }

  // Should the load FileChooser really be different from the save? That means recording two
  // separate default directories
  // and when a user loads a file, don't they expect the save dialog to start at the same place??
  public JFileChooser getLoadTableFileChooser() {
    if (loadTableFileChooser == null) {
      loadTableFileChooser = new JFileChooser();
      loadTableFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
      loadTableFileChooser.addChoosableFileFilter(tableFilter);
      loadTableFileChooser.setDialogTitle(I18N.getText("Label.table.import"));
    }
    loadTableFileChooser.setFileFilter(tableFilter);
    return loadTableFileChooser;
  }

  // end of Table import/export support

  @SuppressWarnings("serial")
  private class MTButtonHotKeyAction extends AbstractAction {
    private final MacroButton macroButton;

    public MTButtonHotKeyAction(MacroButton button) {
      macroButton = button;
    }

    public void actionPerformed(ActionEvent e) {
      if (macroButton.getProperties().getApplyToTokens()) {
        if (MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList().size() > 0) {
          macroButton
              .getProperties()
              .executeMacro(MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList());
        }
      } else {
        macroButton.getProperties().executeMacro();
      }
    }
  }
}
