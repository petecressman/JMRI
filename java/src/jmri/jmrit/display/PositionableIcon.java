package jmri.jmrit.display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Gather common methods for Turnouts, Semsors, SignalHeads, Masts, etc.
 *
 * @author PeteCressman Copyright (C) 2011
 */
public class PositionableIcon extends PositionableLabel {

    protected HashMap<String, NamedIcon> _iconMap;
    protected String _iconFamily;
    protected double _scale = 1.0;          // getScale, come from net result found in one of the icons
    protected int _rotate = 0;

    public PositionableIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/misc/X-red.gif", "resources/icons/misc/X-red.gif"), editor);
    }

    public PositionableIcon(NamedIcon s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
    }

    public PositionableIcon(String s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
    }

    public Positionable deepClone() {
        PositionableIcon pos = new PositionableIcon(_editor);
        return finishClone(pos);
    }
    protected Positionable finishClone(PositionableIcon pos) {
        pos._iconFamily = _iconFamily;
        pos._scale = _scale;
        pos._rotate = _rotate;
        pos._iconMap = cloneMap(_iconMap, pos);
        return super.finishClone(pos);
    }

    /**
     * Get icon by its bean state name key found in
     * jmri.NamedBeanBundle.properties Get icon by its localized bean state name
     */
    public NamedIcon getIcon(String state) {
        return _iconMap.get(state);
    }

    public String getFamily() {
        return _iconFamily;
    }

    public void setFamily(String family) {
        _iconFamily = family;
    }

    public Iterator<String> getIconStateNames() {
        return _iconMap.keySet().iterator();
    }

    public void displayState(int state) {
    }

    public static HashMap<String, NamedIcon> cloneMap(HashMap<String, NamedIcon> map,
            PositionableLabel pos) {
        HashMap<String, NamedIcon> clone = new HashMap<String, NamedIcon>();
        if (map != null) {
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        return clone;
    }

}
