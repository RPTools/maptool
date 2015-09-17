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

package net.rptools.maptool.client.ui.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * This class encapsulates a single FTP request. The possibilities are GET or
 * PUT, and the parameters include the local object to be sent (more on that
 * later) as well as the remote filename and an optional remote directory name.
 * </p>
 * <p>
 * The local object to be sent can be an instance of {@link InputStream} in
 * which case the stream is read and transferred to the FTP server (a PUT
 * operation), or an instance of {@link OutputStream} if the data from the
 * server should be saved locally (a GET operation). The local object can also
 * be an instance of <code>String</code> in which case a {@link FileInputStream}
 * is constructed using the string as the filename. In all cases a reasonable
 * attempt is made to determine the amount of data to be transferred so that a
 * proper ProgressMonitor can be created and configured.
 * </p>
 * <p>
 * All FTPTransferObjects must specify a remote filename for both GET and PUT
 * operations. A remote directory name may also be specified. The
 * {@link FTPClient} implementation caches the remote directory name field,
 * attempting to create the directory whenever a change occurs. This is not
 * required behavior for users of this class.
 * </p>
 * 
 * @author crash
 * 
 */

public class FTPTransferObject {
	public Object local;
	public File remoteDir;
	public String remote;
	public Direction getput;
	public boolean complete;
	public int currentPosition, maximumPosition;

	public enum Direction {
		FTP_GET, FTP_PUT,
	};

	/**
	 * Construct an <code>FTPTransferObject</code> using the local object and
	 * the specified remote filename.
	 * 
	 * @param l
	 * @param r
	 */
	public FTPTransferObject(Direction updown, Object l, String r) {
		this(updown, l, null, r);
	}

	/**
	 * Construct an <code>FTPTransferObject</code> using the local object, the
	 * specified remote directory, and the specified remote filename.
	 * 
	 * @param l
	 * @param r
	 */
	public FTPTransferObject(Direction updown, Object l, File d, String r) {
		local = l;
		remoteDir = d;
		remote = r;
		getput = updown;
		complete = false;
	}

	/**
	 * Advance the ProgressBar to the next position.
	 */
	public void incrCurrentPosition() {
		currentPosition++;
		complete = (currentPosition >= maximumPosition);
	}

	public void setMaximum(int pos) {
		maximumPosition = pos;
		complete = (currentPosition >= maximumPosition);
	}
}
