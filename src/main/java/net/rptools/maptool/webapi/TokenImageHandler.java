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
package net.rptools.maptool.webapi;

import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class TokenImageHandler extends AbstractHandler {
  @Override
  public void handle(
      String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String args[] = target.replaceAll("^/", "").split("/");

    for (String s : args) {
      System.out.println("DEBUG: ARGS: " + s);
    }

    if ("portrait".equalsIgnoreCase(args[0])) {
      baseRequest.setHandled(sendPortrait(response, args[1]));
    } else if ("image".equalsIgnoreCase(args[0])) {
      baseRequest.setHandled(sendImage(response, args[1]));
    } else if ("portraitOrImage".equalsIgnoreCase(args[0])) {
      baseRequest.setHandled(sendPortraitOrImage(response, args[1]));
    }

    /*
     * response.setContentType("text/html; charset=utf-8"); response.setStatus(HttpServletResponse.SC_OK);
     *
     * PrintWriter out = response.getWriter();
     *
     * baseRequest.setHandled(true);
     */
  }

  private boolean sendImage(HttpServletResponse response, String tokenId) throws IOException {
    System.out.println("DEBUG: Here (> 0) as well");
    Token token = WebTokenInfo.getInstance().findTokenFromId(tokenId);
    if (token == null) {
      return false;
      // FIXME: log this error
    }

    if (token.getImageAssetId() == null) {
      response.setContentType("image/png");
      ImageIO.write(ImageManager.BROKEN_IMAGE, "png", response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      Asset asset = AssetManager.getAsset(token.getImageAssetId());
      byte[] image = asset.getData();
      response.setContentType("image/" + asset.getExtension());
      response.setStatus(HttpServletResponse.SC_OK);

      response.setContentLength(image.length);
      response.getOutputStream().write(image);
    }

    return true;
  }

  private boolean sendPortrait(HttpServletResponse response, String tokenId) throws IOException {

    System.out.println("DEBUG: Here (> 0) as well");
    Token token = WebTokenInfo.getInstance().findTokenFromId(tokenId);
    if (token == null) {
      return false;
      // FIXME: log this error
    }

    if (token.getPortraitImage() == null) {
      response.setContentType("image/png");
      ImageIO.write(ImageManager.BROKEN_IMAGE, "png", response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      Asset asset = AssetManager.getAsset(token.getPortraitImage());
      byte[] image = asset.getData();
      response.setContentType("image/" + asset.getExtension());
      response.setStatus(HttpServletResponse.SC_OK);

      response.setContentLength(image.length);
      response.getOutputStream().write(image);
    }

    return true;
  }

  private boolean sendPortraitOrImage(HttpServletResponse response, String tokenId)
      throws IOException {

    System.out.println("DEBUG: Here (> 0) as well");
    Token token = WebTokenInfo.getInstance().findTokenFromId(tokenId);
    if (token == null) {
      return false;
      // FIXME: log this error
    }

    Asset asset = null;
    if (token.getPortraitImage() != null) {
      asset = AssetManager.getAsset(token.getPortraitImage());
    } else if (token.getImageAssetId() != null) {
      asset = AssetManager.getAsset(token.getImageAssetId());
    }

    if (asset != null) {
      byte[] image = asset.getData();
      response.setContentType("image/" + asset.getExtension());
      response.setStatus(HttpServletResponse.SC_OK);

      response.setContentLength(image.length);
      response.getOutputStream().write(image);
    } else {
      response.setContentType("image/png");
      ImageIO.write(ImageManager.BROKEN_IMAGE, "png", response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    }

    return true;
  }
}
