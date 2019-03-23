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
package net.rptools.lib.net;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.imageio.ImageWriter;
import net.rptools.lib.FileUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class FTPLocation implements Location {
  private final String username;
  private transient String password;
  private final String hostname;
  private final String path;
  private final boolean binary;

  public FTPLocation(String username, String password, String hostname, String path) {
    this.username = username;
    this.password = password;
    this.hostname = hostname;
    this.path = path;
    this.binary = true;
  }

  public FTPLocation(
      String username, String password, String hostname, String path, boolean binary) {
    this.username = username;
    this.password = password;
    this.hostname = hostname;
    this.path = path;
    this.binary = binary;
  }

  public boolean isBinary() {
    return binary;
  }

  public String getHostname() {
    return hostname;
  }

  public String getPassword() {
    return password;
  }

  public String getPath() {
    return path;
  }

  public String getUsername() {
    return username;
  }

  public void putContent(InputStream content) throws IOException {
    OutputStream os = null;
    try {
      // os = composeURL().openConnection().getOutputStream();
      os = new URL(composeFileLocation()).openConnection().getOutputStream();
      FileUtil.copyWithClose(content, os);
    } finally {
      IOUtils.closeQuietly(os);
    }
  }

  public void putContent(ImageWriter writer, BufferedImage content) throws IOException {
    OutputStream os = null;
    try {
      // os = composeURL().openConnection().getOutputStream();
      os = new URL(composeFileLocation()).openConnection().getOutputStream();
      writer.setOutput(os);
      writer.write(content);
    } finally {
      IOUtils.closeQuietly(os);
    }
  }

  public InputStream getContent() throws IOException {
    // return composeURL().openConnection().getInputStream();
    return new URL(composeFileLocation()).openConnection().getInputStream();
  }

  private String composeFileLocation() {
    StringBuilder builder = new StringBuilder();

    builder.append("ftp://");
    if (username != null && !StringUtils.isEmpty(username)) {
      builder.append(username.replaceAll("@", "%40").replaceAll(":", "%3A"));
    } else {
      builder.append("anonymous");
    }
    if (password != null && !StringUtils.isEmpty(password)) {
      builder.append(":").append(password.replaceAll("@", "%40").replaceAll(":", "%3A"));
    }
    builder.append("@");
    builder.append(hostname);
    builder.append("/");
    builder.append(path);
    if (binary) {
      builder.append(";type=i");
    }
    return builder.toString();
  }
}
