package jmri.jmrit.display.configurexml;

import apps.gui.GuiLafPreferencesManager;
import java.awt.Color;
import java.awt.Font;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.ToolTip;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.PositionableJComponent objects
 *
 * @author PeteCressman Copyright: Copyright (c) 2018
 */
public class PositionableJComponentXml extends AbstractXmlAdapter {

    public PositionableJComponentXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableJComponent
     *
     * @param o Object to store, of type PositionableJComponent
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        PositionableJComponent p = (PositionableJComponent) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("PositionableJComponent");
        storeCommonAttributes(p, element);
        storeFontInfo(p, element);
        element.setAttribute("class", "jmri.jmrit.display.configurexml.PositionableJComponentXml");
        return element;
    }

    /**
     * Store the text formatting information.
     * <p>
     * This is always stored, even if the icon isn't in text mode, because some
     * uses (subclasses) of PositionableJComponent flip back and forth between icon
     * and text, and want to remember their formatting.
     *
     * @param p       the icon to store
     * @param element the XML representation of the icon
     */
    protected void storeFontInfo(Positionable p, Element element) {
        GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
        String defaultFontName = manager.getDefaultFont().getFontName();

        String fontName = p.getFont().getFontName();
        if (!fontName.equals(defaultFontName)) {
            element.setAttribute("fontname", "" + p.getFont().getFontName());
        }

        element.setAttribute("size", "" + p.getFont().getSize());
        element.setAttribute("style", "" + p.getFont().getStyle());

        // always write the foreground (text) color
        element.setAttribute("red", "" + p.getForeground().getRed());
        element.setAttribute("green", "" + p.getForeground().getGreen());
        element.setAttribute("blue", "" + p.getForeground().getBlue());

        Color backGround = p.getBackgroundColor(); 
        if (backGround!=null) {
            element.setAttribute("redBack", "" + backGround.getRed());
            element.setAttribute("greenBack", "" + backGround.getGreen());
            element.setAttribute("blueBack", "" + backGround.getBlue());
        }

        if (p.getMarginSize() != 0) {
            element.setAttribute("margin", "" + p.getMarginSize());
        }
        if (p.getBorderSize() != 0) {
            element.setAttribute("borderSize", "" + p.getBorderSize());
            element.setAttribute("redBorder", "" + p.getBorderColor().getRed());
            element.setAttribute("greenBorder", "" + p.getBorderColor().getGreen());
            element.setAttribute("blueBorder", "" + p.getBorderColor().getBlue());
        }
        if (p.getFixedWidth() != 0) {
            element.setAttribute("fixedWidth", "" + p.getFixedWidth());
        }
        if (p.getFixedHeight() != 0) {
            element.setAttribute("fixedHeight", "" + p.getFixedHeight());
        }

        String just;
        switch (p.getJustification()) {
            case 0x02:
                just = "right";
                break;
            case 0x04:
                just = "centre";
                break;
            default:
                just = "left";
                break;
        }
        element.setAttribute("justification", just);
    }

    /**
     * Default implementation for storing the common contents of an Icon
     *
     * @param p       the icon to store
     * @param element the XML representation of the icon
     */
    public void storeCommonAttributes(Positionable p, Element element) {

        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("forcecontroloff", !p.isControlling() ? "true" : "false");
        element.setAttribute("hidden", p.isHidden() ? "yes" : "no");
        element.setAttribute("positionable", p.isPositionable() ? "true" : "false");
        element.setAttribute("showtooltip", p.showToolTip() ? "true" : "false");
        element.setAttribute("editable", p.isEditable() ? "true" : "false");
        ToolTip tip = p.getToolTip();
        String txt = tip.getText();
        if (txt != null) {
            Element elem = new Element("tooltip").addContent(txt); // was written as "toolTip" 3.5.1 and before
            element.addContent(elem);
        }
        if (p.getDegrees() != 0) {
            element.setAttribute("degrees", "" + p.getDegrees());
        }
        element.setAttribute("scale", String.valueOf(p.getScale()));
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableJComponent, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor editor = (Editor) o;
        PositionableJComponent l = new PositionableJComponent(editor);;
        loadFontInfo(l, element);

        if (log.isDebugEnabled()) {
            java.util.List<Attribute> attrs = element.getAttributes();
            log.debug("\tElement Has " + attrs.size() + " Attributes:");
            for (int i = 0; i < attrs.size(); i++) {
                Attribute a = attrs.get(i);
                log.debug("\t\t" + a.getName() + " = " + a.getValue());
            }
            java.util.List<Element> kids = element.getChildren();
            log.debug("\tElementHas " + kids.size() + " children:");
            for (int i = 0; i < kids.size(); i++) {
                Element e = kids.get(i);
                log.debug("\t\t" + e.getName() + " = \"" + e.getValue() + "\"");
            }
        }
        editor.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.LABELS, element);
    }

    protected void loadFontInfo(PositionableJComponent l, Element element) {
        if (log.isDebugEnabled()) {
            log.debug("loadTextInfo");
        }
        Attribute a = element.getAttribute("size");
        try {
            if (a != null) {
                l.setFontSize(a.getFloatValue());
            }
        } catch (DataConversionException ex) {
            log.warn("invalid size attribute value");
        }

        a = element.getAttribute("style");
        try {
            if (a != null) {
                int style = a.getIntValue();
                int drop = 0;
                switch (style) {
                    case 0:
                        drop = 1; //0 Normal
                        break;
                    case 2:
                        drop = 1; //italic
                        break;
                    default:
                        // fall through
                        break;
                }
                l.setFontStyle(style | ~drop);
            }
        } catch (DataConversionException ex) {
            log.warn("invalid style attribute value");
        }

        a = element.getAttribute("fontname");
        try {
            if (a != null) {
                l.setFont(new Font(a.getValue(), l.getFont().getStyle(), l.getFont().getSize()));
            }
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        // set color if needed
        try {
            int red = element.getAttribute("red").getIntValue();
            int blue = element.getAttribute("blue").getIntValue();
            int green = element.getAttribute("green").getIntValue();
            l.setForeground(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        try {
            int red = element.getAttribute("redBack").getIntValue();
            int blue = element.getAttribute("blueBack").getIntValue();
            int green = element.getAttribute("greenBack").getIntValue();
            l.setBackgroundColor(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse background color attributes!");
        } catch (NullPointerException e) {
            l.setBackgroundColor(null);
        }            
        
        int fixedWidth = 0;
        int fixedHeight = 0;
        try {
            fixedHeight = element.getAttribute("fixedHeight").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse fixed Height attributes!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        try {
            fixedWidth = element.getAttribute("fixedWidth").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse fixed Width attribute!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }
        l.setFixedWidth(fixedWidth);
        l.setFixedHeight(fixedHeight);
        int margin = 0;
        try {
            margin = element.getAttribute("margin").getIntValue();
            l.setMarginSize(margin);
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse margin attribute!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }
        try {
            l.setBorderSize(element.getAttribute("borderSize").getIntValue());
            int red = element.getAttribute("redBorder").getIntValue();
            int blue = element.getAttribute("blueBorder").getIntValue();
            int green = element.getAttribute("greenBorder").getIntValue();
            l.setBorderColor(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse border attributes!");
        } catch (NullPointerException e) {  // considered normal if the attribute not present
        }

        a = element.getAttribute("justification");
        if (a == null) {
            l.setJustification(PositionableJComponent.LEFT);
        } else if (a.equals("left") ) {
            l.setJustification(PositionableJComponent.LEFT);
        } else if (a.equals("centre") ) {
            l.setJustification(PositionableJComponent.LEFT);
        } else if (a.equals("right") ) {
            l.setJustification(PositionableJComponent.LEFT);
        }
        a = element.getAttribute("orientation");
        if (a != null) {
           String val = a.getValue();
            if (val.equals("vertical_up")) {
                l.setDegrees(-90);                
            } else if (val.equals("vertical_down")) {
                l.setDegrees(90);                
            } else {
                l.setDegrees(0);                
            }
        }

        int deg = 0;
        try {
            a = element.getAttribute("degrees");
            if (a != null) {
                deg = a.getIntValue();
                l.setDegrees(deg);
            }
        } catch (DataConversionException ex) {
            log.warn("invalid 'degrees' value (non integer)");
        }
    }

    public void loadCommonAttributes(Positionable l, int defaultLevel, Element element) {
        try {
            l.setControlling(!element.getAttribute("forcecontroloff").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert positionable label forcecontroloff attribute");
        } catch (Exception e) {
        }

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x, y);

        // find display level
        int level = defaultLevel;
        try {
            level = element.getAttribute("level").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch (NullPointerException e) {
            // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        try {
            boolean value = element.getAttribute("hidden").getBooleanValue();
            l.setHidden(value);
            l.setVisible(!value);
        } catch (DataConversionException e) {
            log.warn("unable to convert positionable label hidden attribute");
        } catch (NullPointerException e) {
            // considered normal if the attribute not present
        }
        try {
            l.setPositionable(element.getAttribute("positionable").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert positionable label positionable attribute");
        } catch (NullPointerException e) {
            // considered normal if the attribute not present
        }
        try {
            l.setShowToolTip(element.getAttribute("showtooltip").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert positionable label showtooltip attribute");
        } catch (NullPointerException e) {
            // considered normal if the attribute not present
        }
        try {
            l.setEditable(element.getAttribute("editable").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert positionable label editable attribute");
        } catch (NullPointerException e) {
            // considered normal if the attribute not present
        }

        Attribute a = element.getAttribute("degrees");
        if (a != null && l instanceof PositionableJComponent) {
            try {
                int deg = a.getIntValue();
                ((PositionableJComponent) l).setDegrees(deg);
            } catch (org.jdom2.DataConversionException dce) {
            }
        }

        Element elem = element.getChild("tooltip");
        if (elem == null) {
            elem = element.getChild("toolTip"); // pre JMRI 3.5.2
        }
        if (elem != null) {
            ToolTip tip = l.getToolTip();
            if (tip != null) {
                tip.setText(elem.getText());
            }
        }
    }


    /**
     * Use general rotation code for orthogonal rotations.
     * @param rotation
     * @param l
     *
    protected void doRotationConversion(int rotation, PositionableJComponent l) {
        switch(rotation) {
            case 1:
                l.setDegrees(90);
                break;
            case 2:
                l.setDegrees(180);
                break;
            case 3:
                l.setDegrees(270);
                break;
            default:
                l.setDegrees(0);
                break;
        }        
    }*/

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponentXml.class);
}