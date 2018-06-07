package jmri.jmrit.display.configurexml;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.PositionableLabel;
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
        // can be both for text overlaid icon
        element.setAttribute("isText", p.isText() ? "yes" : "no");
        element.setAttribute("isIcon", p.isIcon() ? "yes" : "no");

        if (p.getText() != null) {
            element.setAttribute("text", p.getText());
        }
        if (p.getIcon() != null) {
            element.addContent(storeIcon("icon", p.getIcon()));
        }       
        storeCommonAttributes(p, element);
        storeFontInfo(p, element);

        if (p.getFamily() != null) {
            element.setAttribute("family", p.getFamily());
        }
        
        Element elem = new Element("states");
        Iterator<Entry<String, PositionableLabel>> it = p.getIconMap().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, PositionableLabel> entry = it.next();
            elem.addContent(storeStateData(entry.getKey(), entry.getValue()));
        }
        return true;
    }

    public Element storeStateData(String key, PositionableLabel p) {
        Element element = new Element("state");
        element.setAttribute("stateKey", key);

        Element elem = new Element("stateAttributes");
        if (p.getText() != null) {
            elem.setAttribute("text", p.getText());
        }
        if (p.getIcon() != null) {
            elem.addContent(storeIcon(key, p.getIcon()));
        }       
        storeCommonAttributes(p, elem);
        storeFontInfo(p, elem);

        element.addContent(elem);
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
        loadFontInfo(p, element);

        Element elem =element.getChild("states");
        if (elem == null) {
            log.error("No state elements found for PositionableIcon {}", p.getNameString());
            return false;
        }
        List<Element> stateList = elem.getChildren("state");
        if (log.isDebugEnabled()) {
            log.debug("Found {} OBlock objects", stateList.size());
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

        if (element.getAttribute("text") != null) {
            p.setText(element.getAttribute("text").getValue());
        }

        if (element.getAttribute("icon") != null) {
            String name = element.getAttribute("icon").getValue();
            p.setIcon(NamedIcon.getIconByName(name));
        }

        return loaded;
    }

    public boolean loadStateData(Element element, PositionableIcon pi) {
        
        Attribute attr = element.getAttribute("stateKey");
        if (attr == null) {
            log.error("No stateKey for element: {}", element.getName());
            return false;
        }
        String state = attr.getValue();
        
        Element elem = element.getChild("stateAttributes");
        if (elem == null) {
            log.error("No \"stateAttributes\" child for state {} in element: {}", state, element.getName());
            return false;
        }

        PositionableLabel p = pi.getStateData(state);
        if (p == null) {
            log.error("No state class for state {} in element: {}", state, element.getName());
            return false;
        }
        
        if (elem.getAttribute("text") != null) {
            p.setText(elem.getAttribute("text").getValue());
        }

        if (elem.getAttribute("icon") != null) {
            String name = elem.getAttribute("icon").getValue();
            p.setIcon(NamedIcon.getIconByName(name));
        }
        loadFontInfo(p, elem);
        loadCommonAttributes(p, Editor.SENSORS, elem);
        
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableIconXml.class);
}