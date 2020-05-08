package jmri.jmrit.display;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Any common ancestor of Namedbeans having multiple states needs to have multiple
 * displays when represented graphically on a panel. Such Namedbeans may also 
 * have a state controlling feature included in their associated widget.
 * Each state may have either an icon or a text string or both to display.
 * This class gathers common methods for Turnouts, Sensors, SignalHeads, Masts, etc.
 * The class also is used for items that display modes to show status, e.g. IndicatorTrack.
  * IndicatorTrack currently is the only descendant that is not a controlling widget.
 * <p>
 * The flags, _iconDisplay and _textDisplay, determine the current display mode.
 * When both are true, display mode is icon overlaid with text.
 * In text display mode, text and decoration can be individually edited.
 * In Overlay mode, the text is a common (decorated) label for all states.
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @author PeteCressman Copyright (C) 2011, 2018
 */
public class PositionableIcon extends PositionableLabel {

    private HashMap<String, DisplayState> _displayStateMap;
    private String _iconFamily;
    private String _displayState;       // current state or status of the bean/item
    private boolean _iconDisplay;       // Icons display mode 
    private boolean _textDisplay;       // Text display mode
    private boolean _controlling;

    protected static String _redX = "resources/icons/misc/X-red.gif";

    public PositionableIcon(Editor editor) {
        super(editor);
        setName(getClass().getSimpleName());
        _controlling = true;
    }

    public PositionableIcon(NamedIcon s, Editor editor) {
        this(editor);
        setIsIcon(true);
        setIsText(false);
    }

    public PositionableIcon(String s, Editor editor) {
        this(editor);
        setIsIcon(false);
        setIsText(true);
    }

    @Override
    public Positionable deepClone() {
        PositionableIcon pos = new PositionableIcon(_editor);
        return finishClone(pos);
    }
    protected Positionable finishClone(PositionableIcon pos) {
        pos._iconFamily = _iconFamily;
        pos._displayState = _displayState;
        pos._displayStateMap = new HashMap<>();
        Iterator<Entry<String, DisplayState>> it = _displayStateMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, DisplayState> entry = it.next();
            pos._displayStateMap.put(entry.getKey(), entry.getValue().deepClone());
        }
        pos._controlling = _controlling;
        return super.finishClone(pos);
    }

    /**
     * Answers whether icon should be displayed when
     * conditions are normal, i.e. NamedBean is connected
     * and state is known.  Otherwise, super.isIcon() is
     * set to display the abnormal condition. 
     * @return display icon
     */
    @Override
    public boolean isIcon() {
        return _iconDisplay;
    }

    /**
     * Answers whether text should be displayed when
     * conditions are normal, i.e. NamedBean is connected
     * and state is known.  Otherwise, super.isText() is
     * set to display the abnormal condition. 
     * @return display text
     */
    @Override
    public boolean isText() {
        return _textDisplay;
    }
    
    /**
     * Sets the state that should be displayed.
     * @param state state of text and/or icon to display
     */
    protected void setDisplayState(String state) {
        _displayState  = state;
    }
    public String getState() {
        return _displayState;
    }

    public DisplayState getDisplayState(String state) {
        return _displayStateMap.get(state);
    }

    /**
     * Drive the current state of the display due to state change
     *
     * @param state the sensor state
     */
    protected void displayState(String state) {
        log.debug("displayState({})", state);
        if (getNamedBean() == null) {
            setDisconnectedText("BeanDisconnected");
            return;
        }
        DisplayState ds = getDisplayState(state);
        if (ds == null) {
            log.error("Unknown display state {} for {}!", state, getName());
            return;
        }
        restoreConnectionDisplay();

        ds.setDisplayParameters(this);
        _displayState  = state;
        updateSize();
    }

    /**
     * Show bean disconnected.  This may be a temporary condition so
     * save the intended display mode for when case connection is restored.
     * @param text overlay
     */
    protected void setDisconnectedText(String text) {
        log.debug("setDisconnectedText({}) for {}", text, this.getClass().getName());
        setText(Bundle.getMessage(text));
        setIcon(new NamedIcon(_redX, _redX));
        super.setIsText(true);
        super.setIsIcon(true);
        updateSize();
    }

    protected void restoreConnectionDisplay() {
        super.setIsText(_textDisplay);
        super.setIsIcon(_iconDisplay);
    }

    /**
     * Set whether icon should be displayed
     * @param b if true, display icon
     */
    @Override
    public final void setIsIcon(boolean b) {
        _iconDisplay = b;
    }

    /**
     * Set whether text should be displayed
     * @param b if true, display text
     */
    @Override
    public final void setIsText(boolean b) {
        _textDisplay = b;
    }

    protected Collection<String> getStateNameCollection() {
        log.error("getStateNameCollection() must be implemented by extensions!");
        return null;
    }

    /**
     * Make the raw map of display information for each state of the NamedBean.
     * Caller will fill in the data for each state
     */
    protected void makeDisplayMap() {
        Collection<String> col = getStateNameCollection();
        _displayStateMap = new HashMap<>(col.size());
        for (String state : col) {
            DisplayState ds = new DisplayState();
            if (this instanceof SignalMastIcon) {
                ds.setText(state);
            } else {
                ds.setText(Bundle.getMessage(state));
            }
            _displayStateMap.put(state, ds);
        }
        log.debug("makeDisplayMap \"{}\"", getName());
    }

    public HashMap<String, DisplayState> getDisplayStateMap() {
        if (log.isDebugEnabled()) {
            log.debug("getDisplayMap \"{}\"", getName());
            for (Map.Entry<String, DisplayState> e : _displayStateMap.entrySet()) {
                DisplayState ds = e.getValue();
                log.debug("{} state = {}, text= {}, icon= {}", e.getKey(), ds.getText(),
                        (ds.getIcon()==null?"null": ds.getIcon().getName()));
            }
        }
        return _displayStateMap;
    }

    public DisplayState getStateData(String state) {
        return _displayStateMap.get(state);
    }

    /**
     * Verify that there is non-null default text and icon for each state
     * in _displayStateMap
     * @return true if all states are OK
     *
    public boolean isIconMapOK() {
        Iterator<String> iter = getIconStateNames();
        if (iter == null) {
            return false;
        }
        if (isIcon()) {
            while (iter.hasNext()) {
                PositionableLabel pos = _displayStateMap.get(iter.next());
                if (pos.getIcon()== null) {
                    return false;
                }
            }
        } else {
            while (iter.hasNext()) {
                PositionableLabel pos = _displayStateMap.get(iter.next());
                if (pos.getText()== null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get an iterator of state names of the NameBean 
     * as found in jmri.NamedBeanBundle.properties
     */
    public Iterator<String> getStateNames() {
        Collection<String> col = getStateNameCollection();
        if (col == null) {
            log.error("getStateNameCollection not implemented for {}, {}", getName(), getNameString());
            return null;
        }
        return col.iterator();
    }

    /**
     * Get icon by its bean state name key found in
     * jmri.NamedBeanBundle.properties
     * @param state state of the bean
     * @return NamedIcon icon representing the state of the bean
     */
    public NamedIcon getIcon(String state) {
        return _displayStateMap.get(state).getIcon();
    }

    /**
     * Get text by its bean state name key found in
     * jmri.NamedBeanBundle.properties
     * @param state state of the bean
     * @return String text representing the state of the bean
     */
    public String getText(String state) {
        return _displayStateMap.get(state).getText();
    }

    /**
     * Set the icon representing the state of the bean
     * into the state map.
     * @param state state of the bean
     * @param icon NamedIcon representing the state of the bean 
     */
    public void setStateIcon(@Nonnull String state, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("setStateIcon state= \"{}\" icon= {}",
                    state, (icon!=null?icon.getURL():"null"));
        }
        DisplayState p = _displayStateMap.get(state);
        if (p ==null) {
            log.error("Invalid state \"{}\"!", state);
            return;
        }
        p.setIcon(icon);
    }

    /**
     * Set the test representing the state of the bean
     * into the state map.
     * @param state state name of the bean
     * @param text text representing the state of the bean 
     */
    public void setStateText(@Nonnull String state, String text) {
        _displayStateMap.get(state).setText(text);
    }

    /**
     * Set font color for a state of the NamedBean
     * @param state state name of the bean
     * @param color Color for the state
    */
    public void setFontColor(String state, Color color) {
        _displayStateMap.get(state).setForeground(color);
    }

    /**
     * Set font color for a state of the NamedBean
     * @param state state name of the bean
     * @return color for the state
    */
    public Color getFontColor(String state) {
        return _displayStateMap.get(state).getForeground();
    }

    /**
     * Set background color for a state of the NamedBean
     * @param state state name of the bean
     * @param color Color for the state
    */
    public void setBackgroundColor(String state, Color color) {
        _displayStateMap.get(state).setBackground(color);
    }

    /**
     * Set background color for a state of the NamedBean
     * @param state state name of the bean
     * @return color for the state
    */
    public Color getBackgroundColor(String state) {
        return _displayStateMap.get(state).getBackground();
    }

    /**
     * Get the name for the family of icons in the icon map
     * @return name
     */
    public final String getFamily() {
        return _iconFamily;
    }

    /**
     * Set the name for the family of icons in the icon map
     * @param family name
     */
    public void setFamily(String family) {
        _iconFamily = family;
    }

    public void setControlling(boolean enabled) {
        _controlling = enabled;
    }

    public boolean isControlling() {
        return _controlling;
    }

    public boolean setDisableControlMenu(JPopupMenu popup) {
        JCheckBoxMenuItem disableItem = new JCheckBoxMenuItem(Bundle.getMessage("Disable"));
        disableItem.setSelected(!_controlling);
        popup.add(disableItem);
        disableItem.addActionListener((java.awt.event.ActionEvent e) -> {
            setControlling(!disableItem.isSelected());
        });
        return true;
    }

    /**
     * @param popup the menu to display
     * @return true when text is to be displayed
     *
    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        log.debug("setTextEditMenu isIcon={}, isText={}", isIcon(), isText());
        if (isText()) {
            if (!isIcon()) {
                JMenu stateText = new JMenu(Bundle.getMessage("SetSensorText"));
                Iterator<String> iter = getStateNames();
                while (iter.hasNext()) {
                    String state = iter.next();
                    stateText.add(CoordinateEdit.getTextEditAction(this, state));
                }
                popup.add(stateText);
                
                JMenu stateColor = new JMenu(Bundle.getMessage("StateColors"));
                iter = getStateNames();
                while (iter.hasNext()) {
                    stateColor.add(stateColorMenu(iter.next()));
                }
                popup.add(stateColor);
            } else {
                popup.add(CoordinateEdit.getTextEditAction(this, "OverlayText"));
            }
            return true;
        }
        return false;
    }

    /* TODO ???  compare to 4.x to see if above is good enough
    private JMenu stateTextMenu(final String state) {
        JMenu menu = new JMenu(Bundle.getMessage(state));
        return menu;
    }*/

    private JMenu stateColorMenu(final String state) {
        JMenu menu = new JMenu(Bundle.getMessage(state));
        JMenuItem colorMenu = new JMenuItem(Bundle.getMessage("FontColor"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("FontColor"),
                                 getFontColor(state));
            if (desiredColor!=null ) {
                 setFontColor(state, desiredColor);
            }
        });
        menu.add(colorMenu);
        colorMenu = new JMenuItem(Bundle.getMessage("FontBackgroundColor"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("FontBackgroundColor"),
                                 getBackgroundColor(state));
            if (desiredColor!=null ) {
                setBackgroundColor(state, desiredColor);
            }
        });
        menu.add(colorMenu);
        return menu;
    }

    @Override
    public void dispose() {
        _displayStateMap = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableIcon.class);
}
