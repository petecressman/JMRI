package jmri.jmrit.display;

import java.awt.Color;
import java.awt.event.ActionEvent;
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
 * displays when represented on graphically on a panel. Such Namedbeans may also 
 * have a state controlling feature included in their associated widget.
 * Each state may have either an icon or a text string or both to display.
 * Gather common methods for Turnouts, Sensors, SignalHeads, Masts, etc.
 * This class also used for items that use display modes to show status, e.g. IndicatorTrack.
 * IndicatorTrack currently is the only descendant that is not a controlling widget.
 * <p>
 * The flags, _iconDisplay and _textDisplay, determine the current display mode.
 * When both are true, display mode is icon overlaid with text.
 * In text display mode, text and decoration can be individually edited.
 * In Overlay mode, the text is a common (decorated) label for all states.
 *
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
        _controlling = true;
        _displayStateMap = makeDefaultMap();
        _displayState = Bundle.getMessage("BeanStateUnknown");
        setName(getClass().getSimpleName());
        if (log.isDebugEnabled()) {
            String name = getClass().getSimpleName();
            setName(name);
            if (_displayStateMap != null) {
                for (Map.Entry<String, DisplayState> e : _displayStateMap.entrySet()) {
                    DisplayState state = e.getValue();
                    log.debug("{} state = {}, text= {}, icon= {}", 
                            name, e.getKey(), state.getText(),
                            (state.getIcon()==null?"null": state.getIcon().getName()));
                }
                
            } else {
                log.debug("{}) null DisplayStateMap", name);
            }
        }
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
    
    /**
     * NamedBean icon items should return a map with the default localized text
     * names for their states.
     * @return mapping of state names as found in jmri.NamedBeanBundle.properties
     */
    protected HashMap<String, DisplayState> makeDefaultMap() {
        return null;
    }
    public HashMap<String, DisplayState> getDisplayStateMap() {
        return _displayStateMap;
    }
    protected void  setIconMap(HashMap<String, DisplayState> map) {
        _displayStateMap = map;
        if (log.isDebugEnabled()) {
            for (Map.Entry<String, DisplayState> e : _displayStateMap.entrySet()) {
                DisplayState pos = e.getValue();
                log.debug("state = {}, text= {}, icon= {}", e.getKey(), pos.getText(),
                        (pos.getIcon()==null?"null": pos.getIcon().getName()));
            }
        }
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
        if (_displayStateMap.keySet() == null) {
            return null;
        }
        return _displayStateMap.keySet().iterator();
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

    //////// popup Menu method overrides ////////

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

/*
    public static HashMap<String, PositionableLabel> cloneMap(HashMap<String, PositionableLabel> map,
            PositionableIcon pos) {
        HashMap<String, PositionableLabel> clone = new HashMap<>();
        if (map != null) {
            Iterator<Entry<String, PositionableLabel>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, PositionableLabel> entry = it.next();
                clone.put(entry.getKey(), (PositionableLabel)entry.getValue().deepClone());
            }
        }
        return clone;
    }*/

    private final static Logger log = LoggerFactory.getLogger(PositionableIcon.class);
}
