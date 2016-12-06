/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.rptools.lib.transferable;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;

/**
 * A list of token transfer data that came from the Group Tool. Need to specify app in the class
 * so that drag and drop functionality can check the mime type to see if it supports tokens from
 * that particular app.
 * 
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
@SuppressWarnings("serial")
public class GroupTokenTransferData extends ArrayList<TokenTransferData> {
	/**
	 * The data flavor that describes a list of tokens for exporting to maptool.
	 */
	public final static DataFlavor GROUP_TOKEN_LIST_FLAVOR = new DataFlavor(GroupTokenTransferData.class, "Group Tool Token List");
}
