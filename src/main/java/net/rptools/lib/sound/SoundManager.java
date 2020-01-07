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
package net.rptools.lib.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import net.rptools.maptool.client.functions.MediaPlayerAdapter;
import net.rptools.maptool.client.functions.SoundFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

/**
 * This class stores AudioClip for system sounds and sound events. Event sounds are played through
 * the playSoundEvent method.
 */
public class SoundManager {
  private static final Map<String, AudioClip> registeredSoundMap = new HashMap<>();
  private static final Map<String, AudioClip> soundEventMap = new HashMap<>();
  private static final Map<String, SoundManager> userSounds = new HashMap<>();

  private final String strUri;
  private final AudioClip clip;
  private int cycleCount; // store to remember last cycleCount used. Never 0.
  private double volume; // store because player volume also depends on global volume.

  private SoundManager(String strUri, Integer cycleCount, Double volume) {
    this.strUri = strUri;
    this.clip = new AudioClip(strUri);

    editClip(cycleCount, volume, true);
  }

  /**
   * Edit the SoundManager values, and update the AudioClip. Should be accessed from JavaFX app
   * thread. If a parameter is null and useDefault is false, no change * to that value. If null and
   * useDefault is true, change to the defaults.
   *
   * @param cycleCount how many times should the clip play (-1: infinite, default: 1)
   * @param volume the volume level of the clip (0-1, default: 1)
   */
  private void editClip(Integer cycleCount, Double volume, boolean useDefault) {
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
    updateClip(cycleCount != null && cycleCount == 0);
  }

  /**
   * Update the clip with the values in the adapter
   *
   * @param stopPlay should the clip be stopped
   */
  private void updateClip(boolean stopPlay) {
    this.clip.setCycleCount(this.cycleCount);
    this.clip.setVolume(this.volume * MediaPlayerAdapter.getGlobalVolume());

    if (stopPlay) {
      this.clip.stop();
    }
  }

  /**
   * Loads the sound list and register the system sounds.
   *
   * @param configPath The path for the sound resources
   * @throws IOException when configPath can't be read
   */
  public static void configure(String configPath) throws IOException {
    Properties props = new Properties();
    InputStream clipList = SoundManager.class.getClassLoader().getResourceAsStream(configPath);
    if (clipList == null) throw new IOException();
    props.load(clipList);
    configure(props);
  }

  /**
   * Register the system sounds from a Properties file.
   *
   * @param properties the property file
   */
  @SuppressWarnings("unchecked")
  public static void configure(Properties properties) {
    for (Enumeration<String> e = (Enumeration<String>) properties.propertyNames();
        e.hasMoreElements(); ) {
      String key = e.nextElement();
      registerSound(key, properties.getProperty(key));
    }
  }

  /**
   * Register a system sound from a path. If path incorrect or null, remove sound. Also add define
   * the sound to be used in SoundFunctions.
   *
   * @param name the name of the sound
   * @param path the path to the sound
   */
  public static void registerSound(String name, String path) {
    if (path != null && path.trim().length() == 0) path = null;

    URL url = path != null ? SoundManager.class.getClassLoader().getResource(path) : null;
    AudioClip clip = url != null ? new AudioClip(url.toExternalForm()) : null;

    if (clip != null) {
      registeredSoundMap.put(name, clip);
      SoundFunctions.defineSound(name, url.toExternalForm()); // add sound with defineAudioSource
    } else {
      registeredSoundMap.remove(name);
    }
  }

  /**
   * Return a registered sound.
   *
   * @param name the name of the sound
   * @return the audioclip of the sound
   */
  public static AudioClip getRegisteredSound(String name) {
    return registeredSoundMap.get(name);
  }

  /**
   * Associate a sound with an eventId.
   *
   * @param eventId a string for the eventId
   * @param clip the audio clip for the sound
   */
  public static void registerSoundEvent(String eventId, AudioClip clip) {
    soundEventMap.put(eventId, clip);
  }

  /**
   * Associate a blank sound with an eventId.
   *
   * @param eventId a string for the eventId
   */
  public static void registerSoundEvent(String eventId) {
    registerSoundEvent(eventId, null);
  }

  /**
   * Play the sound associated with the eventId.
   *
   * @param eventId a string for the eventId
   */
  public static void playSoundEvent(String eventId) {
    AudioClip clip = soundEventMap.get(eventId);
    double volume = MediaPlayerAdapter.getGlobalVolume();

    if (clip != null && !MediaPlayerAdapter.getGlobalMute() && volume > 0) {
      Platform.runLater(
          // Run on the JavaFX app thread
          new Runnable() {
            @Override
            public void run() {
              clip.play(volume);
            }
          });
    }
  }

  /**
   * Start a given clip from its url string.
   *
   * @param strUri the String url of the clip
   * @param cycleCount how many times should the clip play. -1: infinite
   * @param volume the volume level of the clip (0-1), null keeps previous value
   * @param preloadOnly should the stream be only preloaded and not played
   * @return false if the file doesn't exist, true otherwise
   * @throws ParserException if issue with file
   */
  public static boolean playClip(
      String strUri, Integer cycleCount, Double volume, boolean preloadOnly)
      throws ParserException {
    if (!userSounds.containsKey(strUri)) {
      try {
        if (!SoundFunctions.uriExists(strUri))
          return false; // leave without error message if uri ok but no file
      } catch (Exception e) {
        throw new ParserException(
            I18N.getText(
                "macro.function.sound.illegalargument",
                "playClip",
                strUri,
                e.getLocalizedMessage()));
      }
    }

    // run this on the JavaFX thread
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            SoundManager manager = userSounds.get(strUri);
            if (manager != null) {
              manager.editClip(cycleCount, volume, false);
            } else {
              manager = new SoundManager(strUri, cycleCount, volume);
              userSounds.put(strUri, manager);
            }
            AudioClip clip = manager.clip;

            double playVolume = manager.volume * MediaPlayerAdapter.getGlobalVolume();
            boolean play =
                (cycleCount == null || cycleCount != 0)
                    && playVolume > 0
                    && !MediaPlayerAdapter.getGlobalMute()
                    && !preloadOnly;
            if (play) {
              clip.play(playVolume);
            }
          }
        });
    return true;
  }

  /**
   * Stop a given clip from its url string.
   *
   * @param strUri the String url of the clip
   * @param remove should the clip be disposed
   */
  public static void stopClip(String strUri, boolean remove) {
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            if (strUri.equals("*")) {
              for (HashMap.Entry mapElement : userSounds.entrySet()) {
                ((SoundManager) mapElement.getValue()).clip.stop();
              }
              if (remove) userSounds.clear();
            } else {
              SoundManager manager = userSounds.get(strUri);
              if (manager != null) manager.clip.stop();
              if (remove) userSounds.remove(strUri);
            }
          }
        });
  }

  /**
   * Return the properties of a clip from its uri
   *
   * @param strUri the String uri of the clip
   * @return JsonObject for one clip, JsonArray of JsonObjects if all clips
   */
  public static Object getClipProperties(String strUri) {
    JsonObject info;
    if (strUri.equals("*")) {
      JsonArray infoArray = new JsonArray();
      for (HashMap.Entry mapElement : userSounds.entrySet()) {
        info = ((SoundManager) mapElement.getValue()).getInfo();
        if (info != null) infoArray.add(info);
      }
      return infoArray;
    } else {
      SoundManager manager = userSounds.get(strUri);
      if (manager == null) return "";
      else info = manager.getInfo();
      if (info == null) return "";
      else return info;
    }
  }

  /**
   * Return the properties of a clip
   *
   * @return JsonObject of the properties
   */
  private JsonObject getInfo() {
    try {
      JsonObject info = new JsonObject();

      List<String> listNicks = SoundFunctions.getNicks(this.strUri);
      if (listNicks.size() > 0) {
        info.addProperty("nicknames", String.join(",", listNicks));
      }
      info.addProperty("uri", this.strUri);
      info.addProperty("cycleCount", this.cycleCount);
      info.addProperty("volume", this.volume);
      String status = this.clip.isPlaying() ? "PLAYING" : "STOPPED";
      info.addProperty("status", status);
      info.addProperty("type", "clip");
      return info;
    } catch (Exception e) {
      return null;
    }
  }
}
