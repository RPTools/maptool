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

import com.jeta.forms.components.panel.FormPanel;
import com.jidesoft.swing.CheckBoxListWithSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.rptools.lib.net.FTPLocation;
import net.rptools.lib.net.Location;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author crash */
@SuppressWarnings("serial")
public class UpdateRepoDialog extends JDialog {
  private static final Logger log = LogManager.getLogger(FTPClient.class);
  private static final String UPDATE_REPO_DIALOG =
      "net/rptools/maptool/client/ui/forms/updateRepoDialog.xml";
  private static final FormPanel form = new FormPanel(UPDATE_REPO_DIALOG);

  private int status = -1;
  private CheckBoxListWithSelectable list;
  private FTPLocation location;

  private JTextField saveTo;
  private JTextField hostname;
  private JTextField directory;
  private JTextField username;
  private JCheckBox subdir;
  private JPasswordField password;

  public UpdateRepoDialog(JFrame frame, List<String> repos, Location loc) {
    super(frame, "Update Repository Dialog", true);
    add(form);
    initFields();
    initFTPLocation(loc);

    list.setListData(repos.toArray());
    list.selectAll();
    MouseListener mouseListener =
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
              int index = list.locationToIndex(e.getPoint());
              Object o = list.getModel().getElementAt(index);
              saveTo.setText(o.toString());
              URL url = null;
              try {
                url = new URL(o.toString());
                // System.out.println("URL object contains: " + url);
                hostname.setText(url.getHost());
              } catch (MalformedURLException e1) {
                e1.printStackTrace();
              }
            }
          }
        };
    list.addMouseListener(mouseListener);

    AbstractButton btn = form.getButton("@okButton");
    btn.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setStatus(JOptionPane.OK_OPTION);
            UpdateRepoDialog.this.setVisible(false);
          }
        });
    btn = form.getButton("@cancelButton");
    btn.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setStatus(JOptionPane.CANCEL_OPTION);
            UpdateRepoDialog.this.setVisible(false);
          }
        });
  }

  protected void initFields() {
    list = (CheckBoxListWithSelectable) form.getComponentByName("checkBoxList");
    saveTo = form.getTextField("@saveTo");
    hostname = form.getTextField("@hostname");
    directory = form.getTextField("@directory");
    username = form.getTextField("@username");
    subdir = form.getCheckBox("@subdir");
    password = (JPasswordField) form.getComponentByName("@password");
    // If any of the above are null, there's a mismatch with the form.
    if (list == null
        || saveTo == null
        || hostname == null
        || directory == null
        || username == null
        || subdir == null
        || password == null) log.error("Form does not match code: " + UPDATE_REPO_DIALOG);
  }

  protected void initFTPLocation(Location loc) {
    if (loc instanceof FTPLocation) {
      location = (FTPLocation) loc;
      // Copy the fields into the GUI
      hostname.setText(location.getHostname());
      directory.setText(location.getPath());
      username.setText(location.getUsername());
      password.setText(location.getPassword());
    }
  }

  public FormPanel getForm() {
    return form;
  }

  public FTPLocation getFTPLocation() {
    if (location == null) {
      location =
          new FTPLocation(getUsername(), getPassword(), getHostname(), getDirectory().getPath());
    }
    return location;
  }

  public String getSaveToRepository() {
    return saveTo.getText();
  }

  public String getHostname() {
    return hostname.getText();
  }

  public File getDirectory() {
    String s = directory.getText();
    File f = new File(s == null ? "/" : s);
    return f;
  }

  public String getUsername() {
    return username.getText();
  }

  public String getPassword() {
    return new String(password.getPassword());
  }

  public boolean isCreateSubdir() {
    return subdir.isEnabled();
  }

  public List<String> getSelectedRepositories() {
    Object[] objects = list.getSelectedObjects();
    List<String> repoList = new ArrayList<String>(objects.length);
    for (int i = 0; i < objects.length; i++) {
      Object s = objects[i];
      // System.out.println("repoList[" + i + "] = " + s.toString() + ", type = " +
      // s.getClass().getCanonicalName());
      repoList.add(s.toString());
    }
    return repoList;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int s) {
    status = s;
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
  }
}
