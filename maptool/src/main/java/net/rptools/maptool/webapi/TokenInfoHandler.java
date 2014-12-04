/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source Code.  If not, see <http://www.gnu.org/licenses/>
 */

package net.rptools.maptool.webapi;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.ImageManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TokenInfoHandler extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {


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


        /*response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();

        baseRequest.setHandled(true);*/
    }

    private Token findToken(String tokenId) {
        final GUID id = new GUID(tokenId);

        final List<Token> tokenList = new ArrayList<>();

        List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
        for (ZoneRenderer zr : zrenderers) {
            tokenList.addAll(zr.getZone().getTokensFiltered(new Zone.Filter() {
                public boolean matchToken(Token t) {
                    return t.getId().equals(id);
                }
            }));

            if (tokenList.size() > 0) {
                System.out.println("DEBUG: Here (> 0)");
                break;
            }
        }

        if (tokenList.size() > 0) {
            return tokenList.get(0);
        } else {
            return null;
        }
    }

    private boolean sendImage(HttpServletResponse response, String tokenId) throws IOException {
        System.out.println("DEBUG: Here (> 0) as well");
        Token token = findToken(tokenId);
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
            byte[] image = asset.getImage();
            response.setContentType("image/" + asset.getImageExtension());
            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentLength(image.length);
            response.getOutputStream().write(image);
        }

        return true;
    }

    private boolean sendPortrait(HttpServletResponse response,  String tokenId) throws IOException {

        System.out.println("DEBUG: Here (> 0) as well");
        Token token = findToken(tokenId);
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
            byte[] image = asset.getImage();
            response.setContentType("image/" + asset.getImageExtension());
            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentLength(image.length);
            response.getOutputStream().write(image);
        }

        return true;
    }


    private boolean sendPortraitOrImage(HttpServletResponse response,  String tokenId) throws IOException {

        System.out.println("DEBUG: Here (> 0) as well");
        Token token = findToken(tokenId);
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
            byte[] image = asset.getImage();
            response.setContentType("image/" + asset.getImageExtension());
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
