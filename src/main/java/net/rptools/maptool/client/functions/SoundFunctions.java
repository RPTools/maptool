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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class SoundFunctions extends AbstractFunction {

  /** The singleton instance. */
  private static final SoundFunctions instance = new SoundFunctions();

  private SoundFunctions() {
    super(0, 3, "playStream", "stopStream", "editStream");
  }

  private static HashMap<String, MediaPlayer> mapStreams = new HashMap<>();

  /**
   * Gets the SoundFunctions instance.
   *
   * @return the instance.
   */
  public static SoundFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    int psize = args.size();
    if (functionName.equalsIgnoreCase("playStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      if (!AppPreferences.getPlayStreams()) return -1; // do nothing if disabled in preferences
      String strUrl = args.get(0).toString();
      int cycleCount = psize > 1 ? FunctionUtil.paramAsInteger(functionName, args, 1, true) : 1;
      double volume = psize > 2 ? FunctionUtil.paramAsDouble(functionName, args, 2, true) : 1;
      return playStream(strUrl, cycleCount, volume) ? BigDecimal.ONE : BigDecimal.ZERO;
    } else if (functionName.equalsIgnoreCase("editStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 3);
      String strUrl = psize > 0 ? args.get(0).toString() : "*";
      int cycleCount = psize > 1 ? FunctionUtil.paramAsInteger(functionName, args, 1, true) : 1;
      double volume = psize > 2 ? FunctionUtil.paramAsDouble(functionName, args, 2, true) : 1;
      editStream(strUrl, cycleCount, volume);
      return "";
    } else if (functionName.equalsIgnoreCase("stopStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      String strUrl = psize > 0 ? args.get(0).toString() : "*";
      boolean del = psize > 1 ? FunctionUtil.paramAsBoolean(functionName, args, 1, true) : true;
      stopStream(strUrl, del);
      return "";
    }
    return null;
  }

  /**
   * Start a given stream from its url string. If already streaming the file, dispose of the
   * previous stream.
   *
   * @param strUrl the String url of the stream
   * @param cycleCount how many times should the stream play
   * @param volume the volume level of the stream (0-1)
   * @return false if the file doesn't exist, true otherwise
   * @throws ParserException if issue with file
   */
  private static boolean playStream(String strUrl, int cycleCount, double volume)
      throws ParserException {
    final Media media;
    try {
      if (!uriExists(strUrl)) return false; // leave without error message if uri ok but no file
      media = new Media(strUrl);
    } catch (Exception e) {
      throw new ParserException(
          I18N.getText(
              "macro.function.sound.illegalargument",
              "playStream",
              strUrl,
              e.getLocalizedMessage()));
    }

    // run this on the JavaFX thread
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            if (mapStreams.containsKey(strUrl))
              mapStreams.get(strUrl).dispose(); // dispose previous stream of the same name

            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true); // start stream automatically

            mediaPlayer.setCycleCount(cycleCount);
            mediaPlayer.setVolume(volume);
            mapStreams.put(strUrl, mediaPlayer); // store player for later use
          }
        });
    return true;
  }

  /**
   * Stop a given stream from its url string.
   *
   * @param strUrl the String url of the stream
   * @param remove should the stream be disposed
   */
  public static void stopStream(String strUrl, boolean remove) {
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            if (strUrl.equals("*")) {
              for (HashMap.Entry mapElement : mapStreams.entrySet())
                fxStopStream((String) mapElement.getKey(), remove);
            } else {
              fxStopStream(strUrl, remove);
            }
          }
        });
  }

  /**
   * Stop a given stream from its url string. Should be ran from JavaFX app thread.
   *
   * @param strUrl the String url of the stream
   * @param remove should the stream be disposed
   */
  private static void fxStopStream(String strUrl, boolean remove) {
    if (mapStreams.containsKey(strUrl)) {
      mapStreams.get(strUrl).stop(); // stop previous stream of the same name
      if (remove) {
        mapStreams.get(strUrl).dispose();
        mapStreams.remove(strUrl);
      }
    }
  }

  /**
   * Edit a given stream from its url string.
   *
   * @param strUrl the String url of the stream
   * @param cycleCount how many times should the stream play
   * @param volume the volume level of the stream (0-1)
   */
  private static void editStream(String strUrl, int cycleCount, double volume) {
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            if (strUrl.equals("*")) {
              for (HashMap.Entry mapElement : mapStreams.entrySet())
                fxEditStream((String) mapElement.getKey(), cycleCount, volume);
            } else {
              fxEditStream(strUrl, cycleCount, volume);
            }
          }
        });
  }

  /**
   * Edit a given stream from its url string. Should be accessed from JavaFX app thread.
   *
   * @param strUrl the String url of the stream
   * @param cycleCount how many times should the stream play
   * @param volume the volume level of the stream (0-1)
   */
  private static void fxEditStream(String strUrl, int cycleCount, double volume) {
    if (mapStreams.containsKey(strUrl)) {
      MediaPlayer mediaPlayer = mapStreams.get(strUrl);
      mediaPlayer.setCycleCount(cycleCount);
      mediaPlayer.setVolume(volume);
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
}
