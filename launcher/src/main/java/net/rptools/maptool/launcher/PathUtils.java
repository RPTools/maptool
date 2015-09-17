/**
 * 
 */
package net.rptools.maptool.launcher;

import java.io.File;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * It seems that converting an absolute pathname into a relative pathname (based
 * on some second absolute pathname) is a not-so-simple problem. This class is
 * adapted from <a href=
 * "http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls"
 * >a question on StackOverflow.com</a> where an answer is given that relies on
 * org.apache.commons.io.FilenameUtils. Since we don't want to use external JARs
 * if we don't have to, he dependencies have been re-implemented in this class
 * as public methods (may as well let them be used elsewhere).
 * 
 * @author frank
 */
public class PathUtils {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PathUtils.class.getName());

	public static final char UNIX_SEPARATOR = '/';
	public static final char WINDOWS_SEPARATOR = '\\';
	public static final char SYSTEM_SEPARATOR = File.separatorChar;
	public static final char OTHER_SEPARATOR;

	static {
		if (System.getProperty("os.name").contains("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
			OTHER_SEPARATOR = UNIX_SEPARATOR;
		} else {
			OTHER_SEPARATOR = WINDOWS_SEPARATOR;
		}
	}

	/**
	 * See {@link #getRelativePath(String, String, String)} for details.
	 * 
	 * @param targetPath
	 *            targetPath is calculated to this file
	 * @param basePath
	 *            basePath is calculated from this file
	 * @return resulting relative path string
	 */
	public static String getRelativePath(String targetPath, String basePath) {
		return getRelativePath(targetPath, basePath, File.separator);
	}

	/**
	 * Get the relative path from one file to another, specifying the directory
	 * separator. If one of the provided resources does not exist, it is assumed
	 * to be a file unless it ends with '/' or '\'.
	 * 
	 * @param targetPath
	 *            targetPath is calculated to this file
	 * @param basePath
	 *            basePath is calculated from this file
	 * @param pathSeparator
	 *            directory separator. The platform default is not assumed so
	 *            that we can test Unix behavior when running on Windows (for
	 *            example)
	 * @return resulting relative path string
	 */
	public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {
		// Normalize the paths
		String normalizedTargetPath = normalizeNoEndSeparator(targetPath);
		String normalizedBasePath = normalizeNoEndSeparator(basePath);
		char pathSeparatorCh;

		// Undo the changes to the separators made by normalization
		if (pathSeparator.equals("/")) { //$NON-NLS-1$
			pathSeparatorCh = '/';
			normalizedTargetPath = separatorsToUnix(normalizedTargetPath);
			normalizedBasePath = separatorsToUnix(normalizedBasePath);
		} else if (pathSeparator.equals("\\")) { //$NON-NLS-1$
			pathSeparatorCh = '\\';
			normalizedTargetPath = separatorsToWindows(normalizedTargetPath);
			normalizedBasePath = separatorsToWindows(normalizedBasePath);
		} else {
			throw new IllegalArgumentException(CopiedFromOtherJars.getText("msg.error.unrecognizedDirSeparator", pathSeparator)); //$NON-NLS-1$ 
		}
		final String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
		final String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		final StringBuilder common = new StringBuilder();

		int commonIndex = 0;
		while (commonIndex < target.length && commonIndex < base.length && target[commonIndex].equals(base[commonIndex])) {
			common.append(target[commonIndex] + pathSeparator);
			commonIndex++;
		}
		if (commonIndex == 0) {
			// No single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized.
			final String msg = CopiedFromOtherJars.getText("msg.error.noCommonPath", normalizedTargetPath, normalizedBasePath); //$NON-NLS-1$
			throw new PathResolutionException(msg);
		}

		// The number of directories we have to backtrack depends on whether the base is a file or a dir
		// For example, the relative path from
		//
		// /foo/bar/baz/gg/ff to /foo/bar/baz
		// 
		// ".." if ff is a file
		// "../.." if ff is a directory
		//
		// The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
		// the resource referred to by this path may not actually exist, but it's the best I can do
		boolean baseIsFile = true;

		final File baseResource = new File(normalizedBasePath);

		if (basePath.endsWith(pathSeparator)) {
			baseIsFile = false;
		} else if (baseResource.exists()) {
			baseIsFile = baseResource.isFile();
		}
		final StringBuilder relative = new StringBuilder();

		if (base.length != commonIndex) {
			final int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;
			for (int i = 0; i < numDirsUp; i++) {
				relative.append(".." + pathSeparator); //$NON-NLS-1$
			}
		}
		// 'common' has the slash on the end, 'normalizedTargetPath' does not.
		int len = common.length();
		if (len <= normalizedTargetPath.length()) {
			if (normalizedTargetPath.charAt(len) == pathSeparatorCh)
				len++;
			relative.append(normalizedTargetPath.substring(len));
		} else if (relative.length() < 1) {
			relative.append(".");
		} else {
			relative.deleteCharAt(relative.length() - 1);
		}
		return relative.toString();
	}

	static class PathResolutionException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		PathResolutionException(String msg) {
			super(msg);
		}
	}

	public static String normalizeNoEndSeparator(String filename) {
		return normalizeNoEndSeparator(filename, UNIX_SEPARATOR == SYSTEM_SEPARATOR);
	}

	public static String normalizeNoEndSeparator(String filename, boolean unixSeparator) {
		final char separator = unixSeparator ? UNIX_SEPARATOR : WINDOWS_SEPARATOR;
		return doNormalize(filename, separator, false);
	}

	/**
	 * Internal method to perform the normalization.
	 * 
	 * @param filename
	 *            the filename
	 * @param separator
	 *            The separator character to use
	 * @param keepSeparator
	 *            true to keep the final separator
	 * @return the normalized filename
	 */
	public static String doNormalize(String filename, char separator, boolean keepSeparator) {
		if (filename == null) {
			return null;
		}
		int size = filename.length();
		if (size == 0) {
			return filename;
		}
		final int prefix = getPrefixLength(filename);
		if (prefix < 0) {
			return null;
		}
		final char[] array = new char[size + 2]; // +1 for possible extra slash, +2 for array copy
		filename.getChars(0, filename.length(), array, 0);

		// fix separators throughout
		final char otherSeparator = separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == otherSeparator) {
				array[i] = separator;
			}
		}
		// add extra separator on the end to simplify code below
		boolean lastIsDirectory = true;
		if (array[size - 1] != separator) {
			array[size++] = separator;
			lastIsDirectory = false;
		}
		// adjoining slashes
		for (int i = prefix + 1; i < size; i++) {
			if (array[i] == separator && array[i - 1] == separator) {
				System.arraycopy(array, i, array, i - 1, size - i);
				size--;
				i--;
			}
		}
		// dot slash
		for (int i = prefix + 1; i < size; i++) {
			if (array[i] == separator && array[i - 1] == '.' && (i == prefix + 1 || array[i - 2] == separator)) {
				if (i == size - 1) {
					lastIsDirectory = true;
				}
				System.arraycopy(array, i + 1, array, i - 1, size - i);
				size -= 2;
				i--;
			}
		}
		// double dot slash
		outer:
		for (int i = prefix + 2; i < size; i++) {
			if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' && (i == prefix + 2 || array[i - 3] == separator)) {
				if (i == prefix + 2) {
					return null;
				}
				if (i == size - 1) {
					lastIsDirectory = true;
				}
				int j;
				for (j = i - 4; j >= prefix; j--) {
					if (array[j] == separator) {
						// remove b/../ from a/b/../c
						System.arraycopy(array, i + 1, array, j + 1, size - i);
						size -= i - j;
						i = j + 1;
						continue outer;
					}
				}
				// remove a/../ from a/../c
				System.arraycopy(array, i + 1, array, prefix, size - i);
				size -= i + 1 - prefix;
				i = prefix + 1;
			}
		}
		if (size <= 0) { // should never be less than 0
			return ""; //$NON-NLS-1$
		}
		if (size <= prefix) { // should never be less than prefix
			return new String(array, 0, size);
		}
		if (lastIsDirectory && keepSeparator) {
			return new String(array, 0, size); // keep trailing separator
		}
		return new String(array, 0, size - 1); // lose trailing separator
	}

	/**
	 * Returns the length of the filename prefix, such as <code>C:/</code> or
	 * <code>~/</code>.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * <p>
	 * The prefix length includes the first slash in the full filename if
	 * applicable. Thus, it is possible that the length returned is greater than
	 * the length of the input string.
	 * 
	 * <pre>
	 * Windows:
	 * a\b\c.txt           --> ""          --> relative
	 * \a\b\c.txt          --> "\"         --> current drive absolute
	 * C:a\b\c.txt         --> "C:"        --> drive relative
	 * C:\a\b\c.txt        --> "C:\"       --> absolute
	 * \\server\a\b\c.txt  --> "\\server\" --> UNC
	 * 
	 * Unix:
	 * a/b/c.txt           --> ""          --> relative
	 * /a/b/c.txt          --> "/"         --> absolute
	 * ~/a/b/c.txt         --> "~/"        --> current user
	 * ~                   --> "~/"        --> current user (slash added)
	 * ~user/a/b/c.txt     --> "~user/"    --> named user
	 * ~user               --> "~user/"    --> named user (slash added)
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on. ie. both Unix and Windows prefixes are matched regardless.
	 * 
	 * <b>Note:</b> Fails on Unix if first character of pathname is a colon.
	 * 
	 * @param filename
	 *            the filename to find the prefix in, null returns -1
	 * @return the length of the prefix, -1 if invalid or null
	 */
	public static int getPrefixLength(String filename) {
		if (filename == null) {
			return -1;
		}
		final int len = filename.length();
		if (len == 0) {
			return 0;
		}
		char ch0 = filename.charAt(0);
		if (ch0 == ':') {
			return -1;
		}
		if (len == 1) {
			if (ch0 == '~') {
				return 2; // return a length greater than the input
			}
			return isSeparator(ch0) ? 1 : 0;
		} else {
			if (ch0 == '~') {
				int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
				int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
				if (posUnix == -1 && posWin == -1) {
					return len + 1; // return a length greater than the input
				}
				posUnix = posUnix == -1 ? posWin : posUnix;
				posWin = posWin == -1 ? posUnix : posWin;
				return Math.min(posUnix, posWin) + 1;
			}
			final char ch1 = filename.charAt(1);
			if (ch1 == ':') {
				ch0 = Character.toUpperCase(ch0);
				if (ch0 >= 'A' && ch0 <= 'Z') {
					if (len == 2 || isSeparator(filename.charAt(2)) == false) {
						return 2;
					}
					return 3;
				}
				return -1;
			} else if (isSeparator(ch0) && isSeparator(ch1)) {
				int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
				int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
				if (posUnix == -1 && posWin == -1 || posUnix == 2 || posWin == 2) {
					return -1;
				}
				posUnix = posUnix == -1 ? posWin : posUnix;
				posWin = posWin == -1 ? posUnix : posWin;
				return Math.min(posUnix, posWin) + 1;
			} else {
				return isSeparator(ch0) ? 1 : 0;
			}
		}
	}

	/**
	 * Checks if the character is a separator.
	 * 
	 * @param ch
	 *            the character to check
	 * @return true if it is a separator character
	 */
	private static boolean isSeparator(char ch) {
		return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
	}

	/**
	 * Converts all separators to the Unix separator of forward slash.
	 * 
	 * @param path
	 *            the path to be changed, null ignored
	 * @return the updated path
	 */
	public static String separatorsToUnix(String path) {
		if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
			return path;
		}
		return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
	}

	/**
	 * Converts all separators to the Windows separator of backslash.
	 * 
	 * @param path
	 *            the path to be changed, null ignored
	 * @return the updated path
	 */
	public static String separatorsToWindows(String path) {
		if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
			return path;
		}
		return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
	}
}
