/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.functions.TokenImage.imageType;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.model.Token;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class ImageFunc extends OneArgFunction {

	private MapToolToken sourceToken;
	private imageType type;
	public ImageFunc(MapToolToken source, imageType type) {
		sourceToken = source;
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue size) {
		if (sourceToken.isSelfOrTrusted() && sourceToken.hasToken()) {
			Token t = sourceToken.getToken();
			boolean found = false;
			StringBuilder assetId = new StringBuilder("asset://");
			if (type == imageType.TOKEN_IMAGE && t.getImageAssetId() != null) {
				found = true;
				assetId.append(t.getImageAssetId());
			} else if (type == imageType.TOKEN_PORTRAIT && t.getPortraitImage() != null) {
				found = true;
				assetId.append(t.getPortraitImage());
			} else if (t.getCharsheetImage() != null) {
				found = true;
				assetId.append(t.getCharsheetImage());
			}
			if (!size.isnil()) {
				assetId.append("-");
				int i = Math.max(size.checkint(), 1);
				assetId.append(i);
			}
			if (found) {
				return valueOf(assetId.toString());
			}
		}
		return valueOf("");
	}

}
