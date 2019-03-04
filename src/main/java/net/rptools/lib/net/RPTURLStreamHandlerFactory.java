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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;
import net.rptools.lib.FileUtil;

public class RPTURLStreamHandlerFactory implements URLStreamHandlerFactory {

  private static Map<String, byte[]> imageMap = new HashMap<String, byte[]>();

  private Map<String, URLStreamHandler> protocolMap = new HashMap<String, URLStreamHandler>();

  public RPTURLStreamHandlerFactory() {
    registerProtocol("cp", new ClasspathStreamHandler());
  }

  public void registerProtocol(String protocol, URLStreamHandler handler) {
    protocolMap.put(protocol, handler);
  }

  public URLStreamHandler createURLStreamHandler(String protocol) {

    return protocolMap.get(protocol);
  }

  private static class ClasspathStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {

      // TODO: This should really figure out the exact type
      return new ImageURLConnection(u);
    }
  }

  private static class ImageURLConnection extends URLConnection {

    private byte[] data;

    public ImageURLConnection(URL url) {
      super(url);

      String path = url.getHost() + url.getFile();
      data = imageMap.get(path);
      if (data == null) {
        try {
          data = FileUtil.loadResource(path);
          imageMap.put(path, data);
        } catch (IOException ioe) {
          ioe.printStackTrace();
          data = new byte[] {};
        }
      }
    }

    @Override
    public void connect() throws IOException {
      // Nothing to do
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(data);
    }
  }
}
