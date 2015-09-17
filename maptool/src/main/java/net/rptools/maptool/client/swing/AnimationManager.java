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

package net.rptools.maptool.client.swing;

import java.util.ArrayList;
import java.util.List;

/**
 * @author trevor
 */
public class AnimationManager {

	private static List<Animatable> animatableList = new ArrayList<Animatable>();

	private static List<Animatable> removeList = new ArrayList<Animatable>();
	private static List<Animatable> addList = new ArrayList<Animatable>();

	private static int delay = 200;

	static {
		new AnimThread().start();
	}

	public static void addAnimatable(Animatable animatable) {

		synchronized (animatableList) {
			if (!animatableList.contains(animatable)) {
				addList.add(animatable);
			}
		}
	}

	public static void removeAnimatable(Animatable animatable) {

		synchronized (animatableList) {
			removeList.remove(animatable);
		}
	}

	private static class AnimThread extends Thread {

		public void run() {

			while (true) {

				if (animatableList.size() > 0) {

				}

				synchronized (animatableList) {

					animatableList.addAll(addList);
					addList.clear();

					for (Animatable animatable : animatableList) {
						animatable.animate();
					}

					animatableList.removeAll(removeList);
					removeList.clear();
				}

				try {
					Thread.sleep(delay);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}
}
