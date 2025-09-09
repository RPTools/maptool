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
package net.rptools.maptool.client.ui.token.dialog.edit;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatButton;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.swing.*;
import net.rptools.lib.MD5Key;
import net.rptools.lib.MathUtil;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.*;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.ImageSupport;

/**
 * Support class that does much of the heavy lifting for TokenLayoutPanel. Links control components
 * and the mouse events to property changes in the reference token. Stores a bunch of useful values.
 * Disables the default OK button click on "Enter" when editing spinner fields.
 *
 * @author 2024 - Reverend/Bubblobill
 */
class TokenLayoutPanelHelper {
  protected static final double MAX_ZOOM = 3d;
  protected static final double MIN_ZOOM = 0.3d;
  protected static final double MAX_SCALE = 3d;
  protected static final double MIN_SCALE = 0.1d;

  protected TokenLayoutPanelHelper(
      AbeillePanel<?> parentAbeillePanel, TokenLayoutRenderPanel renderPane, AbstractButton okBtn) {
    parent = parentAbeillePanel;
    renderPanel = renderPane;
    renderPanel.setHelper(this);
    setOKButton((JButton) okBtn);
    init();

    parent.addComponentListener(
        new ComponentAdapter() {
          private void doStuff() {
            if (parentRoot == null) {
              setParentRoot(parent.getRootPane());
            }
            flagAsDirty();
          }

          @Override
          public void componentMoved(ComponentEvent e) {
            super.componentMoved(e);
            doStuff();
          }

          @Override
          public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            doStuff();
          }

          @Override
          public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            doStuff();
          }
        });
  }

  private void init() {
    getSizeCombo().addItemListener(sizeListener);
    getFlippedXCB().addChangeListener(e -> setTokenFlipX(getFlippedXCB().isSelected()));
    getFlippedYCB().addChangeListener(e -> setTokenFlipY(getFlippedYCB().isSelected()));
    getFlippedIsoCB().addChangeListener(e -> setTokenFlipIso(getFlippedIsoCB().isSelected()));

    initButtons();
    initSpinners();
    initSliders();
    pairControls();
  }

  private final EnumSet<ImageSupport.FlipDirection> flipDirections =
      EnumSet.noneOf(ImageSupport.FlipDirection.class);
  private static final UIDefaults UI_DEFAULTS = UIManager.getDefaults();
  private static final double DEFAULT_FONT_SIZE = UI_DEFAULTS.getFont("defaultFont").getSize2D();
  protected Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
  private boolean noGrid = GridFactory.getGridType(grid).equals(GridFactory.NONE);
  private int gridSize = grid.getSize();
  private double cellHeight = grid.getCellHeight();
  private double cellWidth = grid.getCellWidth();
  private static final CellPoint ORIGIN = new CellPoint(0, 0);
  private Token originalToken, mirrorToken;
  private BufferedImage tokenImage;
  private @Nullable TokenFootprint footprint;
  private ArrayList<Point2D> cellCentres;
  protected boolean isIsoFigure = false;
  protected Rectangle2D footprintBounds;
  protected RenderBits renderBits = new RenderBits();

  AbeillePanel<?> parent;
  private JRootPane parentRoot;
  private final TokenLayoutRenderPanel renderPanel;
  private JComboBox<?> sizeCombo;
  private JSpinner anchorXSpinner, anchorYSpinner, scaleSpinner, zoomSpinner;
  private JSlider anchorXSlider, anchorYSlider, scaleSlider, zoomSlider;
  private AbstractButton okButton;

  private void setOKButton(JButton b) {
    okButton = b;
  }

  private void setParentRoot(JRootPane rp) {
    parentRoot = rp;
  }

  private final String helpText = assembleHelpText();

  private JCheckBox flippedIsoCB;
  private JCheckBox flippedXCB;
  private JCheckBox flippedYCB;

  public JCheckBox getFlippedIsoCB() {
    if (flippedIsoCB == null) {
      flippedIsoCB = parent.getCheckBox("@isFlippedIso");
    }
    return flippedIsoCB;
  }

  public JCheckBox getFlippedXCB() {
    if (flippedXCB == null) {
      flippedXCB = parent.getCheckBox("@isFlippedX");
    }
    return flippedXCB;
  }

  public JCheckBox getFlippedYCB() {
    if (flippedYCB == null) {
      flippedYCB = parent.getCheckBox("@isFlippedY");
    }
    return flippedYCB;
  }

  private JComboBox<?> getSizeCombo() {
    if (sizeCombo == null) sizeCombo = (JComboBox<?>) parent.getComponent("size");
    return sizeCombo;
  }

  private JSpinner getAnchorXSpinner() {
    if (anchorXSpinner == null) anchorXSpinner = parent.getSpinner("anchorXSpinner");
    return anchorXSpinner;
  }

  private JSpinner getAnchorYSpinner() {
    if (anchorYSpinner == null) anchorYSpinner = parent.getSpinner("anchorYSpinner");
    return anchorYSpinner;
  }

  private JSpinner getScaleSpinner() {
    if (scaleSpinner == null) scaleSpinner = parent.getSpinner("scaleSpinner");
    return scaleSpinner;
  }

  private JSpinner getZoomSpinner() {
    if (zoomSpinner == null) zoomSpinner = parent.getSpinner("zoomSpinner");
    return zoomSpinner;
  }

  private JSlider getAnchorXSlider() {
    if (anchorXSlider == null) anchorXSlider = (JSlider) parent.getComponent("anchorXSlider");
    return anchorXSlider;
  }

  private JSlider getAnchorYSlider() {
    if (anchorYSlider == null) anchorYSlider = (JSlider) parent.getComponent("anchorYSlider");
    return anchorYSlider;
  }

  private JSlider getScaleSlider() {
    if (scaleSlider == null) scaleSlider = (JSlider) parent.getComponent("scaleSlider");
    return scaleSlider;
  }

  private JSlider getZoomSlider() {
    if (zoomSlider == null) zoomSlider = (JSlider) parent.getComponent("zoomSlider");
    return zoomSlider;
  }

  private TokenLayoutRenderPanel getRenderPanel() {
    return renderPanel;
  }

  SpinnerSliderPaired anchorXPair, anchorYPair, scalePair, zoomPair;

  double getTokenSizeScale() {
    return mirrorToken.getSizeScale();
  }

  int getTokenAnchorX() {
    return mirrorToken.getAnchorX();
  }

  int getTokenAnchorY() {
    return mirrorToken.getAnchorY();
  }

  private boolean getTokenFlippedX() {
    return mirrorToken.isFlippedX();
  }

  private boolean getTokenFlippedY() {
    return mirrorToken.isFlippedY();
  }

  private boolean getTokenFlippedIso() {
    return mirrorToken.getIsFlippedIso();
  }

  private void setTokenSizeScale(Number scale) {
    mirrorToken.setSizeScale(scale.doubleValue());
  }

  private void setTokenAnchorX(Number x) {
    mirrorToken.setAnchorX(x.intValue());
  }

  private void setTokenAnchorY(Number y) {
    mirrorToken.setAnchorY(y.intValue());
  }

  protected void setTokenFlipIso(Boolean b) {
    mirrorToken.setIsFlippedIso(b);
    if (flipDirections.contains(ImageSupport.FlipDirection.ISOMETRIC) && !b) {
      flipDirections.remove(ImageSupport.FlipDirection.ISOMETRIC);
    } else if (!flipDirections.contains(ImageSupport.FlipDirection.ISOMETRIC) && b) {
      flipDirections.add(ImageSupport.FlipDirection.ISOMETRIC);
    }
    flagAsDirty();
  }

  protected void setTokenFlipX(Boolean b) {
    mirrorToken.setFlippedX(b);
    if (flipDirections.contains(ImageSupport.FlipDirection.HORIZONTAL) && !b) {
      flipDirections.remove(ImageSupport.FlipDirection.HORIZONTAL);
    } else if (!flipDirections.contains(ImageSupport.FlipDirection.HORIZONTAL) && b) {
      flipDirections.add(ImageSupport.FlipDirection.HORIZONTAL);
    }
    flagAsDirty();
  }

  protected void setTokenFlipY(Boolean b) {
    mirrorToken.setFlippedY(b);
    if (flipDirections.contains(ImageSupport.FlipDirection.VERTICAL) && !b) {
      flipDirections.remove(ImageSupport.FlipDirection.VERTICAL);
    } else if (!flipDirections.contains(ImageSupport.FlipDirection.VERTICAL) && b) {
      flipDirections.add(ImageSupport.FlipDirection.VERTICAL);
    }
    flagAsDirty();
  }

  /**
   * These functions serve to link the scale and zoom sliders and spinners and allow a useful
   * representation of values < 100%. Slider ranges from -200 to 0 are for values below 100% and 0
   * to 200 for 100% to 300%
   */
  private static final Function<Integer, Number> PERCENT_SLIDER_TO_SPINNER =
      i ->
          i <= 0
              ? MathUtil.mapToRange(i.doubleValue(), -200.0, 0.0, 0.0, 1.0)
              : Math.max(MathUtil.mapToRange(i.doubleValue(), 0.0, 200.0, 1.0, 3.0), 0.05);

  private static final Function<Number, Integer> PERCENT_SPINNER_TO_SLIDER =
      d ->
          (int)
              (d.doubleValue() <= 1
                  ? MathUtil.mapToRange(d.doubleValue(), 0.0, 1.0, -200, 0)
                  : MathUtil.mapToRange(d.doubleValue(), 1.0, 3.0, 0, 200));

  private void storeFlipDirections() {
    if (getTokenFlippedIso()) {
      flipDirections.add(ImageSupport.FlipDirection.ISOMETRIC);
    }
    if (getTokenFlippedX()) {
      flipDirections.add(ImageSupport.FlipDirection.HORIZONTAL);
    }
    if (getTokenFlippedY()) {
      flipDirections.add(ImageSupport.FlipDirection.VERTICAL);
    }
  }

  void resetPanel() {
    setToken(originalToken, false);
    renderPanel.calcZoomFactor();
  }

  void resetPanelToDefault() {
    setToken(originalToken, true);
  }

  private final PropertyChangeListener controlListener = evt -> flagAsDirty();

  private final FocusListener focusListener =
      new FocusListener() {
        /* Stop "Enter" closing the window when editing fields. */
        @Override
        public void focusGained(FocusEvent e) {
          ((JComponent) e.getComponent()).getRootPane().setDefaultButton(null);
        }

        @Override
        public void focusLost(FocusEvent e) {
          if (e.getComponent() instanceof JFormattedTextField formattedTextField) {
            try {
              formattedTextField.commitEdit();
            } catch (ParseException pe) {
              // Edited value is invalid, revert the spinner to the last valid value,
              formattedTextField.setValue(formattedTextField.getValue());
            }
          }
          ((JComponent) e.getComponent()).getRootPane().setDefaultButton((JButton) okButton);
        }
      };

  /** size/footprint lives on another tab. Need to cope with changes. */
  private final ItemListener sizeListener =
      new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          if (mirrorToken != null) {
            final var selected = getSizeCombo().getSelectedItem();
            if (selected instanceof TokenFootprint tmpFP) {
              setFootprint(tmpFP);
            } else {
              setFootprint(null);
            }
          }
        }
      };

  /** Mark the rendering panel in need of repainting */
  protected void flagAsDirty() {
    getRenderPanel().flagAsDirty();
  }

  protected void setTokenImageId(MD5Key tokenImageKey) {
    tokenImage = ImageManager.getImage(tokenImageKey);
  }

  protected BufferedImage getTokenImage() {
    return tokenImage;
  }

  /**
   * Write changes to token
   *
   * @param tok Source token
   */
  protected void commitChanges(Token tok) {
    tok.setAnchor(getTokenAnchorX(), getTokenAnchorY());
    tok.setSizeScale(getTokenSizeScale());
    tok.setFlippedX(flipDirections.contains(ImageSupport.FlipDirection.HORIZONTAL));
    tok.setFlippedY(flipDirections.contains(ImageSupport.FlipDirection.VERTICAL));
    tok.setIsFlippedIso(flipDirections.contains(ImageSupport.FlipDirection.ISOMETRIC));
  }

  protected void setToken(Token token, boolean useDefaults) {
    grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
    boolean isoGrid = grid.isIsometric();
    noGrid = GridFactory.getGridType(grid).equals(GridFactory.NONE);
    renderBits = new RenderBits();
    gridSize = grid.getSize();
    cellHeight = grid.getCellHeight();
    cellWidth = grid.getCellWidth();

    this.originalToken = new Token(token, true); // duplicate for resetting purposes
    /* The mirror token contains all changes prior to being committed by clicking OK */
    this.mirrorToken = new Token(token, false);

    if (useDefaults) {
      mirrorToken.setSizeScale(1d);
      mirrorToken.setAnchor(0, 0);
    }

    if (mirrorToken.getFootprint(grid) != getSizeCombo().getSelectedItem()) {
      var selected = getSizeCombo().getSelectedItem();
      switch (selected) {
        case TokenFootprint tmpFP ->
            mirrorToken.setFootprint(grid, grid.getFootprint(tmpFP.getId()));
        case null, default -> mirrorToken.setSnapToScale(false);
      }
    }

    isIsoFigure =
        isoGrid
            && mirrorToken.getShape() == Token.TokenShape.FIGURE
            && !mirrorToken.getIsFlippedIso();

    /* the image to manipulate */
    tokenImage = ImageManager.getImage(mirrorToken.getImageAssetId());

    if (mirrorToken.isSnapToScale()) {
      setFootprint(mirrorToken.getFootprint(grid));
    } else {
      setFootprint(null);
    }

    /* set slider values to realistic values based on footprint. */
    getAnchorXSlider().setMinimum((int) -Math.ceil(footprintBounds.getWidth()));
    getAnchorXSlider().setMaximum((int) Math.ceil(footprintBounds.getWidth()));
    if (isIsoFigure) {
      /* Allow more vertical travel for iso figures */
      getAnchorYSlider().setMinimum((int) -Math.ceil(1.4 * footprintBounds.getHeight()));
      getAnchorYSlider().setMaximum((int) Math.ceil(1.4 * footprintBounds.getHeight()));
    } else {
      getAnchorYSlider().setMinimum((int) -Math.ceil(footprintBounds.getHeight()));
      getAnchorYSlider().setMaximum((int) Math.ceil(footprintBounds.getHeight()));
    }
    /* align mouse drag bounds with sliders */
    getRenderPanel().setMaxXoff(getAnchorXSlider().getMaximum());
    getRenderPanel().setMaxYoff(getAnchorYSlider().getMaximum());

    storeFlipDirections();

    /* Assign Suppliers and Consumers to the linked controls and add a PropertyChangeListener */
    anchorXPair.setPropertySetter(this::setTokenAnchorX);
    anchorXPair.setPropertyGetter(this::getTokenAnchorX);
    anchorXPair.setPropertyName("AnchorX");
    anchorXPair.addPropertyChangeListener(controlListener);

    anchorYPair.setPropertySetter(this::setTokenAnchorY);
    anchorYPair.setPropertyGetter(this::getTokenAnchorY);
    anchorYPair.setPropertyName("AnchorY");
    anchorYPair.addPropertyChangeListener(controlListener);

    scalePair.setPropertySetter(this::setTokenSizeScale);
    scalePair.setPropertyGetter(this::getTokenSizeScale);
    scalePair.setPropertyName("Scale");
    scalePair.addPropertyChangeListener(controlListener);

    zoomPair.setPropertySetter(getRenderPanel().getZoomConsumer());
    zoomPair.setPropertyGetter(getRenderPanel().getZoomSupplier());
    zoomPair.setPropertyName("Zoom");
    zoomPair.addPropertyChangeListener(controlListener);

    setControlValues();
    getRenderPanel()
        .addComponentListener(
            new ComponentAdapter() {
              @Override
              public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                renderBits.init();
              }
            });
  }

  /**
   * Convert footprint details to things we can use and paint
   *
   * @param fp token's grid footprint
   */
  private void setFootprint(@Nullable TokenFootprint fp) {
    this.footprint = fp;
    setCentredFootprintBounds();

    Set<CellPoint> occupiedCells =
        footprint == null ? Collections.emptySet() : footprint.getOccupiedCells(ORIGIN);
    Rectangle2D aggregateBounds = new Rectangle2D.Double();
    cellCentres = new ArrayList<>(occupiedCells.size());
    for (CellPoint cp : occupiedCells) {
      cellCentres.add(grid.getCellCenter(cp));
      aggregateBounds.add(grid.getBounds(cp));
    }
    double xFix = -aggregateBounds.getCenterX();
    double yFix = -aggregateBounds.getCenterY();
    cellCentres.replaceAll(pt -> new Point2D.Double(pt.getX() + xFix, pt.getY() + yFix));
    flagAsDirty();
  }

  private void setCentredFootprintBounds() {
    if (grid == null) {
      return;
    }
    if (footprint == null) {
      var width = mirrorToken.getWidth();
      var height = mirrorToken.getHeight();
      footprintBounds = new Rectangle2D.Double(-width / 2d, -height / 2d, width, height);
    } else if (!noGrid) {
      footprintBounds = footprint.getBounds(grid, ORIGIN);
      footprintBounds =
          new Rectangle2D.Double(
              -footprintBounds.getWidth() / 2d,
              -footprintBounds.getHeight() / 2d,
              footprintBounds.getWidth(),
              footprintBounds.getHeight());
    } else {
      double factor = footprint.getScale();
      footprintBounds =
          new Rectangle2D.Double(
              -gridSize / 2d * factor,
              -gridSize / 2d * factor,
              gridSize * factor,
              gridSize * factor);
    }
    flagAsDirty();
  }

  /** Link the spinners to the sliders and join them in matrimonial bliss */
  private void pairControls() {
    anchorXPair = new SpinnerSliderPaired(getAnchorXSpinner(), getAnchorXSlider());
    anchorYPair = new SpinnerSliderPaired(getAnchorYSpinner(), getAnchorYSlider());

    scalePair =
        new SpinnerSliderPaired(
            getScaleSpinner(),
            getScaleSlider(),
            null,
            PERCENT_SPINNER_TO_SLIDER,
            PERCENT_SLIDER_TO_SPINNER);
    scalePair.setFloor(MIN_SCALE);
    scalePair.setCeiling(MAX_SCALE);

    zoomPair =
        new SpinnerSliderPaired(
            getZoomSpinner(),
            getZoomSlider(),
            null,
            PERCENT_SPINNER_TO_SLIDER,
            PERCENT_SLIDER_TO_SPINNER);
    zoomPair.setFloor(MIN_SCALE);
    zoomPair.setCeiling(MAX_SCALE);
  }

  private void initSpinners() {
    /* models */
    getAnchorXSpinner().setModel(new SpinnerNumberModel(0d, -200d, 200d, 1d));
    getAnchorYSpinner().setModel(new SpinnerNumberModel(0d, -200d, 200d, 1d));
    getScaleSpinner().setModel(new SpinnerNumberModel(1d, 0d, 3d, 0.1));
    getZoomSpinner().setModel(new SpinnerNumberModel(1d, 0d, 3d, 0.1));
    getZoomSpinner().setVisible(false);
    /* editors */
    getAnchorXSpinner().setEditor(new JSpinner.NumberEditor(anchorXSpinner, "0"));
    getAnchorYSpinner().setEditor(new JSpinner.NumberEditor(anchorYSpinner, "0"));
    getScaleSpinner().setEditor(new JSpinner.NumberEditor(scaleSpinner, "0%"));

    ((JSpinner.NumberEditor) getAnchorXSpinner().getEditor()).getTextField().setColumns(3);
    ((JSpinner.NumberEditor) getAnchorYSpinner().getEditor()).getTextField().setColumns(3);
    ((JSpinner.NumberEditor) getScaleSpinner().getEditor()).getTextField().setColumns(4);

    /* listeners */
    ((JSpinner.NumberEditor) getAnchorXSpinner().getEditor())
        .getTextField()
        .addFocusListener(focusListener);
    ((JSpinner.NumberEditor) getAnchorYSpinner().getEditor())
        .getTextField()
        .addFocusListener(focusListener);
    ((JSpinner.NumberEditor) getScaleSpinner().getEditor())
        .getTextField()
        .addFocusListener(focusListener);
  }

  private void initButtons() {
    FlatButton layoutHelpButton = new FlatButton();
    layoutHelpButton.setButtonType(FlatButton.ButtonType.help);
    parent.replaceComponent("layoutTabPanel", "layoutHelpButton", layoutHelpButton);
    layoutHelpButton.setToolTipText(helpText);
    layoutHelpButton.addActionListener(e -> showHelp());
    layoutHelpButton.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            flagAsDirty();
          }
        });
  }

  private String assembleHelpText() {
    String rowText = "<tr><th align='right'>%s<td>%s";
    String caption = "<html><table><caption color='white' bgcolor='navy'><b>%s</b></caption>";
    return caption.formatted(I18N.getString("EditTokenDialog.layout.help.caption"))
        + rowText.formatted(
            I18N.getString("Mouse.leftDrag"),
            I18N.getString("EditTokenDialog.layout.help.moveImage"))
        + rowText.formatted(
            I18N.getString("Mouse.rightDrag"),
            I18N.getString("EditTokenDialog.layout.help.moveView"))
        + rowText.formatted(
            I18N.getString("Mouse.leftDoubleClick"),
            I18N.getString("EditTokenDialog.layout.help.reset"))
        + rowText.formatted(
            I18N.getString("Mouse.rightDoubleClick"),
            I18N.getString("EditTokenDialog.layout.help.resetDefaults"))
        + rowText.formatted(
            I18N.getString("Mouse.wheel"), I18N.getString("EditTokenDialog.layout.help.scaleImage"))
        + rowText.formatted(
            I18N.getString("Mouse.ctrlWheel"),
            I18N.getString("EditTokenDialog.layout.help.zoomView"));
  }

  private void showHelp() {
    JPanel helpPanel = new JPanel(new BorderLayout());
    GenericDialogFactory gdf =
        GenericDialog.getFactory()
            .setDialogTitle(I18N.getText("EditTokenDialog.layout.help.caption"))
            .setContent(helpPanel)
            .makeModal(true)
            .setCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
            .addButton(ButtonKind.OK, e -> flagAsDirty());
    JLabel helpTextContainer = new JLabel();
    helpTextContainer.setText(helpText);
    helpPanel.add(new JScrollPane(helpTextContainer), BorderLayout.NORTH);
    gdf.display();
  }

  private void initSliders() {
    getAnchorXSlider().setModel(new DefaultBoundedRangeModel(0, gridSize, -gridSize, gridSize));
    getAnchorYSlider().setModel(new DefaultBoundedRangeModel(0, gridSize, -gridSize, gridSize));
    getScaleSlider()
        .setModel(
            new DefaultBoundedRangeModel(
                0, 0, getScaleSlider().getMinimum(), getScaleSlider().getMaximum()));
    getZoomSlider()
        .setModel(
            new DefaultBoundedRangeModel(
                0, 0, getZoomSlider().getMinimum(), getZoomSlider().getMaximum()));

    class VertLabel extends VerticalLabel {
      private double divisor = 1;

      private VertLabel(String text) {
        super(text);
      }

      private void setDivisor(double divisor) {
        this.divisor = divisor;
      }

      protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (isRotated())
          g2d.rotate(
              Math.toRadians(-90 * getRotation() / divisor),
              getPreferredSize().getWidth(),
              getPreferredSize().getHeight());

        super.paintComponent(g2d);
        g2d.dispose();
      }
    }
    Dictionary<Integer, JLabel> offSetYLabels = new Hashtable<>();
    Dictionary<Integer, JLabel> offSetXLabels = new Hashtable<>();
    for (int i = -1200; i <= 1200; i += 50) {
      JLabel label1 = new JLabel(String.valueOf(i));
      offSetYLabels.put(i, label1);
      VertLabel label2 = new VertLabel(String.valueOf(i));
      label2.setRotation(VerticalLabel.ROTATE_RIGHT);
      label2.setDivisor(3d);
      offSetXLabels.put(i, label2);
    }
    getAnchorYSlider().setLabelTable(offSetYLabels);
    getAnchorXSlider().setLabelTable(offSetXLabels);

    DecimalFormat df = new DecimalFormat("##0%");
    Dictionary<Integer, JLabel> pctLabels = new Hashtable<>();
    pctLabels.put(-200, new JLabel(df.format(0)));
    pctLabels.put(-100, new JLabel(df.format(0.5)));
    pctLabels.put(0, new JLabel(df.format(1)));
    pctLabels.put(100, new JLabel(df.format(2)));
    pctLabels.put(200, new JLabel(df.format(3)));

    getScaleSlider().setLabelTable(pctLabels);
    getZoomSlider().setLabelTable(pctLabels);
  }

  /** Set controls to match token values */
  private void setControlValues() {
    getAnchorXSpinner().setValue(getTokenAnchorX());
    getAnchorYSpinner().setValue(getTokenAnchorY());
    getScaleSpinner().setValue(getTokenSizeScale());
    getZoomSpinner().setValue(1d);
  }

  /**
   * Just a convenient place to hold a bunch of stuff that is not token related but purely for the
   * rendering panel
   */
  protected class RenderBits {
    private RenderBits() {
      if (FlatLaf.isLafDark()) {
        panelTexture = ImageUtil.negativeImage(panelTexture);
        for (int i = 0; i < COLOURS.length; i++) {
          COLOURS[i] = new Color(ImageUtil.negativeColourInt(COLOURS[i].getRGB()));
        }
      }

      backgroundTexture =
          new TexturePaint(
              panelTexture, new Rectangle(0, 0, panelTexture.getWidth(), panelTexture.getHeight()));
      setStrokeArrays();
      gridShapeFill = createGridShape(false);
      gridShapeOutline = createGridShape(true);
    }

    protected void init() {
      Dimension size = getRenderPanel().getSize();
      viewBounds = getRenderPanel().getVisibleRect();
      viewOffset = getRenderPanel().getViewOffset();
      centrePoint =
          new Point2D.Double(
              size.width / 2d + viewOffset.getX(), size.height / 2d + viewOffset.getY());

      if (zoomFactor != getRenderPanel().getZoomFactor()) {
        zoomFactor = getRenderPanel().getZoomFactor();
        constrainedZoom = Math.clamp(zoomFactor, 1, 1.6d);
        setStrokeArrays();
      }

      if (tokenImage == null) {
        /* just to avoid Div/0 if called before image loaded */
        tokenImage =
            new BufferedImage(
                (int) footprintBounds.getWidth(),
                (int) footprintBounds.getHeight(),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
      }

      workImage =
          ImageSupport.getScaledTokenImage(
              tokenImage, mirrorToken, grid, zoomFactor, AppPreferences.renderQuality.get());
      workImage = getFlippedImage(workImage);
    }

    private void setStrokeArrays() {
      if (solidStrokes == null) {
        solidStrokes = new BasicStroke[STROKE_MODELS.length];
        dashedStrokes = new BasicStroke[STROKE_MODELS.length];
      }
      float thickest = STROKE_MODELS[0].getLineWidth();
      float thinnest = STROKE_MODELS[3].getLineWidth();
      boolean mapValues = constrainedZoom != 1f;
      for (int i = 0; i < STROKE_MODELS.length; i++) {
        BasicStroke model = STROKE_MODELS[i];
        float useWidth;
        if (mapValues) {
          useWidth =
              (float)
                  MathUtil.mapToRange(
                      model.getLineWidth(),
                      thinnest,
                      thickest,
                      thinnest * constrainedZoom,
                      thickest * constrainedZoom);
          solidStrokes[i] = new BasicStroke(useWidth, model.getEndCap(), model.getLineJoin());
          dashedStrokes[i] =
              new BasicStroke(
                  useWidth,
                  model.getEndCap(),
                  model.getLineJoin(),
                  model.getMiterLimit(),
                  model.getDashArray(),
                  model.getDashPhase());
        }
      }
    }

    protected static final RenderingHints RENDERING_HINTS = ImageUtil.getRenderingHintsQuality();
    private static final float LINE_SIZE = (float) (DEFAULT_FONT_SIZE / 12f);
    protected Rectangle2D viewBounds;
    private Point2D viewOffset, centrePoint;
    private double zoomFactor = 1;
    private double constrainedZoom = 1;
    private static BufferedImage panelTexture = RessourceManager.getImage(Images.TEXTURE_PANEL);
    private BufferedImage workImage;
    protected static TexturePaint backgroundTexture;
    private Shape centreMark;
    private final Shape gridShapeFill;
    private final Shape gridShapeOutline;
    private BasicStroke[] solidStrokes, dashedStrokes;
    private static final BasicStroke[] STROKE_MODELS =
        new BasicStroke[] {
          new BasicStroke(
              LINE_SIZE * 2.35f,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_ROUND,
              1f,
              new float[] {4f, 6f},
              2f),
          new BasicStroke(
              LINE_SIZE * 1.63f,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_ROUND,
              1f,
              new float[] {3.5f, 6.5f},
              1.5f),
          new BasicStroke(
              LINE_SIZE * 1.45f,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_ROUND,
              1f,
              new float[] {2f, 8f},
              1.5f),
          new BasicStroke(
              LINE_SIZE,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_ROUND,
              1f,
              new float[] {2.5f, 7.5f},
              1.5f)
        };
    private static final Color[] COLOURS =
        new Color[] {Color.YELLOW, Color.RED, Color.BLUE, Color.BLACK, Color.DARK_GRAY};

    /**
     * Returns an image flipped according to the token's flip properties
     *
     * @param bi Image to flip
     * @return Flipped bufferedImage
     */
    private BufferedImage getFlippedImage(BufferedImage bi) {
      ImageSupport.FlipDirection direction =
          ImageSupport.FlipDirection.getFlipDirection(
              flipDirections.contains(ImageSupport.FlipDirection.HORIZONTAL),
              flipDirections.contains(ImageSupport.FlipDirection.VERTICAL),
              flipDirections.contains(ImageSupport.FlipDirection.ISOMETRIC));
      if (!direction.equals(ImageSupport.FlipDirection.NONE)) {
        bi = ImageSupport.flipCartesian(bi, direction);
      }
      if (direction.isFlippedIso()) {
        bi = ImageUtil.flipIsometric(bi, true, AppPreferences.renderQuality.get());
      }
      return bi;
    }

    private Shape createGridShape(boolean trueSize) {
      return Grid.createGridShape(
          GridFactory.getGridType(grid), (trueSize ? grid.getSize() : grid.getSize() - 8));
    }

    /** Itty bitty cross to show the dead-centre of the footprint */
    private void createCentreMark() {
      double aperture = Math.max(cellHeight, cellWidth) / 7.0;
      double r = aperture / 4.0;
      Path2D path = new Path2D.Double();
      path.moveTo(-r, -r);
      path.lineTo(r, r);
      path.moveTo(-r, r);
      path.lineTo(r, -r);
      centreMark = path;
    }

    protected void paintCentreMark(Graphics g) {
      if (centreMark == null) {
        createCentreMark();
      }
      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(centrePoint.getX(), centrePoint.getY());
      g2d.scale(constrainedZoom, constrainedZoom);
      g2d.setStroke(
          new BasicStroke(
              (float) (constrainedZoom * 2.5f),
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_BEVEL,
              10f));
      g2d.setColor(COLOURS[4]);
      Composite oldAc = g2d.getComposite();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITIES[0]));
      g2d.draw(centreMark);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITIES[2]));
      g2d.setStroke(
          new BasicStroke(
              (float) (constrainedZoom * 1.5f),
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_BEVEL,
              10f));
      g2d.setColor(COLOURS[3]);
      g2d.draw(centreMark);
      g2d.setComposite(oldAc);
      renderBits.paintShapeOutLine(g2d, centreMark, true, true);
      g2d.dispose();
    }

    /**
     * Used for gridless maps, paints concentric rings with an interval of the grid size. Paints a
     * ring of a different colour at the radius associated with the footprint scale. Also paints
     * some radial lines to assist with alignment
     *
     * @param g graphics object
     * @param zoomFactor zoom level applied to view
     */
    private void paintRings(Graphics g, double zoomFactor) {
      Graphics2D g2d = (Graphics2D) g.create();
      /* used with no grid. A set of rings and radial lines. */
      TokenFootprint fp = mirrorToken.getFootprint(grid);
      Rectangle2D fpCellBounds = fp.getBounds(grid, ORIGIN);
      double footprintScale = footprint == null ? 1 : footprint.getScale();
      fpCellBounds =
          new Rectangle2D.Double(
              0,
              0,
              fpCellBounds.getWidth() * footprintScale,
              fpCellBounds.getHeight() * footprintScale);

      double cx = viewBounds.getCenterX() + viewOffset.getX();
      double cy = viewBounds.getCenterY() + viewOffset.getY();

      double gap = grid.getSize() * zoomFactor;
      double maxRadius = Math.hypot(this.viewBounds.getWidth(), this.viewBounds.getHeight());
      double currentRadius = gap / 2d;
      double tokenRadius = fpCellBounds.getCenterX() * zoomFactor;
      Line2D lineLong = new Line2D.Double(cx + currentRadius / 2d, cy, maxRadius, cy);
      Line2D lineShort = new Line2D.Double(cx + gap * 2d, cy, maxRadius, cy);

      /* draw radial lines */
      for (double i = 0; i < 24; i++) {
        if (i % 6 == 0) {
          continue; /* skip cardinal lines */
        }
        paintShapeOutLine(
            g2d,
            AffineTransform.getRotateInstance(Math.TAU / 24 * i, cx, cy)
                .createTransformedShape(i % 2 == 1 ? lineShort : lineLong),
            false,
            true);
      }

      /* draw rings */
      while (currentRadius < maxRadius) {
        Ellipse2D e =
            new Ellipse2D.Double(
                cx - currentRadius, cy - currentRadius, 2 * currentRadius, 2 * currentRadius);
        paintShapeOutLine(g2d, e, true, true);
        currentRadius += gap;
      }
      paintShapeOutLine(
          g2d,
          new Ellipse2D.Double(
              cx - tokenRadius, cy - tokenRadius, tokenRadius * 2, tokenRadius * 2),
          true,
          false);
      g2d.dispose();
    }

    /**
     * Horizontal and vertical lines oriented on cx/cy
     *
     * @param g graphics object
     * @param solid Draw as solid else dashed
     * @param colourSet1 Use colour set 1 else 2
     */
    protected void paintCentreLines(Graphics g, boolean solid, boolean colourSet1) {
      /* create cross-hair with a central gap */
      double cx = centrePoint.getX();
      double cy = centrePoint.getY();
      Rectangle2D r = viewBounds;
      double x = r.getX() - 1,
          y = r.getY() - 1,
          w = x + r.getWidth() + 2,
          h = y + r.getHeight() + 2;

      double aperture = Math.max(cellHeight, cellWidth) / 5.0;
      Path2D lines = new Path2D.Double();
      lines.moveTo(cx, y - h);
      lines.lineTo(cx, cy - aperture);
      lines.moveTo(cx, cy + aperture);
      lines.lineTo(cx, h * 2);
      lines.moveTo(x - w, cy);
      lines.lineTo(cx - aperture, cy);
      lines.moveTo(cx + aperture, cy);
      lines.lineTo(2 * w, cy);
      paintShapeOutLine(g, lines, solid, colourSet1);
    }

    /*
    Each line is drawn as a sequence of overlapping lines.
    The following arrays are used to define each stroke
     */
    private static final float[] OPACITIES = new float[] {0.15f, 0.85f, 0.6f, 1f};
    private static final float[] STROKE_WIDTHS =
        new float[] {2f * LINE_SIZE, 1.5f * LINE_SIZE, LINE_SIZE, 0.5f * LINE_SIZE};
    private static final float[] DASH_PHASES =
        new float[] {
          2f * LINE_SIZE + 2f, 2f * LINE_SIZE + 1.75f, 2f * LINE_SIZE + 1f, 2f * LINE_SIZE + 1.25f
        };
    private static final float[][] DASHES =
        new float[][] {
          {4f * LINE_SIZE + 4f, 6f},
          {4f * LINE_SIZE + 3.5f, 6.5f},
          {4f * LINE_SIZE + 2f, 8f},
          {4f * LINE_SIZE + 2.5f, 7.5f}
        };

    private void paintShapeOutLine(Graphics g, Shape shp, boolean solid, boolean colourSet1) {
      Graphics2D g2d = (Graphics2D) g.create();
      Composite oldAc = g2d.getComposite();
      AlphaComposite ac;
      for (int i = 0; i < 4; i++) {
        switch (i) {
          case 0, 1 -> g2d.setColor(colourSet1 ? COLOURS[2] : COLOURS[3]);
          case 2, 3 -> g2d.setColor(colourSet1 ? COLOURS[0] : COLOURS[1]);
        }
        g2d.setStroke(
            solid
                ? new BasicStroke(STROKE_WIDTHS[i])
                : new BasicStroke(
                    STROKE_WIDTHS[i],
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    1f,
                    DASHES[i],
                    DASH_PHASES[i]));

        ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITIES[i]);
        g2d.setComposite(ac);
        g2d.draw(shp);
      }
      g2d.setComposite(oldAc);
      g2d.dispose();
    }

    protected void paintFootprint(Graphics g, double zoomFactor) {
      if (noGrid) {
        paintRings(g, zoomFactor);
        return;
      }

      Graphics2D g2d = (Graphics2D) g.create();
      Shape oldClip = g2d.getClip();
      AffineTransform oldXform = g2d.getTransform();
      g2d.setRenderingHints(RENDERING_HINTS);

      g2d.translate(centrePoint.getX(), centrePoint.getY());
      g2d.scale(zoomFactor, zoomFactor);

      Shape tmpShape;

      Area clipArea = new Area(g2d.getClipBounds());
      Area tmpClip = new Area(), fpArea = new Area();
      g2d.setStroke(new BasicStroke(1f));
      g2d.setColor(COLOURS[4]);

      double footprintScale = footprint == null ? 1 : footprint.getScale();
      double yCorrection =
          isIsoFigure ? footprintScale * footprintBounds.getHeight() / 2d * zoomFactor : 0;

      Shape scaledOutline, scaledFill;
      /* for drawing sub-cell-sizes */
      if (footprintScale < 1d) {
        scaledOutline =
            AffineTransform.getScaleInstance(footprintScale, footprintScale)
                .createTransformedShape(gridShapeOutline);
        scaledFill =
            AffineTransform.getScaleInstance(footprintScale, footprintScale)
                .createTransformedShape(gridShapeFill);
      } else {
        scaledOutline = gridShapeOutline;
        scaledFill = gridShapeFill;
      }
      for (Point2D pt : cellCentres) {
        AffineTransform ptXform =
            AffineTransform.getTranslateInstance(pt.getX(), pt.getY() + yCorrection);
        if (footprintScale < 1) {
          g2d.draw(ptXform.createTransformedShape(gridShapeOutline));
        }
        g2d.draw(ptXform.createTransformedShape(scaledOutline));
        tmpShape = ptXform.createTransformedShape(scaledFill);
        fpArea.add(new Area(tmpShape));
      }
      tmpClip.subtract(fpArea);
      g2d.setClip(tmpClip);
      g2d.setPaint(FlatLaf.isLafDark() ? new Color(1f, 1f, 1f, 0.35f) : new Color(0, 0, 0, 0.35f));
      g2d.setClip(fpArea);
      g2d.fill(fpArea);

      g2d.setClip(clipArea);
      paintShapeOutLine(g2d, fpArea, true, true);
      g2d.setTransform(oldXform);
      g2d.setClip(oldClip);

      g2d.dispose();
      paintExtraGuides(g);
    }

    private void paintExtraGuides(Graphics g) {
      if (noGrid) {
        return;
      }
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setRenderingHints(RENDERING_HINTS);

      g2d.translate(
          centrePoint.getX(),
          centrePoint.getY() + (isIsoFigure ? footprintBounds.getHeight() * zoomFactor / 2d : 0));

      g2d.setPaint(COLOURS[4]);
      g2d.setStroke(
          new BasicStroke(
              0.5f,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_ROUND,
              10f,
              new float[] {0.5f, 1.75f},
              0));

      double limit = Math.hypot(g2d.getClipBounds().getWidth(), g2d.getClipBounds().getHeight());
      double radius = footprintBounds.getHeight() * zoomFactor;
      Rectangle2D bounds = gridShapeOutline.getBounds2D();
      while (radius < limit) {
        radius += 2 * cellHeight * zoomFactor;
        Shape s =
            AffineTransform.getScaleInstance(
                    radius / bounds.getHeight(), radius / bounds.getHeight())
                .createTransformedShape(gridShapeOutline);
        g2d.draw(s);
      }
      g2d.dispose();
    }

    protected void paintToken(Graphics g, boolean translucent) {
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setRenderingHints(RENDERING_HINTS);
      if (centreMark == null) {
        createCentreMark();
      }

      g2d.translate(centrePoint.getX(), centrePoint.getY());
      g2d.translate(getTokenAnchorX() * zoomFactor, getTokenAnchorY() * zoomFactor);

      Composite oldAc = g2d.getComposite();

      AffineTransform imageXform =
          AffineTransform.getTranslateInstance(
              -workImage.getWidth() / 2d, -workImage.getHeight() / 2d);

      if (translucent) {
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
        g2d.setComposite(ac);
        g2d.drawImage(workImage, imageXform, null);
        g2d.setComposite(oldAc);
      } else {
        g2d.drawImage(workImage, imageXform, null);
      }
      g2d.dispose();
    }
  }
}
