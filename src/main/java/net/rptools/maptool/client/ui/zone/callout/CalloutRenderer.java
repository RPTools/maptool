package net.rptools.maptool.client.ui.zone.callout;

import java.awt.Graphics2D;
import java.util.List;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

/**
 * Interface implemented by callout renderers.
 */
public interface CalloutRenderer {

  /**
   * Render the callout.
   */
  void render();

}
