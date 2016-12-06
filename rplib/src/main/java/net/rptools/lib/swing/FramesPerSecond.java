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
package net.rptools.lib.swing;

public class FramesPerSecond extends Thread {

	private int count = 0;
	private int lastFPS = 0;

	public FramesPerSecond() {
		setDaemon(true);
	}

	public void bump() {
		count++;
	}

	public int getFramesPerSecond() {
		return lastFPS;
	}

	@Override
	public void run() {
		while (true) {
			lastFPS = count;
			count = 0;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
}
