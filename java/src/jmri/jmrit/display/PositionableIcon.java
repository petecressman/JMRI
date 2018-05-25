package jmri.jmrit.display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import jmri.jmrit.catalog.NamedIcon;
/**
 * Gather common methods for Turnouts, Sensors, SignalHeads, Masts, etc.
 * The common ancestor of Namedbeans having multiple states and displays
 * each state as either an icon or a decorated text string.
 *
 * @author PeteCressman Copyright (C) 2011, 2018
 */
public class PositionableIcon extends PositionableLabel {

    protected HashMap<String, PositionableLabel> _iconMap;
    protected String _iconFamily;
    protected boolean _control;

    public PositionableIcon(Editor editor) {
        super(editor);
        _control = true;
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
        pos._iconMap = cloneMap(_iconMap, pos);
        pos._control = _control;
        return super.finishClone(pos);
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
    }

    /**
     * Set the test representing the state of the bean
     * into the state map.
     * @param state state of the bean
     * @param text text representing the state of the bean 
     */
    public void setStateText(@Nonnull String state, String text) {
        _iconMap.get(state).setText(text);
    }

    /**
     * Get the name for the family of icons in the icon map
     * @return name
     */
    public String getFamily() {
        return _iconFamily;
    }

    /**
     * Set the name for the family of icons in the icon map
     * @param family name
     */
    public void setFamily(String family) {
        _iconFamily = family;
    }

    public Iterator<String> getIconStateNames() {
        return _iconMap.keySet().iterator();
    }

    public void displayState(int state) {
    }

    ///////////////////////////// popup methods ////////////////////////////

    @Override
    public boolean setDisableControlMenu(JPopupMenu popup) {
        if (_control) {
            JCheckBoxMenuItem disableItem = new JCheckBoxMenuItem(Bundle.getMessage("Disable"));
            disableItem.setSelected(!isControlling());
            popup.add(disableItem);
            disableItem.addActionListener((java.awt.event.ActionEvent e) -> {
                setControlling(!disableItem.isSelected());
            });
            return true;
        }
        return false;
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

}
