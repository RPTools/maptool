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
import java.util.concurrent.ConcurrentHashMap;
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
  /** Maps a registered system AudioClip to its name. */
  private static final Map<String, AudioClip> registeredSoundMap = new HashMap<>();

  /** Maps a registered system AudioClip to its event. */
  private static final Map<String, AudioClip> soundEventMap = new HashMap<>();

  /** Maps a SoundManager to the string of its URI. */
  private static final Map<String, SoundManager> userSounds = new ConcurrentHashMap<>();

  /** String of the URI of the sound. */
  private final String strUri;

  /** AudioClip that can play the sound. */
  private final AudioClip clip;

  /** Last cycleCount used. */
  private int cycleCount;

  /** Volume specific to the sound, independently of global sound. */
  private double volume;

  /**
   * Initializes the SoundManager.
   *
   * @param strUri the URI of the sound
   * @param cycleCount the number of times to play the sound (-1: loop, default: 1)
   * @param volume the volume of the sound (0-1, default: 1)
   */
  private SoundManager(String strUri, Integer cycleCount, Double volume) {
    this.strUri = strUri;
    this.clip = new AudioClip(strUri);
    this.cycleCount = cycleCount != null ? cycleCount : 1;
    this.volume = volume != null ? volume : 1.0;
  }

  /**
   * Edit the SoundManager values. If a parameter is null, no change to that value.
   *
   * @param cycleCount how many times should the clip play (-1: infinite)
   * @param volume the volume level of the clip (0-1)
   */
  private void editClip(Integer cycleCount, Double volume) {
    if (cycleCount != null) {
      this.cycleCount = cycleCount;
    }
    if (volume != null) {
      this.volume = volume;
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
    if (clipList == null) {
      throw new IOException();
    }
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
    if (path != null && path.trim().length() == 0) {
      path = null;
    }

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
   * Play the sound associated with the eventId.
   *
   * @param eventId a string for the eventId
   */
  public static void playSoundEvent(String eventId) {
    AudioClip clip = soundEventMap.get(eventId);
    double volume = MediaPlayerAdapter.getGlobalVolume();

    if (clip != null && !MediaPlayerAdapter.getGlobalMute() && volume > 0) {
      // Run on the JavaFX app thread
      Platform.runLater(() -> clip.play(volume));
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
    final SoundManager manager = userSounds.get(strUri);

    // Verify the uri and return an error if uri is incorrect
    if (manager == null) {
      try {
        if (!SoundFunctions.uriExists(strUri)) {
          // leave without error message if uri ok but no file
          return false;
        }
      } catch (Exception e) {
        String key = "macro.function.sound.illegalargument";
        throw new ParserException(I18N.getText(key, "playClip", strUri, e.getMessage()));
      }
      new Thread(
              () -> {
                // Creates a SoundManager on another thread, as it can be slow.
                SoundManager newManager = new SoundManager(strUri, cycleCount, volume);
                userSounds.put(strUri, newManager);
                if (!preloadOnly) {
                  Platform.runLater(newManager::playClip);
                }
              })
          .start();
    } else {
      manager.editClip(cycleCount, volume);
      if (!preloadOnly) {
        Platform.runLater(manager::playClip);
      }
    }
    return true;
  }

  /** Play the clip with appropriate volume and cycleCount. To be ran on the JavaFX App thread. */
  private void playClip() {
    double playVolume = this.volume * MediaPlayerAdapter.getGlobalVolume();
    if (cycleCount != 0 && playVolume > 0 && !MediaPlayerAdapter.getGlobalMute()) {
      clip.setCycleCount(this.cycleCount);
      clip.play(playVolume);
    }
  }

  /**
   * Stop a given clip from its url string.
   *
   * @param strUri the String url of the clip
   * @param remove should the clip be disposed
   */
  public static void stopClip(String strUri, boolean remove) {
    Platform.runLater(
        () -> {
          if (strUri.equals("*")) {
            for (SoundManager manager : userSounds.values()) {
              manager.clip.stop();
            }
            if (remove) {
              userSounds.clear();
            }
          } else {
            SoundManager manager = userSounds.get(strUri);
            if (manager != null) {
              manager.clip.stop();
            }
            if (remove) {
              userSounds.remove(strUri);
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
      for (SoundManager manager : userSounds.values()) {
        info = manager.getInfo();
        if (info != null) {
          infoArray.add(info);
        }
      }
      return infoArray;
    } else {
      SoundManager manager = userSounds.get(strUri);
      if (manager == null) {
        return "";
      } else {
        info = manager.getInfo();
        return info != null ? info : "";
      }
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
