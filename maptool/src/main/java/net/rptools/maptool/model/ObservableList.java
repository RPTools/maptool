/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

// TODO: Make this class implement 'List'
public class ObservableList<K> extends Observable implements Iterable {

	private List<K> list;

	public enum Event {
		add, append, remove, clear,
	}

	public ObservableList() {
		list = new ArrayList<K>();
	}

	public ObservableList(List<K> list) {
		assert list != null : "List cannot be null";

		this.list = list;
	}

	public List<K> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	public void sort(Comparator<K> comparitor) {
		Collections.sort(list, comparitor);
	}

	public boolean contains(K item) {
		return list.contains(item);
	}

	public int indexOf(K item) {
		return list.indexOf(item);
	}

	public K get(int i) {
		return list.get(i);
	}

	public void add(K item) {
		list.add(item);
		fireUpdate(Event.append, item);
	}

	public void add(int index, K element) {
		list.add(index, element);
		fireUpdate((index == list.size() ? Event.append : Event.add), element);
	}

	public void remove(K item) {
		list.remove(item);
		fireUpdate(Event.remove, item);
	}

	public void remove(int i) {
		K source = list.remove(i);
		fireUpdate(Event.remove, source);
	}

	public void clear() {
		list.clear();
		fireUpdate(Event.clear, null);
	}

	public int size() {
		return list.size();
	}

	////
	// INTERNAL
	protected void fireUpdate(Event event, K source) {
		setChanged();
		notifyObservers(event);
	}

	public class ObservableEvent {
		private Event event;
		private K source;

		public ObservableEvent(Event event, K source) {
			this.event = event;
			this.source = source;
		}

		public Event getEvent() {
			return event;
		}

		public K getSource() {
			return source;
		}
	}

	/**
	 * Get an iterator over the items in the list.
	 * 
	 * @return An iterator over the displayed list.
	 */
	public Iterator<K> iterator() {
		return list.iterator();
	}
}
