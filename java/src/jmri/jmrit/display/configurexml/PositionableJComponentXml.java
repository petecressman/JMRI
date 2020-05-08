package jmri.jmrit.display.configurexml;

import jmri.util.gui.GuiLafPreferencesManager;
import java.awt.Color;
import java.awt.Font;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.ToolTip;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.Positionable objects
 *
 * @author PeteCressman Copyright: Copyright (c) 2018
 */
public class PositionableJComponentXml extends AbstractXmlAdapter {

    public PositionableJComponentXml() {
    }

    /**
     * Default implementation for storing the contents of a Positionable
     *
     * @param o Object to store, of type Positionable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Positionable p = (Positionable) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("Positionable");
        storeCommonAttributes(p, element);
        storeFontInfo(p, element);
        element.setAttribute("class", "jmri.jmrit.display.configurexml.PositionableJComponentXml");
        return element;
    }

    /**
     * Store the text formatting information.
     * <p>
     * This is always stored, even if the icon isn't in text mode, because some
     * uses (subclasses) of Positionable flip back and forth between icon
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

        Color foreGround = p.getForeground();
        if (foreGround !=null) {
            element.setAttribute("red", "" + foreGround.getRed());
            element.setAttribute("green", "" + foreGround.getGreen());
            element.setAttribute("blue", "" + foreGround.getBlue());
        }

        Color backGround = p.getBackground();
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
        element.setAttribute("hidden", p.isHidden() ? "yes" : "no");
        element.setAttribute("positionable", p.isPositionable() ? "true" : "false");
        element.setAttribute("showtooltip", p.showToolTip() ? "true" : "false");
        element.setAttribute("editable", p.isEditable() ? "true" : "false");
        String txt = p.getToolTipText();
        if (txt != null) {
            Element elem = new Element("tooltip").addContent(txt); // was written as "toolTip" 3.5.1 and before
            element.addContent(elem);
        }
        if (p.getDegrees() != 0) {
            Element elem = new Element("degrees").addContent(String.valueOf(p.getDegrees()));
            element.addContent(elem);
        }
        Element elem = new Element("scale").addContent(String.valueOf(p.getScale()));
        element.addContent(elem);
    }

    public Element storeColor(String name, Color color) {
        Element element = new Element(name);
        element.setAttribute("red", "" + color.getRed());
        element.setAttribute("green", "" + color.getGreen());
        element.setAttribute("blue", "" + color.getBlue());
        element.setAttribute("alpha", "" + color.getAlpha());
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a Positionable, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor editor = (Editor) o;
        Positionable l = new Positionable(editor);
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

    protected void loadFontInfo(Positionable l, Element element) {
        if (log.isDebugEnabled()) {
            log.debug("loadFontInfo");
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
            l.setBackground(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse background color attributes!");
        } catch (NullPointerException e) {
            l.setBackground(null);
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
            l.setJustification(Positionable.LEFT);
        } else if (a.equals("left") ) {
            l.setJustification(Positionable.LEFT);
        } else if (a.equals("centre") ) {
            l.setJustification(Positionable.LEFT);
        } else if (a.equals("right") ) {
            l.setJustification(Positionable.LEFT);
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
        if (l instanceof jmri.jmrit.display.PositionableIcon &&
                element.getAttribute("forcecontroloff") != null) {    // pre 5.0 or something
            try {
                ((jmri.jmrit.display.PositionableIcon)l).setControlling(!element.getAttribute("forcecontroloff").getBooleanValue());
            } catch (DataConversionException e1) {
                log.warn("unable to convert positionable label forcecontroloff attribute");
            } catch (Exception e) {
            }
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

        Element elem = element.getChild("tooltip");
        if (elem == null) {
            elem = element.getChild("toolTip"); // pre JMRI 3.5.2
        }
        if (elem != null) {
            ToolTip tip = l.getToolTip();
            if (tip != null) {
                tip.setText(elem.getText());
            } else {
                l.setToolTipText(elem.getText());
            }
        }

        elem = element.getChild("degrees");
        if (elem != null) {
            l.setDegrees(Integer.parseInt(elem.getText()));
        }

        elem = element.getChild("scale");
        if (elem != null) {
            l.setScale(Float.parseFloat(elem.getText()));
        }
    }

    Color loadColor(Element elem, String childName, String name) {
        Element element = elem.getChild(childName);
        if (element != null) {
            try {
                int red = 0;
                int blue = 0;
                int green = 0;
                int alpha = 255;
                try {
                    red = element.getAttribute("red").getIntValue();
                } catch (NullPointerException e) {
                }            
                try {
                    blue = element.getAttribute("blue").getIntValue();;
                } catch (NullPointerException e) {
                }            
                try {
                    green = element.getAttribute("green").getIntValue();
                } catch (NullPointerException e) {
                }            
                try {
                    alpha = element.getAttribute("alpha").getIntValue();
                } catch (NullPointerException e) {
                }            
                return new Color(red, green, blue, alpha);
            } catch (org.jdom2.DataConversionException e) {
            }
            log.warn("Could not find color {} for item {}!", childName, name);
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponentXml.class);
}