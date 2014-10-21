/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.rptools.maptool.util;

import junit.framework.TestCase;

public class StringUtilTest extends TestCase {

	
	public void testCountOccurances() throws Exception {
		
		String str = "<div>";
		
		assertEquals(0, StringUtil.countOccurances("", str));
		assertEquals(1, StringUtil.countOccurances("<div>", str));
		assertEquals(1, StringUtil.countOccurances("one<div>two", str));
		assertEquals(2, StringUtil.countOccurances("one<div>two<div>three", str));
		assertEquals(3, StringUtil.countOccurances("one<div>two<div>three<div>", str));
		
	}
}
