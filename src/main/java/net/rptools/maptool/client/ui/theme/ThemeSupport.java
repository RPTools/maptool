package net.rptools.maptool.client.ui.theme;

import java.nio.file.Path;
import net.rptools.maptool.client.AppConstants;

public class ThemeSupport {

  //public static final Path THEME_BASE_PATH = AppConstants.DEFAULT_UI_THEMES;


  /** The Name of the theme. */
  private final String themeName;

  /** The Description for the theme. */
  private final String description;

  /** The author of the theme. */
  private String author;

  /** The email address of the author of the theme. */
  private String email;

  /** The path to the image used for the chat scroll icon. */
  private String chatScrollImage;

  /** The path to the image used for the chat scroll locked icon. */
  private String chatScrollLockImage;

  /** The path to the image for typing notification on. */
  private String showTypingNotificationsImage;

  /** The path to the image for typing notification off. */
  private String hideTypingNotificationsImage;

  /** The path to the image used for cancel button. */
  private String cancelImage;

  /** The path to the image used for the add button. */
  private String addImage;

  /** The path to the icon for the resource library panel. */
  private String resourceLibraryImage;

  /** The path to the  icon for the map explorer panel. */
  private String mapExplorerImage;

  /** The icon for connections panel. */
  private String connectionsImage;

  /** The icon for the chat panel. */
  private String chatImage;
  private String globalPanelImage;
  private String campaignPanelImage;
  private String selectionPanelImage;
  private String impersonatePanelImage;
  private String tablesImage;
  private String initiativeImage;
  private String panelTextureImage;
  private String checkeredTextureImage;
  private String lightLabelBoxImage;
  private String darkLabelBoxImage;
  private String hilightLabelBoxImage;
  private String pdfFolderImage;
  private String heroLabFolderImage;
  private String disconnectedImage;
  private String connectedImage;
  private String serverImage;
  private String transmitOnImage;
  private String transmitOffImage;
  private String receiveOnImage;
  private String receiveOffImage;
  private String pdfImageImage;
  private String heroLabImage;
  private String rptokenDecorationImage;
  private String heroLabDecorationImage;
  private String minimizeToolBarImage;
  private String maximizeToolBarImage;
  private String hollowOvalVBLImage;
  private String ovalVBLImage;
  private String crossVBLImage;
  private String freehandVBLImage;
  private String isoRectangleVBLImage;
  private String hollowISORectangleVBLImage;
  private String rectangleVBLImage;
  private String hollowRectangleVBLImage;
  private String polygonVBLImage;
  private String emoticonButtonImage;
  private String filterImageImage;
  private String pointerToolImage;
  private String pointerToolOnImage;
  private String pointerToolOffImage;
  private String drawToolOnImage;
  private String drawToolOffImage;
  private String templateToolOnImage;
  private String templateToolOffImage;
  private String fogToolOnImage;
  private String fogToolOffImage;
  private String vblToolOnImage;
  private String vblToolOffImage;
  private String measureToolImage;
  private String textToolImage;
  private String audioImage;
  private String audioOffImage;
  private String pathFindingOnImage;
  private String pathFindingOffImage;
  private String tokenSellectAllOnImage;
  private String tokenSellectAllOffImage;
  private String tokenSellectGMOnImage;
  private String tokenSellectGMOffImage;
  private String tokenSellectPCOnImage;
  private String tokenSellectPCOffImage;
  private String tokenSellectNPCOnImage;
  private String tokenSellectNPCOffImage;
  private String mapButtonImageImage;
  private String drawRectangleImage;
  private String drawCircleImage;
  private String drawDiamondImage;
  private String drawFreehandLinesImage;
  private String drawStraightLinesImage;
  private String templateVertexRadiusImage;
  private String templateCellRadiusImage;
  private String templateConeImage;
  private String templateVertexLineImage;
  private String templateCellLineImage;
  private String templateBurstImage;
  private String templateBlastImage;
  private String templateWallImage;
  private String fogDiamondImage;
  private String fogFreehandImage;
  private String fogOvalImage;
  private String fogPolyImage;
  private String fogRectangleImage;
  private String mapPointerImage;
  private String deseletAllImage;
  private String selectPreviousImage;
  private String selectAllImage;
  private String selectNextImage;
  private String revertToPreviousImage;
  private String assetStatusImage;
  private String blockedMoveImage;
  private String brokenImageImage;
  private String chatActionPointerImage;
  private String chatNotificationImage;
  private String initiativeCurrentImage;
  private String mapCursorImage;
  private String diskSpaceImage;
  private String rptokTokenDecorationImage;
  private String herolabTokenDecorationImage;
  private String logConsolePanelImage;
  private String heroLabPortraitImage;
  private String heroLabRefreshDataImage;
  private String heroLabRefreshDataDisableImage;
  private String imageCacheImage;

  ThemeSupport(String name, String desc) {
    themeName = name;
    description = desc;
  }


}
