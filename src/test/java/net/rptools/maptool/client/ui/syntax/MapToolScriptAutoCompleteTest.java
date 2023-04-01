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
package net.rptools.maptool.client.ui.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.junit.jupiter.api.Test;

class MapToolScriptAutoCompleteTest {

  @Test
  void tagsAreStrippedFromAShortDesc() {
    MapToolScriptAutoComplete mapToolScriptAutoComplete = new MapToolScriptAutoComplete();
    DefaultCompletionProvider completionProvider =
        (DefaultCompletionProvider) mapToolScriptAutoComplete.get();

    List<Completion> completionList = completionProvider.getCompletionByInputText("json.length");

    assertEquals(1, completionList.size());
    BasicCompletion completion = (BasicCompletion) completionList.get(0);
    assertEquals(
        "Returns the number of fields in a json object or number of elements in a json array .",
        completion.getShortDescription());
  }
}
