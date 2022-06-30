package net.rptools.maptool.client.script.javascript;

import java.util.*;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.ProxyObject;

public class JSObject extends AbstractMap<String,Object> implements ProxyObject {
  private HashMap<String,Object> map;

  public JSObject() {
    map = new HashMap<String,Object>();
  }
  
  public JSObject(int i) {
    map = new HashMap<String,Object>(i);
  }

  public JSObject(int i, float f) {
    map = new HashMap<String,Object>(i,f);
  }

  public JSObject(Map<? extends String,? extends Object> mapToClone) {
    map = new HashMap<String,Object>(mapToClone);
  }

  @Override
  public Set<Map.Entry<String,Object>> entrySet() {
    return map.entrySet();
  } 
  
  @Override
  public Object put(String key,Object value) {
    return map.put(key,value);
  }
  
  @Override
  public Object getMember(String member) {
    return map.get(member);
  }
  
  @Override
  public Object getMemberKeys() {
    return new JSArray(map.keySet());
  }

  @Override
  public boolean hasMember(String member) {
    return map.containsKey(member);
  }

  @Override
  public void putMember(String member, Value value) {
    map.put(member,value);
  }
}
