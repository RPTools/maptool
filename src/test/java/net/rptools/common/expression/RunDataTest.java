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
package net.rptools.common.expression;

import junit.framework.TestCase;

public class RunDataTest extends TestCase {

    public void testRandomIntInt() {
        RunData runData = new RunData(null);
        
        for (int i = 0; i < 10000; i++) {
            int value = runData.randomInt(10);
            assertTrue(1 <= value && value <= 10);
        }
    }

    public void testRandomIntIntInt() {
        RunData runData = new RunData(null);
        
        for (int i = 0; i < 10000; i++) {
            int value = runData.randomInt(10, 20);
            assertTrue(String.format("Value outside range: %s", value), 10 <= value && value <= 20);
        }
    }
}
