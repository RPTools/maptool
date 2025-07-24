package net.rptools.maptool.client.ui.preferencesdialog;

import javax.swing.*;
    /**
     * The PreferencesDialogView class represents the view for a preferences dialog. It provides methods
     * for accessing the root component of the dialog.
     */
    public class AppConfigDialog {

        /**
         * The mainPanel variable represents the root component of the preferences dialog view. It is an
         * instance of JPanel and serves as the main content panel for the dialog.
         */
        private JPanel mainPanel;

        /**
         * Returns the root component of the preferences dialog view.
         *
         * @return The root component of the preferences dialog view.
         */
        public JComponent getRootComponent() {
            return mainPanel;
        }
    }
