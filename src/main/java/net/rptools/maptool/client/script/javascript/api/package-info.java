/*
 * This software Copyright by the RPTools.net development team, and licensed under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this source Code. If not, see <http://www.gnu.org/licenses/>
 */
/**
 * Provides the classes that are used to provide an API for the internal JavaScript scripting
 * language. All of the classes that are to be exposed as a variable in the JavaScript scope will
 * need to implement the {@link
 * net.rptools.maptool.client.script.javascript.api.MapToolJSAPIInterface} interface and be
 * annotated with {@link net.rptools.maptool.client.script.javascript.api.MapToolJSAPIDefinition}.
 *
 * <p>Any object that matches the above two criteria will be picked up at run time, instantiated and
 * registered with the JavaScript engine to provide the API.
 */
package net.rptools.maptool.client.script.javascript.api;
