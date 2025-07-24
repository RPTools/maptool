package net.rptools.maptool.client.ui.preferencesdialog;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.intellij.uiDesigner.core.GridConstraints;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.theme.ThemeDialog;
import net.rptools.maptool.language.I18N;

import net.rptools.maptool.model.localisedObject.LocalEnumListItem;
import net.rptools.maptool.util.preferences.Preference;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

class PrefParts {
    private static final Logger log = LogManager.getLogger(PrefParts.class);
    /**
     * Main content panel - for displaying preference controls
     */
    protected static final JPanel PREF_PANE = new JPanel(new BorderLayout());

    /**
     * Panel for displaying filtering options/controls
     */
    protected static final JPanel SEARCH_PANE = new JPanel();

    /**
     * This has no use other than testing
     */
    protected static final JPanel SIDE_PANE = new JPanel(new GridLayout(0, 18));
    /**
     * Panel on LHS shortcuts, hopefully temporary
     */
    protected static final JPanel NAV_PANEL = new JPanel();

    /**
     * Map of control components, used for {@link #findComponent(String)} & {@link #findComponent(String, Container)}  layout changes
     */
    protected static final Map<String, Component> COMPONENT_MAP = new HashMap<>();
    /**
     * Separate dialog for managing theme settings
     */
    protected static final ThemeDialog themeDialog = ThemeDialog.getInstance();
    /**
     * Separate dialog for managing developer options
     */
    protected static final DeveloperOptionsDialog developerOptionsDialog = new DeveloperOptionsDialog();
    /**
     * Separate dialog for showing unsettable JVM settings
     */
    protected static final ConfigDialog configDialog = new ConfigDialog();
    private static final int layoutColumns = 6;

    protected enum Archetype {
        SECTION,
        CONTROL,
        FAVOURITE,
        GROUP,
        HEADER,
        LABEL,
        ROW
    }

    protected static final Map<Archetype, GridBagConstraints> CONSTRAINTS = new HashMap<>();// section, group, prefLabel, prefControl, prefFave;

    /**
     * Method to generate the label and control for entries in {@link Classify.Collated}.
     * For most preferences everything is auto-go-matic, for things that need special
     * creation, set the {@link Classify.Collated#useDefaultControls} flag to false and
     * add a branch to the switch statement.
     * @param collated {@link Classify.Collated} enum entry
     * @return Component array of label and control
     */
    protected static Component[] createComponentsFor(Classify.Collated collated) {
        Component[] components;
        if (!collated.useDefaultControls) {
            components = switch (collated) {
                case AUTH_KEY -> new Component[]{
                        Fn.createLabelUseKeys.apply("Preferences.label.auth.publicKey","Preferences.label.auth.publicKey.tooltip", collated),
                        createDeveloperOptionsButton()
                };
                case DEV_OPTIONS -> new Component[]{
                        Fn.createLabelUseKeys.apply("Preferences.tab.developer", null, collated),
                        createDeveloperOptionsButton()
                };
                case STARTUP -> new Component[]{
                        Fn.createLabelUseKeys.apply("Label.startup", null, collated),
                        createConfigButton()
                };
                case THEME -> new Component[]{
                        Fn.createLabelUseKeys.apply("Label.themes", null, collated),
                        createThemeButton()
                };
                case ICONS -> new Component[]{
                        Fn.createLabelUseKeys.apply("Label.iconTheme", "Preferences.label.iconTheme.tooltip", collated),
                        createThemeButton()
                };
                case MACRO_ED_THEME ->
                        new Component[]{
                                Fn.createLabelUseKeys.apply("Label.theme.macroEditor", "Preferences.label.macroEditor.tooltip", collated),
                                createThemeButton()
                };
                default -> null;
            };
        } else {
            components = new Component[]{
                    Fn.createLabel.apply(null, null, collated),
                    Fn.createComponent.apply(collated)
            };
            ((JLabel)components[0]).setLabelFor(components[1]);
        }
        if (components != null) {
            for (Component component : components) {
                COMPONENT_MAP.put(component.getName(), component);
            }
        }
        return components;
    }

    private static AbstractButton createConfigButton() {
        AbstractButton button = Fn.createButton.get();
        button.setText(I18N.getText("Label.startup"));
        button.addActionListener(e -> PrefParts.configDialog.showDialog());
        return button;
    }

    private static AbstractButton createThemeButton() {
        AbstractButton button = Fn.createButton.get();
        button.setText(I18N.getText("Label.themes"));
        button.addActionListener(e -> PrefParts.themeDialog.showDialog());
        return button;
    }

    private static AbstractButton createDeveloperOptionsButton() {
        AbstractButton button = Fn.createButton.get();
        button.setText(I18N.getText("Preferences.tab.developer"));
        button.setIcon(RessourceManager.getSmallIcon(Icons.WARNING));
        button.addActionListener(e -> PrefParts.developerOptionsDialog.showDialog());
        return button;
    }

    /**
     * Functions for generating labels and controls.
     */
    private static class Fn {
        static Supplier<AbstractButton> createButton = () -> {
            AbstractButton button = new FlatButton();
            button.putClientProperty(
                    FlatClientProperties.BUTTON_TYPE, FlatButton.ButtonType.roundRect);
            button.putClientProperty(FlatClientProperties.STYLE, "arc:12");
            return button;
        };
        static Function<Preference<Boolean>, JCheckBox> createCheckbox =
                booleanPreference -> {
                    JCheckBox cb = new JCheckBox();
                    cb.setSelected(booleanPreference.get());
                    cb.setHorizontalAlignment(SwingConstants.LEADING);
                    cb.addActionListener(e -> booleanPreference.set(cb.isSelected()));
                    cb.setName(booleanPreference.getKey());
                    return cb;
                };
        static Function<Preference.Numeric<?>, JSpinner> createSpinner =
                pref -> {
                    Number min = pref.getMinValue();
                    Number max = pref.getMaxValue();
//                    Number step = pref.getPrecision();
                    Number value = pref.get();
                    JSpinner spinner = new JSpinner();
                    spinner.setName(pref.getKey());
                    SpinnerNumberModel numberModel;
                    try {
                        if (pref.cast(Integer.class).isPresent()) {
                            numberModel =
                                    new SpinnerNumberModel(
                                            value.intValue(), min.intValue(), max.intValue(), 1);
                        } else {
                            numberModel =
                                    new SpinnerNumberModel(
                                            value.doubleValue(),
                                            min.doubleValue(),
                                            max.doubleValue(),
                                            0.1);
                        }
                        spinner.setModel(numberModel);
                        JFormattedTextField ftf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
                        ftf.setColumns(3);
                        spinner.addChangeListener(
                                e -> {
                                    Number val =
                                            ((SpinnerNumberModel) ((JSpinner) e.getSource()).getModel()).getNumber();
                                    if (pref.cast(Integer.class).isPresent() || pref.cast(Byte.class).isPresent()) {
                                        pref.cast(Integer.class).get().set(val.intValue());
                                    } else if (pref.cast(Double.class).isPresent()) {
                                        pref.cast(Double.class).get().set(val.doubleValue());
                                    }
                                });
                    } catch (Exception e) {
                        log.warn("Spinner creation failure: {}\n{}", pref.getKey(), e);
                    }
                    return spinner;
                };
        static Function<Preference<String>, JComponent> createTextComponent =
                pref -> {
                    String value = pref.get();
                    JTextComponent component;
                    if (value == null || value.length() < 60) {
                        component = new JTextField(value);
                    } else {
                        component = new JTextArea(value, 6, 30);
                    }
                    component.setName(pref.getKey());
                    component.addFocusListener(
                            new FocusAdapter() {
                                @Override
                                public void focusLost(FocusEvent e) {
                                    if (!e.isTemporary()) {
                                        JTextComponent component = (JTextComponent) e.getComponent();
                                        if (pref.equals(AppPreferences.chatFilenameFormat)) {
                                            StringBuilder saveFile = new StringBuilder(component.getText());
                                            if (saveFile.indexOf(".") < 0) {
                                                saveFile.append(".html");
                                            }
                                            AppPreferences.chatFilenameFormat.set(saveFile.toString());
                                        } else {
                                            pref.set(component.getText());
                                        }
                                    }
                                }
                            });

                    return component;
                };
        static Function<Preference<Color>, Component> createColorWell =
                pref -> {
                    ColorWell colorWell = new ColorWell();
                    colorWell.setColor(pref.get());
                    colorWell.setAlignmentX(0);
                    int side = RessourceManager.bigIconSize / 3 * 2;
                    colorWell.setPreferredSize(new Dimension(3 * side, side));
                    colorWell.addActionListener(e -> pref.set(colorWell.getColor()));
                    return colorWell;
                };
        static Function<Preference<?>, JComboBox<?>> createEnumCombo =
                pref -> {
                    try {
                        return LocalEnumListItem.createComboBox(pref);
                    } catch (Exception e) {
                        log.warn(e);
                        return null;
                    }
                };
        static TriFunction<String, String, Classify.Collated, JLabel> createLabel =
                (labelText, tooltipText, collated) -> {
                    JLabel lbl = new JLabel();
                    lbl.setHorizontalAlignment(SwingConstants.TRAILING);
                    if (collated.emphasise) {
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    }
                    String text;
                    String tooltip;
                    if(collated.preference != null){
                        text = collated.preference.getLabel();
                        tooltip = collated.preference.getTooltip();
                        tooltip = tooltip.equals(collated.id) ? text : tooltip;
                   } else {
                        text = labelText;
                        tooltip = tooltipText;
                    }
                    lbl.setText(text);
                    lbl.setToolTipText(tooltip == null ? text : tooltipText);
                    lbl.setName(collated.id + "Label");
                    return lbl;
                };
        static TriFunction<String, String, Classify.Collated, JLabel>createLabelUseKeys =
                (labelKey, tooltipKey, collated) -> {
                    String text = I18N.getText(labelKey);
                    String tooltip = tooltipKey == null ? text : I18N.getText(tooltipKey);
                    return createLabel.apply(text, tooltip, collated);
                };
        static Function<Classify.Collated, Component> createComponent =
                collated -> {
                    Preference<?> pref = Objects.requireNonNull(collated.preference);
                    Component component = new JLabel();
                    if (pref.getValueClass().getEnumConstants() != null){
                        component = createEnumCombo.apply(pref);
                    } else if (pref.cast(File.class).isPresent()) {
                        log.warn("You should not be seeing this: File Preference -> {}", pref.getKey());
                    } else if (pref.cast(Integer.class).isPresent()) {
                        component = createSpinner.apply((Preference.Numeric<?>) pref.cast(Integer.class).get());
                    } else if (pref.cast(Double.class).isPresent()) {
                        component = createSpinner.apply((Preference.Numeric<?>) pref.cast(Double.class).get());
                    } else if (pref.cast(Color.class).isPresent()) {
                        component = createColorWell.apply(pref.cast(Color.class).get());
                    } else if (pref.cast(String.class).isPresent()) {
                        component = createTextComponent.apply(pref.cast(String.class).get());
                    } else if (pref.cast(Boolean.class).isPresent()) {
                        component = createCheckbox.apply(pref.cast(Boolean.class).get());
                    } else {
                        component = new JLabel();
                    }
                    component.setName(collated.id);
                    return component;
                };
    }


    /**
     * Return component from COMPONENT_MAP or search for it the hard way
     */
    protected static Component findComponent(String name) {
        if (COMPONENT_MAP.containsKey(name)) {
            return COMPONENT_MAP.get(name);
        } else {
            return findComponent(name, PREF_PANE);
        }
    }

    /**
     * Find a component by name the hard way
     */
    protected static Component findComponent(String name, Container parent) {
        for (Component c : parent.getComponents()) {
            if (c.getName() != null && c.getName().equalsIgnoreCase(name)) {
                return c;
            } else if (c instanceof Container container) {
                return findComponent(name, container);
            }
        }
        return null;
    }

    protected static void initLayoutAndConstraints() {
        LayoutManager layout = new GridBagLayout();
        PREF_PANE.setLayout(layout);
        CONSTRAINTS.put(Archetype.HEADER, new GridBagConstraints(
                0,
                0,
                layoutColumns,
                1,
                0,
                0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL,
                new Insets(6, 12, 8, 12),
                4,
                2));

        GridBagConstraints section = new GridBagConstraints(
                0,
                1,
                layoutColumns,
                1,
                0,
                0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL,
                new Insets(6, 12, 8, 12),
                4,
                2);

        GridBagConstraints group = (GridBagConstraints) section.clone();
        group.gridx = 1;
        group.gridwidth = layoutColumns - 1;

        CONSTRAINTS.put(Archetype.ROW, new GridBagConstraints(
                2,
                2,
                layoutColumns - 2,
                1,
                0,
                0,
                GridBagConstraints.LINE_END,
                GridConstraints.FILL_HORIZONTAL,
                new Insets(4, 6, 4, 8),
                4,
                2));

        GridBagConstraints prefLabel = new GridBagConstraints(
                2,
                0,
                1,
                1,
                0,
                0,
                GridBagConstraints.LINE_END,
                GridConstraints.FILL_HORIZONTAL,
                new Insets(4, 6, 4, 8),
                4,
                2);

        GridBagConstraints prefControl = (GridBagConstraints) prefLabel.clone();
        prefControl.gridx = 3;
        prefControl.anchor = GridBagConstraints.LINE_START;
        prefControl.fill = GridBagConstraints.BOTH;

        GridBagConstraints prefFave = (GridBagConstraints) prefLabel.clone();
        prefFave.gridx = 4;
        prefFave.fill = GridBagConstraints.NONE;
        prefFave.anchor = GridBagConstraints.LINE_END;

        CONSTRAINTS.put(Archetype.SECTION, section);
        CONSTRAINTS.put(Archetype.GROUP, group);
        CONSTRAINTS.put(Archetype.LABEL, prefLabel);
        CONSTRAINTS.put(Archetype.CONTROL, prefControl);
        CONSTRAINTS.put(Archetype.FAVOURITE, prefFave);

    }
    /**
     * Listener specific to toggling enabled state of token border related controls
     */
    static final ActionListener labelBorderListener =
            e -> {
                if (((JCheckBox) e.getSource()).isSelected()) {
                    Objects.requireNonNull(PrefParts.findComponent("pcMapLabelBorderColor")).setEnabled(true);
                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderColor")).setEnabled(true);
                    Objects.requireNonNull(PrefParts.findComponent("nonVisMapLabelBorderColor")).setEnabled(true);
                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderWidth")).setEnabled(true);
                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderArc")).setEnabled(true);
                } else {
                    Objects.requireNonNull(PrefParts.findComponent("pcMapLabelBorderColor")).setEnabled(false);
                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderColor")).setEnabled(false);
                    Objects.requireNonNull(PrefParts.findComponent("nonVisMapLabelBorderColor")).setEnabled(false);
                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderWidth")).setEnabled(false);
                    Objects.requireNonNull(PrefParts.findComponent("mapLabelBorderArc")).setEnabled(false);
                }
            };
}
