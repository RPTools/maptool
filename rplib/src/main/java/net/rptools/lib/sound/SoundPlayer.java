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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SoundPlayer {

	private static ExecutorService playerThreadPool = Executors.newCachedThreadPool();
	private static AtomicInteger playerCount = new AtomicInteger();
	public static final String FILE_EXTENSION = "mp3";

	public static void play(File file) throws IOException {
		try {
			Player player = new Player(new FileInputStream(file));
			play(player);
		} catch (JavaLayerException jle) {
			throw new IOException(jle.toString());
		}
	}

	public static void play(URL url) throws IOException {
		try {
			Player player = new Player(url.openStream());
			play(player);
		} catch (JavaLayerException jle) {
			throw new IOException(jle.toString());
		}
	}

	public static void play(String sound) throws IOException {
		try {
			Player player = new Player(SoundPlayer.class.getClassLoader().getResourceAsStream(sound));
			play(player);
			player.close();
		} catch (JavaLayerException jle) {
			throw new IOException(jle.toString());
		} catch (NullPointerException npe) {
			throw new IOException("Could not find sound: " + sound);
		}
	}

	public static int stopAll() {
		int currentThreads = playerCount.get();
		playerThreadPool.shutdownNow();
		System.out.println("Shutdown? " + playerThreadPool.isShutdown());
		playerThreadPool.shutdown();
		System.out.println("Terminated? " + playerThreadPool.isTerminated());

		return currentThreads;
	}

	/**
	 * Wait for all sounds to stop playing (Mostly for testing purposes)
	 */
	public static void waitFor() {

		while (playerCount.get() > 0) {
			try {
				synchronized (playerCount) {
					playerCount.wait();
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	private static void play(final Player player) {
		playerCount.incrementAndGet();

		//Not sure how to use Future here?
		Future<?> f = playerThreadPool.submit(new Runnable() {
			public void run() {
				try {
					player.play();
					playerCount.decrementAndGet();
					synchronized (playerCount) {
						playerCount.notify();
					}
				} catch (JavaLayerException jle) {
					jle.printStackTrace();
				}
			}
		});
	}
}
