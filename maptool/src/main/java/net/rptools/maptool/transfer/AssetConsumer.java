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

package net.rptools.maptool.transfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Receiving end of AssetProducer
 * @author trevor
 */
public class AssetConsumer {
	private File destinationDir;
	private AssetHeader header;
	private long currentPosition;

	/**
	 * Create a new asset consumer, it will prepare a place to receive the incoming
	 * data chunks.  When complete the resulting file can be found at getFilename()
	 *  
	 * @param destinationDir - location to store the incoming file
	 * @param header - from the corresponding AssetProducer
	 */
	public AssetConsumer(File destinationDir, AssetHeader header) {
		if (header == null) {
			throw new IllegalArgumentException("Header cannot be null");
		}
		if (destinationDir == null) {
			destinationDir = new File(".");
		}
		this.destinationDir = destinationDir;
		this.header = header;
		// Setup
		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}
		// Cleanup
		if (getFilename().exists()) {
			getFilename().delete();
		}
	}

	/**
	 * Get the ID of the incoming asset
	 */
	public Serializable getId() {
		return header.getId();
	}

	public String getName() {
		return header.getName();
	}

	/**
	 * Add the next chunk of data to this consumer
	 * @param chunk produced from the corresponding AssetProducer
	 * @throws IOException
	 */
	public void update(AssetChunk chunk) throws IOException {
		File file = getFilename();
		FileOutputStream out = new FileOutputStream(file, true);
		byte[] data = chunk.getData();
		out.write(data);
		out.close();
		currentPosition += data.length;
	}

	/**
	 * Whether all the data has been transferred 
	 * @return
	 */
	public boolean isComplete() {
		return currentPosition >= header.getSize();
	}

	public double getPercentComplete() {
		return currentPosition / (double) header.getSize();
	}

	public long getSize() {
		return header.getSize();
	}

	/**
	 * When complete this will point to the file containing the data
	 * @return
	 */
	public File getFilename() {
		return new File(destinationDir.getAbsolutePath() + "/" + header.getId() + ".part");
	}
}
