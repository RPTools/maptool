/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.HTTPUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * New class extending AbstractFunction to create new "Macro Functions" getRequest & postRequest
 * 
 * getRequest(URL) :: Takes a URL as a string and sends a GET request, returning HTTP data. postRequest(URL, Parameters) :: Takes a URL as a string and a JSON array of Parameters and sends s POST
 * request, returning HTTP data.
 * 
 * HTTPUtil Class from: http://www.codejava.net/java-se/networking/an-http-utility-class-to-send-getpost-request
 * 
 */
public class HTTP_Functions extends AbstractFunction {

	private static final HTTP_Functions instance = new HTTP_Functions();

	private HTTP_Functions() {
		super(1, 3, "getRequest", "postRequest");
	}

	public static HTTP_Functions getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		String responseString = "";

		if (!MapTool.getParser().isMacroPathTrusted())
			throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

		if (!AppPreferences.getAllowExternalMacroAccess())
			throw new ParserException(I18N.getText("macro.function.general.accessDenied", functionName));

		// New function to return a response from a HTTP URL request.
		if (functionName.equals("getRequest")) {
			if (parameters.size() != 1)
				throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

			// Send GET Request to URL
			String requestURL = parameters.get(0).toString();
			InputStream is;

			try {
				is = HTTPUtil.sendGetRequest(requestURL);
				String[] response = HTTPUtil.parseMultipleLinesRespone(is);

				for (String line : response) {
					responseString += line + "\n";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return responseString;
		}

		if (functionName.equals("postRequest")) {
			if (parameters.size() == 0 || parameters.size() > 3)
				throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

			// Send POST request to URL
			Map<String, String> params = new HashMap<String, String>();
			String requestURL = parameters.get(0).toString();

			if (!requestURL.startsWith("syrinscape")) {
				if (parameters.size() > 3)
					throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 2, parameters.size()));

				// Use default key name of jsonData
				if (parameters.size() == 2)
					params.put("jsonData", parameters.get(1).toString());

				if (parameters.size() == 3)
					params.put(parameters.get(1).toString(), parameters.get(2).toString());

				InputStream is;
				try {
					is = HTTPUtil.sendPostRequest(requestURL, params);
					String[] response = HTTPUtil.parseMultipleLinesRespone(is);
					for (String line : response) {
						responseString += line + "\n";
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				return responseString;
			} else {
				if (parameters.size() != 1)
					throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

				if (!AppPreferences.getSyrinscapeActive())
					return BigDecimal.ZERO;

				URI uri = null;

				if (Desktop.isDesktopSupported()) {
					try {
						uri = new URI(requestURL);
						Desktop.getDesktop().browse(uri);
					} catch (IOException | URISyntaxException e) {
						e.printStackTrace();
					}
				}

				return BigDecimal.ONE;
			}
		}

		return "No Response";
	}
}
