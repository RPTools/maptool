package net.rptools.maptool.protocol.syrinscape;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class SyrinscapeURLStreamHandler extends URLStreamHandler {
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return new SyrinscapeConnection(url);
	}
}