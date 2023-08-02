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
package net.rptools.lib.net;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageWriter;
import net.rptools.maptool.server.proto.LocationDto;

public interface Location {
  void putContent(ImageWriter content, BufferedImage img) throws IOException;

  void putContent(InputStream content) throws IOException;

  InputStream getContent() throws IOException;

  LocationDto toDto();

  static Location fromDto(LocationDto dto) {
    switch (dto.getLocationTypCase()) {
      case FTP_LOCATION -> {
        return FTPLocation.fromDto(dto.getFtpLocation());
      }
      case LOCAL_LOCATION -> {
        return LocalLocation.fromDto(dto.getLocalLocation());
      }
    }
    return null;
  }
}
