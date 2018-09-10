package jmri.jmrit.display.configurexml;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jmri.jmrit.display.DisplayState;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableIcon;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.PositionableIcon objects
 *
 * @author PeteCressman Copyright: Copyright (c) 2018
 */
public class PositionableIconXml extends PositionableLabelXml {
    
    public PositionableIconXml() {
    }
    
    /**
     * Default implementation for storing the contents of a PositionableJComponent
     *
     * @param element for a descendant of PositionableIcon
     * @param p the descendant
     * @return Element containing the complete info
     */
    public boolean storePositionableIcon(Element element, PositionableIcon p) {
        if (!p.isActive()) {
            return false;  // if flagged as inactive, don't store
        }
        element.setAttribute("forcecontroloff", !p.isControlling() ? "true" : "false");
        
        storeCommonLabelAttributes(p, element);
        storeFontInfo(p, element);

        Element elem = new Element("stateMaps");
        if (p.getFamily() != null) {
            element.setAttribute("family", p.getFamily());
        }
        
        Iterator<Entry<String, DisplayState>> it = p.getDisplayStateMap().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, DisplayState> entry = it.next();
            elem.addContent(storeStateData(entry.getKey(), entry.getValue()));
        }
        return true;
    }

    public Element storeStateData(String key, DisplayState ds) {
        Element element = new Element("displaystate");
        element.setAttribute("state", key);

        if (ds.getText() != null) {
            Element elem = new Element("text");
            elem.setText(ds.getText());
        }
        if (ds.getIcon() != null) {
            element.addContent(storeIcon("icon", ds.getIcon()));
        }       
        if (ds.getForeground() != null) {
            element.addContent(storeColor("foreground", ds.getForeground()));
        }
        if (ds.getBackground() != null) {
            element.addContent(storeColor("background", ds.getBackground()));
        }
        if (ds.getBorderColor() != null) {
            element.addContent(storeColor("border", ds.getBorderColor()));
        }
      return element;
    }

    /**
     * Load, PositionableIcon element
     *
     * @param element Top level PositionableIcon Element to unpack.
     * @param p PositionableIcon of element
     * @return false if state map not found
     */
    public boolean loadPositionableIcon(Element element, PositionableIcon p) {

        Editor ed = p.getEditor();
        if (ed == null) {
            log.error("No editor to place PositionableIcon {}", p.getNameString());
            return false;
        }
        if (element.getAttribute("forcecontroloff") != null) {
            try {
                p.setControlling(!element.getAttribute("forcecontroloff").getBooleanValue());
            } catch (DataConversionException e1) {
                log.warn("unable to convert positionable label forcecontroloff attribute");
            } catch (Exception e) {
            }
        }
        loadFontInfo(p, element);

        try {
            p.setIsText(element.getAttribute("isText").getBooleanValue());
        } catch (DataConversionException ex) {
            log.warn("unable to convert PositionableIcon \"isText\" attribute");
        } catch (NullPointerException ex) {
            log.warn("PositionableIcon \"isText\" attribute not found");
        }
        try {
            p.setIsIcon(element.getAttribute("isIcon").getBooleanValue());
        } catch (DataConversionException ex) {
            log.warn("unable to convert PositionableIcon \"isIcon\" attribute");
        } catch (NullPointerException ex) {
            log.warn("PositionableIcon \"isIcon\" attribute not found");
        }

        Element elem =element.getChild("stateMaps");
        if (elem == null) {
            log.warn("No state elements found for PositionableIcon {}", p.getNameString());
            return false;
        }
        Attribute attr = elem.getAttribute("family");
        if (attr !=null) {
            p.setFamily(attr.getValue());
        }

        List<Element> stateList = elem.getChildren("displaystate");
        if (log.isDebugEnabled()) {
            log.debug("Found {} displaystate objects", stateList.size());
        }
        if (stateList == null || stateList.size() < 4) {
            log.error("Not enough state elements found for PositionableIcon {}", p.getNameString());
            return false;
        }
        boolean loaded = true;
        for (Element state : stateList) {
            if (!loadStateData(state, p)) {
                loaded = false;
            }
        }
/*
        if (element.getAttribute("text") != null) {
            p.setText(element.getAttribute("text").getValue());
        }

        if (element.getAttribute("icon") != null) {
            String name = element.getAttribute("icon").getValue();
            p.setIcon(NamedIcon.getIconByName(name));
        }*/

        return loaded;
    }

    public boolean loadStateData(Element element, PositionableIcon pi) {
        
        Attribute attr = element.getAttribute("state");
        if (attr == null) {
            log.error("No state name for element: {}", element.getName());
            return false;
        }
        String state = attr.getValue();

        DisplayState ds = pi.getStateData(state);
        if (ds == null) {
            log.error("No DisplayState class \"{}\" in PositionableIcon: {}", state, pi.getName());
            return false;
        }
        
        Element elem = element.getChild("text");
        if (elem != null) {
            ds.setText(elem.getText());
        }
        
        ds.setIcon(getNamedIcon("icon", element, "pi.getName() ", pi.getEditor()));

        ds.setBackground(loadColor(element, "foreground", state));
        ds.setBackground(loadColor(element, "background", state));
        ds.setBackground(loadColor(element, "borderColor", state));
        return true;
    }
    
    public void loadDisplayState(Element element, DisplayState ds) {
        
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableIconXml.class);
}