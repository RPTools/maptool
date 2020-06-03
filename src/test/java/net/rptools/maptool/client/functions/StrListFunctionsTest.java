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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.parser.function.ParameterException;
import org.junit.jupiter.api.Test;

public class StrListFunctionsTest {

  private final StrListFunctions funcs = new StrListFunctions();
  private String delim = ",";

  private List<Object> toParms(Object... parms) {
    ArrayList result = new ArrayList<Object>();
    for (Object arg : parms) {
      if (arg instanceof Integer) arg = new BigDecimal((Integer) arg);
      result.add(arg);
    }
    return result;
  }

  private BigDecimal big(int i) {
    return new BigDecimal(i);
  }

  private Object listGet(int i, String listStr) throws ParameterException {
    return funcs.listGet(toParms("listGet()", i, delim), listStr, delim);
  }

  private void setDelim(String delim) {
    this.delim = delim;
  }

  @Test
  public void testListGet() throws ParameterException {

    setDelim(",");
    assertEquals("Wild Magic Surge", listGet(0, "Wild Magic Surge"));
    setDelim(";");
    assertEquals("Sild Wagic Murge", listGet(1, "Wild Magic Surge;   Sild Wagic Murge"));
    assertEquals("", listGet(2, "Wild Magic Surge;   Sild Wagic Murge;"));

    setDelim(",");
    assertEquals("one", listGet(0, "one,two,three"));
    assertEquals("two", listGet(1, "    one,two,three  "));
    assertEquals("three", listGet(2, "one,   two,three   "));
    assertEquals("", listGet(3, "one,      two,   three"));
    assertEquals("", listGet(0, ""));

    assertEquals("", listGet(0, ""));
    assertEquals("x", listGet(0, "x"));
    assertEquals("b", listGet(1, "a,b"));
    assertEquals(big(1), listGet(0, "1"));
    assertEquals(big(1), listGet(1, "0,1"));

    setDelim(";");
    assertEquals("", listGet(0, ""));
    assertEquals("x", listGet(0, "x"));
    assertEquals("b", listGet(1, "a;b"));
    assertEquals(big(1), listGet(0, "1"));
    assertEquals(big(1), listGet(1, "0;1"));
  }

  private Object listDelete(int i, String listStr) throws ParameterException {
    return funcs.listDelete(toParms("listDelete()", i, delim), listStr, delim);
  }

  @Test
  public void testListDelete() throws ParameterException {

    setDelim(",");
    assertEquals("two,  three", listDelete(0, "one,   two,  three"));
    assertEquals("one,three", listDelete(1, "one,two,three"));
    assertEquals("one,two", listDelete(2, "one,two,  three    "));
    assertEquals("one,two,three", listDelete(-1, "one,two,three"));
    assertEquals("one,two,three", listDelete(3, "one,   two   ,   three"));
    assertEquals(",", listDelete(0, ",,"));
    assertEquals(",", listDelete(1, ",,"));
    assertEquals(",", listDelete(2, ",,"));
    assertEquals("", listDelete(0, ""));

    assertEquals("", listDelete(0, ""));
    assertEquals("", listDelete(0, "x"));
    assertEquals("b", listDelete(0, "a,b"));
    assertEquals("a", listDelete(1, "a,b"));
    assertEquals("a,b", listDelete(2, "a,b"));

    setDelim(";");
    assertEquals("", listDelete(0, ""));
    assertEquals("", listDelete(0, "x"));
    assertEquals("b", listDelete(0, "a;b"));
    assertEquals("a", listDelete(1, "a;b"));
    assertEquals("a;b", listDelete(2, "a;b"));
  }

  private Object listAppend(String listStr, Object element) throws ParameterException {
    return funcs.listAppend(toParms("listAppend()", element, delim), listStr, delim);
  }

  @Test
  public void testListAppend() throws ParameterException {

    setDelim(",");
    assertEquals(
        "one or the other, something with space",
        listAppend("one or the other", "something with space"));
    assertEquals(
        "one or the other,, something with space",
        listAppend("one or the other,", "something with space"));

    assertEquals("one", listAppend("", "one"));
    assertEquals("one, two", listAppend("one", "two"));
    assertEquals("one,   two, three", listAppend("one,   two", "three"));

    assertEquals("a", listAppend("", "a"));
    assertEquals("0", listAppend("", "0"));
    assertEquals("x, y", listAppend("x", "y"));
    assertEquals("x, 1", listAppend("x", 1));
    assertEquals(
        "a,b, c",
        listAppend("a,b", "c")); // This is a change from old behavior that would parse listStr
    assertEquals(
        "1,b, 2", listAppend("1,b", 2)); // .. ","pletely to construct a well formatted string. That
    assertEquals(
        ",, z",
        listAppend(",", "z")); // .. spends a lot of cycles and I don't know the benefit atm.

    setDelim(";");
    assertEquals("a", listAppend("", "a"));
    assertEquals("0", listAppend("", "0"));
    assertEquals("x; y", listAppend("x", "y"));
    assertEquals("x; 1", listAppend("x", 1));
    assertEquals(
        "a;b; c",
        listAppend("a;b", "c")); // This is a change from old behavior that would parse listStr
    assertEquals(
        "1;b; 2", listAppend("1;b", 2)); // .. ","pletely to construct a well formatted string. That
    assertEquals(
        ";; z",
        listAppend(";", "z")); // .. spends a lot of cycles and I don't know the benefit atm.
  }

  private Object listCount(String listStr) throws ParameterException {
    return funcs.listCount(toParms("listCount()", delim), listStr, delim);
  }

  @Test
  public void testListCount() throws ParameterException {

    setDelim(",");
    assertEquals(big(4), listCount("one, two, three and four, five"));
    assertEquals(big(5), listCount("one, two, three and four, five,"));

    assertEquals(big(0), listCount(""));
    assertEquals(big(1), listCount("one"));
    assertEquals(big(2), listCount("one,   two"));
    assertEquals(big(2), listCount(","));

    setDelim(";");
    assertEquals(big(0), listCount(""));
    assertEquals(big(1), listCount("one"));
    assertEquals(big(2), listCount("one;two"));
    assertEquals(big(2), listCount(";"));
  }

  private Object listFind(String listStr, String element) throws ParameterException {
    return funcs.listFind(toParms("listFind()", element, delim), listStr, delim);
  }

  @Test
  public void testListFind() throws ParameterException {

    setDelim(",");
    assertEquals(big(1), listFind("one,     two with spaces , three", "two with spaces"));

    setDelim(",");
    assertEquals(big(-1), listFind("", "foo"));
    assertEquals(big(0), listFind("one", "one"));
    assertEquals(big(1), listFind("one,     two, three", "two"));
    assertEquals(big(2), listFind("    one, two, three    ", "three"));

    assertEquals(big(0), listFind(",", ""));
    assertEquals(big(0), listFind("x", "x"));
    assertEquals(big(1), listFind("a,b", "b"));
    assertEquals(big(-1), listFind("a,b", "c"));
    assertEquals(big(1), listFind("a,0,b", "0"));
    assertEquals(big(-1), listFind("a,0,b", "1"));

    setDelim(";");
    assertEquals(big(0), listFind(";", ""));
    assertEquals(big(0), listFind("x", "x"));
    assertEquals(big(1), listFind("a;b", "b"));
    assertEquals(big(-1), listFind("a;b", "c"));
    assertEquals(big(1), listFind("a;0;b", "0"));
    assertEquals(big(-1), listFind("a;0;b", "1"));
  }

  private Object listContains(String listStr, String element) throws ParameterException {
    return funcs.listContains(toParms("listContains()", element, delim), listStr, delim);
  }

  @Test
  public void testListContains() throws ParameterException {
    setDelim(",");
    assertEquals(big(0), listContains("", "foo"));
    assertEquals(big(1), listContains("one, two, three", "one"));
    assertEquals(big(1), listContains("one, two, three", "two"));
    assertEquals(big(1), listContains("one, two, three", "three"));
    assertEquals(big(2), listContains("one,     two, three, two", "two"));
    assertEquals(big(3), listContains("    one, two, three, two, four, two    ", "two"));

    setDelim(";");
    assertEquals(big(0), listContains("", "foo"));
    assertEquals(big(1), listContains("one; two; three", "one"));
    assertEquals(big(1), listContains("one;two;three", "two"));
  }

  private Object listInsert(String listStr, int index, Object element) throws ParameterException {
    return funcs.listInsert(toParms("listInsert()", index, element, delim), listStr, delim);
  }

  @Test
  public void testListInsert() throws ParameterException {

    setDelim(",");
    assertEquals("", listInsert("", -1, "inserted"));
    assertEquals("inserted", listInsert("", 0, "inserted"));
    assertEquals("", listInsert("", 1, "inserted"));
    assertEquals("", listInsert("", 2, "inserted"));

    assertEquals("inserted, one,two", listInsert("one,two", 0, "inserted"));
    assertEquals("one, inserted, two", listInsert("one,two", 1, "inserted"));
    assertEquals("one, two, inserted", listInsert("one,two", 2, "inserted"));

    assertEquals("x", listInsert("", 0, "x"));
    assertEquals("", listInsert("", 1, "x"));
    assertEquals("5", listInsert("", 0, 5));
    assertEquals("y, x", listInsert("x", 0, "y"));
    assertEquals("x, 5", listInsert("x", 1, 5));
    assertEquals("x", listInsert("x", 2, "y"));
    assertEquals("3, a, b", listInsert("a, b", 0, 3));
    assertEquals("a, b, c", listInsert("a,b", 2, "c"));
    assertEquals("a, b", listInsert("a, b", 3, "c"));

    setDelim(";");
    assertEquals("x", listInsert("", 0, "x"));
    assertEquals("", listInsert("", 1, "x"));
    assertEquals("5", listInsert("", 0, 5));
    assertEquals("y; x", listInsert("x", 0, "y"));
    assertEquals("x; 5", listInsert("x", 1, 5));
    assertEquals("x", listInsert("x", 2, "y"));
    assertEquals("3; a; b", listInsert("a; b", 0, 3));
    assertEquals("a; b; c", listInsert("a;b", 2, "c"));
    assertEquals("a; b", listInsert("a; b", 3, "c"));
  }

  private Object listReplace(String listStr, int index, Object element) throws ParameterException {
    return funcs.listReplace(toParms("listReplace()", index, element, delim), listStr, delim);
  }

  @Test
  public void testListReplace() throws ParameterException {

    setDelim(",");
    assertEquals("", listReplace("", 0, "replaced"));
    assertEquals("replaced", listReplace("one", 0, "replaced"));
    assertEquals("replaced, two", listReplace("one, two", 0, "replaced"));
    assertEquals("one, replaced, three", listReplace("one, two, three", 1, "replaced"));
    assertEquals("one, two, replaced", listReplace("one, two, three", 2, "replaced"));
    assertEquals("one, two, three", listReplace("one, two, three", 3, "replaced"));

    assertEquals("", listReplace("", 0, "a"));
    assertEquals("", listReplace("", 0, 55));
    assertEquals("3", listReplace("x", 0, 3));
    assertEquals("x", listReplace("x", 1, "y"));
    assertEquals("a,", listReplace(",", 0, "a"));
    assertEquals("a, ", listReplace(" , ", 0, "a"));
    assertEquals(", b", listReplace(",", 1, "b"));
    assertEquals(", ", listReplace(",", 2, "c"));
    assertEquals("a, d", listReplace("a,b", 1, "d"));

    setDelim(";");
    assertEquals("", listReplace("", 0, "a"));
    assertEquals("", listReplace("", 0, 55));
    assertEquals("3", listReplace("x", 0, 3));
    assertEquals("x", listReplace("x", 1, "y"));
    assertEquals("a;", listReplace(";", 0, "a"));
    assertEquals("; b", listReplace(";", 1, "b"));
    assertEquals("; ", listReplace(";", 2, "c"));
    assertEquals("a; d", listReplace("a;b", 1, "d"));
  }

  private Object listSort(String listStr, String sortType) throws ParameterException {
    return funcs.listSort(toParms("listSort()", sortType, delim), listStr, delim);
  }

  private Object listSort(String listStr) throws ParameterException {
    return funcs.listSort(toParms("listSort()", delim), listStr, delim);
  }

  @Test
  public void testListSort() throws ParameterException {

    setDelim(",");
    assertEquals(
        "a and 1, b and 2, c and 3, d and 4", listSort(" d and 4, a and 1,  b and 2  ,c and 3"));

    setDelim(",");
    assertEquals("", listSort(""));
    assertEquals("a, b, c, d", listSort("d,a,b,c"));
    assertEquals("a", listSort("         a   "));

    assertEquals("M1, M10, M3", listSort("M3, M10, M1", "A"));
    assertEquals("M3, M10, M1", listSort("M3, M10, M1", "A-"));
    assertEquals("M1, M3, M10", listSort("M3,M10,M1", "N+"));
    assertEquals("M10, M3, M1", listSort("M3,M10,M1", "N-"));

    setDelim(";");
    assertEquals("M1; M10; M3", listSort("M3; M10; M1", "A"));
    assertEquals("M3; M10; M1", listSort("M3; M10; M1", "A-"));
    assertEquals("M1; M3; M10", listSort("M3;M10;M1", "N+"));
    assertEquals("M10; M3; M1", listSort("M3;M10;M1", "N-"));
  }

  private Object listFormat(
      String delim, String listStr, String listFormat, String entryFormat, String entrySep)
      throws ParameterException {
    return funcs.listFormat(
        toParms("listFormat()", listFormat, entryFormat, entrySep, delim), listStr, delim);
  }

  @Test
  public void testListFormat() throws ParameterException {
    assertEquals("[[(a)...(b)...(c)]]", listFormat(",", "a,b,c", "[[%list]]", "(%item)", "..."));
    assertEquals("[[(a)...(b)...(c)]]", listFormat(";", "a;b;c", "[[%list]]", "(%item)", "..."));
  }
}
