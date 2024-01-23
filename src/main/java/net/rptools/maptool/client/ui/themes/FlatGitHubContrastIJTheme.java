/*
 * Copyright 2020 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.rptools.maptool.client.ui.themes;

//
// taken from
// https://github.com/JFormDesigner/FlatLaf/tree/0291dd5416b6bbf8ea2f3d5e4f7a4f22d6368e36/flatlaf-intellij-themes
//

import com.formdev.flatlaf.IntelliJTheme;

/**
 * @author Karl Tauber
 */
public class FlatGitHubContrastIJTheme extends IntelliJTheme.ThemeLaf {
  public static final String NAME = "GitHub Contrast (Material)";

  public static boolean setup() {
    try {
      return setup(new FlatGitHubContrastIJTheme());
    } catch (RuntimeException ex) {
      return false;
    }
  }

  public static void installLafInfo() {
    installLafInfo(NAME, FlatGitHubContrastIJTheme.class);
  }

  public FlatGitHubContrastIJTheme() {
    super(Utils.loadTheme("GitHub Contrast.theme.json"));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
