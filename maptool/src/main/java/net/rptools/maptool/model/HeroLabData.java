package net.rptools.maptool.model;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.iterators.ListIteratorWrapper;
import org.apache.commons.io.monitor.FileEntry;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

/**
 * @author Jamz
 * 
 *         Created to hold various bits of info about a Hero Lab portfolio for
 *         Token class to use and store. Can also be used to store extra data
 *         like statblocks from other sources if needed. The portfolio used is
 *         stored as a File and/or URL so the token can be easily updated.
 * 
 */

/**
 * @author Jamz
 *
 */
/**
 * @author Jamz
 *
 */
public class HeroLabData {
	private String name;
	private String summary;
	private String playerName;
	private String gameSystem;

	private boolean isAlly;
	private boolean isDirty;

	private HashMap statBlocks; // HashMap(<String> StatBlockType, <HashMap>(<String> StatBlockKey, <String> StatBlock))

	public interface StatBlockKey {
		String DATA = "data";
		String LOCATION = "location";
	}

	public interface StatBlockType {
		String TEXT = "text";
		String HTML = "html";
		String XML = "xml";
	}

	private File portfolioFile;
	private URL portfolioURL;
	private FileEntry portfolioWatcher;
	private long lastModified;

	private transient List<Image> images = new ArrayList<Image>();

	public HeroLabData(String name) {
		this.name = name;
	}

	/*
	 * Evaluate the HeroLab XML statBlock against the supplied xPath expression
	 * Sample expression: /document/public/character/race/@racetext
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

	/* Other xpath tests...
		String xml = heroData.getStatBlock_xml();
	
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
	
		InputSource source = new InputSource(new StringReader(xml));
		String results = "";
		try {
			results = xpath.evaluate(searchText, source);
		} catch (XPathExpressionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(searchText);
		System.out.println(results);
	  */

	public boolean hasChanged() {
		return portfolioWatcher.refresh(portfolioFile);
	}

	public HashMap getStatBlocks() {
		return statBlocks;
	}

	public void setStatBlocks(HashMap statBlocks) {
		this.statBlocks = statBlocks;
	}

	public String getStatBlock_location(String type) {
		return (String) ((HashMap) statBlocks.get(type)).get(StatBlockKey.LOCATION);
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

		if ((HashMap) statBlocks.get(type) == null)
			return "";

		Object statBlock = ((HashMap) statBlocks.get(type)).get(StatBlockKey.DATA);

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
		boolean hasChanged = hasChanged();

		if (hasChanged)
			isDirty = true;

		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		if (!isDirty)
			lastModified = portfolioWatcher.getLastModified();

		this.isDirty = isDirty;
	}

	public File getPortfolioFile() {
		return portfolioFile;
	}

	public void setPortfolioFile(File portfolioFile) {
		this.portfolioFile = portfolioFile;
		portfolioWatcher = new FileEntry(portfolioFile);
		portfolioWatcher.refresh(portfolioFile);
		lastModified = portfolioWatcher.getLastModified();
	}

	public String getLastModified() {
		if (lastModified != portfolioWatcher.getLastModified())
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

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public void addImage(Image image) {
		this.images.add(image);
	}

	public void clearImages() {
		this.images.clear();
	}

	public String getGameSystem() {
		return gameSystem;
	}

	public void setGameSystem(String gameSystem) {
		this.gameSystem = gameSystem;
	}

	public String toString() {
		return "Name: " + name
				+ "\n summary: " + summary
				+ "\n isAlly: " + isAlly
				+ "\n playerName: " + playerName
				+ "\n lastModified: " + lastModified
				+ "\n statBlock_text: " + getStatBlock_text()
				+ "\n statBlock_html: " + getStatBlock_html()
				+ "\n statBlock_xml: " + getStatBlock_xml();
	}
}
