package net.rptools.maptool.webendpoint;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import net.rptools.maptool.webendpoint.servlet.general.VersionServlet;

@ApplicationPath("/v1")
public class WebEndPointApp  extends Application {
  @Override
  public Set<Class<?>> getClasses() {
    HashSet<Class<?>> classes = new HashSet<>();
    classes.add(VersionServlet.class);
    // TODO: CDW add classes
    return classes;
  }
}
