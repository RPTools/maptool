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
  private static boolean enforceNotification = false;
  private static File campaignFile;
  private static int gridSize = 1;
  private static boolean showAsPlayer = false;
  private static boolean showLightSources = false;
  private static boolean zoomLocked = false;

  private static boolean collectProfilingData = false;
  private static boolean isLoggingToConsole = false;
  private static boolean isSaving = false;
  private static boolean isLoading = false;

  private static PropertyChangeSupport changeSupport = new PropertyChangeSupport(AppState.class);

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

  public static void setShowMovementMeasurements(boolean show) {
    showMovementMeasurements = show;
  }

  public static boolean getShowMovementMeasurements() {
    return showMovementMeasurements;
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

  public static synchronized void setIsLoading(boolean loading) {
    isLoading = loading;
  }

  public static synchronized boolean isLoading() {
    return isLoading;
  }

  public static synchronized void setIsSaving(boolean saving) {
    if (log.isDebugEnabled())
      log.debug("AppState.isSaving was " + isSaving + "; setting to " + saving); // $NON-NLS-1$
    isSaving = saving;
  }

  public static synchronized boolean isSaving() {
    if (log.isDebugEnabled()) log.debug("AppState.isSaving is " + isSaving); // $NON-NLS-1$
    return isSaving;
  }

  public static boolean isNotificationEnforced() {
    return enforceNotification;
  }

  public static void setNotificationEnforced(boolean enforce) {
    enforceNotification = enforce;
  }
}
