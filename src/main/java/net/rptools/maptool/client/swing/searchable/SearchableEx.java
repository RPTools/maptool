package net.rptools.maptool.client.swing.searchable;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.swing.Searchable;
import com.jidesoft.swing.SearchableProvider;
import com.jidesoft.swing.event.SearchableEvent;
import com.jidesoft.swing.event.SearchableListener;
import com.jidesoft.utils.DefaultWildcardSupport;
import com.jidesoft.utils.WildcardSupport;
import net.rptools.maptool.language.I18N;
import org.bouncycastle.util.Strings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

/**
 * This is just reconstituted JideSearchable
 */
public abstract class SearchableEx {
    public static final String CLIENT_PROPERTY_SEARCHABLE = "SearchableEx";
    private final PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
    protected final JComponent _component;
    private SearchableProvider _searchableProvider;
    private Pattern _pattern;
    private String _searchText;
    private String _previousSearchText;
    private boolean _fromStart = true;
    private boolean _caseSensitive = false;
    private boolean _repeats = true;
    private boolean _wildcardEnabled = true;
    private boolean _countMatch;
    protected int _matchCount;
    private WildcardSupport _wildcardSupport = null;
    private Color _mismatchForeground;
    private Color _foreground = null;
    private Color _background = null;
    protected ComponentListener _componentListener;
    protected KeyListener _keyListener;
    protected FocusListener _focusListener;
    private SearchableListener _searchableListener;
    private int _cursor = -1;
    private String _searchLabel = I18N.getText("Button.find");
    private int _searchingDelay = 0;
    private boolean _reverseOrder = false;
    protected EventListenerList listenerList = new EventListenerList();
    private final Set<Integer> _selection;
    private boolean _processModelChangeEvent = true;
    private SearchTextField _searchTextField = null;

    public SearchableEx(JComponent _component) {
        this._component = _component;
        SearchableEx searchable = getSearchable(_component);
        if (searchable != null) {
            searchable.uninstallSearchable(searchable);
        }

        this._previousSearchText = null;
        this._selection = new HashSet<>();
        this.installListeners();
        this.updateClientProperty(this._component, this);
    }

    @SuppressWarnings("unused")
    public SearchableEx(JComponent _component, SearchableProvider searchableProvider) {
        SearchableEx searchable = getSearchable(_component);
        if (searchable != null) {
            searchable.uninstallSearchable(searchable);
        }

        this._searchableProvider = searchableProvider;
        this._previousSearchText = null;
        this._component = _component;
        this._selection = new HashSet<>();
        this.installListeners();
        this.updateClientProperty(this._component, this);
    }

    protected abstract int getSelectedIndex();

    protected abstract void setSelectedIndex(int index, boolean incremental);

    @SuppressWarnings("unused")
    public void adjustSelectedIndex(int index, boolean incremental) {
        this.setSelectedIndex(index, incremental);
    }

    protected abstract int getElementCount();

    protected abstract Object getElementAt(int index);

    protected abstract String convertElementToString(Object element);

    @SuppressWarnings("unused")
    public String convertToString(Object element) {
        return this.convertElementToString(element);
    }

    @SuppressWarnings("unused")
    public SearchableProvider getSearchableProvider() {
        return this._searchableProvider;
    }

    public void setSearchableProvider(SearchableProvider searchableProvider) {
        this._searchableProvider = searchableProvider;
    }

    protected ComponentListener createComponentListener() {
        return new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                boolean passive = SearchableEx.this._searchableProvider == null || SearchableEx.this._searchableProvider.isPassive();
            }

            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            }

            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
            }
        };
    }

    public void installListeners() {
        if (this._componentListener == null) {
            this._componentListener = this.createComponentListener();
        }

        this._component.addComponentListener(this._componentListener);
        Component scrollPane = JideSwingUtilities.getScrollPane(this._component);
        if (scrollPane != null) {
            scrollPane.addComponentListener(this._componentListener);
        }

        if (this._keyListener == null) {
            this._keyListener = this.createKeyListener();
        }

        JideSwingUtilities.insertKeyListener(this.getComponent(), this._keyListener, 0);
        if (this._focusListener == null) {
            this._focusListener = this.createFocusListener();
        }

        this.getComponent().addFocusListener(this._focusListener);
        if (this._searchableListener == null) {
            this._searchableListener = e -> {
            };
        }

        this.addSearchableListener(this._searchableListener);
    }

    protected KeyListener createKeyListener() {
        return new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                boolean passive = SearchableEx.this._searchableProvider == null || SearchableEx.this._searchableProvider.isPassive();
                if (passive) {
                    SearchableEx.this.keyTypedOrPressed(e);
                }

            }

            public void keyPressed(KeyEvent e) {
                boolean passive = SearchableEx.this._searchableProvider == null || SearchableEx.this._searchableProvider.isPassive();
                if (passive) {
                    SearchableEx.this.keyTypedOrPressed(e);
                }

            }
        };
    }

    protected FocusListener createFocusListener() {
        return new FocusAdapter() {
            public void focusLost(FocusEvent focusevent) {
                boolean passive = SearchableEx.this._searchableProvider == null
                        || SearchableEx.this._searchableProvider.isPassive();
            }
        };
    }

    private void updateClientProperty(JComponent component, SearchableEx searchable) {
        if (component != null) {
            Object clientProperty = this._component.getClientProperty(CLIENT_PROPERTY_SEARCHABLE);
            if (clientProperty instanceof Searchable) {
                ((Searchable) clientProperty).uninstallListeners();
            }

            component.putClientProperty(CLIENT_PROPERTY_SEARCHABLE, searchable);
        }

    }

    public void uninstallListeners() {
        if (this._componentListener != null) {
            this.getComponent().removeComponentListener(this._componentListener);
            Component scrollPane = JideSwingUtilities.getScrollPane(this.getComponent());
            if (scrollPane != null) {
                scrollPane.removeComponentListener(this._componentListener);
            }
            this._componentListener = null;
        }

        if (this._keyListener != null) {
            this.getComponent().removeKeyListener(this._keyListener);
            this._keyListener = null;
        }

        if (this._focusListener != null) {
            this.getComponent().removeFocusListener(this._focusListener);
            this._focusListener = null;
        }

        if (this._searchableListener != null) {
            this.removeSearchableListener(this._searchableListener);
            this._searchableListener = null;
        }

    }

    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        this._propertyChangeSupport.addPropertyChangeListener(propertychangelistener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        this._propertyChangeSupport.removePropertyChangeListener(propertychangelistener);
    }

    public void firePropertyChangeEvent(String searchingText) {
        if (!searchingText.equals(this._previousSearchText)) {
            this._propertyChangeSupport.firePropertyChange("searchText", this._previousSearchText, searchingText);
            this.fireSearchableEvent(new SearchableEvent(this, 3004, searchingText, this.getCurrentIndex(), this._previousSearchText));
            this._previousSearchText = searchingText;
            if (searchingText.isBlank()) {
                this.searchingTextEmpty();
            }
        }
    }

    protected void searchingTextEmpty() {
    }

    protected boolean compare(Object element, String lookFor) {
        return compare(element, Arrays.asList(Strings.split(lookFor, ' ')));
    }

    protected boolean compare(Object element, List<String> lookForList) {
        final String lookAt = this.isCaseSensitive() ? this.convertElementToString(element) : this.convertElementToString(element).toLowerCase();
        if (lookAt == null) {
            return false;
        }
        for (String lookFor : lookForList) {
            if (!this.compare(lookAt, lookFor)) {
                return false;
            }
        }
        return true;
    }

    protected boolean compare(String lookAt, String lookFor) {
        if (lookFor != null && !lookFor.isBlank()) {
            if (this.isWildcardEnabled()) {
                if (this._searchText != null && this._searchText.equals(lookFor) && this._pattern != null) {
                    return this._pattern.matcher(lookAt).find();
                } else {
                    WildcardSupport wildcardSupport = this.getWildcardSupport();
                    String s = wildcardSupport.convert(lookFor);
                    if (lookFor.equals(s)) {
                        return this.isFromStart() ? lookAt.startsWith(lookFor) : lookAt.contains(lookFor);
                    } else {
                        this._searchText = lookFor;
                        try {
                            this._pattern = Pattern.compile(this.isFromStart() ? "^" + s : s, this.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
                            return this._pattern.matcher(lookAt).find();
                        } catch (PatternSyntaxException pse) {
                            return false;
                        }
                    }
                }
            } else {
                filter:
                {
                    if (lookFor.equals(lookAt)) {
                        break filter;
                    }
                    if (lookFor.isBlank()) {
                        if (this.isFromStart()) {
                            if (lookAt.startsWith(lookFor)) {
                                break filter;
                            }
                        } else if (lookAt.contains(lookFor)) {
                            break filter;
                        }
                    }
                    return false;
                }
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean isFromStart() {
        return this._fromStart;
    }

    @SuppressWarnings("unused")
    public void setFromStart(boolean fromStart) {
        this._fromStart = fromStart;
    }

    public int getCursor() {
        return this._cursor;
    }

    public void setCursor(int cursor) {
        this.setCursor(cursor, false);
    }

    public void setCursor(int cursor, boolean incremental) {
        if (!incremental || this._cursor < 0) {
            this._selection.clear();
        }

        if (this._cursor >= 0) {
            this._selection.add(cursor);
        }

        this._cursor = cursor;
    }

    protected void highlightAll() {
        int firstIndex = -1;
        int index = this.getSelectedIndex();
        String text = this.getSearchingText();

        while (index != -1) {
            int newIndex = this.findNext(text);
            if (index == newIndex) {
                index = -1;
            } else {
                index = newIndex;
            }

            if (index != -1) {
                if (firstIndex == -1) {
                    firstIndex = index;
                }

                this.select(index, text);
            }
        }

        if (firstIndex != -1) {
            this.select(firstIndex, text);
        }

    }

    protected void cancelHighlightAll() {
    }

    protected void select(int index, String searchingText) {
        if (index != -1) {
            this.setSelectedIndex(index, true);
            this.setCursor(index, true);
            Object element = this.getElementAt(index);
            this.fireSearchableEvent(new SearchableEvent(this, 3002, searchingText, element, this.convertElementToString(element)));
        } else {
            this.setSelectedIndex(-1, false);
            this.fireSearchableEvent(new SearchableEvent(this, 3003, searchingText));
        }

    }

    public int findNext(String s) {
        String str = this.isCaseSensitive() ? s : s.toLowerCase();
        int count = this.getElementCount();
        if (count == 0) {
            return s.isBlank() ? -1 : 0;
        } else {
            int selectedIndex = this.getCurrentIndex();

            for (int i = selectedIndex + 1; i < count; ++i) {
                Object element = this.getElementAt(i);
                if (this.compare(element, str)) {
                    return i;
                }
            }

            if (this.isRepeats()) {
                for (int i = 0; i < selectedIndex; ++i) {
                    Object element = this.getElementAt(i);
                    if (this.compare(element, str)) {
                        return i;
                    }
                }
            }

            return selectedIndex == -1 ? -1 : (this.compare(this.getElementAt(selectedIndex), str) ? selectedIndex : -1);
        }
    }

    protected int getCurrentIndex() {
        if (this._selection.contains(this.getSelectedIndex())) {
            return this._cursor != -1 ? this._cursor : this.getSelectedIndex();
        } else {
            this._selection.clear();
            return this.getSelectedIndex();
        }
    }

    public int findPrevious(String s) {
        String str = this.isCaseSensitive() ? s : s.toLowerCase();
        int count = this.getElementCount();
        if (count == 0) {
            return s.isBlank() ? -1 : 0;
        } else {
            int selectedIndex = this.getCurrentIndex();

            for (int i = selectedIndex - 1; i >= 0; --i) {
                Object element = this.getElementAt(i);
                if (this.compare(element, str)) {
                    return i;
                }
            }

            if (this.isRepeats()) {
                for (int i = count - 1; i >= selectedIndex; --i) {
                    Object element = this.getElementAt(i);
                    if (this.compare(element, str)) {
                        return i;
                    }
                }
            }

            return selectedIndex == -1 ? -1 : (this.compare(this.getElementAt(selectedIndex), str) ? selectedIndex : -1);
        }
    }

    public int findFromCursor(String s) {
        if (this.isCountMatch()) {
            boolean reverse = this.isReverseOrder();
            this.setReverseOrder(false);
            int selectedIndex = this.getCurrentIndex();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }

            int newIndex = -1;
            this._matchCount = -1;

            int oldIndex;
            do {
                this.setSelectedIndex(newIndex, false);
                oldIndex = newIndex;
                newIndex = this.findNext(s);
                ++this._matchCount;
            } while (newIndex > oldIndex);

            this.setSelectedIndex(selectedIndex, false);
            this.setReverseOrder(reverse);
        }

        if (this.isReverseOrder()) {
            return this.reverseFindFromCursor(s);
        } else {
            String str = this.isCaseSensitive() ? s : s.toLowerCase();
            int selectedIndex = this.getCurrentIndex();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }

            int count = this.getElementCount();
            if (count != 0) {
                for (int i = selectedIndex; i < count; ++i) {
                    Object element = this.getElementAt(i);
                    if (this.compare(element, str)) {
                        return i;
                    }
                }

                for (int i = 0; i < selectedIndex; ++i) {
                    Object element = this.getElementAt(i);
                    if (this.compare(element, str)) {
                        return i;
                    }
                }

            }
            return -1;
        }
    }

    public int reverseFindFromCursor(String s) {
        if (!this.isReverseOrder()) {
            return this.findFromCursor(s);
        } else {
            String str = this.isCaseSensitive() ? s : s.toLowerCase();
            int selectedIndex = this.getCurrentIndex();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }

            int count = this.getElementCount();
            if (count != 0) {
                for (int i = selectedIndex; i >= 0; --i) {
                    Object element = this.getElementAt(i);
                    if (this.compare(element, str)) {
                        return i;
                    }
                }

                for (int i = count - 1; i >= selectedIndex; --i) {
                    Object element = this.getElementAt(i);
                    if (this.compare(element, str)) {
                        return i;
                    }
                }

            }
            return -1;
        }
    }

    public int findFirst(String s) {
        String str = this.isCaseSensitive() ? s : s.toLowerCase();
        int count = this.getElementCount();
        if (count == 0) {
            return s.isBlank() ? -1 : 0;
        } else {
            for (int i = 0; i < count; ++i) {
                int index = this.getIndex(count, i);
                Object element = this.getElementAt(index);
                if (this.compare(element, str)) {
                    return index;
                }
            }

            return -1;
        }
    }

    @SuppressWarnings("unused")
    public int findLast(String s) {
        String str = this.isCaseSensitive() ? s : s.toLowerCase();
        int count = this.getElementCount();
        if (count == 0) {
            return s.isBlank() ? -1 : 0;
        } else {
            for (int i = count - 1; i >= 0; --i) {
                Object element = this.getElementAt(i);
                if (this.compare(element, str)) {
                    return i;
                }
            }

            return -1;
        }
    }

    protected void keyTypedOrPressed(KeyEvent e) {
        if (this._searchableProvider != null && this._searchableProvider.isPassive()) {
            this._searchableProvider.processKeyEvent(e);
        } else {
            if (this.isActivateKey(e)) {
                String searchingText = "";
                if (e.getID() == 400) {
                    if (JideSwingUtilities.isMenuShortcutKeyDown(e)) {
                        return;
                    }
                    if (e.isAltDown()) {
                        return;
                    }
                    searchingText = String.valueOf(e.getKeyChar());
                }
                if (e.getKeyCode() != 10) {
                    e.consume();
                }
            }
        }
    }

    private int getIndex(int count, int index) {
        return this.isReverseOrder() ? count - index - 1 : index;
    }

    public String getSearchingText() {
        return this._searchableProvider != null ? this._searchableProvider.getSearchingText() : "";
    }

    protected boolean isFindFirstKey(KeyEvent e) {
        return e.getKeyCode() == 36;
    }

    protected boolean isFindLastKey(KeyEvent e) {
        return e.getKeyCode() == 35;
    }

    protected boolean isFindPreviousKey(KeyEvent e) {
        return e.getKeyCode() == 38;
    }

    protected boolean isFindNextKey(KeyEvent e) {
        return e.getKeyCode() == 40;
    }

    protected boolean isNavigationKey(KeyEvent e) {
        return this.isFindFirstKey(e) || this.isFindLastKey(e) || this.isFindNextKey(e) || this.isFindPreviousKey(e);
    }

    protected boolean isActivateKey(KeyEvent e) {
        char keyChar = e.getKeyChar();
        return e.getID() == 400 && keyChar > ' ' && keyChar != 127;
    }

    protected boolean isDeactivateKey(KeyEvent e) {
        int keyCode = e.getKeyCode();
        return keyCode == 10 || keyCode == 27 || keyCode == 33 || keyCode == 34 || keyCode == 36 || keyCode == 35 || keyCode == 37 || keyCode == 39 || keyCode == 38 || keyCode == 40;
    }

    protected boolean isSelectAllKey(KeyEvent e) {
        return JideSwingUtilities.isMenuShortcutKeyDown(e) && e.getKeyCode() == 65;
    }

    @SuppressWarnings("unused")
    protected boolean isIncrementalSelectKey(KeyEvent e) {
        return JideSwingUtilities.isMenuShortcutKeyDown(e);
    }

    @SuppressWarnings("unused")
    public Color getMismatchForeground() {
        return this._mismatchForeground == null ? Color.RED : this._mismatchForeground;
    }

    @SuppressWarnings("unused")
    public void setMismatchForeground(Color mismatchForeground) {
        this._mismatchForeground = mismatchForeground;
    }

    public boolean isCaseSensitive() {
        return this._caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this._caseSensitive = caseSensitive;
    }

    public int getSearchingDelay() {
        return this._searchingDelay;
    }

    @SuppressWarnings("unused")
    public void setSearchingDelay(int searchingDelay) {
        this._searchingDelay = searchingDelay;
    }

    @SuppressWarnings("unused")
    public boolean isRepeats() {
        return this._repeats;
    }

    public void setRepeats(boolean repeats) {
        this._repeats = repeats;
    }

    public Color getForeground() {
        return this._foreground == null ? UIDefaultsLookup.getColor("ToolTip.foreground") : this._foreground;
    }

    public void setForeground(Color foreground) {
        this._foreground = foreground;
    }

    public Color getBackground() {
        return this._background == null ? UIDefaultsLookup.getColor("ToolTip.background") : this._background;
    }

    public void setBackground(Color background) {
        this._background = background;
    }

    public boolean isWildcardEnabled() {
        return this._wildcardEnabled;
    }

    public void setWildcardEnabled(boolean wildcardEnabled) {
        this._wildcardEnabled = wildcardEnabled;
    }

    public WildcardSupport getWildcardSupport() {
        if (this._wildcardSupport == null) {
            this._wildcardSupport = new DefaultWildcardSupport();
        }

        return this._wildcardSupport;
    }

    @SuppressWarnings("unused")
    public void setWildcardSupport(WildcardSupport wildcardSupport) {
        this._wildcardSupport = wildcardSupport;
    }

    @SuppressWarnings("unused")
    public String getSearchLabel() {
        return this._searchLabel;
    }

    public void setSearchLabel(String searchLabel) {
        this._searchLabel = searchLabel;
    }

    public void addSearchableListener(SearchableListener l) {
        this.listenerList.add(SearchableListener.class, l);
    }

    public void removeSearchableListener(SearchableListener l) {
        this.listenerList.remove(SearchableListener.class, l);
    }

    public SearchableListener[] getSearchableListeners() {
        return this.listenerList.getListeners(SearchableListener.class);
    }

    @SuppressWarnings("unused")
    public boolean isSearchableListenerInstalled(SearchableListener l) {
        SearchableListener[] listeners = this.getSearchableListeners();

        for (SearchableListener listener : listeners) {
            if (listener == l) {
                return true;
            }
        }

        return false;
    }

    protected void fireSearchableEvent(SearchableEvent e) {
        Object[] listeners = this.listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SearchableListener.class) {
                ((SearchableListener) listeners[i + 1]).searchableEventFired(e);
            }
        }

    }

    public Component getComponent() {
        return this._component;
    }


    public boolean isReverseOrder() {
        return this._reverseOrder;
    }

    public void setReverseOrder(boolean reverseOrder) {
        this._reverseOrder = reverseOrder;
    }

    public static SearchableEx getSearchable(JComponent component) {
        Object clientProperty = component.getClientProperty(CLIENT_PROPERTY_SEARCHABLE);
        return clientProperty instanceof SearchableEx ? (SearchableEx) clientProperty : null;
    }


    public boolean isProcessModelChangeEvent() {
        return this._processModelChangeEvent;
    }

    public void setProcessModelChangeEvent(boolean processModelChangeEvent) {
        this._processModelChangeEvent = processModelChangeEvent;
    }

    public boolean isCountMatch() {
        return this._countMatch;
    }

    public void setCountMatch(boolean countMatch) {
        this._countMatch = countMatch;
    }

    int getMatchCount() {
        return this._matchCount;
    }

    @SuppressWarnings("unused")
    public List<Integer> findAll(String s) {
        String str = this.isCaseSensitive() ? s : s.toLowerCase();
        List<Integer> list = new ArrayList<>();
        int i = 0;

        for (int count = this.getElementCount(); i < count; ++i) {
            Object elementAt = this.getElementAt(i);
            if (this.compare(elementAt, str)) {
                list.add(i);
            }
        }

        return list;
    }

    @SuppressWarnings("unused")
    public String getElementAtAsString(int index) {
        return this.convertElementToString(this.getElementAt(index));
    }

    @SuppressWarnings("unused")
    protected void textChanged(String text) {
        if (text != null && !text.isBlank()) {
            int found = this.findFromCursor(text);
            if (found == -1) {
                this.firePropertyChangeEvent(text);
                this.fireSearchableEvent(new SearchableEvent(this, 3003, text));
            } else {
                this.firePropertyChangeEvent(text);
                Object element = this.getElementAt(found);
                this.fireSearchableEvent(new SearchableEvent(this, 3002, text, element, this.convertElementToString(element)));
            }
        } else {
            this.firePropertyChangeEvent("");
        }
    }

    @SuppressWarnings("unused")
    public int findFirstExactly(String s) {
        String str = this.isCaseSensitive() ? s : s.toLowerCase();
        int count = this.getElementCount();
        if (count == 0) {
            return s.isBlank() ? -1 : 0;
        } else {
            for (int i = 0; i < count; ++i) {
                int index = this.getIndex(count, i);
                Object element = this.getElementAt(index);
                String text = this.convertElementToString(element);
                if (JideSwingUtilities.equals(text, str)) {
                    return index;
                }
            }
            return -1;
        }
    }

    protected SearchTextField getSearchField() {
        if (this._searchTextField == null) {
            this._searchTextField = new SearchTextField();
        }
        return this._searchTextField;
    }

    protected class SearchTextField extends FlatTextField {
        SearchTextField() {
            super();
            setPlaceholderText("^" + I18N.getText("Label.macroPermissions") + " *");
            setColumns(24);

            setShowClearButton(true);
            JideSwingUtilities.setComponentTransparent(this);
        }

        public Dimension getMinimumSize() {
            Dimension size = super.getPreferredSize();
            size.width = this.getFontMetrics(this.getFont()).stringWidth(this.getPlaceholderText()) + 24;
            return size;
        }

        public void processKeyEvent(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == 8 && this.getDocument().getLength() == 0) {
                e.consume();
            } else {
                boolean isNavigationKey = SearchableEx.this.isNavigationKey(e);
                if (SearchableEx.this.isDeactivateKey(e) && !isNavigationKey) {
                    if (keyCode == 27) {
                        e.consume();
                    }
                } else {
                    super.processKeyEvent(e);
                    if (keyCode == 8 || isNavigationKey) {
                        e.consume();
                    }
                    if (SearchableEx.this.isSelectAllKey(e)) {
                        e.consume();
                    }
                }
            }
        }
    }

    private void uninstallSearchable(SearchableEx searchable) {
        if (searchable != null) {
            searchable.uninstallListeners();
            if (searchable.getComponent() instanceof JComponent) {
                Object clientProperty = ((JComponent) searchable.getComponent()).getClientProperty(CLIENT_PROPERTY_SEARCHABLE);
                if (clientProperty == searchable) {
                    ((JComponent) searchable.getComponent()).putClientProperty(CLIENT_PROPERTY_SEARCHABLE, null);
                }
            }
        }
    }
}
