package net.rptools.maptool.client.ui.token;

import com.jeta.forms.components.colors.JETAColorWell;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


public class TokenPropertiesDialogRaw extends JPanel
{
   JPanel m_mainPanel = new JPanel();
   JLabel m_tokenImage = new JLabel();
   JTabbedPane m_TabPane = new JTabbedPane();
   JPanel m_notesPanel = new JPanel();
   JTextArea m__notes = new JTextArea();
   JTextArea m__GMNotes = new JTextArea();
   JPanel m_propertiesPanel = new JPanel();
   JLabel m_propertiesTable = new JLabel();
   JCheckBox m_copyOrMoveCheckbox = new JCheckBox();
   JCheckBox m_inverseVblCheckbox = new JCheckBox();
   JLabel m_ignoreColorLabel = new JLabel();
   JETAColorWell m_vblIgnoreColorWell = new JETAColorWell();
   JToggleButton m_vblColorPickerToggleButton = new JToggleButton();
   JLabel m_alphaSensitivityLabel = new JLabel();
   JCheckBox m_alwaysVisibleButton = new JCheckBox();
   JLabel m_visibilityLabel = new JLabel();
   JLabel m_jtsDistanceToleranceLabel = new JLabel();
   JLabel m_jtsDistanceToleranceLabel1 = new JLabel();
   JLabel m_optimizationLabel = new JLabel();
   JSpinner m_visibilityToleranceSpinner = new JSpinner();
   JComboBox m_jtsMethodComboBox = new JComboBox();
   JSpinner m_alphaSensitivitySpinner = new JSpinner();
   JSpinner m_jtsDistanceToleranceSpinner = new JSpinner();
   JCheckBox m_hideTokenCheckbox = new JCheckBox();
   JButton m_clearVblButton = new JButton();
   JButton m_autoGenerateVblButton = new JButton();
   JButton m_transferVblToMap = new JButton();
   JButton m_transferVblFromMap = new JButton();
   JToggleButton m_wallVblToggle = new JToggleButton();
   JToggleButton m_hillVblToggle = new JToggleButton();
   JToggleButton m_pitVblToggle = new JToggleButton();
   JToggleButton m_mblToggle = new JToggleButton();
   JPanel m_vblPreviewPanel = new JPanel();
   JLabel m_vblPreview = new JLabel();
   JPanel m_statesPanel = new JPanel();
   JTable m_speechTable = new JTable();
   JButton m_speechClearAllButton = new JButton();
   JPanel m_ownershipPanel = new JPanel();
   JCheckBox m__ownedByAll = new JCheckBox();
   JLabel m_ownershipList = new JLabel();
   JComboBox m_shape = new JComboBox();
   JComboBox m_size = new JComboBox();
   JComboBox m_propertyTypeCombo = new JComboBox();
   JCheckBox m__hasSight = new JCheckBox();
   JComboBox m_sightTypeCombo = new JComboBox();
   JCheckBox m__hasImageTable = new JCheckBox();
   JComboBox m_imageTableCombo = new JComboBox();
   JCheckBox m__snapToGrid = new JCheckBox();
   JLabel m_visibleLabel = new JLabel();
   JCheckBox m__visible = new JCheckBox();
   JLabel m_visibleOnlyToOwnerLabel = new JLabel();
   JCheckBox m__visibleOnlyToOwner = new JCheckBox();
   JLabel m_terrainModifierLabel = new JLabel();
   JComboBox m_terrainModifierOperation = new JComboBox();
   JTextField m_terrainModifier = new JTextField();
   JLabel m_tokenOpacityLabel = new JLabel();
   JSlider m_tokenOpacitySlider = new JSlider();
   JLabel m_tokenOpacityValueLabel = new JLabel();
   JLabel m_ignoreTerrainModifierLabel = new JLabel();
   JList m_terrainModifiersIgnored = new JList();
   JPanel m_charsheetPanel = new JPanel();
   JLabel m_charsheet = new JLabel();
   JPanel m_tokenLayoutPanel = new JPanel();
   JLabel m_tokenLayout = new JLabel();
   JPanel m_portraitPanel = new JPanel();
   JLabel m_portrait = new JLabel();
   JLabel m_Label_LibURIError = new JLabel();
   JCheckBox m__allowURIAccess = new JCheckBox();
   JLabel m_summaryLabel = new JLabel();
   JTabbedPane m_StatblockTabPane = new JTabbedPane();
   JEditorPane m_HTMLstatblockTextArea = new JEditorPane();
   JPanel m_xmlStatblockPanel = new JPanel();
   JLabel m_expressionLabel = new JLabel();
   JTextField m_xmlStatblockSearchTextField = new JTextField();
   JButton m_xmlStatblockSearchButton = new JButton();
   JLabel m_xmlStatblockRTextScrollPane = new JLabel();
   JPanel m_textStatblockPanel = new JPanel();
   JButton m_textStatblockSearchButton = new JButton();
   JLabel m_expressionLabel1 = new JLabel();
   JTextField m_textStatblockSearchTextField = new JTextField();
   JLabel m_textStatblockRTextScrollPane = new JLabel();
   JButton m_setAsHandoutButton = new JButton();
   JList m_heroLabImagesList = new JList();
   JButton m_setAsImageButton = new JButton();
   JButton m_setAsPortraitButton = new JButton();
   JLabel m_portfolioLabel = new JLabel();
   JLabel m_summaryText = new JLabel();
   JLabel m_portfolioLocation = new JLabel();
   JLabel m_lastModified = new JLabel();
   JCheckBox m_isAllyCheckBox = new JCheckBox();
   JButton m_refreshDataButton = new JButton();
   JButton m_cancelButton = new JButton();
   JButton m_okButton = new JButton();
   JTextField m__name = new JTextField();
   JLabel m_tokenGMNameLabel = new JLabel();
   JTextField m__GMName = new JTextField();
   JComboBox m_type = new JComboBox();
   JTextField m__label = new JTextField();
   JTextField m__speechName = new JTextField();

   /**
    * Default constructor
    */
   public TokenPropertiesDialogRaw()
   {
      initializePanel();
   }

   /**
    * Main method for panel
    */
   public static void display()
   {
      JFrame frame = new JFrame();
      frame.setSize(600, 400);
      frame.setLocation(100, 100);
      frame.getContentPane().add(new TokenPropertiesDialogRaw());
      frame.setVisible(true);
   }

   /**
    * Adds fill components to empty cells in the first row and first column of the grid.
    * This ensures that the grid spacing will be the same as shown in the designer.
    * @param cols an array of column indices in the first row where fill components should be added.
    * @param rows an array of row indices in the first column where fill components should be added.
    */
   void addFillComponents( Container panel, int[] cols, int[] rows )
   {
      Dimension filler = new Dimension(10,10);

      boolean filled_cell_11 = false;
      CellConstraints cc = new CellConstraints();
      if ( cols.length > 0 && rows.length > 0 )
      {
         if ( cols[0] == 1 && rows[0] == 1 )
         {

            panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
            filled_cell_11 = true;
         }
      }

      for( int index = 0; index < cols.length; index++ )
      {
         if ( cols[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
      }

      for( int index = 0; index < rows.length; index++ )
      {
         if ( rows[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
      }
   }

   /**
    * Helper method to load an image file from the CLASSPATH
    * @param imageName the package and name of the file to load relative to the CLASSPATH
    * @return an ImageIcon instance with the specified image file
    * @throws IllegalArgumentException if the image resource cannot be loaded.
    */
   public ImageIcon loadImage( String imageName )
   {
      try
      {
         ClassLoader classloader = getClass().getClassLoader();
         java.net.URL url = classloader.getResource( imageName );
         if ( url != null )
         {
            ImageIcon icon = new ImageIcon( url );
            return icon;
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

   /**
    * Method for recalculating the component orientation for 
    * right-to-left Locales.
    * @param orientation the component orientation to be applied
    */
   public void applyComponentOrientation( ComponentOrientation orientation )
   {
      // Not yet implemented...
      // I18NUtils.applyComponentOrientation(this, orientation);
      super.applyComponentOrientation(orientation);
   }

   public JPanel createmainPanel()
   {
      m_mainPanel.setName("mainPanel");
      FormLayout formlayout1 = new FormLayout("FILL:8DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:8DLU:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),CENTER:28PX:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      m_mainPanel.setLayout(formlayout1);

      m_tokenImage.setName("tokenImage");
      m_tokenImage.setText("Label.image");
      m_mainPanel.add(m_tokenImage,cc.xy(2,2));

      m_TabPane.setName("TabPane");
      m_TabPane.addTab("EditTokenDialog.tab.notes",null,createnotesPanel());
      m_TabPane.addTab("EditTokenDialog.label.gmnotes",null,createPanel());
      m_TabPane.addTab("EditTokenDialog.tab.properties",null,createpropertiesPanel());
      m_TabPane.addTab("EditTokenDialog.tab.vbl",null,createPanel1());
      m_TabPane.addTab("EditTokenDialog.tab.state",null,createPanel3());
      m_TabPane.addTab("EditTokenDialog.tab.speech",null,createPanel4());
      m_TabPane.addTab("EditTokenDialog.tab.ownership",null,createownershipPanel());
      m_TabPane.addTab("EditTokenDialog.tab.config",null,createPanel6());
      m_TabPane.addTab("EditTokenDialog.tab.libToken",null,createPanel9());
      m_TabPane.addTab("EditTokenDialog.tab.hero",null,createPanel10());
      m_mainPanel.add(m_TabPane,cc.xywh(2,3,3,1));

      m_mainPanel.add(createPanel13(),new CellConstraints(2,4,3,1,CellConstraints.RIGHT,CellConstraints.DEFAULT));
      m_mainPanel.add(createPanel14(),cc.xy(4,2));
      addFillComponents(m_mainPanel,new int[]{ 1,2,3,4,5 },new int[]{ 1,2,3,4 });
      return m_mainPanel;
   }

   public JPanel createnotesPanel()
   {
      m_notesPanel.setName("notesPanel");
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:GROW(1.0),CENTER:2PX:NONE");
      CellConstraints cc = new CellConstraints();
      m_notesPanel.setLayout(formlayout1);

      m__notes.setLineWrap(true);
      m__notes.setName("@notes");
      m__notes.setSelectionEnd(1);
      m__notes.setSelectionStart(1);
      m__notes.setText("	");
      m__notes.setWrapStyleWord(true);
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m__notes);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      m_notesPanel.add(jscrollpane1,new CellConstraints(2,3,1,1,CellConstraints.FILL,CellConstraints.FILL));

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("EditTokenDialog.tab.notes");
      m_notesPanel.add(jlabel1,cc.xy(2,2));

      addFillComponents(m_notesPanel,new int[]{ 1,2,3 },new int[]{ 1,2,3,4 });
      return m_notesPanel;
   }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:3PX:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),CENTER:2PX:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("EditTokenDialog.label.gmnotes");
      jpanel1.add(jlabel1,cc.xy(2,2));

      m__GMNotes.setLineWrap(true);
      m__GMNotes.setName("@GMNotes");
      m__GMNotes.setWrapStyleWord(true);
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m__GMNotes);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xy(2,3));

      addFillComponents(jpanel1,new int[]{ 1,2,3 },new int[]{ 1,2,3,4 });
      return jpanel1;
   }

   public JPanel createpropertiesPanel()
   {
      m_propertiesPanel.setName("propertiesPanel");
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:3PX:NONE,FILL:DEFAULT:GROW(1.0),CENTER:2PX:NONE");
      CellConstraints cc = new CellConstraints();
      m_propertiesPanel.setLayout(formlayout1);

      m_propertiesTable.setName("propertiesTable");
      m_propertiesTable.setText("propertiesTable");
      m_propertiesPanel.add(m_propertiesTable,cc.xywh(2,2,2,1));

      addFillComponents(m_propertiesPanel,new int[]{ 1,2,3,4 },new int[]{ 1,2,3 });
      return m_propertiesPanel;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,LEFT:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:20DLU:NONE,FILL:DEFAULT:NONE,FILL:32DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:1DLU:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:39PX:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:26PX:NONE,CENTER:2PX:NONE,CENTER:26PX:NONE,CENTER:2PX:NONE,CENTER:25PX:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,FILL:DEFAULT:NONE,CENTER:3PX:NONE,FILL:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_copyOrMoveCheckbox.setActionCommand("Always Show	");
      m_copyOrMoveCheckbox.setName("copyOrMoveCheckbox");
      m_copyOrMoveCheckbox.setSelected(true);
      m_copyOrMoveCheckbox.setText("EditTokenDialog.option.vbl.erase");
      m_copyOrMoveCheckbox.setToolTipText("EditTokenDialog.option.vbl.erase.tooltip");
      m_copyOrMoveCheckbox.setHorizontalAlignment(JCheckBox.CENTER);
      jpanel1.add(m_copyOrMoveCheckbox,new CellConstraints(2,24,6,1,CellConstraints.RIGHT,CellConstraints.DEFAULT));

      m_inverseVblCheckbox.setActionCommand("Hide Preview");
      m_inverseVblCheckbox.setName("inverseVblCheckbox");
      m_inverseVblCheckbox.setText("EditTokenDialog.label.vbl.invert");
      m_inverseVblCheckbox.setToolTipText("EditTokenDialog.label.vbl.invert.tooltip");
      jpanel1.add(m_inverseVblCheckbox,new CellConstraints(2,10,6,1,CellConstraints.RIGHT,CellConstraints.DEFAULT));

      m_ignoreColorLabel.setName("ignoreColorLabel");
      m_ignoreColorLabel.setText("EditTokenDialog.label.vbl.color");
      m_ignoreColorLabel.setHorizontalAlignment(JLabel.LEFT);
      jpanel1.add(m_ignoreColorLabel,cc.xy(2,6));

      m_vblIgnoreColorWell.setName("vblIgnoreColorWell");
      m_vblIgnoreColorWell.setToolTipText("EditTokenDialog.label.vbl.well");
      jpanel1.add(m_vblIgnoreColorWell,new CellConstraints(5,6,2,1,CellConstraints.FILL,CellConstraints.FILL));

      m_vblColorPickerToggleButton.setName("vblColorPickerToggleButton");
      m_vblColorPickerToggleButton.setToolTipText("EditTokenDialog.label.vbl.toggle");
      jpanel1.add(m_vblColorPickerToggleButton,cc.xy(7,6));

      m_alphaSensitivityLabel.setName("alphaSensitivityLabel");
      m_alphaSensitivityLabel.setText("EditTokenDialog.label.vbl.sensitivity");
      m_alphaSensitivityLabel.setHorizontalAlignment(JLabel.LEFT);
      jpanel1.add(m_alphaSensitivityLabel,cc.xywh(2,8,2,1));

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("EditTokenDialog.label.vbl.tolerance");
      jlabel1.setHorizontalAlignment(JLabel.LEFT);
      jpanel1.add(jlabel1,cc.xywh(2,30,4,1));

      m_alwaysVisibleButton.setActionCommand("Always Show	");
      m_alwaysVisibleButton.setName("alwaysVisibleButton");
      m_alwaysVisibleButton.setSelected(true);
      m_alwaysVisibleButton.setText("EditTokenDialog.label.vbl.over");
      m_alwaysVisibleButton.setToolTipText("EditTokenDialog.label.vbl.over.tooltip");
      m_alwaysVisibleButton.setHorizontalAlignment(JCheckBox.CENTER);
      jpanel1.add(m_alwaysVisibleButton,new CellConstraints(2,28,6,1,CellConstraints.RIGHT,CellConstraints.DEFAULT));

      m_visibilityLabel.setFont(new Font("SansSerif",Font.BOLD,12));
      m_visibilityLabel.setName("visibilityLabel");
      m_visibilityLabel.setText("EditTokenDialog.label.vbl.visiblity");
      jpanel1.add(m_visibilityLabel,new CellConstraints(2,26,6,1,CellConstraints.CENTER,CellConstraints.DEFAULT));

      m_jtsDistanceToleranceLabel.setName("jtsDistanceToleranceLabel");
      m_jtsDistanceToleranceLabel.setText("EditTokenDialog.label.vbl.level");
      m_jtsDistanceToleranceLabel.setHorizontalAlignment(JLabel.LEFT);
      jpanel1.add(m_jtsDistanceToleranceLabel,cc.xy(2,14));

      m_jtsDistanceToleranceLabel1.setName("jtsDistanceToleranceLabel");
      m_jtsDistanceToleranceLabel1.setText("EditTokenDialog.label.vbl.method");
      m_jtsDistanceToleranceLabel1.setHorizontalAlignment(JLabel.LEFT);
      jpanel1.add(m_jtsDistanceToleranceLabel1,cc.xy(2,16));

      m_optimizationLabel.setFont(new Font("SansSerif",Font.BOLD,12));
      m_optimizationLabel.setName("optimizationLabel");
      m_optimizationLabel.setText("EditTokenDialog.label.vbl.optimize");
      jpanel1.add(m_optimizationLabel,new CellConstraints(2,12,6,1,CellConstraints.CENTER,CellConstraints.DEFAULT));

      m_visibilityToleranceSpinner.setName("visibilityToleranceSpinner");
      m_visibilityToleranceSpinner.setToolTipText("EditTokenDialog.vbl.explanation.tooltip");
      jpanel1.add(m_visibilityToleranceSpinner,cc.xy(7,30));

      m_jtsMethodComboBox.setName("jtsMethodComboBox");
      m_jtsMethodComboBox.setToolTipText("EditTokenDialog.drop.vbl.optimize.tooltip");
      jpanel1.add(m_jtsMethodComboBox,cc.xywh(3,16,5,1));

      m_alphaSensitivitySpinner.setName("alphaSensitivitySpinner");
      m_alphaSensitivitySpinner.setToolTipText("EditTokenDialog.label.vbl.sensitivity.tooltip");
      jpanel1.add(m_alphaSensitivitySpinner,cc.xy(7,8));

      m_jtsDistanceToleranceSpinner.setName("jtsDistanceToleranceSpinner");
      m_jtsDistanceToleranceSpinner.setToolTipText("EditTokenDialog.spinner.tolerance.tooltip");
      jpanel1.add(m_jtsDistanceToleranceSpinner,cc.xy(7,14));

      m_hideTokenCheckbox.setActionCommand("Hide Preview");
      m_hideTokenCheckbox.setName("hideTokenCheckbox");
      m_hideTokenCheckbox.setText("EditTokenDialog.label.vbl.preview");
      m_hideTokenCheckbox.setToolTipText("EditTokenDialog.label.vbl.preview.tooltip");
      jpanel1.add(m_hideTokenCheckbox,new CellConstraints(2,4,6,1,CellConstraints.RIGHT,CellConstraints.DEFAULT));

      m_clearVblButton.setActionCommand("Clear VBL");
      m_clearVblButton.setName("clearVblButton");
      m_clearVblButton.setText("EditTokenDialog.button.vbl.clear");
      m_clearVblButton.setToolTipText("EditTokenDialog.button.vbl.clear.tooltip");
      jpanel1.add(m_clearVblButton,cc.xywh(2,18,6,1));

      m_autoGenerateVblButton.setActionCommand("Generate VBL	");
      m_autoGenerateVblButton.setName("autoGenerateVblButton");
      m_autoGenerateVblButton.setText("EditTokenDialog.button.vbl");
      m_autoGenerateVblButton.setToolTipText("EditTokenDialog.button.vbl.tooltip");
      jpanel1.add(m_autoGenerateVblButton,cc.xywh(2,5,6,1));

      m_transferVblToMap.setActionCommand("Clear VBL");
      m_transferVblToMap.setName("transferVblToMap");
      m_transferVblToMap.setText("EditTokenDialog.button.vbl.tomap");
      jpanel1.add(m_transferVblToMap,cc.xywh(2,20,6,1));

      m_transferVblFromMap.setActionCommand("Clear VBL");
      m_transferVblFromMap.setName("transferVblFromMap");
      m_transferVblFromMap.setText("EditTokenDialog.button.vbl.frommap");
      jpanel1.add(m_transferVblFromMap,cc.xywh(2,22,6,1));

      jpanel1.add(createPanel2(),cc.xywh(2,3,6,1));
      jpanel1.add(createvblPreviewPanel(),cc.xywh(9,2,4,29));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30 });
      return jpanel1;
   }

   public JPanel createPanel2()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_wallVblToggle.setName("wallVblToggle");
      jpanel1.add(m_wallVblToggle,cc.xy(1,1));

      m_hillVblToggle.setName("hillVblToggle");
      jpanel1.add(m_hillVblToggle,cc.xy(2,1));

      m_pitVblToggle.setName("pitVblToggle");
      jpanel1.add(m_pitVblToggle,cc.xy(3,1));

      m_mblToggle.setName("mblToggle");
      jpanel1.add(m_mblToggle,cc.xy(4,1));

      addFillComponents(jpanel1,new int[]{ 5 },new int[0]);
      return jpanel1;
   }

   public JPanel createvblPreviewPanel()
   {
      m_vblPreviewPanel.setName("vblPreviewPanel");
      LineBorder lineborder1 = new LineBorder(new Color(0,0,0),1,false);
      Border border1 = BorderFactory.createTitledBorder(lineborder1,"EditTokenDialog.border.title.vbl.preview",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(49,106,196));
      m_vblPreviewPanel.setBorder(border1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","FILL:DEFAULT:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      m_vblPreviewPanel.setLayout(formlayout1);

      m_vblPreview.setName("vblPreview");
      m_vblPreview.setText("EditTokenDialog.button.vbl.preview");
      m_vblPreviewPanel.add(m_vblPreview,cc.xy(1,1));

      addFillComponents(m_vblPreviewPanel,new int[0],new int[0]);
      return m_vblPreviewPanel;
   }

   public JPanel createPanel3()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:3PX:NONE,FILL:DEFAULT:GROW(1.0),CENTER:2PX:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createstatesPanel(),new CellConstraints(2,2,1,1,CellConstraints.FILL,CellConstraints.FILL));
      addFillComponents(jpanel1,new int[]{ 1,2,3 },new int[]{ 1,2,3 });
      return jpanel1;
   }

   public JScrollPane createstatesPanel()
   {
      m_statesPanel.setName("statesPanel");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m_statesPanel);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      m_statesPanel.setLayout(formlayout1);

      addFillComponents(m_statesPanel,new int[]{ 1,2,3,4,5,6 },new int[]{ 1 });
      return jscrollpane1;
   }

   public JPanel createPanel4()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:2PX:NONE,CENTER:DEFAULT:GROW(1.0),CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:3PX:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_speechTable.setName("speechTable");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m_speechTable);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      jpanel1.add(jscrollpane1,new CellConstraints(2,2,1,1,CellConstraints.FILL,CellConstraints.FILL));

      jpanel1.add(createPanel5(),cc.xy(2,4));
      addFillComponents(jpanel1,new int[]{ 1,2,3 },new int[]{ 1,2,3,4,5 });
      return jpanel1;
   }

   public JPanel createPanel5()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_speechClearAllButton.setActionCommand("Clear All");
      m_speechClearAllButton.setName("speechClearAllButton");
      m_speechClearAllButton.setText("Button.clearall");
      jpanel1.add(m_speechClearAllButton,cc.xy(1,1));

      addFillComponents(jpanel1,new int[0],new int[0]);
      return jpanel1;
   }

   public JPanel createownershipPanel()
   {
      m_ownershipPanel.setName("ownershipPanel");
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:GROW(1.0),CENTER:2PX:NONE");
      CellConstraints cc = new CellConstraints();
      m_ownershipPanel.setLayout(formlayout1);

      m__ownedByAll.setActionCommand("All Players");
      m__ownedByAll.setName("@ownedByAll");
      m__ownedByAll.setText("EditTokenDialog.label.allplayers");
      m_ownershipPanel.add(m__ownedByAll,cc.xy(2,2));

      m_ownershipList.setName("ownershipList");
      m_ownershipPanel.add(m_ownershipList,new CellConstraints(2,3,1,1,CellConstraints.FILL,CellConstraints.FILL));

      addFillComponents(m_ownershipPanel,new int[]{ 1,2,3 },new int[]{ 1,2,3,4 });
      return m_ownershipPanel;
   }

   public JPanel createPanel6()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel7(),cc.xy(1,1));
      jpanel1.add(createPanel8(),cc.xy(1,2));
      addFillComponents(jpanel1,new int[]{ 1 },new int[]{ 1,2,3 });
      return jpanel1;
   }

   public JPanel createPanel7()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:60DLU:NONE,LEFT:5DLU:NONE,FILL:DEFAULT:NONE,FILL:5DLU:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      formlayout1.setColumnGroups(new int[][]{ {16} });
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("EditTokenDialog.label.shape");
      jpanel1.add(jlabel1,cc.xy(2,2));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("EditTokenDialog.label.size");
      jpanel1.add(jlabel2,cc.xy(2,4));

      JLabel jlabel3 = new JLabel();
      jlabel3.setText("EditTokenDialog.label.properties");
      jpanel1.add(jlabel3,cc.xy(2,6));

      m_shape.setName("shape");
      jpanel1.add(m_shape,cc.xywh(4,2,3,1));

      m_size.setName("size");
      jpanel1.add(m_size,cc.xywh(4,4,3,1));

      m_propertyTypeCombo.setName("propertyTypeCombo");
      jpanel1.add(m_propertyTypeCombo,cc.xywh(4,6,3,1));

      JLabel jlabel4 = new JLabel();
      jlabel4.setText("EditTokenDialog.label.sight.has");
      jpanel1.add(jlabel4,cc.xy(2,8));

      m__hasSight.setName("@hasSight");
      jpanel1.add(m__hasSight,cc.xy(4,8));

      m_sightTypeCombo.setName("sightTypeCombo");
      jpanel1.add(m_sightTypeCombo,cc.xy(6,8));

      JLabel jlabel5 = new JLabel();
      jlabel5.setText("EditTokenDialog.label.image");
      jpanel1.add(jlabel5,cc.xy(2,10));

      m__hasImageTable.setName("@hasImageTable");
      jpanel1.add(m__hasImageTable,cc.xy(4,10));

      m_imageTableCombo.setName("imageTableCombo");
      jpanel1.add(m_imageTableCombo,cc.xy(6,10));

      JLabel jlabel6 = new JLabel();
      jlabel6.setText("EditTokenDialog.label.snaptogrid");
      jpanel1.add(jlabel6,cc.xy(8,2));

      m__snapToGrid.setName("@snapToGrid");
      jpanel1.add(m__snapToGrid,cc.xy(10,2));

      m_visibleLabel.setName("visibleLabel");
      m_visibleLabel.setText("EditTokenDialog.label.visible");
      jpanel1.add(m_visibleLabel,cc.xy(12,2));

      m__visible.setName("@visible");
      jpanel1.add(m__visible,cc.xy(14,2));

      m_visibleOnlyToOwnerLabel.setName("visibleOnlyToOwnerLabel");
      m_visibleOnlyToOwnerLabel.setText("EditTokenDialog.label.visible.owner");
      jpanel1.add(m_visibleOnlyToOwnerLabel,cc.xy(8,4));

      m__visibleOnlyToOwner.setName("@visibleOnlyToOwner");
      jpanel1.add(m__visibleOnlyToOwner,cc.xy(10,4));

      m_terrainModifierLabel.setName("terrainModifierLabel");
      m_terrainModifierLabel.setText("EditTokenDialog.label.terrain.mod");
      m_terrainModifierLabel.setToolTipText("EditTokenDialog.label.terrain.mod.tooltip");
      jpanel1.add(m_terrainModifierLabel,cc.xy(12,4));

      m_terrainModifierOperation.setName("terrainModifierOperation");
      m_terrainModifierOperation.setToolTipText("EditTokenDialog.combo.terrain.mod");
      m_terrainModifierOperation.addItem("Token.TerrainModifierOperation.MULTIPLY");
      jpanel1.add(m_terrainModifierOperation,cc.xy(14,4));

      m_terrainModifier.setColumns(4);
      m_terrainModifier.setName("terrainModifier");
      m_terrainModifier.setToolTipText("EditTokenDialog.label.terrain.mod.tooltip");
      jpanel1.add(m_terrainModifier,cc.xy(15,4));

      m_tokenOpacityLabel.setName("tokenOpacityLabel");
      m_tokenOpacityLabel.setText("EditTokenDialog.label.opacity");
      m_tokenOpacityLabel.setToolTipText("EditTokenDialog.label.opacity.tooltip");
      jpanel1.add(m_tokenOpacityLabel,cc.xy(8,6));

      m_tokenOpacitySlider.setMajorTickSpacing(20);
      m_tokenOpacitySlider.setMinimum(5);
      m_tokenOpacitySlider.setMinorTickSpacing(5);
      m_tokenOpacitySlider.setName("tokenOpacitySlider");
      m_tokenOpacitySlider.setPaintTicks(true);
      m_tokenOpacitySlider.setSnapToTicks(true);
      m_tokenOpacitySlider.setValue(100);
      m_tokenOpacitySlider.setValueIsAdjusting(true);
      m_tokenOpacitySlider.setOrientation(JSlider.VERTICAL);
      jpanel1.add(m_tokenOpacitySlider,cc.xywh(9,6,1,5));

      m_tokenOpacityValueLabel.setName("tokenOpacityValueLabel");
      m_tokenOpacityValueLabel.setText("EditTokenDialog.label.opacity.100");
      jpanel1.add(m_tokenOpacityValueLabel,cc.xy(10,6));

      m_ignoreTerrainModifierLabel.setName("ignoreTerrainModifierLabel");
      m_ignoreTerrainModifierLabel.setText("EditTokenDialog.label.terrain.ignore");
      m_ignoreTerrainModifierLabel.setToolTipText("EditTokenDialog.label.terrain.ignore.tooltip");
      jpanel1.add(m_ignoreTerrainModifierLabel,cc.xy(12,6));

      m_terrainModifiersIgnored.setName("terrainModifiersIgnored");
      m_terrainModifiersIgnored.setToolTipText("EditTokenDialog.label.terrain.ignore.tooltip");
      m_terrainModifiersIgnored.setVisibleRowCount(0);
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m_terrainModifiersIgnored);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(14,6,1,5));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20 },new int[]{ 1,2,3,4,5,6,7,8,9,10 });
      return jpanel1;
   }

   public JPanel createPanel8()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createcharsheetPanel(),cc.xy(5,1));
      jpanel1.add(createtokenLayoutPanel(),cc.xy(1,1));
      jpanel1.add(createportraitPanel(),cc.xy(3,1));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6 },new int[]{ 1 });
      return jpanel1;
   }

   public JPanel createcharsheetPanel()
   {
      m_charsheetPanel.setName("charsheetPanel");
      LineBorder lineborder1 = new LineBorder(new Color(0,0,0),1,false);
      Border border1 = BorderFactory.createTitledBorder(lineborder1,"EditTokenDialog.border.title.handout",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(49,106,196));
      m_charsheetPanel.setBorder(border1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      m_charsheetPanel.setLayout(formlayout1);

      m_charsheet.setName("charsheet");
      m_charsheet.setText("EditTokenDialog.border.title.charsheet");
      m_charsheetPanel.add(m_charsheet,cc.xy(1,1));

      addFillComponents(m_charsheetPanel,new int[0],new int[0]);
      return m_charsheetPanel;
   }

   public JPanel createtokenLayoutPanel()
   {
      m_tokenLayoutPanel.setName("tokenLayoutPanel");
      LineBorder lineborder1 = new LineBorder(new Color(0,0,0),1,false);
      Border border1 = BorderFactory.createTitledBorder(lineborder1,"EditTokenDialog.border.title.layout",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(49,106,196));
      m_tokenLayoutPanel.setBorder(border1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      m_tokenLayoutPanel.setLayout(formlayout1);

      m_tokenLayout.setName("tokenLayout");
      m_tokenLayout.setText("EditTokenDialog.border.title.layout");
      m_tokenLayoutPanel.add(m_tokenLayout,cc.xy(1,1));

      addFillComponents(m_tokenLayoutPanel,new int[0],new int[0]);
      return m_tokenLayoutPanel;
   }

   public JPanel createportraitPanel()
   {
      m_portraitPanel.setName("portraitPanel");
      LineBorder lineborder1 = new LineBorder(new Color(0,0,0),1,false);
      Border border1 = BorderFactory.createTitledBorder(lineborder1,"EditTokenDialog.border.title.portrait",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(49,106,196));
      m_portraitPanel.setBorder(border1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      m_portraitPanel.setLayout(formlayout1);

      m_portrait.setName("portrait");
      m_portrait.setText("EditTokenDialog.border.title.portrait");
      m_portraitPanel.add(m_portrait,cc.xy(1,1));

      addFillComponents(m_portraitPanel,new int[0],new int[0]);
      return m_portraitPanel;
   }

   public JPanel createPanel9()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Label.allowURIAccess");
      jpanel1.add(jlabel1,cc.xy(1,1));

      m_Label_LibURIError.setName("Label.LibURIError");
      jpanel1.add(m_Label_LibURIError,cc.xywh(1,3,67,1));

      m__allowURIAccess.setName("@allowURIAccess");
      jpanel1.add(m__allowURIAccess,cc.xy(3,1));

      addFillComponents(jpanel1,new int[]{ 2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67 },new int[]{ 2 });
      return jpanel1;
   }

   public JPanel createPanel10()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,LEFT:DEFAULT:NONE,FILL:5DLU:NONE,LEFT:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,BOTTOM:DEFAULT:NONE,CENTER:2PX:NONE,FILL:DEFAULT:GROW(1.0),CENTER:3PX:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_summaryLabel.setName("summaryLabel");
      m_summaryLabel.setText("EditTokenDialog.label.hero.summary");
      jpanel1.add(m_summaryLabel,cc.xy(2,2));

      m_StatblockTabPane.setName("StatblockTabPane");
      m_StatblockTabPane.setToolTipText("View the statblocks from Hero Lab");
      m_StatblockTabPane.addTab("EditTokenDialog.tab.hero.html",null,createPanel11());
      m_StatblockTabPane.addTab("EditTokenDialog.tab.hero.xml",null,createxmlStatblockPanel());
      m_StatblockTabPane.addTab("EditTokenDialog.tab.hero.text",null,createtextStatblockPanel());
      m_StatblockTabPane.addTab("EditTokenDialog.tab.hero.images",null,createPanel12());
      jpanel1.add(m_StatblockTabPane,cc.xywh(2,8,5,1));

      m_portfolioLabel.setName("portfolioLabel");
      m_portfolioLabel.setText("EditTokenDialog.label.hero.portfolio");
      jpanel1.add(m_portfolioLabel,cc.xy(2,4));

      m_summaryText.setName("summaryText");
      jpanel1.add(m_summaryText,new CellConstraints(4,2,1,1,CellConstraints.FILL,CellConstraints.DEFAULT));

      m_portfolioLocation.setAutoscrolls(true);
      m_portfolioLocation.setName("portfolioLocation");
      jpanel1.add(m_portfolioLocation,new CellConstraints(4,4,1,1,CellConstraints.LEFT,CellConstraints.DEFAULT));

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("EditTokenDialog.label.hero.last");
      jpanel1.add(jlabel1,cc.xy(2,6));

      m_lastModified.setName("lastModified");
      jpanel1.add(m_lastModified,cc.xy(4,6));

      m_isAllyCheckBox.setActionCommand("Is Ally?");
      m_isAllyCheckBox.setEnabled(false);
      m_isAllyCheckBox.setName("isAllyCheckBox");
      m_isAllyCheckBox.setText("EditTokenDialog.label.hero.isAlly");
      jpanel1.add(m_isAllyCheckBox,new CellConstraints(6,6,1,1,CellConstraints.CENTER,CellConstraints.DEFAULT));

      m_refreshDataButton.setAlignmentY(0.0f);
      m_refreshDataButton.setIconTextGap(2);
      m_refreshDataButton.setName("refreshDataButton");
      m_refreshDataButton.setToolTipText("EditTokenDialog.button.hero.refresh.tooltip.off");
      jpanel1.add(m_refreshDataButton,cc.xywh(6,2,1,3));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7 },new int[]{ 1,2,3,4,5,6,7,8,9 });
      return jpanel1;
   }

   public JPanel createPanel11()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_HTMLstatblockTextArea.setEditable(false);
      m_HTMLstatblockTextArea.setName("HTMLstatblockTextArea");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m_HTMLstatblockTextArea);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,new CellConstraints(1,1,1,1,CellConstraints.FILL,CellConstraints.FILL));

      addFillComponents(jpanel1,new int[0],new int[0]);
      return jpanel1;
   }

   public JPanel createxmlStatblockPanel()
   {
      m_xmlStatblockPanel.setName("xmlStatblockPanel");
      FormLayout formlayout1 = new FormLayout("FILL:5DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:5DLU:NONE,FILL:DEFAULT:NONE,FILL:5DLU:NONE","FILL:DEFAULT:GROW(1.0),CENTER:3DLU:NONE,CENTER:DEFAULT:NONE,CENTER:3DLU:NONE");
      CellConstraints cc = new CellConstraints();
      m_xmlStatblockPanel.setLayout(formlayout1);

      m_expressionLabel.setName("expressionLabel");
      m_expressionLabel.setText("EditTokenDialog.label.hero.statBlockSearch");
      m_xmlStatblockPanel.add(m_expressionLabel,cc.xy(2,3));

      m_xmlStatblockSearchTextField.setName("xmlStatblockSearchTextField");
      m_xmlStatblockSearchTextField.setToolTipText("EditTokenDialog.label.hero.statBlockSearch.tooltip");
      m_xmlStatblockPanel.add(m_xmlStatblockSearchTextField,cc.xy(3,3));

      m_xmlStatblockSearchButton.setActionCommand("Find");
      m_xmlStatblockSearchButton.setName("xmlStatblockSearchButton");
      m_xmlStatblockSearchButton.setText("EditTokenDialog.action.hero.statBlockRTextScroll");
      m_xmlStatblockSearchButton.setToolTipText("EditTokenDialog.button.hero.statBlockSearch.tooltip");
      m_xmlStatblockPanel.add(m_xmlStatblockSearchButton,cc.xy(5,3));

      m_xmlStatblockRTextScrollPane.setName("xmlStatblockRTextScrollPane");
      m_xmlStatblockPanel.add(m_xmlStatblockRTextScrollPane,cc.xywh(2,1,4,1));

      addFillComponents(m_xmlStatblockPanel,new int[]{ 1,3,4,5,6 },new int[]{ 1,2,3,4 });
      return m_xmlStatblockPanel;
   }

   public JPanel createtextStatblockPanel()
   {
      m_textStatblockPanel.setName("textStatblockPanel");
      FormLayout formlayout1 = new FormLayout("FILL:5DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:5DLU:NONE,FILL:DEFAULT:NONE,FILL:5DLU:NONE","FILL:DEFAULT:GROW(1.0),CENTER:3DLU:NONE,CENTER:DEFAULT:NONE,CENTER:3DLU:NONE");
      CellConstraints cc = new CellConstraints();
      m_textStatblockPanel.setLayout(formlayout1);

      m_textStatblockSearchButton.setActionCommand("Find");
      m_textStatblockSearchButton.setName("textStatblockSearchButton");
      m_textStatblockSearchButton.setText("EditTokenDialog.action.hero.statBlockRTextScroll");
      m_textStatblockSearchButton.setToolTipText("EditTokenDialog.button.hero.statBlockSearch.tooltip");
      m_textStatblockPanel.add(m_textStatblockSearchButton,cc.xy(5,3));

      m_expressionLabel1.setName("expressionLabel");
      m_expressionLabel1.setText("EditTokenDialog.label.hero.statBlockSearch");
      m_textStatblockPanel.add(m_expressionLabel1,cc.xy(2,3));

      m_textStatblockSearchTextField.setName("textStatblockSearchTextField");
      m_textStatblockSearchTextField.setToolTipText("EditTokenDialog.label.hero.statBlockSearch.tooltip");
      m_textStatblockPanel.add(m_textStatblockSearchTextField,cc.xy(3,3));

      m_textStatblockRTextScrollPane.setName("textStatblockRTextScrollPane");
      m_textStatblockPanel.add(m_textStatblockRTextScrollPane,cc.xywh(2,1,4,1));

      addFillComponents(m_textStatblockPanel,new int[]{ 1,3,4,5,6 },new int[]{ 1,2,3,4 });
      return m_textStatblockPanel;
   }

   public JPanel createPanel12()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_setAsHandoutButton.setActionCommand("Set as Token Handout");
      m_setAsHandoutButton.setName("setAsHandoutButton");
      m_setAsHandoutButton.setText("EditTokenDialog.button.hero.setAsTokenHandout");
      jpanel1.add(m_setAsHandoutButton,cc.xy(2,7));

      m_heroLabImagesList.setName("heroLabImagesList");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(m_heroLabImagesList);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(4,2,1,7));

      m_setAsImageButton.setActionCommand("Set as Token Image");
      m_setAsImageButton.setName("setAsImageButton");
      m_setAsImageButton.setText("EditTokenDialog.button.hero.setAsTokenImage");
      jpanel1.add(m_setAsImageButton,cc.xy(2,5));

      m_setAsPortraitButton.setActionCommand("Set as Token Portrait");
      m_setAsPortraitButton.setName("setAsPortraitButton");
      m_setAsPortraitButton.setText("EditTokenDialog.button.hero.setAsTokenPortrait");
      jpanel1.add(m_setAsPortraitButton,cc.xy(2,3));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5 },new int[]{ 1,2,3,4,5,6,7,8,9 });
      return jpanel1;
   }

   public JPanel createPanel13()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:8DLU:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      m_cancelButton.setActionCommand("Cancel");
      m_cancelButton.setName("cancelButton");
      m_cancelButton.setText("Button.cancel");
      jpanel1.add(m_cancelButton,cc.xy(3,1));

      m_okButton.setActionCommand("OK");
      m_okButton.setName("okButton");
      m_okButton.setText("Button.ok");
      jpanel1.add(m_okButton,cc.xy(1,1));

      addFillComponents(jpanel1,new int[]{ 2 },new int[0]);
      return jpanel1;
   }

   public JPanel createPanel14()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE,CENTER:2PX:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Label.name");
      jpanel1.add(jlabel1,cc.xy(1,1));

      m__name.setColumns(30);
      m__name.setFont(new Font("SansSerif",Font.BOLD,12));
      m__name.setName("@name");
      jpanel1.add(m__name,cc.xy(3,1));

      m_tokenGMNameLabel.setName("tokenGMNameLabel");
      m_tokenGMNameLabel.setText("Label.gmname");
      jpanel1.add(m_tokenGMNameLabel,cc.xy(1,3));

      m__GMName.setColumns(30);
      m__GMName.setFont(new Font("SansSerif",Font.BOLD,12));
      m__GMName.setName("@GMName");
      jpanel1.add(m__GMName,cc.xy(3,3));

      m_type.setName("type");
      jpanel1.add(m_type,cc.xy(5,1));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("EditTokenDialog.label.label");
      jpanel1.add(jlabel2,cc.xy(1,5));

      m__label.setName("@label");
      jpanel1.add(m__label,cc.xy(3,5));

      JLabel jlabel3 = new JLabel();
      jlabel3.setText("Label.speechName");
      jpanel1.add(jlabel3,cc.xy(1,7));

      m__speechName.setName("@speechName");
      jpanel1.add(m__speechName,cc.xy(3,7));

      addFillComponents(jpanel1,new int[]{ 2,4 },new int[]{ 2,4,6 });
      return jpanel1;
   }

   /**
    * Initializer
    */
   protected void initializePanel()
   {
      setLayout(new BorderLayout());
      add(createmainPanel(), BorderLayout.CENTER);
   }


}
