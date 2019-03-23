/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.io;

/** @author crash */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.rptools.maptool.client.ui.io.FTPTransferObject.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTPClient {
  private static final Logger log = LogManager.getLogger(FTPClient.class);

  protected FTPClientConn cconn;

  protected List<Object> fifoQueue;
  protected Map<Object, FTPTransferObject> todoMap; // Todo list for uploads
  protected Map<Object, FTPTransferObject> transferringMap; // Currently in process...

  private int numThreads = 1;
  private List<ChangeListener> changeListeners;
  private boolean running;

  public FTPClient(String _host, String _user, String _password) {
    cconn = new FTPClientConn(_host, _user, _password);

    fifoQueue = new LinkedList<Object>();
    todoMap = new HashMap<Object, FTPTransferObject>();
    transferringMap = new HashMap<Object, FTPTransferObject>();
  }

  /**
   * This method enables or disables the processing of transfer objects in the queue.
   *
   * <p>If this method is called with a <code>true</code> parameter, the object begins processing
   * FTP requests as soon as they are added to the queue, up to the maximum number of simultaneous
   * transfers as set by a call to {@link #setNumberOfThreads(int)}.
   *
   * <p>If this method is called with a <code>false</code> parameter, transfer requests will be
   * added to the queue but no transfers in the queue will be started. If there are existing
   * transfers already in progress, they will continue but the queue will not be read to process
   * additional ones.
   *
   * <p>
   *
   * @param b whether this object should process transfer requests from the queue
   */
  public void setEnabled(boolean b) {
    boolean old = running;
    running = b;
    if (old != b && b == true) {
      // We just enabled this object from a disabled state, so start the first transfer
      startNextTransfer();
    }
  }

  public synchronized void addChangeListener(ChangeListener listener) {
    getChangeListeners().add(listener);
  }

  public synchronized void removeChangeListener(ChangeListener listener) {
    getChangeListeners().remove(listener);
  }

  public synchronized List<ChangeListener> getChangeListeners() {
    if (changeListeners == null) changeListeners = new LinkedList<ChangeListener>();
    return changeListeners;
  }

  /**
   * This method may be called by threads other than the EventDispatch thread, so we use <code>
   * SwingUtilities.invokeLater()</code> to handle running it on the AWT EDT.
   *
   * @param data
   */
  protected void fireStateChanged(final Object data) {
    if (SwingUtilities.isEventDispatchThread()) postAllChangeEvents(data);
    else
      SwingUtilities.invokeLater(
          new Runnable() {
            public void run() {
              postAllChangeEvents(data);
            }
          });
  }

  private void postAllChangeEvents(Object fto) {
    ChangeEvent ev = new ChangeEvent(fto);
    for (ChangeListener listener : getChangeListeners()) {
      listener.stateChanged(ev);
    }
  }

  public int mkdir(String dir) {
    return cconn.mkdir(dir);
  }

  public int remove(String filename) {
    return cconn.remove(filename);
  }

  public void setNumberOfThreads(int num) {
    if (num > 0) numThreads = num;
  }

  public int getNumberOfThreads() {
    return numThreads;
  }

  public void addToQueue(FTPTransferObject xfer) {
    synchronized (todoMap) {
      fifoQueue.add(xfer.local);
      todoMap.put(xfer.local, xfer);
    }
    /*
     * Could perhaps optimize this better by looking for GET vs. PUT jobs and doing one or two GETs in parallel with one or two PUTs. Most of time this application will be using PUT so it probably
     * doesn't matter.
     */
    boolean startAnother = false;
    synchronized (transferringMap) {
      if (transferringMap.size() < numThreads) startAnother = true;
    }
    if (startAnother) startNextTransfer();
  }

  public void removeFromQueue(Object local) {
    // First take it out of the todo list so it isn't started (we can't do an FTP ABORT
    // using the Sun JDK implementation).
    synchronized (todoMap) {
      fifoQueue.remove(local);
      todoMap.remove(local);
    }
    // Now check to see if it's running. If so, deleting it from the transferringMap means
    // we don't care about the results.
    synchronized (transferringMap) {
      transferringMap.remove(local);
    }
  }

  public void removeAllFromQueue() {
    /*
     * We can't actually stop an FTP transfer; we have to let it finish. This is ugly. It means that we need to let the transfer finish and remove the remote file, but we don't want to be
     * displaying the progress bar(s) in the mean time. The calling method must turn off display of the dialog yet not remove the change changeListeners until all outstanding transfer objects have
     * been completed. Then those objects should be removed from the server.
     */
    synchronized (todoMap) {
      fifoQueue.clear();
      todoMap.clear();
    }
    synchronized (transferringMap) {
      transferringMap.clear();
    }
  }

  private synchronized void startNextTransfer() {
    FTPTransferObject data;
    Object local;
    synchronized (todoMap) {
      if (!running || fifoQueue.isEmpty()) return;
      local = fifoQueue.remove(0);
      data = todoMap.remove(local);
    }
    synchronized (transferringMap) {
      transferringMap.put(local, data);
    }
    final FTPTransferObject thread_data = data;
    Thread th =
        new Thread("FTP " + data.remote) {
          @Override
          public void run() {
            doit(thread_data);
          }
        };
    th.start();
  }

  private void uploadDone(FTPTransferObject data, boolean keep) {
    boolean startAnother = false;
    synchronized (transferringMap) {
      if (transferringMap.containsKey(data.local)) transferringMap.remove(data.local);
      // TODO Should delete the remote file for uploading, or remove the local
      // file for downloading.
      if (fifoQueue.isEmpty() == false && transferringMap.size() < numThreads) startAnother = true;
    }
    if (startAnother) startNextTransfer();
    else if (fifoQueue.isEmpty()) fireStateChanged(this);
  }

  private static final int BLOCKSIZE = 4 * 1024;

  protected InputStream prepareInputStream(FTPTransferObject data) throws IOException {
    InputStream is = null;
    if (data.getput == Direction.FTP_PUT) {
      /*
       * In this situation, "data.local" is the InputStream.
       */
      if (data.local instanceof byte[]) {
        is = new ByteArrayInputStream((byte[]) data.local);
      } else if (data.local instanceof ByteArrayInputStream) {
        is = (ByteArrayInputStream) data.local;
      } else if (data.local instanceof InputStream) {
        is = (InputStream) data.local;
        // System.err.println("is.available() = " + is.available());
      } else if (data.local instanceof String) {
        File file = new File((String) data.local);
        try {
          is = new FileInputStream(file);
          data.setMaximum((int) ((file.length() + BLOCKSIZE - 1) / BLOCKSIZE));
          fireStateChanged(data);
        } catch (FileNotFoundException e) {
          log.error("Can't find local asset " + file, e);
        }
      } else {
        log.error("Illegal input object class: " + data.local.getClass());
      }
      if (is instanceof ByteArrayInputStream) {
        data.setMaximum((((ByteArrayInputStream) is).available() + BLOCKSIZE - 1) / BLOCKSIZE);
        fireStateChanged(data);
      }
    } else {
      /*
       * In this situation, "data.remote" is the InputStream.
       */
      try {
        is = cconn.openDownloadStream(data.remoteDir.getPath(), data.remote);
      } catch (IOException e) {
        File file = new File(data.remoteDir, data.remote);
        log.error("Attempting to open remote file " + file.getPath(), e);
      }
    }
    return is;
  }

  protected OutputStream prepareOutputStream(FTPTransferObject data) throws IOException {
    OutputStream os = null;
    if (data.getput == Direction.FTP_PUT) {
      /*
       * In this situation, "data.remote" is the OutputStream.
       */
      // try {
      if (data.remoteDir != null) {
        cconn.mkdir(data.remoteDir.getPath());
        os = cconn.openUploadStream(data.remoteDir.getPath(), data.remote);
      } else os = cconn.openUploadStream(data.remote);
      // } catch (IOException e) {
      // File file = new File(data.remoteDir, data.remote);
      // log.error("Attempting to FTP_PUT local asset " + file.getPath());
      // e.printStackTrace();
      // }
    } else {
      /*
       * In this situation, "data.local" is the OutputStream.
       */
      if (data.local instanceof String) {
        File file = new File((String) data.local);
        try {
          os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
          log.error("Can't write local file " + file, e);
        }
      } else if (data.local instanceof OutputStream) {
        os = (OutputStream) data.local;
      } else {
        log.error("Illegal output object class: " + data.local.getClass());
        throw new IllegalArgumentException("Cannot determine output type for " + data.local);
      }
    }
    return os;
  }

  protected void doit(FTPTransferObject data) {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = prepareInputStream(data);
      os = prepareOutputStream(data);
      if (is == null || os == null) {
        log.error("Can't build connection");
        return;
      }
      byte[] buf = new byte[BLOCKSIZE];
      int c = 1;
      while (c > 0) {
        c = is.read(buf);
        if (c > 0) os.write(buf, 0, c);
        data.incrCurrentPosition();
        fireStateChanged(data);
      }
      data.incrCurrentPosition();
    } catch (IOException e) {
      /*
       * For an IOException, it doesn't matter if it's a networking problem or just that the FTP server doesn't like the commands we're sending. Either way, we can mark this connection as dead
       * and skip any remaining transfers. Since we might be multithreaded though, we just flag this client as "not usable" and let the queue drain normally.
       */
      log.error(e.getMessage(), e);
      setEnabled(false);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      uploadDone(data, false);
    }
  }

  public static void main(String args[]) {
    JFrame frame = new JFrame("FTP Test");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    JLabel progress = new JLabel();
    frame.add(progress, BorderLayout.SOUTH);

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(0, 1, 5, 5));
    frame.add(panel, BorderLayout.CENTER);
    frame.setSize(new Dimension(400, 200));
    frame.setVisible(true);

    String[] uploadList =
        new String[] {
          "campaignItemList.xml", "mockup.jfpr", "standard.mtprops", "updateRepoDialog.xml",
        };
    FTPClient ftp = new FTPClient("www.eeconsulting.net", "username", "password");
    // ftp.setNumberOfThreads(3);
    File dir = new File("testdir");
    for (int i = 0; i < uploadList.length; i++) {
      FTPTransferObject fto =
          new FTPTransferObject(
              FTPTransferObject.Direction.FTP_PUT, uploadList[i], dir, uploadList[i]);
      ftp.addToQueue(fto);
    }
    // Need to listen for all progress bars to finish and count down using 'progress'.
  }
}
