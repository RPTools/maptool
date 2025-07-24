package net.rptools.maptool.client.ui.theme;

import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.PreferencesChanged;
import net.rptools.maptool.client.swing.*;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.localisedObject.LocalListItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A separate dialogue that holds Theme related preferences.
 * preferences.
 *
 * <p>This dialog window is modal.
 */
public class ThemeDialog {
    /**
     * Logger instance used for logging messages in the PreferencesDialog class.
     */
    private static final Logger log = LogManager.getLogger(ThemeDialog.class);

    private static final JComponent MAIN = new ThemeDialogForm().getRootComponent();
    private static final AbeillePanel<Object> CONTENT = new AbeillePanel<>(MAIN);
    private static final AbeillePanel<?> themeFontPreferences = new ThemeFontPreferences();

    private static final GenericDialogFactory dialogFactory = GenericDialog.getFactory()
            .makeModal(true)
            .addButton(ButtonKind.CLOSE)
            .setDialogTitle(I18N.getText("Label.themes"));

    /* ComboBoxes */
    private static final JComboBox<?> macroEditorThemeComboSource = CONTENT.getComboBox("macroEditorThemeCombo");
    private static final JComboBox<?> iconThemeComboSource = CONTENT.getComboBox("iconThemeCombo");
    private static final JComboBox<?> themeFilterComboSource = CONTENT.getComboBox("themeFilterCombo");

    /* Lists */
    private static JList<?> themeListSource = (JList<?>) CONTENT.getList("themeList");
    private static ListModel<String> allThemesListModel;
    private static ListModel<String> lightThemesListModel;
    private static ListModel<String> darkThemesListModel;

    /* Labels */
    private static final JLabel themeImageLabel = (JLabel) CONTENT.getComponent("themeImage");
    private static final JLabel themeNameLabel = (JLabel) CONTENT.getComponent("currentThemeName");

    /**
     * Checkbox for if the theme should be applied to the chat window.
     */
    private static final JCheckBox useThemeForChat = CONTENT.getCheckBox("useThemeForChat");

    private static final LocalListItem[] themeFilterComboItems = new LocalListItem[]{
            new LocalListItem("All", "Preferences.combo.themes.filter.all"),
            new LocalListItem("Dark", "Preferences.combo.themes.filter.dark"),
            new LocalListItem("Light", "Preferences.combo.themes.filter.light")
    };
    /**
     * Flag indicating if theme has been changed.
     */
    private static boolean themeChanged = false;

    static {
        initComponents();
        dialogFactory
                .onBeforeClose(
                        e -> {
                            themeChanged = themeChanged | themeFontPreferences.commit();
                            new MapToolEventBus().getMainEventBus().post(new PreferencesChanged());
                            if (themeChanged || ThemeSupport.needsRestartForNewTheme()) {
                                MapTool.showMessage(
                                        "themeChangeWarning",
                                        "themeChangeWarningTitle",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        })
                .setContent(CONTENT);
    }


    private static final ThemeDialog instance = new ThemeDialog();
public static ThemeDialog getInstance(){
    return instance;
}

    private static void initComponents() {
        CONTENT.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        CONTENT.replaceComponent("uiFontPrefsContainer", "uiFontPrefs", themeFontPreferences);


        JComboBox<?> iconThemeCombo = new JComboBox<AppPreferenceEnums.UIIconType>();
        iconThemeCombo.setSelectedItem(AppPreferences.iconTheme.get());
        iconThemeCombo.addItemListener(
                e -> AppPreferences.iconTheme.set((String) iconThemeCombo.getSelectedItem()));
        CONTENT.replaceComponent(iconThemeComboSource.getParent().getName(), iconThemeComboSource.getName(), iconThemeCombo);


        var lm = new DefaultListModel<String>();
        Arrays.stream(ThemeSupport.THEMES).map(ThemeSupport.ThemeDetails::name).sorted().forEach(lm::addElement);
        allThemesListModel = lm;

        lm = new DefaultListModel<>();
        Arrays.stream(ThemeSupport.THEMES)
                .filter(ThemeSupport.ThemeDetails::dark)
                .map(ThemeSupport.ThemeDetails::name)
                .sorted()
                .forEach(lm::addElement);
        darkThemesListModel = lm;

        lm = new DefaultListModel<>();
        Arrays.stream(ThemeSupport.THEMES)
                .filter(t -> !t.dark())
                .map(ThemeSupport.ThemeDetails::name)
                .sorted()
                .forEach(lm::addElement);
        lightThemesListModel = lm;

        JList<String> themeList = new JList<>();
        themeList.setModel(allThemesListModel);
        themeList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        themeList.setSelectedValue(ThemeSupport.getThemeName(), true);
        SwingUtilities.invokeLater(
                () -> {
                    themeImageLabel.setIcon(ThemeSupport.getExampleImageIcon(themeImageLabel.getSize()));
                });
        themeList.addListSelectionListener(
                e -> {
                    if (!e.getValueIsAdjusting()) {
                        String theme = themeList.getSelectedValue();
                        ThemeSupport.setTheme(theme);
                        themeImageLabel.setIcon(
                                ThemeSupport.getExampleImageIcon(theme, themeImageLabel.getSize()));
                    }
                });


        themeNameLabel.setText(ThemeSupport.getThemeName());

        useThemeForChat.setSelected(ThemeSupport.shouldUseThemeColorsForChat());
        useThemeForChat.addActionListener(
                l -> {
                    ThemeSupport.setUseThemeColorsForChat(useThemeForChat.isSelected());
                });

        JComboBox<String> macroEditorThemeCombo = new JComboBox<>();
        macroEditorThemeCombo.setModel(new DefaultComboBoxModel<>());
        try (Stream<Path> paths = Files.list(AppConstants.THEMES_DIR.toPath())) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".xml"))
                    .forEach(
                            p ->
                                    macroEditorThemeCombo.addItem(
                                            FilenameUtils.removeExtension(p.getFileName().toString())));
            macroEditorThemeCombo.setSelectedItem(AppPreferences.defaultMacroEditorTheme.get());
        } catch (IOException ioe) {
            log.warn("Unable to list macro editor themes.", ioe);
            macroEditorThemeCombo.addItem("Default");
        }
        macroEditorThemeCombo.addItemListener(
                e ->
                        AppPreferences.defaultMacroEditorTheme.set(
                                (String) macroEditorThemeCombo.getSelectedItem()));
        CONTENT.replaceComponent(macroEditorThemeComboSource.getParent().getName(), macroEditorThemeComboSource.getName(), macroEditorThemeCombo);

        themeListSource = themeList;

        JComboBox<LocalListItem> themeFilterCombo = new JComboBox<>();
        themeFilterCombo.setModel(LocalListItem.getLocalisedComboBoxModel(themeFilterComboItems));
        themeFilterCombo.addItemListener(
                e -> {
                    String filter = ((LocalListItem) Objects.requireNonNull(themeFilterCombo.getSelectedItem())).getValue().toString();
                    switch (filter) {
                        case "All":
                            themeList.setModel(allThemesListModel);
                            break;
                        case "Dark":
                            themeList.setModel(darkThemesListModel);
                            break;
                        case "Light":
                            themeList.setModel(lightThemesListModel);
                            break;
                    }
                });
        CONTENT.replaceComponent(themeFilterComboSource.getParent().getName(), themeFilterComboSource.getName(), themeFilterCombo);
    }

    public void showDialog() {
        themeChanged = false;
        dialogFactory.display();
    }
}