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
package net.rptools.lib.io;

import com.thoughtworks.xstream.XStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.FileUtil;
import net.rptools.lib.ModelVersionManager;
import net.rptools.maptool.model.GUID;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a container of content/files within a single actual file.
 *
 * <p>A packed file contains three parts:
 *
 * <ul>
 *   <li>Contents - A single object that represents the core content of the packed file, as a
 *       convenience method
 *   <li>Properties - A String to Object map of arbitrary properties that can be used to describe
 *       the packed file
 *   <li>Files - Any arbitrary files/data that are packed within the packed file
 * </ul>
 *
 * <p>The implementation uses two {@link Set}s, <b>addedFileSet</b> and <b>removedFileSet</b>, to
 * keep track of paths for content that has been added or removed from the packed file,
 * respectively. This is because the actual file itself isn't written until the {@link #save()}
 * method is called, yet the application may want to dynamically add and remove paths to the packed
 * file and query the state of which files are currently included or excluded from the packed file.
 *
 * <p>In addition, the API allows for storing multiple types of objects into the packed file. The
 * easiest to understand are byte streams as they are binary values that are not modified by
 * character set encoding during output. They are represented by an array of bytes or an {@link
 * InputStream}.
 *
 * <p>The second type of data is the file, represented here as a URL as it is more universally
 * applicable (although currently it is unused outside this class). URLs have their content
 * retrieved from the source and are currently written into the packed file without any character
 * encoding (it is possible that the <code>Content-Type</code> of the data stream could provide
 * information on how the data should be written so this may change in the future).
 *
 * <p>The last and most important type of data is the POJO (plain old Java object). These are
 * converted into XML using the XStream library from codehaus.org. As the data is written to the
 * output file it is character set encoded to UTF-8. It is hoped that this solves the localization
 * issues with saved macros and other data not being restored properly. Because of this, data loaded
 * from a packed file is always retrieved without character set encoding <b>unless</b> it is XML
 * data. This should preserve binary data such as JPEG and PNG images properly. A side effect of
 * this is that all character data should be written to the packed file as POJOs in order to obtain
 * the automatic character set encoding. (Otherwise, strings can be converted to UTF-8 using the
 * {@link String#getBytes(String)} method.
 */
public class PackedFile {

  private static final String PROPERTY_FILE = "properties.xml";
  private static final String CONTENT_FILE = "content.xml";

  private static final Logger log = LogManager.getLogger(PackedFile.class);

  private static File tmpDir =
      new File(System.getProperty("java.io.tmpdir")); // Shared temporary directory

  private final XStream xstream = FileUtil.getConfiguredXStream();

  private final File file; // Original zip file
  private final File tmpFile; // Temporary directory where changes are kept

  private boolean dirty;
  private boolean propsLoaded;

  private Map<String, Object> propertyMap = new HashMap<String, Object>();
  private final Set<String> addedFileSet = new HashSet<String>();
  private final Set<String> removedFileSet = new HashSet<String>();

  private ModelVersionManager versionManager;

  /**
   * By default all temporary files are handled in /tmp. Use this method to globally set the
   * location of the temporary directory
   */
  public static void init(File tmpDir) {
    PackedFile.tmpDir = tmpDir;
  }

  public static File getTmpDir() {
    return PackedFile.tmpDir;
  }

  public void setModelVersionManager(ModelVersionManager versionManager) {
    this.versionManager = versionManager;
  }

  /** Useful for configuring the xstream for object serialization */
  public XStream getXStream() {
    return xstream;
  }

  public PackedFile(File file) {
    this.file = file;
    dirty = !file.exists();
    tmpFile = new File(tmpDir, new GUID() + ".tmp");
  }

  /**
   * Retrieves the property map from the campaign file and accesses the given key, returning an
   * Object that the key holds. The Object is constructed from the XML content and could be
   * anything.
   *
   * @param key key for accessing the property map
   * @return the value (typically a String)
   * @throws IOException
   */
  public Object getProperty(String key) throws IOException {
    return getPropertyMap().get(key);
  }

  /**
   * Returns a list of all keys in the campaign's property map. See also {@link
   * #getProperty(String)}.
   *
   * @return list of all keys
   * @throws IOException
   */
  public Iterator<String> getPropertyNames() throws IOException {
    return getPropertyMap().keySet().iterator();
  }

  /**
   * Stores a new key/value pair into the property map. Existing keys are overwritten.
   *
   * @param key
   * @param value any POJO; will be serialized into XML upon writing
   * @return the previous value for the given key
   * @throws IOException
   */
  public Object setProperty(String key, Object value) throws IOException {
    dirty = true;
    return getPropertyMap().put(key, value);
  }

  /**
   * Remove the property with the associated key from the property map.
   *
   * @param key
   * @return the previous value for the given key
   * @throws IOException
   */
  public Object removeProperty(String key) throws IOException {
    dirty = true;
    return getPropertyMap().remove(key);
  }

  /**
   * Retrieves the contents of the <code>CONTENT_FILE</code> as a POJO. This object is the top-level
   * data structure for all information regarding the content of the PackedFile.
   *
   * @return the results of the deserialization
   * @throws IOException
   */
  public Object getContent() throws IOException {
    return getContent(versionManager, (String) getProperty("version"));
  }

  /**
   * Same as {@link #getContent()} except that the version can be specified. This allows a newer
   * release of an application to provide automatic transformation information that will be applied
   * to the XML as the object is deserialized. The default transformation manager is used. (Think of
   * the transformation as a simplified XSTL process.)
   *
   * @param fileVersion such as "1.3.70"
   * @return the results of the deserialization
   * @throws IOException
   */
  public Object getContent(String fileVersion) throws IOException {
    return getContent(versionManager, fileVersion);
  }

  /**
   * Same as {@link #getContent(String)} except that the transformation manager, <code>
   * versionManager</code>, is specified as a parameter.
   *
   * @param versionManager which set of transforms to apply to older file versions
   * @param fileVersion such as "1.3.70"
   * @return the results of the deserialization
   * @throws IOException
   */
  public Object getContent(ModelVersionManager versionManager, String fileVersion)
      throws IOException {
    Reader r = null;
    try {
      if (versionManager != null && versionManager.isTransformationRequired(fileVersion)) {
        r = getFileAsReader(CONTENT_FILE);
        String xml = IOUtils.toString(r);
        xml = versionManager.transform(xml, fileVersion);
        xstream.ignoreUnknownElements(); // Jamz: Should we use this? This will ignore new
        // classes/fields added.
        return xstream.fromXML(xml);
      } else {
        return getFileObject(CONTENT_FILE);
      }
    } catch (NullPointerException npe) {
      log.error("Problem finding/converting content file", npe);
      return null;
    } finally {
      IOUtils.closeQuietly(r);
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getPropertyMap() throws IOException {
    if (hasFile(PROPERTY_FILE) && !propsLoaded) {
      propertyMap = null;
      // This is the case when we're pointing to a file but haven't loaded it yet
      try {
        Object obj = getFileObject(PROPERTY_FILE);
        if (obj instanceof Map<?, ?>) {
          propertyMap = (Map<String, Object>) obj;
          propsLoaded = true;
        } else log.error("Unexpected class type for property object: " + obj.getClass().getName());
      } catch (NullPointerException npe) {
        log.error("Problem finding/converting property file", npe);
      }
    }
    return propertyMap;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void save() throws IOException {
    CodeTimer saveTimer;

    if (!dirty) {
      return;
    }
    saveTimer = new CodeTimer("PackedFile.save");
    saveTimer.setEnabled(log.isDebugEnabled());

    InputStream is = null;

    // Create the new file
    File newFile = new File(tmpDir, new GUID() + ".pak");
    ZipOutputStream zout =
        new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(newFile)));
    zout.setLevel(Deflater.BEST_COMPRESSION); // fast compression
    try {
      saveTimer.start(CONTENT_FILE);
      if (hasFile(CONTENT_FILE)) {
        zout.putNextEntry(new ZipEntry(CONTENT_FILE));
        is = getFileAsInputStream(CONTENT_FILE); // When copying, always use an InputStream
        IOUtils.copy(is, zout);
        IOUtils.closeQuietly(is);
        zout.closeEntry();
      }
      saveTimer.stop(CONTENT_FILE);

      saveTimer.start(PROPERTY_FILE);
      if (getPropertyMap().isEmpty()) {
        removeFile(PROPERTY_FILE);
      } else {
        zout.putNextEntry(new ZipEntry(PROPERTY_FILE));
        xstream.toXML(getPropertyMap(), zout);
        zout.closeEntry();
      }
      saveTimer.stop(PROPERTY_FILE);

      // Now put each file
      saveTimer.start("addFiles");
      addedFileSet.remove(CONTENT_FILE);
      for (String path : addedFileSet) {
        zout.putNextEntry(new ZipEntry(path));
        is = getFileAsInputStream(path); // When copying, always use an InputStream
        IOUtils.copy(is, zout);
        IOUtils.closeQuietly(is);
        zout.closeEntry();
      }
      saveTimer.stop("addFiles");

      // Copy the rest of the zip entries over
      saveTimer.start("copyFiles");
      if (file.exists()) {
        Enumeration<? extends ZipEntry> entries = zFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          if (!entry.isDirectory()
              && !addedFileSet.contains(entry.getName())
              && !removedFileSet.contains(entry.getName())
              && !CONTENT_FILE.equals(entry.getName())
              && !PROPERTY_FILE.equals(entry.getName())) {
            // if (entry.getName().endsWith(".png") ||
            // entry.getName().endsWith(".gif") ||
            // entry.getName().endsWith(".jpeg"))
            // zout.setLevel(Deflater.NO_COMPRESSION); // none needed for images as they are already
            // compressed
            // else
            // zout.setLevel(Deflater.BEST_COMPRESSION); // fast compression
            zout.putNextEntry(entry);
            is = getFileAsInputStream(entry.getName()); // When copying, always use an InputStream
            IOUtils.copy(is, zout);
            IOUtils.closeQuietly(is);
            zout.closeEntry();
          } else if (entry.isDirectory()) {
            zout.putNextEntry(entry);
            zout.closeEntry();
          }
        }
      }
      try {
        if (zFile != null) zFile.close();
      } catch (IOException e) {
        // ignore close exception
      }
      zFile = null;
      saveTimer.stop("copyFiles");

      saveTimer.start("close");
      IOUtils.closeQuietly(zout);
      zout = null;
      saveTimer.stop("close");

      // Backup the original
      saveTimer.start("backup");
      File backupFile = new File(tmpDir, new GUID() + ".mv");
      if (file.exists()) {
        backupFile.delete(); // Always delete the old backup file first; renameTo() is very
        // platform-dependent
        if (!file.renameTo(backupFile)) {
          saveTimer.start("backup file");
          FileUtil.copyFile(file, backupFile);
          file.delete();
          saveTimer.stop("backup file");
        }
      }
      saveTimer.stop("backup");

      saveTimer.start("finalize");
      // Finalize
      if (!newFile.renameTo(file)) {
        saveTimer.start("backup newFile");
        FileUtil.copyFile(newFile, file);
        saveTimer.stop("backup newFile");
      }
      if (backupFile.exists()) backupFile.delete();
      saveTimer.stop("finalize");

      dirty = false;
    } finally {
      saveTimer.start("cleanup");
      try {
        if (zFile != null) zFile.close();
      } catch (IOException e) {
        // ignore close exception
      }
      if (newFile.exists()) newFile.delete();
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(zout);
      saveTimer.stop("cleanup");

      if (log.isDebugEnabled()) log.debug(saveTimer);
      saveTimer = null;
    }
  }

  /**
   * Set the given object as the information to write to the 'content.xml' file in the archive.
   *
   * @param content
   * @throws IOException
   */
  public void setContent(Object content) throws IOException {
    putFile(CONTENT_FILE, content);
  }

  /**
   * Does the work of preparing for output to a temporary file, returning the {@link File} object
   * associated with the temporary location. The caller is then expected to open and write their
   * data to the file which will later be added to the ZIP file.
   *
   * @param path path within the ZIP to write to
   * @return the <code>File</code> object for the temporary location
   * @throws IOException
   */
  private File putFileImpl(String path) throws IOException {
    if (!tmpFile.exists()) tmpFile.getParentFile().mkdirs();

    // Have to store it in the exploded area since we can't directly save it to the zip
    File explodedFile = getExplodedFile(path);
    if (explodedFile.exists()) {
      explodedFile.delete();
    } else {
      explodedFile.getParentFile().mkdirs();
    }

    // We just remember that we added it, then go look for it later...
    addedFileSet.add(path);
    removedFileSet.remove(path);
    dirty = true;
    return explodedFile;
  }

  /**
   * Write the <code>byte</code> data to the given path in the ZIP file; as the data is binary there
   * is no {@link Charset} conversion.
   *
   * @param path location within the ZIP file
   * @param data the binary data to be written
   * @throws IOException
   */
  public void putFile(String path, byte[] data) throws IOException {
    InputStream is = new ByteArrayInputStream(data);
    putFile(path, is);
    IOUtils.closeQuietly(is);
  }

  /**
   * Write the <b>binary</b> data to the given path in the ZIP file; as the data is presumed to be
   * binary there is no charset conversion.
   *
   * @param path location within the ZIP file
   * @param is the binary data to be written in the form of an InputStream
   * @throws IOException
   */
  public void putFile(String path, InputStream is) throws IOException {
    File explodedFile = putFileImpl(path);
    FileOutputStream fos = new FileOutputStream(explodedFile);
    IOUtils.copy(is, fos);
    IOUtils.closeQuietly(fos);
  }

  /**
   * Write the serialized object to the given path in the ZIP file; as the data is an object it is
   * first converted to XML and character set encoding will take place as the data is written to the
   * (temporary) file.
   *
   * @param path location within the ZIP file
   * @param obj the object to be written
   * @throws IOException
   */
  public void putFile(String path, Object obj) throws IOException {
    File explodedFile = putFileImpl(path);
    FileOutputStream fos = new FileOutputStream(explodedFile);
    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    BufferedWriter bw = new BufferedWriter(osw);

    xstream.toXML(obj, bw);

    bw.newLine(); // Not necessary but editing the file looks nicer. ;-)
    IOUtils.closeQuietly(bw);
  }

  /**
   * Write the data from the given URL to the path in the ZIP file; as the data is presumed binary
   * there is no {@link Charset} conversion.
   *
   * <p>FIXME Should the MIME type of the InputStream be checked??
   *
   * @param path location within the ZIP file
   * @param url the url of the binary data to be written
   * @throws IOException
   */
  public void putFile(String path, URL url) throws IOException {
    InputStream is = url.openStream();
    putFile(path, is);
    IOUtils.closeQuietly(is);
  }

  public boolean hasFile(String path) throws IOException {
    if (removedFileSet.contains(path)) return false;

    File explodedFile = getExplodedFile(path);
    if (explodedFile.exists()) return true;

    boolean ret = false;
    if (file.exists()) {
      ZipFile zipFile = getZipFile();
      ZipEntry ze = zipFile.getEntry(path);
      ret = (ze != null);
    }
    return ret;
  }

  private ZipFile zFile = null;

  private ZipFile getZipFile() throws IOException {
    if (zFile == null) zFile = new ZipFile(file);
    return zFile;
  }

  /**
   * Returns a POJO by reading the contents of the zip archive path specified and converting the XML
   * via the associated XStream object. (Because the XML is character data, this routine calls
   * {@link #getFileAsReader(String)} to handle character encoding.)
   *
   * <p><b>TODO:</b> add {@link ModelVersionManager} support
   *
   * @param path zip file archive path entry
   * @return Object created by translating the XML
   * @throws IOException
   */
  public Object getFileObject(String path) throws IOException {
    // This next line really should be routed thru the version manager...
    // Update: a new XStreamConverter was created for the Asset object that
    // never marshalls the image data, but *does* unmarshall it. This allows
    // older pre-1.3.b64 campaigns to be loaded but only the newer format
    // (with a separate image file) works on output.
    LineNumberReader r = getFileAsReader(path);
    Object o = null;
    try {
      xstream
          .ignoreUnknownElements(); // Jamz: Should we use this? This will ignore new classes/fields
      // added.
      o = xstream.fromXML(r);
    } catch (InstantiationError ie) {
      log.error("Found at line number " + r.getLineNumber());
      log.error("Cannot convert XML to Object", ie);
      throw ie;
    } finally {
      IOUtils.closeQuietly(r);
    }
    return o;
  }

  /**
   * Returns an InputStreamReader that corresponds to the zip file path specified. This method
   * should be called only for character-based file contents such as the <b>CONTENT_FILE</b> and
   * <b>PROPERTY_FILE</b>. For binary data, such as images (assets and thumbnails) use {@link
   * #getFileAsInputStream(String)} instead.
   *
   * @param path zip file archive path entry
   * @return Reader representing the data stream
   * @throws IOException
   */
  public LineNumberReader getFileAsReader(String path) throws IOException {
    File explodedFile = getExplodedFile(path);
    if ((!file.exists() && !tmpFile.exists() && !explodedFile.exists())
        || removedFileSet.contains(path)) throw new FileNotFoundException(path);
    if (explodedFile.exists()) return new LineNumberReader(FileUtil.getFileAsReader(explodedFile));

    ZipEntry entry = new ZipEntry(path);
    ZipFile zipFile = getZipFile();
    InputStream in = null;
    try {
      in = new BufferedInputStream(zipFile.getInputStream(entry));
      if (log.isDebugEnabled()) {
        String type;
        type = FileUtil.getContentType(in);
        if (type == null) type = FileUtil.getContentType(explodedFile);
        log.debug("FileUtil.getContentType() returned " + (type != null ? type : "(null)"));
      }
      return new LineNumberReader(new InputStreamReader(in, "UTF-8"));
    } catch (IOException ex) {
      // Don't need to close 'in' since zipFile.close() will do so
      throw ex;
    }
  }

  /**
   * Returns an InputStream that corresponds to the zip file path specified. This method should be
   * called only for binary file contents such as images (assets and thumbnails). For
   * character-based data, use {@link #getFileAsReader(String)} instead.
   *
   * @param path zip file archive path entry
   * @return InputStream representing the data stream
   * @throws IOException
   */
  public InputStream getFileAsInputStream(String path) throws IOException {
    File explodedFile = getExplodedFile(path);
    if ((!file.exists() && !tmpFile.exists() && !explodedFile.exists())
        || removedFileSet.contains(path)) throw new FileNotFoundException(path);
    if (explodedFile.exists()) return FileUtil.getFileAsInputStream(explodedFile);

    ZipEntry entry = new ZipEntry(path);
    ZipFile zipFile = getZipFile();
    InputStream in = null;
    try {
      in = zipFile.getInputStream(entry);
      if (in == null) throw new FileNotFoundException(path);
      String type = FileUtil.getContentType(in);
      if (log.isDebugEnabled() && type != null)
        log.debug("FileUtil.getContentType() returned " + type);
      return in;
    } catch (IOException ex) {
      // Don't need to close 'in' since zipFile.close() will do so
      IOUtils.closeQuietly(in);
      throw ex;
    }
  }

  public void close() {
    if (zFile != null) {
      try {
        zFile.close();
      } catch (IOException e) {
        // Ignore it
      }
      zFile = null;
    }
    if (tmpFile.exists()) FileUtil.delete(tmpFile);
    propertyMap.clear();
    addedFileSet.clear();
    removedFileSet.clear();
    propsLoaded = false;
    dirty = !file.exists();
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  protected File getExplodedFile(String path) {
    return new File(tmpFile, path);
  }

  /**
   * Get all of the path names for this packed file.
   *
   * @return All the path names. Changing this set does not affect the packed file. Changes to the
   *     file made after this method is called are not reflected in the path and do not cause a
   *     ConcurrentModificationException. Directories in the packed file are also included in the
   *     set.
   * @throws IOException Problem with the zip file.
   */
  public Set<String> getPaths() throws IOException {
    Set<String> paths = new HashSet<String>(addedFileSet);
    paths.add(CONTENT_FILE);
    paths.add(PROPERTY_FILE);
    if (file.exists()) {
      ZipFile zf = getZipFile();
      Enumeration<? extends ZipEntry> e = zf.entries();
      while (e.hasMoreElements()) {
        paths.add(e.nextElement().getName());
      }
    }
    paths.removeAll(removedFileSet);
    return paths;
  }

  /** @return Getter for file */
  public File getPackedFile() {
    return file;
  }

  /**
   * Return a URL for a path in this file.
   *
   * @param path Get the url for this path
   * @return URL that can be used to access the file.
   * @throws IOException invalid zip file.
   */
  public URL getURL(String path) throws IOException {
    if (!hasFile(path))
      throw new FileNotFoundException("The path '" + path + "' is not in this packed file.");
    try {
      // Check for exploded first
      File explodedFile = getExplodedFile(path);
      if (explodedFile.exists()) return explodedFile.toURI().toURL();

      // Otherwise it is in the zip file.
      if (!path.startsWith("/")) path = "/" + path;
      String url = "jar:" + file.toURI().toURL().toExternalForm() + "!" + path;
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Couldn't create a URL for path: '" + path + "'");
    }
  }

  /**
   * Remove a path from the packed file.
   *
   * @param path Remove this path
   */
  public void removeFile(String path) {
    removedFileSet.add(path);
    addedFileSet.remove(path);
    File explodedFile = getExplodedFile(path);
    if (explodedFile.exists()) {
      explodedFile.delete();
    }
    dirty = true;
  }

  /**
   * Remove all files and directories from the zip file.
   *
   * @throws IOException Problem reading the zip file.
   */
  public void removeAll() throws IOException {
    Set<String> paths = getPaths();
    for (String path : paths) {
      removeFile(path);
    } // endfor
  }

  /**
   * Create an output stream that will be written to the packed file. Caller is responsible for
   * closing the stream.
   *
   * @param path Path of the file being saved.
   * @return Stream that can be used to write the data.
   * @throws IOException Error opening the stream.
   */
  public OutputStream getOutputStream(String path) throws IOException {
    if (!tmpFile.exists()) {
      tmpFile.mkdirs();
    }
    File explodedFile = getExplodedFile(path);
    dirty = true;
    if (explodedFile.exists()) {
      return new FileOutputStream(explodedFile);
    } else {
      explodedFile.getParentFile().mkdirs();
    }
    addedFileSet.add(path);
    removedFileSet.remove(path);
    dirty = true;
    return new FileOutputStream(explodedFile);
  }
}
