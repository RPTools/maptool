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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.rptools.lib.image.ImageUtil;

public class ImageLabel {

	private BufferedImage labelBoxLeftImage;
	private BufferedImage labelBoxRightImage;
	private BufferedImage labelBoxMiddleImage;
	private int leftMargin = 4;
	private int rightMargin = 4;

	public ImageLabel(String labelImage, int leftMargin, int rightMargin) {
		this.leftMargin = leftMargin;
		this.rightMargin = rightMargin;

		try {
			parseImage(ImageUtil.getCompatibleImage(labelImage));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void renderLabel(Graphics2D g, int x, int y, int width, int height) {
		g.drawImage(labelBoxLeftImage, x, y, labelBoxLeftImage.getWidth(), height, null);
		g.drawImage(labelBoxRightImage, x + width - rightMargin, y, rightMargin, height, null);
		g.drawImage(labelBoxMiddleImage, x + leftMargin, y, width - rightMargin - leftMargin, height, null);
	}

	private void parseImage(BufferedImage image) {

		labelBoxLeftImage = image.getSubimage(0, 0, leftMargin, image.getHeight());
		labelBoxRightImage = image.getSubimage(image.getWidth() - rightMargin, 0, rightMargin, image.getHeight());
		labelBoxMiddleImage = image.getSubimage(leftMargin, 0, image.getWidth() - leftMargin - rightMargin, image.getHeight());
	}
}
