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
package net.rptools.lib.net;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import net.rptools.lib.FileUtil;

public class LocalLocation implements Location {

	private String localFile;

	public LocalLocation() {
		// For serialization
	}

	public LocalLocation(File file) {
		this.localFile = file.getAbsolutePath();
	}

	public File getFile() {
		return new File(localFile);
	}

	public InputStream getContent() throws IOException {
		return new BufferedInputStream(new FileInputStream(getFile()));
	}

	public void putContent(InputStream content) throws IOException {

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(getFile()));

			FileUtil.copyWithClose(content, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public void putContent(ImageWriter writer, BufferedImage content) throws IOException {
		FileImageOutputStream out = null;
		try {
			out = new FileImageOutputStream(getFile());

			writer.setOutput(out);
			writer.write(content);
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

	/*
	public void backgroundPutContent(ImageWriter writer, BufferedImage content) throws IOException {
		FileImageOutputStream out = null;
		// TODO: put this in another thread
		try {
	        out = new FileImageOutputStream(getFile());
	
	        writer.setOutput(out);
	        writer.write(content);
	    } finally {
	        if (out != null) {
	            out.close();
	        }
	    }
	}
	*/
}
