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
package net.rptools.maptool.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppState {
  private static final Logger log = LogManager.getLogger(AppState.class);
  public static final String USE_DOUBLE_WIDE_PROP_NAME = "useDoubleWide";

  private static boolean showGrid = false;
  private static boolean showCoordinates = false;
  private static boolean showTokenNames = false;
  private static boolean linkPlayerViews = false;
  private static boolean useDoubleWideLine = true;
  private static boolean showMovementMeasurements = true;
  private static boolean showTextLabels = true;
  private static boolean enforceNotification = false;
  private static File campaignFile;
  private static int gridSize = 1;
  private static boolean showLumensOverlay;
  private static boolean showLights;
  private static boolean showAsPlayer = false;
  private static boolean showLightSources = false;
  private static boolean zoomLocked = false;

  private static boolean collectProfilingData = false;
  private static boolean isLoggingToConsole = false;
  private static boolean isLockedForBackgroundTask = false;
  private static boolean enableFullScreenUI = true;

  private static PropertyChangeSupport changeSupport = new PropertyChangeSupport(AppState.class);

  static {
    showLumensOverlay = AppPreferences.lumensOverlayShowByDefault.get();
    showLights = AppPreferences.lightsShowByDefault.get();
  }

  public static void addPropertyChangeListener(PropertyChangeListener listener) {
    changeSupport.addPropertyChangeListener(listener);
  }

  public static void addPropertyChangeListener(
      String propertyName, PropertyChangeListener listener) {
    changeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public static boolean isCollectProfilingData() {
    return collectProfilingData;
  }

  public static void setCollectProfilingData(boolean flag) {
    collectProfilingData = flag;
  }

  public static boolean isLoggingToConsole() {
    return isLoggingToConsole;
  }

  public static void setLoggingToConsole(boolean flag) {
    isLoggingToConsole = flag;
  }

  public static int getGridSize() {
    return gridSize;
  }

  public static void setGridSize(int size) {
    gridSize = size;
  }

  public static boolean useDoubleWideLine() {
    return useDoubleWideLine;
  }

  public static void setUseDoubleWideLine(boolean useDoubleWideLine) {
    boolean old = AppState.useDoubleWideLine;
    AppState.useDoubleWideLine = useDoubleWideLine;
    changeSupport.firePropertyChange(USE_DOUBLE_WIDE_PROP_NAME, old, useDoubleWideLine);
  }

  public static boolean isShowGrid() {
    return showGrid;
  }

  public static void setShowGrid(boolean flag) {
    showGrid = flag;
  }

  public static boolean isShowCoordinates() {
    return showCoordinates;
  }

  public static boolean isZoomLocked() {
    return zoomLocked;
  }

  public static void setZoomLocked(boolean zoomLock) {
    boolean oldVal = AppState.zoomLocked;
    AppState.zoomLocked = zoomLock;
    changeSupport.firePropertyChange("zoomLock", oldVal, zoomLock);
  }

  public static void setShowCoordinates(boolean flag) {
    showCoordinates = flag;
  }

  public static void setShowTokenNames(boolean flag) {
    showTokenNames = flag;
  }

  public static boolean isShowTokenNames() {
    return showTokenNames;
  }

  public static boolean isPlayerViewLinked() {
    return linkPlayerViews;
  }

  public static void setPlayerViewLinked(boolean flag) {
    linkPlayerViews = flag;
  }

  public static File getCampaignFile() {
    return campaignFile;
  }

  public static void setCampaignFile(File campaignFile) {
    AppState.campaignFile = campaignFile;
  }

  /**
   * Returns the campaign name (without extension) from the campaign file. If no campaign file is
   * defined, instead returns "Default".
   *
   * @return The string containing the campaign name
   */
  public static String getCampaignName() {
    if (AppState.campaignFile == null) {
      return "Default";
    } else {
      String s = AppState.campaignFile.getName();
      // remove the file extension of the campaign file name
      return s.substring(0, s.length() - AppConstants.CAMPAIGN_FILE_EXTENSION.length());
    }
  }

  public static void setShowMovementMeasurements(boolean show) {
    showMovementMeasurements = show;
  }

  public static boolean getShowMovementMeasurements() {
    return showMovementMeasurements;
  }

  public static void setShowTextLabels(boolean show) {
    showTextLabels = show;
  }

  public static boolean getShowTextLabels() {
    return showTextLabels;
  }

  public static boolean isShowLights() {
    return showLights;
  }

  public static void setShowLights(boolean show) {
    showLights = show;
  }

  public static boolean isShowLumensOverlay() {
    return showLumensOverlay;
  }

  public static void setShowLumensOverlay(boolean show) {
    showLumensOverlay = show;
  }

  public static boolean isShowAsPlayer() {
    return showAsPlayer;
  }

  public static void setShowAsPlayer(boolean showAsPlayer) {
    AppState.showAsPlayer = showAsPlayer;
  }

  public static boolean isShowLightSources() {
    return showLightSources;
  }

  public static void setShowLightSources(boolean show) {
    showLightSources = show;
  }

  private static ReentrantLock backgroundTaskLock = new ReentrantLock();

  public static boolean testBackgroundTaskLock() {
    return backgroundTaskLock.isLocked();
  }

  public static class FailedToAcquireLockException extends Exception {}

  public static void acquireBackgroundTaskLock(int waitSeconds)
      throws FailedToAcquireLockException {
    try {
      if (backgroundTaskLock.tryLock(waitSeconds, TimeUnit.SECONDS)) return;
    } catch (InterruptedException ie) {
    }
    throw new FailedToAcquireLockException();
  }

  public static void releaseBackgroundTaskLock() {
    backgroundTaskLock.unlock();
  }

  public static boolean isNotificationEnforced() {
    return enforceNotification;
  }

  public static void setNotificationEnforced(boolean enforce) {
    enforceNotification = enforce;
  }

  public static boolean isFullScreenUIEnabled() {
    return enableFullScreenUI;
  }

  public static void setFullScreenUIEnabled(boolean value) {
    enableFullScreenUI = value;
  }
}
