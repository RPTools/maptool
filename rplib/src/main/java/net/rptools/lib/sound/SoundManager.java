/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib.sound;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SoundManager {
	private final Map<String, URL> registeredSoundMap = new HashMap<String, URL>();
	private final Map<String, URL> soundEventMap = new HashMap<String, URL>();

	public void configure(String configPath) throws IOException {
		Properties props = new Properties();
		props.load(SoundManager.class.getClassLoader().getResourceAsStream(configPath));
		configure(props);
	}

	@SuppressWarnings("unchecked")
	public void configure(Properties properties) {
		for (Enumeration<String> e = (Enumeration<String>) properties.propertyNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			registerSound(key, properties.getProperty(key));
		}
	}

	/**
	 * These represent the built-in sounds. This is different than user contributed sounds which have an actual path to
	 * them.
	 */
	public void registerSound(String name, URL sound) {
		if (sound != null) {
			registeredSoundMap.put(name, sound);
		} else {
			registeredSoundMap.remove(name);
		}
	}

	/**
	 * These represent the built-in sounds. This is different than user contributed sounds which have an actual path to
	 * them. The file is pulled from the class path.
	 */
	public void registerSound(String name, String path) {
		if (path != null && path.trim().length() == 0) {
			path = null;
		}
		registerSound(name, path != null ? SoundManager.class.getClassLoader().getResource(path) : null);
	}

	public URL getRegisteredSound(String name) {
		return registeredSoundMap.get(name);
	}

	/**
	 * A sound event plays the sound associated with the event ID, this adds a new event type
	 */
	public void registerSoundEvent(String eventId, URL sound) {
		soundEventMap.put(eventId, sound);
	}

	/**
	 * A sound event plays the sound associated with the event ID, this adds a new event type
	 */
	public void registerSoundEvent(String eventId) {
		registerSoundEvent(eventId, null);
	}

	/**
	 * Play the sound associated with the eventId
	 */
	public void playSoundEvent(String eventId) {
		URL sound = soundEventMap.get(eventId);

		if (sound != null) {
			try {
				SoundPlayer.play(sound);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
