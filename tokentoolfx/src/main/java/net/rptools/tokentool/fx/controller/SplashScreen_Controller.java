/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

public class SplashScreen_Controller {

	@FXML private StackPane splashLayout; // Value injected by FXMLLoader

	@FXML private ProgressBar loadProgress; // Value injected by FXMLLoader

	@FXML private Label progressText; // Value injected by FXMLLoader

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert splashLayout != null : "fx:id=\"splashLayout\" was not injected: check your FXML file 'SplashScreenLoader.fxml'.";
		assert loadProgress != null : "fx:id=\"loadProgress\" was not injected: check your FXML file 'SplashScreenLoader.fxml'.";
		assert progressText != null : "fx:id=\"progressText\" was not injected: check your FXML file 'SplashScreenLoader.fxml'.";
	}

	public void setLoadProgress(Double progress) {
		this.loadProgress.setProgress(progress);
	}

	public String getProgressText() {
		return progressText.getText();
	}

	public void setProgressText(String text) {
		this.progressText.setText(text);
	}
}
