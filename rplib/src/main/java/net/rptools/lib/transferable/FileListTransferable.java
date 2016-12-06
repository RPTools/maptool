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
import java.util.LinkedList;
import java.util.List;

public class FileListTransferable implements Transferable {

	public static final DataFlavor FLAVOR = new DataFlavor("application/x-java-file-list;class=java.util.List", null);

	private List<File> fileList;

	public FileListTransferable(List<File> fileList) {
		this.fileList = fileList;
	}

	public FileListTransferable(File file) {
		fileList = new LinkedList<File>();
		fileList.add(file);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(FLAVOR);
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

		if (!flavor.equals(FLAVOR)) {
			throw new UnsupportedFlavorException(flavor);
		}

		return fileList;
	}

}
