/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.maptool.util;

import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.HeroLabData;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extracts character information from a Hero Lab portfolio
 * 
 * @author Jamz
 *
 */
public final class ExtractHeroLab {
	private static final File tmpDir = AppUtil.getTmpDir();
	private static final int BUFFER_SIZE = 4096;

	private static final Image HERO_LAB_PORTRAIT = new ImageIcon(
			ExtractHeroLab.class.getClassLoader().getResource("net/rptools/maptool/client/image/powered_by_hero_lab_small.png")).getImage();
	private static final Image HERO_LAB_TOKEN = new ImageIcon(
			ExtractHeroLab.class.getClassLoader().getResource("net/rptools/maptool/client/image/hero-lab-token.png")).getImage();

	private File finalTempDir;
	private File extractComplete;
	private File portfolioFile;

	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;

	public ExtractHeroLab(File heroLabPortfolio, boolean forceRescan) {
		this.portfolioFile = heroLabPortfolio;
		factory = DocumentBuilderFactory.newInstance();

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finalTempDir = new File(tmpDir + "/hero_lab_" + heroLabPortfolio.hashCode());
		extractComplete = new File(tmpDir + "/hero_lab_" + heroLabPortfolio.hashCode() + "/completed_" + heroLabPortfolio.hashCode() + ".txt");

		if (forceRescan)
			FileUtils.deleteQuietly(finalTempDir);

		finalTempDir.mkdirs();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(finalTempDir)));
	}

	public File getTempDir() {
		return finalTempDir;
	}

	public boolean isExtracted() {
		if (extractComplete.exists())
			return true;
		else
			return false;
	}

	public void markComplete() {
		try {
			extractComplete.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<File> getCharacterList() {
		return getCharacterList(false);
	}

	public List<File> getCharacterList(boolean forceRescan) {
		List<File> heroes = new ArrayList<File>();

		if (isExtracted() && !forceRescan) {
			heroes.addAll(Arrays.asList(finalTempDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".rptok");
				}
			})));

			return heroes;
		}

		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			Document portfolioIndex = getPortolioIndex();

			XPathExpression xPath_characters = xpath.compile("//character"); // Jamz: using this vs //document/characters/character which also captures //document/characters/character/minions/character
			XPathExpression xPath_portraitImage = xpath.compile("images/image[1]");
			XPathExpression xPath_tokenImage = xpath.compile("images/image[2]");
			IteratableNodeList heroNodes = new IteratableNodeList((NodeList) xPath_characters.evaluate(portfolioIndex, XPathConstants.NODESET));

			for (Node hero : heroNodes) {
				String heroName = ((Element) hero).getAttribute("name");
				File heroFile = getFileName(finalTempDir.getCanonicalPath(), heroName, "." + Token.FILE_EXTENSION);
				File portraitImageFile = getFileName(finalTempDir.getCanonicalPath(), heroName + "_portrait", ".png");
				File heroImageFile = getFileName(finalTempDir.getCanonicalPath(), heroName + "_token", ".png");
				HeroLabData heroLabData = new HeroLabData(heroName);

				// We need to set a convention so the second image in Hero Lab will be the token image
				Node tokenImageNode = (Node) xPath_tokenImage.evaluate(hero, XPathConstants.NODE);
				Element tokenImageElement = (Element) tokenImageNode;
				if (tokenImageElement != null) {
					String imageFileName = tokenImageElement.getAttribute("filename");
					String imageFolder = tokenImageElement.getAttribute("folder");
					heroImageFile = getFileName(finalTempDir.getCanonicalPath(), imageFileName, "");
					extractFile(imageFolder + "/" + imageFileName, heroImageFile);
				} else {
					createDefaultImage(heroImageFile, HERO_LAB_TOKEN);
				}

				// We need to set a convention so the first image in Hero Lab will be the portrait
				Node portraitImageNode = (Node) xPath_portraitImage.evaluate(hero, XPathConstants.NODE);
				Element portraitImageElement = (Element) portraitImageNode;
				if (portraitImageElement != null) {
					String imageFileName = portraitImageElement.getAttribute("filename");
					String imageFolder = portraitImageElement.getAttribute("folder");
					portraitImageFile = getFileName(finalTempDir.getCanonicalPath(), imageFileName, "");
					extractFile(imageFolder + "/" + imageFileName, portraitImageFile);
				} else {
					createDefaultImage(portraitImageFile, HERO_LAB_PORTRAIT);
				}

				// If there's at least a portrait, lets show that
				if (tokenImageElement == null && portraitImageElement != null) {
					heroImageFile = portraitImageFile;
				}

				// Lets create the token!
				Token heroLabToken = new Token();
				Asset heroImageAsset = null;
				heroImageAsset = AssetManager.createAsset(heroImageFile);
				AssetManager.putAsset(heroImageAsset);
				heroLabToken = new Token(heroName, heroImageAsset.getId());
				heroLabToken.setGMName(heroName);

				// set the image shape
				Image image = ImageIO.read(heroImageFile);
				heroLabToken.setShape(TokenUtil.guessTokenType(image));
				heroLabData.addImage(image);

				// set the height/width, fixes dragging to stamp layer issue
				heroLabToken.setHeight(ImageUtil.createCompatibleImage(image).getHeight());
				heroLabToken.setWidth(ImageUtil.createCompatibleImage(image).getWidth());

				// set the portrait image asset
				Asset portraitAsset = AssetManager.createAsset(portraitImageFile); // Change for portrait
				AssetManager.putAsset(portraitAsset);
				heroLabToken.setPortraitImage(portraitAsset.getId());
				heroLabData.addImage(ImageIO.read(portraitImageFile));
				// Save the token and add it to the file list

				heroLabData.setPortfolioFile(portfolioFile);
				heroLabData.setSummary(((Element) hero).getAttribute("summary"));
				heroLabData.setPlayerName(((Element) hero).getAttribute("playername"));
				heroLabData.setAlly(((Element) hero).getAttribute("isally").equalsIgnoreCase("yes") ? true : false);
				heroLabData.setStatBlocks(getStatBlocks(xpath, hero));

				if (heroLabData.isAlly()) {
					heroLabToken.setType(Type.PC);
					heroLabToken.setOwnedByAll(true);
				} else {
					heroLabToken.setType(Type.NPC);
				}

				heroLabToken.setHeroLabData(heroLabData);

				//Jamz TODO: lets make an option to show portrait OR image in asset panel! Also how about a HeroLab icon decoration?
				PersistenceUtil.saveToken(heroLabToken, heroFile, true);
				heroes.add(heroFile);
				markComplete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return heroes;
	}

	private File getFileName(String filePath, String fileName, String extension) {
		File newFileName = new File(filePath + "/" + fileName + extension);

		try {
			newFileName = newFileName.getCanonicalFile();
		} catch (IOException e) {
			System.out.println("oh oh!");
			e.printStackTrace();
		}

		int count = 2;
		while (newFileName.exists()) {
			newFileName = new File(filePath + "/" + fileName + "_" + count++ + extension);
		}

		try {
			if (newFileName.createNewFile()) {
				newFileName.delete();
			}
		} catch (IOException e) {
			System.out.println("Bad file name, replacing bad characters in: " + fileName + extension);
			fileName = fileName.replaceAll("[^a-zA-Z0-9\\._ \\/`~!@#$%\\^&\\(\\)\\-=\\+\\[\\]\\{\\}',\\\\:]+", "_");
			newFileName = new File(filePath + "/" + fileName + extension);
			System.out.println("New file name: " + newFileName.getAbsolutePath());
		}

		return newFileName;

	}

	private Document getPortolioIndex() throws IOException, SAXException {
		ZipFile por = new ZipFile(portfolioFile);
		ZipEntry indexEntry = por.getEntry("index.xml");
		Document indexXml = null;

		if (indexEntry != null) {
			InputStream is = por.getInputStream(indexEntry);
			indexXml = builder.parse(is);
		}

		por.close();

		return indexXml;
	}

	public HeroLabData refreshStatblocks(HeroLabData heroData) {
		HashMap statBlocks = new HashMap(3);

		statBlocks.put(HeroLabData.StatBlockType.TEXT, getStatBlock(heroData.getStatBlock_location(HeroLabData.StatBlockType.TEXT), HeroLabData.StatBlockType.TEXT));
		statBlocks.put(HeroLabData.StatBlockType.HTML, getStatBlock(heroData.getStatBlock_location(HeroLabData.StatBlockType.HTML), HeroLabData.StatBlockType.HTML));
		statBlocks.put(HeroLabData.StatBlockType.XML, getStatBlock(heroData.getStatBlock_location(HeroLabData.StatBlockType.XML), HeroLabData.StatBlockType.XML));

		heroData.setStatBlocks(statBlocks);

		return heroData;
	}

	private HashMap getStatBlocks(XPath xpath, Node hero) {
		HashMap statBlocks = new HashMap(3);

		statBlocks.put(HeroLabData.StatBlockType.TEXT, getStatBlock(getStatBlockPath(xpath, hero, HeroLabData.StatBlockType.TEXT), HeroLabData.StatBlockType.TEXT));
		statBlocks.put(HeroLabData.StatBlockType.HTML, getStatBlock(getStatBlockPath(xpath, hero, HeroLabData.StatBlockType.HTML), HeroLabData.StatBlockType.HTML));
		statBlocks.put(HeroLabData.StatBlockType.XML, getStatBlock(getStatBlockPath(xpath, hero, HeroLabData.StatBlockType.XML), HeroLabData.StatBlockType.XML));

		return statBlocks;
	}

	private String getStatBlockPath(XPath xpath, Node hero, String type) {
		String path = "/";

		try {
			XPathExpression xPath_statBlock = xpath.compile("statblocks/statblock[@format='" + type + "']");
			Node statBlockNode = (Node) xPath_statBlock.evaluate(hero, XPathConstants.NODE);
			if (statBlockNode != null)
				path = ((Element) statBlockNode).getAttribute("folder") + "/" + ((Element) statBlockNode).getAttribute("filename");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return path;
	}

	private HashMap getStatBlock(String zipPath, String type) {
		HashMap statBlock = new HashMap(2);
		ZipFile por;

		try {
			por = new ZipFile(portfolioFile);
			ZipEntry indexEntry = por.getEntry(zipPath);

			if (indexEntry != null) {
				statBlock.put("location", zipPath);
				statBlock.put("data", IOUtils.toString(por.getInputStream(indexEntry), "UTF-8"));
			} else {
				statBlock.put("location", null);
				statBlock.put("data", "<HTML>Unable to retrieve " + type + " statblock</HTML>");
			}

			por.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return statBlock;
	}

	private File extractFile(String fileInputPath, File fileOutputPath) throws IOException {
		ZipFile por = new ZipFile(portfolioFile);
		ZipEntry entry = por.getEntry(fileInputPath);
		InputStream zipIn = por.getInputStream(entry);

		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileOutputPath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
		por.close();

		return fileOutputPath;
	}

	private void createDefaultImage(File fileOutputPath, Image image) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileOutputPath));
		byte[] bytesIn = ImageUtil.imageToBytes(ImageUtil.createCompatibleImage(image), "png");

		bos.write(bytesIn);
		bos.close();
	}

	/*
	 * Use: printDocument(doc, System.out);
	 */
	private static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc),
				new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
}