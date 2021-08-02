package net.rptools.maptool.webendpoint;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import net.rptools.maptool.webendpoint.servlet.general.VersionServlet;
import net.rptools.maptool.webendpoint.servlet.player.PlayerDatabaseServlet;
import net.rptools.maptool.webendpoint.servlet.player.PlayerServlet;

@ApplicationPath("/v1")
public class WebEndPointApp  extends Application {
  @Override
  public Set<Class<?>> getClasses() {
    HashSet<Class<?>> classes = new HashSet<>();
    classes.add(VersionServlet.class);
    classes.add(PlayerServlet.class);
    classes.add(PlayerDatabaseServlet.class);
    // TODO: CDW add classes
    return classes;
  }
}
