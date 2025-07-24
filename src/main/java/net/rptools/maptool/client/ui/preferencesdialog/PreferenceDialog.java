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

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TreeUI;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.formdev.flatlaf.ui.FlatTreeUI;
import com.jidesoft.grid.ButtonTableCellEditorRenderer;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.event.SearchableEvent;
import com.jidesoft.swing.event.SearchableListener;
import net.rptools.lib.cipher.CipherUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.*;
import net.rptools.maptool.client.swing.searchable.SearchableBarEx;
import net.rptools.maptool.client.swing.searchable.SearchableEx;
import net.rptools.maptool.client.swing.searchable.SearchableTree;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.localisedObject.LocalObject;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class: PreferenceDialog
 *
 * <p>A dialog box to manage user preferences.
 */
public class PreferenceDialog extends AbeillePanel<Object> {
    /**
     * Logger instance used for logging messages in the PreferenceDialog class.
     */
    private static final Logger log = LogManager.getLogger(PreferenceDialog.class);

    private static final PreferenceDialog instance;

    public static PreferenceDialog getInstance() {
        return instance;
    }

    /**
     * my version of percentile screen width
     */
    private static final int vw =
            (int)
                    Math.max(
                            GraphicsEnvironment.getLocalGraphicsEnvironment()
                                    .getDefaultScreenDevice()
                                    .getDefaultConfiguration()
                                    .getBounds()
                                    .getWidth()
                                    / 100,
                            8);

    /**
     * Because we want to, you know, show this to people
     */
    private static final GenericDialogFactory dialogFactory =
            GenericDialog.getFactory()
                    .setDialogTitle(I18N.getString("Label.preferences"))
                    .makeModal(true)
                    .setContent(PrefParts.PREF_PANE)
                    .setNavPane(new JScrollPane(PrefParts.NAV_PANEL))
                    .setToolbar(PrefParts.SEARCH_PANE)
                    .setSideBar(new JScrollPane(PrefParts.SIDE_PANE));

    private static final FlatTree prefTree;
    private static final TableModel tableModel = new PreferencesTableModel();
    private static final PreferencesTable table;
    private static final SearchableEx SEARCHABLE_TREE;
    private static final SearchableBarEx SEARCHABLE_BAR;

    static {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        final Color CLEAR = new Color(1f, 1f, 1f, 0);
        PrefParts.initLayoutAndConstraints();
        final Map<Classify.Section, List<Classify.Collated>> sectionMembers =
                Arrays.stream(Classify.Collated.values())
                        .sorted(Comparator.nullsFirst(Comparator.comparing(o -> o.group)))
                        .toList().stream()
                        .sorted(Comparator.nullsFirst(Comparator.comparing(collated -> collated.section)))
                        .toList()
                        .stream().collect(Collectors.groupingBy(collated -> collated.section));
        int sectionRow = 0;
        for (Classify.Section sect : Classify.Section.values()) {
            final List<Classify.Collated> members = sectionMembers.get(sect);
            if (members == null) {
                continue;
            }
            DefaultMutableTreeNode sectNode = new DefaultMutableTreeNode(sect, true);
            root.add(sectNode);
            JPanel sectionPanel = new JPanel(new GridBagLayout());
            PrefParts.CONSTRAINTS.get(PrefParts.Archetype.SECTION).gridy = sectionRow;
            PrefParts.PREF_PANE.add(sectionPanel, PrefParts.CONSTRAINTS.get(PrefParts.Archetype.SECTION));
            sectionRow++;
            sectionPanel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(
                                    BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Window.border")),
                                    sect.displayName),
                            BorderFactory.createEmptyBorder(6, 12, 4, 8)
                    ));
            final List<Classify.Collated> ungrouped = members.stream().filter(collated -> collated.group == null).toList();
            final List<Classify.Collated> grouped = members.stream().filter(collated -> collated.group != null).toList();
            final Map<Classify.Group, List<Classify.Collated>> memberGroups = grouped.stream()
                    .sorted(Comparator.comparing(collated -> Classify.Collated.valueOf(collated.name()).ordinal())).collect(Collectors.groupingBy(collated -> collated.group));

            int groupRow = 0;
            for (Classify.Group g : memberGroups.keySet()) {
                DefaultMutableTreeNode groupNode;
                if (g.equals(Classify.Group.NONE)) {
                    groupNode = sectNode;
                } else {
                    groupNode = new DefaultMutableTreeNode(g, true);
                }


                JPanel groupPanel = new JPanel(new GridBagLayout());
                PrefParts.CONSTRAINTS.get(PrefParts.Archetype.GROUP).gridy = groupRow;
                groupRow++;
                sectionPanel.add(groupPanel, PrefParts.CONSTRAINTS.get(PrefParts.Archetype.GROUP));
                groupPanel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(
                                        g.equals(Classify.Group.NONE) ?
                                                BorderFactory.createLineBorder(CLEAR) :
                                                BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Window.border"))
                                        ,
                                        g.equals(Classify.Group.NONE) ? null : g.displayName),
                                BorderFactory.createEmptyBorder(6, 8, 4, 12)
                        ));
                int controlRow = 0;

                for (Classify.Collated collated : memberGroups.get(g)) {
                    Component[] components = PrefParts.createComponentsFor(collated);
                    PrefParts.CONSTRAINTS.get(PrefParts.Archetype.LABEL).gridy = controlRow;
                    PrefParts.CONSTRAINTS.get(PrefParts.Archetype.CONTROL).gridy = controlRow;
                    PrefParts.CONSTRAINTS.get(PrefParts.Archetype.FAVOURITE).gridy = controlRow;
                    try {
                        groupPanel.add(components[0], PrefParts.CONSTRAINTS.get(PrefParts.Archetype.LABEL));
                        groupPanel.add(components[1], PrefParts.CONSTRAINTS.get(PrefParts.Archetype.CONTROL));
                        groupPanel.add(new JCheckBox(), PrefParts.CONSTRAINTS.get(PrefParts.Archetype.FAVOURITE));
                        DefaultMutableTreeNode prefNode = new DefaultMutableTreeNode(collated);

                        groupNode.add(prefNode);
                    } catch (Exception e) {
                        log.error(e);
                    }
                    controlRow++;
                }
                if (!groupNode.equals(sectNode)) {
                    sectNode.add(groupNode);
                }
            }
        }
        TreeModel treeModel = new DefaultTreeModel(root);
        prefTree = new PreferenceTree(treeModel);
        prefTree.setRootVisible(false);
        prefTree.putClientPropertyBoolean("Tree.paintLines", true, true);
        prefTree.setCellRenderer(new PrefTreeCellRenderer());
        TreeUI UI = (TreeUI) PreferenceTreeUI.createUI(prefTree);
        prefTree.setUI(UI);


        PrefParts.PREF_PANE.removeAll();

//        {
//            static class JComponentRenderer extends JComponent implements TableCellRenderer, UIResource {
//                private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
//
//                public JComponentRenderer() {
//                    super();
//                    setAlignmentX(0.5f);
//                    setBorder(noFocusBorder);
//                }
//
//                public void setValue(Object value) { if(value instanceof JComponent jComponent){add(jComponent); }};
//                @Override
//                public Component getTableCellRendererComponent(JTable table, Object value,
//                                                               boolean isSelected, boolean hasFocus, int row, int column) {
//                    if (isSelected) {
//                        setForeground(table.getSelectionForeground());
//                        super.setBackground(table.getSelectionBackground());
//                    }
//                    else {
//                        setForeground(table.getForeground());
//                        setBackground(table.getBackground());
//                    }
//
//                    if (hasFocus) {
//                        setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
//                    } else {
//                        setBorder(noFocusBorder);
//                    }
//
//                    return this;
//                }
//            }
//            @Override
//            protected void createDefaultRenderers() {
//                super.createDefaultRenderers();
//                defaultRenderersByColumnClass.put(JComponent.class, (UIDefaults.LazyValue)
//                        t -> new JComponentRenderer());
//            }
//        };
//        table.setFillsViewportHeight(true);
//        table.setDefaultRenderer(Component.class, new ButtonTableCellEditorRenderer());
//        TableColumnModel columnModel = table.getColumnModel();
//        for (int i = 0; i < columnModel.getColumnCount(); i++) {
//            TableColumn column = columnModel.getColumn(i);
//            column.setResizable(i < columnModel.getColumnCount() - 1);
//        }
//        System.out.println(tableModel.getColumnCount());
        JideScrollPane tableScrollPane = new JideScrollPane();
        table = new PreferencesTable(tableScrollPane);
        table.doLayout();
        PrefParts.PREF_PANE.setLayout(new BorderLayout());
        PrefParts.PREF_PANE.add(tableScrollPane, BorderLayout.CENTER);


        SEARCHABLE_TREE = new SearchableTree(prefTree);
        SEARCHABLE_TREE.addSearchableListener(getSearchableListener());
        SEARCHABLE_TREE.setWildcardEnabled(true);
        SEARCHABLE_TREE.setCaseSensitive(false);
        SEARCHABLE_TREE.setProcessModelChangeEvent(true);
        SEARCHABLE_TREE.setSearchLabel("What");
        SEARCHABLE_TREE.setRepeats(true);

        SEARCHABLE_BAR = new SearchableBarEx(SEARCHABLE_TREE, true);
        SEARCHABLE_BAR.setHighlightAll(true);
        SEARCHABLE_BAR.setCompact(true);
        SEARCHABLE_BAR.setVisibleButtons(SearchableBarEx.SHOW_NAVIGATION
                + SearchableBarEx.SHOW_HIGHLIGHTS
                + SearchableBarEx.SHOW_REPEATS
                + SearchableBarEx.SHOW_STATUS
                + SearchableBarEx.SHOW_WHOLE_WORDS
                + SearchableBarEx.SHOW_MATCHCASE
                + SearchableBarEx.SHOW_REGEX
        );
        SEARCHABLE_BAR.addPresetSearchValues(Arrays.stream(Classify.Section.values()).collect(Collectors.toSet()));
        SEARCHABLE_BAR.getSearchable().addSearchableListener(searchableEvent ->
                table.setFilterText(searchableEvent.getSearchingText()));
        PrefParts.NAV_PANEL.add(prefTree);

        instance = new PreferenceDialog();
    }



    /**
     * The PreferenceDialog class represents a dialog window that allows users to customize their
     * preferences.
     *
     * <p>This dialog window is modal.
     */
    public PreferenceDialog() {
        super(new PreferencesEditorDialog().getRootComponent());
        dialogFactory
                .setCloseOperation(WindowConstants.HIDE_ON_CLOSE)
                .addButton(ButtonKind.CLOSE)
                .setDefaultButton(ButtonKind.CLOSE);

        setInitialState();

        //    copyPublicKey.addActionListener(
        //        e -> {
        //          Toolkit.getDefaultToolkit()
        //              .getSystemClipboard()
        ////              .setContents(new StringSelection(publicKeyTextArea.getText()), null);
        //        });
        //
        //    regeneratePublicKey.addActionListener(
        //        e -> {
        //          CompletableFuture<CipherUtil.Key> keys = new
        // PublicPrivateKeyStore().regenerateKeys();
        //
        //          keys.thenAccept(
        //              cu -> {
        //                SwingUtilities.invokeLater(
        //                    () -> {
        ////                      publicKeyTextArea.setText(cu.getEncodedPublicKeyText());
        //                    });
        //              });
        //        });
    }


    public void showDialog() {
        dialogFactory.display();
    }

    /// Most controls have their state set at creation. Initialise things here where this is
    /// insufficient, such as adding listeners or the like. Custom creation should be handled in {@link PrefParts#createComponentsFor(Classify.Collated)}
    private void setInitialState() {
        JCheckBox labelBorderCheckbox =
                ((JCheckBox) PrefParts.findComponent(AppPreferences.mapLabelShowBorder.getKey()));
        Objects.requireNonNull(labelBorderCheckbox).addActionListener(PrefParts.labelBorderListener);
        labelBorderCheckbox.setSelected(AppPreferences.mapLabelShowBorder.get());

        Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderColor"))
                .setEnabled(labelBorderCheckbox.isSelected());
        Objects.requireNonNull(PrefParts.findComponent("pcMapLabelBorderColor"))
                .setEnabled(labelBorderCheckbox.isSelected());
        Objects.requireNonNull(PrefParts.findComponent("nonVisMapLabelBorderColor"))
                .setEnabled(labelBorderCheckbox.isSelected());
        Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderWidth"))
                .setEnabled(labelBorderCheckbox.isSelected());
        Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderArc"))
                .setEnabled(labelBorderCheckbox.isSelected());

        CompletableFuture<CipherUtil.Key> keys = MapTool.getKeyStore().getKeys();

        keys.thenAccept(
                cu -> {
                    SwingUtilities.invokeLater(
                            () -> {
                                //                  publicKeyTextArea.setText(cu.getEncodedPublicKeyText());
                            });
                });
    }

    static {
        AbstractButton collapseButton = new FlatToggleButton();
        collapseButton.setIcon(RessourceManager.getSmallIcon(Icons.PROPERTIES_TABLE_COLLAPSE));
        collapseButton.addActionListener(e -> {
            for (int i = 0; i < prefTree.getRowCount(); i++) {
                prefTree.collapseRow(i);
            }
        });
        AbstractButton expandButton = new FlatToggleButton();
        expandButton.setIcon(RessourceManager.getSmallIcon(Icons.PROPERTIES_TABLE_EXPAND));
        expandButton.addActionListener(e -> {
            for (int i = 0; i < prefTree.getRowCount(); i++) {
                prefTree.expandRow(i);
            }
        });
        PrefParts.NAV_PANEL.add(collapseButton, 0);
        PrefParts.NAV_PANEL.add(expandButton, 0);

        PrefParts.SEARCH_PANE.add(SEARCHABLE_BAR);
    }

    private static SearchableListener getSearchableListener() {
        return new SearchableListener() {
            @Override
            public void searchableEventFired(SearchableEvent searchableEvent) {
                if (searchableEvent.getID() == SearchableEvent.SEARCHABLE_MATCH) {
                    Object o = searchableEvent.getMatchingObject();
                    if (o instanceof Component[] components) {
                        Rectangle r = components[1].getBounds();
                        System.out.println(r);
                        Point p = PrefParts.findComponent(components[1].getName()).getLocation();
                        System.out.println(p);
                        PrefParts.PREF_PANE.scrollRectToVisible(r);
                    }
                } else {
                    log.info(searchableEvent.paramString());
                }
                log.info(searchableEvent);
            }
        };
    }

    private static class PrefTreeCellRenderer extends DefaultTreeCellRenderer {
        /**
         * Icon used to show leaf nodes.
         */
        protected transient Icon leafIcon = RessourceManager.getSmallIcon(Icons.MAPTOOL);

        private static final FlatSVGIcon.ColorFilter NEGATIVE_FILTER = new FlatSVGIcon.ColorFilter().addAll(GraphicsUtil.INVERTED_ICON_COLOURS);

        @Override
        public Icon getLeafIcon() {
            return leafIcon;
        }

        @Override
        public void setLeafIcon(Icon leafIcon) {
        }

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging the tree with
         * <code>convertValueToText</code>, which ultimately invokes
         * <code>toString</code> on <code>value</code>.
         * The foreground color is set based on the selection and the icon
         * is set based on the <code>leaf</code> and <code>expanded</code>
         * parameters.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf, int row,
                                                      boolean hasFocus) {
            PreferenceTree pTree = (PreferenceTree) tree;
            DefaultMutableTreeNode node = null;
            if (value != null) {
                node = (DefaultMutableTreeNode) value;
            }
            String stringValue = pTree.convertValueToText(node == null ? value : node, sel, expanded, leaf, row, hasFocus);
            String tipText = pTree.convertValueToTooltipText(node == null ? value : node, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            setText(stringValue);
            setToolTipText(tipText);
            Color fg = null;

            JTree.DropLocation dropLocation = tree.getDropLocation();
            if (dropLocation != null
                    && dropLocation.getChildIndex() == -1
                    && tree.getRowForPath(dropLocation.getPath()) == row) {
            } else if (sel) {
                fg = getTextSelectionColor();
            } else {
                fg = getTextNonSelectionColor();
            }

            setForeground(fg);

            Icon icon = null;
            if (leaf) {
                icon = leafIcon;
            }

            if (!tree.isEnabled()) {
                setEnabled(false);
                LookAndFeel laf = UIManager.getLookAndFeel();
                Icon disabledIcon = sel ? laf.getDisabledSelectedIcon(tree, icon) : laf.getDisabledIcon(tree, icon);
                if (disabledIcon != null) icon = disabledIcon;
                setDisabledIcon(icon);
            } else {
                setEnabled(true);
                if (icon instanceof FlatSVGIcon svgIcon) {
                    svgIcon.setColorFilter(!sel ? null : NEGATIVE_FILTER);
                }
                setIcon(icon);
            }
            setComponentOrientation(tree.getComponentOrientation());
            selected = sel;
            return this;
        }
    }

    private static class PreferenceTree extends FlatTree {
        protected transient PrefTreeCellRenderer cellRenderer = new PrefTreeCellRenderer();
        PreferenceTree(TreeModel model) {
            super();
            super.setModel(model);
            super.setCellRenderer(cellRenderer);
            super.setRootVisible(false);
            super.setScrollsOnExpand(true);
            super.setShowsRootHandles(true);
            super.setWideCellRenderer(true);
        }

        /**
         * Called by the renderers to convert the specified value to
         * text. This implementation returns <code>value.toString</code>, ignoring
         * all other arguments. To control the conversion, subclass this
         * method and use any of the arguments you need.
         *
         * @param value    the <code>Object</code> to convert to text
         * @param selected true if the node is selected
         * @param expanded true if the node is expanded
         * @param leaf     true if the node is a leaf node
         * @param row      an integer specifying the node's display row, where 0 is
         *                 the first row in the display
         * @param hasFocus true if the node has the focus
         * @return the <code>String</code> representation of the node's value
         */
        @Override
        public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode node) {
                Object o = node.getUserObject();
                if (o instanceof LocalObject localObject) {
                    return localObject.getDisplayName();
                } else if (o instanceof Classify.Collated collated) {
                    if (collated.preference != null) {
                        return collated.preference.getLabel();
                    } else {
                        return collated.id;
                    }
                }
            }
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        }

        public String convertValueToTooltipText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode node) {
                Object o = node.getUserObject();
                if (o instanceof Classify.Collated collated) {
                    if (collated.preference != null) {
                        return collated.preference.getTooltip();
                    } else {
                        return collated.id;
                    }
                }
            }
            return null;
        }
    }
    private class PreferenceTreeUI extends FlatTreeUI{
        public static ComponentUI createUI(JComponent c ) {
            return new FlatTreeUI();
        }
        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            tree = (JTree) c;
        }
        @Override
        protected void installDefaults() {
            super.installDefaults();
            try {
                Field paintLines = super.getClass().getField("paintLines");
                paintLines.setAccessible(true);
                paintLines.setBoolean(tree.getUI(), Boolean.parseBoolean(tree.getClientProperty("Tree.paintLines").toString()));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }
}