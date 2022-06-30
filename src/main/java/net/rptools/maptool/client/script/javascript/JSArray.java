package net.rptools.maptool.client.script.javascript;

import java.util.*;
import org.graalvm.polyglot.proxy.*;
import org.graalvm.polyglot.*;

public class JSArray extends AbstractList<Object> implements ProxyArray {
  private ArrayList<Object> array;
  
  public JSArray() {
    array = new ArrayList<Object>();
  }
  
  public JSArray(int size) {
    array = new ArrayList<Object>(size);
  }

  public JSArray(Collection<? extends Object> collection) {
    array = new ArrayList<Object>(collection);
  }

  @Override
  public Object get(int i) {
    return array.get(i);
  }
  
  @Override
  public int size() {
    return array.size();
  }

  @Override
  public Object set(int i, Object elemen) {
    return array.set(i,elemen);
  }
  
  @Override
  public void add(int i, Object elemen) {
    array.add(i,elemen);
  }

  @Override
  public Object remove(int i) {
    return array.remove(i);
  }

  @Override
  public Object get(long index) {
    checkLong(index);
    return get((int) index);
  }

  @Override
  public long getSize() {
    return size();
  }

  @Override
  public void set(long index, Value value) {
    checkLong(index);
    array.set((int) index,value);
  }

  private void checkLong(long index) {
    if(index < Integer.MAX_VALUE) {
      return ;
    }
    else {
      String message = "tried to acess index " + index + " which deosn't fit in int";
      throw new ArrayIndexOutOfBoundsException(message);
    }
  }
}
