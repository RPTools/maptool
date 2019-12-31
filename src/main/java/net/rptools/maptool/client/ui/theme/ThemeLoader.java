package net.rptools.maptool.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;

public class ThemeLoader {

  public ThemeSupport loadTheme(String themeDefFile) {
    JsonParser jsonParser = new JsonParser();

    JsonElement jsonElement = jsonParser.parse(themeDefFile);
    if (!jsonElement.isJsonObject()) {
      // TODO: CDW raise error.
      return null;
    }

    return new ThemeSupport();

  }

}
