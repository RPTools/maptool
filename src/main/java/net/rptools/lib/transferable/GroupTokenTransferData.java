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
package net.rptools.lib.transferable;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;

/**
 * A list of token transfer data that came from the Group Tool. Need to specify app in the class so
 * that drag and drop functionality can check the mime type to see if it supports tokens from that
 * particular app.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
@SuppressWarnings("serial")
public class GroupTokenTransferData extends ArrayList<TokenTransferData> {
  /** The data flavor that describes a list of tokens for exporting to maptool. */
  public static final DataFlavor GROUP_TOKEN_LIST_FLAVOR =
      new DataFlavor(GroupTokenTransferData.class, "Group Tool Token List");
}
