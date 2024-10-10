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
package net.rptools.maptool.client.functions;

import com.google.gson.JsonArray;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.rptools.lib.sound.SoundManager;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** This class handles functions to play, stop, and edit audio clips and streams. */
public class SoundFunctions extends AbstractFunction {

  /** The singleton instance. */
  private static final SoundFunctions instance = new SoundFunctions();

  private SoundFunctions() {
    super(
        0,
        7,
        "playStream",
        "playClip",
        "stopSound",
        "getSoundProperties",
        "editStream",
        "defineAudioSource");
  }

  private static final ConcurrentHashMap<String, String> mapSounds = new ConcurrentHashMap<>();

  /**
   * Gets the SoundFunctions instance.
   *
   * @return the instance.
   */
  public static SoundFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    int psize = args.size();
    if (functionName.equalsIgnoreCase("playStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 5);
      if (!AppPreferences.playStreams.get()) return -1; // do nothing if disabled in preferences
      String strUri = convertToURI(args.get(0), true);

      Integer cycleCount = getCycleCount(functionName, args, 1);
      Double volume = getDouble(functionName, args, 2);
      Double start = getDouble(functionName, args, 3);
      Double stop = getDouble(functionName, args, 4);

      return MediaPlayerAdapter.playStream(strUri, cycleCount, volume, start, stop, false)
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    } else if (functionName.equalsIgnoreCase("playClip")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      if (!AppPreferences.playStreams.get()) return -1; // do nothing if disabled in preferences
      String strUri = convertToURI(args.get(0), true);

      Integer cycleCount = getCycleCount(functionName, args, 1);
      Double volume = getDouble(functionName, args, 2);
      return SoundManager.playClip(strUri, cycleCount, volume, false)
          ? BigDecimal.ONE
          : BigDecimal.ZERO;

    } else if (functionName.equalsIgnoreCase("editStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 2, 5);
      String strUri = convertToURI(args.get(0), true);

      Integer cycleCount = getCycleCount(functionName, args, 1);
      Double volume = getDouble(functionName, args, 2);
      Double start = getDouble(functionName, args, 3);
      Double stop = getDouble(functionName, args, 4);

      MediaPlayerAdapter.editStream(strUri, cycleCount, volume, start, stop);
      return "";
    } else if (functionName.equalsIgnoreCase("stopSound")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 3);
      String strUri = psize > 0 ? convertToURI(args.get(0), true) : "*";

      boolean del = psize > 1 ? FunctionUtil.paramAsBoolean(functionName, args, 1, true) : true;
      double fade = psize > 2 ? FunctionUtil.paramAsDouble(functionName, args, 2, true) : 0;
      stopSound(strUri, del, fade); // stop clip and/or stream
      return "";
    } else if (functionName.equalsIgnoreCase("getSoundProperties")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 1);
      String strUri = psize > 0 ? convertToURI(args.get(0), true) : "*";
      return getSoundProperties(strUri);
    } else if (functionName.equalsIgnoreCase("defineAudioSource")) {
      FunctionUtil.checkNumberParam(functionName, args, 2, 7);
      String nickName = args.get(0).toString();
      String strUri = convertToURI(args.get(1), false);
      defineSound(nickName, strUri);

      String preload = args.size() > 2 ? args.get(2).toString() : "none";
      if (!preload.equalsIgnoreCase("none")) {
        Integer cycleCount = getCycleCount(functionName, args, 3);
        Double volume = getDouble(functionName, args, 4);
        Double start = getDouble(functionName, args, 5);
        Double stop = getDouble(functionName, args, 6);

        if (preload.equalsIgnoreCase("clip")) {
          SoundManager.playClip(strUri, cycleCount, volume, true);
        } else if (preload.equalsIgnoreCase("stream")) {
          MediaPlayerAdapter.playStream(strUri, cycleCount, volume, start, stop, true);
        }
      }
      return "";
    }
    return null;
  }

  /**
   * Return the cycle count, or a null
   *
   * @param functionName the name of the function
   * @param args the arguments to the function
   * @param index the index of the cycleCount
   * @return an integer for the cycleCount or a null
   * @throws ParserException if the parameter is of incorrect type
   */
  private static Integer getCycleCount(String functionName, List<Object> args, int index)
      throws ParserException {
    if (args.size() > index && !args.get(index).equals("")) {
      return FunctionUtil.paramAsInteger(functionName, args, index, true);
    } else {
      return null;
    }
  }

  /**
   * Return the double of the parameter, or a null
   *
   * @param functionName the name of the function
   * @param args the arguments to the function
   * @param index the index of the parameter
   * @return a double, or a null if parameter is "" or not set
   * @throws ParserException if the parameter is of incorrect type
   */
  private static Double getDouble(String functionName, List<Object> args, int index)
      throws ParserException {
    if (args.size() > index && !args.get(index).equals("")) {
      return FunctionUtil.paramAsDouble(functionName, args, index, true);
    } else {
      return null;
    }
  }

  /**
   * Give a sound URI a nickname for easier access.
   *
   * @param nickName the nickname to use for the audio
   * @param strUri the String uri of the stream or clip
   */
  public static void defineSound(String nickName, String strUri) {
    mapSounds.put(nickName, strUri);
  }

  /**
   * Get a JSON containing the properties of the sound(s)
   *
   * @param strUri the String uri of the stream or clip. Can also be "*", "streams", or "clips".
   */
  private static Object getSoundProperties(String strUri) {
    switch (strUri.toLowerCase()) {
      case "streams":
        return MediaPlayerAdapter.getStreamProperties("*");
      case "clips":
        return SoundManager.getClipProperties("*");
      case "*":
        JsonArray all = (JsonArray) MediaPlayerAdapter.getStreamProperties("*");
        all.addAll((JsonArray) SoundManager.getClipProperties("*"));
        return all;
      default:
        Object streams = MediaPlayerAdapter.getStreamProperties(strUri);
        if (!"".equals(streams)) {
          return streams;
        } else {
          return SoundManager.getClipProperties(strUri);
        }
    }
  }

  /**
   * Stop a sound, either a stream, clip, or both
   *
   * @param strUri the String uri of the stream or clip. Can also be "*", "streams", or "clips".
   */
  private static void stopSound(String strUri, boolean remove, double fadeout) {
    switch (strUri.toLowerCase()) {
      case "streams":
        MediaPlayerAdapter.stopStream("*", remove, fadeout);
        break;
      case "clips":
        SoundManager.stopClip("*", remove);
        break;
      default:
        MediaPlayerAdapter.stopStream(strUri, remove, fadeout);
        SoundManager.stopClip(strUri, remove);
        break;
    }
  }

  /**
   * Convert a string into a uri string. Spaces are replaced by %20, among other things. The string
   * "*" is returned as-is
   *
   * @param string the string to convert
   * @param checkMapSounds should we check mapSounds for a defined sound?
   * @return the converted string
   */
  private static String convertToURI(Object string, boolean checkMapSounds) {
    String strUri = string.toString().trim();
    if (strUri.equals("*")
        || strUri.equalsIgnoreCase("clips")
        || strUri.equalsIgnoreCase("streams")) {
      return strUri;
    }
    if (checkMapSounds && mapSounds.containsKey(strUri)) {
      return mapSounds.get(strUri);
    }

    if (!isWeb(strUri) && !strUri.toUpperCase().startsWith("FILE")) {
      strUri = "FILE:/" + strUri;
    }

    try {
      String decodedURL = URLDecoder.decode(strUri, StandardCharsets.UTF_8);
      URL url = new URL(decodedURL);
      URI uri =
          new URI(
              url.getProtocol(),
              url.getUserInfo(),
              url.getHost(),
              url.getPort(),
              url.getPath(),
              url.getQuery(),
              url.getRef());
      return uri.toString();
    } catch (Exception ex) {
      return strUri;
    }
  }

  /**
   * Return the existence status of resource from String uri
   *
   * @param strUri the String uri of the resource
   * @return true if resource exists, false otherwise
   * @throws IOException if uri is url, but url is incorrect
   * @throws URISyntaxException if uri is for local file, but uri is incorrect
   */
  public static boolean uriExists(String strUri) throws IOException, URISyntaxException {
    return isWeb(strUri) ? urlExist(strUri) : fileExist(strUri);
  }

  /**
   * Returns true if the uri is for a web resource, false otherwise
   *
   * @param strUri the String uri of the resource
   * @return true if String uri is URL, false otherwise
   */
  private static boolean isWeb(String strUri) {
    String s = strUri.trim().toLowerCase();
    return s.startsWith("http://") || s.startsWith("https://");
  }

  /**
   * Return the existence status of web resource from String uri
   *
   * @param strUri the String uri of the resource
   * @return true if resource exists, false otherwise
   * @throws IOException if uri is incorrect
   */
  private static boolean urlExist(String strUri) throws IOException {
    HttpURLConnection.setFollowRedirects(false);
    HttpURLConnection con = (HttpURLConnection) new URL(strUri).openConnection();
    con.setRequestMethod("HEAD");
    return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
  }

  /**
   * Return the existence status of local resource from String uri
   *
   * @param strUri the String uri of the resource
   * @return true if resource exists, false otherwise
   * @throws URISyntaxException if uri is incorrect
   */
  private static boolean fileExist(String strUri) throws URISyntaxException {
    return new File(new URI(strUri).getPath()).exists();
  }

  /**
   * Return the nicknames associated with a string uri
   *
   * @param strUri the String uri of the resource
   * @return a list with the nicknames corresponding to the resource
   */
  public static List<String> getNicks(String strUri) {
    return mapSounds.entrySet().stream()
        .filter(entry -> strUri.equals(entry.getValue()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }
}
