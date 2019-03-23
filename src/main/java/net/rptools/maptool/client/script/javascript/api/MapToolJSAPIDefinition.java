/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.script.javascript.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.TYPE)
/**
 * Annotation that is used to tell the JavaScript engine the name of the variable that an object of
 * this class will be bound to to provide the API functionality. Only objects that need to have a
 * variable defined for them in the main scope need to have this annotation.
 */
public @interface MapToolJSAPIDefinition {
  /**
   * The name of the variable that the object will be bound to in the JavaScript scope.
   *
   * @return the name of the variable.
   */
  String javaScriptVariableName();
}
