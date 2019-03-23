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
package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.HeroLabData;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ExtractHeroLab;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import org.apache.commons.lang.math.NumberUtils;

/**
 * @author Jamz
 *     <p>Functions to support reading new Hero Lab data from a token.
 */
public class HeroLabFunctions extends AbstractFunction {

  private static final HeroLabFunctions instance = new HeroLabFunctions();

  private HeroLabFunctions() {
    super(
        0,
        2,
        "herolab.getInfo",
        "herolab.getStatBlock",
        "herolab.hasChanged",
        "herolab.refresh",
        "herolab.XPath",
        "herolab.getImage",
        "herolab.isMinion",
        "herolab.getMasterName");
  }

  public static HeroLabFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("herolab.getInfo")) {
      Token token;

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (token.getHeroLabData() == null) return BigDecimal.ZERO;

      return token.getHeroLabData().getInfo();
    } else if (functionName.equals("herolab.getStatBlock")) {
      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));

      if (parameters.isEmpty())
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

      Token token = null;

      if (parameters.size() == 2) {
        token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 1) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();

        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      String statBlockType = parameters.get(0).toString();

      return token.getHeroLabData().getStatBlock_data(statBlockType);
    } else if (functionName.equals("herolab.hasChanged")) {
      Token token;

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      return token.getHeroLabData().isDirty() ? BigDecimal.ONE : BigDecimal.ZERO;
    } else if (functionName.equals("herolab.refresh")) {
      Token token;

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      HeroLabData heroLabData = token.getHeroLabData();
      ExtractHeroLab heroLabExtract = new ExtractHeroLab(heroLabData.getPortfolioFile(), false);
      heroLabData = heroLabExtract.refreshCharacter(heroLabData);

      if (heroLabData != null) {
        token.setHeroLabData(heroLabData);

        // Update the images
        MD5Key tokenAsset = heroLabData.getTokenImage();
        if (tokenAsset != null) token.setImageAsset(null, tokenAsset);

        MD5Key portraitAsset = heroLabData.getPortraitImage();
        if (portraitAsset != null) token.setPortraitImage(portraitAsset);

        MD5Key handoutAsset = heroLabData.getHandoutImage();
        if (handoutAsset != null) token.setCharsheetImage(handoutAsset);

        return BigDecimal.ONE;
      } else {
        return BigDecimal.ZERO;
      }

    } else if (functionName.equals("herolab.XPath")) {
      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));

      if (parameters.isEmpty())
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

      Token token = null;

      if (parameters.size() == 2) {
        token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 1) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();

        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      String xPathExpression = parameters.get(0).toString();
      String result = token.getHeroLabData().parseXML(xPathExpression);

      if (NumberUtils.isNumber(result))
        return new BigDecimal(result).stripTrailingZeros().toPlainString();
      else return result;
    } else if (functionName.equals("herolab.getImage")) {
      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));

      if (parameters.isEmpty())
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

      Token token = null;

      if (parameters.size() == 2) {
        token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 1) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();

        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      int imageIndex = Integer.parseInt(parameters.get(0).toString());
      MD5Key assetID = token.getHeroLabData().getImageAssetID(imageIndex);
      if (assetID == null) return "";

      StringBuilder assetURL = new StringBuilder("asset://");
      assetURL.append(assetID.toString());

      return assetURL.toString();
    } else if (functionName.equals("herolab.isMinion")) {
      Token token;

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      return token.getHeroLabData().isMinion() ? BigDecimal.ONE : BigDecimal.ZERO;
    } else if (functionName.equals("herolab.getMasterName")) {
      Token token;

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (token.getHeroLabData() == null)
        throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

      return token.getHeroLabData().getMinionMasterName();
    }

    return "<ERROR>";
  }
}
