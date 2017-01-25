package net.rptools.maptool.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import net.rptools.lib.MD5Key;
import net.sf.json.JSONObject;

/**
 * @author Jamz
 * 
 *         Created to hold various bits of info about a Hero Lab portfolio for Token class to use and store. Can also be used to store extra data like statblocks from other sources if needed. The
 *         portfolio used is stored as a File and/or URL so the token can be easily updated.
 * 
 */
public class HeroLabData {
	private static Asset HERO_LAB_TOKEN_ASSET;
	private static Asset HERO_LAB_PORTRAIT_ASSET;

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
	private URL portfolioURL;
	//	private transient FileEntry portfolioWatcher;
	private long lastModified = 0L;

	private HashMap<String, Asset> heroAssets = new HashMap<>();
	private HashMap<String, HashMap<String, String>> statBlocks;

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
			HERO_LAB_TOKEN_ASSET = new Asset("DEFAULT_HERO_LAB_TOKEN",
					ImageIO.read(HeroLabData.class.getClassLoader().getResource("net/rptools/maptool/client/image/hero-lab-token.png")));
			HERO_LAB_PORTRAIT_ASSET = new Asset("DEFAULT_HERO_LAB_PORTRAIT",
					ImageIO.read(HeroLabData.class.getClassLoader().getResource("net/rptools/maptool/client/image/powered_by_hero_lab_small.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!AssetManager.hasAsset(HERO_LAB_TOKEN_ASSET))
			AssetManager.putAsset(HERO_LAB_TOKEN_ASSET);

		if (!AssetManager.hasAsset(HERO_LAB_PORTRAIT_ASSET))
			AssetManager.putAsset(HERO_LAB_PORTRAIT_ASSET);

		heroAssets.put(DefaultAssetKey.TOKEN_KEY, HERO_LAB_TOKEN_ASSET);
		heroAssets.put(DefaultAssetKey.PORTRAIT_KEY, HERO_LAB_PORTRAIT_ASSET);
	}

	/*
	 * Evaluate the HeroLab XML statBlock against the supplied xPath expression Sample expression: /document/public/character/race/@racetext
	 */
	public String parseXML(String xPathExpression) {
		if (xPathExpression.isEmpty())
			return "Error: No XPath expression given.";

		String results;
		XML xmlObj = new XMLDocument(getStatBlock_xml());
		results = String.join(", ", xmlObj.xpath(xPathExpression));

		//		System.out.println("HeroLabData parseXML(" + xPathExpression + ") :: '" + results + "'");

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
		//		return portfolioWatcher.refresh(portfolioFile);
		//		System.out.println("lastModified = " + lastModified);
		//		System.out.println("getPortfolioLastModified() = " + getPortfolioLastModified());

		return lastModified != getPortfolioLastModified();
	}

	public HashMap<String, HashMap<String, String>> getStatBlocks() {
		return statBlocks;
	}

	public void setStatBlocks(HashMap<String, HashMap<String, String>> statBlocks) {
		this.statBlocks = statBlocks;
	}

	public String getStatBlock_location(String type) {
		return (String) statBlocks.get(type).get(StatBlockKey.LOCATION);
	}

	public String getStatBlock_data(String type) {
		if (statBlocks == null)
			return "";

		if (type.equalsIgnoreCase(StatBlockType.HTML))
			type = StatBlockType.HTML;
		else if (type.equalsIgnoreCase(StatBlockType.XML))
			type = StatBlockType.XML;
		else
			type = StatBlockType.TEXT;

		if (statBlocks.get(type) == null)
			return "";

		Object statBlock = statBlocks.get(type).get(StatBlockKey.DATA);

		if (statBlock == null)
			return "";
		else
			return (String) statBlock;
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

		if (hasChanged)
			isDirty = true;

		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		if (!isDirty)
			lastModified = getPortfolioLastModified();
		//			lastModified = portfolioWatcher.getLastModified();

		this.isDirty = isDirty;
	}

	public File getPortfolioFile() {
		return portfolioFile;
	}

	public void setPortfolioFile(File portfolioFile) {
		//		System.out.println("portfolioFile null? why? " + (portfolioFile == null));

		this.portfolioFile = portfolioFile;
		//		portfolioWatcher = new FileEntry(portfolioFile);
		//		portfolioWatcher.refresh(portfolioFile);
		//		lastModified = portfolioWatcher.getLastModified();
		lastModified = getPortfolioLastModified();
	}

	private long getPortfolioLastModified() {
		if (portfolioFile != null)
			return portfolioFile.lastModified();
		else
			return 0L;
	}

	public String getLastModifiedDateString() {
		//		if (lastModified != portfolioWatcher.getLastModified())
		if (lastModified != getPortfolioLastModified())
			return "<html><i>" + new Date(lastModified).toString() + "</i></html>";
		else
			return new Date(lastModified).toString();
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

	public URL getPortfolioURL() {
		return portfolioURL;
	}

	public void setPortfolioURL(URL portfolioURL) {
		this.portfolioURL = portfolioURL;
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

	public Asset getTokenImage() {
		if (!heroAssets.containsKey(DefaultAssetKey.TOKEN_KEY))
			heroAssets.put(DefaultAssetKey.TOKEN_KEY, HERO_LAB_TOKEN_ASSET);

		return heroAssets.get(DefaultAssetKey.TOKEN_KEY);
	}

	public void setTokenImage(Asset imageAsset) {
		heroAssets.put(DefaultAssetKey.TOKEN_KEY, imageAsset);
	}

	public Asset getPortraitImage() {
		if (!heroAssets.containsKey(DefaultAssetKey.PORTRAIT_KEY))
			heroAssets.put(DefaultAssetKey.PORTRAIT_KEY, HERO_LAB_PORTRAIT_ASSET);

		return heroAssets.get(DefaultAssetKey.PORTRAIT_KEY);
	}

	public void setPortraitImage(Asset imageAsset) {
		heroAssets.put(DefaultAssetKey.PORTRAIT_KEY, imageAsset);
	}

	public Asset getHandoutImage() {
		if (heroAssets.containsKey(DefaultAssetKey.HANDOUT_KEY))
			return heroAssets.get(DefaultAssetKey.HANDOUT_KEY);
		else
			return null;
	}

	public void setHandoutImage(Asset imageAsset) {
		heroAssets.put(DefaultAssetKey.HANDOUT_KEY, imageAsset);
	}

	public int getImageCount() {
		return heroAssets.size();
	}

	public Map<String, Asset> getAssetMap() {
		return heroAssets;
	}

	public Set<MD5Key> getAllImageAssets() {
		Set<MD5Key> assetSet = new HashSet<MD5Key>();

		for (Asset asset : heroAssets.values())
			assetSet.add(asset.getId());

		return assetSet;
	}

	public List<String> getAllImageAssetsURLs() {
		List<String> assetSet = new ArrayList<String>();

		for (Asset asset : heroAssets.values())
			assetSet.add("asset://" + asset.getId().toString());

		return assetSet;
	}

	public MD5Key getImageAssetID(int index) {
		return heroAssets.get(Integer.toString(index)).getId();
	}

	public void addImage(String imageName, BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Asset imageAsset = new Asset(imageName, baos.toByteArray());
		AssetManager.putAsset(imageAsset);
		this.heroAssets.put(Integer.toString(heroAssets.size()), imageAsset);
	}

	public void clearImages() {
		this.heroAssets.clear();
	}

	public void setDefaultImages() {
		heroAssets.put(DefaultAssetKey.TOKEN_KEY, HERO_LAB_TOKEN_ASSET);
		heroAssets.put(DefaultAssetKey.PORTRAIT_KEY, HERO_LAB_PORTRAIT_ASSET);
	}

	public String toString() {
		return getInfo().toString(4);
	}

	public JSONObject getInfo() {
		Map<String, Object> heroLabInfo = new HashMap<String, Object>();
		heroLabInfo.put("name", name);
		heroLabInfo.put("summary", summary);
		heroLabInfo.put("playerName", playerName);
		heroLabInfo.put("gameSystem", gameSystem);
		heroLabInfo.put("heroLabIndex", heroLabIndex);
		heroLabInfo.put("masterIndex", minionMasterIndex);
		heroLabInfo.put("masterName", minionMasterName);
		heroLabInfo.put("isDirty", isDirty());
		heroLabInfo.put("isAlly", isAlly);
		heroLabInfo.put("isMinion", isMinion);
		heroLabInfo.put("portfolioFile", portfolioFile.getAbsolutePath());
		heroLabInfo.put("lastModified", getLastModifiedDateString());
		heroLabInfo.put("images", getAllImageAssetsURLs().toArray());

		return JSONObject.fromObject(heroLabInfo);
	}
}
