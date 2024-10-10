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
package net.rptools.maptool.client.ui.commandpanel;

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.ChatMessageAdded;
import net.rptools.maptool.client.events.PreferencesChanged;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.chat.ChatProcessor;
import net.rptools.maptool.client.ui.chat.SmileyChatTranslationRuleGroup;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.tokens.TokenPanelChanged;
import net.rptools.maptool.model.zones.TokenEdited;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;

public class CommandPanel extends JPanel {
  private static final long serialVersionUID = 8710948417044703674L;

  private final List<String> commandHistory = new LinkedList<String>();

  private JLabel characterLabel;
  private JTextPane commandTextArea;
  private MessagePanel messagePanel;
  private int commandHistoryIndex;
  private TextColorWell textColorWell;
  private JToggleButton scrollLockButton;
  private JToggleButton chatNotifyButton;
  private AvatarPanel avatarPanel;
  private JPopupMenu emotePopup;
  private JButton emotePopupButton;
  private String typedCommandBuffer;
  private BufferedImage cancelButton =
      ImageUtil.createCompatibleImage(
          RessourceManager.getSmallIcon(Icons.ACTION_CANCEL).getImage());

  // Chat timers
  // private long chatNotifyDuration; // Initialize it on first load
  // private Timer chatTimer;

  private ChatProcessor chatProcessor;

  /** The impersonated identity as displayed in the Impersonate panel. */
  private TokenIdentity globalIdentity = new TokenIdentity();

  /** The stack of impersonated identities. The most current is at the top of the stack. */
  private final Stack<TokenIdentity> identityStack = new Stack<>();

  /** The identity representing no impersonation. */
  private static final TokenIdentity emptyIdentity = new TokenIdentity();

  public CommandPanel() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createLineBorder(Color.gray));
    add(BorderLayout.SOUTH, createSouthPanel());
    add(BorderLayout.CENTER, getMessagePanel());
    initializeSmilies();
    addFocusHotKey();

    new MapToolEventBus().getMainEventBus().register(this);
  }

  public ChatProcessor getChatProcessor() {
    return chatProcessor;
  }

  private void initializeSmilies() {
    SmileyChatTranslationRuleGroup smileyRuleGroup = new SmileyChatTranslationRuleGroup();
    emotePopup = smileyRuleGroup.getEmotePopup();

    chatProcessor = new ChatProcessor();
    chatProcessor.install(smileyRuleGroup);
  }

  /** Clears both the identity stack and the global identity. */
  public void clearAllIdentities() {
    identityStack.clear();
    setGlobalIdentity(emptyIdentity);
  }

  /**
   * Whether the player is currently impersonating a token
   *
   * @return {@code true} if there is a token being impersonated.
   */
  public boolean isImpersonating() {
    return getCurrentIdentity().hasName();
  }

  /**
   * The name currently in use; if the user is not impersonating a token, this will return the
   * player's name.
   *
   * @return the identity to use.
   */
  public String getIdentity() {
    return getCurrentIdentity().getIdentity();
  }

  /**
   * If the current impersonation was assigned using a GUID, that value is returned. This allows the
   * calling code to find a specific token even if there are duplicate names. If a GUID was not used
   * (perhaps an arbitrary strings was used) then <code>null</code> is returned.
   *
   * @return ID of the token that is being impersonated if it was set via ID.
   */
  public GUID getIdentityGUID() {
    return getCurrentIdentity().getIdentityGUID();
  }

  /**
   * Globally impersonates a token.
   *
   * @param token the token to impersonate.
   */
  public void setGlobalIdentity(Token token) {
    setGlobalIdentity(new TokenIdentity(token));
  }

  /** Clears the globally impersonated token. */
  public void clearGlobalIdentity() {
    setGlobalIdentity(emptyIdentity);
  }

  /**
   * Changes the globally impersonated identity.
   *
   * @param globalIdentity the identity to impersonate
   */
  public void setGlobalIdentity(TokenIdentity globalIdentity) {
    this.globalIdentity = globalIdentity;
    Token token = globalIdentity.getToken();

    // Change the impersonated panel
    if (token == null || !globalIdentity.canImpersonate) {
      MapTool.getFrame().getImpersonatePanel().stopImpersonating();
    } else {
      MapTool.getFrame().getImpersonatePanel().startImpersonating(token);
    }
    // Update the image and label.
    updateImageAndLabel(token, globalIdentity.getCharacterLabel());

    // Fires the event for impersonation.
    HTMLFrameFactory.impersonateToken();
  }

  /** Refreshes the global identity so that it matches the impersonated token. */
  public void refreshGlobalIdentity() {
    if (globalIdentity.getIdentityGUID() != null) {
      TokenIdentity identity = globalIdentity;
      identity = new TokenIdentity(identity.getIdentityGUID(), identity.getIdentity());
      setGlobalIdentity(identity);
    }
  }

  /**
   * Change the current impersonated identity. If the identityStack is empty, change the global
   * identity; otherwise, change the current temporary one.
   *
   * @param macroIdentity the identity to change to
   */
  public void setIdentity(TokenIdentity macroIdentity) {
    if (identityStack.isEmpty()) {
      setGlobalIdentity(macroIdentity);
    } else {
      identityStack.pop();
      enterContextIdentity(macroIdentity);
    }
  }

  /**
   * @return whether the current identity is a token.
   */
  public boolean isImpersonatingToken() {
    return getCurrentIdentity().validToken();
  }

  /**
   * @return whether the global identity is a token.
   */
  public boolean isGlobalImpersonatingToken() {
    return globalIdentity.validToken();
  }

  /**
   * Enters an identity context in which the identity is temporarily different.
   *
   * @param macroIdentity the identity to temporarily adopt.
   */
  public void enterContextIdentity(TokenIdentity macroIdentity) {
    identityStack.push(macroIdentity);
  }

  /**
   * Leaves the current identity context. Returns to the previous in the identity stack or to the
   * global one if the stack is empty.
   */
  public void leaveContextIdentity() {
    identityStack.pop();
  }

  /**
   * Gets the current identity. The current identity is the one at the top of the stack, or the
   * current one if it is empty.
   *
   * @return the current identity
   */
  private TokenIdentity getCurrentIdentity() {
    return identityStack.isEmpty() ? globalIdentity : identityStack.peek();
  }

  /**
   * Updates the label and the image displayed in the chat.
   *
   * @param token the token to change the image and label to
   * @param label the backup label to set the name to if the token is null
   */
  private void updateImageAndLabel(Token token, String label) {
    BufferedImage image = null;
    if (token != null) {
      image = ImageManager.getImageAndWait(token.getImageAssetId());
    }
    avatarPanel.setImage(image);
    setCharacterLabel(token == null ? label : token.getName());
  }

  private boolean isTokenImpersonated(Token token) {
    return token != null && token.getId().equals(globalIdentity.getIdentityGUID());
  }

  private Token getImpersonatedAmongList(List<Token> list) {
    for (Token token : list) {
      if (isTokenImpersonated(token)) {
        return token;
      }
    }
    return null;
  }

  @Subscribe
  private void onTokenPanelChanged(TokenPanelChanged event) {
    updateIdentityIfImpersonatedChanged(Collections.singletonList(event.token()));
  }

  @Subscribe
  private void onTokenEdited(TokenEdited event) {
    updateIdentityIfImpersonatedChanged(Collections.singletonList(event.token()));
  }

  private void updateIdentityIfImpersonatedChanged(List<Token> tokens) {
    GUID tokenId = globalIdentity.getIdentityGUID();
    if (tokenId != null) {
      // If the impersonated token has changed, update the identity

      Token impersonated = getImpersonatedAmongList(tokens);
      if (impersonated != null) {
        setGlobalIdentity(new TokenIdentity(impersonated));
      }
    }
  }

  @Subscribe
  void onPreferencesChanged(PreferencesChanged event) {
    // Resize on demand
    if (commandTextArea != null) {
      commandTextArea.setFont(
          commandTextArea.getFont().deriveFont((float) AppPreferences.fontSize.get()));
      doLayout();
    }

    // Update whenever the preferences change
    if (messagePanel != null) {
      messagePanel.refreshRenderer();
    }
  }

  @Subscribe
  void onChatMessageAdded(ChatMessageAdded event) {
    addMessage(event.message());
    System.out.printf("Added message %s%n", event.message());
  }

  /**
   * Class describing an identity that can be impersonated. The identity can be a token, or any
   * specified name.
   */
  public static class TokenIdentity {
    /** The name of the identity. If null, nothing is impersonated. */
    private final String identityName;

    /** The GUID of the identity. */
    private final GUID identityGUID;

    /** Whether the player is allowed to set the token in the Impersonate panel. */
    private final boolean canImpersonate;

    /** Creates an empty identity (nothing impersonated). */
    public TokenIdentity() {
      identityName = null;
      identityGUID = null;
      canImpersonate = false;
    }

    /**
     * Creates an identity from the token. If null, nothing is impersonated.
     *
     * @param token the token to impersonate
     */
    TokenIdentity(Token token) {
      this(token, null);
    }

    /**
     * Creates an identity from a name. If null, nothing is impersonated.
     *
     * @param name the name to impersonate
     */
    TokenIdentity(String name) {
      this(null, name, false);
    }

    /**
     * Creates an identity from a token. If the token is null, the identity uses the specified
     * backup name.
     *
     * @param token the token to impersonate
     * @param backupName the backup name to impersonate if the token is null
     */
    public TokenIdentity(Token token, String backupName) {
      this(token, backupName, true);
    }

    /**
     * Creates an identity from a GUID. If there is no associated token, the identity uses the
     * specified backup name.
     *
     * @param tokenId the token GUID
     * @param backupName the backup name to impersonate if the token is null
     */
    public TokenIdentity(GUID tokenId, String backupName) {
      this(FindTokenFunctions.findToken(tokenId, null), backupName);
    }

    /**
     * Creates an identity from a token. If the token is null, the identity uses the specified
     * backup name. Impersonation through the Impersonate panel can be disabled.
     *
     * @param token the token to impersonate
     * @param backupName the backup name to impersonate if the token is null
     * @param canImpersonate whether the token can be impersonated in the Impersonate panel
     */
    public TokenIdentity(Token token, String backupName, boolean canImpersonate) {
      if (token != null) {
        this.identityGUID = token.getId();
        this.identityName = token.getName();
        this.canImpersonate = canImpersonate;
      } else {
        this.identityGUID = null;
        this.identityName = backupName;
        this.canImpersonate = false;
      }
    }

    /**
     * @return a string representing the identity.
     */
    public String getIdentity() {
      if (identityName == null) {
        if (identityGUID == null) return MapTool.getPlayer().getName();
        else return identityGUID.toString();
      }
      return identityName;
    }

    /**
     * @return a string for the character label of the identity.
     */
    public String getCharacterLabel() {
      return hasName() ? identityName : "";
    }

    /**
     * @return the GUID of the identity.
     */
    public GUID getIdentityGUID() {
      return identityGUID;
    }

    /**
     * @return the token of the identity.
     */
    public Token getToken() {
      return FindTokenFunctions.findToken(identityGUID, null);
    }

    /**
     * @return whether the identity has a name.
     */
    public boolean hasName() {
      return identityName != null;
    }

    /**
     * @return whether the token can still be found on the current map.
     */
    public boolean validToken() {
      if (identityGUID == null) {
        return false;
      } else {
        return FindTokenFunctions.findToken(identityGUID, null) != null;
      }
    }
  }

  public JButton getEmotePopupButton() {
    if (emotePopupButton == null) {
      emotePopupButton = new JButton(RessourceManager.getSmallIcon(Icons.CHAT_SMILEY));
      emotePopupButton.setMargin(new Insets(0, 0, 0, 0));
      emotePopupButton.setContentAreaFilled(false);
      emotePopupButton.setBorderPainted(false);
      emotePopupButton.setFocusPainted(false);
      emotePopupButton.setOpaque(false);
      emotePopupButton.addActionListener(e -> emotePopup.show(emotePopupButton, 0, 0));
    }
    return emotePopupButton;
  }

  public JToggleButton getScrollLockButton() {
    if (scrollLockButton == null) {
      scrollLockButton = new JToggleButton();
      scrollLockButton.setIcon(RessourceManager.getSmallIcon(Icons.CHAT_SCROLL_LOCK_OFF));
      scrollLockButton.setSelectedIcon(RessourceManager.getSmallIcon(Icons.CHAT_SCROLL_LOCK_ON));
      scrollLockButton.setToolTipText(I18N.getText("action.chat.scrolllock.tooltip"));
      scrollLockButton.setUI(new BasicToggleButtonUI());
      scrollLockButton.setBorderPainted(false);
      scrollLockButton.setFocusPainted(false);
      scrollLockButton.setPreferredSize(new Dimension(16, 16));
    }
    return scrollLockButton;
  }

  /**
   * Gets the button for sending or suppressing notification that a player is typing in the chat
   * text area.
   *
   * @return the notification button
   */
  public JToggleButton getNotifyButton() {
    if (chatNotifyButton == null) {
      chatNotifyButton = new JToggleButton();
      chatNotifyButton.setIcon(RessourceManager.getSmallIcon(Icons.CHAT_SHOW_TYPING_NOTIFICATION));
      chatNotifyButton.setSelectedIcon(
          RessourceManager.getSmallIcon(Icons.CHAT_HIDE_TYPING_NOTIFICATION));
      chatNotifyButton.setToolTipText(I18N.getText("action.chat.showhide.tooltip"));
      chatNotifyButton.setUI(new BasicToggleButtonUI());
      chatNotifyButton.setBorderPainted(false);
      chatNotifyButton.setFocusPainted(false);
      chatNotifyButton.setPreferredSize(new Dimension(16, 16));
      chatNotifyButton.addItemListener(
          new ItemListener() {
            private ChatTypingListener ours = null;

            public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                if (ours != null) commandTextArea.removeKeyListener(ours);
                ours = null;
                // Go ahead and turn off the chat panel right away.
                MapTool.getFrame().getChatTypingPanel().setVisible(false);
              } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (ours == null) ours = new ChatTypingListener();
                commandTextArea.addKeyListener(ours);
              }
            }
          });
    }
    return chatNotifyButton;
  }

  private JComponent createSouthPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel subPanel = new JPanel(new BorderLayout());

    subPanel.add(BorderLayout.EAST, createTextPropertiesPanel());
    subPanel.add(BorderLayout.NORTH, createCharacterLabel());
    subPanel.add(BorderLayout.CENTER, createCommandPanel());

    panel.add(BorderLayout.WEST, createAvatarPanel());
    panel.add(BorderLayout.CENTER, subPanel);

    return panel;
  }

  private JComponent createAvatarPanel() {
    avatarPanel = new AvatarPanel(new Dimension(60, 60));
    return avatarPanel;
  }

  private JComponent createCommandPanel() {
    JScrollPane pane =
        new JScrollPane(
            getCommandTextArea(),
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    return pane;
  }

  private JComponent createTextPropertiesPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;

    panel.add(getTextColorWell(), constraints);

    // constraints.gridy++;
    // panel.add(Box.createVerticalStrut(2), constraints);

    constraints.gridy++;
    panel.add(getScrollLockButton(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 0;
    panel.add(getEmotePopupButton(), constraints);

    constraints.gridy = 1;
    panel.add(getNotifyButton(), constraints);

    return panel;
  }

  public String getMessageHistory() {
    return messagePanel.getMessagesText();
  }

  public void setCharacterLabel(String label) {
    characterLabel.setText(label);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(50, 50);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  public JLabel createCharacterLabel() {
    characterLabel = new JLabel("", JLabel.LEFT);
    characterLabel.setText("");
    characterLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
    return characterLabel;
  }

  /**
   * Creates the label for live typing.
   *
   * @return a {@link JTextPane} to be displayed.
   */
  public JTextPane getCommandTextArea() {
    if (commandTextArea == null) {
      commandTextArea =
          new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
              super.paintComponent(g);

              Dimension size = getSize();
              g.setColor(Color.gray);
              g.drawLine(0, 0, size.width, 0);
            }
          };
      commandTextArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      commandTextArea.setPreferredSize(new Dimension(50, 40)); // XXX should be resizable
      commandTextArea.setFont(new Font("sans-serif", 0, AppPreferences.fontSize.get()));
      if (!ThemeSupport.shouldUseThemeColorsForChat()) {
        commandTextArea.setBackground(Color.WHITE);
        commandTextArea.setForeground(Color.BLACK);
      }
      commandTextArea.addKeyListener(new ChatTypingListener());
      SwingUtil.useAntiAliasing(commandTextArea);

      ActionMap actions = commandTextArea.getActionMap();
      actions.put(AppActions.COMMIT_COMMAND_ID, AppActions.COMMIT_COMMAND);
      actions.put(AppActions.ENTER_COMMAND_ID, AppActions.ENTER_COMMAND);
      actions.put(AppActions.CANCEL_COMMAND_ID, AppActions.CANCEL_COMMAND);
      actions.put(AppActions.COMMAND_UP_ID, new CommandHistoryUpAction());
      actions.put(AppActions.COMMAND_DOWN_ID, new CommandHistoryDownAction());
      actions.put(AppActions.NEWLINE_COMMAND_ID, AppActions.NEWLINE_COMMAND);

      InputMap inputs = commandTextArea.getInputMap();
      inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), AppActions.CANCEL_COMMAND_ID);
      inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), AppActions.COMMIT_COMMAND_ID);
      inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), AppActions.COMMAND_UP_ID);
      inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), AppActions.COMMAND_DOWN_ID);
      inputs.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK),
          AppActions.NEWLINE_COMMAND_ID);
    }
    return commandTextArea;
  }

  /**
   * KeyListener for command area to handle live typing notification. Implements an idle timer that
   * removes the typing notification after the duration set in AppPreferences expires.
   */
  private class ChatTypingListener extends KeyAdapter {
    @Override
    public void keyReleased(KeyEvent kre) {
      // Get the key released
      int key = kre.getKeyCode();

      if (key == KeyEvent.VK_ENTER) {
        // User hit enter, reset the label and stop the timer
        MapTool.serverCommand().setLiveTypingLabel(MapTool.getPlayer().getName(), false);
      } else if (!chatNotifyButton.isSelected()) {
        MapTool.serverCommand().setLiveTypingLabel(MapTool.getPlayer().getName(), true);
      }
    }
  }

  /**
   * Disables the chat notification toggle if the GM enforces notification
   *
   * @param disable whether to disable the toggle
   */
  public void disableNotifyButton(Boolean disable) {
    // Little clumsy, but when the menu item is _enabled_, the button should be _disabled_
    if (!MapTool.getPlayer().isGM()) {
      chatNotifyButton.setSelected(false);
      chatNotifyButton.setEnabled(!disable);
      maybeAddTypingListener();
    }
  }

  /*
   * FIXME: this is insufficient for stopping faked rolls; the user can still do something like &{"laquo;"}.
   */
  public static final Pattern CHEATER_PATTERN =
      Pattern.compile("\u00AB|\u00BB|&#171;?|&#187;?|&laquo;?|&raquo;?|&#xAB;?|&#xBB;?|\036|\037");

  /** Execute the command in the command field. */
  public void commitCommand() {
    String command = commandTextArea.getText().trim();
    // Command history
    // Don't store up a bunch of repeats
    if (commandHistory.size() == 0
        || !command.equals(commandHistory.get(commandHistory.size() - 1))) {
      commandHistory.add(command);
      typedCommandBuffer = null;
    }
    commandHistoryIndex = commandHistory.size();

    commitCommand(command, null);
    commandTextArea.setText("");
    MapTool.serverCommand().setLiveTypingLabel(MapTool.getPlayer().getName(), false);
  }

  /**
   * Execute the given command
   *
   * @param command The command to execute
   */
  public void commitCommand(String command) {
    commitCommand(command, null);
  }

  /**
   * Execute the given command
   *
   * @param command The command to execute
   * @param macroContext The context we are calling the macro in.
   */
  public void commitCommand(String command, MapToolMacroContext macroContext) {
    command = command.trim();
    if (command.length() == 0) {
      return;
    }

    // Detect whether the person is attempting to fake rolls.
    if (CHEATER_PATTERN.matcher(command).find()) {
      MapTool.addServerMessage(
          TextMessage.me(null, I18N.getString("msg.commandPanel.cheater.self")));
      MapTool.serverCommand()
          .message(
              TextMessage.gm(
                  null,
                  I18N.getText(
                      "msg.commandPanel.cheater.gm", MapTool.getPlayer().getName(), command)));
      return;
    }
    // Make sure they aren't trying to break out of the div
    // FIXME: as above, </{"div"}> can be used to get around this
    int divCount = StringUtil.countOccurances(command, "<div");
    int closeDivCount = StringUtil.countOccurances(command, "</div>");
    while (closeDivCount < divCount) {
      command += "</div>";
      closeDivCount++;
    }
    if (closeDivCount > divCount) {
      MapTool.addServerMessage(TextMessage.me(null, I18N.getString("msg.commandPanel.div")));
      return;
    }
    if (command.charAt(0) != '/') {
      // Assume a "SAY"
      command = "/s " + command;
    }
    MacroManager.executeMacro(command, macroContext);
  }

  public void clearMessagePanel() {
    messagePanel.clearMessages();
  }

  /** Cancel the current command in the command field. */
  public void cancelCommand() {
    commandTextArea.setText("");
    validate();
    // Why were we closing the chat window on Esc?
    // MapTool.getFrame().hideCommandPanel();
  }

  /** Inserts a newline into the chat input box. */
  public void insertNewline() {
    String text = commandTextArea.getText();
    commandTextArea.setText(text + "\n");
  }

  public void startMacro() {
    MapTool.getFrame().showCommandPanel();
    commandTextArea.requestFocusInWindow();
    commandTextArea.setText("/");
  }

  public void startChat() {
    MapTool.getFrame().showCommandPanel();
    commandTextArea.requestFocusInWindow();
  }

  public TextColorWell getTextColorWell() {
    if (textColorWell == null) {
      textColorWell = new TextColorWell();
    }
    return textColorWell;
  }

  private class CommandHistoryUpAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (commandHistory.size() == 0) {
        return;
      }
      if (commandHistoryIndex == commandHistory.size()) {
        typedCommandBuffer = getCommandTextArea().getText();
      }
      commandHistoryIndex--;
      if (commandHistoryIndex < 0) {
        commandHistoryIndex = 0;
      }
      commandTextArea.setText(commandHistory.get(commandHistoryIndex));
    }
  }

  private class CommandHistoryDownAction extends AbstractAction {
    private static final long serialVersionUID = 7070274680351186504L;

    public void actionPerformed(ActionEvent e) {
      if (commandHistory.size() == 0) {
        return;
      }
      commandHistoryIndex++;
      if (commandHistoryIndex == commandHistory.size()) {
        commandTextArea.setText(typedCommandBuffer != null ? typedCommandBuffer : "");
        commandHistoryIndex = commandHistory.size();
      } else if (commandHistoryIndex >= commandHistory.size()) {
        commandHistoryIndex--;
      } else {
        commandTextArea.setText(commandHistory.get(commandHistoryIndex));
      }
    }
  }

  @Override
  public void requestFocus() {
    commandTextArea.requestFocus();
  }

  private MessagePanel getMessagePanel() {
    if (messagePanel == null) {
      messagePanel = new MessagePanel();
    }
    return messagePanel;
  }

  private void addMessage(TextMessage message) {
    messagePanel.addMessage(message);
  }

  public void setTrustedMacroPrefixColors(Color foreground, Color background) {
    getMessagePanel().setTrustedMacroPrefixColors(foreground, background);
  }

  public static class TextColorWell extends JPanel {
    private static final long serialVersionUID = -9006587537198176935L;

    // Set the Color from the saved chat color from AppPreferences
    private Color color = AppPreferences.chatColor.get();

    public TextColorWell() {
      setMinimumSize(new Dimension(15, 15));
      setPreferredSize(new Dimension(15, 15));
      setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      setToolTipText(I18N.getText("action.chat.color.tooltip"));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              Color newColor =
                  JColorChooser.showDialog(
                      TextColorWell.this, I18N.getString("dialog.colorChooser.title"), color);
              if (newColor != null) {
                setColor(newColor);
              }
            }
          });
    }

    public void setColor(Color newColor) {
      color = newColor;
      repaint();
      AppPreferences.chatColor.set(color); // Set the Chat Color in AppPreferences
    }

    public Color getColor() {
      return color;
    }

    @Override
    protected void paintComponent(Graphics g) {
      g.setColor(color);
      g.fillRect(0, 0, getSize().width, getSize().height);
    }
  }

  private class AvatarPanel extends JComponent {
    private static final long serialVersionUID = -8027749503951260361L;
    private static final int PADDING = 5;

    private final Dimension preferredSize;

    private Image image;
    private Rectangle cancelBounds;

    public AvatarPanel(Dimension preferredSize) {
      this.preferredSize = preferredSize;
      setImage(null);
      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              if (cancelBounds != null && cancelBounds.contains(e.getPoint())) {
                MapTool.getFrame().getCommandPanel().commitCommand("/im");
              }
            }
          });
    }

    public void setImage(Image image) {
      this.image = image;
      setPreferredSize(image != null ? preferredSize : new Dimension(0, 0));
      invalidate();
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      Dimension size = getSize();
      g.setColor(getBackground());
      g.fillRect(0, 0, size.width, size.height);

      cancelBounds = null;
      if (image == null) {
        return;
      }
      Dimension imgSize = new Dimension(image.getWidth(null), image.getHeight(null));
      SwingUtil.constrainTo(imgSize, size.width - PADDING * 2, size.height - PADDING * 2);

      AppPreferences.renderQuality.get().setShrinkRenderingHints((Graphics2D) g);
      g.drawImage(
          image,
          (size.width - imgSize.width) / 2,
          (size.height - imgSize.height) / 2,
          imgSize.width,
          imgSize.height,
          this);

      // Cancel
      int x = size.width - cancelButton.getWidth();
      int y = 2;
      g.drawImage(cancelButton, x, y, this);
      cancelBounds = new Rectangle(x, y, cancelButton.getWidth(), cancelButton.getHeight());
    }
  }

  private void addFocusHotKey() {
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Object actionObject = new Object();
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, actionObject);
    getActionMap()
        .put(
            actionObject,
            new AbstractAction() {
              public void actionPerformed(ActionEvent event) {
                requestFocusInWindow();
              }
            });
  }

  /*
   * Gets the chat notification duration. Method is unused at this time.
   *
   * @return time in milliseconds before chat notifications disappear
   *
   * public long getChatNotifyDuration(){ return chatNotifyDuration; }
   */

  /**
   * If the GM enforces typing notification and no listener is present (because the client had
   * notification off), a new listener is added to the command text area
   */
  private void maybeAddTypingListener() {
    if (commandTextArea.getListeners(ChatTypingListener.class).length == 0) {
      commandTextArea.addKeyListener(new ChatTypingListener());
    }
  }
}
