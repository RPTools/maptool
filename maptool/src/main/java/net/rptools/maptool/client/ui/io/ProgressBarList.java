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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import net.rptools.lib.swing.SwingUtil;

/**
 * <p>
 * This class produces a modal dialog that shows the progress being made by FTP transfers.
 * </p>
 * <p>
 * It builds a panel and places it inside the dialog.  Each entry in the dialog is a panel with
 * exactly the same size as all other panels.  The panel contains a ProgressBar on the
 * left and a JLabel on the right (to hold the remote filename).
 * </p>
 * @author crash
 */
@SuppressWarnings("serial")
public class ProgressBarList extends JDialog implements ChangeListener {
	private static final Logger log = Logger.getLogger(ProgressBarList.class);
	/*
	 * The general layout approach for this class is to put a BorderLayout on the
	 * main window.  GridLayout is added to a panel that becomes the CENTER
	 * and the SOUTH is used for a Hide and Cancel button.
	 */
	private FTPClient ftp;
	private JPanel progressBars;
	private JCheckBox autohide;
	private JButton hideButton, cancelButton;
	private JLabel countDown;
	private GridLayout grid;
	private Map<FTPTransferObject, JProgressBar> bars;
	private boolean cancelling;
	private int numFiles;

	public ProgressBarList(JFrame frame, FTPClient f, int num) {
		super(frame, "FTP Progress - " + num + " file(s) to process", false);
		setLayout(new BorderLayout());
		ftp = f;
		cancelling = false;
		numFiles = num;

		int thrds = ftp.getNumberOfThreads();
		bars = new HashMap<FTPTransferObject, JProgressBar>(thrds);

		grid = new GridLayout(0, 1, 2, 6); // Always just a single column
		progressBars = new JPanel(grid);
		add(progressBars, BorderLayout.CENTER);

		// Any time the processing queue is updated (such as a new upload or download being
		// added) or any time one of the items in the queue progresses to the next milestone,
		// the event will fire.
		ftp.addChangeListener(this);

		hideButton = new JButton("Hide");
		hideButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProgressBarList.this.setVisible(false);
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelling = true;
				JButton btn = (JButton) e.getSource();
				btn.setText("Cancelling...");
				ftp.setEnabled(false);
				ftp.removeAllFromQueue();
			}
		});
		autohide = new JCheckBox("Close this window on completion");
		countDown = new JLabel(numFiles + " file(s) remaining");
		JPanel buttons = new JPanel();
		buttons.add(autohide);
		buttons.add(hideButton);
		buttons.add(cancelButton);
		buttons.add(countDown);
		add(buttons, BorderLayout.SOUTH);
		pack();
		SwingUtil.centerOver(this, frame);
		setVisible(true);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == ftp) {
			// This event is sent when the FTPClient has no more data in the 'todo' queue
			// and there are no additional operations pending.  We use our checkbox to
			// determine if we should auto-hideButton.
			progressBars.removeAll();
			cancelButton.setEnabled(false);
			hideButton.setText("Close");
			if (autohide.isSelected())
				setVisible(false);
		} else {
			FTPTransferObject fto = (FTPTransferObject) e.getSource();
			JProgressBar bar;
			if (fto.complete) {
				// Transfer complete so remove from panel and allow the slot to be reused.
				bar = bars.remove(fto);
				if (bar != null) {
					bar.setValue(fto.currentPosition);
					countDown.setText(--numFiles + " file(s) remaining");
					progressBars.remove(bar);
				}
				if (cancelling) {
					// If it's a cancelButton request, delete the completely transferred file.
					log.error("Canceled; removing " + fto.remoteDir + fto.remote);
					ftp.remove(fto.remoteDir + "/" + fto.remote);
				}
			} else if (bars.containsKey(fto)) {
				bar = bars.get(fto);
				bar.setValue(fto.currentPosition);
			} else {
				// New FTO that's not being shown yet...
				bar = new JProgressBar();
				bar.setStringPainted(true);
				//				bar.setString(fto.remoteDir + fto.remote);
				bar.setMaximum(fto.maximumPosition);
				bars.put(fto, bar);
				progressBars.add(bar);
				pack();
			}
		}
	}
}
