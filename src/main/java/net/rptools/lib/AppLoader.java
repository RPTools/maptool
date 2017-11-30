/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.lib;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.GZIPInputStream;
import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.util.HTTPUtil;
import net.sf.json.JSONObject;

/**
 * This class checks for an updated main jar and, if found, downloads it. After update check, forwards on to main jar main method.
 * 
 * https://reportmill.wordpress.com/2014/12/04/automatically-update-your-javapackager-applications/
 */
public class AppLoader {
	private static final Logger log = LogManager.getLogger(AppLoader.class);

	// The App directory
	static final String AppDirName = "MyApp";

	// The main jar name
	static final String JarName = "MyApp.jar";

	// The path to the update jar at website
	static final String JarURL = "http://your-site.com/jars/MyApp.jar";

	static final String GIT_HUB_API_URL = "https://api.github.com/repos/JamzTheMan/MapTool/releases/latest";

	// The name of jar that holds this class
	static final String LoaderJarName = "AppLoader.jar";

	// The main class
	static final String MainClass = "whatever.app.App";

	static final String TEST_DATA = "{'url':'https://api.github.com/repos/JamzTheMan/maptool/releases/8691534','assets_url':'https://api.github.com/repos/JamzTheMan/maptool/releases/8691534/assets','upload_url':'https://uploads.github.com/repos/JamzTheMan/maptool/releases/8691534/assets{?name,label}','html_url':'https://github.com/JamzTheMan/maptool/releases/tag/1.4.0.0b1','id':8691534,'tag_name':'1.4.0.0b1','target_commitish':'aa6e1be3449ab850be90d4a3e6bb275273aa1eca','name':'MapTool 1.4.0.0 Beta 1','draft':false,'author':{'login':'JamzTheMan','id':8051654,'avatar_url':'https://avatars2.githubusercontent.com/u/8051654?v=4','gravatar_id':'','url':'https://api.github.com/users/JamzTheMan','html_url':'https://github.com/JamzTheMan','followers_url':'https://api.github.com/users/JamzTheMan/followers','following_url':'https://api.github.com/users/JamzTheMan/following{/other_user}','gists_url':'https://api.github.com/users/JamzTheMan/gists{/gist_id}','starred_url':'https://api.github.com/users/JamzTheMan/starred{/owner}{/repo}','subscriptions_url':'https://api.github.com/users/JamzTheMan/subscriptions','organizations_url':'https://api.github.com/users/JamzTheMan/orgs','repos_url':'https://api.github.com/users/JamzTheMan/repos','events_url':'https://api.github.com/users/JamzTheMan/events{/privacy}','received_events_url':'https://api.github.com/users/JamzTheMan/received_events','type':'User','site_admin':false},'prerelease':false,'created_at':'2017-11-28T21:49:08Z','published_at':'2017-11-28T22:01:45Z','assets':[{'url':'https://api.github.com/repos/JamzTheMan/maptool/releases/assets/5471856','id':5471856,'name':'MapTool-1.4.4.0.exe','label':null,'uploader':{'login':'JamzTheMan','id':8051654,'avatar_url':'https://avatars2.githubusercontent.com/u/8051654?v=4','gravatar_id':'','url':'https://api.github.com/users/JamzTheMan','html_url':'https://github.com/JamzTheMan','followers_url':'https://api.github.com/users/JamzTheMan/followers','following_url':'https://api.github.com/users/JamzTheMan/following{/other_user}','gists_url':'https://api.github.com/users/JamzTheMan/gists{/gist_id}','starred_url':'https://api.github.com/users/JamzTheMan/starred{/owner}{/repo}','subscriptions_url':'https://api.github.com/users/JamzTheMan/subscriptions','organizations_url':'https://api.github.com/users/JamzTheMan/orgs','repos_url':'https://api.github.com/users/JamzTheMan/repos','events_url':'https://api.github.com/users/JamzTheMan/events{/privacy}','received_events_url':'https://api.github.com/users/JamzTheMan/received_events','type':'User','site_admin':false},'content_type':'application/x-msdownload','state':'uploaded','size':92949767,'download_count':3,'created_at':'2017-11-28T22:48:15Z','updated_at':'2017-11-28T22:50:45Z','browser_download_url':'https://github.com/JamzTheMan/maptool/releases/download/1.4.0.0b1/MapTool-1.4.4.0.exe'},{'url':'https://api.github.com/repos/JamzTheMan/maptool/releases/assets/5490394','id':5490394,'name':'MapTool-1.4.4.0.jar','label':null,'uploader':{'login':'JamzTheMan','id':8051654,'avatar_url':'https://avatars2.githubusercontent.com/u/8051654?v=4','gravatar_id':'','url':'https://api.github.com/users/JamzTheMan','html_url':'https://github.com/JamzTheMan','followers_url':'https://api.github.com/users/JamzTheMan/followers','following_url':'https://api.github.com/users/JamzTheMan/following{/other_user}','gists_url':'https://api.github.com/users/JamzTheMan/gists{/gist_id}','starred_url':'https://api.github.com/users/JamzTheMan/starred{/owner}{/repo}','subscriptions_url':'https://api.github.com/users/JamzTheMan/subscriptions','organizations_url':'https://api.github.com/users/JamzTheMan/orgs','repos_url':'https://api.github.com/users/JamzTheMan/repos','events_url':'https://api.github.com/users/JamzTheMan/events{/privacy}','received_events_url':'https://api.github.com/users/JamzTheMan/received_events','type':'User','site_admin':false},'content_type':'application/octet-stream','state':'uploaded','size':48292335,'download_count':0,'created_at':'2017-11-30T15:23:24Z','updated_at':'2017-11-30T15:24:42Z','browser_download_url':'https://github.com/JamzTheMan/maptool/releases/download/1.4.0.0b1/MapTool-1.4.4.0.jar'},{'url':'https://api.github.com/repos/JamzTheMan/maptool/releases/assets/5471857','id':5471857,'name':'MapTool-1.4.4.0.msi','label':null,'uploader':{'login':'JamzTheMan','id':8051654,'avatar_url':'https://avatars2.githubusercontent.com/u/8051654?v=4','gravatar_id':'','url':'https://api.github.com/users/JamzTheMan','html_url':'https://github.com/JamzTheMan','followers_url':'https://api.github.com/users/JamzTheMan/followers','following_url':'https://api.github.com/users/JamzTheMan/following{/other_user}','gists_url':'https://api.github.com/users/JamzTheMan/gists{/gist_id}','starred_url':'https://api.github.com/users/JamzTheMan/starred{/owner}{/repo}','subscriptions_url':'https://api.github.com/users/JamzTheMan/subscriptions','organizations_url':'https://api.github.com/users/JamzTheMan/orgs','repos_url':'https://api.github.com/users/JamzTheMan/repos','events_url':'https://api.github.com/users/JamzTheMan/events{/privacy}','received_events_url':'https://api.github.com/users/JamzTheMan/received_events','type':'User','site_admin':false},'content_type':'application/octet-stream','state':'uploaded','size':117374976,'download_count':1,'created_at':'2017-11-28T22:48:15Z','updated_at':'2017-11-28T22:53:54Z','browser_download_url':'https://github.com/JamzTheMan/maptool/releases/download/1.4.0.0b1/MapTool-1.4.4.0.msi'}],'tarball_url':'https://api.github.com/repos/JamzTheMan/maptool/tarball/1.4.0.0b1','zipball_url':'https://api.github.com/repos/JamzTheMan/maptool/zipball/1.4.0.0b1','body':'Beta release for MapTool 1.4.0.0. Testing CI automated installs for windows.\\r\\n\\r\\nNot ready for production!'}";

	/**
	 * Main method - reinvokes main1() on app event thread in exception handler.
	 */
	public static void main(String args[]) {
		// Invoke real main with exception handler
		try {
			log.info("New version detected? " + checkForUpdatesUsingCommit());

			// main1(args);
		} catch (Throwable e) {
			showMessage("Error in Main", e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Main method: - Gets main Jar file from default, if missing - Updates main Jar file from local update file, if previously loaded - Load main Jar into URLClassLoader, load main class and invoke
	 * main method - Check for update from remove site in background
	 */
	public static void main1(String args[]) throws Exception {
		// Make sure default jar is in place
		try {
			copyDefaultMainJar();
		} catch (Exception e) {
			showMessage("Error Copying Main Jar", e.toString());
			e.printStackTrace();
		}

		// If Update Jar exists, copy it into place
		File jar = getAppFile(JarName);
		File updateJar = getAppFile(JarName + ".update");
		if (updateJar.exists()) {
			copyFile(updateJar, jar);
			jar.setLastModified(updateJar.lastModified());
			updateJar.delete();
		}

		// If jar doesn't exist complain bitterly
		if (!jar.exists() || !jar.canRead())
			throw new RuntimeException("Main Jar not found!");

		// Check for updates in background thread
		new Thread(() -> checkForUpdatesSilent()).start();

		// Create URLClassLoader for main jar file, get App class and invoke main
		URLClassLoader ucl = new URLClassLoader(new URL[] { jar.toURI().toURL() });
		Class cls = ucl.loadClass(MainClass); // ucl.close();
		Method meth = cls.getMethod("main", new Class[] { String[].class });
		meth.invoke(null, new Object[] { args });
		if (cls == Object.class)
			ucl.close(); // Getting rid of warning message for ucl
	}

	/**
	 * Copies the default main jar into place.
	 */
	private static void copyDefaultMainJar() throws IOException, ParseException {
		// Get date from app package
		URL url = AppLoader.class.getProtectionDomain().getCodeSource().getLocation();
		String path0 = url.getPath();
		path0 = URLDecoder.decode(path0, "UTF-8");
		String path2 = path0.replace(LoaderJarName, "BuildInfo.txt");
		BufferedReader br = new BufferedReader(new FileReader(path2));
		String text = br.readLine();
		br.close();
		if (text == null || text.length() < 0)
			return;
		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
		Date date = formatter.parse(text);
		long time = date.getTime();

		// Get main jar from app package
		String path1 = path0.replace(LoaderJarName, JarName);
		File jar0 = getAppFile(JarName);
		File jar1 = new File(path1);

		// If app package main jar is newer, copy it into place and set time
		if (jar0.exists() && jar0.lastModified() >= time)
			return;
		copyFile(jar1, jar0);
		jar0.setLastModified(time);
	}

	/**
	 * Check for updates.
	 */
	private static void checkForUpdatesSilent() {
		try {
			checkForUpdates();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean checkForUpdatesUsingCommit() {
		String response = TEST_DATA;
		String jarCommit = null;
		String latestGitHubReleaseCommit = null;

		// Attempt to get current commit out of JAR Manifest, if null is return, most likely ran from IDE/non-JAR version so skip
		URLClassLoader cl = (URLClassLoader) MapTool.class.getClassLoader();
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());

			Attributes attr = manifest.getMainAttributes();
			jarCommit = attr.getValue("Git-Commit-SHA");
			log.info("Git-Commit-SHA from Manifest: " + jarCommit);
		} catch (IOException e) {
			log.error("No Git-Commit-SHA attribute found in MANIFEST.MF, skip looking for updates...", e);
		}

		// If we don't have a commit attribute from JAR, we're done!
		if (jarCommit == null)
			return false;

		log.info("Hmm?");

		// try {
		// response = HTTPUtil.getJsonPaylod(GIT_HUB_API_URL);
		// log.info("Response: " + response);
		// } catch (IOException e) {
		// log.error("Unable to reach " + GIT_HUB_API_URL, e.getLocalizedMessage());
		// }

		try {
			JSONObject releases = JSONObject.fromObject(response);
			latestGitHubReleaseCommit = releases.get("target_commitish").toString();
			log.info("target_commitish from GitHub: " + latestGitHubReleaseCommit);
		} catch (Exception e) {
			log.error("Unable to parse JSON payload from GitHub...", e);
		}

		// If the commits are the same, we're done!
		if (jarCommit.equals(jarCommit))
			return false;

		return true;
	}

	/**
	 * Check for updates.
	 */
	private static void checkForUpdates() throws IOException, MalformedURLException {
		// Get URL connection and lastModified time
		File jarFile = getAppFile(JarName);
		URL url = new URL(JarURL);
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		long mod0 = jarFile.lastModified(), mod1 = connection.getLastModified();
		if (mod0 >= mod1) {
			System.out.println("No update available at " + JarURL + '(' + mod0 + '>' + mod1 + ')');
			return;
		}

		// Load Update bytes from URL connection
		System.out.println("Loading update from " + JarURL);
		byte bytes[] = getBytes(connection);
		System.out.println("Update loaded");
		File updateFile = getAppFile(JarName + ".update");

		// If packed write to pack file and unpack
		if (JarURL.endsWith(".pack.gz")) {
			File updatePacked = getAppFile(JarName + ".pack.gz");
			writeBytes(updatePacked, bytes);
			System.out.println("Update saved: " + updatePacked);
			unpack(updatePacked, updateFile);
			System.out.println("Update unpacked: " + updateFile);
			updatePacked.delete();
		}

		// If not packed just write to file
		else {
			writeBytes(updateFile, bytes);
			System.out.println("Update saved: " + updateFile);
		}

		// Set modified time so it matches server
		updateFile.setLastModified(mod1);

		// Let the user know
		String msg = "A new update is available. Restart application to apply";
		SwingUtilities.invokeLater(() -> showMessage("New Update Found", msg));
	}

	/**
	 * Returns the Main jar file.
	 */
	private static File getAppFile(String aName) {
		return new File(getAppDir(), aName);
	}

	/**
	 * Returns the Main jar file.
	 */
	private static File getAppDir() {
		return getAppDataDir(AppDirName, true);
	}

	/**
	 * 
	 * Utility Methods for AppLoader.
	 * 
	 */

	static void showMessage(String aTitle, String aMsg) {
		JOptionPane.showMessageDialog(null, aMsg, aTitle, JOptionPane.INFORMATION_MESSAGE);
		// Alert a = new Alert(AlertType.INFORMATION); a.setTitle(aTitle); a.setHeaderText(aMsg); a.showAndWait();
	}

	/**
	 * Copies a file from one location to another.
	 */
	public static File copyFile(File aSource, File aDest) throws IOException {
		// Get input stream, output file and output stream
		FileInputStream fis = new FileInputStream(aSource);
		File out = aDest.isDirectory() ? new File(aDest, aSource.getName()) : aDest;
		FileOutputStream fos = new FileOutputStream(out);

		// Iterate over read/write until all bytes written
		byte[] buf = new byte[8192];
		for (int i = fis.read(buf); i != -1; i = fis.read(buf))
			fos.write(buf, 0, i);

		// Close in/out streams and return out file
		fis.close();
		fos.close();
		return out;
	}

	/**
	 * Writes the given bytes (within the specified range) to the given file.
	 */
	public static void writeBytes(File aFile, byte theBytes[]) throws IOException {
		if (theBytes == null) {
			aFile.delete();
			return;
		}
		FileOutputStream fileStream = new FileOutputStream(aFile);
		fileStream.write(theBytes);
		fileStream.close();
	}

	/**
	 * Unpacks the given file into the destination file.
	 */
	public static File unpack(File aFile, File aDestFile) throws IOException {
		// Get dest file - if already unpacked, return
		File destFile = getUnpackDestination(aFile, aDestFile);
		if (destFile.exists() && destFile.lastModified() > aFile.lastModified())
			return destFile;

		// Create streams: FileInputStream -> GZIPInputStream -> JarOutputStream -> FileOutputStream
		FileInputStream fileInput = new FileInputStream(aFile);
		GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
		FileOutputStream fileOut = new FileOutputStream(destFile);
		JarOutputStream jarOut = new JarOutputStream(fileOut);

		// Unpack file
		Pack200.newUnpacker().unpack(gzipInput, jarOut);

		// Close streams
		fileInput.close();
		gzipInput.close();
		jarOut.close();
		fileOut.close();

		// Return destination file
		return destFile;
	}

	/**
	 * Returns the file that given packed file would be saved to using the unpack method.
	 */
	public static File getUnpackDestination(File aFile, File aDestFile) {
		// Get dest file - if null, create from packed file minus .pack.gz
		File destFile = aDestFile;
		if (destFile == null)
			destFile = new File(aFile.getPath().replace(".pack.gz", ""));

		// If dest file is directory, change to file inside with packed file minus .pack.gz
		else if (destFile.isDirectory())
			destFile = new File(destFile, aFile.getName().replace(".pack.gz", ""));

		// Return destination file
		return destFile;
	}

	/**
	 * Returns the AppData or Application Support directory file.
	 */
	public static File getAppDataDir(String aName, boolean doCreate) {
		// Get user home + AppDataDir (platform specific) + name (if provided)
		String dir = System.getProperty("user.home");
		if (isWindows)
			dir += File.separator + "AppData" + File.separator + "Local";
		else if (isMac)
			dir += File.separator + "Library" + File.separator + "Application Support";
		if (aName != null)
			dir += File.separator + aName;

		// Create file, actual directory (if requested) and return
		File dfile = new File(dir);
		if (doCreate && aName != null)
			dfile.mkdirs();
		return dfile;
	}

	/**
	 * Returns bytes for connection.
	 */
	public static byte[] getBytes(URLConnection aConnection) throws IOException {
		InputStream stream = aConnection.getInputStream(); // Get stream for connection
		byte bytes[] = getBytes(stream); // Get bytes for stream
		stream.close(); // Close stream
		return bytes; // Return bytes
	}

	/**
	 * Returns bytes for an input stream.
	 */
	public static byte[] getBytes(InputStream aStream) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		byte chunk[] = new byte[8192];
		for (int len = aStream.read(chunk, 0, 8192); len > 0; len = aStream.read(chunk, 0, 8192))
			bs.write(chunk, 0, len);
		return bs.toByteArray();
	}

	// Whether Windows/Mac
	static boolean isWindows = (System.getProperty("os.name").indexOf("Windows") >= 0);
	static boolean isMac = (System.getProperty("os.name").indexOf("Mac OS X") >= 0);

}