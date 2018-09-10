package jmri.jmrit.display.configurexml;

import java.awt.Color;
import java.util.List;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayState;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SensorIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class SensorIconXml extends PositionableIconXml {

    /**
     * Default implementation for storing the contents of a SensorIcon
     *
     * @param o Object to store, of type SensorIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SensorIcon p = (SensorIcon) o;
        Element element = new Element("sensoricon");
        
        if (!storePositionableIcon(element, p)) {
            return null;
        }
        element.setAttribute("sensor", p.getNamedSensor().getName());
        element.setAttribute("momentary", p.getMomentary() ? "true" : "false");

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SensorIconXml");
        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        Editor ed = (Editor) o;
        SensorIcon l = new SensorIcon(ed);

        String name;
        Attribute attr = element.getAttribute("sensor");
        if (attr == null) {
            log.error("incorrect information for sensor; must use sensor name");
            ed.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }
        if (!loadPositionableIcon(element, l)) {
            loadPre50(element, l, name);
        }
        Attribute a = element.getAttribute("momentary");
        if ((a != null) && a.getValue().equals("true")) {
            l.setMomentary(true);
        } else {
            l.setMomentary(false);
        }

        l.setSensor(name);

        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SENSORS, element);
    }
    
    /*
     * pre release 5.0 or something like that
     */
    private void loadPre50(Element element, SensorIcon l, String name) {
        boolean isIcon = true;
        if (element.getAttribute("icon") != null) {
            String yesno = element.getAttribute("icon").getValue();
            if ((yesno != null) && (!yesno.equals(""))) {
                if (yesno.equals("yes")) {
                    isIcon = true;
                } else if (yesno.equals("no")) {
                    isIcon = false;
                }
            }
        }
        l.setIsIcon(isIcon);
        if (element.getAttribute("text") != null) {
            if (element.getAttribute("text").getValue().length()>=0) {
                l.setIsText(true); //text mode
                if (l.isIcon()) {
                    log.debug("Sensor "+l.getNameString()+" is both text and icon for overlay.");
                }
            }
        }

        try {
            int rotation = element.getAttribute("rotate").getIntValue();
            PositionableLabelXml.doRotationConversion(rotation, l);
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        Editor ed = l.getEditor();
        loadSensorIcon("active", "SensorStateActive", l, element, name, ed);      
        loadSensorIcon("inactive", "SensorStateInactive", l, element, name, ed);
        loadSensorIcon("unknown", "BeanStateUnknown", l, element, name, ed);
        loadSensorIcon("inconsistent", "BeanStateInconsistent", l, element, name, ed);
        
        loadSensorTextState("Active", "SensorStateActive", l, element);
        loadSensorTextState("InActive", "SensorStateInactive", l, element);
        loadSensorTextState("Unknown", "BeanStateUnknown", l, element);
        loadSensorTextState("Inconsistent", "BeanStateInconsistent", l, element);

        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            Attribute attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }
    }

    /*
     * pre 5.0
     */
    private NamedIcon loadSensorIcon(String key, String state, SensorIcon l, Element element, String name, Editor ed) {
        String msg = "SensorIcon \"" + name + "\": icon \"" + state + "\" ";
        // loadIcon gets icon as an element
        NamedIcon icon = PositionableLabelXml.loadIcon(l, key, element, msg, ed);
        if (icon == null) {
            // old config files may define icons as attributes
            String iconName;
            if (element.getAttribute(key) != null
                    && !(iconName = element.getAttribute(key).getValue()).equals("")) {

                icon = NamedIcon.getIconByName(iconName);
                if (icon == null) {
                    icon = ed.loadFailed(msg, iconName);
                    if (icon == null) {
                        log.info(msg + " removed for url= " + iconName);
                    }
                }
            } else {
                log.warn("did not locate " + key + " icon file for " + name);
            }
        }
        if (icon == null) {
            log.info(msg + " removed", msg);
        } else {
            l.setStateIcon(state, icon);
        }
        return icon;
    }

    /*
     * pre 5.0
     */
    private void loadSensorTextState(String key, String state, SensorIcon si, Element element) {
        String name = null;
        Color clrText = null;
        Color clrBackground = null;
        List<Element> textList = element.getChildren(key.toLowerCase() + "Text");
        if (log.isDebugEnabled()) {
            log.debug("Found " + textList.size() + " " + key + " text objects");
        }
        if (textList.size() > 0) {
            Element elem = textList.get(0);
            try {
                name = elem.getAttribute("text").getValue();
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = elem.getAttribute("red").getIntValue();
                int blue = elem.getAttribute("blue").getIntValue();
                int green = elem.getAttribute("green").getIntValue();
                clrText = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = elem.getAttribute("redBack").getIntValue();
                int blue = elem.getAttribute("blueBack").getIntValue();
                int green = elem.getAttribute("greenBack").getIntValue();
                clrBackground = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
        } else {
            if (element.getAttribute(key.toLowerCase()) != null) {
                name = element.getAttribute(key.toLowerCase()).getValue();
            }
            try {
                int red = element.getAttribute("red" + key).getIntValue();
                int blue = element.getAttribute("blue" + key).getIntValue();
                int green = element.getAttribute("green" + key).getIntValue();
                clrText = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = element.getAttribute("red" + key + "Back").getIntValue();
                int blue = element.getAttribute("blue" + key + "Back").getIntValue();
                int green = element.getAttribute("green" + key + "Back").getIntValue();
                clrBackground = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
        }

        DisplayState pos = si.getDisplayState(state);
        pos.setText(name);
        pos.setForeground(clrText);
        pos.setBackground(clrBackground);
        log.debug("loadSensorTextState: text= {}, foreGround= {}, backGround= {}", name, clrText, clrBackground);
    }

    private final static Logger log = LoggerFactory.getLogger(SensorIconXml.class);
}
