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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.library.addon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryEventsDto;
import net.rptools.maptool.model.library.proto.AddOnStatSheetsDto;
import net.rptools.maptool.model.library.proto.AddonSlashCommandsDto;
import net.rptools.maptool.model.library.proto.MTScriptPropertiesDto;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

/** The list of files an add-on library reports, which add-ons use to load their scripts. */
class AddOnLibraryFilesTest {

  @Test
  void filesAreListedInTheSameSortedOrderEveryTime() throws Exception {
    List<String> paths = new ArrayList<>();
    for (int i = 0; i < 60; i++) {
      paths.add(String.format("public/script/part%02d/file%02d.js", i % 7, i));
    }
    List<String> shuffled = new ArrayList<>(paths);
    Collections.shuffle(shuffled, new Random(6047));
    Map<String, Pair<MD5Key, Type>> assets = new HashMap<>();
    for (String path : shuffled) {
      assets.put(path, Pair.with(new MD5Key(path.getBytes()), Type.JAVASCRIPT));
    }

    var library =
        AddOnLibrary.fromDto(
            new MD5Key("library".getBytes()),
            AddOnLibraryDto.newBuilder()
                .setName("Order test")
                .setVersion("1.0.0")
                .setNamespace("net.example.order")
                .build(),
            MTScriptPropertiesDto.getDefaultInstance(),
            AddOnLibraryEventsDto.getDefaultInstance(),
            AddOnStatSheetsDto.getDefaultInstance(),
            AddonSlashCommandsDto.getDefaultInstance(),
            assets);

    List<String> sorted = new ArrayList<>(paths);
    Collections.sort(sorted);
    assertEquals(sorted, library.getAllFiles().get());
  }
}
