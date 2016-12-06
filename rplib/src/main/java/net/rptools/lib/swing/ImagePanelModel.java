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

import java.awt.Image;
import java.awt.Paint;
import java.awt.datatransfer.Transferable;

public interface ImagePanelModel {

	public int getImageCount();

	public Transferable getTransferable(int index);

	public Object getID(int index);

	public Image getImage(Object ID);

	public Image getImage(int index);

	public String getCaption(int index);

	public String getCaption(int index, boolean withDimensions);

	public Paint getBackground(int index);

	public Image[] getDecorations(int index);
}
