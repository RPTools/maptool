/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool;

import java.io.File;

import net.rptools.lib.AppUtil;

public class AppConstants {

	public static final String APP_NAME = "tokenTool";
	public static final File OVERLAY_DIR = AppUtil.getAppHome("overlays");
	public static final String DEFAULT_TOKEN_EXTENSION = ".rptok";
	public static final String DEFAULT_IMAGE_EXTENSION = ".png";
	public static final String DEFAULT_TOKEN_DRAG_NAME = "token_drag";
}
