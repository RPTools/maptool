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
package net.rptools.lib.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileTransferableHandler extends TransferableHandler {
	private static final DataFlavor fileList = DataFlavor.javaFileListFlavor;

	@Override
	public List<URL> getTransferObject(Transferable transferable) throws IOException, UnsupportedFlavorException {
		if (transferable.isDataFlavorSupported(fileList)) {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>) transferable.getTransferData(fileList);
			List<URL> urls = new ArrayList<URL>(files.size());
			for (File file : files)
				urls.add(file.toURI().toURL());
			return urls;
		}
		throw new UnsupportedFlavorException(null);
	}
}
