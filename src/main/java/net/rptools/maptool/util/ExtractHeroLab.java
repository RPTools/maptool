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
package net.rptools.maptool.util;

import com.jcabi.xml.XMLDocument;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
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
 * Extracts character information from a Hero Lab portfolio and can create rptok token files in
 * .maptool/tmp or updates the token live
 *
 * @author Jamz
 */
public final class ExtractHeroLab {

  private static final File tmpDir = AppUtil.getTmpDir();
  private static final String MISSING_XML_ERROR_MESSAGE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<error>\n    XML Data is missing from Hero Lab portfolio.\n    Contact LWD Technology support at https://www.wolflair.com\n</error>";
  private final File finalTempDir;
  private final File extractComplete;
  private final File portfolioFile;

  private DocumentBuilderFactory factory;
  private DocumentBuilder builder;

  public ExtractHeroLab(File heroLabPortfolio, boolean forceRescan) {
    this.portfolioFile = validatePortfolioFile(heroLabPortfolio);

    factory = DocumentBuilderFactory.newInstance();

    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }

    finalTempDir = new File(tmpDir + "/hero_lab_" + heroLabPortfolio.hashCode());
    extractComplete =
        new File(
            tmpDir
                + "/hero_lab_"
                + heroLabPortfolio.hashCode()
                + "/completed_"
                + heroLabPortfolio.hashCode()
                + ".txt");

    if (forceRescan) {
      FileUtils.deleteQuietly(finalTempDir);
    }

    finalTempDir.mkdirs();
  }

  private File validatePortfolioFile(File heroLabPortfolio) {
    // If unable to find file, prompt user to point to new location or fail
    if (!heroLabPortfolio.exists()) {
      JFileChooser fileChooser = new JFileChooser(AppPreferences.fileSyncPath.get());
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Hero Lab Portfolio", "por");
      fileChooser.setFileFilter(filter);

      int returnVal = fileChooser.showOpenDialog(null);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        System.out.println(
            "New portfolio picked: " + fileChooser.getSelectedFile().getAbsolutePath());
        return fileChooser.getSelectedFile();
      }
    }

    return heroLabPortfolio;
  }

  private boolean isExtracted() {
    return extractComplete.exists();
  }

  private void markComplete() {
    try {
      extractComplete.createNewFile();
      FileUtils.forceDeleteOnExit(finalTempDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void markComplete(String contents) {
    try {
      PrintWriter out = new PrintWriter(extractComplete);
      out.print(contents);
      out.close();
      FileUtils.forceDeleteOnExit(finalTempDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public List<File> extractAllCharacters() {
    return extractAllCharacters(false);
  }

  public List<File> extractAllCharacters(boolean forceRescan) {
    List<File> heroes = new ArrayList<File>();

    if (isExtracted() && !forceRescan) {
      heroes.addAll(
          Arrays.asList(
              finalTempDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".rptok"))));

      return heroes;
    }

    try {
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      Document portfolioIndex = getPortolioIndex();

      XPathExpression xPath_characters = xpath.compile("//character"); // Jamz: using this vs
      // //document/characters/character which
      // also captures
      // //document/characters/character/minions/character
      XPathExpression xPath_gameSystem = xpath.compile("/document/game/@name");
      XPathExpression xPath_images = xpath.compile("images/image");

      String gameSystem = (String) xPath_gameSystem.evaluate(portfolioIndex, XPathConstants.STRING);
      IteratableNodeList heroNodes =
          new IteratableNodeList(
              (NodeList) xPath_characters.evaluate(portfolioIndex, XPathConstants.NODESET));

      for (Node hero : heroNodes) {
        Token heroLabToken = new Token();
        String heroName = ((Element) hero).getAttribute("name");
        String heroLabIndex = ((Element) hero).getAttribute("herolableadindex");
        HeroLabData heroLabData = new HeroLabData(heroName);
        File heroFile =
            FileUtil.getCleanFileName(
                finalTempDir.getCanonicalPath(), heroName, "." + Token.FILE_EXTENSION);

        // Store all the images for macro use, eg store disguises or alternate portraits
        IteratableNodeList imageNodes =
            new IteratableNodeList((NodeList) xPath_images.evaluate(hero, XPathConstants.NODESET));
        heroLabData.clearImages();
        for (Node image : imageNodes) {
          Element imageElement = (Element) image;
          if (imageElement != null) {
            String imageFileName = imageElement.getAttribute("filename");
            String imageFolder = imageElement.getAttribute("folder");
            heroLabData.addImage(imageFileName, extractImage(imageFolder + "/" + imageFileName));
          }
        }

        // If there's at least a portrait, lets show that
        if (heroLabData.getImageCount() == 1) {
          heroLabData.setTokenImage(heroLabData.getPortraitImage());
        } else if (heroLabData.getImageCount() == 0) {
          heroLabData.setDefaultImages();
        }

        // Lets add everything to the token
        heroLabToken = new Token(heroName, heroLabData.getTokenImage());
        heroLabToken.setGMName(heroName);

        // set the image shape
        heroLabToken.setShape(
            TokenUtil.guessTokenType(ImageManager.getImageAndWait(heroLabData.getTokenImage())));

        // set the portrait image asset
        heroLabToken.setPortraitImage(heroLabData.getPortraitImage());

        // set the hand out image asset (if exists)
        if (heroLabData.getHandoutImage() != null) {
          heroLabToken.setCharsheetImage(heroLabData.getHandoutImage());
        }

        heroLabData.setPortfolioFile(portfolioFile);
        heroLabData.setHeroLabIndex(heroLabIndex);
        heroLabData.setGameSystem(gameSystem);
        heroLabData.setSummary(((Element) hero).getAttribute("summary"));
        heroLabData.setPlayerName(((Element) hero).getAttribute("playername"));
        heroLabData.setAlly(((Element) hero).getAttribute("isally").equalsIgnoreCase("yes"));

        // Is it a minion?
        if (hero.getParentNode().getNodeName().equalsIgnoreCase("minions")) {
          Node master = hero.getParentNode().getParentNode();
          String minionMasterIndex = ((Element) master).getAttribute("herolableadindex");
          String minionMasterName = ((Element) master).getAttribute("name");

          heroLabData.setMinion(true);
          heroLabData.setMinionMasterIndex(minionMasterIndex);
          heroLabData.setMinionMasterName(minionMasterName);
          heroLabData.setStatBlocks(getStatBlocks(xpath, hero, master, heroName));
        } else {
          heroLabData.setStatBlocks(getStatBlocks(xpath, hero));
        }

        if (heroLabData.isAlly()) {
          heroLabToken.setType(Type.PC);
          heroLabToken.setOwnedByAll(true);
        } else {
          heroLabToken.setType(Type.NPC);
        }

        heroLabToken.setHeroLabData(heroLabData);

        PersistenceUtil.saveToken(heroLabToken, heroFile, true);
        heroes.add(heroFile);
        markComplete(new XMLDocument(portfolioIndex).toString());
      }
    } catch (IOException | XPathExpressionException | SAXException e) {
      e.printStackTrace();
    }

    return heroes;
  }

  public HeroLabData refreshCharacter(HeroLabData heroLabData) {
    try {
      String heroLabIndex = heroLabData.getHeroLabIndex();

      // Ugg, @herolableadindex is always 0 for all minions :(
      String minionCriteria = "";
      if (heroLabData.isMinion()) {
        minionCriteria = "' and @name='" + heroLabData.getName();
      }

      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      Document portfolioIndex = getPortolioIndex();

      XPathExpression xPath_characters =
          xpath.compile("//character[@herolableadindex='" + heroLabIndex + minionCriteria + "']");
      XPathExpression xPath_gameSystem = xpath.compile("/document/game/@name");
      XPathExpression xPath_images = xpath.compile("images/image");

      String gameSystem = (String) xPath_gameSystem.evaluate(portfolioIndex, XPathConstants.STRING);
      Node hero = (Node) xPath_characters.evaluate(portfolioIndex, XPathConstants.NODE);

      String heroName = ((Element) hero).getAttribute("name");
      heroLabData = new HeroLabData(heroName);

      IteratableNodeList imageNodes =
          new IteratableNodeList((NodeList) xPath_images.evaluate(hero, XPathConstants.NODESET));
      heroLabData.clearImages();
      for (Node image : imageNodes) {
        Element imageElement = (Element) image;
        if (imageElement != null) {
          String imageFileName = imageElement.getAttribute("filename");
          String imageFolder = imageElement.getAttribute("folder");
          heroLabData.addImage(imageFileName, extractImage(imageFolder + "/" + imageFileName));
        }
      }

      // If there's at least a portrait, lets show that
      if (heroLabData.getImageCount() == 1) {
        heroLabData.setTokenImage(heroLabData.getPortraitImage());
      } else if (heroLabData.getImageCount() == 0) {
        heroLabData.setDefaultImages();
      }

      heroLabData.setPortfolioFile(portfolioFile);
      heroLabData.setHeroLabIndex(heroLabIndex);
      heroLabData.setGameSystem(gameSystem);
      heroLabData.setSummary(((Element) hero).getAttribute("summary"));
      heroLabData.setPlayerName(((Element) hero).getAttribute("playername"));
      heroLabData.setAlly(((Element) hero).getAttribute("isally").equalsIgnoreCase("yes"));

      // Is it a minion?
      if (hero.getParentNode().getNodeName().equalsIgnoreCase("minions")) {
        Node master = hero.getParentNode().getParentNode();
        String minionMasterIndex = ((Element) master).getAttribute("herolableadindex");

        heroLabData.setMinion(true);
        heroLabData.setMinionMasterIndex(minionMasterIndex);

        heroLabData.setStatBlocks(getStatBlocks(xpath, hero, master, heroName));
      } else {
        heroLabData.setStatBlocks(getStatBlocks(xpath, hero));
      }

      markComplete(new XMLDocument(portfolioIndex).toString());
    } catch (IOException | XPathExpressionException | SAXException e) {
      e.printStackTrace();
    }

    return heroLabData;
  }

  private Document getPortolioIndex() throws IOException, SAXException {
    return getXmlFromZip("index.xml");
  }

  private Document getXmlFromZip(String zipPath) throws IOException, SAXException {
    ZipFile por = new ZipFile(portfolioFile);
    ZipEntry indexEntry = por.getEntry(zipPath);
    Document indexXml = null;

    if (indexEntry != null) {
      InputStream is = por.getInputStream(indexEntry);
      indexXml = builder.parse(is);
    }

    por.close();

    return indexXml;
  }

  public HeroLabData refreshHeroLabData(HeroLabData heroData) {
    HashMap<String, Map<String, String>> statBlocks = new HashMap<>(3);

    statBlocks.put(
        HeroLabData.StatBlockType.TEXT,
        getStatBlock(
            heroData.getStatBlock_location(HeroLabData.StatBlockType.TEXT),
            HeroLabData.StatBlockType.TEXT));
    statBlocks.put(
        HeroLabData.StatBlockType.HTML,
        getStatBlock(
            heroData.getStatBlock_location(HeroLabData.StatBlockType.HTML),
            HeroLabData.StatBlockType.HTML));
    statBlocks.put(
        HeroLabData.StatBlockType.XML,
        getStatBlock(
            heroData.getStatBlock_location(HeroLabData.StatBlockType.XML),
            HeroLabData.StatBlockType.XML));

    heroData.setStatBlocks(statBlocks);

    return heroData;
  }

  private Map<String, Map<String, String>> getStatBlocks(XPath xpath, Node hero) {
    return getStatBlocks(xpath, hero, null, null);
  }

  private Map<String, Map<String, String>> getStatBlocks(
      XPath xpath, Node hero, Node master, String minionName) {
    HashMap<String, Map<String, String>> statBlocks = new HashMap<>(3);

    statBlocks.put(
        HeroLabData.StatBlockType.TEXT,
        getStatBlock(
            getStatBlockPath(xpath, hero, HeroLabData.StatBlockType.TEXT),
            HeroLabData.StatBlockType.TEXT));
    statBlocks.put(
        HeroLabData.StatBlockType.HTML,
        getStatBlock(
            getStatBlockPath(xpath, hero, HeroLabData.StatBlockType.HTML),
            HeroLabData.StatBlockType.HTML));

    HashMap<String, String> xmlStatBlockMap = new HashMap<String, String>(2);
    String zipPath;
    // Minion XML statblocks are actually stored in the main characters <minions
    // node, ugg...
    if (master == null) {
      zipPath = getStatBlockPath(xpath, hero, HeroLabData.StatBlockType.XML);
    } else {
      zipPath = getStatBlockPath(xpath, master, HeroLabData.StatBlockType.XML);
    }
    xmlStatBlockMap.put("location", zipPath);
    Document xmlStatBlock;

    try {
      xmlStatBlock = getXmlFromZip(zipPath);

      // This shouldn't happen but unsupported running of Hero Lab on Linux via Wine can cause this
      if (xmlStatBlock == null) {
        xmlStatBlockMap.put("data", MISSING_XML_ERROR_MESSAGE);
        statBlocks.put(HeroLabData.StatBlockType.XML, xmlStatBlockMap);
        return statBlocks;
      }

      if (master == null) {
        // We don't need the minion data in the main character so we will remove that
        // node to save space
        NodeList nodes = xmlStatBlock.getElementsByTagName("minions");
        Element minions = (Element) nodes.item(0);
        minions.getParentNode().removeChild(minions);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlStatBlock);
        transformer.transform(source, result);

        xmlStatBlockMap.put("data", result.getWriter().toString());

      } else {
        // We only need the <character> node for this minion, so lets find it and clone
        // it...
        NodeList nodes = xmlStatBlock.getElementsByTagName("character");
        Node minionNode = null;

        IteratableNodeList allNodes = new IteratableNodeList(nodes);
        for (Node character : allNodes) {
          Element characterElement = (Element) character;
          if (characterElement != null) {
            String characterName = characterElement.getAttribute("name");
            if (characterName.equals(minionName)) {
              minionNode = character.cloneNode(true);

              break;
            }
          }
        }

        // If we find it, remove all <character> nodes and insert the saved clone copy
        // under the proper parent node, <public>
        if (minionNode != null) {
          nodes = xmlStatBlock.getElementsByTagName("character");
          Element character = (Element) nodes.item(0);
          character.getParentNode().removeChild(character);

          nodes = xmlStatBlock.getElementsByTagName("public");
          nodes.item(0).insertBefore(minionNode, null);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlStatBlock);
        transformer.transform(source, result);

        xmlStatBlockMap.put("data", result.getWriter().toString());
      }
      statBlocks.put(HeroLabData.StatBlockType.XML, xmlStatBlockMap);

    } catch (IOException
        | SAXException
        | TransformerFactoryConfigurationError
        | TransformerException e1) {
      e1.printStackTrace();
    }

    return statBlocks;
  }

  private String getStatBlockPath(XPath xpath, Node hero, String type) {
    String path = "/";

    try {
      XPathExpression xPath_statBlock =
          xpath.compile("statblocks/statblock[@format='" + type + "']");
      Node statBlockNode = (Node) xPath_statBlock.evaluate(hero, XPathConstants.NODE);
      if (statBlockNode != null) {
        path =
            ((Element) statBlockNode).getAttribute("folder")
                + "/"
                + ((Element) statBlockNode).getAttribute("filename");
      }
    } catch (XPathExpressionException e) {
      e.printStackTrace();
    }

    return path;
  }

  private Map<String, String> getStatBlock(String zipPath, String type) {
    Map<String, String> statBlock = new HashMap<String, String>(2);
    ZipFile por;

    try {
      por = new ZipFile(portfolioFile);
      ZipEntry indexEntry = por.getEntry(zipPath);

      if (indexEntry != null) {
        statBlock.put("location", zipPath);
        statBlock.put(
            "data", IOUtils.toString(por.getInputStream(indexEntry), StandardCharsets.UTF_8));
      } else {
        statBlock.put("location", null);
        statBlock.put("data", "<HTML>Unable to retrieve " + type + " statblock</HTML>");
      }

      por.close();
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return statBlock;
  }

  private BufferedImage extractImage(String fileInputPath) throws IOException {
    ZipFile por = new ZipFile(portfolioFile);
    ZipEntry entry = por.getEntry(fileInputPath);
    InputStream zipIn = por.getInputStream(entry);
    BufferedImage zipImage = ImageIO.read(zipIn);

    por.close();

    return zipImage;
  }
}
