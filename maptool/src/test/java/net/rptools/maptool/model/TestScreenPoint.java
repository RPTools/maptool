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
package net.rptools.maptool.model;

import junit.framework.TestCase;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.ZoneRendererFactory;

public class TestScreenPoint extends TestCase {
	public void testConversion() throws Exception {
		ZoneRenderer renderer = ZoneRendererFactory.newRenderer(new Zone());
		renderer.moveViewBy(-100, -100);

		for (int i = -10; i < 10; i++) {
			for (int j = -10; j < 10; j++) {
				ZonePoint zp = new ZonePoint(i, j);
				assertEquals(zp, ScreenPoint.fromZonePoint(renderer, zp).convertToZone(renderer));
			}
		}
	}
}
