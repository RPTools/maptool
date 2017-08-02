/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.fx.view;

import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.*;
import javafx.util.Duration;
import net.rptools.tokentool.fx.controller.SplashScreen_Controller;
import javafx.scene.layout.StackPane;

/**
 * Example of displaying a splash page for a standalone JavaFX application
 */
public class SplashScreenLoader extends Preloader {
	private final String TOKEN_TOOL_SPLASH_ICON = "/net/rptools/tokentool/image/token_tool_splash_icon.png";
	private final String SPLASH_SCREEN_FXML = "/net/rptools/tokentool/fxml/SplashScreenLoader.fxml";
	private final String TOKEN_TOOL_BUNDLE = "net.rptools.tokentool.i18n.TokenTool";

	private Stage stage;
	private StackPane root;
	private SplashScreen_Controller controller;

	public void start(Stage stage) throws Exception {
		// System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
		setUserAgentStylesheet(STYLESHEET_CASPIAN); // I like the look of the this progress bar better for this screen...

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SPLASH_SCREEN_FXML), ResourceBundle.getBundle(TOKEN_TOOL_BUNDLE));
		root = (StackPane) fxmlLoader.load();
		controller = (SplashScreen_Controller) fxmlLoader.getController();

		Scene scene = new Scene(root);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.getIcons().add(new Image(getClass().getResourceAsStream(TOKEN_TOOL_SPLASH_ICON)));
		stage.setScene(scene);
		stage.show();

		this.stage = stage;
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification evt) {
		if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
			if (stage.isShowing()) {
				// fade out, hide stage at the end of animation
				FadeTransition ft = new FadeTransition(
						Duration.millis(1000), stage.getScene().getRoot());
				ft.setFromValue(1.0);
				ft.setToValue(0.0);
				final Stage s = stage;
				EventHandler<ActionEvent> eh = new EventHandler<ActionEvent>() {
					public void handle(ActionEvent t) {
						s.hide();
					}
				};
				ft.setOnFinished(eh);
				ft.play();
			} else {
				stage.hide();
			}
		}
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification pn) {
		if (pn instanceof ProgressNotification) {
			// expect application to send us progress notifications with progress ranging from 0 to 1.0
			double v = ((ProgressNotification) pn).getProgress();
			controller.setLoadProgress(v);
			// System.out.println("progress: " + v);
		} else if (pn instanceof StateChangeNotification) {
			// hide after get any state update from application
			stage.hide();
		}
	}
}