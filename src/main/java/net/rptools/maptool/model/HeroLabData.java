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
package net.rptools.maptool.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.StringValue;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.server.proto.HeroLabDataDto;

/**
 * @author Jamz
 *     <p>Created to hold various bits of info about a Hero Lab portfolio for Token class to use and
 *     store. Can also be used to store extra data like getStatBlocks() from other sources if
 *     needed. The portfolio used is stored as a File and/or URL so the token can be easily updated.
 */
public class HeroLabData {
  private static Asset DEFAULT_HERO_LAB_TOKEN_ASSET;
  private static Asset DEFAULT_HERO_LAB_PORTRAIT_ASSET;
  private MD5Key heroLabStatblockAssetID;

  private String name;
  private String summary;
  private String playerName;
  private String gameSystem;
  private String heroLabIndex;
  private String minionMasterIndex;
  private String minionMasterName = "";

  private boolean isAlly = true;
  private boolean isDirty = false;
  private boolean isMinion = false;

  private File portfolioFile;
  private String portfolioPath;
  private long lastModified = 0L;

  private final Map<String, MD5Key> heroImageAssets = new HashMap<>();

  private static interface DefaultAssetKey {
    final String PORTRAIT_KEY = "0";
    final String TOKEN_KEY = "1";
    final String HANDOUT_KEY = "2";
  }

  public interface StatBlockKey {
    String DATA = "data";
    String LOCATION = "location";
  }

  public interface StatBlockType {
    String TEXT = "text";
    String HTML = "html";
    String XML = "xml";
  }

  public HeroLabData(String name) {
    this.name = name;

    try {
      DEFAULT_HERO_LAB_TOKEN_ASSET =
          Asset.createImageAsset(
              "DEFAULT_HERO_LAB_TOKEN", RessourceManager.getImage(Images.HEROLABS_TOKEN));
      DEFAULT_HERO_LAB_PORTRAIT_ASSET =
          Asset.createImageAsset(
              "DEFAULT_HERO_LAB_PORTRAIT", RessourceManager.getImage(Images.HEROLABS_PORTRAIT));
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!AssetManager.hasAsset(DEFAULT_HERO_LAB_TOKEN_ASSET))
      AssetManager.putAsset(DEFAULT_HERO_LAB_TOKEN_ASSET);

    if (!AssetManager.hasAsset(DEFAULT_HERO_LAB_PORTRAIT_ASSET))
      AssetManager.putAsset(DEFAULT_HERO_LAB_PORTRAIT_ASSET);

    heroImageAssets.put(DefaultAssetKey.TOKEN_KEY, DEFAULT_HERO_LAB_TOKEN_ASSET.getMD5Key());
    heroImageAssets.put(DefaultAssetKey.PORTRAIT_KEY, DEFAULT_HERO_LAB_PORTRAIT_ASSET.getMD5Key());
  }

  /**
   * Evaluate the HeroLab XML statBlock against the supplied xPath expression, returning the results
   * as a String List with the requested delimiter.
   *
   * <p>Sample expression: /document/public/character/race/@racetext
   *
   * @param xPathExpression the expression
   * @param delim the delimiter to use for the returned list
   * @return the String list (which may contain only a single element)
   * @throws IllegalArgumentException if xPathExpression is empty, invalid, or does not point to
   *     text or attribute nodes.
   */
  public String parseXML(String xPathExpression, String delim) {
    if (xPathExpression.isEmpty()) throw new IllegalArgumentException("Empty XPath expression");

    String results;
    XML xmlObj = new XMLDocument(getStatBlock_xml());
    results = String.join(delim, xmlObj.xpath(xPathExpression));

    // System.out.println("HeroLabData parseXML(" + xPathExpression + ") :: '" + results + "'");

    return results;
  }

  /**
   * Evaluate the HeroLab XML statBlock against the supplied xPath expression, returning the results
   * as a JsonArray.
   *
   * @param xPathExpression the expression
   * @return a JsonArray of results
   * @throws IllegalArgumentException if xPathExpression is empty, invalid, or does not point to
   *     text or attribute nodes.
   */
  public JsonArray parseXmlToJson(String xPathExpression) {
    if (xPathExpression.isEmpty()) throw new IllegalArgumentException("Empty XPath expression");

    JsonArray results = new JsonArray();
    XML xmlObj = new XMLDocument(getStatBlock_xml());
    for (String r : xmlObj.xpath(xPathExpression)) {
      results.add(r);
    }

    return results;
  }

  /*
   * Other xpath tests... String xml = heroData.getStatBlock_xml();
   *
   * XPathFactory xpathFactory = XPathFactory.newInstance(); XPath xpath = xpathFactory.newXPath();
   *
   * InputSource source = new InputSource(new StringReader(xml)); String results = ""; try { results = xpath.evaluate(searchText, source); } catch (XPathExpressionException e1) { // TODO
   * Auto-generated catch block e1.printStackTrace(); } System.out.println(searchText); System.out.println(results);
   */

  public boolean refresh() {
    return lastModified != getPortfolioLastModified();
  }

  @SuppressWarnings("unchecked")
  public Map<String, HashMap<String, String>> getStatBlocks() {
    Map<String, HashMap<String, String>> statBlocks = new HashMap<>();

    if (heroLabStatblockAssetID == null) return statBlocks;

    Asset statBlockAsset = AssetManager.getAsset(heroLabStatblockAssetID);
    if (statBlockAsset == null) {
      System.out.println("Requesting asset from the server...");
      statBlockAsset = AssetManager.requestAssetFromServer(heroLabStatblockAssetID);
      int maxWait = 100;
      while (!AssetManager.hasAsset(heroLabStatblockAssetID) && maxWait-- > 0) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        System.out.println("Waiting on asset from server... " + maxWait);
      }

      statBlockAsset = AssetManager.getAsset(heroLabStatblockAssetID);
    }

    try {
      ByteArrayInputStream byteIn = new ByteArrayInputStream(statBlockAsset.getData());
      ObjectInputStream in = new ObjectInputStream(byteIn);
      statBlocks = (HashMap<String, HashMap<String, String>>) in.readObject();
      byteIn.close();
      in.close();
    } catch (ClassNotFoundException | IOException e) {
      System.out.println("ERROR: Attempting to read Asset ID: " + heroLabStatblockAssetID);
      e.printStackTrace();
    }

    return statBlocks;
  }

  public void setStatBlocks(Map<String, Map<String, String>> statBlocks) {
    // Jamz: Since statblocks do not change or accessed often, moved object data to an Asset
    // as heroLabData could be pretty large with XML data causing lag on token transfers
    if (heroLabStatblockAssetID != null) AssetManager.removeAsset(heroLabStatblockAssetID);

    // Convert Map to byte array
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream out;
    try {
      out = new ObjectOutputStream(byteOut);
      if (statBlocks != null) {
        out.writeObject(statBlocks);

        String assetName = getPortfolioFile() + "/" + heroLabIndex + "/" + name;
        Asset statBlockAsset = Asset.createAssetDetectType(assetName, byteOut.toByteArray());
        AssetManager.putAsset(statBlockAsset);
        heroLabStatblockAssetID = statBlockAsset.getMD5Key();

        out.close();
        byteOut.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getStatBlock_location(String type) {
    return getStatBlocks().get(type).get(StatBlockKey.LOCATION);
  }

  public String getStatBlock_data(String type) {
    if (getStatBlocks() == null) return "";

    if (type.equalsIgnoreCase(StatBlockType.HTML)) type = StatBlockType.HTML;
    else if (type.equalsIgnoreCase(StatBlockType.XML)) type = StatBlockType.XML;
    else type = StatBlockType.TEXT;

    if (getStatBlocks().get(type) == null) return "";

    String statBlock = getStatBlocks().get(type).get(StatBlockKey.DATA);

    if (statBlock == null) return "";
    else return statBlock;
  }

  public String getStatBlock_text() {
    return getStatBlock_data(StatBlockType.TEXT);
  }

  public String getStatBlock_html() {
    return getStatBlock_data(StatBlockType.HTML);
  }

  public String getStatBlock_xml() {
    return getStatBlock_data(StatBlockType.XML);
  }

  public boolean isDirty() {
    boolean hasChanged = refresh();

    if (hasChanged) isDirty = true;

    return isDirty;
  }

  public void setDirty(boolean isDirty) {
    if (!isDirty) lastModified = getPortfolioLastModified();
    // lastModified = portfolioWatcher.getLastModified();

    this.isDirty = isDirty;
  }

  public String getPortfolioPath() {
    if (portfolioPath == null) setPortfolioPath("");

    return portfolioPath;
  }

  public void setPortfolioPath(String portfolioPath) {
    this.portfolioPath = portfolioPath;
  }

  public File getPortfolioFile() {
    String fileSyncPath = AppPreferences.fileSyncPath.get();

    if (portfolioPath == null || portfolioPath.isEmpty() || fileSyncPath.isEmpty()) {
      return portfolioFile;
    } else {
      if (portfolioPath.startsWith(fileSyncPath)) return new File(portfolioPath);
      else return new File(fileSyncPath, portfolioPath);
    }
  }

  public void setPortfolioFile(File portfolioFile) {
    this.portfolioFile = portfolioFile;
    lastModified = getPortfolioLastModified();

    if (!AppPreferences.fileSyncPath.get().isEmpty()) {
      try {
        portfolioPath =
            Paths.get(AppPreferences.fileSyncPath.get())
                .relativize(portfolioFile.toPath())
                .toString();
      } catch (IllegalArgumentException e) {
        System.out.println(
            "Unable to relativize paths for: ["
                + portfolioFile
                + "] ["
                + AppPreferences.fileSyncPath.get()
                + "]");
        portfolioPath = "";
      }
    } else {
      portfolioPath = portfolioFile.getPath();
    }
  }

  private long getPortfolioLastModified() {
    if (getPortfolioFile() != null) return getPortfolioFile().lastModified();
    else return 0L;
  }

  public String getLastModifiedDateString() {
    if (lastModified != getPortfolioLastModified())
      return "<html><i>" + new Date(lastModified).toString() + "</i></html>";
    else return new Date(lastModified).toString();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHeroLabIndex() {
    return heroLabIndex;
  }

  public void setHeroLabIndex(String heroLabIndex) {
    this.heroLabIndex = heroLabIndex;
  }

  public String getMinionMasterIndex() {
    return minionMasterIndex;
  }

  public void setMinionMasterIndex(String minionsMasterIndex) {
    this.minionMasterIndex = minionsMasterIndex;
  }

  public String getMinionMasterName() {
    return minionMasterName;
  }

  public void setMinionMasterName(String minionMasterName) {
    this.minionMasterName = minionMasterName;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public boolean isAlly() {
    return isAlly;
  }

  public void setAlly(boolean isAlly) {
    this.isAlly = isAlly;
  }

  public boolean isMinion() {
    return isMinion;
  }

  public void setMinion(boolean isMinion) {
    this.isMinion = isMinion;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getGameSystem() {
    return gameSystem;
  }

  public void setGameSystem(String gameSystem) {
    this.gameSystem = gameSystem;
  }

  public MD5Key getTokenImage() {
    if (!heroImageAssets.containsKey(DefaultAssetKey.TOKEN_KEY))
      heroImageAssets.put(DefaultAssetKey.TOKEN_KEY, DEFAULT_HERO_LAB_TOKEN_ASSET.getMD5Key());

    return heroImageAssets.get(DefaultAssetKey.TOKEN_KEY);
  }

  public void setTokenImage(MD5Key imageAsset) {
    heroImageAssets.put(DefaultAssetKey.TOKEN_KEY, imageAsset);
  }

  public MD5Key getPortraitImage() {
    if (!heroImageAssets.containsKey(DefaultAssetKey.PORTRAIT_KEY))
      heroImageAssets.put(
          DefaultAssetKey.PORTRAIT_KEY, DEFAULT_HERO_LAB_PORTRAIT_ASSET.getMD5Key());

    return heroImageAssets.get(DefaultAssetKey.PORTRAIT_KEY);
  }

  public void setPortraitImage(MD5Key imageAsset) {
    heroImageAssets.put(DefaultAssetKey.PORTRAIT_KEY, imageAsset);
  }

  public MD5Key getHandoutImage() {
    return heroImageAssets.getOrDefault(DefaultAssetKey.HANDOUT_KEY, null);
  }

  public void setHandoutImage(MD5Key imageAsset) {
    heroImageAssets.put(DefaultAssetKey.HANDOUT_KEY, imageAsset);
  }

  public int getImageCount() {
    return heroImageAssets.size();
  }

  public Map<String, MD5Key> getAssetMap() {
    return heroImageAssets;
  }

  public Collection<MD5Key> getAllAssetIDs() {
    HashMap<String, MD5Key> allAssetIDs = new HashMap<>();

    if (heroImageAssets != null) allAssetIDs.putAll(heroImageAssets);

    if (heroLabStatblockAssetID != null) allAssetIDs.put("statBlocks", heroLabStatblockAssetID);

    return allAssetIDs.values();
  }

  public List<String> getAllImageAssetsURLs() {
    List<String> assetSet = new ArrayList<String>();

    for (MD5Key assetKey : heroImageAssets.values()) assetSet.add("asset://" + assetKey.toString());

    return assetSet;
  }

  public MD5Key getImageAssetID(int index) {
    return heroImageAssets.get(Integer.toString(index));
  }

  public void addImage(String imageName, BufferedImage image) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", baos);
    } catch (IOException e) {
      e.printStackTrace();
    }

    Asset imageAsset = Asset.createImageAsset(imageName, baos.toByteArray());
    AssetManager.putAsset(imageAsset);
    this.heroImageAssets.put(Integer.toString(heroImageAssets.size()), imageAsset.getMD5Key());
  }

  public void clearImages() {
    this.heroImageAssets.clear();
  }

  public void setDefaultImages() {
    heroImageAssets.put(DefaultAssetKey.TOKEN_KEY, DEFAULT_HERO_LAB_TOKEN_ASSET.getMD5Key());
    heroImageAssets.put(DefaultAssetKey.PORTRAIT_KEY, DEFAULT_HERO_LAB_PORTRAIT_ASSET.getMD5Key());
  }

  @Override
  public String toString() {
    return JSONMacroFunctions.getInstance().jsonIndent(getInfo(), 4);
  }

  public JsonObject getInfo() {
    JsonObject heroLabInfo = new JsonObject();
    heroLabInfo.addProperty("name", name);
    heroLabInfo.addProperty("summary", summary);
    heroLabInfo.addProperty("playerName", playerName);
    heroLabInfo.addProperty("gameSystem", gameSystem);
    heroLabInfo.addProperty("heroLabIndex", heroLabIndex);
    heroLabInfo.addProperty("masterIndex", minionMasterIndex);
    heroLabInfo.addProperty("masterName", minionMasterName);
    heroLabInfo.addProperty("isDirty", isDirty());
    heroLabInfo.addProperty("isAlly", isAlly);
    heroLabInfo.addProperty("isMinion", isMinion);
    heroLabInfo.addProperty("portfolioFile", getPortfolioFile().getAbsolutePath());
    heroLabInfo.addProperty("portfolioPath", portfolioPath);
    heroLabInfo.addProperty("lastModified", getLastModifiedDateString());

    JsonArray urls = new JsonArray();
    for (String url : getAllImageAssetsURLs()) {
      urls.add(url);
    }
    heroLabInfo.add("images", urls);

    return heroLabInfo;
  }

  public static HeroLabData fromDto(HeroLabDataDto dto) {
    var data = new HeroLabData(dto.getName());
    if (dto.hasHeroLabStatblockAssetId()) {
      data.heroLabStatblockAssetID = new MD5Key(dto.getHeroLabStatblockAssetId().getValue());
    }
    if (dto.hasSummary()) {
      data.summary = dto.getSummary().getValue();
    }
    if (dto.hasPlayerName()) {
      data.playerName = dto.getPlayerName().getValue();
    }
    if (dto.hasGameSystem()) {
      data.gameSystem = dto.getGameSystem().getValue();
    }
    if (dto.hasHeroLabIndex()) {
      data.heroLabIndex = dto.getHeroLabIndex().getValue();
    }
    if (dto.hasMinionMasterIndex()) {
      data.minionMasterIndex = dto.getMinionMasterIndex().getValue();
    }
    if (dto.hasMinionMasterName()) {
      data.minionMasterName = dto.getMinionMasterName().getValue();
    }
    data.isAlly = dto.getIsAlly();
    data.isDirty = dto.getIsDirty();
    data.isMinion = dto.getIsMinion();
    if (dto.hasPortfolioPath()) {
      data.portfolioPath = dto.getPortfolioPath().getValue();
    }

    if (dto.hasPortfolioFile()) {
      data.portfolioFile = new File(dto.getPortfolioFile().getValue());
    }

    dto.getHeroImageAssetsMap()
        .forEach((key, value) -> data.heroImageAssets.put(key, new MD5Key(value)));
    return data;
  }

  public HeroLabDataDto toDto() {
    var data = this;
    var dto = HeroLabDataDto.newBuilder();
    if (heroLabStatblockAssetID != null) {
      dto.setHeroLabStatblockAssetId(StringValue.of(heroLabStatblockAssetID.toString()));
    }
    dto.setName(name);
    if (summary != null) {
      dto.setSummary(StringValue.of(summary));
    }
    if (playerName != null) {
      dto.setPlayerName(StringValue.of(playerName));
    }
    if (gameSystem != null) {
      dto.setGameSystem(StringValue.of(gameSystem));
    }
    if (heroLabIndex != null) {
      dto.setHeroLabIndex(StringValue.of(heroLabIndex));
    }
    if (minionMasterIndex != null) {
      dto.setMinionMasterIndex(StringValue.of(minionMasterIndex));
    }
    if (minionMasterName != null) {
      dto.setMinionMasterName(StringValue.of(minionMasterName));
    }
    dto.setIsAlly(isAlly);
    dto.setIsDirty(isDirty);
    dto.setIsMinion(isMinion);
    if (portfolioPath != null) {
      dto.setPortfolioPath(StringValue.of(portfolioPath));
    }

    if (portfolioFile != null) {
      dto.setPortfolioFile(StringValue.of(portfolioFile.toString()));
    }

    heroImageAssets.forEach((key, value) -> dto.putHeroImageAssets(key, value.toString()));
    return dto.build();
  }
}
