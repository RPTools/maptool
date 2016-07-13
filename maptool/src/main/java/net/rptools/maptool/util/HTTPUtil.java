package net.rptools.maptool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class encapsulates methods for requesting a server via HTTP GET/POST
 * and provides methods for parsing response from the server.
 * 
 * @author Ha Minh Nam
 *
 */

// Jamz: There appears to be a webdownloader(url) function already, replace?
public class HTTPUtil {

	/**
	 * Makes an HTTP request using GET method to the specified URL and returns
	 * an input stream that wraps server's response.
	 * 
	 * @param requestURL
	 *            the URL of the remote server
	 * @return An InputStream object to read server's response
	 * @throws IOException
	 *             thrown if any I/O error occurred
	 */
	public static InputStream sendGetRequest(String requestURL) throws IOException {
		URL url = new URL(requestURL);
		URLConnection urlConn = url.openConnection();
		urlConn.setUseCaches(false);

		urlConn.setDoInput(true); // true indicates the server returns response
		urlConn.setDoOutput(false); // false indicates GET request

		return urlConn.getInputStream();
	}

	/**
	 * Makes an HTTP request using POST method to the specified URL and returns
	 * an input stream that wraps server's response.
	 * 
	 * @param requestURL
	 *            the URL of the remote server
	 * @param params
	 *            A map containing POST data in form of key-value pairs
	 * @return An InputStream object to read server's response
	 * @throws IOException
	 *             thrown if any I/O error occurred
	 */
	public static InputStream sendPostRequest(String requestURL, Map<String, String> params) throws IOException {
		URL url = new URL(requestURL);
		URLConnection urlConn = url.openConnection();
		urlConn.setUseCaches(false);

		urlConn.setDoInput(true); // true indicates the server returns response

		StringBuffer requestParams = new StringBuffer();

		if (params != null && params.size() > 0) {

			urlConn.setDoOutput(true); // true indicates POST request

			// creates the params string, encode them using URLEncoder
			Iterator<String> paramIterator = params.keySet().iterator();
			while (paramIterator.hasNext()) {
				String key = paramIterator.next();
				String value = params.get(key);
				requestParams.append(URLEncoder.encode(key, "UTF-8"));
				requestParams.append("=").append(URLEncoder.encode(value, "UTF-8"));
				requestParams.append("&");
			}

			// sends POST data
			OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
			writer.write(requestParams.toString());
			writer.flush();
		}

		return urlConn.getInputStream();
	}

	/**
	 * Returns only one line from the server's response. This method should be used
	 * if the server returns only a single line of String.
	 * 
	 * @param inputStream
	 *            the InputStream to parse server's response
	 * @return a String of the server's response
	 * @throws IOException
	 *             thrown if any I/O error occurred
	 */
	public static String parseSingleLineRespone(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String response = reader.readLine();
		reader.close();

		return response;
	}

	/**
	 * Returns an array of lines from the server's response. This method should be used
	 * if the server returns multiple lines of String.
	 * 
	 * @param inputStream
	 *            the InputStream to parse server's response
	 * @return an array of Strings of the server's response
	 * @throws IOException
	 *             thrown if any I/O error occurred
	 */
	public static String[] parseMultipleLinesRespone(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		List<String> response = new ArrayList<String>();

		String line = "";
		while ((line = reader.readLine()) != null) {
			response.add(line);
		}
		reader.close();

		return (String[]) response.toArray(new String[0]);
	}
}