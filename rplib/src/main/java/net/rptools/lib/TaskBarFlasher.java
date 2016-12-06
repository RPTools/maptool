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

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class TaskBarFlasher {

	private static final int FLASH_DELAY = 500;

	private final BufferedImage flashImage;
	private final Image originalImage;
	private final Frame frame;

	private FlashThread flashThread;

	public TaskBarFlasher(Frame frame) {
		this.frame = frame;

		originalImage = frame.getIconImage();
		flashImage = new BufferedImage(originalImage.getWidth(null), originalImage.getHeight(null), BufferedImage.OPAQUE);
		Graphics g = flashImage.getGraphics();
		g.setColor(Color.blue);
		g.fillRect(0, 0, flashImage.getWidth(), flashImage.getHeight());
		g.drawImage(originalImage, 0, 0, null);
		g.dispose();
	}

	public synchronized void flash() {
		if (flashThread != null) {
			// Already flashing
			return;
		}

		flashThread = new FlashThread();
		flashThread.start();
	}

	private class FlashThread extends Thread {
		@Override
		public void run() {
			while (!frame.isFocused()) {
				try {
					Thread.sleep(FLASH_DELAY);
					frame.setIconImage(flashImage);
					Thread.sleep(FLASH_DELAY);
					frame.setIconImage(originalImage);
				} catch (InterruptedException ie) {
					// Just leave, whatever
					break;
				}
			}
			flashThread = null;
		}
	}
}
