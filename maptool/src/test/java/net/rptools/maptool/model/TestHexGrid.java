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

public class TestHexGrid extends TestCase {
	public void testConvertCellToZone() throws Exception {
		int start = -100;
		//int start = 0;
		HexGrid grid = new HexGridHorizontal();
		for (int y = start; y < 100; y++) {
			for (int x = start; x < 100; x++) {
				CellPoint cp = new CellPoint(x, y);
				ZonePoint zp = grid.convert(cp);
				assertEquals(cp, grid.convert(zp));
			}
		}
	}

	public void testSpotCheck() throws Exception {
		HexGrid grid = new HexGridHorizontal();

		CellPoint cp1 = new CellPoint(4, 1);
		CellPoint cp2 = new CellPoint(3, 1);

		ZonePoint zp1 = grid.convert(cp1);
		ZonePoint zp2 = grid.convert(cp2);

		System.out.println(zp1 + " - " + grid.convert(zp1));
		System.out.println(zp2 + " - " + grid.convert(zp2));
	}
}
