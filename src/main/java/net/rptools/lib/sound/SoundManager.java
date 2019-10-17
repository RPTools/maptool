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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import net.rptools.maptool.client.functions.MediaPlayerAdapter;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * This class stores AudioClip for system sounds and sound events. Event sounds are played through
 * the playSoundEvent method.
 */
public class SoundManager {
  private static final Map<String, AudioClip> registeredSoundMap = new HashMap<String, AudioClip>();
  private static final Map<String, AudioClip> soundEventMap = new HashMap<String, AudioClip>();
  private static final Map<String, AudioClip> userSounds = new HashMap<String, AudioClip>();

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
   * Register a system sound from a path. If path incorrect or null, remove sound.
   *
   * @param name the name of the sound
   * @param path the path to the sound
   */
  public static void registerSound(String name, String path) {
    if (path != null && path.trim().length() == 0) path = null;

    URL url = path != null ? SoundManager.class.getClassLoader().getResource(path) : null;
    AudioClip clip = url != null ? new AudioClip(url.toExternalForm()) : null;

    if (clip != null) registeredSoundMap.put(name, clip);
    else registeredSoundMap.remove(name);
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
              clip.setVolume(volume);
              clip.play();
            }
          });
    }
  }

  /**
   * Start a given clip from its url string.
   *
   * @param strUri the String url of the clip
   * @param cycleCount how many times should the clip play. -1: infinite
   * @param volume the volume level of the clip (0-1)
   * @return false if the file doesn't exist, true otherwise
   * @throws ParserException if issue with file
   */
  public static boolean playClip(String strUri, int cycleCount, double volume)
      throws ParserException {
    if (!userSounds.containsKey(strUri)) {
      try {
        if (!MediaPlayerAdapter.uriExists(strUri))
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
            AudioClip clip = userSounds.get(strUri);
            if (clip == null) {
              clip = new AudioClip(strUri);
              userSounds.put(strUri, clip);
            }
            double playVolume = volume * MediaPlayerAdapter.getGlobalVolume();
            if (cycleCount != 0 && playVolume > 0 && !MediaPlayerAdapter.getGlobalMute()) {
              clip.setCycleCount(cycleCount);
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
                ((AudioClip) mapElement.getValue()).stop();
              }
              if (remove) userSounds.clear();
            } else {
              AudioClip clip = userSounds.get(strUri);
              if (clip != null) clip.stop();
              if (remove) userSounds.remove(strUri);
            }
          }
        });
  }

  /**
   * Return the properties of a clip from its uri
   *
   * @param strUri the String uri of the clip
   * @return JSONObject for one clip, JSONArray of JSONObjects if all clips
   */
  public static Object getClipProperties(String strUri) {
    JSONObject info;
    if (strUri.equals("*")) {
      JSONArray infoArray = new JSONArray();
      for (HashMap.Entry mapElement : userSounds.entrySet()) {
        info = getInfo((String) mapElement.getKey());
        if (info != null) infoArray.add(info);
      }
      return infoArray;
    } else {
      info = getInfo(strUri);
      if (info == null) return "";
      else return info;
    }
  }

  /**
   * Return the properties of a clip
   *
   * @return JSONObject of the properties
   */
  private static JSONObject getInfo(String strUri) {
    AudioClip clip = userSounds.get(strUri);
    if (clip == null) return null;
    try {
      JSONObject info = new JSONObject();
      info.put("uri", strUri);
      String status = clip.isPlaying() ? "PLAYING" : "STOPPED";
      info.put("status", status);
      return info;
    } catch (Exception e) {
      return null;
    }
  }
}
