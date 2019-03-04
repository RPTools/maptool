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

import de.muntjak.tinylookandfeel.TinyLookAndFeel;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.text.DefaultEditorKit;

public class TinyLookAndFeelMac extends TinyLookAndFeel {
  private static final long serialVersionUID = 1L;

  @Override
  protected void initComponentDefaults(UIDefaults table) {
    super.initComponentDefaults(table);

    Object fieldInputMap =
        new UIDefaults.LazyInputMap(
            new Object[] {
              // @formatter:off
              "meta C", DefaultEditorKit.copyAction,
              "meta V", DefaultEditorKit.pasteAction,
              "meta X", DefaultEditorKit.cutAction,
              "COPY", DefaultEditorKit.copyAction,
              "PASTE", DefaultEditorKit.pasteAction,
              "CUT", DefaultEditorKit.cutAction,
              "shift LEFT", DefaultEditorKit.selectionBackwardAction,
              "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
              "shift RIGHT", DefaultEditorKit.selectionForwardAction,
              "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
              "ctrl LEFT", DefaultEditorKit.previousWordAction,
              "ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
              "ctrl RIGHT", DefaultEditorKit.nextWordAction,
              "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
              "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
              "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
              "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
              "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
              "meta A", DefaultEditorKit.selectAllAction,
              "HOME", DefaultEditorKit.beginLineAction,
              "END", DefaultEditorKit.endLineAction,
              "shift HOME", DefaultEditorKit.selectionBeginLineAction,
              "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction, // FJE
              "shift END", DefaultEditorKit.selectionEndLineAction,
              "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction, // FJE
              "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
              "ctrl H", DefaultEditorKit.deletePrevCharAction,
              "DELETE", DefaultEditorKit.deleteNextCharAction,
              "RIGHT", DefaultEditorKit.forwardAction,
              "LEFT", DefaultEditorKit.backwardAction,
              "KP_RIGHT", DefaultEditorKit.forwardAction,
              "KP_LEFT", DefaultEditorKit.backwardAction,
              "ENTER", JTextField.notifyAction,
              "ctrl BACK_SLASH", "unselect", // DefaultEditorKit.unselectAction
              // "control shift O", "toggle-componentOrientation", //
              // DefaultEditorKit.toggleComponentOrientation
              // FJE: {
              "meta LEFT", DefaultEditorKit.beginLineAction,
              "meta KP_LEFT", DefaultEditorKit.beginLineAction,
              "meta RIGHT", DefaultEditorKit.endLineAction,
              "meta KP_RIGHT", DefaultEditorKit.endLineAction,
              // }
              // @formatter:on
            });

    Object passwordInputMap =
        new UIDefaults.LazyInputMap(
            new Object[] {
              // @formatter:off
              "meta C", DefaultEditorKit.copyAction,
              "meta V", DefaultEditorKit.pasteAction,
              "meta X", DefaultEditorKit.cutAction,
              "COPY", DefaultEditorKit.copyAction,
              "PASTE", DefaultEditorKit.pasteAction,
              "CUT", DefaultEditorKit.cutAction,
              "shift LEFT", DefaultEditorKit.selectionBackwardAction,
              "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
              "shift RIGHT", DefaultEditorKit.selectionForwardAction,
              "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
              "ctrl LEFT", DefaultEditorKit.beginLineAction,
              "ctrl KP_LEFT", DefaultEditorKit.beginLineAction,
              "ctrl RIGHT", DefaultEditorKit.endLineAction,
              "ctrl KP_RIGHT", DefaultEditorKit.endLineAction,
              "ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
              "ctrl shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
              "ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
              "ctrl shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
              "meta A", DefaultEditorKit.selectAllAction,
              "HOME", DefaultEditorKit.beginLineAction,
              "END", DefaultEditorKit.endLineAction,
              "shift HOME", DefaultEditorKit.selectionBeginLineAction,
              "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction, // FJE
              "shift END", DefaultEditorKit.selectionEndLineAction,
              "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction, // FJE
              "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
              "ctrl H", DefaultEditorKit.deletePrevCharAction,
              "DELETE", DefaultEditorKit.deleteNextCharAction,
              "RIGHT", DefaultEditorKit.forwardAction,
              "LEFT", DefaultEditorKit.backwardAction,
              "KP_RIGHT", DefaultEditorKit.forwardAction,
              "KP_LEFT", DefaultEditorKit.backwardAction,
              "ENTER", JTextField.notifyAction,
              "ctrl BACK_SLASH", "unselect" /*
												 * DefaultEditorKit. unselectAction
												 */,
              // "control shift O",
              // "toggle-componentOrientation"/*DefaultEditorKit.toggleComponentOrientation*/
              // FJE: {
              "meta LEFT", DefaultEditorKit.beginLineAction,
              "meta KP_LEFT", DefaultEditorKit.beginLineAction,
              "meta RIGHT", DefaultEditorKit.endLineAction,
              "meta KP_RIGHT", DefaultEditorKit.endLineAction,
              // }
              // @formatter:on
            });

    Object multilineInputMap =
        new UIDefaults.LazyInputMap(
            new Object[] {
              // @formatter:off
              "meta C", DefaultEditorKit.copyAction,
              "meta V", DefaultEditorKit.pasteAction,
              "meta X", DefaultEditorKit.cutAction,
              "COPY", DefaultEditorKit.copyAction,
              "PASTE", DefaultEditorKit.pasteAction,
              "CUT", DefaultEditorKit.cutAction,
              "shift LEFT", DefaultEditorKit.selectionBackwardAction,
              "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
              "shift RIGHT", DefaultEditorKit.selectionForwardAction,
              "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
              "ctrl LEFT", DefaultEditorKit.previousWordAction,
              "ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
              "ctrl RIGHT", DefaultEditorKit.nextWordAction,
              "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
              "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
              "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
              "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
              "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
              "meta A", DefaultEditorKit.selectAllAction,
              "HOME", DefaultEditorKit.beginLineAction,
              "END", DefaultEditorKit.endLineAction,
              "shift HOME", DefaultEditorKit.selectionBeginLineAction,
              "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction, // FJE
              "shift END", DefaultEditorKit.selectionEndLineAction,
              "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction, // FJE
              "UP", DefaultEditorKit.upAction,
              "KP_UP", DefaultEditorKit.upAction,
              "DOWN", DefaultEditorKit.downAction,
              "KP_DOWN", DefaultEditorKit.downAction,
              "PAGE_UP", DefaultEditorKit.pageUpAction,
              "PAGE_DOWN", DefaultEditorKit.pageDownAction,
              "shift PAGE_UP", "selection-page-up", // not 'public' in DefaultEditorKit
              "shift PAGE_DOWN", "selection-page-down",
              "ctrl shift PAGE_UP", "selection-page-left",
              "ctrl shift PAGE_DOWN", "selection-page-right",
              "shift UP", DefaultEditorKit.selectionUpAction,
              "shift KP_UP", DefaultEditorKit.selectionUpAction,
              "shift DOWN", DefaultEditorKit.selectionDownAction,
              "shift KP_DOWN", DefaultEditorKit.selectionDownAction,
              "ENTER", DefaultEditorKit.insertBreakAction,
              "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
              "ctrl H", DefaultEditorKit.deletePrevCharAction,
              "DELETE", DefaultEditorKit.deleteNextCharAction,
              "RIGHT", DefaultEditorKit.forwardAction,
              "LEFT", DefaultEditorKit.backwardAction,
              "KP_RIGHT", DefaultEditorKit.forwardAction,
              "KP_LEFT", DefaultEditorKit.backwardAction,
              "TAB", DefaultEditorKit.insertTabAction,
              "ctrl BACK_SLASH", "unselect",
              "ctrl HOME", DefaultEditorKit.beginAction,
              "ctrl END", DefaultEditorKit.endAction,
              "ctrl shift HOME", DefaultEditorKit.selectionBeginAction,
              "ctrl shift END", DefaultEditorKit.selectionEndAction,
              "ctrl T", "next-link-action",
              "ctrl shift T", "previous-link-action",
              "ctrl SPACE", "activate-link-action",
              // "control shift O", "toggle-componentOrientation", //
              // DefaultEditorKit.toggleComponentOrientation
              // FJE: {
              "meta LEFT", DefaultEditorKit.beginLineAction,
              "meta KP_LEFT", DefaultEditorKit.beginLineAction,
              "meta RIGHT", DefaultEditorKit.endLineAction,
              "meta KP_RIGHT", DefaultEditorKit.endLineAction,
              "meta HOME", DefaultEditorKit.beginAction, // added 1.5.0
              "meta END", DefaultEditorKit.endAction, // added 1.5.0
              // }
              // @formatter:on
            });

    Object[] defaults = {
      // @formatter:off
      "TextField.focusInputMap",
      fieldInputMap,
      "PasswordField.focusInputMap",
      passwordInputMap,
      "TextArea.focusInputMap",
      multilineInputMap,
      "TextPane.focusInputMap",
      multilineInputMap,
      "EditorPane.focusInputMap",
      multilineInputMap,
      "FormattedTextField.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "meta C", DefaultEditorKit.copyAction,
            "meta V", DefaultEditorKit.pasteAction,
            "meta X", DefaultEditorKit.cutAction,
            "COPY", DefaultEditorKit.copyAction,
            "PASTE", DefaultEditorKit.pasteAction,
            "CUT", DefaultEditorKit.cutAction,
            "shift LEFT", DefaultEditorKit.selectionBackwardAction,
            "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
            "shift RIGHT", DefaultEditorKit.selectionForwardAction,
            "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
            "ctrl LEFT", DefaultEditorKit.previousWordAction,
            "ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
            "ctrl RIGHT", DefaultEditorKit.nextWordAction,
            "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
            "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
            "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
            "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
            "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
            "meta A", DefaultEditorKit.selectAllAction,
            "HOME", DefaultEditorKit.beginLineAction,
            "END", DefaultEditorKit.endLineAction,
            "shift HOME", DefaultEditorKit.selectionBeginLineAction,
            "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction, // FJE
            "shift END", DefaultEditorKit.selectionEndLineAction,
            "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction, // FJE
            "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
            "ctrl H", DefaultEditorKit.deletePrevCharAction,
            "DELETE", DefaultEditorKit.deleteNextCharAction,
            "RIGHT", DefaultEditorKit.forwardAction,
            "LEFT", DefaultEditorKit.backwardAction,
            "KP_RIGHT", DefaultEditorKit.forwardAction,
            "KP_LEFT", DefaultEditorKit.backwardAction,
            "ENTER", JTextField.notifyAction,
            "ctrl BACK_SLASH", "unselect",
            // "ctrl shift O", "toggle-componentOrientation",
            "ESCAPE", "reset-field-edit",
            "UP", "increment",
            "KP_UP", "increment",
            "DOWN", "decrement",
            "KP_DOWN", "decrement",
            // FJE: {
            "meta LEFT", DefaultEditorKit.beginLineAction,
            "meta KP_LEFT", DefaultEditorKit.beginLineAction,
            "meta RIGHT", DefaultEditorKit.endLineAction,
            "meta KP_RIGHT", DefaultEditorKit.endLineAction,
            // }
          }),
      "Button.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "SPACE", "pressed",
            "released SPACE", "released"
          }),
      "CheckBox.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "SPACE", "pressed",
            "released SPACE", "released"
          }),
      "RadioButton.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "SPACE", "pressed",
            "released SPACE", "released"
          }),
      "ToggleButton.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "SPACE", "pressed",
            "released SPACE", "released"
          }),
      "FileChooser.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "ESCAPE", "cancelSelection",
            "F2", "editFileName",
            "F5", "refresh",
            "BACK_SPACE", "Go Up",
            "ENTER", "approveSelection"
          }),
      "Slider.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "RIGHT", "positiveUnitIncrement",
            "KP_RIGHT", "positiveUnitIncrement",
            "DOWN", "negativeUnitIncrement",
            "KP_DOWN", "negativeUnitIncrement",
            "PAGE_DOWN", "negativeBlockIncrement",
            "ctrl PAGE_DOWN", "negativeBlockIncrement",
            "LEFT", "negativeUnitIncrement",
            "KP_LEFT", "negativeUnitIncrement",
            "UP", "positiveUnitIncrement",
            "KP_UP", "positiveUnitIncrement",
            "PAGE_UP", "positiveBlockIncrement",
            "ctrl PAGE_UP", "positiveBlockIncrement",
            "HOME", "minScroll",
            "meta LEFT", "minScroll", // FJE
            "END", "maxScroll",
            "meta RIGHT", "maxScroll" // FJE
          }),
      "ComboBox.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "ESCAPE", "hidePopup",
            "PAGE_UP", "pageUpPassThrough",
            "PAGE_DOWN", "pageDownPassThrough",
            "HOME", "homePassThrough",
            "END", "endPassThrough",
            "DOWN", "selectNext",
            "KP_DOWN", "selectNext",
            "alt DOWN", "togglePopup",
            "alt KP_DOWN", "togglePopup",
            "alt UP", "togglePopup",
            "alt KP_UP", "togglePopup",
            "SPACE", "spacePopup",
            "ENTER", "enterPressed",
            "UP", "selectPrevious",
            "KP_UP", "selectPrevious"
          }),
      "Desktop.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "ctrl F5", "restore",
            "ctrl F4", "close",
            "ctrl F7", "move",
            "ctrl F8", "resize",
            "RIGHT", "right",
            "KP_RIGHT", "right",
            "shift RIGHT", "shrinkRight",
            "shift KP_RIGHT", "shrinkRight",
            "LEFT", "left",
            "KP_LEFT", "left",
            "shift LEFT", "shrinkLeft",
            "shift KP_LEFT", "shrinkLeft",
            "UP", "up",
            "KP_UP", "up",
            "shift UP", "shrinkUp",
            "shift KP_UP", "shrinkUp",
            "DOWN", "down",
            "KP_DOWN", "down",
            "shift DOWN", "shrinkDown",
            "shift KP_DOWN", "shrinkDown",
            "ESCAPE", "escape",
            "ctrl F9", "minimize",
            "ctrl F10", "maximize",
            "ctrl F6", "selectNextFrame", // FJE Should be (meta ~)?
            "ctrl TAB", "selectNextFrame",
            "meta Alt F6", "selectNextFrame",
            "shift meta Alt F6", "selectPreviousFrame",
            "ctrl F12", "navigateNext",
            "shift ctrl F12", "navigatePrevious"
          }),
      "List.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "meta C", "copy",
            "meta V", "paste",
            "meta X", "cut",
            "COPY", "copy",
            "PASTE", "paste",
            "CUT", "cut",
            "UP", "selectPreviousRow",
            "KP_UP", "selectPreviousRow",
            "shift UP", "selectPreviousRowExtendSelection",
            "shift KP_UP", "selectPreviousRowExtendSelection",
            "ctrl shift UP", "selectPreviousRowExtendSelection",
            "ctrl shift KP_UP", "selectPreviousRowExtendSelection",
            "ctrl UP", "selectPreviousRowChangeLead",
            "ctrl KP_UP", "selectPreviousRowChangeLead",
            "DOWN", "selectNextRow",
            "KP_DOWN", "selectNextRow",
            "shift DOWN", "selectNextRowExtendSelection",
            "shift KP_DOWN", "selectNextRowExtendSelection",
            "ctrl shift DOWN", "selectNextRowExtendSelection",
            "ctrl shift KP_DOWN", "selectNextRowExtendSelection",
            "ctrl DOWN", "selectNextRowChangeLead",
            "ctrl KP_DOWN", "selectNextRowChangeLead",
            "LEFT", "selectPreviousColumn",
            "KP_LEFT", "selectPreviousColumn",
            "shift LEFT", "selectPreviousColumnExtendSelection",
            "shift KP_LEFT", "selectPreviousColumnExtendSelection",
            "ctrl shift LEFT", "selectPreviousColumnExtendSelection",
            "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection",
            "ctrl LEFT", "selectPreviousColumnChangeLead",
            "ctrl KP_LEFT", "selectPreviousColumnChangeLead",
            "RIGHT", "selectNextColumn",
            "KP_RIGHT", "selectNextColumn",
            "shift RIGHT", "selectNextColumnExtendSelection",
            "shift KP_RIGHT", "selectNextColumnExtendSelection",
            "ctrl shift RIGHT", "selectNextColumnExtendSelection",
            "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection",
            "ctrl RIGHT", "selectNextColumnChangeLead",
            "ctrl KP_RIGHT", "selectNextColumnChangeLead",
            "HOME", "selectFirstRow",
            "shift HOME", "selectFirstRowExtendSelection",
            "ctrl shift HOME", "selectFirstRowExtendSelection",
            "ctrl HOME", "selectFirstRowChangeLead",
            "END", "selectLastRow",
            "shift END", "selectLastRowExtendSelection",
            "ctrl shift END", "selectLastRowExtendSelection",
            "ctrl END", "selectLastRowChangeLead",
            "PAGE_UP", "scrollUp",
            "shift PAGE_UP", "scrollUpExtendSelection",
            "ctrl shift PAGE_UP", "scrollUpExtendSelection",
            "ctrl PAGE_UP", "scrollUpChangeLead",
            "PAGE_DOWN", "scrollDown",
            "shift PAGE_DOWN", "scrollDownExtendSelection",
            "ctrl shift PAGE_DOWN", "scrollDownExtendSelection",
            "ctrl PAGE_DOWN", "scrollDownChangeLead",
            "meta A", "selectAll",
            "ctrl SLASH", "selectAll",
            "ctrl BACK_SLASH", "clearSelection",
            "SPACE", "addToSelection",
            "ctrl SPACE", "toggleAndAnchor",
            "shift SPACE", "extendTo",
            "ctrl shift SPACE", "moveSelectionTo"
          }),
      "ScrollBar.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "RIGHT", "positiveUnitIncrement",
            "KP_RIGHT", "positiveUnitIncrement",
            "DOWN", "positiveUnitIncrement",
            "KP_DOWN", "positiveUnitIncrement",
            "PAGE_DOWN", "positiveBlockIncrement",
            "LEFT", "negativeUnitIncrement",
            "KP_LEFT", "negativeUnitIncrement",
            "UP", "negativeUnitIncrement",
            "KP_UP", "negativeUnitIncrement",
            "PAGE_UP", "negativeBlockIncrement",
            "HOME", "minScroll",
            "meta LEFT", "minScroll", // FJE
            "END", "maxScroll",
            "meta RIGHT", "maxScroll" // FJE
          }),
      "ScrollPane.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "RIGHT", "unitScrollRight",
            "KP_RIGHT", "unitScrollRight",
            "DOWN", "unitScrollDown",
            "KP_DOWN", "unitScrollDown",
            "LEFT", "unitScrollLeft",
            "KP_LEFT", "unitScrollLeft",
            "UP", "unitScrollUp",
            "KP_UP", "unitScrollUp",
            "PAGE_UP", "scrollUp",
            "PAGE_DOWN", "scrollDown",
            "ctrl PAGE_UP", "scrollLeft",
            "ctrl PAGE_DOWN", "scrollRight",
            "ctrl HOME", "scrollHome",
            "meta ctrl LEFT", "scrollHome", // FJE
            "meta ctrl RIGHT", "scrollEnd", // FJE
            "ctrl END", "scrollEnd"
          }),
      "TabbedPane.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "RIGHT", "navigateRight",
            "KP_RIGHT", "navigateRight",
            "LEFT", "navigateLeft",
            "KP_LEFT", "navigateLeft",
            "UP", "navigateUp",
            "KP_UP", "navigateUp",
            "DOWN", "navigateDown",
            "KP_DOWN", "navigateDown",
            "ctrl DOWN", "requestFocusForVisibleComponent",
            "ctrl KP_DOWN", "requestFocusForVisibleComponent",
          }),
      "TabbedPane.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "ctrl PAGE_DOWN", "navigatePageDown",
            "ctrl PAGE_UP", "navigatePageUp",
            "ctrl UP", "requestFocus",
            "ctrl KP_UP", "requestFocus",
          }),
      "Table.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "meta C", "copy",
            "meta V", "paste",
            "meta X", "cut",
            "COPY", "copy",
            "PASTE", "paste",
            "CUT", "cut",
            "RIGHT", "selectNextColumn",
            "KP_RIGHT", "selectNextColumn",
            "shift RIGHT", "selectNextColumnExtendSelection",
            "shift KP_RIGHT", "selectNextColumnExtendSelection",
            "ctrl shift RIGHT", "selectNextColumnExtendSelection",
            "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection",
            "ctrl RIGHT", "selectNextColumnChangeLead",
            "ctrl KP_RIGHT", "selectNextColumnChangeLead",
            "LEFT", "selectPreviousColumn",
            "KP_LEFT", "selectPreviousColumn",
            "shift LEFT", "selectPreviousColumnExtendSelection",
            "shift KP_LEFT", "selectPreviousColumnExtendSelection",
            "ctrl shift LEFT", "selectPreviousColumnExtendSelection",
            "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection",
            "ctrl LEFT", "selectPreviousColumnChangeLead",
            "ctrl KP_LEFT", "selectPreviousColumnChangeLead",
            "DOWN", "selectNextRow",
            "KP_DOWN", "selectNextRow",
            "shift DOWN", "selectNextRowExtendSelection",
            "shift KP_DOWN", "selectNextRowExtendSelection",
            "ctrl shift DOWN", "selectNextRowExtendSelection",
            "ctrl shift KP_DOWN", "selectNextRowExtendSelection",
            "ctrl DOWN", "selectNextRowChangeLead",
            "ctrl KP_DOWN", "selectNextRowChangeLead",
            "UP", "selectPreviousRow",
            "KP_UP", "selectPreviousRow",
            "shift UP", "selectPreviousRowExtendSelection",
            "shift KP_UP", "selectPreviousRowExtendSelection",
            "ctrl shift UP", "selectPreviousRowExtendSelection",
            "ctrl shift KP_UP", "selectPreviousRowExtendSelection",
            "ctrl UP", "selectPreviousRowChangeLead",
            "ctrl KP_UP", "selectPreviousRowChangeLead",
            "HOME", "selectFirstColumn",
            "shift HOME", "selectFirstColumnExtendSelection",
            "ctrl shift HOME", "selectFirstRowExtendSelection",
            "ctrl HOME", "selectFirstRow",
            "END", "selectLastColumn",
            "shift END", "selectLastColumnExtendSelection",
            "ctrl shift END", "selectLastRowExtendSelection",
            "ctrl END", "selectLastRow",
            "PAGE_UP", "scrollUpChangeSelection",
            "shift PAGE_UP", "scrollUpExtendSelection",
            "ctrl shift PAGE_UP", "scrollLeftExtendSelection",
            "ctrl PAGE_UP", "scrollLeftChangeSelection",
            "PAGE_DOWN", "scrollDownChangeSelection",
            "shift PAGE_DOWN", "scrollDownExtendSelection",
            "ctrl shift PAGE_DOWN", "scrollRightExtendSelection",
            "ctrl PAGE_DOWN", "scrollRightChangeSelection",
            "TAB", "selectNextColumnCell",
            "shift TAB", "selectPreviousColumnCell",
            "ENTER", "selectNextRowCell",
            "shift ENTER", "selectPreviousRowCell",
            "meta A", "selectAll",
            "ctrl SLASH", "selectAll",
            "ctrl BACK_SLASH", "clearSelection",
            "ESCAPE", "cancel",
            "F2", "startEditing",
            "SPACE", "addToSelection",
            "ctrl SPACE", "toggleAndAnchor",
            "shift SPACE", "extendTo",
            "ctrl shift SPACE", "moveSelectionTo"
          }),
      "Spinner.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "UP", "increment",
            "KP_UP", "increment",
            "DOWN", "decrement",
            "KP_DOWN", "decrement",
          }),
      "SplitPane.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "UP", "negativeIncrement",
            "DOWN", "positiveIncrement",
            "LEFT", "negativeIncrement",
            "RIGHT", "positiveIncrement",
            "KP_UP", "negativeIncrement",
            "KP_DOWN", "positiveIncrement",
            "KP_LEFT", "negativeIncrement",
            "KP_RIGHT", "positiveIncrement",
            "HOME", "selectMin",
            "END", "selectMax",
            "F8", "startResize",
            "F6", "toggleFocus",
            "ctrl TAB", "focusOutForward",
            "ctrl shift TAB", "focusOutBackward"
          }),
      "Tree.focusInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "ADD", "expand",
            "SUBTRACT", "collapse",
            "meta C", "copy",
            "meta V", "paste",
            "meta X", "cut",
            "COPY", "copy",
            "PASTE", "paste",
            "CUT", "cut",
            "UP", "selectPrevious",
            "KP_UP", "selectPrevious",
            "shift UP", "selectPreviousExtendSelection",
            "shift KP_UP", "selectPreviousExtendSelection",
            "ctrl shift UP", "selectPreviousExtendSelection",
            "ctrl shift KP_UP", "selectPreviousExtendSelection",
            "ctrl UP", "selectPreviousChangeLead",
            "ctrl KP_UP", "selectPreviousChangeLead",
            "DOWN", "selectNext",
            "KP_DOWN", "selectNext",
            "shift DOWN", "selectNextExtendSelection",
            "shift KP_DOWN", "selectNextExtendSelection",
            "ctrl shift DOWN", "selectNextExtendSelection",
            "ctrl shift KP_DOWN", "selectNextExtendSelection",
            "ctrl DOWN", "selectNextChangeLead",
            "ctrl KP_DOWN", "selectNextChangeLead",
            "RIGHT", "selectChild",
            "KP_RIGHT", "selectChild",
            "LEFT", "selectParent",
            "KP_LEFT", "selectParent",
            "PAGE_UP", "scrollUpChangeSelection",
            "shift PAGE_UP", "scrollUpExtendSelection",
            "ctrl shift PAGE_UP", "scrollUpExtendSelection",
            "ctrl PAGE_UP", "scrollUpChangeLead",
            "PAGE_DOWN", "scrollDownChangeSelection",
            "shift PAGE_DOWN", "scrollDownExtendSelection",
            "ctrl shift PAGE_DOWN", "scrollDownExtendSelection",
            "ctrl PAGE_DOWN", "scrollDownChangeLead",
            "HOME", "selectFirst",
            "shift HOME", "selectFirstExtendSelection",
            "ctrl shift HOME", "selectFirstExtendSelection",
            "ctrl HOME", "selectFirstChangeLead",
            "END", "selectLast",
            "shift END", "selectLastExtendSelection",
            "ctrl shift END", "selectLastExtendSelection",
            "ctrl END", "selectLastChangeLead",
            "F2", "startEditing",
            "meta A", "selectAll",
            "ctrl SLASH", "selectAll",
            "ctrl BACK_SLASH", "clearSelection",
            "ctrl LEFT", "scrollLeft",
            "ctrl KP_LEFT", "scrollLeft",
            "ctrl RIGHT", "scrollRight",
            "ctrl KP_RIGHT", "scrollRight",
            "SPACE", "addToSelection",
            "ctrl SPACE", "toggleAndAnchor",
            "shift SPACE", "extendTo",
            "ctrl shift SPACE", "moveSelectionTo"
          }),
      "Tree.ancestorInputMap",
      new UIDefaults.LazyInputMap(new Object[] {"ESCAPE", "cancel"}),
      "ToolBar.ancestorInputMap",
      new UIDefaults.LazyInputMap(
          new Object[] {
            "UP", "navigateUp",
            "KP_UP", "navigateUp",
            "DOWN", "navigateDown",
            "KP_DOWN", "navigateDown",
            "LEFT", "navigateLeft",
            "KP_LEFT", "navigateLeft",
            "RIGHT", "navigateRight",
            "KP_RIGHT", "navigateRight"
          }),
      // These bindings are only enabled when there is a default
      // button set on the rootpane.
      "RootPane.defaultButtonWindowKeyBindings",
      new Object[] {
        "ENTER", "press",
        "released ENTER", "release",
        "ctrl ENTER", "press",
        "ctrl released ENTER", "release"
      },
      // @formatter:on
    };
    table.putDefaults(defaults);
  }
}
