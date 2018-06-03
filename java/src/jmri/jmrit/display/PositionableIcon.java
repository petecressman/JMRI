package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The common ancestor of Namedbeans having multiple states and displays
 * each state as either an icon or a decorated text string.
 * Gather common methods for Turnouts, Sensors, SignalHeads, Masts, etc.
  *
 * @author PeteCressman Copyright (C) 2011, 2018
 */
public class PositionableIcon extends PositionableLabel {

    private HashMap<String, PositionableLabel> _iconMap;
    private String _iconFamily;
    private String _displayState;


    public PositionableIcon(Editor editor) {
        super(editor);
        _control = true;
        _iconMap = makeDefaultMap();
        _displayState = Bundle.getMessage("BeanStateUnknown");
    }

    public PositionableIcon(NamedIcon s, Editor editor) {
        super(editor);
        setIsIcon(true);
        setIsText(false);
    }

    public PositionableIcon(String s, Editor editor) {
        super(editor);
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
        pos._iconMap = cloneMap(_iconMap, pos);
        return super.finishClone(pos);
    }

    /**
     * Sets the state that should be displayed.
     * @param state state of text and/or icon to display
     */
    protected void setDisplayState(String state) {
        _displayState  = state;
    }

    /**
     * NamedBean icon items should return a map with the default localized text
     * names for their states.
     * @return mapping of state names as found in jmri.NamedBeanBundle.properties
     */
    protected HashMap<String, PositionableLabel> makeDefaultMap() {
        return null;
    }
    public HashMap<String, PositionableLabel> getIconMap() {
        return _iconMap;
    }
    protected void  setIconMap(HashMap<String, PositionableLabel> map) {
        _iconMap = map;
    }
    public PositionableLabel getStateData(String state) {
        return _iconMap.get(state);
    }

    /**
     * Verify that there is non-null default text and icon for each state
     * in _iconMap
     * @return true if all states are OK
     */
    public boolean isIconMapOK() {
        Iterator<String> iter = getIconStateNames();
        if (iter == null) {
            return false;
        }
        if (isIcon()) {
            while (iter.hasNext()) {
                PositionableLabel pos = _iconMap.get(iter.next());
                if (pos.getIcon()== null) {
                    return false;
                }
            }
        } else {
            while (iter.hasNext()) {
                PositionableLabel pos = _iconMap.get(iter.next());
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
     * @return Iterator for the state names
     */
    public Iterator<String> getIconStateNames() {
        if (_iconMap.keySet() == null) {
            return null;
        }
        return _iconMap.keySet().iterator();
    }

    /**
     * Get icon by its bean state name key found in
     * jmri.NamedBeanBundle.properties
     * @param state state of the bean
     * @return NamedIcon icon representing the state of the bean
     */
    public NamedIcon getIcon(String state) {
        return _iconMap.get(state).getIcon();
    }

    /**
     * Get text by its bean state name key found in
     * jmri.NamedBeanBundle.properties
     * @param state state of the bean
     * @return String text representing the state of the bean
     */
    public String getText(String state) {
        return _iconMap.get(state).getText();
    }

    /**
     * Set the icon representing the state of the bean
     * into the state map.
     * @param state state of the bean
     * @param icon NamedIcon representing the state of the bean 
     */
    public void setStateIcon(@Nonnull String state, NamedIcon icon) {
        _iconMap.get(state).setIcon(icon);
        _iconMap.get(state).setIsIcon(icon != null);
    }

    /**
     * Set the test representing the state of the bean
     * into the state map.
     * @param state state name of the bean
     * @param text text representing the state of the bean 
     */
    public void setStateText(@Nonnull String state, String text) {
        _iconMap.get(state).setText(text);
        _iconMap.get(state).setIsText(text != null);
    }

    /**
     * Set font color for a state of the NamedBean
     * @param state state name of the bean
     * @param color Color for the state
    */
    public void setFontColor(String state, Color color) {
        _iconMap.get(state).setForeground(color);
    }

    /**
     * Set font color for a state of the NamedBean
     * @param state state name of the bean
     * @return color for the state
    */
    public Color getFontColor(String state) {
        return _iconMap.get(state).getForeground();
    }

    /**
     * Set background color for a state of the NamedBean
     * @param state state name of the bean
     * @param color Color for the state
    */
    public void setBackgroundColor(String state, Color color) {
        _iconMap.get(state).setBackground(color);
    }

    /**
     * Set background color for a state of the NamedBean
     * @param state state name of the bean
     * @return color for the state
    */
    public Color getBackgroundColor(String state) {
        return _iconMap.get(state).getBackground();
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
    public final void setFamily(String family) {
        _iconFamily = family;
    }

    //////// popup AbstractAction.actionPerformed method overrides ////////
    /**
     * @param popup the menu to display
     * @return always true
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            if (isIcon()) {
                popup.add(new AbstractAction(Bundle.getMessage("ChangeToText")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeLayoutSensorType();
                    }
                });
            } else {
                popup.add(new AbstractAction(Bundle.getMessage("ChangeToIcon")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeLayoutSensorType();
                    }
                });
            }
        } else {
            setAdditionalViewPopUpMenu(popup);
        }
        return true;
    }

    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        log.debug("setTextEditMenu isIcon={}, isText={}", isIcon(), isText());
        if (isIcon()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "OverlayText"));
        } else if (isText()) {
            JMenu stateText = new JMenu(Bundle.getMessage("SetSensorText"));
            Iterator<String> iter = getIconStateNames();
            while (iter.hasNext()) {
                stateText.add(CoordinateEdit.getTextEditAction(_iconMap.get(iter.next()), "OverlayText"));  //temp
            }
            
            JMenu stateColor = new JMenu(Bundle.getMessage("StateColors"));
            iter = getIconStateNames();
            while (iter.hasNext()) {
                stateColor.add(stateColorMenu(iter.next()));
            }
        }
        return true;
    }

    /// TODO!!!
    private JMenu stateTextMenu(final String state) {
        JMenu menu = new JMenu(Bundle.getMessage(state));
        return menu;
    }

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

    void changeLayoutSensorType() {
        if (isIcon()) {
            setIsIcon(false);
            setIsText(true);
            setIcon(null);
        } else if (isText()) {
            setIsIcon(true);
            setIsText(false);
            setText(null);
        }
    }

    @Override
    public void displayState() {
    }

    @Override
    public void dispose() {
        _iconMap = null;
        super.dispose();
    }

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
    }

    @Override
    public void paintComponent(Graphics g) {
        PositionableLabel pos = _iconMap.get(_displayState);
        pos.paintComponent(g);
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableIcon.class);
}
