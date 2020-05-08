package jmri.jmrit.display.configurexml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayState;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.logix.OBlock;
import org.jdom2.Attribute;
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

        HashMap<String, HashMap<String, DisplayState>> displayMaps = p.getDisplayMaps();
        Element el = new Element("statusmaps");
        String family = p.getFamily();
        if (family != null) {
            el.setAttribute("family", family);
        }
        
        for (Entry<String, HashMap<String, DisplayState>> entry : displayMaps.entrySet()) {
            elem = new Element(entry.getKey());
            HashMap<String, DisplayState> stateMap = entry.getValue();

            Iterator<Entry<String, DisplayState>> it = stateMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, DisplayState> ent = it.next();
                elem.addContent(storeStateData(ent.getKey(), ent.getValue()));
            }
            el.addContent(elem);
        }
        element.addContent(el);

        elem = new Element("paths");
        ArrayList<String> paths = p.getPaths();
        if (paths != null) {
            for (String path : paths) {
                Element e = new Element("path");
                e.addContent(path);
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
        }
        loadPositionableIcon(element, pi);
        pi.setTurnout(name);
        
        
        HashMap<String, HashMap<String, DisplayState>> displayMaps = pi.getDisplayMaps();
        elem = element.getChild("statusmaps");
        boolean loaded = true;
        if (elem != null) {
            List<Element> statusMaps = elem.getChildren();
            if (log.isDebugEnabled()) {
                log.debug("Found {} displaystatus objects of {} for {}", statusMaps.size(), elem.getName(), pi.getNameString());
            }
            if (statusMaps.size() < 6) {
                log.error("Not enough state elements found for status {}, {}", elem.getName(), pi.getNameString());
                loaded = false;
            }
            for (Element status : statusMaps) {
                HashMap<String, DisplayState> stateMap = loadStatusMap(status, pi);
                if (stateMap == null) {
                    loadPre50(status, pi, name);
                }
                String statusName = status.getName();
                displayMaps.put(statusName, stateMap);
            }
        } else {
            elem = element.getChild("iconmaps");
            List<Element> maps = elem.getChildren();
            for (Element status : maps) {
                loadPre50(status, pi, name);
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
            for (Element value : pth) {
                paths.add(value.getText());
            }
            pi.setPaths(paths);
        }

        pi.updateSize();
        ed.putItem(pi);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(pi, Editor.TURNOUTS, element);
    }

    public HashMap<String, DisplayState> loadStatusMap(Element element, IndicatorTurnoutIcon pi) {
        String status = element.getName();
        HashMap<String, DisplayState> map = new HashMap<>();
        List<Element> stateList = element.getChildren("displaystate");
        for (Element state : stateList) {
            Attribute attr = state.getAttribute("state");
            if (attr == null) {
                log.error("No state name for element: {} of status {}", state.getName(), status);
                return null;
            }
            String stateName = attr.getValue();

            DisplayState ds = new DisplayState();
            
            Element elem = state.getChild("text");
            if (elem != null) {
                ds.setText(elem.getText());
            }
            
            ds.setIcon(getNamedIcon("icon", state, "pi.getName() ", pi.getEditor()));

            ds.setBackground(loadColor(state, "foreground", stateName));
            ds.setBackground(loadColor(state, "background", stateName));
            ds.setBackground(loadColor(state, "borderColor", stateName));
            map.put(stateName, ds);
        }
        return map;
    }

    @Override
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
            if (elem.getAttribute("text") != null) {
                ds.setText(elem.getAttribute("text").getValue());
            }
        }

        ds.setIcon(getNamedIcon("icon", element, "pi.getName() ", pi.getEditor()));

        ds.setBackground(loadColor(element, "foreground", state));
        ds.setBackground(loadColor(element, "background", state));
        ds.setBackground(loadColor(element, "borderColor", state));
        return true;
    }
    
    /*
     * pre release 5.0 or something like that
     */
    private void loadPre50(Element status, IndicatorTurnoutIcon p, String name) {
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
                    p.setStateIcon(statusName, stateName, icon);
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
