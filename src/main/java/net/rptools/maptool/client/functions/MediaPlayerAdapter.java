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
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import net.rptools.lib.sound.SoundManager;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

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

  private int cycleCount; // store to remember last cycleCount used. Never 0.
  private double volume; // store because player volume also depends on global volume
  private Duration start;
  private Duration stop; // can be null to not set stop time for player, needed to avoid bug

  /**
   * Set the values of the adapter, create the MediaPlayer, and set the properties of the
   * MediaPlayer
   *
   * @param strUri the String url of the stream
   * @param media the media to play
   * @param cycleCount the number of cycles (null: 1)
   * @param volume the volume level of the stream (0-1, null: 1)
   * @param start the start time in seconds (null: 0)
   * @param stop the stop time in seconds (null/negative: max length)
   */
  private MediaPlayerAdapter(
      String strUri, Media media, Integer cycleCount, Double volume, Double start, Double stop)
      throws MediaException {
    this.player = new MediaPlayer(media);

    this.strUri = strUri;
    this.media = media;
    editStream(cycleCount, volume, start, stop, true);

    this.player.setOnEndOfMedia(
        () -> {
          int curCount = player.getCurrentCount();
          int cycCount = player.getCycleCount();
          if (cycCount != MediaPlayer.INDEFINITE && curCount >= cycCount) {
            player.stop(); // otherwise, status stuck on "PLAYING" at end
            // make sure we use start property as editStream() above catches nulls
            player.seek(
                this.start); // fixes the problem with getStreamProperties()/currentTime #2658
          }
        });
  }

  /**
   * Edit the adapter values, and update the player. Should be accessed from JavaFX app thread. If a
   * parameter is null and useDefault is false, no change to that value. If null and useDefault is
   * true, change to the defaults.
   *
   * @param cycleCount how many times should the stream play (-1: infinite, default: 1)
   * @param volume the volume level of the stream (0-1, default: 1)
   * @param start the start time in seconds (default: 0)
   * @param stop the stop time in seconds (-1: file duration, default: -1)
   * @param useDefault if true, use default when receiving a null argument
   */
  private void editStream(
      Integer cycleCount, Double volume, Double start, Double stop, boolean useDefault) {
    // don't change adapter cycleCount if 0, but stop play.
    if (cycleCount != null && cycleCount != 0) {
      this.cycleCount = cycleCount;
    } else if (useDefault) {
      this.cycleCount = 1;
    }
    if (volume != null) {
      this.volume = volume;
    } else if (useDefault) {
      this.volume = 1.0;
    }
    if (start != null) {
      this.start = Duration.seconds(start);
    } else if (useDefault) {
      this.start = Duration.seconds(0.0);
    }
    if (stop != null) {
      this.stop = stop >= 0 ? Duration.seconds(stop) : player.getMedia().getDuration();
    } else if (useDefault) {
      this.stop = null;
    }
    updatePlayer(cycleCount != null && cycleCount == 0);
  }

  /**
   * Update the MediaPlayer with the values in the adapter
   *
   * @param stopPlay should the player be stopped
   */
  private void updatePlayer(boolean stopPlay) {
    int newCycle =
        this.cycleCount >= 0 ? this.cycleCount + player.getCurrentCount() : this.cycleCount;

    this.player.setCycleCount(newCycle);
    this.player.setVolume(this.volume * globalVolume);
    this.player.setStartTime(this.start);
    if (this.stop != null) this.player.setStopTime(this.stop);

    this.player.setMute(globalMute);
    if (stopPlay) {
      this.player.stop();
    }
  }

  /**
   * Start a given stream from its url string. If already streaming the file, dispose of the
   * previous stream.
   *
   * @param strUri the String url of the stream
   * @param cycleCount how many times should the stream play. -1: infinite
   * @param volume the volume level of the stream (0-1)
   * @param start the start time in ms
   * @param stop the stop time in ms, -1: file duration
   * @param preloadOnly should the stream be only preloaded and not played
   * @return false if the file doesn't exist, true otherwise
   * @throws ParserException if issue with file
   */
  public static boolean playStream(
      String strUri,
      Integer cycleCount,
      Double volume,
      Double start,
      Double stop,
      boolean preloadOnly)
      throws ParserException {
    final Media media;
    try {
      if (mapStreams.containsKey(strUri)) {
        media = mapStreams.get(strUri).media;
      } else {
        if (!SoundFunctions.uriExists(strUri))
          return false; // leave without error message if uri ok but no file
        media = new Media(strUri);
      }
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
        () -> {
          MediaPlayerAdapter adapter = mapStreams.get(strUri);
          boolean old = adapter != null;
          if (old) {
            adapter.editStream(cycleCount, volume, start, stop, false);
          } else {
            try {
              adapter = new MediaPlayerAdapter(strUri, media, cycleCount, volume, start, stop);
            } catch (MediaException e) {
              MapTool.showError(
                  I18N.getText("macro.function.sound.mediaexception", "playStream", strUri), e);
              return; // exit without playing stream. Fix sentry error #1564
            }
            mapStreams.put(strUri, adapter);
          }
          MediaPlayer player = adapter.player;

          // cycleCount of zero doesn't change adapter, but instead doesn't activate play
          boolean play = (cycleCount == null || cycleCount != 0) && !preloadOnly;
          if (play) {
            player.seek(player.getStartTime()); // start playing from the start
            if (old) player.play();
            else player.setAutoPlay(true);
          } else player.stop();
        });
    return true;
  }

  /**
   * Stop a given stream from its url string.
   *
   * @param strUri the String url of the stream
   * @param remove should the stream be disposed
   * @param fadeout time in seconds to fadeout (0: no fadeout)
   */
  public static void stopStream(String strUri, boolean remove, double fadeout) {
    Platform.runLater(
        () -> {
          if (strUri.equals("*")) {
            for (HashMap.Entry mapElement : mapStreams.entrySet())
              ((MediaPlayerAdapter) mapElement.getValue()).stopStream(remove, fadeout);
          } else {
            MediaPlayerAdapter adapter = mapStreams.get(strUri);
            if (adapter != null) adapter.stopStream(remove, fadeout);
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
   * @param fadeout time in seconds to fadeout (0: no fadeout)
   */
  private void stopStream(boolean remove, double fadeout) {
    if (fadeout <= 0) stopStream(remove);
    else {
      Timeline timeline =
          new Timeline(
              new KeyFrame(Duration.seconds(fadeout), new KeyValue(player.volumeProperty(), 0)));
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
        () -> {
          if (strUri.equals("*")) {
            for (HashMap.Entry mapElement : mapStreams.entrySet())
              ((MediaPlayerAdapter) mapElement.getValue())
                  .editStream(cycleCount, volume, start, stop, false);
          } else {
            MediaPlayerAdapter adapter = mapStreams.get(strUri);
            if (adapter != null) adapter.editStream(cycleCount, volume, start, stop, false);
          }
        });
  }

  /**
   * Return the properties of a stream from its uri
   *
   * @param strUri the String uri of the stream
   * @return JsonObject for one stream, JsonArray of JsonObjects if all streams
   */
  public static Object getStreamProperties(String strUri) {
    JsonObject info;
    if (strUri.equals("*")) {
      JsonArray infoArray = new JsonArray();
      for (HashMap.Entry mapElement : mapStreams.entrySet()) {
        info = ((MediaPlayerAdapter) mapElement.getValue()).getInfo();
        if (info != null) infoArray.add(info);
      }
      return infoArray;
    } else {
      MediaPlayerAdapter adapter = mapStreams.get(strUri);
      if (adapter == null) {
        return "";
      } else {
        info = adapter.getInfo();
      }
      return Objects.requireNonNullElse(info, "");
    }
  }

  /**
   * Return the properties of a stream
   *
   * @return JsonObject of the properties
   */
  private JsonObject getInfo() {
    if (player.getStatus() == MediaPlayer.Status.UNKNOWN) {
      return null;
    }
    try {
      Duration durTotal = media.getDuration();
      Object objTotal;
      if (durTotal.equals(Duration.INDEFINITE)) {
        objTotal = -1;
      } else if (durTotal.equals(Duration.UNKNOWN)) {
        objTotal = "UNKNOWN";
      } else {
        objTotal = durTotal.toSeconds();
      }

      Duration durStop = player.getStopTime();
      Object objStop;
      if (durStop.equals(Duration.INDEFINITE) || durStop.equals(Duration.UNKNOWN)) {
        objStop = objTotal;
      } else {
        objStop = durStop.toSeconds();
      }

      JsonObject info = new JsonObject();

      List<String> listNicks = SoundFunctions.getNicks(strUri);
      if (listNicks.size() > 0) {
        info.addProperty("nicknames", String.join(",", listNicks));
      }
      info.addProperty("uri", strUri);
      info.addProperty("volume", volume);
      info.addProperty("cycleCount", cycleCount);
      info.addProperty("startTime", start.toSeconds());
      info.add("stopTime", JSONMacroFunctions.getInstance().asJsonElement(objStop));
      info.addProperty("currentTime", player.getCurrentTime().toSeconds());
      info.add("totalTime", JSONMacroFunctions.getInstance().asJsonElement(objTotal));
      info.addProperty("bufferTime", player.getBufferProgressTime().toSeconds());
      info.addProperty("currentCount", player.getCurrentCount());
      info.addProperty("endCount", player.getCycleCount());
      info.addProperty("status", player.getStatus().toString());
      info.addProperty("type", "stream");
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
        () -> {
          for (HashMap.Entry mapElement : mapStreams.entrySet())
            ((MediaPlayerAdapter) mapElement.getValue()).updateVolume();
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
        () -> {
          // mute / unmute all streams
          for (HashMap.Entry mapElement : mapStreams.entrySet()) {
            ((MediaPlayerAdapter) mapElement.getValue()).updateMute();
          }
          // if muting, stop all audio clips
          if (mute) {
            SoundManager.stopClip("*", false);
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
}
