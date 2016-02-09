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

public class TestSquareGrid extends TestCase {
	public void testSpotCheck() throws Exception {
		Grid grid = new SquareGrid();
		System.out.println(grid.convert(new CellPoint(-1, 0)));
	}
}
