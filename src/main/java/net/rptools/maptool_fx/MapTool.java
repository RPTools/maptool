/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Runtime.Version;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.dockfx.DockPane;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.plaf.basic.ThemePainter;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.rptools.clientserver.hessian.client.ClientConnection;
import net.rptools.lib.BackupManager;
import net.rptools.lib.DebugStream;
import net.rptools.lib.EventDispatcher;
import net.rptools.lib.FileUtil;
import net.rptools.lib.TaskBarFlasher;
import net.rptools.lib.image.ThumbnailManager;
import net.rptools.lib.net.RPTURLStreamHandlerFactory;
import net.rptools.lib.sound.SoundManager;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.box2d.DesktopLauncher;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppSetup;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUpdate;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.AssetTransferHandler;
import net.rptools.maptool.client.AssetURLStreamHandler;
import net.rptools.maptool.client.AutoSaveManager;
import net.rptools.maptool.client.ChatAutoSave;
import net.rptools.maptool.client.ClientMethodHandler;
import net.rptools.maptool.client.MapToolConnection;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.client.ServerCommandClientImpl;
import net.rptools.maptool.client.ServerDisconnectHandler;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.maptool.client.swing.MapToolEventQueue;
import net.rptools.maptool.client.swing.NoteFrame;
import net.rptools.maptool.client.ui.AppMenuBar;
import net.rptools.maptool.client.ui.ConnectionStatusPanel;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.StartServerDialogPreferences;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.ZoneRendererFactory;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignFactory;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.ObservableList;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZoneFactory;
import net.rptools.maptool.protocol.syrinscape.SyrinscapeURLStreamHandler;
import net.rptools.maptool.server.MapToolServer;
import net.rptools.maptool.server.ServerCommand;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.transfer.AssetTransferManager;
import net.rptools.maptool.util.UPnPUtil;
import net.rptools.maptool.util.UserJvmPrefs;
import net.rptools.maptool.webapi.MTWebAppServer;
import net.rptools.maptool_fx.controller.MapTool_Controller;
import net.rptools.maptool_fx.util.CommandLineOptionHelper;
import net.rptools.maptool_fx.util.LogHelper;

import net.tsc.servicediscovery.ServiceAnnouncer;

/**
 */
public class MapTool extends Application {
	private static Logger log;

	private static final String USAGE = "<html><body width=\"400\">You are running MapTool with insufficient memory allocated (%dMB).<br><br>"
			+ "You may experience odd behavior, especially when connecting to or hosting a server.<br><br>  "
			+ "MapTool will launch anyway, but it is recommended that you increase the maximum memory allocated or don't set a limit.</body></html>";

	private final String MAPTOOL_FXML = "/net/rptools/maptool/fx/view/MapTool.fxml";
	private static final String SOUND_PROPERTIES = "net/rptools/maptool/client/sounds.properties";
	public static final String SND_INVALID_OPERATION = "invalidOperation";

	public static enum ZoneEvent {
		Added, Removed, Activated, Deactivated
	}

	public static enum PreferencesEvent {
		Changed
	}

	private static String version = "DEVELOPMENT";;
	private static String vendor = "Nerps!";
	private static String clientId = AppUtil.readClientId();

	private static final Dimension THUMBNAIL_SIZE = new Dimension(500, 500);

	private static ThumbnailManager thumbnailManager;

	private static Campaign campaign;

	private static ObservableList<Player> playerList;
	private static ObservableList<TextMessage> messageList;
	private static Player player;

	private static ClientConnection conn;
	private static ClientMethodHandler handler;
	private static JMenuBar menuBar;
	private static MapToolFrame clientFrame;
	private static NoteFrame profilingNoteFrame;
	private static MapToolServer server;
	private static ServerCommand serverCommand;
	private static ServerPolicy serverPolicy;

	private static BackupManager backupManager;
	private static AssetTransferManager assetTransferManager;
	private static ServiceAnnouncer announcer;
	private static AutoSaveManager autoSaveManager;
	private static SoundManager soundManager;
	private static TaskBarFlasher taskbarFlasher;
	private static EventDispatcher eventDispatcher;
	private static MapToolLineParser parser = new MapToolLineParser();
	private static String lastWhisperer;

	private static final MTWebAppServer webAppServer = new MTWebAppServer();
	private static DesktopLauncher MapToolLwjglApplication;

	// Jamz: To support new command line parameters for multi-monitor support & enhanced PrintStream
	private static boolean debug = false;
	private static int graphicsMonitor = -1;
	private static boolean useFullScreen = false;
	private static int windowWidth = -1;
	private static int windowHeight = -1;
	private static int windowX = -1;
	private static int windowY = -1;
	private static boolean startLibGDX = false;
	public static boolean libgdxLoaded = false;

	private MapTool_Controller mapTool_Controller;

	@Override
	public void init() throws Exception {
		// Since we are using multiple plugins (Twelve Monkeys for PSD and JAI for jpeg2000) in the same uber jar,
		// the META-INF/services/javax.imageio.spi.ImageReaderSpi gets overwritten. So we need to register them manually:
		// https://github.com/jai-imageio/jai-imageio-core/issues/29
		// IIORegistry registry = IIORegistry.getDefaultInstance();
		// registry.registerServiceProvider(new com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi());

		// long mem = Runtime.getRuntime().maxMemory();
		// String msg = new String(String.format(USAGE, mem / (1024 * 1024)));
		//
		// // Asking for 256MB via the -Xmx256M switch doesn't guarantee that the amount maxMemory() reports will be 256MB.
		// // The actual amount seems to vary from PC to PC. 200MB seems to be a safe value for now. <Phergus>
		// // TODO: FXify this
		// if (mem < 200 * 1024 * 1024) {
		// // TODO FX this! -Jamz
		// JOptionPane.showMessageDialog(new JFrame(), msg, "Usage", JOptionPane.INFORMATION_MESSAGE);
		// }

		// This is to initialize the log4j to set the path for logs. Just calling AppUtil sets the System.property
		// Before anything else, create a place to store all the data
		try {
			AppUtil.getAppHome();
		} catch (Throwable t) {
			t.printStackTrace();

			// TODO FX this! -Jamz
			// Create an empty frame so there's something to click on if the dialog goes in the background
			JFrame frame = new JFrame();
			SwingUtil.centerOnScreen(frame);
			frame.setVisible(true);

			String errorCreatingDir = "Error creating data directory";
			log.error(errorCreatingDir, t);
			JOptionPane.showMessageDialog(frame, t.getMessage(), errorCreatingDir, JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		log = LogManager.getLogger(MapTool.class);

		log.info("AppHome System Property: " + System.getProperty("appHome"));
		log.info("Logging to: " + LogHelper.getLoggerFileName(log));
		log.info("3D Hardware Available? " + Platform.isSupported(ConditionalFeature.SCENE3D));

		if (MapTool.class.getPackage().getImplementationVersion() != null) {
			version = MapTool.class.getPackage().getImplementationVersion().trim();
			log.info("setting MapTool version from manifest: " + version);
		}

		if (MapTool.class.getPackage().getImplementationVendor() != null) {
			vendor = MapTool.class.getPackage().getImplementationVendor().trim();
			log.info("setting MapTool vendor from manifest:  " + vendor);
		}

		// TODO: Enable later...
		// Initialize Sentry.io logging
		// Sentry.init();

		// Get command line arguments...
		String[] args = getParameters().getRaw().toArray(new String[0]);

		// Jamz: Overwrite version for testing if passed as command line argument using -v or -version
		Options cmdOptions = new Options();
		cmdOptions.addOption("d", "debug", false, "turn on System.out enhanced debug output");
		cmdOptions.addOption("v", "version", true, "override MapTool version");
		cmdOptions.addOption("m", "monitor", true, "sets which monitor to use");
		cmdOptions.addOption("f", "fullscreen", false, "set to maximize window");
		cmdOptions.addOption("w", "width", true, "override MapTool window width");
		cmdOptions.addOption("h", "height", true, "override MapTool window height");
		cmdOptions.addOption("x", "xpos", true, "override MapTool window starting x coordinate");
		cmdOptions.addOption("y", "ypos", true, "override MapTool window starting y coordinate");
		cmdOptions.addOption("m", "macros", false, "display defined list of macro functions");
		cmdOptions.addOption("r", "reset", false, "reset startup options to defaults");

		// For libGDX testing
		cmdOptions.addOption("l", "libgdx", false, "start & show libGDX application window");
		startLibGDX = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "libgdx", args);

		debug = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "debug", args);
		version = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "version", version, args);
		graphicsMonitor = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "monitor", graphicsMonitor, args);
		useFullScreen = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "fullscreen", args);

		windowWidth = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "width", windowWidth, args);
		windowHeight = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "height", windowHeight, args);
		windowX = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "xpos", windowX, args);
		windowY = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "ypos", windowY, args);

		if (CommandLineOptionHelper.getCommandLineOption(cmdOptions, "reset", args))
			UserJvmPrefs.resetJvmOptions();

		boolean listMacros = CommandLineOptionHelper.getCommandLineOption(cmdOptions, "macros", args);

		// Jamz: Just a little console log formatter for system.out to hyperlink messages to source.
		if (debug)
			DebugStream.activate();
		else
			DebugStream.deactivate();

		// List out passed in arguments
		for (String arg : args) {
			log.info("argument passed via command line: " + arg);
		}

		if (cmdOptions.hasOption("version"))
			log.info("overriding MapTool version from command line to: " + version);
		else
			log.info("MapTool version: " + version);

		log.info("MapTool vendor: " + vendor);

		// TODO: Enable later...
		// Set MapTool version, release, os, & environment in Sentry
		// SentryClient sentryClient = Sentry.getStoredClient();
		// sentryClient.setRelease(AppUpdate.getCommitSHA());
		//
		// if (sentryClient.getRelease() == null) {
		// sentryClient.setEnvironment("Development");
		// }
		//
		// sentryClient.addTag("os", System.getProperty("os.name"));
		// sentryClient.addTag("version", MapTool.getVersion());
		//
		// Sentry.setStoredClient(sentryClient);

		if (listMacros) {
			String logOutput = null;
			List<String> macroList = parser.listAllMacroFunctions();
			Collections.sort(macroList);

			for (String macro : macroList) {
				logOutput += "\n" + macro;
			}

			log.info("Current list of Macro Functions: " + logOutput);
		}

		// System properties
		System.setProperty("swing.aatext", "true");

		// TODO: FX ME! -Jamz
		// final SplashScreen splash = new SplashScreen((isDevelopment()) ? getVersion() : "v" + getVersion());

		// Protocol handlers
		// cp:// is registered by the RPTURLStreamHandlerFactory constructor (why?)
		RPTURLStreamHandlerFactory factory = new RPTURLStreamHandlerFactory();
		factory.registerProtocol("asset", new AssetURLStreamHandler());

		// Syrinscape Protocols
		if (AppPreferences.getSyrinscapeActive()) {
			factory.registerProtocol("syrinscape-fantasy", new SyrinscapeURLStreamHandler());
			factory.registerProtocol("syrinscape-sci-fi", new SyrinscapeURLStreamHandler());
			factory.registerProtocol("syrinscape-boardgame", new SyrinscapeURLStreamHandler());
		}

		URL.setURLStreamHandlerFactory(factory);

		final Toolkit tk = Toolkit.getDefaultToolkit();
		tk.getSystemEventQueue().push(new MapToolEventQueue());

		// splash.hideSplashScreen();

		// All Done!
		// notifyPreloader(new Preloader.ProgressNotification(1.0));

		log.info("MapTool.init() complete.");
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// Do pre initialization needed before FXML loading...
		verifyJavaVersion(); // TODO: Move to preloader later?

		com.jidesoft.utils.Lm.verifyLicense("Trevor Croft", "rptools", "5MfIVe:WXJBDrToeLWPhMv3kI2s3VFo");
		configureJide(); // OK HERE?
		initialize(); // shouldn't be doing UI work here now, set up server and everything else?

		// load the FX UI now...
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAPTOOL_FXML), ResourceBundle.getBundle(AppConstants.MAP_TOOL_BUNDLE));
		VBox root = fxmlLoader.load();
		mapTool_Controller = (MapTool_Controller) fxmlLoader.getController();

		Scene scene = new Scene(root);
		primaryStage.setTitle(AppConstants.APP_NAME + AppConstants.APP_TAG_LINE);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(AppConstants.MAP_TOOL_ICON)));
		primaryStage.setScene(scene);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(javafx.stage.WindowEvent event) {
				log.info("Window closed, exiting MapTool.");
				System.exit(1);
			}
		});

		primaryStage.show();
		setupDockFX();
	    
		Platform.runLater(() -> {
			mapTool_Controller.setDefaultPanes(clientFrame);
			mapTool_Controller.setIntialTitledPane();
			postInitialize();

			// Now that the Application is loaded, check for new release...
			AppUpdate.gitHubReleases();

		});
	}

	private void setupDockFX() {
		var dockPane = new DockPane();
		
		// create a default test node for the center of the dock area
	    var tabs = new TabPane();
	    var htmlEditor = new HTMLEditor();
	    htmlEditor.setHtmlText("Hello MapTool");
	}

	public static String getVersion() {
		if (!version.isEmpty())
			return version;

		version = "DEVELOPMENT";

		if (MapTool.class.getPackage().getImplementationVersion() != null) {
			version = MapTool.class.getPackage().getImplementationVersion().trim();
		}

		return version;
	}

	/**
	 * Check to see if we're running on Java 11+.
	 * <p>
	 * Now that we are packing the JRE with MapTool this is normally not required, however for Development and running the JAR manually, we need the check. MapTool is written for OpenJDK 11 + OpenJFX
	 */
	private static void verifyJavaVersion() {
		Version version;
		boolean keepgoing;
		try {
			version = java.lang.Runtime.version();
			log.info("Java Version = " + version);

			if (version.feature() < AppConstants.REQUIRED_JAVA_VERSION) {
				log.warn("Wrong version of Java detected and unsupported!");
				keepgoing = confirm("msg.error.wrongJavaVersion", version);
			} else {
				return;
			}
		} catch (Exception e) {
			log.error("Error determining Java version.", e);
			keepgoing = confirm("msg.error.unknownJavaVersion");
		}

		if (keepgoing)
			return;
		else
			System.exit(11);
	}

	private static void initialize() {
		// First time
		AppSetup.install();

		// Clean up after ourselves
		try {
			FileUtil.delete(AppUtil.getAppHome("tmp"), 2);
		} catch (IOException ioe) {
			// MapTool.showError("While initializing (cleaning tmpdir)", ioe);
		}
		// We'll manage our own images
		ImageIO.setUseCache(false);

		eventDispatcher = new EventDispatcher();
		registerEvents();

		// TODO FX ME
		soundManager = new SoundManager();
		try {
			soundManager.configure(SOUND_PROPERTIES);
			soundManager.registerSoundEvent(SND_INVALID_OPERATION, soundManager.getRegisteredSound("Dink"));
		} catch (IOException ioe) {
			MapTool.showError("While initializing (configuring sound)", ioe);
		}

		assetTransferManager = new AssetTransferManager();
		assetTransferManager.addConsumerListener(new AssetTransferHandler());

		playerList = new ObservableList<Player>();
		messageList = new ObservableList<TextMessage>(Collections.synchronizedList(new ArrayList<TextMessage>()));

		handler = new ClientMethodHandler();

		setClientFrame(new MapToolFrame(new AppMenuBar()));

		serverCommand = new ServerCommandClientImpl();

		player = new Player("", Player.Role.GM, "");

		try {
			startPersonalServer(CampaignFactory.createBasicCampaign());
		} catch (Exception e) {
			MapTool.showError("While starting personal server", e);
		}
		AppActions.updateActions();

		ToolTipManager.sharedInstance().setInitialDelay(AppPreferences.getToolTipInitialDelay());
		ToolTipManager.sharedInstance().setDismissDelay(AppPreferences.getToolTipDismissDelay());
		ChatAutoSave.changeTimeout(AppPreferences.getChatAutosaveTime());

		// TODO: make this more formal when we switch to mina
		new ServerHeartBeatThread().start();
	}

	private static void setClientFrame(MapToolFrame frame) {
		clientFrame = frame;

		if (graphicsMonitor > -1) {
			moveToMonitor(clientFrame, graphicsMonitor, useFullScreen);
		} else if (useFullScreen) {
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
	}

	/*
	 * original code below...
	 */

	public static Dimension getThumbnailSize() {
		return THUMBNAIL_SIZE;
	}

	/**
	 * This method looks up the message key in the properties file and returns the resultant text with the detail message from the <code>Throwable</code> appended to the end.
	 *
	 * @param msgKey
	 *            the string to use when calling {@link I18N#getText(String)}
	 * @param t
	 *            the exception to be processed
	 * @return the <code>String</code> result
	 */
	public static String generateMessage(String msgKey, Throwable t) {
		String msg;
		if (t == null) {
			msg = I18N.getText(msgKey);
		} else if (msgKey == null) {
			msg = t.toString();
		} else {
			msg = I18N.getText(msgKey) + "<br/>" + t.toString();
		}
		return msg;
	}

	/**
	 * This method is the base method for putting a dialog box up on the screen that might be an error, a warning, or just an information message. Do not use this method if the desired result is a
	 * simple confirmation box (use {@link #confirm(String, Object...)} instead).
	 *
	 * @param message
	 *            the key in the properties file to put in the body of the dialog (formatted using <code>params</code>)
	 * @param titleKey
	 *            the key in the properties file to use when creating the title of the dialog window (formatted using <code>params</code>)
	 * @param messageType
	 *            JOptionPane.{ERROR|WARNING|INFORMATION}_MESSAGE
	 * @param params
	 *            optional parameters to use when formatting the data from the properties file
	 */
	public static void showMessage(String message, String titleKey, int messageType, Object... params) {
		String title = I18N.getText(titleKey, params);
		JOptionPane.showMessageDialog(clientFrame, "<html>" + I18N.getText(message, params), title, messageType);
	}

	/**
	 * Same as {@link #showMessage(String, String, int, Object...)} except that <code>messages</code> is stored into a JList and that component is then used as the content of the dialog box. This
	 * allows multiple strings to be displayed in a manner consistent with other message dialogs.
	 *
	 * @param messages
	 *            the Objects (normally strings) to put in the body of the dialog; no properties file lookup is performed!
	 * @param titleKey
	 *            the key in the properties file to use when creating the title of the dialog window (formatted using <code>params</code>)
	 * @param messageType
	 *            one of <code>JOptionPane.ERROR_MESSAGE</code>, <code>JOptionPane.WARNING_MESSAGE</code>, <code>JOptionPane.INFORMATION_MESSAGE</code>
	 * @param params
	 *            optional parameters to use when formatting the title text from the properties file
	 */
	public static void showMessage(Object[] messages, String titleKey, int messageType, Object... params) {
		String title = I18N.getText(titleKey, params);
		JList list = new JList(messages);
		JOptionPane.showMessageDialog(clientFrame, list, title, messageType);
	}

	/**
	 * Displays the messages provided as <code>messages</code> by calling {@link #showMessage(Object[], String, int, Object...)} and passing <code>"msg.title.messageDialogFeedback"</code> and
	 * <code>JOptionPane.ERROR_MESSAGE</code> as parameters.
	 *
	 * @param messages
	 *            the Objects (normally strings) to put in the body of the dialog; no properties file lookup is performed!
	 */
	public static void showFeedback(Object[] messages) {
		showMessage(messages, "msg.title.messageDialogFeedback", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a dialog box by calling {@link #showError(String, Throwable)} and passing <code>null</code> for the second parameter.
	 *
	 * @param msgKey
	 *            the key to use when calling {@link I18N#getText(String)}
	 */
	public static void showError(String msgKey) {
		showError(msgKey, null);
	}

	/**
	 * Displays a dialog box with a predefined title and type, and a message crafted by calling {@link #generateMessage(String, Throwable)} and passing it the two parameters. Also logs an entry using
	 * the {@link Logger#error(Object, Throwable)} method.
	 * <p>
	 * The title is the property key <code>"msg.title.messageDialogError"</code> , and the dialog type is <code>JOptionPane.ERROR_MESSAGE</code>.
	 *
	 * @param msgKey
	 *            the key to use when calling {@link I18N#getText(String)}
	 * @param t
	 *            the exception to be processed
	 */
	public static void showError(String msgKey, Throwable t) {
		String msg = generateMessage(msgKey, t);
		log.error(msgKey, t);
		showMessage(msg, "msg.title.messageDialogError", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a dialog box by calling {@link #showWarning(String, Throwable)} and passing <code>null</code> for the second parameter.
	 *
	 * @param msgKey
	 *            the key to use when calling {@link I18N#getText(String)}
	 */
	public static void showWarning(String msgKey) {
		showWarning(msgKey, null);
	}

	/**
	 * Displays a dialog box with a predefined title and type, and a message crafted by calling {@link #generateMessage(String, Throwable)} and passing it the two parameters. Also logs an entry using
	 * the {@link Logger#warn(Object, Throwable)} method.
	 * <p>
	 * The title is the property key <code>"msg.title.messageDialogWarning"</code>, and the dialog type is <code>JOptionPane.WARNING_MESSAGE</code>.
	 *
	 * @param msgKey
	 *            the key to use when calling {@link I18N#getText(String)}
	 * @param t
	 *            the exception to be processed
	 */
	public static void showWarning(String msgKey, Throwable t) {
		String msg = generateMessage(msgKey, t);
		log.warn(msgKey, t);
		showMessage(msg, "msg.title.messageDialogWarning", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Displays a dialog box by calling {@link #showInformation(String, Throwable)} and passing <code>null</code> for the second parameter.
	 *
	 * @param msgKey
	 *            the key to use when calling {@link I18N#getText(String)}
	 */
	public static void showInformation(String msgKey) {
		showInformation(msgKey, null);
	}

	/**
	 * Displays a dialog box with a predefined title and type, and a message crafted by calling {@link #generateMessage(String, Throwable)} and passing it the two parameters. Also logs an entry using
	 * the {@link Logger#info(Object, Throwable)} method.
	 * <p>
	 * The title is the property key <code>"msg.title.messageDialogInfo"</code>, and the dialog type is <code>JOptionPane.INFORMATION_MESSAGE</code>.
	 *
	 * @param msgKey
	 *            the key to use when calling {@link I18N#getText(String)}
	 * @param t
	 *            the exception to be processed
	 */
	public static void showInformation(String msgKey, Throwable t) {
		String msg = generateMessage(msgKey, t);
		log.info(msgKey, t);
		showMessage(msg, "msg.title.messageDialogInfo", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Displays a confirmation dialog that uses the message as a key to the properties file, and the additional values as parameters to the formatting of the key lookup.
	 *
	 * @param message
	 *            key from the properties file (preferred) or hard-coded string to display
	 * @param params
	 *            optional arguments for the formatting of the property value
	 * @return <code>true</code> if the user clicks the OK button, <code>false</code> otherwise
	 */
	public static boolean confirm(String message, Object... params) {
		// String msg = I18N.getText(message, params);
		// log.debug(message);
		String title = I18N.getText("msg.title.messageDialogConfirm");
		// return JOptionPane.showConfirmDialog(clientFrame, msg, title, JOptionPane.OK_OPTION) ==
		// JOptionPane.OK_OPTION;
		// return confirmImpl(title, JOptionPane.OK_OPTION, message, params) == JOptionPane.OK_OPTION;
		return confirmImpl(title, JOptionPane.OK_OPTION, message, params) == ButtonType.OK;
	}

	/**
	 * Displays a confirmation dialog that uses the message as a key to the properties file, and the additional values as parameters to the formatting of the key lookup.
	 *
	 * @param title
	 * @param buttons
	 * @param message
	 *            key from the properties file (preferred) or hard-coded string to display
	 * @param params
	 *            optional arguments for the formatting of the property value
	 * @return <code>true</code> if the user clicks the OK button, <code>false</code> otherwise
	 */
	public static ButtonType confirmImpl(String title, int buttons, String message, Object... params) {
		String msg = I18N.getText(message, params);
		log.debug(message);
		// return JOptionPane.showConfirmDialog(clientFrame, msg, title, buttons);
		// TODO: Implement buttons later

		Alert alert = new Alert(AlertType.CONFIRMATION);
		// alert.setTitle(title);
		alert.setHeaderText(title);
		alert.setContentText(msg);

		return alert.showAndWait().get();
	}

	/**
	 * This method is specific to deleting a token, but it can be used as a basis for any other method which wants to be turned off via a property.
	 *
	 * @return true if the token should be deleted.
	 */
	public static boolean confirmTokenDelete() {
		if (!AppPreferences.getTokensWarnWhenDeleted()) {
			return true;
		}

		String msg = I18N.getText("msg.confirm.deleteToken");
		log.debug(msg);
		Object[] options = { I18N.getText("msg.title.messageDialog.yes"), I18N.getText("msg.title.messageDialog.no"),
				I18N.getText("msg.title.messageDialog.dontAskAgain") };
		String title = I18N.getText("msg.title.messageDialogConfirm");
		int val = JOptionPane.showOptionDialog(clientFrame, msg, title, JOptionPane.NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);

		// "Yes, don't show again" Button
		if (val == 2) {
			showInformation("msg.confirm.deleteToken.removed");
			AppPreferences.setTokensWarnWhenDeleted(false);
		}
		// Any version of 'Yes'...
		if (val == JOptionPane.YES_OPTION || val == 2) {
			return true;
		}
		// Assume 'No' response
		return false;
	}

	public static boolean confirmDrawDelete() {
		if (!AppPreferences.getDrawWarnWhenDeleted()) {
			return true;
		}

		String msg = I18N.getText("msg.confirm.deleteDraw");
		log.debug(msg);
		Object[] options = { I18N.getText("msg.title.messageDialog.yes"), I18N.getText("msg.title.messageDialog.no"),
				I18N.getText("msg.title.messageDialog.dontAskAgain") };
		String title = I18N.getText("msg.title.messageDialogConfirm");
		int val = JOptionPane.showOptionDialog(clientFrame, msg, title, JOptionPane.NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);

		// "Yes, don't show again" Button
		if (val == 2) {
			showInformation("msg.confirm.deleteDraw.removed");
			AppPreferences.setDrawWarnWhenDeleted(false);
		}
		// Any version of 'Yes'...
		if (val == JOptionPane.YES_OPTION || val == 2) {
			return true;
		}
		// Assume 'No' response
		return false;
	}

	// private MapTool() {
	// // Not to be instantiated
	// throw new Error("cannot construct MapTool object!");
	// }

	public static BackupManager getBackupManager() {
		if (backupManager == null) {
			try {
				backupManager = new BackupManager(AppUtil.getAppHome("backup"));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return backupManager;
	}

	/**
	 * Launch the platform's web browser and ask it to open the given URL. Note that this should not be called from any uncontrolled macros as there are both security and denial-of-service attacks
	 * possible.
	 *
	 * @param url
	 */
	public static void showDocument(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			URI uri = null;
			try {
				uri = new URI(url);
				desktop.browse(uri);
			} catch (Exception e) {
				MapTool.showError(I18N.getText("msg.error.browser.cannotStart", uri), e);
			}
		} else {
			String errorMessage = "msg.error.browser.notFound";
			Exception exception = null;
			String[] envvars = { "MAPTOOL_BROWSER", "BROWSER" };
			String param = envvars[0];
			boolean apparentlyItWorked = false;
			for (String var : envvars) {
				String browser = System.getenv(var);
				if (browser != null) {
					try {
						param = var + "=\"" + browser + "\"";
						Runtime.getRuntime().exec(new String[] { browser, url });
						apparentlyItWorked = true;
					} catch (Exception e) {
						errorMessage = "msg.error.browser.cannotStart";
						exception = e;
					}
				}
			}
			if (apparentlyItWorked == false) {
				MapTool.showError(I18N.getText(errorMessage, param), exception);
			}
		}
	}

	public static SoundManager getSoundManager() {
		return soundManager;
	}

	public static void playSound(String eventId) {
		if (AppPreferences.getPlaySystemSounds()) {
			if (AppPreferences.getPlaySystemSoundsOnlyWhenNotFocused() && isInFocus()) {
				return;
			}
			soundManager.playSoundEvent(eventId);
		}
	}

	public static void updateServerPolicy(ServerPolicy policy) {
		setServerPolicy(policy);

		// Give everyone the new policy
		if (serverCommand != null) {
			serverCommand.setServerPolicy(policy);
		}
	}

	public static boolean isInFocus() {
		// TODO: This should probably also check owned windows
		return getFrame().isFocused();
	}

	// TODO: This method is redundant now. It should be rolled into the
	// TODO: ExportDialog screenshot method. But until that has proven stable
	// TODO: for a while, I don't want to mess with this. (version 1.3b70 is most recent)
	public static BufferedImage takeMapScreenShot(final PlayerView view) {
		final ZoneRenderer renderer = clientFrame.getCurrentZoneRenderer();
		if (renderer == null) {
			return null;
		}

		Dimension size = renderer.getSize();
		if (size.width == 0 || size.height == 0) {
			return null;
		}

		BufferedImage image = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
		final Graphics2D g = image.createGraphics();
		g.setClip(0, 0, size.width, size.height);

		// Have to do this on the EDT so that there aren't any odd side effects
		// of rendering
		// using a renderer that's on screen
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						renderer.renderZone(g, view);
					}
				});
			} catch (InterruptedException ie) {
				MapTool.showError("While creating snapshot", ie);
			} catch (InvocationTargetException ite) {
				MapTool.showError("While creating snapshot", ite);
			}
		} else {
			renderer.renderZone(g, view);
		}

		g.dispose();

		return image;
	}

	public static AutoSaveManager getAutoSaveManager() {
		if (autoSaveManager == null) {
			autoSaveManager = new AutoSaveManager();
		}
		return autoSaveManager;
	}

	public static EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	private static void registerEvents() {
		getEventDispatcher().registerEvents(ZoneEvent.values());
		getEventDispatcher().registerEvents(PreferencesEvent.values());
	}

	/**
	 * For Multi-monitor support, allows you to move the frame to a specific monitor. It will also set the height, width and x, y position of the frame.
	 *
	 * @author Jamz
	 * @since 1.4.1.0
	 *
	 * @param frame
	 *            The JFrame to move
	 * @param monitor
	 *            The monitor number as an int. Note the first monitor start at 0, not 1.
	 * @param maximize
	 *            set to true if you want to maximize the frame to that monitor.
	 */
	private static void moveToMonitor(JFrame frame, int monitor, boolean maximize) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();

		if (monitor > -1 && monitor < gd.length) {
			if (windowWidth > -1 && windowHeight > -1) {
				frame.setSize(windowWidth, windowHeight);
			}

			if (windowX > -1 && windowY > -1) {
				frame.setLocation(windowX + gd[monitor].getDefaultConfiguration().getBounds().x,
						windowY + gd[monitor].getDefaultConfiguration().getBounds().y);

			} else {
				frame.setLocation(gd[monitor].getDefaultConfiguration().getBounds().x, frame.getY());
			}
		} else if (gd.length > 0) {
			frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
		} else {
			throw new RuntimeException("No Screens Found");
		}

		if (maximize)
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	// private static void initialize() {
	// // First time
	// AppSetup.install();
	//
	// // Clean up after ourselves
	// try {
	// FileUtil.delete(AppUtil.getAppHome("tmp"), 2);
	// } catch (IOException ioe) {
	// MapTool.showError("While initializing (cleaning tmpdir)", ioe);
	// }
	// // We'll manage our own images
	// ImageIO.setUseCache(false);
	//
	// eventDispatcher = new EventDispatcher();
	// registerEvents();
	//
	// soundManager = new SoundManager();
	// try {
	// soundManager.configure(SOUND_PROPERTIES);
	// soundManager.registerSoundEvent(SND_INVALID_OPERATION, soundManager.getRegisteredSound("Dink"));
	// } catch (IOException ioe) {
	// MapTool.showError("While initializing (configuring sound)", ioe);
	// }
	//
	// assetTransferManager = new AssetTransferManager();
	// assetTransferManager.addConsumerListener(new AssetTransferHandler());
	//
	// playerList = new ObservableList<Player>();
	// messageList = new ObservableList<TextMessage>(Collections.synchronizedList(new ArrayList<TextMessage>()));
	//
	// handler = new ClientMethodHandler();
	//
	// setClientFrame(new MapToolFrame(menuBar));
	//
	// serverCommand = new ServerCommandClientImpl();
	//
	// player = new Player("", Player.Role.GM, "");
	//
	// try {
	// startPersonalServer(CampaignFactory.createBasicCampaign());
	// } catch (Exception e) {
	// MapTool.showError("While starting personal server", e);
	// }
	// AppActions.updateActions();
	//
	// ToolTipManager.sharedInstance().setInitialDelay(AppPreferences.getToolTipInitialDelay());
	// ToolTipManager.sharedInstance().setDismissDelay(AppPreferences.getToolTipDismissDelay());
	// ChatAutoSave.changeTimeout(AppPreferences.getChatAutosaveTime());
	//
	// // TODO: make this more formal when we switch to mina
	// new ServerHeartBeatThread().start();
	// }

	public static NoteFrame getProfilingNoteFrame() {
		if (profilingNoteFrame == null) {
			profilingNoteFrame = new NoteFrame();
			profilingNoteFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			profilingNoteFrame.addWindowListener(new WindowAdapter() {
				// @Override
				public void windowClosing(WindowEvent e) {
					AppState.setCollectProfilingData(false);
					profilingNoteFrame.setVisible(false);
				}
			});
			profilingNoteFrame.setSize(profilingNoteFrame.getPreferredSize());
			// It's possible that the SelectionPanel may cause text to be added to the NoteFrame, so it
			// can happen before MapTool.initialize() has had a chance to init the clientFrame.
			if (clientFrame != null)
				SwingUtil.centerOver(profilingNoteFrame, clientFrame);
		}
		return profilingNoteFrame;
	}

	// public static String getVersion() {
	// return version;
	// }

	public static boolean isDevelopment() {
		return "DEVELOPMENT".equals(version) || "@buildNumber@".equals(version);
	}

	public static ServerPolicy getServerPolicy() {
		return serverPolicy;
	}

	public static ServerCommand serverCommand() {
		return serverCommand;
	}

	public static MapToolServer getServer() {
		return server;
	}

	public static void addPlayer(Player player) {
		if (!playerList.contains(player)) {
			playerList.add(player);

			// LATER: Make this non-anonymous
			playerList.sort(new Comparator<Player>() {
				public int compare(Player arg0, Player arg1) {
					return arg0.getName().compareToIgnoreCase(arg1.getName());
				}
			});

			if (!player.equals(MapTool.getPlayer())) {
				String msg = MessageFormat.format(I18N.getText("msg.info.playerConnected"), player.getName());
				addLocalMessage("<span style='color:#0000ff'><i>" + msg + "</i></span>");
			}
		}
	}

	public Player getPlayer(String name) {
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).getName().equals(name)) {
				return playerList.get(i);
			}
		}
		return null;
	}

	public static void removePlayer(Player player) {
		if (player == null) {
			return;
		}
		playerList.remove(player);

		if (MapTool.getPlayer() != null && !player.equals(MapTool.getPlayer())) {
			String msg = MessageFormat.format(I18N.getText("msg.info.playerDisconnected"), player.getName());
			addLocalMessage("<span style='color:#0000ff'><i>" + msg + "</i></span>");
		}
	}

	public static ObservableList<TextMessage> getMessageList() {
		return messageList;
	}

	/**
	 * These are the messages that originate from the server
	 */
	public static void addServerMessage(TextMessage message) {
		// Filter
		if (message.isGM() && !getPlayer().isGM()) {
			return;
		}
		if (message.isWhisper() && !getPlayer().getName().equalsIgnoreCase(message.getTarget())) {
			return;
		}
		if (!getFrame().isCommandPanelVisible()) {
			getFrame().getChatActionLabel().setVisible(true);
		}
		// Flashing
		if (!isInFocus()) {
			taskbarFlasher.flash();
		}
		if (message.isWhisper()) {
			setLastWhisperer(message.getSource());
		}
		messageList.add(message);
	}

	/**
	 * These are the messages that are generated locally
	 */
	public static void addMessage(TextMessage message) {
		// Filter stuff
		addServerMessage(message);

		if (!message.isMe()) {
			serverCommand().message(message);
		}
	}

	/**
	 * Add a message only this client can see. This is a shortcut for addMessage(ME, ...)
	 *
	 * @param message
	 */
	public static void addLocalMessage(String message) {
		addMessage(TextMessage.me(null, message));
	}

	/**
	 * Add a message all clients can see. This is a shortcut for addMessage(SAY, ...)
	 *
	 * @param message
	 */
	public static void addGlobalMessage(String message) {
		addMessage(TextMessage.say(null, message));
	}

	/**
	 * Add a message all specified clients will see. This is a shortcut for addMessage(WHISPER, ...) and addMessage(GM, ...). The <code>targets</code> is expected do be in a string list built with
	 * <code>separator</code>.
	 *
	 * @param message
	 *            message to be sent
	 * @param targets
	 *            string specifying clients to send the message to (spaces are trimmed)
	 * @param separator
	 *            the separator between entries in <code>targets</code>
	 */
	public static void addGlobalMessage(String message, String targets, String separator) {
		List<String> list = new LinkedList<String>();
		for (String target : targets.split(separator))
			list.add(target.trim());
		addGlobalMessage(message, list);
	}

	/**
	 * Add a message all specified clients will see. This is a shortcut for addMessage(WHISPER, ...) and addMessage(GM, ...).
	 *
	 * @param message
	 *            message to be sent
	 * @param targets
	 *            list of <code>String</code>s specifying clients to send the message to
	 */
	public static void addGlobalMessage(String message, List<String> targets) {
		for (String target : targets) {
			if ("gm".equalsIgnoreCase(target)) {
				addMessage(TextMessage.gm(null, message));
			} else {
				addMessage(TextMessage.whisper(null, target, message));
			}
		}
	}

	public static Campaign getCampaign() {
		if (campaign == null) {
			campaign = new Campaign();
		}
		return campaign;
	}

	public static MapToolLineParser getParser() {
		return parser;
	}

	public static void setCampaign(Campaign campaign) {
		setCampaign(campaign, null);
	}

	public static void setCampaign(Campaign campaign, GUID defaultRendererId) {
		// Load up the new
		MapTool.campaign = campaign;
		ZoneRenderer currRenderer = null;

		// Clean up
		clientFrame.setCurrentZoneRenderer(null);
		clientFrame.clearZoneRendererList();
		clientFrame.getInitiativePanel().setZone(null);
		clientFrame.clearTokenTree();
		if (campaign == null) {
			return;
		}
		// Install new campaign
		for (Zone zone : campaign.getZones()) {
			ZoneRenderer renderer = ZoneRendererFactory.newRenderer(zone);
			clientFrame.addZoneRenderer(renderer);
			if ((currRenderer == null || zone.getId().equals(defaultRendererId)) && (getPlayer().isGM() || zone.isVisible())) {
				currRenderer = renderer;
			}
			eventDispatcher.fireEvent(ZoneEvent.Added, campaign, null, zone);
		}
		clientFrame.setCurrentZoneRenderer(currRenderer);
		clientFrame.getInitiativePanel().setOwnerPermissions(campaign.isInitiativeOwnerPermissions());
		clientFrame.getInitiativePanel().setMovementLock(campaign.isInitiativeMovementLock());

		AssetManager.updateRepositoryList();
		MapTool.getFrame().getCampaignPanel().reset();
		UserDefinedMacroFunctions.getInstance().loadCampaignLibFunctions();
	}

	public static void setServerPolicy(ServerPolicy policy) {
		serverPolicy = policy;
	}

	public static AssetTransferManager getAssetTransferManager() {
		return assetTransferManager;
	}

	public static void startServer(String id, ServerConfig config, ServerPolicy policy, Campaign campaign)
			throws IOException {
		if (server != null) {
			Thread.dumpStack();
			showError("msg.error.alreadyRunningServer");
			return;
		}

		assetTransferManager.flush();

		// TODO: the client and server campaign MUST be different objects.
		// Figure out a better init method
		server = new MapToolServer(config, policy);
		server.setCampaign(campaign);

		serverPolicy = server.getPolicy();

		if (announcer != null) {
			announcer.stop();
		}
		// Don't announce personal servers
		if (!config.isPersonalServer().get()) {
			announcer = new ServiceAnnouncer(id, server.getConfig().getPort(), AppConstants.SERVICE_GROUP);
			announcer.start();
		}

		// Registered ?
		if (config.isServerRegistered() && !config.isPersonalServer().get()) {
			try {
				int result = MapToolRegistry.registerInstance(config.getServerName(), config.getPort());
				if (result == 3) {
					MapTool.showError("msg.error.alreadyRegistered");
				}
				// TODO: I don't like this
			} catch (Exception e) {
				MapTool.showError("msg.error.failedCannotRegisterServer", e);
			}
		}
	}

	public static ThumbnailManager getThumbnailManager() {
		if (thumbnailManager == null) {
			thumbnailManager = new ThumbnailManager(AppUtil.getAppHome("imageThumbs"), THUMBNAIL_SIZE);
		}

		return thumbnailManager;
	}

	public static void stopServer() {
		if (server == null) {
			return;
		}

		disconnect();
		server.stop();
		server = null;
	}

	public static ObservableList<Player> getPlayerList() {
		return playerList;
	}

	public static List<String> getGMs() {
		Iterator<Player> pliter = playerList.iterator();
		List<String> gms = new ArrayList<String>(playerList.size());
		while (pliter.hasNext()) {
			Player plr = pliter.next();
			if (plr.isGM()) {
				gms.add(plr.getName());
			}
		}
		return gms;
	}

	/**
	 * Whether a specific player is connected to the game
	 */
	public static boolean isPlayerConnected(String player) {
		for (int i = 0; i < playerList.size(); i++) {
			Player p = playerList.get(i);
			if (p.getName().equalsIgnoreCase(player)) {
				return true;
			}
		}
		return false;
	}

	public static void removeZone(Zone zone) {
		MapTool.serverCommand().removeZone(zone.getId());
		MapTool.getFrame().removeZoneRenderer(MapTool.getFrame().getZoneRenderer(zone.getId()));
		MapTool.getCampaign().removeZone(zone.getId());
	}

	public static void addZone(Zone zone) {
		addZone(zone, true);
	}

	public static void addZone(Zone zone, boolean changeZone) {
		if (getCampaign().getZones().size() == 1) {
			// Remove the default map
			Zone singleZone = getCampaign().getZones().get(0);
			if (ZoneFactory.DEFAULT_MAP_NAME.equals(singleZone.getName()) && singleZone.isEmpty()) {
				removeZone(singleZone);
			}
		}
		getCampaign().putZone(zone);
		serverCommand().putZone(zone);
		eventDispatcher.fireEvent(ZoneEvent.Added, getCampaign(), null, zone);

		// Show the new zone
		if (changeZone)
			clientFrame.setCurrentZoneRenderer(ZoneRendererFactory.newRenderer(zone));
		else {
			getFrame().getZoneRenderers().add(ZoneRendererFactory.newRenderer(zone));
		}
	}

	public static Player getPlayer() {
		return player;
	}

	public static void startPersonalServer(Campaign campaign) throws IOException {
		ServerConfig config = ServerConfig.createPersonalServerConfig();
		MapTool.startServer(null, config, new ServerPolicy(), campaign);

		String username = System.getProperty("user.name", "Player");

		// Connect to server
		MapTool.createConnection("localhost", config.getPort(), new Player(username, Player.Role.GM, null));

		// connecting
		MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.server);
	}

	public static void createConnection(String host, int port, Player player) throws UnknownHostException, IOException {
		MapTool.player = player;
		MapTool.getFrame().getCommandPanel().setIdentityName(null);

		ClientConnection clientConn = new MapToolConnection(host, port, player);

		clientConn.addMessageHandler(handler);
		clientConn.addActivityListener(clientFrame.getActivityMonitor());
		clientConn.addDisconnectHandler(new ServerDisconnectHandler());

		clientConn.start();

		// LATER: I really, really, really don't like this startup pattern
		if (clientConn.isAlive()) {
			conn = clientConn;
		}
		clientFrame.getLookupTablePanel().updateView();
		clientFrame.getInitiativePanel().updateView();
	}

	public static void closeConnection() throws IOException {
		if (conn != null) {
			conn.close();
		}
	}

	public static ClientConnection getConnection() {
		return conn;
	}

	public static boolean isPersonalServer() {
		return server != null && server.getConfig().isPersonalServer().get();
	}

	public static boolean isHostingServer() {
		return server != null && !server.getConfig().isPersonalServer().get();
	}

	public static void disconnect() {
		// Close UPnP port mapping if used
		StartServerDialogPreferences serverProps = new StartServerDialogPreferences();
		if (serverProps.getUseUPnP()) {
			int port = serverProps.getPort();
			UPnPUtil.closePort(port);
		}
		boolean isPersonalServer = isPersonalServer();

		if (announcer != null) {
			announcer.stop();
			announcer = null;
		}
		if (conn == null || !conn.isAlive()) {
			return;
		}
		// Unregister ourselves
		if (server != null && server.getConfig().isServerRegistered() && !isPersonalServer) {
			try {
				MapToolRegistry.unregisterInstance(server.getConfig().getPort());
			} catch (Throwable t) {
				MapTool.showError("While unregistering server instance", t);
			}
		}

		try {
			conn.close();
			conn = null;
			playerList.clear();
		} catch (IOException ioe) {
			// This isn't critical, we're closing it anyway
			log.debug("While closing connection", ioe);
		}
		MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.disconnected);

		if (!isPersonalServer) {
			addLocalMessage("<span style='color:blue'><i>" + I18N.getText("msg.info.disconnected") + "</i></span>");
		}
	}

	public static MapToolFrame getFrame() {
		return clientFrame;
	}

	public static DesktopLauncher getApp() {
		return MapToolLwjglApplication;
	}

	public static void loadBox2dTest() {
		if (MapToolLwjglApplication == null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MapToolLwjglApplication = new DesktopLauncher(clientFrame);
					libgdxLoaded = true;
				}
			});
		} else if (!MapToolLwjglApplication.isVisible()) {
			MapToolLwjglApplication.setVisible(true);
		}
	}

	private static final void configureJide() {
		LookAndFeelFactory.UIDefaultsCustomizer uiDefaultsCustomizer = new LookAndFeelFactory.UIDefaultsCustomizer() {
			public void customize(UIDefaults defaults) {
				ThemePainter painter = (ThemePainter) UIDefaultsLookup.get("Theme.painter");
				defaults.put("OptionPaneUI", "com.jidesoft.plaf.basic.BasicJideOptionPaneUI");

				defaults.put("OptionPane.showBanner", Boolean.TRUE); // show banner or not. default is true
				defaults.put("OptionPane.bannerIcon", new ImageIcon(MapTool.class.getClassLoader().getResource("net/rptools/maptool/client/image/maptool_icon.png")));
				defaults.put("OptionPane.bannerFontSize", 13);
				defaults.put("OptionPane.bannerFontStyle", Font.BOLD);
				defaults.put("OptionPane.bannerMaxCharsPerLine", 60);
				defaults.put("OptionPane.bannerForeground", painter != null ? painter.getOptionPaneBannerForeground() : null); // you should adjust this if banner background is not the default
																																// gradient paint
				defaults.put("OptionPane.bannerBorder", null); // use default border

				// set both bannerBackgroundDk and bannerBackgroundLt to null if you don't want gradient
				defaults.put("OptionPane.bannerBackgroundDk", painter != null ? painter.getOptionPaneBannerDk() : null);
				defaults.put("OptionPane.bannerBackgroundLt", painter != null ? painter.getOptionPaneBannerLt() : null);
				defaults.put("OptionPane.bannerBackgroundDirection", Boolean.TRUE); // default is true

				// optionally, you can set a Paint object for BannerPanel. If so, the three UIDefaults related to banner background above will be ignored.
				defaults.put("OptionPane.bannerBackgroundPaint", null);

				defaults.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(6, 6, 6, 6));
				defaults.put("OptionPane.buttonOrientation", SwingConstants.RIGHT);
			}
		};
		uiDefaultsCustomizer.customize(UIManager.getDefaults());
	}

	private static void postInitialize() {
		// Check to see if there is an autosave file from MT crashing
		getAutoSaveManager().check();
		getAutoSaveManager().restart();

		// taskbarFlasher = new TaskBarFlasher(clientFrame);

		// Jamz: After preferences are loaded, Asset Tree and ImagePanel are out of sync,
		// so after frame is all done loading we sync them back up.
		// MapTool.getFrame().getAssetPanel().getAssetTree().initialize();
	}

	/**
	 * Return whether the campaign file has changed. Only checks to see if there is a single empty map with the default name (ZoneFactory.DEFAULT_MAP_NAME). If so, the campaign is "empty". We really
	 * should check against things like campaign property changes as well, including campaign macros...
	 */
	public static boolean isCampaignDirty() {
		// TODO: This is a very naive check, but it's better than nothing
		if (getCampaign().getZones().size() == 1) {
			Zone singleZone = MapTool.getCampaign().getZones().get(0);
			if (ZoneFactory.DEFAULT_MAP_NAME.equals(singleZone.getName()) && singleZone.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static void setLastWhisperer(String lastWhisperer) {
		if (lastWhisperer != null) {
			MapTool.lastWhisperer = lastWhisperer;
		}
	}

	public static String getLastWhisperer() {
		return lastWhisperer;
	}

	public static boolean useToolTipsForUnformatedRolls() {
		if (isPersonalServer()) {
			return AppPreferences.getUseToolTipForInlineRoll();
		} else {
			return getServerPolicy().getUseToolTipsForDefaultRollFormat();
		}
	}

	public static MTWebAppServer getWebAppServer() {
		return webAppServer;
	}

	public static void startWebAppServer(final int port) {
		try {
			Thread webAppThread = new Thread() {
				@Override
				public void run() {
					webAppServer.setPort(port);
					webAppServer.startServer();
				}
			};

			webAppThread.run();
		} catch (Exception e) { // TODO: This needs to be logged
			System.out.println("Unable to start web server");
			e.printStackTrace();
		}
	}

	public static String getClientId() {
		return clientId;
	}

	private static class ServerHeartBeatThread extends Thread {
		@Override
		public void run() {

			// This should always run, so we should be able to safely
			// loop forever
			while (true) {
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				ServerCommand command = serverCommand;
				if (command != null) {
					command.heartbeat(getPlayer().getName());
				}

			}
		}
	}

	/**
	 * An example method that throws an exception.
	 */
	static void unsafeMethod() {
		throw new UnsupportedOperationException("You shouldn't call this either!");
	}

	/**
	 * Examples using the (recommended) static API.
	 */
	static void testSentryAPI() {
		// Note that all fields set on the context are optional. Context data is copied onto
		// all future events in the current context (until the context is cleared).

		// Record a breadcrumb in the current context. By default the last 100 breadcrumbs are kept.
		Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("User made an action").build());

		// Set the user in the current context.
		Sentry.getContext().setUser(new UserBuilder().setEmail("hello@sentry.io").build());

		// Add extra data to future events in this context.
		Sentry.getContext().addExtra("extra", "thing");

		// Add an additional tag to future events in this context.
		Sentry.getContext().addTag("tagName", "tagValue");

		/*
		 * This sends a simple event to Sentry using the statically stored instance that was created in the ``main`` method.
		 */
		Sentry.capture("This is another logWithStaticAPI test");

		try {
			unsafeMethod();
		} catch (Exception e) {
			// This sends an exception event to Sentry using the statically stored instance
			// that was created in the ``main`` method.
			Sentry.capture(e);
		}
	}

	public static String getLoggerFileName() {
		org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) log;
		Appender appender = loggerImpl.getAppenders().get("LogFile");

		if (appender != null)
			return ((FileAppender) appender).getFileName();
		else
			return "NOT_CONFIGURED";
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
