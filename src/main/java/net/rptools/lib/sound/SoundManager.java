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

/**
 * This class stores AudioClip for system sounds and sound events. Event sounds are played through
 * the playSoundEvent method.
 */
public class SoundManager {
  private final Map<String, AudioClip> registeredSoundMap = new HashMap<String, AudioClip>();
  private final Map<String, AudioClip> soundEventMap = new HashMap<String, AudioClip>();

  /**
   * Loads the sound list and register the system sounds.
   *
   * @param configPath The path for the sound resources
   * @throws IOException when configPath can't be read
   */
  public void configure(String configPath) throws IOException {
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
  public void configure(Properties properties) {
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
  public void registerSound(String name, String path) {
    if (path != null && path.trim().length() == 0) path = null;

    URL url = path != null ? getClass().getClassLoader().getResource(path) : null;
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
  public AudioClip getRegisteredSound(String name) {
    return registeredSoundMap.get(name);
  }

  /**
   * Associate a sound with an eventId.
   *
   * @param eventId a string for the eventId
   * @param clip the audio clip for the sound
   */
  public void registerSoundEvent(String eventId, AudioClip clip) {
    soundEventMap.put(eventId, clip);
  }

  /**
   * Associate a blank sound with an eventId.
   *
   * @param eventId a string for the eventId
   */
  public void registerSoundEvent(String eventId) {
    registerSoundEvent(eventId, null);
  }

  /**
   * Play the sound associated with the eventId.
   *
   * @param eventId a string for the eventId
   */
  public void playSoundEvent(String eventId) {
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
}
