/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class FileUtil {
	private static final Logger log = Logger.getLogger(FileUtil.class);

	/**
	 * Can't use this for String objects yet as it's Java 6+ and we're trying to be Java 5 compatible. But soon...
	 */
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * Reads the entire content of the given file into a byte array.
	 * 
	 * @deprecated use {@link FileUtils#readFileToByteArray(File)} instead.
	 * @param file
	 * @return byte contents of the file
	 * @throws IOException
	 */
	@Deprecated
	public static byte[] loadFile(File file) throws IOException {
		return FileUtils.readFileToByteArray(file);
	}

	/**
	 * Reads the entire content of the given file into a byte array.
	 * 
	 * @deprecated use {@link FileUtils#readFileToByteArray(File)} instead.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static byte[] getBytes(File file) throws IOException {
		return FileUtils.readFileToByteArray(file);
	}

	public static Object objFromResource(String res) throws IOException {
		XStream xs = new XStream();
		InputStream is = null;
		try {
			is = FileUtil.class.getClassLoader().getResourceAsStream(res);
			return xs.fromXML(new InputStreamReader(is, "UTF-8"));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static byte[] loadResource(String resource) throws IOException {
		InputStream is = null;
		try {
			is = FileUtil.class.getClassLoader().getResourceAsStream(resource);
			if (is == null) {
				throw new IOException("Resource \"" + resource + "\" cannot be opened as stream.");
			}
			return IOUtils.toByteArray(new InputStreamReader(is, "UTF-8"));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getLines(File file) throws IOException {
		List<String> list;
		FileReader fr = new FileReader(file);
		try {
			list = IOUtils.readLines(fr);
		} finally {
			fr.close();
		}
		return list;
	}

	public static void saveResource(String resource, File destDir) throws IOException {
		int index = resource.lastIndexOf('/');
		String filename = index >= 0 ? resource.substring(index + 1) : resource;
		saveResource(resource, destDir, filename);
	}

	public static void saveResource(String resource, File destDir, String filename) throws IOException {
		File outFilename = new File(destDir, filename);
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			inStream = FileUtil.class.getClassLoader().getResourceAsStream(resource);
			outStream = new BufferedOutputStream(new FileOutputStream(outFilename));
			IOUtils.copy(inStream, outStream);
		} finally {
			IOUtils.closeQuietly(inStream);
			IOUtils.closeQuietly(outStream);
		}
	}

	private static final Pattern TRIM_EXTENSION_PATTERN = Pattern.compile("^(.*)\\.([^\\.]*)$");

	public static String getNameWithoutExtension(File file) {
		return getNameWithoutExtension(file.getName());
	}

	public static String getNameWithoutExtension(String filename) {
		if (filename == null) {
			return null;
		}
		Matcher matcher = TRIM_EXTENSION_PATTERN.matcher(filename);
		if (!matcher.matches()) {
			return filename;
		}
		return matcher.group(1);
	}

	public static String getNameWithoutExtension(URL url) {
		String file = url.getFile();
		try {
			file = url.toURI().getPath();
			//			int beginning = file.lastIndexOf(File.separatorChar); // Don't need to strip the path since the File() constructor will take care of that
			//			file = file.substring(beginning < 0 ? 0 : beginning + 1);
		} catch (URISyntaxException e) {
			// If the conversion doesn't work, ignore it and use the original file name.
		}
		return getNameWithoutExtension(new File(file));
	}

	public static byte[] getBytes(URL url) throws IOException {
		InputStream is = null;
		try {
			is = url.openStream();
			return IOUtils.toByteArray(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Returns the data in a file using the UTF-8 character encoding. The platform default may not be appropriate since
	 * the file could've been produced on a different platform. The only safe thing to do is use UTF-8 and hope that
	 * everyone uses it by default when they edit text files. :-/
	 * 
	 * @deprecated This is not in use, and {@link IOUtils#toCharArray(InputStream, String)} should be used directly
	 *             anyways
	 */
	@Deprecated
	public static String getString(InputStream is) throws IOException {
		if (is == null)
			throw new IllegalArgumentException("InputStream cannot be null");
		return IOUtils.toString(is, "UTF-8");
	}

	/**
	 * Reads the given file as UTF-8 bytes and returns the contents as a standard Java string.
	 * 
	 * @deprecated This is not in use, and {@link FileUtils#readFileToString(File, String)} should be used directly
	 *             anyways
	 * @param file
	 *            file to retrieve contents from
	 * @return String representing the contents
	 * @throws IOException
	 */
	@Deprecated
	public static String getString(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}
		return FileUtils.readFileToString(file, "UTF-8");
	}

	/**
	 * Given an InputStream this method tries to figure out what the content type might be.
	 * 
	 * @param in
	 *            the InputStream to check
	 * @return a <code>String</code> representing the content type name
	 */
	public static String getContentType(InputStream in) {
		String type = "";
		try {
			type = URLConnection.guessContentTypeFromStream(in);
			if (log.isDebugEnabled())
				log.debug("result from guessContentTypeFromStream() is " + type);
		} catch (IOException e) {
		}
		return type;
	}

	/**
	 * Given a URL this method tries to figure out what the content type might be based only on the filename extension.
	 * 
	 * @param url
	 *            the URL to check
	 * @return a <code>String</code> representing the content type name
	 */
	public static String getContentType(URL url) {
		String type = "";
		type = URLConnection.guessContentTypeFromName(url.getPath());
		if (log.isDebugEnabled())
			log.debug("result from guessContentTypeFromName(" + url.getPath() + ") is " + type);
		return type;
	}

	/**
	 * Given a <code>File</code> this method tries to figure out what the content type might be based only on the
	 * filename extension.
	 * 
	 * @param file
	 *            the File to check
	 * @return a <code>String</code> representing the content type name
	 */
	public static String getContentType(File file) {
		try {
			return getContentType(file.toURI().toURL());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
		 * Returns a {@link BufferedReader from the given <code>File</code> object.  The contents
		 * of the file are expected to be UTF-8.
		 * 
		 * @param file the input data source
		 * @return a String representing the data
		 * @throws IOException
		 */
	public static BufferedReader getFileAsReader(File file) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
	}

	/**
	 * Given a URL this method determines the content type of the URL (if possible) and then returns a Reader with the
	 * appropriate character encoding.
	 * 
	 * @param url
	 *            the source of the data stream
	 * @return String representing the data
	 * @throws IOException
	 */
	public static Reader getURLAsReader(URL url) throws IOException {
		InputStreamReader isr = null;
		URLConnection conn = null;
		// We're assuming character here, but it could be bytes.  Perhaps we should
		// check the MIME type returned by the network server?
		conn = url.openConnection();
		if (log.isDebugEnabled()) {
			String type = URLConnection.guessContentTypeFromName(url.getPath());
			log.debug("result from guessContentTypeFromName(" + url.getPath() + ") is " + type);
			type = getContentType(conn.getInputStream());
			// Now make a guess and change 'encoding' to match the content type...
		}
		isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
		return isr;
	}

	public static InputStream getFileAsInputStream(File file) throws IOException {
		return getURLAsInputStream(file.toURI().toURL());
	}

	/**
	 * Given a URL this method determines the content type of the URL (if possible) and then returns an InputStream.
	 * 
	 * @param url
	 *            the source of the data stream
	 * @return InputStream representing the data
	 * @throws IOException
	 */
	public static InputStream getURLAsInputStream(URL url) throws IOException {
		InputStream is = null;
		URLConnection conn = null;
		// We're assuming character here, but it could be bytes.  Perhaps we should
		// check the MIME type returned by the network server?
		conn = url.openConnection();
		if (log.isDebugEnabled()) {
			String type = URLConnection.guessContentTypeFromName(url.getPath());
			log.debug("result from guessContentTypeFromName(" + url.getPath() + ") is " + type);
			type = getContentType(conn.getInputStream());
			log.debug("result from getContentType(" + url.getPath() + ") is " + type);
		}
		is = conn.getInputStream();
		return is;
	}

	/**
	 * Writes given bytes to file indicated by <code>file</code>. This method will overwrite any existing file at that
	 * location, and will create any sub-directories required.
	 * 
	 * @deprecated use {@link FileUtils#writeByteArrayToFile(File, byte[])} instead.
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	@Deprecated
	public static void writeBytes(File file, byte[] data) throws IOException {
		FileUtils.writeByteArrayToFile(file, data);
	}

	/**
	 * Copies <code>sourceFile</code> to <code>destFile</code> overwriting as required, and <b>not</b> preserving the
	 * source file's last modified time. The destination directory is created if it does not exist, and if the
	 * destination file exists, it is overwritten.
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		FileUtils.copyFile(sourceFile, destFile, false);
	}

	/**
	 * Unzips the indicated file from the <code>classpathFile</code> location into the indicated <code>destDir</code>.
	 * 
	 * @param classpathFile
	 * @param destDir
	 * @throws IOException
	 */
	public static void unzip(String classpathFile, File destDir) throws IOException {
		try {
			unzip(FileUtil.class.getClassLoader().getResource(classpathFile), destDir);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Loads the given {@link URL}, and unzips the URL's contents into the given <code>destDir</code>.
	 * 
	 * @param url
	 * @param destDir
	 * @throws IOException
	 */
	public static void unzip(URL url, File destDir) throws IOException {
		if (url == null)
			throw new IOException("URL cannot be null");

		InputStream is = url.openStream();
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new BufferedInputStream(is));
			unzip(zis, destDir);
		} finally {
			IOUtils.closeQuietly(zis);
		}
	}

	public static void unzip(ZipInputStream in, File destDir) throws IOException {
		if (in == null)
			throw new IOException("input stream cannot be null");

		// Prepare destination
		destDir.mkdirs();
		File absDestDir = destDir.getAbsoluteFile();

		// Pull out the files
		OutputStream out = null;
		ZipEntry entry = null;
		try {
			while ((entry = in.getNextEntry()) != null) {
				if (entry.isDirectory())
					continue;

				// Prepare file destination
				File entryFile = new File(absDestDir, entry.getName());
				entryFile.getParentFile().mkdirs();

				out = new FileOutputStream(entryFile);
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(out);
				in.closeEntry();
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void unzipFile(File sourceFile, File destDir) throws IOException {
		if (!sourceFile.exists())
			throw new IOException("source file does not exist: " + sourceFile);

		ZipFile zipFile = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			zipFile = new ZipFile(sourceFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory())
					continue;

				File file = new File(destDir, entry.getName());
				String path = file.getAbsolutePath();
				file.getParentFile().mkdirs();

				//System.out.println("Writing file: " + path);
				is = zipFile.getInputStream(entry);
				os = new BufferedOutputStream(new FileOutputStream(path));
				copyWithClose(is, os);
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
			try {
				if (zipFile != null)
					zipFile.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Copies all bytes from InputStream to OutputStream without closing either stream.
	 * 
	 * @deprecated not in use. Use {@link IOUtils#copy(InputStream, OutputStream)} instead.
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	@Deprecated
	public static void copyWithoutClose(InputStream is, OutputStream os) throws IOException {
		IOUtils.copy(is, os);
	}

	/**
	 * Copies all bytes from InputStream to OutputStream, and close both streams before returning.
	 * 
	 * @param is
	 *            input stream to read data from.
	 * @param os
	 *            output stream to write data to.
	 * @throws IOException
	 */
	public static void copyWithClose(InputStream is, OutputStream os) throws IOException {
		try {
			IOUtils.copy(is, os);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	/**
	 * Recursively deletes all files and/or directories that have been modified longer than <code>daysOld</code> days
	 * ago. Note that this will recursively examine a directory, and only deletes those items that are too old.
	 * 
	 * @param file
	 *            the file or directory to recursively check and possibly delete
	 * @param daysOld
	 *            number of days old a file or directory can be before it is considered for deletion
	 * @throws IOException
	 *             if something goes wrong
	 */
	public static void delete(File file, int daysOld) throws IOException {
		Calendar olderThan = new GregorianCalendar();
		olderThan.add(Calendar.DATE, -daysOld);

		boolean shouldDelete = new Date(file.lastModified()).before(olderThan.getTime());

		if (file.isDirectory()) {
			// Wipe the contents first
			for (File currfile : file.listFiles()) {
				if (".".equals(currfile.getName()) || "..".equals(currfile.getName()))
					continue;
				delete(currfile, daysOld);
			}
		}
		if (shouldDelete)
			file.delete();
	}

	/**
	 * Recursively deletes the given file or directory
	 * 
	 * @see {@link FileUtils#deleteQuietly(File)}
	 * @param file
	 *            to recursively delete
	 */
	public static void delete(File file) {
		FileUtils.deleteQuietly(file);
	}

	/**
	 * Replace invalid File name characters, useful for token Save function to
	 * replace the : in Lib tokens.
	 * 
	 * @author Jamz
	 * @since 1.4.0.2
	 * 
	 * @param fileName
	 * @return
	 */
	public static String stripInvalidCharacters(String fileName) {
		return fileName = fileName.replaceAll("[^\\w\\s.,-]", "_");
	}
}
