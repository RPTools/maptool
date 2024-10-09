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

import java.io.File;
import javax.swing.*;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.util.PersistenceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author tylere
 *     <p>Attempts to recover campaigns when the application crashes.
 */
public class AutoSaveManager {

  private static final Logger log = LogManager.getLogger(AutoSaveManager.class);

  private Timer autoSaveTimer;
  private long lastAutoSave = -1;

  public static final File AUTOSAVE_FILE =
      new File(
          AppUtil.getAppHome("autosave"), // $NON-NLS-1$
          "AutoSave" + AppConstants.CAMPAIGN_FILE_EXTENSION);

  /**
   * Queries the auto-save increment from {@link AppPreferences} and starts a new timer.
   *
   * <p>The <code>synchronized</code> is necessary -- this method is also called from the code in
   * {@link AppActions#loadCampaign(File)} from inside a separate thread.
   */
  public void start() {
    if (autoSaveTimer == null) {
      autoSaveTimer = new Timer(1000, (Object) -> execute());
      autoSaveTimer.setRepeats(false);
      log.debug("Logging level of 'DEBUG' sets timeout to seconds"); // $NON-NLS-1$
      next(true);
    }
  }

  private void next(boolean markSaved) {
    autoSaveTimer.start();
    if (markSaved) lastAutoSave = System.currentTimeMillis();
  }

  private void execute() {
    if (executeAndContinue()) next(false);
  }

  private boolean executeAndContinue() {

    int interval =
        AppPreferences.autoSaveIncrement.get()
            * 1000
            * (DeveloperOptions.Toggle.AutoSaveMeasuredInSeconds.isEnabled() ? 1 : 60);

    // auto-save is turned off with <= 0
    if (interval <= 0) {
      log.debug("Skipping autosave because interval <=0"); // $NON-NLS-1$
      return true;
    }

    // time's not up yet?
    if (System.currentTimeMillis() - lastAutoSave < interval) return true;

    // Don't autosave if we don't "own" the campaign
    if (!MapTool.isHostingServer() && !MapTool.isPersonalServer()) {
      log.debug("Skipping autosave because we're not a server"); // $NON-NLS-1$
      return true;
    }

    if (AppState.testBackgroundTaskLock()) {
      log.info("Delaying autosave because user initiated SAVE or LOAD operation"); // $NON-NLS-1$
      return true;
    }

    MapTool.getFrame().setStatusMessage(I18N.getString("AutoSaveManager.status.autoSaving"));

    long startCopy = System.currentTimeMillis();
    // This occurs on the event dispatch thread, so it's ok to mess with the models.  (XXX Is this
    // true?  What about
    // updates coming in on the network?)
    // We need to clone the campaign so that we can save in the background, but
    // not have concurrency issues with the original model.
    //
    // NOTE: This is a cheesy way to clone the campaign, but it makes it so that I
    // don't have to keep all the various models' clone methods updated on each change.
    final Campaign campaign = new Campaign(MapTool.getCampaign());
    log.info(
        "Time to copy Campaign object (ms): "
            + (System.currentTimeMillis() - startCopy)); // $NON-NLS-1$

    new SaveWorker(campaign).execute();

    return false;
  }

  private class SaveWorker extends SwingWorker<String, String> {

    private Campaign campaign;

    private SaveWorker(Campaign campaign) {
      this.campaign = campaign;
    }

    @Override
    protected String doInBackground() throws Exception {

      AppState.acquireBackgroundTaskLock(0);

      try {
        long startSave = System.currentTimeMillis();
        log.info("Starting autosave..."); // $NON-NLS-1$
        PersistenceUtil.saveCampaign(campaign, AUTOSAVE_FILE);
        String msg =
            I18N.getText(
                "AutoSaveManager.status.autoSaveComplete", System.currentTimeMillis() - startSave);
        log.info(msg);
        return msg;

      } finally {
        AppState.releaseBackgroundTaskLock();
      }
    }

    @Override
    protected void done() {

      try {
        MapTool.getFrame().setStatusMessage(get());
      } catch (Throwable t) {
        log.debug("Throwable during autosave: " + t.getCause());
        if (t.getCause() instanceof AppState.FailedToAcquireLockException)
          MapTool.getFrame().setStatusMessage(I18N.getText("AutoSaveManager.status.lockFailed"));
        else MapTool.showError("AutoSaveManager.failed", t.getCause());
      }

      next(true);
    }
  }

  /** Removes any autosaved files */
  public void purge() {
    if (AUTOSAVE_FILE.exists()) {
      AUTOSAVE_FILE.delete();
    }
  }

  /** Removes the campaignFile if it's from Autosave, forcing to save as new */
  public void tidy() {
    if (AUTOSAVE_FILE.equals(AppState.getCampaignFile())) {
      AppState.setCampaignFile(null);
    }
    purge();
  }

  /** Check to see if autosave recovery is necessary. */
  public boolean check() {
    if (AUTOSAVE_FILE.exists()) {
      boolean okay;
      okay = MapTool.confirm("msg.confirm.recoverAutosave", AUTOSAVE_FILE.lastModified());
      if (okay) {
        AppActions.loadCampaign(AUTOSAVE_FILE);
        return true;
      }
    }
    return false;
  }
}
