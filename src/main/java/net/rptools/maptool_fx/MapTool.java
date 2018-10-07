/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx;

import java.awt.Toolkit;
import java.io.IOException;
import java.lang.Runtime.Version;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rptools.clientserver.hessian.client.ClientConnection;
import net.rptools.lib.BackupManager;
import net.rptools.lib.DebugStream;
import net.rptools.lib.EventDispatcher;
import net.rptools.lib.net.RPTURLStreamHandlerFactory;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUpdate;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.AssetURLStreamHandler;
import net.rptools.maptool.client.AutoSaveManager;
import net.rptools.maptool.client.ClientMethodHandler;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.swing.MapToolEventQueue;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.ObservableList;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.protocol.syrinscape.SyrinscapeURLStreamHandler;
import net.rptools.maptool.server.MapToolServer;
import net.rptools.maptool.server.ServerCommand;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.transfer.AssetTransferManager;
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

	private static String clientId = AppUtil.readClientId();

	public static enum ZoneEvent {
		Added, Removed, Activated, Deactivated
	}

	public static enum PreferencesEvent {
		Changed
	}

	private static String version = "DEVELOPMENT";;
	private static String vendor = "Nerps!";

	private static Campaign campaign;

	private static ObservableList<Player> playerList;
	private static ObservableList<TextMessage> messageList;
	private static Player player;

	private static ClientConnection conn;
	private static ClientMethodHandler handler;
	private static MapToolServer server;
	private static ServerCommand serverCommand;
	private static ServerPolicy serverPolicy;

	private static BackupManager backupManager;
	private static AssetTransferManager assetTransferManager;
	private static ServiceAnnouncer announcer;
	private static AutoSaveManager autoSaveManager;
	private static EventDispatcher eventDispatcher;
	private static MapToolLineParser parser = new MapToolLineParser();

	private static final MTWebAppServer webAppServer = new MTWebAppServer();

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

	private final String MAPTOOL_FXML = "/net/rptools/maptool/fx/view/MapTool.fxml";

	private static final String USAGE = "<html><body width=\"400\">You are running MapTool with insufficient memory allocated (%dMB).<br><br>"
			+ "You may experience odd behavior, especially when connecting to or hosting a server.<br><br>  "
			+ "MapTool will launch anyway, but it is recommended that you increase the maximum memory allocated or don't set a limit.</body></html>";

	static MapTool_Controller mapTool_Controller;

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
			// JFrame frame = new JFrame();
			// SwingUtil.centerOnScreen(frame);
			// frame.setVisible(true);

			String errorCreatingDir = "Error creating data directory";
			// log.error(errorCreatingDir, t);
			// JOptionPane.showMessageDialog(frame, t.getMessage(), errorCreatingDir, JOptionPane.ERROR_MESSAGE);
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

		// Initialize Sentry.io logging
		Sentry.init();

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

		// Set MapTool version, release, os, & environment in Sentry
		SentryClient sentryClient = Sentry.getStoredClient();
		sentryClient.setRelease(AppUpdate.getCommitSHA());

		if (sentryClient.getRelease() == null)
			sentryClient.setEnvironment("Development");

		sentryClient.addTag("os", System.getProperty("os.name"));
		sentryClient.addTag("version", MapTool.getVersion());

		Sentry.setStoredClient(sentryClient);

		if (listMacros) {
			String logOutput = null;
			List<String> macroList = parser.listAllMacroFunctions();
			Collections.sort(macroList);

			for (String macro : macroList) {
				logOutput += "\n" + macro;
			}

			log.info("Current list of Macro Functions: " + logOutput);
		}

		verifyJavaVersion();

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
		// stage = primaryStage;
		// setUserAgentStylesheet(STYLESHEET_MODENA); // Setting the style back to the new Modena
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAPTOOL_FXML), ResourceBundle.getBundle(AppConstants.MAP_TOOL_BUNDLE));
		VBox root = fxmlLoader.load();
		mapTool_Controller = (MapTool_Controller) fxmlLoader.getController();

		Scene scene = new Scene(root);
		primaryStage.setTitle(AppConstants.APP_NAME + AppConstants.APP_TAG_LINE);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(AppConstants.MAP_TOOL_ICON)));
		primaryStage.setScene(scene);

		// primaryStage.setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
		// @Override
		// public void handle(javafx.stage.WindowEvent event) {
		// // tokentool_Controller.exitApplication();
		// }
		// });

		// EventQueue.invokeLater(new Runnable() {
		// public void run() {
		// initialize();
		//
		// EventQueue.invokeLater(new Runnable() {
		// public void run() {
		// clientFrame.setVisible(true);
		//
		// // Add a LibGDX App/window for testing
		// // if (startLibGDX) {
		// // SwingUtilities.invokeLater(new Runnable() {
		// // public void run() {
		// // MapToolLwjglApplication = new DesktopLauncher(clientFrame);
		// // libgdxLoaded = true;
		// // }
		// // });
		// // }
		//
		// EventQueue.invokeLater(new Runnable() {
		// public void run() {
		// postInitialize();
		// }
		// });
		// }
		// });
		// }
		// });
		// new Thread(new HeapSpy()).start();

		// old // initialize();
		// MapTool_Controller.setMapToolFrameNode(clientFrame);
		primaryStage.show();
		// old // postInitialize();

		// Now that the Application is loaded, check for new release...
		AppUpdate.gitHubReleases();
	}

	public static void main(String[] args) {
		launch(args);
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
		try {
			version = java.lang.Runtime.version();
			log.info("Java Version = " + version);

			// boolean keepgoing = true;
			if (version.feature() < 11) {
				log.warn("Wrong version of Java detected and unsupported!");
				// keepgoing = confirm("msg.error.wrongJavaVersion", version);
			} else {
				return;
			}
		} catch (Exception e) {
			log.error("Error determining Java version.", e);
			// keepgoing = confirm("msg.error.unknownJavaVersion");
		}

		System.exit(11);
	}
}
