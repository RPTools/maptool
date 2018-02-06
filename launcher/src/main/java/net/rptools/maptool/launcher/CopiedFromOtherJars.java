/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.launcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class CopiedFromOtherJars {
	private static final String EMPTY = "";

	private static String bundlePackage = "net.rptools.maptool.launcher.language.i18n";
	private static String languageProperties = "net/rptools/maptool/launcher/language/languages.properties";
	private static final ResourceBundle VERSION = ResourceBundle.getBundle("net.rptools.maptool.launcher.build");
	private static ResourceBundle BUNDLE = null;

	/**
	 * This is used when parsing integers in order to keep the input style
	 * local-aware.
	 */
	private static NumberFormat nf = NumberFormat.getNumberInstance();

	private static ResourceBundle getBundle() {
		if (BUNDLE == null) {
			BUNDLE = ResourceBundle.getBundle(bundlePackage);
		}
		return BUNDLE;
	}

	/**
	 * Generates a list of all translations available within the package in the
	 * same storage file as this class. The return value is a Map of
	 * &lt;key,value&gt; pairs. The key is the locale code as determined by
	 * stripping the prefix and suffix from the property filename. The value is
	 * the name of the language as read from the file itself, looking for a line
	 * that literally starts with <b><code># LANGUAGE=</code></b> and then using
	 * the remaining text on that line.
	 * 
	 * Created this method to support property files stored in the jar vs
	 * external source.
	 * 
	 * @author Jamz
	 * @since 1.4.0.1
	 * 
	 * @return a Map of &lt;key,value&gt; pairs
	 */
	public static Map<String, String> getListOfLanguages() {

		Map<String, String> languages = new HashMap<String, String>();

		try {
			Configuration configuration = new PropertiesConfiguration(languageProperties);
			Iterator<String> keys = configuration.getKeys();
			while (keys.hasNext()) {
				String key = keys.next();
				languages.put(key, configuration.getString(key));
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return languages;
	}

	public static boolean setLanguage(String name) {
		boolean result = false;
		Locale loc = new Locale(name);
		BUNDLE = ResourceBundle.getBundle(bundlePackage);
		result = true;
		ResourceBundle.clearCache();
		Locale.setDefault(loc);
		return result;
	}

	/**
	 * Positions the passed in parameter so that it's centered on the screen.
	 * 
	 * @param window
	 */
	public static void centerOnScreen(Component window) {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension windowSize = window.getSize();

		final int x = (screenSize.width - windowSize.width) / 2;
		final int y = (screenSize.height - windowSize.height) / 2;

		window.setLocation(x, y);
	}

	/**
	 * Returns <code>text</code> converted to an integer value, or the value of
	 * <code>def</code> if the string cannot be converted. This method is
	 * locale-aware (which doesn't mean much for integers).
	 * 
	 * @param text
	 *            string to convert to a number
	 * @param def
	 *            default value to use if a ParseException is thrown
	 * @return the result
	 */
	public static Integer parseInteger(String text, Integer def) {
		if (text == null) {
			return def;
		}
		try {
			return parseInteger(text);
		} catch (final ParseException e) {
			return def;
		}
	}

	/**
	 * Returns <code>text</code> converted to an integer value or throws an
	 * exception. This method is locale-aware (which doesn't mean much for
	 * integers).
	 * 
	 * @param text
	 *            string to convert to a number
	 * @param def
	 *            default value to use if a ParseException is thrown
	 * @return the result
	 */
	public static Integer parseInteger(String text) throws ParseException {
		int def = 0;
		if (text == null) {
			return def;
		}
		def = nf.parse(text).intValue();
		return def;
	}

	/**
	 * Returns <code>text</code> converted to a Boolean value, or the value of
	 * <code>def</code> if the string cannot be converted. This method returns
	 * <code>Boolean.TRUE</code> if the string provided is not <code>null</code>
	 * and is "true" using a case-insensitive comparison, or if it is parseable
	 * as an integer and represents a non-zero value.
	 * 
	 * @param text
	 *            string to convert to a Boolean
	 * @param def
	 *            default value to use if a ParseException is thrown
	 * @return the result
	 */
	public static Boolean parseBoolean(String text, Boolean def) {
		if (text == null) {
			return def;
		}
		try {
			return parseBoolean(text);
		} catch (final ParseException e) {
			return def;
		}
	}

	/**
	 * Returns <code>text</code> converted to a Boolean value or throws an
	 * exception. This method returns <code>Boolean.TRUE</code> if the string
	 * provided is not <code>null</code> and is "true" using a case-insensitive
	 * comparison or represents a non-zero value as an integer.
	 * 
	 * @param text
	 *            string to convert to a Boolean
	 * @return the result
	 */
	public static Boolean parseBoolean(String text) throws ParseException {
		Boolean def = Boolean.FALSE;
		if (text != null) {
			text = text.toLowerCase();
			if (text.equals("true")) {
				return Boolean.TRUE;
			} else if (text.equals("false")) {
				return Boolean.FALSE;
			}
			def = parseInteger(text) != 0;
		}
		return def;
	}

	/**
	 * Displays the messages provided as <code>messages</code> by creating a
	 * message dialog and populating it with each entry in the array converted
	 * to a string.
	 * 
	 * @param msgType
	 *            the message type passed to the JOptionPane. common values are
	 *            <code>JOptionPane.ERROR_MESSAGE</code> and
	 *            <code>JOptionPane.WARNING_MESSAGE</code>
	 * @param messages
	 *            the Objects (normally strings) to put in the body of the
	 *            dialog; no properties file lookup is performed!
	 */
	public static void showFeedback(int msgType, Object[] messages) {
		final String title = getText("msg.title.messageDialogFeedback");
		for (final Object msg : messages) {
			MapToolLauncher.log.log(Level.WARNING, msg.toString());
		}
		final JList<?> list = new JList<Object>(messages);
		JOptionPane.showMessageDialog(null, list, title, msgType);
	}

	/**
	 * Returns the String that results from a lookup within the properties file.
	 * 
	 * @param key
	 *            the component to search for
	 * @return the String found or <code>null</code>
	 */
	public static String getVersion() {
		try {
			// Jamz: Do we really need the buildDate in the title?
			//String buildDate = VERSION.getString("app.buildDate");
			String buildNumber = VERSION.getString("app.buildNumber");
			//return buildDate + "." + buildNumber;
			return buildNumber;
		} catch (final MissingResourceException e) {
			return null;
		}
	}

	/**
	 * Returns the String that results from a lookup within the properties file.
	 * 
	 * @param key
	 *            the component to search for
	 * @return the String found or <code>null</code>
	 */
	public static String getString(String key) {
		try {
			return getBundle().getString(key);
		} catch (final MissingResourceException e) {
			return null;
		}
	}

	/**
	 * Returns the text associated with the given key after removing any menu
	 * mnemonic. So for the key <b>action.loadMap</b> that has the value
	 * "&Load Map" in the properties file, this method returns "Load Map".
	 * 
	 * @param key
	 *            the component to search for
	 * @return the String found with mnemonics removed, or the input key if not
	 *         found
	 */
	public static String getText(String key) {
		final String value = getString(key);
		if (value == null) {
			final String msg = MessageFormat.format("Cannot find key ''{0}'' in properties file.", key);
			MapToolLauncher.log.log(Level.INFO, msg);
			return key;
		}
		return value.replaceAll("\\&", EMPTY);
	}

	/**
	 * Functionally identical to {@link #getText(String key)} except that this
	 * one bundles the formatting calls into this code. This version of the
	 * method is truly only needed when the string being retrieved contains
	 * parameters, but using it when no parameters are provided or are
	 * <code>null</code> is still valid and will work, as long as the retrieved
	 * message doesn't require parameters. In MapTool, this commonly means the
	 * player's name or a filename. See the "Parameterized Strings" section of
	 * the <b>i18n.properties</b> file for example usage. Full documentation for
	 * this technique can be found under {@link MessageFormat#format}.
	 * 
	 * @param key
	 *            the <code>propertyKey</code> to use for lookup in the
	 *            properties file
	 * @param args
	 *            parameters needed for formatting purposes
	 * @return the formatted String
	 */
	public static String getText(String key, Object... args) {
		// If the key doesn't exist in the file, the key becomes the format and
		// nothing will be substituted; it's a little extra work, but is not the normal case
		// anyway.
		final String msg = MessageFormat.format(getText(key), args);
		return msg;
	}

	/**
	 * Closes a <code>Reader</code> while ignoring any thrown exceptions.
	 * 
	 * @param f
	 *            <code>Reader</code> to close
	 */
	public static void closeQuietly(Reader f) {
		if (f != null) {
			try {
				f.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * Closes a <code>Writer</code> while ignoring any thrown exceptions.
	 * 
	 * @param f
	 *            <code>Writer</code> to close
	 */
	public static void closeQuietly(Writer f) {
		if (f != null) {
			try {
				f.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * Copies <code>from</code> file to <code>to</code> file returning success
	 * or failure.
	 * 
	 * @param from
	 *            <code>File</code> representing object to copy
	 * @param to
	 *            <code>File</code> representing destination filename for copied
	 *            object (must not be directory)
	 * @return true or false whether the copy succeeded
	 */
	public static boolean copyFile(final File from, final File to) {
		to.delete();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(from));
		} catch (final FileNotFoundException e) {
			return false;
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(to));
		} catch (final IOException e) {
			closeQuietly(in);
			return false;
		}
		try {
			final char[] buff = new char[1024];
			while (true) {
				final int bytes = in.read(buff);
				if (bytes < 0) {
					break;
				}
				out.write(buff, 0, bytes);
			}
		} catch (final IOException e) {
			return false;
		} finally {
			closeQuietly(out);
			closeQuietly(in);
		}
		return true;
	}

	public static ImageIcon resizeImage(ImageIcon imageIcon, int w, int h) {
		return new ImageIcon(imageIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
	}
}