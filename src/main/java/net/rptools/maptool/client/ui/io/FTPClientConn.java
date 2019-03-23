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
package net.rptools.maptool.client.ui.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/** @author crash */
class FTPClientConn {
  private static final String PROTOCOL = "ftp://";
  private static final String TYPE_IMAGE = ";type=i";
  public final String host;
  public final String user;
  protected final String password;
  protected final String proto_user_pswd_host;

  public FTPClientConn(String _host, String _user, String _password) {
    host = _host;
    user = _user;
    password = _password;

    if (user == null) proto_user_pswd_host = PROTOCOL + host + "/";
    else if (password == null)
      proto_user_pswd_host = PROTOCOL + encodeUser(user) + "@" + host + "/";
    else proto_user_pswd_host = PROTOCOL + encodeUser(user) + ":" + password + "@" + host + "/";
  }

  private static String encodeUser(String u) {
    return u.replace("@", "%40");
  }

  /**
   * Since this method uses a separate connection to the FTP server, the full pathname to the
   * directory must be specified.
   *
   * <p>Note that the directory separator may be "/" locally and in URLs, but FTP servers are not
   * required to support it. This means that we really should be starting at the top of the tree and
   * issuing <code>cd()</code> calls for the entire pathname, creating any that fail along the way.
   * Too much work for now. FIXME
   *
   * @param dir the full pathname to the directory to create
   * @return success or failure
   */
  public int mkdir(String dir) {
    int result = -1;
    FTPCommand myftp = null;
    try {
      myftp = new FTPCommand(host);
      myftp.login(user, password);
      result = myftp.mkdir(dir);
      // System.err.print("Response from mkdir() is " + myftp.getResponseString());
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (myftp != null) {
        try {
          myftp.closeServer();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  /**
   * Since this method uses a separate connection to the FTP server, the full pathname to the file
   * must be specified.
   *
   * <p>Note that the directory separator may be "/" locally and in URLs, but FTP servers are not
   * required to support it. This means that we really should be starting at the top of the tree and
   * issuing <code>cd()</code> calls for the entire pathname, creating any that fail along the way.
   * Too much work for now. FIXME
   *
   * @param filename the full pathname to the file to remove
   * @return success or failure
   */
  public int remove(String filename) {
    int result = -1;
    FTPCommand myftp = null;
    try {
      myftp = new FTPCommand(host);
      myftp.login(user, password);
      result = myftp.remove(filename);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (myftp != null) {
        try {
          myftp.closeServer();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  protected URL makeURL(String directory, String targetfile) throws MalformedURLException {
    return new URL(
        proto_user_pswd_host
            + (directory == null ? "" : directory + "/")
            + targetfile
            + TYPE_IMAGE);
  }

  protected InputStream openDownloadStream(String targetfile) throws IOException {
    return openDownloadStream(null, targetfile);
  }

  protected InputStream openDownloadStream(String dir, String targetfile) throws IOException {
    FTPCommand cmd = new FTPCommand(host);
    cmd.login(user, password);
    InputStream is = cmd.retrieveFileStream((dir == null ? "" : dir + "/") + targetfile);
    return is;
  }

  protected OutputStream openUploadStream(String targetfile) throws IOException {
    return openUploadStream(null, targetfile);
  }

  protected OutputStream openUploadStream(String dir, String targetfile) throws IOException {
    FTPCommand cmd = new FTPCommand(host);
    cmd.login(user, password);
    OutputStream os = cmd.storeFileStream((dir == null ? "" : dir + "/") + targetfile);
    return os;
  }
}
