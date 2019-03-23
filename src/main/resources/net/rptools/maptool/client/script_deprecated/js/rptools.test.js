/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *  
 *	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

(function() { // Create a local scope
///// @register 'rptools.test.echo'
// @register { name: 'rptools.test.echo', alias: ['echo'] }
    rptools.test = {
	accessJavaPackage: function() {
		return new java.util.Date();
	},
	
	pi: function() {
		return java.lang.Math.PI;
	},
	
	_counter: 0,
	
	increment: function() {
		return this._counter++;
	},
	
	echo: function(a) {
		return a;
	},
	
	add: function(a) {
		return a + 1;
	},
	
	arrayLength: function(a) {
		return a.length;
	},
	
	parserFunction: function() {
		var total = 0;
		
		for (var i = 1; i < arguments.length; i++)
			total += arguments[i];
		
		return arguments[0] + ": " + total;
	},
	
	parserFunctionFixedArguments: function(a, b) {
		return a + b;
	},
	
	parserFunctionGetVariable: function(vname) {
		return rptools.scope.parser.getVariable(vname);
	},
	
	parserFunctionSetVariable: function(vname) {
		var total = 10;
		
		rptools.scope.parser.setVariable(vname, total);
		
		return total;
	},
	
	parserEvaluate: function() {
		var value = rptools.scope.parser.evaluate("10+2");
		
		return value;
	}
    };
})(); // End local scope
