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

import com.google.gson.JsonObject;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.MemoryStatusBar;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapToolSysInfoProvider implements SysInfoProvider {

  private static final Logger log = LogManager.getLogger(MapToolSysInfoProvider.class);

  private static final DecimalFormat format = new DecimalFormat("#,##0.#");
  private static String os = "";
  private List<String> rows = new ArrayList<>();

  private static String getEncoding() {
    final byte[] bytes = {'D'};
    final InputStream inputStream = new ByteArrayInputStream(bytes);
    final InputStreamReader reader = new InputStreamReader(inputStream);
    return reader.getEncoding();
  }

  private static String getRouterIP() {
    String oneOctet = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    String exIP = "(?:" + oneOctet + "\\.){3}" + oneOctet;

    Pattern pat = Pattern.compile("^\\s*(?:0\\.0\\.0\\.0\\s*){1,2}(" + exIP + ").*");
    try {
      Process proc = Runtime.getRuntime().exec("netstat -rn");
      InputStream inputstream = proc.getInputStream();
      InputStreamReader inputstreamreader = new InputStreamReader(inputstream);

      BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
      String line;
      while ((line = bufferedreader.readLine()) != null) {
        Matcher m = pat.matcher(line);

        if (m.matches()) {
          return m.group(1);
        }
        if (line.startsWith("default")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          return st.nextToken();
        }
      }
    } catch (IOException ex) {
      log.error(ex);
      return "Failed";
    }
    return "Unknown";
  }

  @Override
  public JsonObject getSysInfoJSON() {
    JsonObject info = new JsonObject();

    JsonObject mt = new JsonObject();

    JsonObject java = new JsonObject();
    JsonObject locale = new JsonObject();
    JsonObject os = new JsonObject();

    Properties p = System.getProperties();

    // maptool info
    mt.addProperty("version", MapTool.getVersion());
    // mt.addProperty("home", AppUtil.getAppHome()); // this line crashes, I didnt figured out why
    mt.addProperty(
        "max mem avail", format.format(Runtime.getRuntime().maxMemory() / (1024 * 1024)));
    mt.addProperty(
        "max mem used", format.format(MemoryStatusBar.getInstance().getLargestMemoryUsed()));
    info.add("maptool", mt);

    // java
    java.addProperty("vendor", p.getProperty("java.vendor"));
    java.addProperty("home", p.getProperty("java.home"));
    java.addProperty("version", p.getProperty("java.version"));
    info.add("java", java);

    // locale
    Locale loc = Locale.getDefault();
    locale.addProperty("country", loc.getDisplayCountry());
    locale.addProperty("language", loc.getDisplayLanguage());
    locale.addProperty("locale", loc.getDisplayName());
    locale.addProperty("variant", loc.getDisplayVariant());
    info.add("locale", locale);

    // os
    os.addProperty("name", p.getProperty("os.name"));
    os.addProperty("version", p.getProperty("os.version"));
    os.addProperty("arch", p.getProperty("os.arch"));
    // os.addProperty("path", (env.get("PATH") != null ? env.get("PATH") :
    // p.getProperty("java.library.path")));
    info.add("os", os);

    return info;
  }

  private void appendInfo(String s) {
    rows.add(s + "\n");
  }

  private void getMapToolInfo(Properties p) {
    appendInfo("==== MapTool Information ====");
    appendInfo("MapTool Version: " + MapTool.getVersion());
    appendInfo("MapTool Home...: " + AppUtil.getAppHome());
    appendInfo("MapTool Install: " + AppUtil.getAppInstallLocation());
    appendInfo(
        "Max mem avail..: " + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()));
    appendInfo(
        "Max mem used...: "
            + FileUtils.byteCountToDisplaySize(
                MemoryStatusBar.getInstance().getLargestMemoryUsed()));

    for (String prop : p.stringPropertyNames()) {
      if (prop.startsWith("MAPTOOL_")) {
        appendInfo("Custom Property: -D" + prop + "=" + p.getProperty(prop));
      }
    }

    appendInfo("");
  }

  private void getJavaInfo(Properties p) {
    appendInfo("==== Java Information ====");
    appendInfo("Java Home......: " + p.getProperty("java.home"));
    appendInfo("Java Vendor....: " + p.getProperty("java.vendor"));
    appendInfo("Java Version...: " + p.getProperty("java.version"));
    appendInfo("Java Parameters: ");

    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> jvmParmList = runtimeMxBean.getInputArguments();

    for (String jvmParm : jvmParmList) {
      if (!jvmParm.startsWith("-DMAPTOOL_") && !jvmParm.isBlank()) {
        appendInfo("  " + jvmParm);
      }
    }

    appendInfo("");
  }

  private void getLocaleInfo() {
    appendInfo("\n==== Locale Information ====");
    Locale loc = Locale.getDefault();
    appendInfo("Country.: " + loc.getDisplayCountry());
    appendInfo("Language: " + loc.getDisplayLanguage());
    appendInfo("Locale..: " + loc.getDisplayName());
    appendInfo("Variant.: " + loc.getDisplayVariant());
    appendInfo("");
  }

  private void getEncodingInfo() {
    appendInfo("==== Encoding Information ====");
    appendInfo("Default Locale:   " + Locale.getDefault());
    appendInfo("Default Charset:  " + Charset.defaultCharset());
    appendInfo("file.encoding:    " + System.getProperty("file.encoding"));
    appendInfo("sun.jnu.encoding: " + System.getProperty("sun.jnu.encoding"));
    appendInfo("Default Encoding: " + getEncoding());
    appendInfo("");
  }

  private void getOsInfo(Properties p) {
    Map<String, String> env = System.getenv();

    appendInfo("\n==== OS Information ====");
    appendInfo("OS Name........: " + p.getProperty("os.name"));
    appendInfo("OS Version.....: " + p.getProperty("os.version"));
    appendInfo("OS Architecture: " + p.getProperty("os.arch"));
    os = p.getProperty("os.name");
    if (os.contains("Windows")) {
      appendInfo("Processor......: " + env.get("PROCESSOR_IDENTIFIER"));
    }
    appendInfo(
        "PATH...........: "
            + (env.get("PATH") != null ? env.get("PATH") : p.getProperty("java.library.path")));
    appendInfo("Number of Procs: " + Runtime.getRuntime().availableProcessors());

    appendInfo("\n==== User Information ====");
    appendInfo("User Name: " + p.getProperty("user.name"));
    appendInfo("User Home: " + p.getProperty("user.home"));
    appendInfo("User Dir.: " + p.getProperty("user.dir"));
  }

  private void getNetworkInterfaces() {
    appendInfo("\n==== Network Interfaces ====");
    try {
      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface netIf : Collections.list(nets)) {
        if ((os.contains("Mac")) || (netIf.isUp())) {
          appendInfo("Display Name..: " + netIf.getDisplayName());
          appendInfo("Interface Name: " + netIf.getName());
          Enumeration<InetAddress> inetAddresses = netIf.getInetAddresses();
          for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            appendInfo("Address...: " + inetAddress.getHostAddress());
          }
          appendInfo("");
        }
      }

      try {
        String hostIP = InetAddress.getLocalHost().getHostAddress();
        appendInfo("Host Address...: " + hostIP);
      } catch (UnknownHostException ex) {
        appendInfo("Host Address...: failed");
        log.error(ex);
      }

      String routerIP = getRouterIP();
      appendInfo("Default Gateway: " + routerIP);
    } catch (SocketException se) {
      appendInfo("*** Could not get list of network interfaces ***");
      log.error(se);
    }
  }

  private void getDisplayInfo() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    appendInfo("==== Display Information ====");
    appendInfo("Number of Displays: " + gs.length);
    // XXX Is there any way to report on the actual hardware? nVidia and
    // ATI/AMD cards sometimes have bugs in their proprietary drivers that
    // cause headache for Java. It would be nice to have that information.
    // For Windows it would be good to see DirectX module names and version
    // numbers, but can we obtain that from the JRE...?
    int i = 0;
    for (GraphicsDevice gd : gs) {
      i++;
      DisplayMode dm = gd.getDisplayMode();
      int bits = dm.getBitDepth();
      String depth = "(" + bits + ")";
      appendInfo("Display " + i + ": " + dm.getWidth() + "x" + dm.getHeight() + depth);
    }
  }

  private void getIGDs() {
    int discoveryTimeout = 5000;
    InternetGatewayDevice[] IGDs = null;

    appendInfo("\n==== Internet Gateway Devices ====");
    try {
      IGDs = InternetGatewayDevice.getDevices(discoveryTimeout);
    } catch (IOException ex) {
      appendInfo("\tError scanning for IGDs.");
      log.error(ex);
    }

    if (IGDs != null) {
      for (InternetGatewayDevice igd : IGDs) {
        UPNPRootDevice rootDev = igd.getIGDRootDevice();
        appendInfo("Device Name.: " + rootDev.getFriendlyName());
        appendInfo("Model Name..: " + rootDev.getModelName());
        appendInfo("Manufacturer: " + rootDev.getManufacturer());
        appendInfo("Model Number: " + rootDev.getModelNumber());
        appendInfo("Model Desc..: " + rootDev.getModelDescription());
        appendInfo("Firmware....: " + rootDev.getVendorFirmware());
        try {
          appendInfo("External IP.: " + igd.getExternalIPAddress());
        } catch (UPNPResponseException ex) {
          appendInfo("UPNPResponseException" + ex);
          log.error(ex);
        } catch (IOException ex) {
          appendInfo("IOException" + ex);
          log.error(ex);
        }
        appendInfo("");
      }
    } else {
      appendInfo("\tNo IGDs Found!");
    }
  }

  public List<String> getInfo() {
    Properties p = System.getProperties();
    getMapToolInfo(p);
    getJavaInfo(p);
    getOsInfo(p);
    getNetworkInterfaces();
    getLocaleInfo();
    getEncodingInfo();
    getDisplayInfo();
    getIGDs();
    return rows;
  }
}
