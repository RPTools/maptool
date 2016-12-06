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
