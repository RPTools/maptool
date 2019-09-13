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
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * This class handles audio streaming through MediaPlayer. MediaPlayer methods must be run on the
 * JavaFX app thread to work, which is why Platform.runLater is used extensively.
 *
 * @since 1.5.5
 */
public class MediaPlayerAdapter {
  private static final ConcurrentHashMap<String, MediaPlayerAdapter> mapStreams =
      new ConcurrentHashMap<>();

  private static double globalVolume = 1;
  private static boolean globalMute = false;

  private final String strUri;
  private final Media media;
  private final MediaPlayer player;
  private Double volume; // stored because player volume also depends on global volume

  private MediaPlayerAdapter(String strUri, Media media) {
    this.strUri = strUri;
    this.media = media;
    this.player = new MediaPlayer(media);
    this.player.setOnEndOfMedia(
        new Runnable() {
          @Override
          public void run() {
            int curCount = player.getCurrentCount();
            int cycCount = player.getCycleCount();
            if (cycCount != MediaPlayer.INDEFINITE && curCount >= cycCount)
              player.stop(); // otherwise, status stuck on "PLAYING" at end
          }
        });
  };
  /**
   * Start a given stream from its url string. If already streaming the file, dispose of the
   * previous stream.
   *
   * @param strUri the String url of the stream
   * @param cycleCount how many times should the stream play. -1: infinite
   * @param volume the volume level of the stream (0-1)
   * @param start the start time in ms
   * @param stop the stop time in ms, -1: file duration
   * @return false if the file doesn't exist, true otherwise
   * @throws ParserException if issue with file
   */
  public static boolean playStream(
      String strUri, int cycleCount, double volume, double start, double stop)
      throws ParserException {
    final Media media;
    try {
      if (!uriExists(strUri)) return false; // leave without error message if uri ok but no file
      media = new Media(strUri);
    } catch (Exception e) {
      throw new ParserException(
          I18N.getText(
              "macro.function.sound.illegalargument",
              "playStream",
              strUri,
              e.getLocalizedMessage()));
    }

    // run this on the JavaFX thread
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            MediaPlayerAdapter adapter = mapStreams.get(strUri);
            boolean old = adapter != null;
            if (!old) {
              adapter = new MediaPlayerAdapter(strUri, media);
              mapStreams.put(strUri, adapter);
            }
            adapter.volume = volume;
            MediaPlayer player = adapter.player;

            int newCycle = cycleCount >= 0 ? cycleCount + player.getCurrentCount() : cycleCount;
            Duration durStart = new Duration(start);
            Duration durStop = stop >= 0 ? new Duration(stop) : player.getMedia().getDuration();

            player.setCycleCount(newCycle);
            player.setVolume(volume * globalVolume);
            player.setStartTime(durStart);
            player.setStopTime(durStop);

            player.setMute(globalMute);
            player.seek(durStart); // start playing from the start
            if (cycleCount != 0) {
              if (old) player.play();
              else player.setAutoPlay(true);
            } else player.stop();
          }
        });
    return true;
  }

  /**
   * Stop a given stream from its url string.
   *
   * @param strUri the String url of the stream
   * @param remove should the stream be disposed
   * @param fadeout time in ms to fadeout (0: no fadeout)
   */
  public static void stopStream(String strUri, boolean remove, double fadeout) {
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            if (strUri.equals("*")) {
              for (HashMap.Entry mapElement : mapStreams.entrySet())
                ((MediaPlayerAdapter) mapElement.getValue()).stopStream(remove, fadeout);
            } else {
              MediaPlayerAdapter adapter = mapStreams.get(strUri);
              if (adapter != null) adapter.stopStream(remove, fadeout);
            }
          }
        });
  }

  /**
   * Stop the stream. Should be ran from JavaFX app thread.
   *
   * @param remove should the stream be disposed and map updated
   */
  private void stopStream(boolean remove) {
    if (remove) {
      player.dispose();
      mapStreams.remove(this.strUri);
    } else player.stop();
  }

  /**
   * Stop the stream. Should be ran from JavaFX app thread.
   *
   * @param remove should the stream be disposed and map updated
   * @param fadeout time in ms to fadeout (0: no fadeout)
   */
  private void stopStream(boolean remove, double fadeout) {
    if (fadeout <= 0) stopStream(remove);
    else {
      Timeline timeline =
          new Timeline(
              new KeyFrame(Duration.millis(fadeout), new KeyValue(player.volumeProperty(), 0)));
      timeline.setOnFinished(
          (event -> {
            stopStream(remove); // stop the stream at the end
          }));
      timeline.play();
    }
  }

  /**
   * Edit a given stream from its url string. If a parameter is null, no change to that value.
   *
   * @param strUri the String uri of the stream
   * @param cycleCount how many times should the stream play. -1: infinite
   * @param volume the volume level of the stream (0-1)
   * @param start the start time in ms
   * @param stop the stop time in ms, -1: file duration
   */
  public static void editStream(
      String strUri, Integer cycleCount, Double volume, Double start, Double stop) {
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            if (strUri.equals("*")) {
              for (HashMap.Entry mapElement : mapStreams.entrySet())
                ((MediaPlayerAdapter) mapElement.getValue())
                    .editStream(cycleCount, volume, start, stop);
            } else {
              MediaPlayerAdapter adapter = mapStreams.get(strUri);
              if (adapter != null) adapter.editStream(cycleCount, volume, start, stop);
            }
          }
        });
  }

  /**
   * Edit the stream. Should be accessed from JavaFX app thread. If a parameter is null, no change
   * to that value.
   *
   * @param cycleCount how many times should the stream play. -1: infinite
   * @param volume the volume level of the stream (0-1)
   * @param start the start time in ms
   * @param stop the stop time in ms, -1: file duration
   */
  private void editStream(Integer cycleCount, Double volume, Double start, Double stop) {
    if (cycleCount != null) {
      if (cycleCount == 0) player.stop();
      else {
        int newCycle = cycleCount >= 0 ? cycleCount + player.getCurrentCount() : cycleCount;
        player.setCycleCount(newCycle);
      }
    }
    if (volume != null) {
      this.volume = volume;
      player.setVolume(volume * globalVolume);
    }
    if (start != null) player.setStartTime(new Duration(start));
    if (stop != null) {
      Duration durStop = stop >= 0 ? new Duration(stop) : Duration.INDEFINITE;
      player.setStopTime(durStop);
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
   * Return the properties of a stream from its uri
   *
   * @param strUri the String uri of the stream
   * @return JSONObject for one stream, JSONArray of JSONObjects if all streams
   */
  public static Object getStreamProperties(String strUri) {
    JSONObject info;
    if (strUri.equals("*")) {
      JSONArray infoArray = new JSONArray();
      for (HashMap.Entry mapElement : mapStreams.entrySet()) {
        info = ((MediaPlayerAdapter) mapElement.getValue()).getInfo();
        if (info != null) infoArray.add(info);
      }
      return infoArray;
    } else {
      MediaPlayerAdapter adapter = mapStreams.get(strUri);
      if (adapter == null) return "";
      else info = adapter.getInfo();
      if (info == null) return "";
      else return info;
    }
  }

  /**
   * Return the properties of a stream
   *
   * @return JSONObject of the properties
   */
  private JSONObject getInfo() {
    if (player.getStatus() == MediaPlayer.Status.UNKNOWN) return null;
    try {
      Duration durTotal = media.getDuration();
      Object objTotal;
      if (durTotal.equals(Duration.INDEFINITE)) objTotal = -1;
      else if (durTotal.equals(Duration.UNKNOWN)) objTotal = "UNKNOWN";
      else objTotal = durTotal.toSeconds();

      Duration durStop = player.getStopTime();
      Object objStop;
      if (durStop.equals(Duration.INDEFINITE) || durStop.equals(Duration.UNKNOWN))
        objStop = objTotal;
      else objStop = durStop.toSeconds();

      JSONObject info = new JSONObject();
      info.put("uri", strUri);
      info.put("cycleCount", player.getCycleCount());
      info.put("volume", volume);
      info.put("startTime", " " + player.getStartTime().toSeconds());
      info.put("stopTime", " " + objStop);
      info.put("currentTime", " " + player.getCurrentTime().toSeconds());
      info.put("totalTime", " " + objTotal);
      info.put("bufferTime", " " + player.getBufferProgressTime().toSeconds());
      info.put("currentCount", " " + player.getCurrentCount());
      info.put("status", player.getStatus().toString());
      return info;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Set the global volume, and update the volume of all players
   *
   * @param volume the global volume (0-1)
   */
  public static void setGlobalVolume(double volume) {
    globalVolume = volume;

    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            for (HashMap.Entry mapElement : mapStreams.entrySet())
              ((MediaPlayerAdapter) mapElement.getValue()).updateVolume();
          }
        });
  }

  /** Update the volume of the stream */
  private void updateVolume() {
    player.setVolume(volume * globalVolume);
  }

  /**
   * Get the global volume
   *
   * @return the global volume (0-1)
   */
  public static double getGlobalVolume() {
    return globalVolume;
  }

  /**
   * Set the global mute status
   *
   * @param mute the mute status
   */
  public static void setGlobalMute(boolean mute) {
    globalMute = mute;
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            for (HashMap.Entry mapElement : mapStreams.entrySet())
              ((MediaPlayerAdapter) mapElement.getValue()).updateMute();
          }
        });
  }

  /** Update the mute of the stream */
  private void updateMute() {
    player.setMute(globalMute);
  }
  /**
   * Get the global mute status
   *
   * @return the mute status
   */
  public static boolean getGlobalMute() {
    return globalMute;
  }

  /**
   * Convert a string into a uri string. Spaces are replaced by %20, among other things. The string
   * "*" is returned as-is
   *
   * @param string the string to convert
   * @return the converted string
   */
  public static String convertToURI(Object string) {
    String strUri = string.toString().trim();
    if (strUri.equals("*")) return strUri;
    if (!isWeb(strUri) && !strUri.toUpperCase().startsWith("FILE")) {
      strUri = "FILE:/" + strUri;
    }

    try {
      String decodedURL = URLDecoder.decode(strUri, "UTF-8");
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
}
