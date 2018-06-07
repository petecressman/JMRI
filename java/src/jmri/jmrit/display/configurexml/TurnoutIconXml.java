package jmri.jmrit.display.configurexml;

import java.util.List;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.TurnoutIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class TurnoutIconXml extends PositionableIconXml {

    /**
     * Default implementation for storing the contents of a TurnoutIcon
     *
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        TurnoutIcon p = (TurnoutIcon) o;
        Element element = new Element("turnouticon");
        if (!storePositionableIcon(element, p)) {
            return null;
        }
        element.setAttribute("turnout", p.getNamedTurnout().getName());

        element.setAttribute("tristate", p.getTristate() ? "true" : "false");
        element.setAttribute("momentary", p.getMomentary() ? "true" : "false");
        element.setAttribute("directControl", p.getDirectControl() ? "true" : "false");

        element.setAttribute("class", "jmri.jmrit.display.configurexml.TurnoutIconXml");
        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @SuppressWarnings("null")
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;
        TurnoutIcon l = new TurnoutIcon(ed);
        String name;
        try {
            name = element.getAttribute("turnout").getValue();
        } catch (NullPointerException e) {
            log.error("incorrect information for turnout; must use turnout name");
            ed.loadFailed();
            return;
        }
        if (!loadPositionableIcon(element, l)) {
            loadPre50(element, l, name);
        }

        l.setTurnout(name);

        Attribute a = element.getAttribute("tristate");
        if ((a == null) || a.getValue().equals("true")) {
            l.setTristate(true);
        } else {
            l.setTristate(false);
        }

        a = element.getAttribute("momentary");
        if ((a != null) && a.getValue().equals("true")) {
            l.setMomentary(true);
        } else {
            l.setMomentary(false);
        }

        a = element.getAttribute("directControl");
        if ((a != null) && a.getValue().equals("true")) {
            l.setDirectControl(true);
        } else {
            l.setDirectControl(false);
        }


        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }

    /*
     * pre release 5.0 or something like that
     */
    static protected  void loadPre50(Element element, PositionableIcon l, String name) {
        Editor ed = l.getEditor();

        try {
            int rotation = element.getAttribute("rotate").getIntValue();
            PositionableLabelXml.doRotationConversion(rotation, l);
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        List<Element> states = element.getChildren();
        if (states.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Main element has " + states.size() + " items");
            }
            Element elem = element;     // the element containing the icons
            Element icons = element.getChild("icons");
            if (icons != null) {
                List<Element> s = icons.getChildren();
                states = s;
                elem = icons;          // the element containing the icons
                if (log.isDebugEnabled()) {
                    log.debug("icons element has " + states.size() + " items");
                }
            }
            for (int i = 0; i < states.size(); i++) {
                String key = states.get(i).getName();
                String state;
                if (key.equals("thrown")) {
                    state = "TurnoutStateThrown";
                } else if (key.equals("closed")) {
                    state = "TurnoutStateClosed";
                } else if (key.equals("unknown")) {
                    state = "BeanStateUnknown";
                } else {
                    state = "BeanStateInconsistent";
                }
                if (log.isDebugEnabled()) {
                    log.debug("setIcon for key \"" + key
                            + "\" and " + state);
                }
                NamedIcon icon = PositionableLabelXml.loadIcon(l, key, elem, "TurnoutIcon \"" + name + "\": icon \"" + state + "\" ", ed);
                if (icon != null) {
                    l.setStateIcon(state, icon);
                } else {
                    log.info("TurnoutIcon \"" + name + "\": icon \"" + state + "\" removed");
                    return;
                }
            }
            log.debug(states.size() + " icons loaded for " + l.getNameString());
        } else {        // case when everything was attributes
            loadTurnoutIcon("thrown", "TurnoutStateThrown", l, element, name, ed);
            loadTurnoutIcon("closed", "TurnoutStateClosed", l, element, name, ed);
            loadTurnoutIcon("unknown", "BeanStateUnknown", l, element, name, ed);
            loadTurnoutIcon("inconsistent", "BeanStateInconsistent", l, element, name, ed);
        }
        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            Attribute attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }
        
    }

    static private void loadTurnoutIcon(String key, String state, PositionableIcon l, Element element,
            String name, Editor ed) {
        NamedIcon icon = null;
        if (element.getAttribute(key) != null) {
            String iconName = element.getAttribute(key).getValue();
            icon = NamedIcon.getIconByName(iconName);
            if (icon == null) {
                icon = ed.loadFailed("Turnout \"" + name + "\" icon \"" + key + "\" ", iconName);
                if (icon == null) {
                    log.info("Turnout \"" + name + "\" icon \"" + key + "\" removed for url= " + iconName);
                }
            }
        } else {
            log.warn("did not locate " + key + " icon file for Turnout " + name);
        }
        if (icon == null) {
            log.info("Turnout Icon \"" + name + "\": icon \"" + key + "\" removed");
        } else {
            l.setStateIcon(state, icon);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutIconXml.class);
}
