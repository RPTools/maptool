/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool;

import java.awt.Color;

public class CompositionProperties {

	private double translucency = 1;
	private int fudgeFactor = 20;
	private boolean solidBackground = true;
	private boolean base = false;
	private Color backgroundColor = Color.white;

	public double getTranslucency() {
		return translucency;
	}

	public void setTranslucency(double alpha) {
		this.translucency = alpha;
	}

	public int getFudgeFactor() {
		return fudgeFactor;
	}

	public void setFudgeFactor(int fudgeFactor) {
		this.fudgeFactor = fudgeFactor;
	}

	public boolean isSolidBackground() {
		return solidBackground;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color newColor) {
		this.backgroundColor = newColor;
	}

	public boolean isBase() {
		return base;
	}

	public void setBase(boolean base) {
		this.base = base;
	}

	public void setSolidBackground(boolean solidBackground) {
		this.solidBackground = solidBackground;
	}

}
