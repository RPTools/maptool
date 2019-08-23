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
package net.rptools.maptool.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author Phil Wright */
public class UPnPUtil {
  private static final Logger log = LogManager.getLogger(UPnPUtil.class);
  private static Map<InternetGatewayDevice, NetworkInterface> igds;
  private static List<InternetGatewayDevice> mappings;
  private static JDialog dialog = null;
  private static JPanel panel = new JPanel(new BorderLayout());
  private static Font labelFont = new Font("Dialog", Font.BOLD, 14);
  private static JLabel label = new JLabel("", SwingConstants.CENTER);

  private static void showMessage(String device, String msg) {
    if (dialog == null) {
      dialog = new JDialog(MapTool.getFrame());
      dialog.setContentPane(panel);
      panel.add(label, BorderLayout.CENTER);
      label.setFont(labelFont);
    }
    if (device == null) {
      dialog.setVisible(false);
    } else {
      dialog.setTitle("Scanning device " + device);
      label.setText(msg);

      Dimension d = label.getMinimumSize();
      d.width += 50;
      d.height += 50;
      label.setPreferredSize(d);

      dialog.pack();
      SwingUtil.centerOver(dialog, MapTool.getFrame());
      dialog.setVisible(true);
    }
  }

  public static boolean findIGDs() {
    igds = new HashMap<InternetGatewayDevice, NetworkInterface>();
    try {
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
      while (e.hasMoreElements()) {
        NetworkInterface ni = e.nextElement();
        try {
          if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
            int found = 0;
            try {
              if (log.isInfoEnabled()) log.info("UPnP:  Trying interface " + ni.getDisplayName());
              InternetGatewayDevice[] thisNI;
              showMessage(
                  ni.getDisplayName(), "Looking for gateway devices on " + ni.getDisplayName());
              thisNI =
                  InternetGatewayDevice.getDevices(
                      AppPreferences.getUpnpDiscoveryTimeout(),
                      Discovery.DEFAULT_TTL,
                      Discovery.DEFAULT_MX,
                      ni);
              showMessage(null, null);
              if (thisNI != null) {
                for (InternetGatewayDevice igd : thisNI) {
                  found++;
                  if (log.isInfoEnabled())
                    log.info("UPnP:  Found IGD: " + igd.getIGDRootDevice().getModelName());
                  if (igds.put(igd, ni) != null) {
                    // There was a previous mapping for this IGD! It's unlikely to have two NICs on
                    // the
                    // the same network segment, but it IS possible. For example, both a wired and
                    // wireless connection using the same router as the gateway. For our purposes it
                    // doesn't really matter which one we use, but in the future we should give the
                    // user a choice.
                    // FIXME We SHOULD be using the "networking binding order" (Windows)
                    // or "network service order" on OSX.
                    if (log.isInfoEnabled())
                      log.info("UPnP:  This was not the first time this IGD was found!");
                  }
                }
              }
            } catch (IOException ex) {
              showMessage(null, null);
              // some IO Exception occurred during communication with device
              log.warn("While searching for internet gateway devices", ex);
            }
            if (log.isInfoEnabled())
              log.info("Found " + found + " IGDs on interface " + ni.getDisplayName());
          }
        } catch (SocketException se) {
          continue;
        }
      }
    } catch (SocketException se) {
      // Nothing to do, but we DO want the 'mappings' member to be initialized
    }
    mappings = new ArrayList<InternetGatewayDevice>(igds.size());
    return !igds.isEmpty();
  }

  public static boolean openPort(int port) {
    if (igds == null || igds.isEmpty()) {
      findIGDs();
    }
    if (igds == null || igds.isEmpty()) {
      MapTool.showError(
          "UPnP Error - No Internet Gateway Devices found.<br><br>UPnP port mapping will not be available.");
      return false;
    }
    for (InternetGatewayDevice gd : igds.keySet()) {
      NetworkInterface ni = null;
      String localHostIP = "(NULL)";
      try {
        ni = igds.get(gd);
        switch (ni.getInterfaceAddresses().size()) {
          case 0:
            log.error("IGD shows up in list of IGDs, but no NICs stored therein?!");
            break;
          case 1:
            localHostIP = ni.getInterfaceAddresses().get(0).getAddress().getHostAddress();
            break;
          default:
            for (Iterator<InterfaceAddress> iter = ni.getInterfaceAddresses().iterator();
                iter.hasNext(); ) {
              InterfaceAddress ifAddr = iter.next();
              if (ifAddr.getAddress() instanceof Inet4Address) {
                localHostIP = ifAddr.getAddress().getHostAddress();
                if (log.isInfoEnabled())
                  log.info("IP address " + localHostIP + " on interface " + ni.getDisplayName());
              }
            }
            break;
        }
        boolean mapped = gd.addPortMapping("MapTool", null, port, port, localHostIP, 0, "TCP");
        if (mapped) {
          mappings.add(gd);
          if (log.isInfoEnabled())
            log.info(
                "UPnP: Port "
                    + port
                    + " mapped on "
                    + ni.getDisplayName()
                    + " at address "
                    + localHostIP);
        }
      } catch (UPNPResponseException respEx) {
        // oops the IGD did not like something !!
        log.error(
            "UPnP Error 1: Could not add port mapping on device "
                + ni.getDisplayName()
                + ", IP address "
                + localHostIP,
            respEx);
      } catch (IOException ioe) {
        log.error(
            "UPnP Error 2: Could not add port mapping on device "
                + ni.getDisplayName()
                + ", IP address "
                + localHostIP,
            ioe);
      }
    }
    if (mappings.isEmpty())
      MapTool.showError("UPnP: found " + igds.size() + " IGDs but no port mapping succeeded!?");
    return !mappings.isEmpty();
  }

  public static boolean closePort(int port) {
    if (igds == null || igds.isEmpty()) return true;

    int count = 0;
    for (Iterator<InternetGatewayDevice> iter = igds.keySet().iterator(); iter.hasNext(); ) {
      InternetGatewayDevice gd = iter.next();
      try {
        ActionResponse actResp = gd.getSpecificPortMappingEntry(null, port, "TCP");
        if (actResp != null
            && "MapTool".equals(actResp.getOutActionArgumentValue("NewPortMappingDescription"))) {
          // NewInternalPort=51234
          // NewEnabled=1
          // NewInternalClient=192.168.0.30
          // NewLeaseDuration=0
          // NewPortMappingDescription=MapTool
          boolean unmapped = gd.deletePortMapping(null, port, "TCP");
          if (unmapped) {
            count++;
            if (log.isInfoEnabled())
              log.info("UPnP: Port unmapped from " + igds.get(gd).getDisplayName());
            iter.remove();
          } else {
            if (log.isInfoEnabled())
              log.info("UPnP: Failed to unmap port from " + igds.get(gd).getDisplayName());
          }
        }
      } catch (IOException e) {
        log.info("UPnP: IOException while talking to IGD", e);
      } catch (UPNPResponseException e) {
        log.info("UPnP: UPNPResponseException while talking to IGD", e);
      }
    }
    return count > 0;
  }
}
