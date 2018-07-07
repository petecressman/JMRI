package jmri.jmrit.display.configurexml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.logix.OBlock;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.IndicatorTurnoutIconXml objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2010
 */
public class IndicatorTurnoutIconXml extends PositionableIconXml {

    public IndicatorTurnoutIconXml() {
    }

    /**
     * Default implementation for storing the contents of a IndicatorTurnoutIcon
     *
     * @param o Object to store, of type IndicatorTurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        IndicatorTurnoutIcon p = (IndicatorTurnoutIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("indicatorturnouticon");
        storeCommonAttributes(p, element);

        NamedBeanHandle<Turnout> t = p.getNamedTurnout();
        if (t != null) {
            element.addContent(storeNamedBean("turnout", t));
        }
        NamedBeanHandle<OBlock> b = p.getNamedOccBlock();
        if (b != null) {
            element.addContent(storeNamedBean("occupancyblock", b));
        }
        NamedBeanHandle<Sensor> s = p.getNamedOccSensor();
        if (b == null && s != null) { // only write sensor if no OBlock
            element.addContent(storeNamedBean("occupancysensor", s));
        }

        Element elem = new Element("showTrainName");
        String show = "no";
        if (p.showTrain()) {
            show = "yes";
        }
        elem.addContent(show);
        element.addContent(elem);

        HashMap<String, PositionableLabel> statusMaps = p.getIconMap();        
        Element el = new Element("iconmaps");
        String family = p.getFamily();
        if (family != null) {
            el.setAttribute("family", family);
        }
        
        for (Entry<String, PositionableLabel> entry : statusMaps.entrySet()) {
            elem = new Element(entry.getKey());
            PositionableIcon pi = (PositionableIcon)entry.getValue();
            if (!storePositionableIcon(elem, pi)) {
                return null;
            }
            el.addContent(elem);
        }
        element.addContent(el);

        elem = new Element("paths");
        ArrayList<String> paths = p.getPaths();
        if (paths != null) {
            for (int i = 0; i < paths.size(); i++) {
                Element e = new Element("path");
                e.addContent(paths.get(i));
                elem.addContent(e);

            }
            element.addContent(elem);
        }

        element.setAttribute("class", "jmri.jmrit.display.configurexml.IndicatorTurnoutIconXml");
        return element;
    }

    Element storeNamedBean(String elemName, NamedBeanHandle<?> nb) {
        Element elem = new Element(elemName);
        elem.addContent(nb.getName());
        return elem;
    }

    /**
     * Create a IndicatorTurnoutIcon, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;

        IndicatorTurnoutIcon pi = new IndicatorTurnoutIcon(ed);
        Element elem = element.getChild("turnout");
        String name;

        if (elem == null) {
            log.error("incorrect information for turnout; must use turnout name");
            ed.loadFailed();
            return;
        } else {
            name = elem.getText();
            pi.setTurnout(name);
        }
        elem = element.getChild("iconmaps");
        if (elem != null) {
            List<Element> maps = elem.getChildren();
            for (Element status : maps) {
                PositionableIcon p = (PositionableIcon)pi.getStateData(status.getName());
                if (!loadPositionableIcon(status, p)) {
                    log.debug("No loadable PositionableIcon for status {}", status.getName());
                    loadPre50(status, p, name);
                }
            }
        }

        elem = element.getChild("occupancyblock");
        if (elem != null) {
            pi.setOccBlock(elem.getText());
        } else {        // only write sensor if no OBlock, don't write double sensing
            elem = element.getChild("occupancysensor");
            if (elem != null) {
                pi.setOccSensor(elem.getText());
            }            
        }

        pi.setShowTrain(false);
        elem = element.getChild("showTrainName");
        if (elem != null) {
            if ("yes".equals(elem.getText())) {
                pi.setShowTrain(true);
            }
        }

        elem = element.getChild("paths");
        if (elem != null) {
            ArrayList<String> paths = new ArrayList<>();
            List<Element> pth = elem.getChildren();
            for (int i = 0; i < pth.size(); i++) {
                paths.add(pth.get(i).getText());
            }
            pi.setPaths(paths);
        }

//        l.updateSize();
        ed.putItem(pi);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(pi, Editor.TURNOUTS, element);
    }

    /*
     * pre release 5.0 or something like that
     */
    private void loadPre50(Element status, PositionableIcon p, String name) {
        Editor ed = p.getEditor();

        try {
            int rotation = status.getAttribute("rotate").getIntValue();
            doRotationConversion(rotation, p);
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        List<Element> states = status.getChildren();
        String statusName = status.getName();
        if (states != null) {
            if (log.isDebugEnabled()) {
                log.debug("status element {} has {} states", statusName, states.size());
            }
            for (int i = 0; i < states.size(); i++) {
                String stateName = states.get(i).getName();
                NamedIcon icon = PositionableLabelXml.loadIcon(p, stateName, status, 
                        "IndicatorTurnoutIcon \"" + statusName + "\": icon \"" + stateName + "\" ", ed);
                if (icon != null) {
                    p.setStateIcon(stateName, icon);
                    if (log.isDebugEnabled()) {
                        log.debug("Icon for  {}, {} loaded", statusName, stateName);
                    }
                } else {
                    log.info("IndicatorTurnoutIcon \"{}\": icon for {}, {} removed", name, statusName, stateName);
                    return;
                }
            }
       }
    }
    
    private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconXml.class);
}
