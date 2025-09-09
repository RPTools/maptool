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
package net.rptools.maptool.client.swing;

import com.jidesoft.dialog.ButtonPanel;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import net.rptools.maptool.language.I18N;

public enum ButtonKind {

  //  @spotless:off
  ACCEPT         ("ACCEPT"         , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.accept"        , "Button.accept.mnemonic"        , null, -1),
  ADD            ("ADD"            , ButtonPanel.OTHER_BUTTON      , "Button.add"           , "Button.add.mnemonic"           , null, -1),
  APPLY          ("APPLY"          , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.apply"         , "Button.apply.mnemonic"         , null, -1),
  BACK           ("BACK"           , ButtonPanel.OTHER_BUTTON      , "Button.back"          , "Button.back.mnemonic"          , null, -1),
  BROWSE         ("BROWSE"         , ButtonPanel.OTHER_BUTTON      , "Button.browse"        , "Button.browse.mnemonic"        , null, -1),
  CANCEL         ("CANCEL"         , ButtonPanel.CANCEL_BUTTON     , "Button.cancel"        , "Button.cancel.mnemonic"        , null, -1),
  CLEAR          ("CLEAR"          , ButtonPanel.OTHER_BUTTON      , "Button.clear"         , "Button.clear.mnemonic"         , null, -1),
  CLEAR_ALL      ("CLEAR_ALL"      , ButtonPanel.OTHER_BUTTON      , "Button.clearAll"      , "Button.clearAll.mnemonic"      , null, -1),
  CLOSE          ("CLOSE"          , ButtonPanel.CANCEL_BUTTON     , "Button.close"         , "Button.close.mnemonic"         , null, -1),
  CONTINUE       ("CONTINUE"       , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.continue"      , "Button.continue.mnemonic"      , null, -1),
  DELETE         ("DELETE"         , ButtonPanel.OTHER_BUTTON      , "Button.delete"        , "Button.delete.mnemonic"        , null, -1),
  DETAILS        ("DETAILS"        , ButtonPanel.OTHER_BUTTON      , "Button.details"       , "Button.details.mnemonic"       , null, -1),
  DISABLE        ("DISABLE"        , ButtonPanel.OTHER_BUTTON      , "Button.disable"       , "Button.disable.mnemonic"       , null, -1),
  EDIT           ("EDIT"           , ButtonPanel.OTHER_BUTTON      , "Button.edit"          , "Button.edit.mnemonic"          , null, -1),
  ENABLE         ("ENABLE"         , ButtonPanel.OTHER_BUTTON      , "Button.enable"        , "Button.enable.mnemonic"        , null, -1),
  EXIT           ("EXIT"           , ButtonPanel.CANCEL_BUTTON     , "Button.exit"          , "Button.exit.mnemonic"          , null, -1),
  EXPORT         ("EXPORT"         , ButtonPanel.OTHER_BUTTON      , "Button.export"        , "Button.export.mnemonic"        , null, -1),
  FIND           ("FIND"           , ButtonPanel.OTHER_BUTTON      , "Button.find"          , "Button.find.mnemonic"          , null, -1),
  FIND_NEXT      ("FIND_NEXT"      , ButtonPanel.OTHER_BUTTON      , "Button.findNext"      , "Button.findNext.mnemonic"      , null, -1),
  FINISH         ("FINISH"         , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.finish"        , "Button.finish.mnemonic"        , null, -1),
  FORWARD        ("FORWARD"        , ButtonPanel.OTHER_BUTTON      , "Button.forward"       , "Button.forward.mnemonic"       , null, -1),
  HELP           ("HELP"           , ButtonPanel.HELP_BUTTON       , "Button.help"          , "Button.help.mnemonic"          , null, -1),
  HIDE_DETAILS   ("HIDE_DETAILS"   , ButtonPanel.OTHER_BUTTON      , "Button.hideDetails"   , "Button.hideDetails.mnemonic"   , null, -1),
  INSTALL        ("INSTALL"        , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.install"       , "Button.install.mnemonic"       , null, -1),
  IMPORT         ("IMPORT"         , ButtonPanel.OTHER_BUTTON      , "Button.import"        , "Button.import.mnemonic"        , null, -1),
  NEW            ("NEW"            , ButtonPanel.OTHER_BUTTON      , "Button.new"           , "Button.new.mnemonic"           , null, -1),
  NEXT           ("NEXT"           , ButtonPanel.OTHER_BUTTON      , "Button.next"          , "Button.next.mnemonic"          , null, -1),
  NETWORKING_HELP("NETWORKING_HELP", ButtonPanel.HELP              , "Button.networkingHelp", "Button.networkingHelp.mnemonic", null, -1),
  NO             ("NO"             , ButtonPanel.CANCEL_BUTTON     , "Button.no"            , "Button.no.mnemonic"            , null, -1),
  OK             ("OK"             , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.ok"            , "Button.ok.mnemonic"            , null, -1),
  OPEN           ("OPEN"           , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.open"          , "Button.open.mnemonic"          , null, -1),
  PRINT          ("PRINT"          , ButtonPanel.OTHER_BUTTON      , "Button.print"         , "Button.print.mnemonic"         , null, -1),
  REFRESH        ("REFRESH"        , ButtonPanel.OTHER_BUTTON      , "Button.refresh"       , "Button.refresh.mnemonic"       , null, -1),
  REPLACE        ("REPLACE"        , ButtonPanel.OTHER_BUTTON      , "Button.replace"       , "Button.replace.mnemonic"       , null, -1),
  RESET          ("RESET"          , ButtonPanel.OTHER_BUTTON      , "Button.reset"         , "Button.reset.mnemonic"         , null, -1),
  RETRY          ("RETRY"          , ButtonPanel.OTHER_BUTTON      , "Button.retry"         , "Button.retry.mnemonic"         , null, -1),
  REVERT         ("REVERT"         , ButtonPanel.OTHER_BUTTON      , "Button.revert"        , "Button.revert.mnemonic"        , null, -1),
  SAVE           ("SAVE"           , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.save"          , "Button.save.mnemonic"          , null, -1),
  SAVE_AS        ("SAVE_AS"        , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.saveAs"        , "Button.saveAs.mnemonic"        , null, -1),
  SHOW_DETAILS   ("SHOW_DETAILS"   , ButtonPanel.OTHER_BUTTON      , "Button.showDetails"   , "Button.showDetails.mnemonic"   , null, -1),
  STOP           ("STOP"           , ButtonPanel.OTHER_BUTTON      , "Button.stop"          , "Button.stop.mnemonic"          , null, -1),
  UPDATE         ("UPDATE"         , ButtonPanel.OTHER_BUTTON      , "Button.update"        , "Button.update.mnemonic"        , null, -1),
  YES            ("YES"            , ButtonPanel.AFFIRMATIVE_BUTTON, "Button.yes"           , "Button.yes.mnemonic"           , null, -1),
  MOVE_UP        ("MOVE_UP"        , ButtonPanel.OTHER_BUTTON      , "Button.moveUp"        , "Button.moveUp.mnemonic"        , null, -1),
  MOVE_DOWN      ("MOVE_DOWN"      , ButtonPanel.OTHER_BUTTON      , "Button.moveDown"      , "Button.moveDown.mnemonic"      , null, -1);
  //  @spotless:on
  final String name;
  final String buttonPanelButtonType;
  final String i18nKey;
  final String i18nMnemonicKey;
  final String i18nText;
  final int i18nMnemonicKeyCode;
  final Map<String, Integer> hardCodedShortcuts;

  {
    hardCodedShortcuts =
        new HashMap<>() {
          {
            put("Button.back.mnemonic", KeyEvent.VK_LEFT);
            put("Button.cancel.mnemonic", KeyEvent.VK_ESCAPE);
            put("Button.close.mnemonic", KeyEvent.VK_ESCAPE);
            put("Button.forward.mnemonic", KeyEvent.VK_RIGHT);
            put("Button.help.mnemonic", KeyEvent.VK_F1);
            put("Button.networkingHelp.mnemonic", KeyEvent.VK_F1);
            put("Button.moveDown.mnemonic", KeyEvent.VK_DOWN);
            put("Button.moveUp.mnemonic", KeyEvent.VK_UP);
            put("Button.stop.mnemonic", KeyEvent.VK_ESCAPE);
          }
        };
  }

  ButtonKind(
      String name,
      String buttonPanelButtonType,
      String i18nKey,
      String i18nMnemonicKey,
      String ignoredI18nText,
      int ignoredI18nMnemonicKeyCode) {
    this.name = name;
    this.buttonPanelButtonType = buttonPanelButtonType;
    this.i18nKey = i18nKey;
    this.i18nMnemonicKey = i18nMnemonicKey;
    this.i18nText = I18N.getText(i18nKey);
    this.i18nMnemonicKeyCode =
        hardCodedShortcuts.getOrDefault(
            i18nMnemonicKey, KeyEvent.getExtendedKeyCodeForChar(i18nMnemonicKey.trim().charAt(0)));
  }
}
