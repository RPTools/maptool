/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 * 
 * Created on May 30, 2010, 10:27:59 AM
 * 
 * Lee: Features extended on February, 2013
 * 
 * Azhrei: extensive cleanup, string externalization, configuration file
 * handling. June 10th, 2013
 */

package net.rptools.maptool.launcher;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.rptools.maptool.launcher.PathUtils.PathResolutionException;

/**
 * Generic launcher for MapTool.
 * 
 * Prompts user for the various memory settings to use when starting MapTool and
 * then launches it with those settings preserving them in the mt.cfg file for
 * use later.
 * 
 * @authors Phergus, Lee, Azhrei
 */
@SuppressWarnings("serial")
public class MapToolLauncher extends JFrame {
	// Public so that other classes in this file can get to it.
	public static final Logger log = Logger.getLogger(MapToolLauncher.class.getName());

	private static final String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
	private static final boolean IS_MAC = OS.contains("mac"); //$NON-NLS-1$
	private static final boolean IS_WINDOWS = OS.contains("windows"); //$NON-NLS-1$

	private static final String EMPTY = ""; //$NON-NLS-1$
	private static final String ASSERTIONS_OPTION = "-ea"; //$NON-NLS-1$
	private static final String DATADIR_OPTION = "-DMAPTOOL_DATADIR="; //$NON-NLS-1$
	private static final String MAC_TITLE_FIX = "-Xdock:name=MapTool"; // $NON-NLS-1$
	private static final String LOCALE_LANGUAGE_OPTION = "-Duser.language="; //$NON-NLS-1$
	private static final String LOCALE_COUNTRY_OPTION = "-Duser.country="; //$NON-NLS-1$

	private static final int DEFAULT_MAXMEM = 512;
	private static final int DEFAULT_MINMEM = 64;
	private static final int DEFAULT_STACKSIZE = 2;

	private static int maxMemVal = DEFAULT_MAXMEM;
	private static int minMemVal = DEFAULT_MINMEM;
	private static int stackSizeVal = DEFAULT_STACKSIZE;

	private static boolean promptUser = true;
	private static boolean startConsole = false;

	// Lee: path info
	// FIXME Change 'javaDir' from String to File
	private static String javaDir = EMPTY;
	private static String jbPathText = null;
	private static String jbMTJarText = null;
	private static String maxMemFormat = "-Xmx%dM"; //$NON-NLS-1$
	private static String maxMemStr;
	private static String minMemFormat = "-Xms%dM"; //$NON-NLS-1$
	private static String minMemStr;
	private static String stackSizeFormat = "-Xss%dM"; //$NON-NLS-1$
	private static String stackSizeStr;
	private static String jarCommand = "-jar"; //$NON-NLS-1$

	/*
	 * Lee: registering the MT executable due to the wildcard bug in J7u10+.
	 * This is also helpful for multiple MT instances with different versions.
	 */
	private static String mapToolJarName = null;
	private static String mapToolVersion = EMPTY; // added to MAC_TITLE_FIX string

	private static String mtArg = "run"; //$NON-NLS-1$
	private static String extraArgs = EMPTY;
	private static File currentDir;
	private static String mapToolDataDir = EMPTY;
	private static File mapToolJarDir;
	private static String mapToolLocale = EMPTY;

	private static List<LoggingConfig> logConfigs = null;
	private static Map<String, String> originalSettings;
	private static Map<String, String> locales;

	/**
	 * The list of fields that will be recognized when reading the configuration
	 * file. This is also the order that the fields will be written out (if they
	 * exist) whenever the configuration file is saved.
	 */
	// @formatter:off
	private static final String[] recognizedFields = new String[] {
		"MAXMEM", "MINMEM", "STACKSIZE", //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
		// The first is allowed when reading, but the second is always used when writing.
		"DATA_DIRECTORY", "MAPTOOL_DATADIR", //$NON-NLS-1$ $NON-NLS-2$
		"MAPTOOL_DIRECTORY", "EXECUTABLE", //$NON-NLS-1$ $NON-NLS-2$
		"CONSOLE", "PROMPT", //$NON-NLS-1$ $NON-NLS-2$
		"RELATIVE_PATHS", // $NON-NLS-1$
		"ARGS", //$NON-NLS-1$
		"LOGGING", //$NON-NLS-1$
		"LOCALE", //$NON-NLS-1$
		"JAVA_DIRECTORY", //$NON-NLS-1$
	};
	private static final String[][] invocationCommands = new String[][] {
		new String[] { }, // empty array means nothing to add as a command line prefix when Console option enabled
		new String[] { "cmd", "/k", "start" }, // Windows
		new String[] { "xterm", "-T", "MapTool", "-e" }, // Linux, Unix !OSX
		new String[] { "osascript", "-e", "tell application ''Terminal'' to do script \"cd ''{0}''; {1}\"" }, // OSX (doesn't work -- use #1)
	};
	// @formatter:on

	private File cfgFile = null;

	private static ImageIcon icon;

	private static final JButton jbLaunch = new JButton();
	// Lee: to set the executable - to counteract the wildcard bug in Java 7u10+
	private static final JButton jbMTJar = new JButton();
	// Lee: path to JVM
	private static final JButton jbPath = new JButton();
	private static final ButtonGroup langGroup = new ButtonGroup();

	// Lee: to toggle between fg MT with a console or not
	private static final JCheckBox jcbRelativePath = new JCheckBox();
	private static final JCheckBox jcbConsole = new JCheckBox();
	private static final JCheckBox jcbPromptUser = new JCheckBox();
	private static final JCheckBox jcbKeepOpen = new JCheckBox();

	// Lee: check box series for troubleshooting tab
	private static final JCheckBox jcbEnableAssertions = new JCheckBox();

	private static final JLabel jlMTLogo = new JLabel();

	private static final InfoTextField jtfCommand = new InfoTextField();
	private static final InfoTextField jtfArgs = new InfoTextField();
	private static final InfoTextField jtfMaxMem = new InfoTextField();
	private static final InfoTextField jtfMinMem = new InfoTextField();
	private static final InfoTextField jtfStackSize = new InfoTextField();

	private static final JTabbedPane mtlOptions = new JTabbedPane();

	/**
	 * Creates new form MTLGui
	 * 
	 * @throws IOException
	 */
	public MapToolLauncher() throws IOException, URISyntaxException {
		final File dir = new File(MapToolLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		currentDir = mapToolJarDir = dir.getParentFile();

		File fromDir = new File(currentDir, "translations");
		locales = CopiedFromOtherJars.getListOfLanguages(fromDir);

		cfgFile = new File(currentDir, "mt.cfg"); //$NON-NLS-1$
		icon = new ImageIcon(getClass().getResource("MapToolLogo90x90.png")); //$NON-NLS-1$
		// This must be done before calling any methods of CopiedFromOtherJars or the LOCALE setting won't work.
		readCfgFile();

		final File dataDirPath = new File(mapToolDataDir);
		if (!dataDirPath.exists())
			promptUser = true;

		if (promptUser) {
			initComponents();
			if (mapToolJarName.equalsIgnoreCase(CopiedFromOtherJars.getText("msg.info.selectMapToolJar"))) {
				jbMTJar.requestFocusInWindow();
			} else {
				jbLaunch.requestFocusInWindow();
			}
			CopiedFromOtherJars.centerOnScreen(this);
			checkMinMem(); // sets default values and stores results in JTextFields
			checkMaxMem();
			checkStackSize();
			updateCommand();
			jbLaunch.requestFocusInWindow();
		} else {
			// Do we need to recreate the 'logging.xml' every time?  Or save the configuration
			// file every time?
			createLogConfig();
			saveCfgFile();

			// Make a list of all XML files in the current directory.  We're going to copy new ones
			// to the data directory when the Launch button is activated.
			copyXmlFiles(logConfigs, dataDirPath);

			setVisible(false);
			launchMapTool();
			System.exit(0);
		}
	}

	/**
	 * Launch MT in a separate process with its own JVM.
	 * 
	 * Note that each string passed in must have just a single "argument". Which
	 * is to say that you can't just lump them all into one string and pass
	 * that. So the max mem gets one, the min mem gets one and so on.
	 * 
	 * @throws IOException
	 */
	private void launchMapTool() throws IOException {
		ProcessBuilder pb = null;
		List<String> cmdArgs;
		File log = new File(mapToolDataDir, "log.txt");

		// Lee: console options
		if (startConsole) {
			if (IS_WINDOWS) {
				cmdArgs = getLaunchCommand(invocationCommands[1], mapToolJarDir);
				//				log = null; // Windows doesn't like having multiple tasks opening the same file for writing all at once
			} else if (IS_MAC) {
				cmdArgs = getLaunchCommand(invocationCommands[0], mapToolJarDir);
				pb = new ProcessBuilder(new String[] { "open", "-a", "Console", log.toString() });
				pb.start();
			} else {
				// Lee: Linux launch
				cmdArgs = getLaunchCommand(invocationCommands[2], mapToolJarDir);
			}
		} else {
			// Lee: launch normally
			cmdArgs = getLaunchCommand(invocationCommands[0], mapToolJarDir);
		}
		pb = new ProcessBuilder(cmdArgs);
		logMsg(Level.INFO, "Setting current directory to: {0}.  Command line is {1}.", null, new Object[] { mapToolJarDir, cmdArgs.toString() });
		try {
			pb.directory(mapToolJarDir);
			// This is a great idea, but .redirectOutput() requires Java 1.7+ so it won't build for
			// J6 and hence users with the Java provided by Apple can't use the launcher. :(
			//			if (log != null)
			//				pb.redirectOutput(log).redirectErrorStream(true);
			pb.start();
		} catch (final IOException ex) {
			logMsg(Level.SEVERE, "Error starting MapTool instance; dir={0}, cmd={1}\n{2}", "msg.error.startingMapTool", mapToolJarDir, cmdArgs, ex);
			return;
		}
	}

	private JPanel buildBasicPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		// BASIC:  Top panel
		final JPanel logoPanel = new JPanel();
		logoPanel.setLayout(new FlowLayout());
		logoPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), CopiedFromOtherJars.getText("msg.logoPanel.border"))); //$NON-NLS-1$

		jlMTLogo.setIcon(icon);
		logoPanel.add(jlMTLogo);

		// BASIC:  Middle panel
		final JPanel memPanel = new JPanel();
		memPanel.setLayout(new GridLayout(3, 2));
		memPanel.setBorder(new LineBorder(Color.WHITE));

		jtfMaxMem.setHorizontalAlignment(SwingConstants.RIGHT);
		jtfMaxMem.setInfo(CopiedFromOtherJars.getText("msg.info.javaMaxMem", DEFAULT_MAXMEM)); //$NON-NLS-1$ 
		jtfMaxMem.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.javaMaxMem")); //$NON-NLS-1$
		jtfMaxMem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jtfMaxMemActionPerformed(evt);
			}
		});
		jtfMaxMem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent evt) {
				jtfMaxMemFocusLost(evt);
			}

			@Override
			public void focusGained(FocusEvent evt) {
				jtfMaxMemFocusLost(evt);
			}
		});
		jtfMaxMem.addKeyListener(new InputValidator());

		jtfMinMem.setHorizontalAlignment(SwingConstants.RIGHT);
		jtfMinMem.setInfo(CopiedFromOtherJars.getText("msg.info.javaMinMem", DEFAULT_MINMEM)); //$NON-NLS-1$ 
		jtfMinMem.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.javaMinMem")); //$NON-NLS-1$
		jtfMinMem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jtfMinMemActionPerformed(evt);
			}
		});
		jtfMinMem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent evt) {
				jtfMinMemFocusLost(evt);
			}

			@Override
			public void focusGained(FocusEvent evt) {
				jtfMinMemFocusLost(evt);
			}
		});
		jtfMinMem.addKeyListener(new InputValidator());

		jtfStackSize.setHorizontalAlignment(SwingConstants.RIGHT);
		jtfStackSize.setInfo(CopiedFromOtherJars.getText("msg.info.javaStackSize", DEFAULT_STACKSIZE)); //$NON-NLS-1$ 
		jtfStackSize.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.javaStackSize")); //$NON-NLS-1$
		jtfStackSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jtfStackSizeActionPerformed(evt);
			}
		});
		jtfStackSize.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent evt) {
				jtfStackSizeFocusLost(evt);
			}

			@Override
			public void focusGained(FocusEvent evt) {
				jtfStackSizeFocusLost(evt);
			}
		});
		jtfStackSize.addKeyListener(new InputValidator());

		memPanel.add(jtfMaxMem);
		memPanel.add(jtfMinMem);
		memPanel.add(jtfStackSize);

		// BASIC:  Bottom panel
		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());

		final JPanel cbPanel = new JPanel();
		cbPanel.setLayout(new GridLayout(2, 1));
		cbPanel.setBorder(new LineBorder(Color.GRAY));

		jcbPromptUser.setSelected(true);
		jcbPromptUser.setText(CopiedFromOtherJars.getText("msg.info.promptAtNextLaunch")); //$NON-NLS-1$
		jcbPromptUser.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.promptAtNextLaunch")); //$NON-NLS-1$
		jcbPromptUser.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				promptUser = jcbPromptUser.isSelected();
			}
		});

		jbMTJar.setText(jbMTJarText);
		jbMTJar.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.registerMapToolJar")); //$NON-NLS-1$
		jbMTJar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileFilter filter = new FileNameExtensionFilter(CopiedFromOtherJars.getText("msg.chooser.javaExecutable"), "jar"); //$NON-NLS-1$ //$NON-NLS-2$
				jfc.addChoosableFileFilter(filter);
				jfc.setFileFilter(filter);
				if (IS_MAC) {
					filter = new FileNameExtensionFilter(CopiedFromOtherJars.getText("msg.chooser.appleApplicationBundle"), "app"); //$NON-NLS-1$ //$NON-NLS-2$
					jfc.addChoosableFileFilter(filter);
				}
				jfc.setCurrentDirectory(mapToolJarDir);

				final int returnVal = jfc.showOpenDialog(jbMTJar);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File f = jfc.getSelectedFile();
					final String fileName = f.getName();
					if (IS_MAC && fileName.endsWith(".app")) { //$NON-NLS-1$
						File jarDir = new File(f.getParentFile(), fileName);
						if (jarDir.isDirectory()) {
							jarDir = new File(jarDir, "Contents/Resources/Java"); //$NON-NLS-1$
							if (jarDir.isDirectory()) {
								mapToolJarDir = jarDir;
								mapToolJarName = fileName.replace(".app", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								logMsg(Level.SEVERE, "{0} does not contain 'Contents/Resources/Java' like it should!", "msg.chooser.badAppLocation", jarDir); //$NON-NLS-1$ //$NON-NLS-2$
								return;
							}
						} else {
							logMsg(Level.SEVERE, "{0} is not a directory and it should be!", "msg.chooser.badAppLocation", jarDir); //$NON-NLS-1$ //$NON-NLS-2$
							return;
						}
					} else {
						mapToolJarName = fileName;
						mapToolJarDir = f.getParentFile();
					}
					logMsg(Level.INFO, f.toString(), null);
					jbMTJar.setText(fileName.replace(".jar", EMPTY)); //$NON-NLS-1$
					if (fileName.toLowerCase().startsWith("maptool-")) {
						// We expect the name matches 'maptool-1.3.b89.jar'
						mapToolVersion = " " + fileName.substring(8, 11);
					} else {
						logMsg(Level.SEVERE, "Cannot determine MapTool version number from JAR filename: {0}", "msg.info.noMapToolVersion", fileName); //$NON-NLS-1$
						mapToolVersion = EMPTY;
					}
					jbLaunch.setEnabled(true);
					updateCommand();
					jbLaunch.requestFocusInWindow();
				}
			}
		});

		cbPanel.add(jcbPromptUser);
		cbPanel.add(jbMTJar);

		southPanel.add(cbPanel, BorderLayout.CENTER);

		p.add(memPanel, BorderLayout.CENTER);
		p.add(logoPanel, BorderLayout.NORTH);
		p.add(southPanel, BorderLayout.SOUTH);
		p.setBorder(new LineBorder(Color.BLACK));
		return p;
	}

	private JPanel buildLanguagePanel() {
		final JPanel langPanel = new JPanel();
		langPanel.setLayout(new BorderLayout());

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 1));
		buttonPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), CopiedFromOtherJars.getText("msg.langPanel.border"))); //$NON-NLS-1$

		String[] localeArray = locales.keySet().toArray(new String[0]);
		Arrays.sort(localeArray);

		ActionListener localeUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapToolLocale = e.getActionCommand();
				// Setting the language won't work without reinitalizing the interface.
				// Instead, we just save it and use it for MapTool.
				//				CopiedFromOtherJars.setLanguage(mapToolLocale);
				updateCommand();
			}
		};
		// Always set the first button ("Default Locale") to true and let one of the others change it, if needed.
		JRadioButton jrb = new JRadioButton(CopiedFromOtherJars.getText("msg.info.defaultLocale"), true);
		jrb.setActionCommand(EMPTY);
		langGroup.add(jrb);
		buttonPanel.add(jrb);
		jrb.addActionListener(localeUpdate);

		for (String locale : localeArray) {
			String name = locale + " - " + locales.get(locale);
			jrb = new JRadioButton(name);
			jrb.setActionCommand(locale);
			jrb.addActionListener(localeUpdate);
			langGroup.add(jrb);
			buttonPanel.add(jrb);
			if (mapToolLocale.equalsIgnoreCase(locale))
				jrb.setSelected(true);
		}
		langPanel.add(buttonPanel, BorderLayout.NORTH);
		return langPanel;
	}

	private JPanel buildAdvancedPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(new LineBorder(Color.BLACK));

		final JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());

		final JPanel argPanel = new JPanel();
		argPanel.setLayout(new BorderLayout());

		jtfArgs.setInfo(CopiedFromOtherJars.getText("msg.info.javaArgumentsHere")); //$NON-NLS-1$
		jtfArgs.setText(extraArgs);
		jtfArgs.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.javaArgumentsHere")); //$NON-NLS-1$
		jtfArgs.setCaretPosition(0);
		jtfArgs.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					jbLaunch.requestFocusInWindow();
				}
			}
		});
		jtfArgs.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				jtfArgs.selectAll();
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				jtfArgs.setCaretPosition(0);
				if (!jtfArgs.getText().trim().equals(extraArgs)) {
					extraArgs = jtfArgs.getText();
					jcbEnableAssertions.setSelected(extraArgs.contains(ASSERTIONS_OPTION));
					if (extraArgs.contains(DATADIR_OPTION)) {
						extraArgs = cleanExtraArgs(extraArgs);
					}
					updateCommand();
				}
			}
		});

		argPanel.add(jtfArgs, BorderLayout.CENTER);
		controlPanel.add(argPanel, BorderLayout.NORTH);

		final JPanel holdPanel = new JPanel();
		holdPanel.setLayout(new GridLayout(0, 1));

		jcbRelativePath.setText(CopiedFromOtherJars.getText("msg.info.useRelativePathnames")); //$NON-NLS-1$
		jcbRelativePath.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.useRelativePathnames")); //$NON-NLS-1$
		jcbRelativePath.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateCommand();
			}
		});
		//		jcbRelativePath.setSelected(false); // since initComponents() is called after reading the config, don't do this here

		jcbConsole.setText(CopiedFromOtherJars.getText("msg.info.launchWithConsole")); //$NON-NLS-1$
		jcbConsole.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.launchWithConsole")); //$NON-NLS-1$
		jcbConsole.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				startConsole = jcbConsole.isSelected();
				updateCommand();
			}
		});
		jcbConsole.setSelected(startConsole);

		jbPath.setText(jbPathText);
		jbPath.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.dirForAltJava")); //$NON-NLS-1$
		jbPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jbPath.getText().equalsIgnoreCase(CopiedFromOtherJars.getText("msg.info.setJavaVersion"))) { //$NON-NLS-1$
					final JFileChooser jfc = new JFileChooser();
					if (!javaDir.isEmpty()) {
						jfc.setCurrentDirectory(new File(javaDir));
					} else {
						jfc.setCurrentDirectory(new File(".")); //$NON-NLS-1$
					}
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					final int returnVal = jfc.showOpenDialog(jbPath);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						final File f = jfc.getSelectedFile();
						final String test = f.getPath() + File.separator;

						// Lee: naive search for java command. will improve in the future
						final List<String> fileList = Arrays.asList(f.list());
						boolean javaFound = false;

						for (final String fileName : fileList) {
							final File check = new File(f, fileName);
							final String lc = check.getName().toLowerCase();
							if (lc.equals("java") || (IS_WINDOWS && lc.startsWith("java."))) { //$NON-NLS-1$ //$NON-NLS-2$ 
								javaFound = true;
								break;
							}
						}
						if (javaFound) {
							jbPath.setText(CopiedFromOtherJars.getText("msg.info.resetToDefaultJava")); //$NON-NLS-1$
							javaDir = test;
							updateCommand();
						} else {
							logMsg(Level.SEVERE, "Java executable not found in {0}", "msg.error.javaCommandNotFound", f); //$NON-NLS-1$ $NON-NLS-2$
						}
					}
				} else {
					jbPath.setText(CopiedFromOtherJars.getText("msg.info.setJavaVersion")); //$NON-NLS-1$
					javaDir = EMPTY;
				}
			}
		});

		holdPanel.add(jcbRelativePath);
		holdPanel.add(jcbConsole);
		holdPanel.add(jbPath);
		controlPanel.add(holdPanel, BorderLayout.SOUTH);

		final JPanel logPanel = new JPanel();
		logPanel.setLayout(new GridLayout(0, 1));
		logPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), CopiedFromOtherJars.getText("msg.logPanel.border"))); //$NON-NLS-1$
		for (final LoggingConfig config : logConfigs) {
			config.chkbox.setText(config.getProperty("desc")); //$NON-NLS-1$
			config.chkbox.setToolTipText(config.getProperty("ttip")); //$NON-NLS-1$
			logPanel.add(config.chkbox);
		}
		p.add(logPanel, BorderLayout.CENTER);
		p.add(controlPanel, BorderLayout.SOUTH);
		return p;
	}

	private JPanel buildTroubleshootingPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		ActionListener levelChange = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Level x = Level.parse(e.getActionCommand());
				if (Level.OFF.equals(x) || Level.INFO.equals(x) || Level.WARNING.equals(x) || Level.SEVERE.equals(x))
					log.setLevel(x);
			}
		};
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new GridLayout(0, 1));
		logPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), CopiedFromOtherJars.getText("msg.logDetailPanel.border"))); //$NON-NLS-1$
		logPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		ButtonGroup logGroup = new ButtonGroup();
		for (Level type : new Level[] { Level.OFF, Level.INFO, Level.WARNING, Level.SEVERE }) {
			JRadioButton jrb = new JRadioButton(type.toString());
			jrb.setActionCommand(type.toString());
			jrb.addActionListener(levelChange);
			jrb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.red), jrb.getBorder()));
			logPanel.add(jrb);
			logGroup.add(jrb);
			if (type == Level.WARNING) {
				jrb.setSelected(true);
				log.setLevel(type);
			}
		}
		jcbEnableAssertions.setAlignmentX(Component.LEFT_ALIGNMENT);
		jcbEnableAssertions.setText(CopiedFromOtherJars.getText("msg.info.enableAssertions")); //$NON-NLS-1$
		jcbEnableAssertions.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.enableAssertions")); //$NON-NLS-1$
		jcbEnableAssertions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (!extraArgs.contains(ASSERTIONS_OPTION)) {
						extraArgs = (ASSERTIONS_OPTION + " " + extraArgs); //$NON-NLS-1$
					}
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					extraArgs = extraArgs.replace(ASSERTIONS_OPTION, ""); //$NON-NLS-1$
				}
				extraArgs = extraArgs.trim();
				jtfArgs.setText(extraArgs);
				updateCommand();
			}
		});
		p.add(logPanel, BorderLayout.NORTH);
		Box other = new Box(BoxLayout.PAGE_AXIS);
		other.add(jcbEnableAssertions);
		other.add(Box.createVerticalGlue());
		p.add(other, BorderLayout.CENTER);
		return p;
	}

	/**
	 * This method is called from within the constructor to initialize the form
	 * components.
	 */
	private void initComponents() {
		// Lee: for aesthetics and Linux won't display window controls on an untitled window.
		final String version = CopiedFromOtherJars.getVersion();
		setTitle(CopiedFromOtherJars.getText("msg.title.mainWindow", version)); //$NON-NLS-1$

		if (jbPathText == null)
			jbPathText = CopiedFromOtherJars.getText("msg.info.setJavaVersion"); //$NON-NLS-1$
		if (jbMTJarText == null)
			jbMTJarText = CopiedFromOtherJars.getText("msg.info.selectMapToolJar"); //$NON-NLS-1$
		if (mapToolJarName == null)
			mapToolJarName = CopiedFromOtherJars.getText("msg.info.selectMapToolJar"); //$NON-NLS-1$

		final Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		final JPanel basicPanel = buildBasicPanel();
		final JPanel langPanel = buildLanguagePanel();
		final JPanel advancedPanel = buildAdvancedPanel();
		final JPanel tsPanel = buildTroubleshootingPanel();

		mtlOptions.addTab(CopiedFromOtherJars.getText("msg.tab.basic"), basicPanel); //$NON-NLS-1$
		mtlOptions.addTab(CopiedFromOtherJars.getText("msg.tab.language"), langPanel); //$NON-NLS-1$
		mtlOptions.addTab(CopiedFromOtherJars.getText("msg.tab.advanced"), advancedPanel); //$NON-NLS-1$
		mtlOptions.addTab(CopiedFromOtherJars.getText("msg.tab.troubleshoot"), tsPanel); //$NON-NLS-1$

		cp.add(mtlOptions, BorderLayout.CENTER);
		cp.add(jtfCommand, BorderLayout.SOUTH);

		// Lee: user must register MT executable
		jbLaunch.setEnabled(!mapToolJarName.equalsIgnoreCase(CopiedFromOtherJars.getText("msg.info.selectMapToolJar"))); //$NON-NLS-1$

		jbLaunch.setText(CopiedFromOtherJars.getText("msg.info.launchMapTool")); //$NON-NLS-1$
		jbLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					jbLaunchActionPerformed(evt);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});

		jtfCommand.setEditable(false);
		jtfCommand.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		jtfCommand.setText(CopiedFromOtherJars.getText("msg.info.cmdLineShownHere")); //$NON-NLS-1$
		jtfCommand.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				jtfCommand.selectAll();
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				jtfCommand.setCaretPosition(0);
			}
		});

		jcbKeepOpen.setSelected(false);
		jcbKeepOpen.setText(CopiedFromOtherJars.getText("msg.info.keepLauncherOpen")); //$NON-NLS-1$
		jcbKeepOpen.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.keepLauncherOpen")); //$NON-NLS-1$

		final JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(3, 1));
		lowerPanel.setBorder(new LineBorder(Color.BLACK));
		lowerPanel.add(jbLaunch);
		lowerPanel.add(jcbKeepOpen);
		lowerPanel.add(jtfCommand);
		cp.add(lowerPanel, BorderLayout.SOUTH);

		mtlOptions.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final JTabbedPane source = (JTabbedPane) e.getSource();
				final String tabName = source.getTitleAt(source.getSelectedIndex());

				if (jbLaunch.isEnabled()) {
					jbLaunch.requestFocusInWindow();
				} else {
					if (tabName.equals(CopiedFromOtherJars.getText("msg.tab.basic"))) { //$NON-NLS-1$
						jbMTJar.requestFocusInWindow();
					} else if (tabName.equals(CopiedFromOtherJars.getText("msg.tab.language"))) { //$NON-NLS-1$
						// This should work as long as there's at least one language in the list.
						langGroup.getElements().nextElement().requestFocusInWindow();
					} else if (tabName.equals(CopiedFromOtherJars.getText("msg.tab.advanced"))) { //$NON-NLS-1$
						jcbConsole.requestFocusInWindow();
						jtfArgs.setText(extraArgs);
					} else {
						jcbEnableAssertions.requestFocusInWindow();
					}
				}
			}
		});
		Dimension d = new Dimension(advancedPanel.getPreferredSize().width, 25);
		jtfArgs.setPreferredSize(d);
		//		mtlOptions.setPreferredSize(new Dimension(350, getPreferredSize().height));
		d.width = -1;
		d.height = -1;
		JLabel tabLabel = null;
		int tabs = mtlOptions.getTabCount();
		while (tabs-- > 0) {
			Component tab = mtlOptions.getTabComponentAt(tabs);
			if (tab == null) {
				if (tabLabel == null)
					tabLabel = new JLabel();
				tabLabel.setText(mtlOptions.getTitleAt(tabs));
				tab = tabLabel;
			}
			Dimension dim = tab.getPreferredSize();
			d.width = Math.max(dim.width, d.width);
			d.height = Math.max(dim.height, d.height);
		}
		// Set width to width of largest tab * number of tabs, then add 20%.
		d.width = d.width * 120 / 100 * mtlOptions.getTabCount();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setIconImage(icon.getImage());

		// To prevent the tabs from wrapping or scrolling
		setMinimumSize(new Dimension(d.width, getSize().height));
		pack();
		setResizable(true);
	}

	/**
	 * Accumulates all logging facilities in one place, ie. invoke this guy and
	 * he'll invoke the Logger's log() method as well as JOptionPane's
	 * showMessageDialog() method.
	 * 
	 * @param type
	 *            severity level of the message
	 * @param english
	 *            English message (only displayed to Logger)
	 * @param msgId
	 *            key for properties file; displayed by Logger and looked up in
	 *            properties for user dialog
	 * @param args
	 *            parameters for <code>english</code> and <code>msgId</code>
	 *            value from properties
	 */
	public static void logMsg(Level type, String english, String msgId, Object... args) {
		if (msgId == null) {
			log.log(type, english, args);
		} else {
			log.log(type, msgId + ": " + english, args);
			String msg = CopiedFromOtherJars.getText(msgId, args);
			// If we don't find the string in the properties file, try using the English string as
			// a property value and use it to format the arguments.
			if (msg.equals(msgId))
				msg = MessageFormat.format(english, args);
			// @formatter:off
			JOptionPane.showMessageDialog(null, msg, msgId,
				type == Level.SEVERE ? ERROR_MESSAGE :
				type == Level.WARNING ? WARNING_MESSAGE :
				INFORMATION_MESSAGE);
			// @formatter:on
		}
	}

	private void checkStackSize() {
		final String text = jtfStackSize.getText();
		if (text.length() == 0) {
			stackSizeVal = DEFAULT_STACKSIZE;
		} else {
			int ss = CopiedFromOtherJars.parseInteger(text, DEFAULT_STACKSIZE);
			ss = Math.max(ss, 1);
			ss = Math.min(ss, maxMemVal / 64);
			stackSizeVal = ss;
		}
		assignStackSize();
	}

	private void assignStackSize() {
		jtfStackSize.setText(stackSizeVal == DEFAULT_STACKSIZE ? "" : Integer.toString(stackSizeVal)); //$NON-NLS-1$
	}

	private void checkMaxMem() {
		final String text = jtfMaxMem.getText();
		if (text.length() == 0) {
			maxMemVal = DEFAULT_MAXMEM;
		} else {
			int max = CopiedFromOtherJars.parseInteger(text, DEFAULT_MAXMEM);
			max = Math.max(max, minMemVal);
			max = Math.max(max, DEFAULT_MAXMEM);
			maxMemVal = max;
		}
		assignMaxMem();
	}

	private void assignMaxMem() {
		jtfMaxMem.setText(maxMemVal == DEFAULT_MAXMEM ? "" : Integer.toString(maxMemVal)); //$NON-NLS-1$
	}

	private void checkMinMem() {
		final String text = jtfMinMem.getText();
		if (text.length() == 0) {
			minMemVal = DEFAULT_MINMEM;
		} else {
			int min = CopiedFromOtherJars.parseInteger(text, DEFAULT_MINMEM);
			min = Math.max(min, 16);
			min = Math.min(min, maxMemVal);
			minMemVal = min;
		}
		assignMinMem();
	}

	private void assignMinMem() {
		jtfMinMem.setText(minMemVal == DEFAULT_MINMEM ? "" : Integer.toString(minMemVal)); //$NON-NLS-1$
	}

	private void updateStrings() {
		minMemStr = String.format(minMemFormat, minMemVal);
		maxMemStr = String.format(maxMemFormat, maxMemVal);
		stackSizeStr = String.format(stackSizeFormat, stackSizeVal);
	}

	private void updateCommand() {
		if (jbMTJar.getText().equals(CopiedFromOtherJars.getText("msg.info.selectMapToolJar"))) { //$NON-NLS-1$
			jtfCommand.setText(CopiedFromOtherJars.getText("msg.info.registerMapToolJar")); //$NON-NLS-1$
			return;
		}
		final String convert = commandToString(getCommand());
		jtfCommand.setText(convert);
		jtfCommand.setCaretPosition(0);
		jtfCommand.setToolTipText(CopiedFromOtherJars.getText("msg.tooltip.currentDirectory", currentDir.getAbsolutePath(), mapToolJarDir, convert)); //$NON-NLS-1$
	}

	private String[] getCommand() {
		updateStrings();
		String path = mapToolDataDir;
		if (path != null && jcbRelativePath.isSelected()) {
			try {
				path = PathUtils.getRelativePath(path + File.separator, mapToolJarDir + File.separator);
			} catch (final PathResolutionException e) {
				// If there is no common path, just use the original string.
			}
		}
		String jDir = javaDir;
		if (jDir != null && !jDir.isEmpty()) {
			try {
				if (jcbRelativePath.isSelected()) {
					jDir = PathUtils.getRelativePath(jDir + File.separator, mapToolJarDir + File.separator);
				}
			} catch (final PathResolutionException e) {
				// If there is no common path, just use the original string.
			}
			jDir = new File(jDir, "java").getPath(); //$NON-NLS-1$
		} else {
			jDir = "java"; //$NON-NLS-1$
		}
		// @formatter:off
		final String command[] = {
			jDir,
			minMemStr, maxMemStr, stackSizeStr,
			extraArgs,
			path != null ? DATADIR_OPTION + path : EMPTY,
			IS_MAC ? MAC_TITLE_FIX+mapToolVersion : EMPTY,
			localeToOptions(mapToolLocale),
			jarCommand, mapToolJarName, mtArg
		};
		// @formatter:on
		return command;
	}

	private List<String> getLaunchCommand(final String[] cmd, File jarDir) {
		final List<String> lc = new ArrayList<String>(16); // should only need a dozen or so

		lc.addAll(Arrays.asList(cmd));
		if (javaDir == null || javaDir.isEmpty()) {
			lc.add("java"); //$NON-NLS-1$
		} else {
			lc.add(javaDir + File.separator + "java"); //$NON-NLS-1$
		}
		lc.add(minMemStr);
		lc.add(maxMemStr);
		lc.add(stackSizeStr);

		if (extraArgs != null && extraArgs.length() > 0) {
			final String[] eaSplit = extraArgs.split(" "); //$NON-NLS-1$
			lc.addAll(Arrays.asList(eaSplit));
		}
		String path = mapToolDataDir;
		if (path != null && jcbRelativePath.isSelected()) {
			try {
				path = PathUtils.getRelativePath(path + File.separator, jarDir + File.separator);
			} catch (final PathResolutionException e) {
				// If there is no common path, just use the original string.
			}
		}
		if (path != null && !path.isEmpty()) {
			lc.add(DATADIR_OPTION + path);
		}

		if (IS_MAC) {
			lc.add(MAC_TITLE_FIX + mapToolVersion);
		}
		if (!mapToolLocale.isEmpty())
			lc.add(localeToOptions(mapToolLocale));

		lc.add(jarCommand);
		lc.add(mapToolJarName); // mapToolJarDir not needed, since it's set when building the ProcessBuilder
		lc.add(mtArg);
		return lc;
	}

	/**
	 * Given a locale string of the form <b>aa</b> or <b>aa_BB</b>, returns the
	 * appropriate Java command line options for setting the language to
	 * <b>aa</b> and, if provided, the country to <b>BB</b>. For this example,
	 * that means returning the string <code>-Duser.language=aa</code> or
	 * <code>-Duser.language=aa -Duser.country=BB</code>, respectively.
	 * 
	 * @param locale
	 *            locale string to convert
	 * @return Java command line options required
	 */
	private String localeToOptions(String locale) {
		if (mapToolLocale.isEmpty())
			return EMPTY;
		StringBuilder sb = new StringBuilder();
		sb.append(LOCALE_LANGUAGE_OPTION + mapToolLocale.substring(0, 2));
		if (mapToolLocale.length() > 3)
			sb.append(LOCALE_COUNTRY_OPTION + mapToolLocale.substring(3, 5));
		return sb.toString();
	}

	private String commandToString(String[] command) {
		final StringBuilder sb = new StringBuilder();
		for (final String entry : command) {
			sb.append(entry);
			sb.append(" "); //$NON-NLS-1$
		}
		return sb.toString().trim();
	}

	private void parseCfgValues(Map<String, String> values, List<String> errors) {
		String dir;
		File f;

		// This must be done before calling any methods of CopiedFromOtherJars or the LOCALE setting won't work.
		dir = values.get("LOCALE");
		if (dir != null) {
			dir = dir.trim();
			String prop = System.getProperty("user.language").trim();
			if (prop.isEmpty() || !dir.isEmpty()) {
				if (CopiedFromOtherJars.setLanguage(dir))
					mapToolLocale = dir;
			}
		}
		maxMemVal = CopiedFromOtherJars.parseInteger(values.get("MAXMEM"), DEFAULT_MAXMEM); //$NON-NLS-1$
		minMemVal = CopiedFromOtherJars.parseInteger(values.get("MINMEM"), DEFAULT_MINMEM); //$NON-NLS-1$
		stackSizeVal = CopiedFromOtherJars.parseInteger(values.get("STACKSIZE"), DEFAULT_STACKSIZE); //$NON-NLS-1$
		startConsole = CopiedFromOtherJars.parseBoolean(values.get("CONSOLE"), false); //$NON-NLS-1$
		promptUser = CopiedFromOtherJars.parseBoolean(values.get("PROMPT"), false); //$NON-NLS-1$
		jcbRelativePath.setSelected(CopiedFromOtherJars.parseBoolean(values.get("RELATIVE_PATHS"), false)); //$NON-NLS-1$

		// For the directory fields, they may be stored as either relative or absolute pathnames.
		// We keep only absolute pathnames internally and decide when writing out the config
		// file which type to save...
		dir = values.get("MAPTOOL_DIRECTORY"); //$NON-NLS-1$
		if (dir != null && !dir.isEmpty()) {
			// Returns 'null' when a relative name is passed in
			String normalDir = PathUtils.normalizeNoEndSeparator(dir);
			if (normalDir == null || normalDir.isEmpty()) {
				normalDir = PathUtils.normalizeNoEndSeparator(currentDir + File.separator + dir);
			}
			f = new File(normalDir);
			if (f.isDirectory()) {
				mapToolJarDir = f;
				logMsg(Level.INFO, "MAPTOOL_DIRECTORY={0}", null, f.toString());
			} else {
				String msg = CopiedFromOtherJars.getText("msg.error.bad.MAPTOOL_DIRECTORY", normalDir); //$NON-NLS-1$
				logMsg(Level.INFO, "Invalid directory for MAPTOOL_DIRECTORY: {0}\n{1}", null, dir, msg);
				errors.add(msg);
			}
		}
		dir = values.get("JAVA_DIRECTORY"); //$NON-NLS-1$
		if (dir != null && !dir.isEmpty()) {
			jbPathText = CopiedFromOtherJars.getText("msg.info.resetToDefaultJava"); //$NON-NLS-1$
			// Returns 'null' when a relative name is passed in
			String normalDir = PathUtils.normalizeNoEndSeparator(dir);
			if (normalDir == null || normalDir.isEmpty()) {
				normalDir = PathUtils.normalizeNoEndSeparator(mapToolJarDir + File.separator + dir);
			}
			f = new File(normalDir);
			if (f.isDirectory()) {
				javaDir = f.getAbsolutePath();
				logMsg(Level.INFO, "JAVA_DIRECTORY={0}", null, javaDir);
			} else {
				String msg = CopiedFromOtherJars.getText("msg.error.bad.JAVA_DIRECTORY", normalDir); //$NON-NLS-1$
				errors.add(msg);
			}
		}
		// For the data directory field allow multiple values.  They are, in increasing precedence:
		//   ~/.maptool
		//   DATA_DIRECTORY
		//   MAPTOOL_DATADIR
		//   MAPTOOL_DATADIR system property (from command line)
		dir = values.get("MAPTOOL_DATADIR"); //$NON-NLS-1$
		if (dir == null || dir.isEmpty()) {
			dir = values.get("DATA_DIRECTORY"); //$NON-NLS-1$
		}
		// If overridden on the command line, use that one in preference to what is currently in the file.
		// Note that this new value will be written back to the config file if/when the config file is saved.
		if (System.getProperty("MAPTOOL_DATADIR") != null) {
			dir = System.getProperty("MAPTOOL_DATADIR"); //$NON-NLS-1$
		}
		// If still no value, create one based on the user's home directory and the ".maptool" subdirectory.
		if (dir == null || dir.isEmpty()) {
			dir = System.getProperty("user.home") + File.separatorChar + ".maptool"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Returns 'null' when a relative name is passed in
		String normalDir = PathUtils.normalizeNoEndSeparator(dir);
		if (normalDir == null || normalDir.isEmpty()) {
			normalDir = PathUtils.normalizeNoEndSeparator(mapToolJarDir + File.separator + dir);
		}
		f = new File(normalDir);
		mapToolDataDir = f.getAbsolutePath();
		if (f.isDirectory()) {
			logMsg(Level.INFO, "MAPTOOL_DATADIR={0}", null, f.getPath());
		} else {
			String msg = CopiedFromOtherJars.getText("msg.error.bad.MAPTOOL_DATADIR", normalDir); //$NON-NLS-1$
			errors.add(msg);
		}

		extraArgs = values.get("ARGS"); //$NON-NLS-1$
		if (extraArgs != null) {
			extraArgs = cleanExtraArgs(extraArgs);
			// This doesn't really work since we're not enforcing a word boundary on either side...
			jcbEnableAssertions.setSelected(extraArgs.contains(ASSERTIONS_OPTION));
		} else {
			extraArgs = EMPTY;
		}

		String[] logFiles;
		final String logging = values.get("LOGGING"); //$NON-NLS-1$
		if (logging != null && !logging.isEmpty()) {
			logFiles = logging.split("\\s*,\\s*"); //$NON-NLS-1$
			Arrays.sort(logFiles);
			for (final LoggingConfig config : logConfigs) {
				final int on = Arrays.binarySearch(logFiles, config.fname.getName());
				config.chkbox.setSelected(on >= 0);
			}
		}
		String jar = values.get("EXECUTABLE"); //$NON-NLS-1$
		if (jar != null) {
			// If specified, make sure it really exists.
			f = new File(mapToolJarDir, jar);
			if (f.isFile()) {
				mapToolJarName = f.getName();
			} else {
				String msg = CopiedFromOtherJars.getText("msg.error.bad.EXECUTABLE", f.toString()); //$NON-NLS-1$
				errors.add(msg);
				jar = null; // reset, so the next chunk can try to find a JAR file
			}
		}
		if (jar == null && mapToolJarDir != null) {
			// If there is no EXECUTABLE field (or it failed the above tests) but there is a
			// MAPTOOL_DIRECTORY field,
			// we want to search the MAPTOOL_DIRECTORY directory looking for a valid
			// matching MapTool jar and auto-select it if it's the only one.  Just trying to
			// save the user a little trouble.  Note that it will be written out later when
			// saving the config file.  Maybe we shouldn't do that?  That's a discussion for
			// the forums, I think.
			String[] names = mapToolJarDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					// Directory doesn't matter.  Just compare the filename.  We ignore case when
					// comparing, but we shouldn't need to.  MapTool is distributed with the JAR
					// filename in all lowercase, but what if the user decides to rename it to make
					// it look good?  (I've done that myself sometimes, in rare cases.)
					String n = name.toLowerCase();
					return n.startsWith("maptool-") && n.endsWith(".jar");
				}
			});
			if (names.length == 1)
				mapToolJarName = new File(mapToolJarDir, names[0]).getName();
		}
		if (mapToolJarName != null) {
			logMsg(Level.INFO, "EXECUTABLE={0}", null, mapToolJarName);
			jbMTJarText = mapToolJarName.replace(".jar", EMPTY); //$NON-NLS-1$
			if (mapToolJarName.toLowerCase().startsWith("maptool-")) { //$NON-NLS-1$
				// We expect the name matches 'maptool-1.3.b89.jar'
				mapToolVersion = " " + mapToolJarName.substring(8, 11); //$NON-NLS-1$
			} else {
				logMsg(Level.WARNING, "Cannot determine MapTool version number from JAR filename: {0}", null, f); //$NON-NLS-1$
				mapToolVersion = EMPTY;
			}
		}
	}

	/**
	 * Reads from a file named mt.cfg in the same directory to get the following
	 * options. Each option is placed on a single line followed by an equal sign
	 * ('=') and then the appropriate value. The default values are coded below.
	 * 
	 * All memory sizes are in megabytes.
	 */
	private boolean readCfgFile() {
		boolean rv = false;

		// Set the defaults in the map.  As lines are read from the config file, overwrite the
		// map entries with the new values.  When we're done, we can look at the map entries
		// in an appropriate order and ensure dependencies are handled correctly as well as
		// convert values to the proper data types.
		final Map<String, String> values = new HashMap<String, String>(10);
		values.put("MAXMEM", Integer.toString(maxMemVal)); //$NON-NLS-1$
		values.put("MINMEM", Integer.toString(minMemVal)); //$NON-NLS-1$
		values.put("STACKSIZE", Integer.toString(stackSizeVal)); //$NON-NLS-1$
		values.put("PROMPT", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		final List<String> errors = new ArrayList<String>();
		if (cfgFile.isFile() && cfgFile.length() > 0) {
			rv = true; // Assume that something was found.

			LineNumberReader lnbr = null;
			try {
				lnbr = new LineNumberReader(new BufferedReader(new FileReader(cfgFile)));
				try {
					String line = lnbr.readLine();
					while (line != null) {
						line = line.trim();
						if (!line.startsWith("#") && !line.isEmpty()) { //$NON-NLS-1$
							final String[] arg = line.split("=", 2); // Only apply first delimiter //$NON-NLS-1$
							if (arg.length == 2) {
								values.put(arg[0].toUpperCase().trim(), arg[1].trim());
							} else {
								errors.add(CopiedFromOtherJars.getText("msg.error.configBadFormat", cfgFile, lnbr.getLineNumber(), line)); //$NON-NLS-1$
							}
						}
						line = lnbr.readLine();
					}
				} catch (final IOException ex) {
					logMsg(Level.SEVERE, "Error reading configuration file: {0}", "msg.error.configIOError", cfgFile); //$NON-NLS-1$ $NON-NLS-2$
				}
			} catch (final FileNotFoundException ex) {
				// This shouldn't happen since we specifically used cfgFile.isFIle(), above, and that can't be true
				// unless the file actually exists.
				logMsg(Level.SEVERE, "Configuration file {0} not found.", "msg.error.configFileNotFound", cfgFile); //$NON-NLS-1$ $NON-NLS-2$
			} finally {
				try {
					lnbr.close();
				} catch (final IOException ex) {
					logMsg(Level.SEVERE, "Error closing configuration file {0}.", "msg.error.configClosing", cfgFile); //$NON-NLS-1$ $NON-NLS-2$
				}
			}
		} else {
			logMsg(Level.INFO, "Configuration file not found; using built-in defaults", "msg.error.configNotFound", cfgFile); //$NON-NLS-1$ $NON-NLS-2$
		}
		// Build a list of all XML files in the same directory as the launcher.  This list of
		// filenames will be used to validate the LOGGING parameter from the configuration file.
		File logging = new File(currentDir, "logging"); //$NON-NLS-1$
		if (!logging.isDirectory()) {
			logging = currentDir;
		}
		logConfigs = buildLoggingFileList(logging);

		// Now process the records just read in (or the defaults).  Errors are accumulated into 'errors'.
		parseCfgValues(values, errors);
		if (!errors.isEmpty()) {
			errors.add(0, CopiedFromOtherJars.getText("msg.info.configFeedback")); //$NON-NLS-1$
			CopiedFromOtherJars.showFeedback(ERROR_MESSAGE, errors.toArray());
		}
		// Keep track of the original values.  When we go to save the configuration file, we
		// only write to it if something has changed.
		originalSettings = values;

		// Update UI fields for these three values.
		assignMinMem();
		assignMaxMem();
		assignStackSize();

		updateStrings();
		return rv;
	}

	/**
	 * Reads all XML files within the given directory and creates a list of
	 * {@link LoggingConfig} objects to represent those files. The list is
	 * returned sorted in order by the description string embedded in the XML
	 * file, or by filename if no description is available.
	 * 
	 * @param fromDir
	 *            <code>File</code> object representing the directory (must be
	 *            non-null)
	 * @return <code>List</code> of {@link LoggingConfig} objects
	 */
	private List<LoggingConfig> buildLoggingFileList(File fromDir) {
		final File[] files = fromDir.listFiles();
		final List<LoggingConfig> configs = new ArrayList<LoggingConfig>(files.length);
		for (final File fn : files) {
			if (fn.getName().toLowerCase().endsWith(".xml")) { //$NON-NLS-1$
				final LoggingConfig config = readDescFromXmlFile(fn);
				configs.add(config);
			}
		}
		Collections.sort(configs);
		return configs;
	}

	/**
	 * Read the given XML file and look for specially formatted lines so that a
	 * description of the files purpose and text to display in a tooltip are
	 * retrieved. The current process involves looking for XML comments that
	 * match the regex <code><b>^&lt;!--\s+(\w+):\s*(.*)\s+--&gt;$</b></code>
	 * and pull out the two fields; the first field is the key and the second is
	 * the value for storing the information in the returned object.
	 * 
	 * @param xml
	 *            <code>File</code> object representing the file to read
	 * @return a {@link LoggingConfig} object which stores information about the
	 *         XML file
	 */
	private LoggingConfig readDescFromXmlFile(File xml) {
		final LoggingConfig config = new LoggingConfig(xml, new JCheckBox());
		LineNumberReader lnbr = null;
		final String REGEX = "^<!--\\s+(\\w+):\\s*(.*)\\s+-->$"; //$NON-NLS-1$
		final Pattern pattern = Pattern.compile(REGEX);
		try {
			int count = 0;
			lnbr = new LineNumberReader(new BufferedReader(new FileReader(xml)));
			String line = lnbr.readLine();
			config.addProperty("desc", xml.getName()); //$NON-NLS-1$
			config.addProperty("ttip", xml.getName()); //$NON-NLS-1$
			while (count < 2 && line != null) {
				line = line.trim();
				final Matcher m = pattern.matcher(line);
				if (m.matches()) {
					final String key = m.group(1);
					final String value = m.group(2);
					config.addProperty(key, value);
					count++;
				}
				line = lnbr.readLine();
			}
			if (count < 2) {
				JOptionPane.showMessageDialog(null, CopiedFromOtherJars.getText("msg.error.loggingBadFormat", xml.getName()), //$NON-NLS-1$
						CopiedFromOtherJars.getText("msg.title.loggingBadFormat", xml), //$NON-NLS-1$
						ERROR_MESSAGE);
			}
		} catch (final FileNotFoundException e) {
		} catch (final IOException e) {
		} finally {
			CopiedFromOtherJars.closeQuietly(lnbr);
		}
		return config;
	}

	/**
	 * Copies all XML files from <code>fileList</code> to <code>toDir</code> if
	 * they are newer than the ones already there or if they are newer than the
	 * configuration file itself. As files are copied the corresponding
	 * <code>JCheckBox</code> item is enabled based on whether the destination
	 * file actually exists. (It's possible the copy could fail because the file
	 * is already there; the checkbox will be enabled in that case since the
	 * destination <i>does</i> exist.)
	 * 
	 * @param fileList
	 *            List of XML files to process
	 * @param toDir
	 *            destination directory (must be non-null)
	 * @return count of the number of unsuccessfully copied files (non-zero is
	 *         bad!)
	 */
	private int copyXmlFiles(List<LoggingConfig> fileList, File toDir) {
		if (!toDir.exists()) {
			toDir.mkdirs(); // throws SecurityManagerException if there's a problem
		}

		final List<String> notCopied = new LinkedList<String>();
		for (final LoggingConfig config : fileList) {
			final File srcFile = config.fname;
			final File destFile = new File(toDir, srcFile.getName());
			final long srcTime = srcFile.lastModified();
			if (!destFile.exists() || srcTime > destFile.lastModified() || srcTime > cfgFile.lastModified()) {
				if (!CopiedFromOtherJars.copyFile(srcFile, destFile)) {
					notCopied.add(CopiedFromOtherJars.getText("msg.info.notCopiedTo", srcFile.getName(), toDir.getPath())); //$NON-NLS-1$
				}
			}
			config.chkbox.setEnabled(destFile.exists());
		}
		if (!notCopied.isEmpty()) {
			notCopied.add(0, CopiedFromOtherJars.getText("msg.info.notCopiedFeedback")); //$NON-NLS-1$
			CopiedFromOtherJars.showFeedback(ERROR_MESSAGE, notCopied.toArray());
			notCopied.remove(0);
		}
		return notCopied.size();
	}

	private String getLoggingString() {
		final StringBuilder tmp = new StringBuilder();
		for (final LoggingConfig config : logConfigs) {
			if (config.chkbox.isSelected()) {
				if (tmp.length() > 0) {
					tmp.append(',');
				}
				tmp.append(config.fname.getName());
			}
		}
		return tmp.toString();
	}

	private void createLogConfig() {
		final File logconf = new File(mapToolDataDir, "logging.xml"); //$NON-NLS-1$
		logconf.delete();

		String loggingNow = getLoggingString();
		String val = originalSettings.get("LOGGING");
		if (val == null)
			val = EMPTY;
		if (val.equalsIgnoreCase(loggingNow))
			return;

		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(logconf));
		} catch (final IOException e) {
			logMsg(Level.SEVERE, "Error creating logging configuration {0}", "msg.error.loggingUnwritable", logconf, e); //$NON-NLS-1$ $NON-NLS-2$
			return;
		}
		try {
			for (final LoggingConfig config : logConfigs) {
				if (config.chkbox.isSelected()) {
					appendFileToWriter(config.fname, bw);
				}
			}
		} finally {
			CopiedFromOtherJars.closeQuietly(bw);
		}
		return;
	}

	private void appendFileToWriter(File from, Writer out) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(from));
			try {
				final char[] buff = new char[1024];
				while (true) {
					int bytes;
					bytes = br.read(buff);
					if (bytes < 0) {
						break;
					}
					out.write(buff, 0, bytes);
				}
			} catch (final IOException e) {
				logMsg(Level.SEVERE, "Error reading logging configuration file {0}", "msg.error.loggingUnreadable", from, e); //$NON-NLS-1$
			} finally {
				CopiedFromOtherJars.closeQuietly(br);
			}
		} catch (final FileNotFoundException e) {
			logMsg(Level.SEVERE, "Error opening logging configuration file {0}", "msg.error.loggingFileNotFound", from, e); //$NON-NLS-1$
		}
		return;
	}

	/**
	 * Examines the passed in string and removes anything that doesn't need to
	 * be there. Examples include "-DMAPTOOL_DATADIR" since there's a separate
	 * option for that.
	 * 
	 * @param extraArgs2
	 *            String to clean
	 * @return replacement string that has been cleaned
	 */
	private String cleanExtraArgs(String args) {
		// Start by looking for any existing (deprecated) option, like "-DMAPTOOL_DATADIR" (since
		// we now have a separate UI option for it).
		final int index = extraArgs.indexOf(DATADIR_OPTION);
		if (index != -1) {
			String test;
			// Found it.  Remove it.  The value ends at the first unescaped whitespace character.
			// (Too bad we can't use StringEscapeUtils from org.apache.commons.lang)
			if (extraArgs.indexOf(" ", index + DATADIR_OPTION.length()) == -1) { //$NON-NLS-1$
				// There are no spaces, escaped or otherwise.  Just use the rest of the string.
				test = extraArgs.substring(index + DATADIR_OPTION.length());
				extraArgs = extraArgs.substring(0, index);
			} else {
				// This whole mess is why we have a separate field for it now!!
				final StringBuffer buff = new StringBuffer(40);
				int idx;
				for (idx = index + DATADIR_OPTION.length(); idx < extraArgs.length(); idx++) {
					char ch = extraArgs.charAt(idx);
					if (ch == '\\') {
						// Treat the next character literally and throw away the backslash...
						// but only if the next character would normally be special (whitespace)
						// because on Windows, the backslash character is also File.separator.
						ch = extraArgs.charAt(++idx);
						if (!Character.isWhitespace(ch)) {
							buff.append('\\');
						}
					} else if (Character.isWhitespace(ch)) {
						break;
					}
					buff.append(ch);
				}
				test = buff.toString();
				extraArgs = extraArgs.substring(0, index) + extraArgs.substring(idx);
			}
			final File f = new File(test);
			if (f.isDirectory()) {
				mapToolDataDir = f.getPath();
			}
		}
		return extraArgs;
	}

	private void saveCfgFile() {
		String baseDir, targetDir, relDir;

		final Map<String, Object> values = new HashMap<String, Object>(15);
		values.put("MAXMEM", maxMemVal); //$NON-NLS-1$
		values.put("MINMEM", minMemVal); //$NON-NLS-1$
		values.put("STACKSIZE", stackSizeVal); //$NON-NLS-1$
		values.put("CONSOLE", startConsole); //$NON-NLS-1$
		values.put("PROMPT", promptUser); //$NON-NLS-1$
		values.put("RELATIVE_PATHS", jcbRelativePath.isSelected()); //$NON-NLS-1$
		if (!mapToolLocale.isEmpty()) {
			values.put("LOCALE", mapToolLocale);
		}

		// Force trailing separators so relative path can be constructed properly
		baseDir = currentDir.getAbsolutePath() + File.separator;
		//		baseDir = "E:\\MapTool\\MapTool V1-3b89\\"; // For testing the relative pathing routines

		relDir = mapToolJarDir.getAbsolutePath();
		if (jcbRelativePath.isSelected()) {
			targetDir = relDir + File.separator;
			//			targetDir = "E:\\MapTool\\MapTool V1-3b89\\";
			try {
				relDir = PathUtils.getRelativePath(targetDir, baseDir);
			} catch (final PathResolutionException e) {
				// If there is no common path, just use the original string.
			}
		}
		values.put("MAPTOOL_DIRECTORY", relDir); //$NON-NLS-1$

		values.put("EXECUTABLE", mapToolJarName); //$NON-NLS-1$

		baseDir = mapToolJarDir.getAbsolutePath() + File.separator;
		if (mapToolDataDir != null && !mapToolDataDir.isEmpty()) {
			relDir = mapToolDataDir;
			if (jcbRelativePath.isSelected()) {
				targetDir = relDir + File.separator;
				try {
					relDir = PathUtils.getRelativePath(targetDir, baseDir);
				} catch (final PathResolutionException e) {
					// If there is no common path, just use the original string.
				}
			}
			values.put("MAPTOOL_DATADIR", relDir); //$NON-NLS-1$
		}
		if (javaDir != null && !javaDir.isEmpty()) {
			relDir = javaDir;
			if (jcbRelativePath.isSelected()) {
				targetDir = relDir + File.separator;
				try {
					relDir = PathUtils.getRelativePath(targetDir, baseDir);
				} catch (final PathResolutionException e) {
					// If there is no common path, just use the original string.
				}
			}
			values.put("JAVA_DIRECTORY", relDir); //$NON-NLS-1$
		}
		if (extraArgs != null && !extraArgs.isEmpty()) {
			values.put("ARGS", extraArgs.trim()); //$NON-NLS-1$
		}
		values.put("LOGGING", getLoggingString()); //$NON-NLS-1$

		// Check to see if anything is different from the configuration we read in earlier.
		// If it's all the same, no reason to write a new one, right?
		int writeNewFile = 0;
		for (final String key : recognizedFields) {
			if (!originalSettings.containsKey(key) || (values.containsKey(key) && originalSettings.get(key).equals(values.get(key)))) {
				writeNewFile++;
			}
		}
		if (writeNewFile == originalSettings.size()) {
			// no need to write the file if all of the settings are identical
			return;
		}
		// Create another map for comments that should precede any particular field.
		final Map<String, String> comments = new HashMap<String, String>();
		comments.put("MAXMEM", CopiedFromOtherJars.getText("msg.info.comment.MAXMEM")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("MINMEM", CopiedFromOtherJars.getText("msg.info.comment.MINMEM")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("STACKSIZE", CopiedFromOtherJars.getText("msg.info.comment.STACKSIZE")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("MAPTOOL_DATADIR", CopiedFromOtherJars.getText("msg.info.comment.MAPTOOL_DATADIR")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("MAPTOOL_DIRECTORY", CopiedFromOtherJars.getText("msg.info.comment.MAPTOOL_DIRECTORY")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("EXECUTABLE", CopiedFromOtherJars.getText("msg.info.comment.EXECUTABLE")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("LOCALE", CopiedFromOtherJars.getText("msg.info.comment.LOCALE")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("LOGGING", CopiedFromOtherJars.getText("msg.info.comment.LOGGING")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("JAVA_DIRECTORY", CopiedFromOtherJars.getText("msg.info.comment.JAVA_DIRECTORY")); //$NON-NLS-1$ //$NON-NLS-2$
		comments.put("ARGS", CopiedFromOtherJars.getText("msg.info.comment.ARGS")); //$NON-NLS-1$ //$NON-NLS-2$

		BufferedWriter br = null;
		Exception ex = null;
		try {
			br = new BufferedWriter(new FileWriter(cfgFile));
			br.write(CopiedFromOtherJars.getText("msg.info.comment.01")); //$NON-NLS-1$
			br.write(CopiedFromOtherJars.getText("msg.info.comment.02")); //$NON-NLS-1$
			br.write(CopiedFromOtherJars.getText("msg.info.comment.03")); //$NON-NLS-1$
			br.write(CopiedFromOtherJars.getText("msg.info.comment.04")); //$NON-NLS-1$
			br.write(CopiedFromOtherJars.getText("msg.info.comment.05")); //$NON-NLS-1$
			br.write("#\n"); //$NON-NLS-1$
			for (String key : recognizedFields) {
				if (comments.containsKey(key)) {
					br.write("# " + comments.get(key).toString()); //$NON-NLS-1$
					br.newLine();
				}
				Object v = values.get(key);
				if (v == null) {
					// No values was assigned, so leave a placeholder in the config file.
					br.write("#" + key + "="); //$NON-NLS-1$ $NON-NLS-2$
				} else {
					br.write(key + "=" + v.toString()); //$NON-NLS-1$
					values.remove(key);
				}
				br.newLine();
			}
		} catch (final IOException e) {
			ex = e;
		} finally {
			try {
				br.close();
			} catch (final IOException e) {
				ex = e;
			}
			if (ex != null) {
				logMsg(Level.SEVERE, "Configuration file {0} is unwritable", "msg.error.configUnwritable", cfgFile, ex);
			}
		}
		if (!values.isEmpty())
			logMsg(Level.SEVERE, "Programming error: values.isEmpty() is not true!", null);
	}

	private void jbLaunchActionPerformed(ActionEvent evt) throws IOException {
		checkStackSize();
		checkMaxMem();
		checkMinMem();
		updateCommand(); // FIXME Shouldn't need this, right?

		final File dataDirPath = new File(mapToolDataDir);
		if (!dataDirPath.isDirectory()) {
			if (!dataDirPath.mkdirs()) {
				// This isn't a *huge* problem because MapTool will create the directory itself.
				// There just won't be any of the XML logging files copied into it by this launcher.
				logMsg(Level.SEVERE, "Failed to create MAPTOOL_DATADIR: " + dataDirPath, null);
			}
		}
		if (dataDirPath.isDirectory()) {
			createLogConfig();
		}
		saveCfgFile(); // Probably shouldn't do this unless something changed but we're not designed for that. :(
		launchMapTool();

		if (!jcbKeepOpen.isSelected()) {
			System.exit(0);
		}
	}

	private void jtfStackSizeActionPerformed(ActionEvent evt) {
		checkStackSize();
		updateCommand();
	}

	private void jtfStackSizeFocusLost(FocusEvent evt) {
		checkStackSize();
		updateCommand();
	}

	private void jtfMaxMemActionPerformed(ActionEvent evt) {
		checkMaxMem();
		updateCommand();
	}

	private void jtfMaxMemFocusLost(FocusEvent evt) {
		checkMaxMem();
		updateCommand();
	}

	private void jtfMinMemActionPerformed(ActionEvent evt) {
		checkMinMem();
		updateCommand();
	}

	private void jtfMinMemFocusLost(FocusEvent evt) {
		checkMinMem();
		updateCommand();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		log.setLevel(Level.WARNING);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new MapToolLauncher().setVisible(true);
				} catch (final IOException e) {
					logMsg(Level.SEVERE, "IOException starting MapToolLauncher", "msg.error.startingMapTool", e); //$NON-NLS-1$ $NON-NLS-2$
				} catch (final URISyntaxException e) {
					logMsg(Level.SEVERE, "URISyntaxException starting MapToolLauncher", "msg.error.startingMapTool", e); //$NON-NLS-1$ $NON-NLS-2$
				}
			}
		});
	}

	private class InputValidator extends KeyAdapter {
		public InputValidator() {
		}

		@Override
		public void keyTyped(KeyEvent e) {
			final char check = e.getKeyChar();
			if (!Character.isDigit(check) && !(check == KeyEvent.VK_BACK_SPACE) && !(check == KeyEvent.VK_DELETE)) {
				e.consume();
			}
		}
	}
}
