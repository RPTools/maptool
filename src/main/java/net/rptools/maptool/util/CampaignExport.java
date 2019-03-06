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

import java.io.IOException;
import net.rptools.lib.io.PackedFile;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.PersistenceUtil.PersistedCampaign;

/**
 * A simple class to segregate and group exporting rules for campaigns...
 *
 * @author Jamz
 * @since 1.4.1.4
 */
public class CampaignExport {
  // Generally, Major Version number will never change and Minor would be a huge update.
  // Release version is the most common and changes to campaign files should force an increase to
  // Release version.
  // Build version is usually bug fixes and small changes, a campaign file should remain compatible
  // across all build versions.

  private static int VERSION_MAJOR = 0;
  private static int VERSION_MINOR = 0;
  private static int VERSION_RELEASE = 0;
  private static int VERSION_BUILD = 0;

  // A list of versions we can export back to. There should be a corresponding note in the language
  // files.
  private static final String[] VERSION_ARRAY = {"1.4.0.1"};

  /**
   * This method strips out the extra classes & fields using X-Stream based on which MapTool Version
   * you want go back to.
   *
   * @author Jamz
   * @since 1.4.1.4
   * @param pakFile
   * @param persistedCampaign
   * @param campaignVersion
   * @return
   * @throws IOException
   */
  public static PackedFile stripContent(
      PackedFile pakFile, PersistedCampaign persistedCampaign, String campaignVersion)
      throws IOException {
    if (!setVersions(campaignVersion)) {
      return pakFile;
    }

    // Check Major version for sanity's sake...
    if (VERSION_MAJOR == 1 && VERSION_MINOR <= 4) {
      // Lumens, tokenSelection, & several Token class fields were introduced in 1.4.1.x
      if (VERSION_RELEASE == 0) {
        pakFile.getXStream().omitField(LightSource.class, "lumens");
        pakFile.getXStream().omitField(LightSource.class, "scaleWithToken");
        pakFile.getXStream().omitField(SightType.class, "scaleWithToken");
        pakFile.getXStream().omitField(Zone.class, "tokenSelection");
        pakFile.getXStream().omitField(Token.class, "vbl");
        pakFile.getXStream().omitField(Token.class, "isoWidth");
        pakFile.getXStream().omitField(Token.class, "isoHeight");
        pakFile.getXStream().omitField(Token.class, "vblAlphaSensitivity");
        pakFile.getXStream().omitField(Token.class, "isAlwaysVisible");
        pakFile.getXStream().omitField(Token.class, "alwaysVisibleTolerance");
        pakFile.getXStream().omitField(Token.class, "tokenOpacity");
        pakFile.getXStream().omitField(Token.class, "heroLabData");
        pakFile.getXStream().omitField(MacroButtonProperties.class, "macroUUID");

        // DrawnElement was added in 1.4.0.1
        // FIXME: Stripping the class still leaves an empty XML tag so unable to export to 1.4.0.0
        // at this time :(
        if (VERSION_BUILD == 0) {
          // pakFile.getXStream().registerConverter(new DrawablesGroupConverter());
        }
      }
    }

    pakFile.setContent(persistedCampaign);
    pakFile.setProperty(
        PersistenceUtil.PROP_CAMPAIGN_VERSION,
        VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_RELEASE);
    pakFile.setProperty(PersistenceUtil.PROP_VERSION, campaignVersion);

    return pakFile;
  }

  /**
   * A convenience method to break up the version number string into it's component version
   * identifiers. Version string is expected in the format of w.x.y.z eg 1.4.1.5
   *
   * @author Jamz
   * @since 1.4.1.4
   * @param mapToolVersion
   * @return true if valid version format
   */
  private static boolean setVersions(String mapToolVersion) {
    String[] mapToolVersions =
        mapToolVersion.indexOf(".") > 0
            ? mapToolVersion.split("\\.")
            : new String[] {mapToolVersion};

    // Break down version numbers from 1.4.0.0 format for easier comparison
    try {
      VERSION_MAJOR = Integer.parseInt(mapToolVersions[0]);
      VERSION_MINOR = Integer.parseInt(mapToolVersions[1]);
      VERSION_RELEASE = Integer.parseInt(mapToolVersions[2]);
      VERSION_BUILD = Integer.parseInt(mapToolVersions[3]);
    } catch (ArrayIndexOutOfBoundsException ex) {
      MapTool.showError(I18N.getString("dialog.campaignexport.error.invalidversion"), ex);
      return false;
    }

    return true;
  }

  public static String[] getVersionArray() {
    return VERSION_ARRAY;
  }
}
