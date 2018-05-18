package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.util.MenuScroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class handles text attributes for Positionables. Font size, style and
 * color. Margin size and color, Border size and color, Fixed sizes.
 * Justification.
 * </p>
 *
 * moved from PositionableLabel
 * @author Pete Cressman copyright (C) 2010
 */
public class PositionablePopupUtil {

    protected Positionable _parent;
    protected PositionablePropertiesUtil _propertiesUtil;

    protected final int LABEL = 1;
    protected final int TEXTFIELD = 2;
    protected final int JCOMPONENT = 3;

    public PositionablePopupUtil(Positionable parent) {
        _parent = parent;
        _propertiesUtil = new PositionablePropertiesUtil(_parent);
    }
    
    @Override
    public PositionablePopupUtil clone() {
        PositionablePopupUtil util = new PositionablePopupUtil(_parent);
        return finishClone(util);
    }

    public PositionablePopupUtil finishClone(PositionablePopupUtil util) {
        util.setJustification(getJustification());
        util.setFixedWidth(getFixedWidth());
        util.setFixedHeight(getFixedHeight());
        
        _parent.setFont(_parent.getFont().deriveFont(getFontStyle()));
        util.setFontSize(getFontSize());
        return util;
    }

    @Override
    public String toString() {
        return _parent.getNameString() + ": fixedWidth= " + fixedWidth + ", fixedHeight= " + fixedHeight;
    }

    /**
     * *************************************************************************************
     */
    static final public int FONT_COLOR = 0x00;
    static final public int BACKGROUND_COLOR = 0x01;
    static final public int BORDER_COLOR = 0x02;
    static final public int MIN_SIZE = 5;

    private int fixedWidth = 0;
    private int fixedHeight = 0;

    JMenuItem italic = null;
    JMenuItem bold = null;

    public void setBackgroundMenu(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("FontBackgroundColor"));
        edit.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog((JComponent)_parent,
                                 Bundle.getMessage("FontBackgroundColor"),
                                 _parent.getBackgroundColor());
            if (desiredColor!=null ) {
                _parent.setBackgroundColor(desiredColor);
           }
        });
 
        popup.add(edit);

    }

    public void setTextBorderMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditBorder"));
        int _borderSize = _parent.getBorderSize();
        JMenuItem jmi = edit.add("Border Size = " + _borderSize);
        jmi.setEnabled(false);
        edit.add(CoordinateEdit.getBorderEditAction(_parent));
        JMenuItem colorMenu = new JMenuItem(Bundle.getMessage("BorderColorMenu"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog((JComponent)_parent,
                                 Bundle.getMessage("BorderColorMenu"),
                                 _parent.getBorderColor());
            if (desiredColor!=null ) {
                _parent.setBorderColor(desiredColor);
           }
        });
        edit.add(colorMenu);
        popup.add(edit);
    }

    public void setTextFontMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFont"));
        edit.add(makeFontMenu());
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        JMenuItem colorMenu = new JMenuItem(Bundle.getMessage("FontColor"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog((JComponent)_parent,
                                 Bundle.getMessage("FontColor"),
                                 _parent.getForeground());
            if (desiredColor!=null ) {
                _parent.setForeground(desiredColor);
           }
        });
        edit.add(colorMenu);
        popup.add(edit);
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(int w) {
        fixedWidth = w;
        if (log.isDebugEnabled()) {
            log.debug("setFixedWidth()=" + getFixedWidth());
        }
        _parent.updateSize();
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(int h) {
        fixedHeight = h;
        if (log.isDebugEnabled()) {
            log.debug("setFixedHeight()=" + getFixedHeight());
        }
        _parent.updateSize();
    }

    public void setFixedSize(int w, int h) {
        fixedWidth = w;
        fixedHeight = h;
        if (log.isDebugEnabled()) {
            log.debug("setFixedSize()=" + "(" + getFixedWidth() + "," + getFixedHeight() + ")");
        }
        _parent.updateSize();
    }

    //////////////////// menu methods /////////////////////////
    
    public void propertyUtil(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("MenuItemProperties") + "...");
        edit.addActionListener((ActionEvent e) -> {
            _propertiesUtil.display();
        });
        popup.add(edit);
    }

    public void setFixedTextMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFixed"));
        if (getFixedWidth() == 0) {
            edit.add("Width= Auto");
        } else {
            edit.add("Width= " + _parent.getWidth());
        }

        if (getFixedHeight() == 0) {
            edit.add("Height= Auto");
        } else {
            edit.add("Height= " + _parent.getHeight());
        }

        edit.add(CoordinateEdit.getFixedSizeEditAction(_parent));
        popup.add(edit);
    }

    public void setTextMarginMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditMargin"));
        if ((fixedHeight == 0) || (fixedWidth == 0)) {
            edit.add("Margin= " + _parent.getMarginSize());
            edit.add(CoordinateEdit.getMarginEditAction(_parent));
        }
        popup.add(edit);
    }

    protected JMenu makeFontMenu() {
        JMenu fontMenu = new JMenu("Font"); // create font menu
        //fontMenu.setMnemonic('n'); // set mnemonic to n

        // get the current font family name
        String defaultFontFamilyName = _parent.getFont().getFamily();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontFamilyNames[] = ge.getAvailableFontFamilyNames();

        // create radiobutton menu items for font names
        ButtonGroup fontButtonGroup = new ButtonGroup(); // manages font names

        // create Font radio button menu items
        for (String fontFamilyName : fontFamilyNames) {
            // create its menu item
            JCheckBoxMenuItem fontMenuItem = new JCheckBoxMenuItem(fontFamilyName);
            Font menuFont = fontMenuItem.getFont();
            menuFont = new Font(fontFamilyName, menuFont.getStyle(), menuFont.getSize());
            fontMenuItem.setFont(menuFont);

            // set its action listener
            fontMenuItem.addActionListener((ActionEvent e) -> {
                Font oldFont = _parent.getFont();
                Font newFont = new Font(fontFamilyName, oldFont.getStyle(), oldFont.getSize());
                if (!oldFont.equals(newFont)) {
                    _parent.setFont(newFont);
                }
            });

            // add to button group
            fontButtonGroup.add(fontMenuItem);
            // set (de)selected
            fontMenuItem.setSelected(defaultFontFamilyName.equals(fontFamilyName));
            // add to font menu
            fontMenu.add(fontMenuItem);
        }

        MenuScroller.setScrollerFor(fontMenu, 36);

        return fontMenu;
    }
/*    
    public void setTextBorderMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditBorder"));
        edit.add("Border Size= " + _borderSize);
        edit.add(CoordinateEdit.getBorderEditAction(_parent));
        JMenu colorMenu = new JMenu(Bundle.getMessage("BorderColorMenu"));
        makeColorMenu(colorMenu, BORDER_COLOR);
        edit.add(colorMenu);
        popup.add(edit);
    }

    public void setTextFontMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFont"));
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        JMenu colorMenu = new JMenu(Bundle.getMessage("FontColor"));
        makeColorMenu(colorMenu, PositionablePopupUtil.FONT_COLOR);
        edit.add(colorMenu);
        popup.add(edit);
    }
*/
    protected JMenu makeFontSizeMenu() {
        JMenu sizeMenu = new JMenu("Font Size");
        ButtonGroup buttonGrp = new ButtonGroup();
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 6);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 8);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 10);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 11);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 12);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 14);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 16);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 18);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 20);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 24);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 28);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 32);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 36);
        return sizeMenu;
    }

    void addFontSizeMenuEntry(JMenu menu, ButtonGroup fontButtonGroup, final int size) {
        JRadioButtonMenuItem r = new JRadioButtonMenuItem("" + size);
        r.addActionListener((ActionEvent e) -> {
            setFontSize(size);
        });
        fontButtonGroup.add(r);
        r.setSelected(_parent.getFont().getSize() == size);
        menu.add(r);
    }
    protected JMenu makeFontStyleMenu() {
        JMenu styleMenu = new JMenu(Bundle.getMessage("FontStyle"));
        styleMenu.add(italic = newStyleMenuItem(new AbstractAction(Bundle.getMessage("Italic")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled()) { // Avoid action lookup unless needed
                    log.debug("When style item selected {} italic state is {}", getValue(NAME), italic.isSelected());
                }
                if (italic.isSelected()) {
                    setFontStyle(Font.ITALIC, 0);
                } else {
                    setFontStyle(0, Font.ITALIC);
                }
            }
        }, Font.ITALIC));

        styleMenu.add(bold = newStyleMenuItem(new AbstractAction(Bundle.getMessage("Bold")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled()) { // Avoid action lookup unless needed
                    log.debug("When style item selected {} bold state is {}",
                            getValue(NAME), bold.isSelected());
                }
                if (bold.isSelected()) {
                    setFontStyle(Font.BOLD, 0);
                } else {
                    setFontStyle(0, Font.BOLD);
                }
            }
        }, Font.BOLD));
        return styleMenu;
    }

    protected JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String) a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (log.isDebugEnabled()) { // Avoid action lookup unless needed
            log.debug("When creating style item {} mask was {} state was {}",
                     a.getValue(AbstractAction.NAME), mask, getFontStyle());
        }
        if ((mask & getFontStyle()) == mask) {
            c.setSelected(true);
        }
        return c;
    }

    public void setFontSize(float newSize) {
        _parent.setFont(_parent.getFont().deriveFont(newSize));
        _parent.updateSize();
    }

    public int getFontSize() {
        return _parent.getFont().getSize();
    }

    void setItalic() {
        log.debug("When style item selected italic state is {}", italic.isSelected());
        if (italic.isSelected()) {
            setFontStyle(Font.ITALIC, 0);
        } else {
            setFontStyle(0, Font.ITALIC);
        }
    }

    void setBold() {
        log.debug("When style item selected bold state is {}", bold.isSelected());
        if (bold.isSelected()) {
            setFontStyle(Font.BOLD, 0);
        } else {
            setFontStyle(0, Font.BOLD);
        }
    }

    public void setFontStyle(int style) {
        _parent.setFont(_parent.getFont().deriveFont(style));
        _parent.updateSize();
    }

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFontStyle() & ~dropStyle) | addStyle;
        log.debug("setFontStyle: addStyle={}, dropStyle={}, net styleValue is {}", addStyle, dropStyle, styleValue);
        if (bold != null) {
            bold.setSelected((styleValue & Font.BOLD) != 0);
        }
        if (italic != null) {
            italic.setSelected((styleValue & Font.ITALIC) != 0);
        }
        _parent.setFont(_parent.getFont().deriveFont(styleValue));
        _parent.updateSize();
    }

    public int getFontStyle() {
        return _parent.getFont().getStyle();
    }

/*
    protected ButtonGroup makeColorMenu(JMenu colorMenu, int type) {
        ButtonGroup buttonGrp = new ButtonGroup();
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Black"), Color.black, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("DarkGray"), Color.darkGray, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Gray"), Color.gray, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("LightGray"), Color.lightGray, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("White"), Color.white, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Red"), Color.red, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Orange"), Color.orange, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Yellow"), Color.yellow, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Green"), Color.green, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Blue"), Color.blue, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Magenta"), Color.magenta, type);
        if (type == BACKGROUND_COLOR) {
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("ColorClear"), null, type);
        }
        return buttonGrp;
    }

    protected void addColorMenuEntry(JMenu menu, ButtonGroup colorButtonGroup,
            final String name, Color color, final int colorType) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            Color desiredColor;

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (colorType) {
                    case FONT_COLOR:
                        _textComponent.setForeground(desiredColor);
                        break;
                    case BACKGROUND_COLOR:
                        setBackgroundColor(desiredColor);
                        break;
                    case BORDER_COLOR:
                        setBorderColor(desiredColor);
                        break;
                    default:
                        log.warn("Unhandled color type code: {}", colorType);
                        break;
                }
                _parent.getEditor().setAttributes(_self, _parent);
            }

            ActionListener init(Color c) {
                desiredColor = c;
                return this;
            }
        }.init(color);
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        if (log.isDebugEnabled()) {
            log.debug("setColorButton: colorType=" + colorType);
        }
        switch (colorType) {
            case FONT_COLOR:
                if (color == null) {
                    setColorButton(_textComponent.getForeground(), _textComponent.getForeground(), r);
                } else {
                    setColorButton(_textComponent.getForeground(), color, r);                    
                }
                break;
            case BACKGROUND_COLOR:
                if (color == null) {
                    setColorButton(_textComponent.getBackground(), _textComponent.getBackground(), r);
                } else {
                    setColorButton(_textComponent.getBackground(), color, r);                    
                }
                break;
            case BORDER_COLOR:
                if (color == null) {
                    setColorButton(getBorderColor(), _parent.getBackground(), r);
                } else {
                    setColorButton(_parent.getBackground(), color, r);                    
                }
                break;
            default:
                log.warn("Unhandled color type code: {}", colorType);
                break;
        }
        colorButtonGroup.add(r);
        menu.add(r);
    }

    protected void setColorButton(Color color, Color buttonColor, JRadioButtonMenuItem r) {
        if (log.isDebugEnabled()) { // Avoid color to string computations unless needed
            log.debug("setColorButton: color = {} (RGB = {}) buttonColor = {} (RGB = {})",
                    color, (color == null ? "" : color.getRGB()),
                    buttonColor, (buttonColor == null ? "" : buttonColor.getRGB()));
        }
        if (buttonColor != null) {
            if (color != null && buttonColor.getRGB() == color.getRGB()) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
        } else if (color == null) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
    }

///>>>>>>> branch 'pc-PaintByCoodAlias' of https://github.com/petecressman/JMRI.git*/
    public void copyItem(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem("Copy");
        edit.addActionListener((ActionEvent e) -> {
            _parent.getEditor().copyItem(_parent);
        });
        popup.add(edit);
    }

    /*
     * ************* Justification ***********************
     */
    public void setTextJustificationMenu(JPopupMenu popup) {
        JMenu justMenu = new JMenu(Bundle.getMessage("Justification"));
        addJustificationMenuEntry(justMenu, LEFT);
        addJustificationMenuEntry(justMenu, RIGHT);
        addJustificationMenuEntry(justMenu, CENTRE);
        popup.add(justMenu);
    }

    static public final int LEFT = 0x00;
    static public final int RIGHT = 0x02;
    static public final int CENTRE = 0x04;

    private int justification = CENTRE; //Default is always Centre

    public void setJustification(int just) {
        log.debug("setJustification: justification={}", just);
        justification = just;
        _parent.updateSize();
    }

    public void setJustification(String just) {
        log.debug("setJustification: justification ={}", just);
        switch (just) {
            case "right":
                justification = RIGHT;
                break;
            case "center":
            case "centre":
                // allow US or UK spellings
                justification = CENTRE;
                break;
            default:
                justification = LEFT;
                break;
        }
        _parent.updateSize();
    }

    public int getJustification() {
        log.debug("getJustification: justification ={}", justification);
        return justification;
    }

    void addJustificationMenuEntry(JMenu menu, final int just) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch (just) {
            case RIGHT:
                r = new JRadioButtonMenuItem(Bundle.getMessage("right"));
                break;
            case CENTRE:
                r = new JRadioButtonMenuItem(Bundle.getMessage("center"));
                break;
            case LEFT:
            default:
                r = new JRadioButtonMenuItem(Bundle.getMessage("left"));
        }
        r.addActionListener((ActionEvent e) -> {
            setJustification(just);
        } //final int justification = just;
        );
        justButtonGroup.add(r);
        if (justification == just) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
    }

    public final static int HORIZONTAL = 0x00;
    public final static int VERTICAL_UP = 0x01;
    public final static int VERTICAL_DOWN = 0x02;

    private int orientation = HORIZONTAL;

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int ori) {
        orientation = ori;
        _parent.updateSize();
    }

    public void setOrientation(String ori) {
        if (ori.equals("vertical_up")) {
            _parent.setDegrees(90);
//            setOrientation(VERTICAL_UP);
        } else if (ori.equals("vertical_down")) {
            _parent.setDegrees(-90);
//            setOrientation(VERTICAL_DOWN);
        } else {
            _parent.setDegrees(0);
//            setOrientation(HORIZONTAL);
        }
    }

    public void setTextOrientationMenu(JPopupMenu popup) {
        JMenu oriMenu = new JMenu(Bundle.getMessage("Orientation"));
        addOrientationMenuEntry(oriMenu, HORIZONTAL);
        addOrientationMenuEntry(oriMenu, VERTICAL_UP);
        addOrientationMenuEntry(oriMenu, VERTICAL_DOWN);
        popup.add(oriMenu);
    }

    void addOrientationMenuEntry(JMenu menu, final int ori) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch (ori) {
            default:
            case HORIZONTAL:
                r = new JRadioButtonMenuItem("Horizontal");
                break;
            case VERTICAL_UP:
                r = new JRadioButtonMenuItem("Vertical Up");
                break;
            case VERTICAL_DOWN:
                r = new JRadioButtonMenuItem("Vertical Down");
                break;
        }
        r.addActionListener((ActionEvent e) -> {
            setOrientation(ori);
        });
        justButtonGroup.add(r);
        if (orientation == ori) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
    }

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<>(0);

    /**
     * Add a menu item to be displayed when the popup menu is called for when in
     * edit mode.
     *
     * @param menu the item to add
     */
    public void addEditPopUpMenu(JMenuItem menu) {
        if (!editAdditionalMenu.contains(menu)) {
            editAdditionalMenu.add(menu);
        }
    }

    /**
     * Add a menu item to be displayed when the popup menu is called for when in
     * view mode.
     *
     * @param menu menu item or submenu to add
     */
    public void addViewPopUpMenu(JMenuItem menu) {
        if (!viewAdditionalMenu.contains(menu)) {
            viewAdditionalMenu.add(menu);
        }
    }

    /**
     * Add the menu items to the edit popup menu
     *
     * @param popup the menu to add items to
     */
    public void setAdditionalEditPopUpMenu(JPopupMenu popup) {
        if (editAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        editAdditionalMenu.forEach((mi) -> {
            popup.add(mi);
        });
    }

    /**
     * Add the menu items to the view popup menu.
     *
     * @param popup the menu to add items to
     */
    public void setAdditionalViewPopUpMenu(JPopupMenu popup) {
        if (viewAdditionalMenu.isEmpty()) {
            return;
        }
        viewAdditionalMenu.forEach((mi) -> {
            popup.add(mi);
        });
    }

    private final static Logger log = LoggerFactory.getLogger(PositionablePopupUtil.class);
}
