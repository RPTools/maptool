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
package net.rptools.common.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Result {
  private final String expression;
  private Supplier<String> detailExpression;
  private Object value;
  private String description;
  private List<Integer> rolled;

  private final Map<String, String> properties = new HashMap<String, String>();

  public Result(String expression) {
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

  public String getDetailExpression() {
    return detailExpression != null ? detailExpression.get() : "";
  }

  public void setDetailExpression(Supplier<String> detailExpression) {
    this.detailExpression = detailExpression;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Map<String, String> getProperties() {
    return this.properties;
  }

  public void setRolled(List<Integer> rolls) {
    rolled = new ArrayList<>(rolls);
  }

  public List<Integer> getRolled() {
    return Collections.unmodifiableList(rolled);
  }

  public String format() {
    StringBuilder sb = new StringBuilder(64);
    sb.append(expression).append(" = ");

    if (detailExpression != null && !detailExpression.equals(value.toString())) {
      sb.append("(").append(detailExpression).append(") = ");
    }

    sb.append(value);
    if (description != null) {
      sb.append(" // ").append(description);
    }
    return sb.toString();
  }
}
