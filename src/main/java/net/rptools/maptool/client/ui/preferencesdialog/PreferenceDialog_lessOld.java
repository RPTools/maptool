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
package net.rptools.maptool.client.ui.preferencesdialog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import com.formdev.flatlaf.extras.components.FlatTree;
import com.jidesoft.swing.event.SearchableEvent;
import com.jidesoft.swing.event.SearchableListener;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.GenericDialogFactory;
import net.rptools.maptool.client.swing.searchable.SearchableBarEx;
import net.rptools.maptool.client.swing.searchable.SearchableEx;
import net.rptools.maptool.client.swing.searchable.SearchableTree;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.GraphicsUtil;
import net.rptools.maptool.util.preferences.Preference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Class: PreferenceDialog
 *
 * <p>A dialog box to manage user preferences.
 */
public class PreferenceDialog_lessOld {//} extends AbeillePanel<Object> {
//    /**
//     * Logger instance used for logging messages in the PreferenceDialog class.
//     */
//    private static final Logger log = LogManager.getLogger(PreferenceDialog_lessOld.class);
//
//    private static final PreferenceDialog_lessOld instance;
//
//    public static PreferenceDialog_lessOld getInstance() {
//        return instance;
//    }
//
//    protected static final List<Preference<?>> ENUM_PREFS =
//            List.of(
//                    AppPreferences.defaultGridType,
//                    AppPreferences.numberTokenDuplicateMethod,
//                    AppPreferences.showTokenNumberOn,
//                    AppPreferences.newTokenName,
//                    AppPreferences.renderQuality,
//                    AppPreferences.mapSortType,
//                    AppPreferences.uvttLosImportType,
//                    AppPreferences.movementMetric,
//                    AppPreferences.UIIcons,
//                    AppPreferences.defaultVisionType);
//
//
//    /**
//     * Listener specific to toggling enabled state of token border related controls
//     */
//    private static final ActionListener labelBorderListener =
//            e -> {
//                if (((JCheckBox) e.getSource()).isSelected()) {
//                    Objects.requireNonNull(PrefParts.findComponent("pcMapLabelBorderColor")).setEnabled(true);
//                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderColor")).setEnabled(true);
//                    Objects.requireNonNull(PrefParts.findComponent("nonVisMapLabelBorderColor")).setEnabled(true);
//                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderWidth")).setEnabled(true);
//                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderArc")).setEnabled(true);
//                } else {
//                    Objects.requireNonNull(PrefParts.findComponent("pcMapLabelBorderColor")).setEnabled(false);
//                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderColor")).setEnabled(false);
//                    Objects.requireNonNull(PrefParts.findComponent("nonVisMapLabelBorderColor")).setEnabled(false);
//                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderWidth")).setEnabled(false);
//                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderArc")).setEnabled(false);
//                }
//            };
//
//
//    /**
//     * my version of percentile screen width
//     */
//    private static final int vw =
//            (int)
//                    Math.max(
//                            GraphicsEnvironment.getLocalGraphicsEnvironment()
//                                    .getDefaultScreenDevice()
//                                    .getDefaultConfiguration()
//                                    .getBounds()
//                                    .getWidth()
//                                    / 100,
//                            8);
//
//    /**
//     * Because we want to, you know, show this to people
//     */
//    private static final GenericDialogFactory dialogFactory =
//            GenericDialog.getFactory()
//                    .setDialogTitle(I18N.getString("Label.preferences"))
//                    .makeModal(true)
//                    .setContent(PrefParts.PREF_PANE)
//                    .setNavPane(new JScrollPane(PrefParts.NAV_PANEL))
//                    .setToolbar(PrefParts.SEARCH_PANE)
//                    .setSideBar(new JScrollPane(PrefParts.SIDE_PANE));
//
//    private static final JTreeTable treeTable;
//    private static final FlatTree prefTree;
//    private static final SearchableEx SEARCHABLE_TREE;
//    private static final SearchableBarEx SEARCHABLE_BAR;
//
//    static {
//        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
//
//        final Color CLEAR = new Color(1f, 1f, 1f, 0);
//        PrefParts.initLayoutAndConstraints();
//        final Map<Classify.Section, List<Classify.Collated>> sectionMembers =
//                Arrays.stream(Classify.Collated.values())
//                        .sorted(Comparator.comparing(collated -> Classify.Collated.valueOf(collated.name()).ordinal()))
//                        .collect(Collectors.groupingBy(collated -> collated.section));
//        int sectionRow = 0;
//        for (Classify.Section sect : Classify.Section.values()) {
//            final List<Classify.Collated> members = sectionMembers.get(sect);
//            if (members == null) {
//                continue;
//            }
//            DefaultMutableTreeNode sectNode = new DefaultMutableTreeNode(sect, true);
//            root.add(sectNode);
//            JPanel sectionPanel = new JPanel(new GridBagLayout());
//            PrefParts.CONSTRAINTS.get(PrefParts.Archetype.SECTION).gridy = sectionRow;
//            PrefParts.PREF_PANE.add(sectionPanel, PrefParts.CONSTRAINTS.get(PrefParts.Archetype.SECTION));
//            sectionRow++;
//            sectionPanel.setBorder(
//                    BorderFactory.createCompoundBorder(
//                            BorderFactory.createTitledBorder(
//                                    BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Window.border")),
//                                    sect.displayName),
//                            BorderFactory.createEmptyBorder(6, 12, 4, 8)
//                    ));
//            final Map<Classify.Group, List<Classify.Collated>> memberGroups = members.stream().sorted(Comparator.comparing(collated -> Classify.Collated.valueOf(collated.name()).ordinal())).collect(Collectors.groupingBy(collated -> collated.group));
//
//            int groupRow = 0;
//            for (Classify.Group g : memberGroups.keySet()) {
//                DefaultMutableTreeNode groupNode;
//                if (g.equals(Classify.Group.NONE)) {
//                    groupNode = sectNode;
//                } else {
//                    groupNode = new DefaultMutableTreeNode(g, true);
//                }
//                JPanel groupPanel = new JPanel(new GridBagLayout());
//                PrefParts.CONSTRAINTS.get(PrefParts.Archetype.GROUP).gridy = groupRow;
//                groupRow++;
//                sectionPanel.add(groupPanel, PrefParts.CONSTRAINTS.get(PrefParts.Archetype.GROUP));
//                groupPanel.setBorder(
//                        BorderFactory.createCompoundBorder(
//                                BorderFactory.createTitledBorder(
//                                        g.equals(Classify.Group.NONE) ?
//                                                BorderFactory.createLineBorder(CLEAR) :
//                                                BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Window.border"))
//                                        ,
//                                        g.equals(Classify.Group.NONE) ? null : g.displayName),
//                                BorderFactory.createEmptyBorder(6, 8, 4, 12)
//                        ));
//                int controlRow = 0;
//                for (Classify.Collated collated : memberGroups.get(g)) {
//                    Component[] components = PrefParts.createComponentsFor(collated);
//                    PrefParts.CONSTRAINTS.get(PrefParts.Archetype.LABEL).gridy = controlRow;
//                    PrefParts.CONSTRAINTS.get(PrefParts.Archetype.CONTROL).gridy = controlRow;
//                    PrefParts.CONSTRAINTS.get(PrefParts.Archetype.FAVOURITE).gridy = controlRow;
//                    try {
//                        groupPanel.add(components[0], PrefParts.CONSTRAINTS.get(PrefParts.Archetype.LABEL));
//                        groupPanel.add(components[1], PrefParts.CONSTRAINTS.get(PrefParts.Archetype.CONTROL));
//                        groupPanel.add(new JCheckBox(), PrefParts.CONSTRAINTS.get(PrefParts.Archetype.FAVOURITE));
//                        DefaultMutableTreeNode prefNode = new DefaultMutableTreeNode(collated);
//
//                        groupNode.add(prefNode);
//                    } catch (Exception e) {
//                        log.error(e);
//                    }
//                    controlRow++;
//                }
//                if (!groupNode.equals(sectNode)) {
//                    sectNode.add(groupNode);
//                }
//            }
//        }
//        TreeModel treeModel = new DefaultTreeModel(root);
//        prefTree = new PreferenceTree(treeModel);
//        prefTree.setRootVisible(true);
//
//        TreeTableModel treeTableModel = DefaultTreeTableModelII.createDefaultTreeTableModelII(root);
//        treeTable = new JTreeTable(treeTableModel);
////        treeTable.updateUI();
//
//
//        prefTree.putClientProperty("JTree.lineStyle", "Angled");
////        prefTree.setCellRenderer(new StyledTreeCellRenderer());
//
//        SEARCHABLE_TREE = new SearchableTree(prefTree);
//        SEARCHABLE_TREE.addSearchableListener(getSearchableListener());
//        SEARCHABLE_TREE.setWildcardEnabled(true);
//        SEARCHABLE_TREE.setCaseSensitive(false);
//        SEARCHABLE_TREE.setProcessModelChangeEvent(true);
//        SEARCHABLE_TREE.setSearchLabel("WHat");
//        SEARCHABLE_TREE.setRepeats(true);
//
//        SEARCHABLE_BAR = new SearchableBarEx(SEARCHABLE_TREE, true);
//        SEARCHABLE_BAR.setHighlightAll(true);
//        SEARCHABLE_BAR.setCompact(true);
//        SEARCHABLE_BAR.setVisibleButtons(SearchableBarEx.SHOW_NAVIGATION
//                + SearchableBarEx.SHOW_HIGHLIGHTS
//                + SearchableBarEx.SHOW_REPEATS
//                + SearchableBarEx.SHOW_STATUS
//                + SearchableBarEx.SHOW_WHOLE_WORDS
//                + SearchableBarEx.SHOW_MATCHCASE
//                + SearchableBarEx.SHOW_REGEX
//        );
//        SEARCHABLE_BAR.addPresetSearchValues(Arrays.stream(Classify.Section.values()).collect(Collectors.toSet()));
//        PrefParts.NAV_PANEL.add(prefTree);
//
//        instance = new PreferenceDialog_lessOld();
//    }
//
//    /**
//     * The PreferenceDialog class represents a dialog window that allows users to customize their
//     * preferences.
//     *
//     * <p>This dialog window is modal.
//     */
//    public PreferenceDialog_lessOld() {
//        super(new PreferencesEditorDialog().getRootComponent());
//        dialogFactory
//                .setCloseOperation(WindowConstants.HIDE_ON_CLOSE)
//                .addButton(ButtonKind.CLOSE)
//                .setDefaultButton(ButtonKind.CLOSE);
//
//        JCheckBox labelBorderCheckbox =
//                ((JCheckBox) PrefParts.findComponent(AppPreferences.mapLabelShowBorder.getKey()));
//        Objects.requireNonNull(labelBorderCheckbox).addActionListener(labelBorderListener);
//        labelBorderCheckbox.setSelected(AppPreferences.mapLabelShowBorder.get());
//        Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderColor"))
//                .setEnabled(labelBorderCheckbox.isSelected());
//        Objects.requireNonNull(PrefParts.findComponent("pcMapLabelBorderColor"))
//                .setEnabled(labelBorderCheckbox.isSelected());
//        Objects.requireNonNull(PrefParts.findComponent("nonVisMapLabelBorderColor"))
//                .setEnabled(labelBorderCheckbox.isSelected());
//        Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderWidth"))
//                .setEnabled(labelBorderCheckbox.isSelected());
//        Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderArc"))
//                .setEnabled(labelBorderCheckbox.isSelected());
//
//        setInitialState();
//
//        //    copyPublicKey.addActionListener(
//        //        e -> {
//        //          Toolkit.getDefaultToolkit()
//        //              .getSystemClipboard()
//        ////              .setContents(new StringSelection(publicKeyTextArea.getText()), null);
//        //        });
//        //
//        //    regeneratePublicKey.addActionListener(
//        //        e -> {
//        //          CompletableFuture<CipherUtil.Key> keys = new
//        // PublicPrivateKeyStore().regenerateKeys();
//        //
//        //          keys.thenAccept(
//        //              cu -> {
//        //                SwingUtilities.invokeLater(
//        //                    () -> {
//        ////                      publicKeyTextArea.setText(cu.getEncodedPublicKeyText());
//        //                    });
//        //              });
//        //        });
//    }
//
//
//    public void showDialog() {
//        dialogFactory.display();
//    }
//
//    /**
//     * Initializes and sets the initial state of various user preferences in the application. This
//     * method is called during the initialization process.
//     */
//    private void setInitialState() {
//        CompletableFuture<CipherUtil.Key> keys = new PublicPrivateKeyStore().getKeys();
//
//        keys.thenAccept(
//                cu -> {
//                    SwingUtilities.invokeLater(
//                            () -> {
//                                //                  publicKeyTextArea.setText(cu.getEncodedPublicKeyText());
//                            });
//                });
//    }
//
//    static {
//        AbstractButton collapseButton = new FlatToggleButton();
//        collapseButton.setIcon(RessourceManager.getSmallIcon(Icons.PROPERTIES_TABLE_COLLAPSE));
//        collapseButton.addActionListener(e -> {
//            for (int i = 0; i < prefTree.getRowCount(); i++) {
//                treeTable.getTree().collapseRow(i);
//            }
//        });
//        AbstractButton expandButton = new FlatToggleButton();
//        expandButton.setIcon(RessourceManager.getSmallIcon(Icons.PROPERTIES_TABLE_EXPAND));
//        expandButton.addActionListener(e -> {
//            for (int i = 0; i < prefTree.getRowCount(); i++) {
//                treeTable.getTree().expandRow(i);
//            }
//        });
//        PrefParts.NAV_PANEL.add(collapseButton, 0);
//        PrefParts.NAV_PANEL.add(expandButton, 0);
//
//        PrefParts.SEARCH_PANE.add(SEARCHABLE_BAR);
//    }
//
//    private static SearchableListener getSearchableListener() {
//        return new SearchableListener() {
//            @Override
//            public void searchableEventFired(SearchableEvent searchableEvent) {
//                if (searchableEvent.getID() == SearchableEvent.SEARCHABLE_MATCH) {
//                    Object o = searchableEvent.getMatchingObject();
//                    if (o instanceof Component[] components) {
//                        Rectangle r = components[1].getBounds();
//                        System.out.println(r);
//                        Point p = PrefParts.findComponent(components[1].getName()).getLocation();
//                        System.out.println(p);
//                        PrefParts.PREF_PANE.scrollRectToVisible(r);
//                    }
//                } else {
//                    log.info(searchableEvent.paramString());
//                }
//                log.info(searchableEvent);
//            }
//        };
//    }
//
//    private static class PrefTreeCellRenderer extends DefaultTreeCellRenderer {
//        /**
//         * Icon used to show leaf nodes.
//         */
//        protected transient Icon leafIcon = RessourceManager.getSmallIcon(Icons.TREE_LEAF);
//
//        private static final FlatSVGIcon.ColorFilter NEGATIVE_FILTER = new FlatSVGIcon.ColorFilter().addAll(GraphicsUtil.INVERTED_ICON_COLOURS);
//
//        @Override
//        public Icon getLeafIcon() {
//            return leafIcon;
//        }
//
//        @Override
//        public void setLeafIcon(Icon leafIcon) {
//        }
//
//        /**
//         * Configures the renderer based on the passed in components.
//         * The value is set from messaging the tree with
//         * <code>convertValueToText</code>, which ultimately invokes
//         * <code>toString</code> on <code>value</code>.
//         * The foreground color is set based on the selection and the icon
//         * is set based on the <code>leaf</code> and <code>expanded</code>
//         * parameters.
//         */
//        public Component getTreeCellRendererComponent(JTree tree, Object value,
//                                                      boolean sel,
//                                                      boolean expanded,
//                                                      boolean leaf, int row,
//                                                      boolean hasFocus) {
//            PreferenceTree pTree = (PreferenceTree) tree;
//            DefaultMutableTreeNode node = null;
//            if (value != null) {
//                node = (DefaultMutableTreeNode) value;
//            }
//            String stringValue = pTree.convertValueToText(node == null ? value : node, sel, expanded, leaf, row, hasFocus);
//            String tipText = pTree.convertValueToTooltipText(node == null ? value : node, sel, expanded, leaf, row, hasFocus);
//            this.hasFocus = hasFocus;
//            setText(stringValue);
//            setToolTipText(tipText);
//            Color fg = null;
////            isDropCell = false;
//
//            JTree.DropLocation dropLocation = tree.getDropLocation();
//            if (dropLocation != null
//                    && dropLocation.getChildIndex() == -1
//                    && tree.getRowForPath(dropLocation.getPath()) == row) {
////
////                Color col = DefaultLookup.getColor(this, ui, "Tree.dropCellForeground");
////                if (col != null) {
////                    fg = col;
////                } else {
////                    fg = getTextSelectionColor();
////                }
//
////                isDropCell = true;
//            } else if (sel) {
//                fg = getTextSelectionColor();
//            } else {
//                fg = getTextNonSelectionColor();
//            }
//
//            setForeground(fg);
//
//            Icon icon = null;
//            if (leaf) {
//                icon = leafIcon;
//            }
//
//            if (!tree.isEnabled()) {
//                setEnabled(false);
//                LookAndFeel laf = UIManager.getLookAndFeel();
//                Icon disabledIcon = sel ? laf.getDisabledSelectedIcon(tree, icon) : laf.getDisabledIcon(tree, icon);
//                if (disabledIcon != null) icon = disabledIcon;
//                setDisabledIcon(icon);
//            } else {
//                setEnabled(true);
//                if (icon instanceof FlatSVGIcon svgIcon) {
//                    svgIcon.setColorFilter(!sel ? null : NEGATIVE_FILTER);
//                }
//                setIcon(icon);
//            }
//            setComponentOrientation(tree.getComponentOrientation());
//            selected = sel;
//            return this;
//        }
//    }
//
//    private static class PreferenceTree extends FlatTree {
//        protected transient PrefTreeCellRenderer cellRenderer = new PrefTreeCellRenderer();
//
//        PreferenceTree(TreeModel model) {
//            super();
//            super.setModel(model);
//            super.setCellRenderer(cellRenderer);
//            super.setRootVisible(false);
//            super.setScrollsOnExpand(true);
//            super.setShowsRootHandles(true);
//            super.setWideCellRenderer(true);
//        }
//
//        /**
//         * Called by the renderers to convert the specified value to
//         * text. This implementation returns <code>value.toString</code>, ignoring
//         * all other arguments. To control the conversion, subclass this
//         * method and use any of the arguments you need.
//         *
//         * @param value    the <code>Object</code> to convert to text
//         * @param selected true if the node is selected
//         * @param expanded true if the node is expanded
//         * @param leaf     true if the node is a leaf node
//         * @param row      an integer specifying the node's display row, where 0 is
//         *                 the first row in the display
//         * @param hasFocus true if the node has the focus
//         * @return the <code>String</code> representation of the node's value
//         */
//        @Override
//        public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//            if (value instanceof DefaultMutableTreeNode node) {
//                Object o = node.getUserObject();
//                if (o instanceof LocalisedItem localisedItem) {
//                    return localisedItem.toString();
//                } else if (o instanceof Classify.Collated collated) {
//                    if (collated.preference != null) {
//                        return collated.preference.getLabel();
//                    } else {
//                        return collated.id;
//                    }
//                }
//            }
//            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
//        }
//
//        public String convertValueToTooltipText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//            if (value instanceof DefaultMutableTreeNode node) {
//                Object o = node.getUserObject();
//                if (o instanceof Classify.Collated collated) {
//                    if (collated.preference != null) {
//                        return collated.preference.getTooltip();
//                    } else {
//                        return collated.id;
//                    }
//                }
//            }
//            return null;
//        }
//    }
//    static {
//        final Map<Classify.Section, List<Classify.Collated>> sectionMembers =
//                Arrays.stream(Classify.Collated.values())
//                        .sorted(Comparator.comparing(collated -> Classify.Collated.valueOf(collated.name()).ordinal()))
//                        .collect(Collectors.groupingBy(collated -> collated.section));
//        List<PrefDataNode> sectionGroups = new ArrayList<>();
//        for(Classify.Section section: sectionMembers.keySet()){
//            Map<Classify.Group, List<Classify.Collated>> grouped = sectionMembers.get(section).stream().collect(Collectors.groupingBy(collated -> collated.group));
//            List<PrefDataNode> groupedChildren = new ArrayList<>();
//            for(Classify.Group group: grouped.keySet().stream().sorted(Comparator.nullsFirst(Comparator.comparing(Classify.Group::toString))).toList()){
//                System.out.println(group);
//                List<PrefDataNode> children = new ArrayList<>();
//                for(Classify.Collated collated: grouped.get(group)){
//                    Component[] components = PrefParts.createComponentsFor(collated);
//                    children.add(new PrefDataNode(collated.section, collated.group, (JLabel) components[0], components[1], new JCheckBox(),collated, null));
//                }
//                groupedChildren.add(new PrefDataNode(section, group,null, null, null, null, children));
//            }
//            sectionGroups.add(new PrefDataNode(section, null,null, null, null, null, groupedChildren));
//        }
//        PrefDataNode myRootDataNode = new PrefDataNode(null, null,null,null,null, null, sectionGroups);
//        MyAbstractTreeTableModel treeTableModel = new PrefDataModel(myRootDataNode);
//        MyTreeTable myTreeTable = new MyTreeTable(treeTableModel);
//        PrefParts.PREF_PANE.add(myTreeTable, PrefParts.CONSTRAINTS.get(PrefParts.Archetype.SECTION));
//    }
//    protected static class PrefDataModel extends MyAbstractTreeTableModel {
//        static protected String[] columnNames = {"Tree", "Section", "Group", "Label", "Control", "Fave", "Collated", "Children"};
//
//        // Spalten Typen.
//        static protected Class<?>[] columnTypes = {PrefDataModel.class, Classify.Section.class, Classify.Group.class, Component.class, Component.class, Component.class, Classify.Collated.class, PrefDataNode.class};
//
//        public PrefDataModel(PrefDataNode rootNode) {
//            super(rootNode);
//            root = rootNode;
//        }
//
//        public Object getChild(Object parent, int index) {
//            return ((PrefDataNode) parent).children.get(index);
//        }
//
//
//        public int getChildCount(Object parent) {
//            return ((PrefDataNode) parent).children.size();
//        }
//
//
//        public int getColumnCount() {
//            return columnNames.length;
//        }
//
//
//        public String getColumnName(int column) {
//            return columnNames[column];
//        }
//
//
//        public Class<?> getColumnClass(int column) {
//            return columnTypes[column];
//        }
//
//        public Object getValueAt(Object node, int column) {
//            return switch (column) {
//                case 0 -> ((PrefDataNode) node).section;
//                case 1 -> ((PrefDataNode) node).group;
//                case 2 -> ((PrefDataNode) node).label;
//                case 3 -> ((PrefDataNode) node).component;
//                case 4 -> ((PrefDataNode) node).fave;
//                case 5 -> ((PrefDataNode) node).collated;
//                case 6 -> ((PrefDataNode) node).children;
//                default -> null;
//            };
//        }
//
//        public boolean isCellEditable(Object node, int column) {
//            return true; // Important to activate TreeExpandListener
//        }
//
//        public void setValueAt(Object aValue, Object node, int column) {
//        }
//
//    }
//    protected static class PrefDataNode {
//        Classify.Section section;
//        Classify.Group group;
//        JLabel label;
//        Component component;
//        Component fave;
//        Classify.Collated collated;
//        List<PrefDataNode> children;
//
//
//        public PrefDataNode(Classify.Section section, Classify.Group group, JLabel label, Component component, Component fave, Classify.Collated collated, List<PrefDataNode> children) {
//            this.section = section;
//            this.group = group;
//            this.label = label;
//            this.component = component;
//            this.fave = fave;
//            this.children = children;
//            if (this.children == null) {
//                this.children = Collections.emptyList();
//            }
//        }
//        public String toString() {
//            if(label != null) {
//                return label.getText();
//            } else if(group != null){
//                return group.toString();
//            } else if(section != null){
//                return section.toString();
//            }
//            return "Preferences";
//        }
//    }
}