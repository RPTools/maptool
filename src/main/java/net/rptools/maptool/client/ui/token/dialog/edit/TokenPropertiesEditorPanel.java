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

import static com.jidesoft.converter.ConverterContext.DEFAULT_CONTEXT;
import static com.jidesoft.swing.SearchableBar.*;

import com.jidesoft.converter.ConverterContext;
import com.jidesoft.grid.*;
import com.jidesoft.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.table.MTMultilineStringCellEditor;
import net.rptools.maptool.client.swing.table.WordWrapCellRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Permissions;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenPropertiesEditorPanel extends PropertyPane {
  private static final Logger log = LogManager.getLogger();
  private final EditTokenDialog editTokenDialog;
  private final WordWrapCellRenderer wordWrapCellRenderer = new WordWrapCellRenderer();
  private static final String GM = I18N.getText("permission.displayName.gm");
  private static final String GM_SUFFIX = String.format(" (%s)", GM);
  private Token token;
  private TableSearchable searchable;

  public TokenPropertiesEditorPanel(EditTokenDialog editTokenDialog) {
    super(
        new PropertyTable() {
          @Override
          public String getToolTipText(MouseEvent event) {
            String text = super.getToolTipText(event);
            return text != null && text.length() > 100 ? text.substring(0, 100) + " ..." : text;
          }
        },
        1);

    this.editTokenDialog = editTokenDialog;

    setShowDescription(false);
    searchable = SearchableUtils.installSearchable(getPropertyTable());
    searchable.setSearchColumnIndices(new int[] {0, 1});

    SearchableBar searchableBar = new SearchableBar(searchable, true);
    searchableBar.setHighlightAll(true);
    searchableBar.setVisibleButtons(
        SHOW_NAVIGATION
            | SHOW_HIGHLIGHTS
            | SHOW_MATCHCASE
            | SHOW_REPEATS
            | SHOW_STATUS
            | SHOW_WHOLE_WORDS);

    UIManager.getDefaults()
        .entrySet()
        .forEach(
            entry -> {
              if (entry.getKey() instanceof String key) {
                if (key.toLowerCase().contains("table")
                    || key.contains("jide")
                    || key.startsWith("com.")) {
                  System.out.println(key + ":" + entry.getValue());
                }
              }
            });
    UIManager.getDefaults().put("Table.rowHeight", 24);
    // move sort buttons to searchable toolbar
    JideBoxLayout toolbarLayout = (JideBoxLayout) searchableBar.getLayout();
    toolbarLayout.addLayoutComponent(Box.createHorizontalStrut(2), JideBoxLayout.VARY);
    searchableBar.add(new JSeparator(), 0);
    for (Component c : getToolBar().getComponents()) {
      searchableBar.add(c, 0);
    }
    add(searchableBar, BorderLayout.BEFORE_FIRST_LINE);

    getPropertyTable().setModel(new TokenPropertyTableModel());
    getPropertyTable().setFillsViewportHeight(true);
    getPropertyTable().setDisableUneditableCells(false);
    getPropertyTable().setName("propertiesTable");

    /* wrap button and functionality */
    JPanel buttonsAndPropertyTable = new JPanel();
    buttonsAndPropertyTable.setLayout(new BorderLayout());
    JCheckBox wrapToggle = new JCheckBox(I18N.getString("EditTokenDialog.msg.wrap"));
    wrapToggle.addActionListener(
        e -> {
          wordWrapCellRenderer.setLineWrap(wrapToggle.isSelected());
          getPropertyTable().repaint();
        });
    buttonsAndPropertyTable.add(wrapToggle, BorderLayout.PAGE_END);

    buttonsAndPropertyTable.add(this, BorderLayout.CENTER);
    setMinimumSize(new Dimension(300, 200));
    setPreferredSize(new Dimension(-1, -1));
    setVisible(true);
  }

  public void reset(Token token) {}

  /**
   * Updates the property table.
   *
   * @param propertyType the property type of the token (unused).
   */
  protected void updatePropertiesTable(@Nullable Token token, final String propertyType) {
    EventQueue.invokeLater(
        () -> {
          PropertyTable pp = getPropertyTable();
          List<TokenProperty> propertyTypeProperties =
              MapTool.getCampaign().getTokenPropertyList(propertyType);
          pp.setModel(
              new TokenPropertyTableModel(
                  token, propertyType, propertyTypeProperties, wordWrapCellRenderer));
          pp.expandAll();
        });
  }

  protected static class TokenPropertyTableModel
      extends PropertyTableModel<TokenPropertyTableModel.TableTokenProperty>
      implements NavigableModel {

    private final Map<String, String> propertyMap;
    private final ArrayList<TableTokenProperty> gridProperties = new ArrayList<>();
    private Set<String> otherPropertyNames;

    public TokenPropertyTableModel() {
      otherPropertyNames = Set.of();
      propertyMap = Map.of();
    }

    public TokenPropertyTableModel(
        @Nullable Token model,
        String propertyTypeName,
        List<TokenProperty> propertyList,
        WordWrapCellRenderer wordWrapCellRenderer) {
      gridProperties.clear();

      Player player = MapTool.getPlayer();

      Set<String> typePropertyNames =
          propertyList.stream().map(TokenProperty::getName).collect(Collectors.toSet());
      Set<String> otherPropertyNames = Set.of();
      if (model != null) {
        otherPropertyNames = model.getPropertyNamesRaw();
        otherPropertyNames =
            otherPropertyNames.stream()
                .filter(name -> !typePropertyNames.stream().toList().contains(name))
                .collect(Collectors.toSet());
      }

      this.propertyMap = new HashMap<>();

      for (TokenProperty tp : propertyList) {
        String value = null;
        if (model != null) {
          value = (String) model.getProperty(tp.getName());
        }
        this.propertyMap.put(
            tp.getName(), value == null && tp.hasDefaultValue() ? tp.getDefaultValue() : value);

        TableTokenProperty gridProperty = new TableTokenProperty(tp, propertyTypeName);
        gridProperty.setTableCellRenderer(wordWrapCellRenderer);
        gridProperty.setEditable(tp.getEditorEditPermission().hasPermission(player, model));
        gridProperty.setCellEditor(new MTMultilineStringCellEditor());
        gridProperties.add(gridProperty);
      }
      for (String propName : otherPropertyNames) {
        this.propertyMap.put(propName, (String) model.getProperty(propName));

        TableTokenProperty gridProperty = new TableTokenProperty(propName, "", GM);
        gridProperty.setTableCellRenderer(wordWrapCellRenderer);
        gridProperty.setCellEditor(new MTMultilineStringCellEditor());
        gridProperties.add(gridProperty);
      }

      setOriginalProperties(gridProperties);
    }

    public void applyTo(Token token) {
      for (TableTokenProperty tableProp : gridProperties) {
        String value = propertyMap.get(tableProp.getName());
        if (tableProp.hasTokenProperty()) {
          TokenProperty tp = tableProp.getTokenProperty();
          if (tp.getDefaultValue() != null && tp.getDefaultValue().equals(value)) {
            token.setProperty(tableProp.getName(), null); // Clear original value
            continue;
          }
        }
        token.setProperty(tableProp.getName(), value);
      }
    }

    @Override
    public boolean isNavigableAt(int rowIndex, int columnIndex) {
      /* make the property name column non-navigable so that tab takes you directly to the next property value cell. */
      return (columnIndex != 0);
    }

    @Override
    public boolean isNavigationOn() {
      return true;
    }

    class TableTokenProperty extends Property {
      TokenProperty tokenProperty;

      public TableTokenProperty(TokenProperty tokenProperty, String propertyType) {
        this(
            tokenProperty.getName(),
            tokenProperty.getDisplayName(),
            String.class,
            propertyType,
            DEFAULT_CONTEXT,
            null);
        this.tokenProperty = tokenProperty;
        if (tokenProperty.getEditorViewPermission().equals(Permissions.GM)) {
          this.setCategory(this.getCategory() + GM_SUFFIX);
        }
      }

      public TableTokenProperty(String propertyName, String propertyType, String category) {
        this(propertyName, propertyName, String.class, propertyType, DEFAULT_CONTEXT, null);
      }

      public TableTokenProperty(
          String name,
          String displayName,
          Class<?> klass,
          String category,
          ConverterContext converterContext,
          List<Property> children) {
        super(name, "", klass, category, converterContext, children);
        setDisplayName(displayName);
      }

      public boolean hasTokenProperty() {
        return tokenProperty != null;
      }

      public TokenProperty getTokenProperty() {
        return tokenProperty;
      }

      @Override
      public Object getValue() {
        return propertyMap.get(getName());
      }

      @Override
      public void setValue(Object value) {
        propertyMap.put(getName(), (String) value);
      }

      @Override
      public boolean hasValue() {
        return propertyMap.get(getName()) != null;
      }
    }
  }
}
