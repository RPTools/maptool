package net.rptools.maptool_fx.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineOptionHelper {
	/**
	 * Search for command line arguments for options. Expecting arguments specified as -parameter=value pair and returns a string.
	 *
	 * Examples: -version=1.4.0.1 -user=Jamz
	 *
	 * @author Jamz
	 * @since 1.4.0.1
	 *
	 * @param options
	 *            {@link org.apache.commons.cli.Options}
	 * @param searchValue
	 *            Option string to search for, ie -version
	 * @param defaultValue
	 *            A default value to return if option is not found
	 * @param args
	 *            String array of passed in args
	 * @return Option value found as a String, or defaultValue if not found
	 */
	public static String getCommandLineOption(Options options, String searchValue, String defaultValue,
			String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(searchValue)) {
				return cmd.getOptionValue(searchValue);
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return defaultValue;
	}

	/**
	 * Search for command line arguments for options. Expecting arguments formatted as a switch
	 *
	 * Examples: -x or -fullscreen
	 *
	 * @author Jamz
	 * @since 1.4.0.1
	 *
	 * @param options
	 *            {@link org.apache.commons.cli.Options}
	 * @param searchValue
	 *            Option string to search for, ie -version
	 * @param args
	 *            String array of passed in args
	 * @return A boolean value of true if option parameter found
	 */
	public static boolean getCommandLineOption(Options options, String searchValue, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption(searchValue)) {
				return true;
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}

	/**
	 * Search for command line arguments for options. Expecting arguments specified as -parameter=value pair and returns a string.
	 *
	 * Examples: -monitor=1 -x=0 -y=0 -w=1200 -h=960
	 *
	 * @author Jamz
	 * @since 1.4.0.1
	 *
	 * @param options
	 *            {@link org.apache.commons.cli.Options}
	 * @param searchValue
	 *            Option string to search for, ie -version
	 * @param defaultValue
	 *            A default value to return if option is not found
	 * @param args
	 *            String array of passed in args
	 * @return Int value of the matching option parameter if found
	 */
	public static int getCommandLineOption(Options options, String searchValue, int defaultValue, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption(searchValue)) {
				return Integer.parseInt(cmd.getOptionValue(searchValue));
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return -1;
	}
}
