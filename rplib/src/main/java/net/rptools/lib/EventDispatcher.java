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
package net.rptools.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application level event dispatch.
 * @author trevor
 */
public class EventDispatcher {

	private Map<Enum<?>, List<AppEventListener>> listenerMap = new HashMap<Enum<?>, List<AppEventListener>>();

	public synchronized void registerEvents(Enum<?>[] ids) {
		for (Enum<?> e : ids) {
			registerEvent(e);
		}
	}

	public synchronized void registerEvent(Enum<?> id) {
		if (listenerMap.containsKey(id)) {
			throw new IllegalArgumentException("Event '" + id + "' is already registered.");
		}

		listenerMap.put(id, new ArrayList<AppEventListener>());
	}

	public synchronized void addListener(Enum<?> id, AppEventListener listener) {
		addListener(listener, id);
	}

	public synchronized void addListener(AppEventListener listener, Enum<?>... ids) {
		for (Enum<?> id : ids) {
			if (!listenerMap.containsKey(id)) {
				throw new IllegalArgumentException("Event '" + id + "' is not registered.");
			}

			List<AppEventListener> list = listenerMap.get(id);
			if (!list.contains(listener)) {
				list.add(listener);
			}
		}
	}

	public synchronized void fireEvent(Enum<?> id) {
		fireEvent(id, null, null, null);
	}

	public synchronized void fireEvent(Enum<?> id, Object source) {
		fireEvent(id, source, null, null);
	}

	public synchronized void fireEvent(Enum<?> id, Object source, Object newValue) {
		fireEvent(id, source, null, newValue);
	}

	public synchronized void fireEvent(Enum<?> id, Object source, Object oldValue, Object newValue) {
		if (!listenerMap.containsKey(id)) {
			throw new IllegalArgumentException("Event '" + id + "' is not registered.");
		}

		List<AppEventListener> list = listenerMap.get(id);
		for (AppEventListener listener : list) {
			listener.handleAppEvent(new AppEvent(id, source, oldValue, newValue));
		}
	}

}
