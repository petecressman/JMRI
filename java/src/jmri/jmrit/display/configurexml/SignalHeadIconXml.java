package jmri.jmrit.display.configurexml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalHeadIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.SignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class SignalHeadIconXml extends PositionableIconXml {

    /**
     * Default implementation for storing the contents of a SignalHeadIcon
     *
     * @param o Object to store, of type SignalHeadIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        SignalHeadIcon p = (SignalHeadIcon) o;
        Element element = new Element("signalheadicon");

        if (!storePositionableIcon(element, p)) {
            return null;
        }
        element.setAttribute("signalhead", "" + p.getNamedSignalHead().getName());
        element.setAttribute("clickmode", "" + p.getClickMode());
        element.setAttribute("litmode", "" + p.getLitMode());

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SignalHeadIconXml");
        return element;
    }

    /**
     * Create a SignalHeadIcon, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;
        SignalHeadIcon l = new SignalHeadIcon(ed);

        String name;
        Attribute attr = element.getAttribute("signalhead");
        if (attr == null) {
            log.error("incorrect information for signal head; must use signalhead name");
            ed.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }
        SignalHead sh = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);

        if (!loadPositionableIcon(element, l)) {
            loadPre50(element, l, name);
        }

        if (sh != null) {
            l.setSignalHead(name);
        } else {
            log.error("SignalHead named '" + attr.getValue() + "' not found.");
            //    ed.loadFailed();
            return;
        }
        try {
            attr = element.getAttribute("clickmode");
            if (attr != null) {
                l.setClickMode(attr.getIntValue());
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("Failed on clickmode attribute: " + e);
        }

        try {
            attr = element.getAttribute("litmode");
            if (attr != null) {
                l.setLitMode(attr.getBooleanValue());
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("Failed on litmode attribute: " + e);
        }

        l.updateSize();
        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SIGNALS, element);
    }
    
    /*
     * pre release 5.0 or something like that
     */
    private void loadPre50(Element element, SignalHeadIcon l, String name) {
        // map previous stored 'English' names to names found in the file jmri.NamedBeanBundle.properties
        java.util.ResourceBundle rbean = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("red", "SignalHeadStateRed");
        nameMap.put("yellow", "SignalHeadStateYellow");
        nameMap.put("green", "SignalHeadStateGreen");
        nameMap.put("lunar", "SignalHeadStateLunar");
        nameMap.put("held", "SignalHeadStateHeld");
        nameMap.put("dark", "SignalHeadStateDark");
        nameMap.put("flashred", "SignalHeadStateFlashingRed");
        nameMap.put("flashyellow", "SignalHeadStateFlashingYellow");
        nameMap.put("flashgreen", "SignalHeadStateFlashingGreen");
        nameMap.put("flashlunar", "SignalHeadStateFlashingLunar");

        try {
            int rotation = element.getAttribute("rotate").getIntValue();
            PositionableLabelXml.doRotationConversion(rotation, l);
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        Editor ed = l.getEditor();

        List<Element> aspects = element.getChildren();
        log.debug("Found {} children of element {}", aspects.size(), element.getName());
        if (aspects.size() > 0) {
            Element icons = element.getChild("icons");
            Element elem = element;
            if (icons != null) {
                List<Element> c = icons.getChildren();
                aspects = c;
                elem = icons;
            }
            for (int i = 0; i < aspects.size(); i++) {
                String aspect = aspects.get(i).getName();
                NamedIcon icon = PositionableLabelXml.loadIcon(l, aspect, elem, "SignalHead \"" + name + "\": icon \"" + aspect + "\" ", ed);
                if (icon != null) {
                    l.setStateIcon(nameMap.get(aspect), icon);
                } else {
                    log.info("SignalHead \"" + name + "\": icon \"" + aspect + "\" removed");
                }
            }
            log.debug(aspects.size() + " icons loaded for " + l.getNameString());
        } else {
            // old style as attributes - somewhere around pre 2.5.4
            Iterator<Map.Entry<String, String>> iter = nameMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                loadSignalIcon(entry.getKey(), entry.getValue(), l, element, name, ed);
            }
        }
        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            Attribute attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }
        
    }

    private void loadSignalIcon(String key, String aspect, SignalHeadIcon l,
            Element element, String name, Editor ed) {
        String msg = "SignalHead \"" + name + "\": icon \"" + key + "\" ";
        NamedIcon icon = PositionableLabelXml.loadIcon(l, key, element, msg, ed);
        if (icon == null) {
            if (element.getAttribute(key) != null) {
                String iconName = element.getAttribute(key).getValue();
                icon = NamedIcon.getIconByName(iconName);
                if (icon == null) {
                    icon = ed.loadFailed(msg, iconName);
                    if (icon == null) {
                        log.info(msg + " removed for url= " + iconName);
                    }
                }
            } else {
                log.info("did not load file aspect " + aspect + " for SignalHead " + name);
            }
        }
        if (icon == null) {
            log.info("SignalHead Icon \"" + name + "\": icon \"" + aspect + "\" removed");
        }
        l.setStateIcon(aspect, icon);
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadIconXml.class);
}
