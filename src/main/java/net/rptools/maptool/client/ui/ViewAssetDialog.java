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
package net.rptools.maptool.client.ui;

import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javax.swing.JDialog;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Asset;

/**
 * Creates a dialog that can be used to view an asset. Currently, only supports Plain Text, HTML,
 * and Markdown.
 */
public class ViewAssetDialog {
  /** The Swing Dialog to display the asset in. */
  private final JDialog dialog;

  /** Embedded JavaFX panel used to display the asset. */
  private final JFXPanel jfxPanel;

  /** The width of the dialog, */
  private final int width;

  /** The height of the dialog, */
  private final int height;

  /**
   * Creates a new ViewAssetDialog.
   *
   * @param asset the asset to display.
   * @param title the title of the dialog.
   * @param width the width of the dialog.
   * @param height the height of the dialog.
   * @throws IllegalArgumentException if the asset type is not supported.
   */
  public ViewAssetDialog(Asset asset, String title, int width, int height) {
    dialog = new JDialog(MapTool.getFrame(), title);
    dialog.setSize(width, height);
    this.width = width;
    this.height = height;
    jfxPanel = new JFXPanel();

    dialog.getContentPane().add(jfxPanel);

    switch (asset.getType()) {
      case TEXT -> textDialog(asset);
      case HTML -> htmlDialog(asset);
      case MARKDOWN -> markdownDialog(asset);
      default -> throw new IllegalArgumentException("Unsupported asset type: " + asset.getType());
    }

    dialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            dialog.dispose();
          }
        });
  }

  /**
   * Creates a markdown control to display the asset.
   *
   * @param asset the asset to display.
   */
  private void markdownDialog(Asset asset) {
    List<Extension> extensions =
        List.of(
            TablesExtension.create(),
            TaskListExtension.create(),
            DefinitionExtension.create(),
            TocExtension.create());
    MutableDataHolder options = new MutableDataSet();
    options
        .set(com.vladsch.flexmark.parser.Parser.SPACE_IN_LINK_URLS, true)
        .setFrom(ParserEmulationProfile.GITHUB_DOC)
        .set(TablesExtension.COLUMN_SPANS, false)
        .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
        .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
        .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
        .set(com.vladsch.flexmark.parser.Parser.EXTENSIONS, extensions);

    var mdParser = com.vladsch.flexmark.parser.Parser.builder(options).build();
    HtmlRenderer renderer = HtmlRenderer.builder(options).build();

    Node document = mdParser.parse(asset.getDataAsString());

    htmlDialog(renderer.render(document));
  }

  /**
   * Creates a html control to display the asset.
   *
   * @param asset the asset to display.
   */
  private void htmlDialog(Asset asset) {
    htmlDialog(asset.getDataAsString());
  }

  /**
   * Creates a html control to display a string.
   *
   * @param html the html to display.
   */
  private void htmlDialog(String html) {
    Platform.runLater(
        () -> {
          var pane = new StackPane();
          var scene = new Scene(pane, width, height);
          jfxPanel.setScene(scene);
          var webView = new WebView();
          webView.getEngine().loadContent(html);
          pane.getChildren().add(webView);
        });
  }

  /**
   * Creates a text control to display the asset.
   *
   * @param asset the asset to display.
   */
  private void textDialog(Asset asset) {
    Platform.runLater(
        () -> {
          var pane = new StackPane();
          var scene = new Scene(pane, width, height);
          jfxPanel.setScene(scene);
          var textArea = new TextArea(asset.getDataAsString());
          textArea.setEditable(false);
          pane.getChildren().add(textArea);
        });
  }

  /** Displays the dialog. */
  public void show() {
    SwingUtil.centerOver(dialog, dialog.getOwner());
    dialog.setVisible(true);
  }

  /** Displays the dialog in a modal state. */
  public void showModal() {
    dialog.setModal(true);
    show();
  }
}
