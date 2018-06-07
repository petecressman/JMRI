package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.Border;
import jmri.util.MenuScroller;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 2016 - change method of painting items to editor's target frame.  Rather than
 * transforming the image of the panel object, paint the image by aliasing the 
 * coordinates of Graphics2D.  This allows animated icons to continue their
 * animation after an AffineTransform.
 * </p>
 * This is the root class for all panel display objects.  It holds the data needed for
 * transforming the position of the object.
 *
 * @author Howard G. Penny copyright (C) 2005
 * @author Pete Cressman copyright (C) 2017
 * @version $Revision$
 */
public class PositionableJComponent extends JComponent implements Positionable {

    protected Editor _editor = null;

    private ToolTip _tooltip;
    private boolean _showTooltip = true;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _viewCoordinates = false;
    private boolean _controlling = true;
    private boolean _hidden = false;
    private int _displayLevel;
    
    protected JFrame _iconEditorFrame;
    protected IconAdder _iconEditor;

    private double _scale = 1.0;         // user's scaling factor
    private int _degree;
    private Dimension _displayDim = new Dimension(0, 0);
    private int _flip;
    public final static int NOFLIP = 0X00;
    public final static int HORIZONTALFLIP = 0X01;
    public final static int VERTICALFLIP = 0X02;
    
    private int _marginSize = 0;
    private int _borderSize = 0;
    private Color _borderColor = null;
    private Color _backgroundColor = null;
    
    private int _fixedWidth = 0;
    private int _fixedHeight = 0;
    private int _justification = CENTRE; //Default is always Centre
    static public final int LEFT = 0x00;
    static public final int RIGHT = 0x02;
    static public final int CENTRE = 0x04;

    protected PositionablePropertiesUtil _propertiesUtil;

    private AffineTransform _transformCA = new AffineTransform();   // Scaled, Rotated & translated for coord alias

    public PositionableJComponent(Editor editor) {
        _editor = editor;
        _scale = 1.0;
        _flip = NOFLIP;
        setOpaque(false);
        _propertiesUtil = new PositionablePropertiesUtil(this);
    }

    @Override
    public Positionable deepClone() {
        PositionableJComponent pos = new PositionableJComponent(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableJComponent pos) {
        pos._scale = _scale;
        pos._degree = _degree;
        pos._flip = _flip;
        pos.setLocation(getX(), getY());
        pos._displayLevel = _displayLevel;
        pos._controlling = _controlling;
        pos._hidden = _hidden;
        pos._positionable = _positionable;
        pos._showTooltip = _showTooltip;
        pos._tooltip = _tooltip;
        pos._editable = _editable;

        pos.setOpaque(isOpaque());
        pos._borderSize = _borderSize;
        pos._borderColor = _borderColor;
        pos._marginSize = _marginSize;
        pos.setBackgroundColor(_backgroundColor);
        pos.setBackground(getBackground());
        pos.setForeground(getForeground());

        pos.updateSize();
        return pos;
    }

    @Override
    public void displayState() {
    }

    /**
     * *************** Positionable methods *********************
     */
    @Override
    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    @Override
    public boolean isPositionable() {
        return _positionable;
    }

    @Override
    public void setEditable(boolean enabled) {
        _editable = enabled;
        showHidden();
    }

    @Override
    public boolean isEditable() {
        return _editable;
    }

    @Override
    public void setViewCoordinates(boolean enabled) {
        _viewCoordinates = enabled;
    }

    @Override
    public boolean getViewCoordinates() {
        return _viewCoordinates;
    }

    @Override
    public void setControlling(boolean enabled) {
        _controlling = enabled;
    }

    @Override
    public boolean isControlling() {
        return _controlling;
    }

    @Override
    public void setHidden(boolean hide) {
        _hidden = hide;
    }

    @Override
    public boolean isHidden() {
        return _hidden;
    }

    @Override
    public void showHidden() {
        if (!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    /**
     * Delayed setDisplayLevel for DnD
     * @param l display level
     */
    public void setLevel(int l) {
        _displayLevel = l;
    }

    @Override
    public void setDisplayLevel(int l) {
        int oldDisplayLevel = _displayLevel;
        _displayLevel = l;
        if (oldDisplayLevel != l) {
            if (_editor != null) {
                _editor.displayLevelChange(this);
            }
            log.debug("Changing label display level from " + oldDisplayLevel + " to " + _displayLevel);
        }
    }

    @Override
    public int getDisplayLevel() {
        return _displayLevel;
    }

    @Override
    public void setShowToolTip(boolean set) {
        _showTooltip = set;
    }

    @Override
    public boolean showToolTip() {
        return _showTooltip;
    }

    @Override
    public void setToolTip(ToolTip tip) {
        _tooltip = tip;
    }

    @Override
    public ToolTip getToolTip() {
        return _tooltip;
    }
    
    @Override
    public void setScale(double s) {
        if (s < .01) {
            _scale = .1;
            log.error(getName()+" Scale cannot be less than 1%!!");
        } else {
            _scale = s;            
        }
        updateSize();
        displayState();
    }

    @Override
    public final double getScale() {
        return _scale;
    }

    @Override
    public void setDegrees(int deg) {
        _degree = deg % 360;
        updateSize();
        displayState();
    }

    @Override
    public final int getDegrees() {
        return _degree;
    }

    @Override
    public final void setBorderSize(int border) {
        _borderSize = border;
    }

    @Override
    public int getBorderSize() {
        return _borderSize;
    }

    @Override
    public final void setBorderColor(Color color) {
        _borderColor = color;
    }

    @Override
    public Color getBorderColor() {
        return _borderColor;
    }

    @Override
    public final void setMarginSize(int margin) {
        _marginSize = margin;
    }

    @Override
    public int getMarginSize() {
        return _marginSize;
    }

    @Override
    public final void setBackgroundColor(Color color) {
        _backgroundColor = color;
    }

    @Override
    public Color getBackgroundColor() {
        return _backgroundColor;
    }

    /*    public void setBackground(Color c) {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
        comps[i].setBackground(c);
    }
    super.setBackground(c);
}*/

    /*
     ****************** Fixed width & height *************** 
     */
    @Override
    public void setFixedSize(int w, int h) {
        _fixedWidth = w;
        _fixedHeight = h;
    }

    @Override
    public int getFixedWidth() {
        return _fixedWidth;
    }

    @Override
    public void setFixedWidth(int w) {
        _fixedWidth = w;
        if (log.isDebugEnabled()) {
            log.debug("setFixedWidth()=" + getFixedWidth());
        }
        updateSize();
    }

    @Override
    public int getFixedHeight() {
        return _fixedHeight;
    }

    @Override
    public void setFixedHeight(int h) {
        _fixedHeight = h;
        if (log.isDebugEnabled()) {
            log.debug("setFixedHeight()=" + getFixedHeight());
        }
        updateSize();
    }
    
    @Override
    public Font getFont() {
        Font f = super.getFont();
        if (f == null) {
            f = new Font(Font.DIALOG, Font.PLAIN, 12);
        }
        return f;
    }

    @Override
    public void setFontStyle(int styleValue) {
        setFont(getFont().deriveFont(styleValue));
        updateSize();
    }

    @Override
    public void setFontSize(float newSize) {
        Font f = getFont();
        setFont(f.deriveFont(newSize));
        updateSize();
    }

    @Override
    public void setJustification(int just) {
        log.debug("setJustification: justification={}", just);
        _justification = just;
        updateSize();
    }

    @Override
    public int getJustification() {
        log.debug("getJustification: justification ={}", _justification);
        return _justification;
    }

    @Override
    public String getNameString() {
        return getName();
    }

    @Override
    public final Editor getEditor() {
        return _editor;
    }

    @Override
    public final void setEditor(Editor ed) {
        _editor = ed;
    }

    // overide where used - e.g. momentary
    @Override
    public void doMousePressed(MouseEvent event) {
    }

    @Override
    public void doMouseReleased(MouseEvent event) {
    }

    @Override
    public void doMouseClicked(MouseEvent event) {
    }

    @Override
    public void doMouseDragged(MouseEvent event) {
    }

    @Override
    public void doMouseMoved(MouseEvent event) {
    }

    @Override
    public void doMouseEntered(MouseEvent event) {
    }

    @Override
    public void doMouseExited(MouseEvent event) {
    }

    @Override
    public boolean storeItem() {
        return true;
    }

    //////////////////////////////// Popup Menu methods //////////////////////////////////////
    @Override
    public boolean doViemMenu() {
        return true;
    }

    public void setBackgroundMenu(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("FontBackgroundColor"));
        edit.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("FontBackgroundColor"),
                                 this.getBackgroundColor());
            if (desiredColor!=null ) {
                this.setBackgroundColor(desiredColor);
           }
        });
 
        popup.add(edit);

    }

    public void setTextMarginMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditMargin"));
        edit.add("Margin= " + getMarginSize());
        edit.add(CoordinateEdit.getMarginEditAction(this));
        popup.add(edit);
    }

    public void setTextBorderMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditBorder"));
        int _borderSize = this.getBorderSize();
        JMenuItem jmi = edit.add("Border Size = " + _borderSize);
        jmi.setEnabled(false);
        edit.add(CoordinateEdit.getBorderEditAction(this));
        JMenuItem colorMenu = new JMenuItem(Bundle.getMessage("BorderColorMenu"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("BorderColorMenu"),
                                 this.getBorderColor());
            if (desiredColor!=null ) {
                this.setBorderColor(desiredColor);
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
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("FontColor"),
                                 this.getForeground());
            if (desiredColor!=null ) {
                this.setForeground(desiredColor);
           }
        });
        edit.add(colorMenu);
        popup.add(edit);
    }

    protected JMenu makeFontMenu() {
        JMenu fontMenu = new JMenu("Font"); // create font menu
        //fontMenu.setMnemonic('n'); // set mnemonic to n

        // get the current font family name
        String defaultFontFamilyName = getFont().getFamily();

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
                Font oldFont = getFont();
                Font newFont = new Font(fontFamilyName, oldFont.getStyle(), oldFont.getSize());
                if (!oldFont.equals(newFont)) {
                    setFont(newFont);
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
        r.setSelected(getFont().getSize() == size);
        menu.add(r);
    }
    JMenuItem italic = null;
    JMenuItem bold = null;

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
                     a.getValue(AbstractAction.NAME), mask, getFont().getStyle());
        }
        if ((mask & getFont().getStyle()) == mask) {
            c.setSelected(true);
        }
        return c;
    }

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFont().getStyle() & ~dropStyle) | addStyle;
        log.debug("setFontStyle: addStyle={}, dropStyle={}, net styleValue is {}", addStyle, dropStyle, styleValue);
        if (bold != null) {
            bold.setSelected((styleValue & Font.BOLD) != 0);
        }
        if (italic != null) {
            italic.setSelected((styleValue & Font.ITALIC) != 0);
        }
        setFont(getFont().deriveFont(styleValue));
    }

    public void setFixedTextMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFixed"));
        int fixedWidth = this.getFixedWidth();
        if (fixedWidth == 0) {
            edit.add("Width= Auto");
        } else {
            edit.add("Width= " + fixedWidth);
        }

        int fixedHeight = this.getFixedHeight();
        if (fixedHeight == 0) {
            edit.add("Height= Auto");
        } else {
            edit.add("Height= " + fixedHeight);
        }

        edit.add(CoordinateEdit.getFixedSizeEditAction(this));
        popup.add(edit);
    }
    
    public void propertyUtil(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("MenuItemProperties") + "...");
        edit.addActionListener((ActionEvent e) -> {
            _propertiesUtil.display();
        });
        popup.add(edit);
    }

    public void setTextJustificationMenu(JPopupMenu popup) {
        JMenu justMenu = new JMenu(Bundle.getMessage("Justification"));
        justMenu.add(addJustificationMenuEntry(PositionableJComponent.LEFT));
        justMenu.add(addJustificationMenuEntry(PositionableJComponent.RIGHT));
        justMenu.add(addJustificationMenuEntry(PositionableJComponent.CENTRE));
        popup.add(justMenu);
    }
/*
    public void setJustification(String just) {
        log.debug("setJustification: justification ={}", just);
        switch (just) {
            case "right":
                setJustification(PositionableJComponent.RIGHT);
                break;
            case "center":
            case "centre":
                // allow US or UK spellings
                setJustification(PositionableJComponent.CENTRE);
                break;
            default:
                setJustification(PositionableJComponent.LEFT);
                break;
        }
    }*/

    private JRadioButtonMenuItem addJustificationMenuEntry(final int just) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch (just) {
            case PositionableJComponent.RIGHT:
                r = new JRadioButtonMenuItem(Bundle.getMessage("right"));
                break;
            case PositionableJComponent.CENTRE:
                r = new JRadioButtonMenuItem(Bundle.getMessage("center"));
                break;
            case PositionableJComponent.LEFT:
            default:
                r = new JRadioButtonMenuItem(Bundle.getMessage("left"));
        }
        r.addActionListener((ActionEvent e) -> {
            setJustification(just);
        } //final int justification = just;
        );
        justButtonGroup.add(r);
        
        if (getJustification() == just) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
       return r;
    }

    ////////////////////// Orientation /////////////////////////
    
    public final static int HORIZONTAL = 0x00;
    public final static int VERTICAL_UP = 270;
    public final static int VERTICAL_DOWN = 90;

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
        if (getDegrees() == ori) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
    }

    public void setOrientation(int ori) {
        switch (ori) {
            default:
            case HORIZONTAL:
                setDegrees(0);
                break;
            case VERTICAL_UP:
                setDegrees(-90);
                break;
            case VERTICAL_DOWN:
                setDegrees(90);
                break;
        }
    }

    public void copyItem(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem("Copy");
        edit.addActionListener((ActionEvent e) -> {
            getEditor().copyItem(this);
        });
        popup.add(edit);
    }

    @Override
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setRotateMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setDisableControlMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
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

    /*
     * Utility
     */
    protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor != null) {
            _iconEditor = editor;
        } else {
            _iconEditor = new IconAdder(name);
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                _iconEditorFrame.dispose();
                _iconEditorFrame = null;
            }
        });
        _iconEditor.setParent(_iconEditorFrame);
        _iconEditorFrame.setLocationRelativeTo(pos);
        _iconEditorFrame.toFront();
        _iconEditorFrame.setVisible(true);
    }

    @Override
    public void setFlip(int f) {
        _flip = f;
        updateSize();
    }
    int getFlip() {
        return _flip;
    }

    /**
     * ************** end Positionable methods *********************
     */
    /**
     * Removes this object from display and persistance
     */
    @Override
    public void remove() {
        _editor.removeFromContents(this);
        dispose();
        // remove from persistence by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done
     */
    public void dispose() {
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     * @return should object be stored.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Provides a generic method to return the bean associated with the
     * Positionable
     */
    @Override
    public jmri.NamedBean getNamedBean() {
        return null;
    }
    
    /**
     * This method must be called whenever there is a change in text, rotation,
     * scale, mirror, font size or justification borders or margins.  That is,
     * whenever any property may change the display size of the object.
     * Done so the class "display" members can make more efficient repaints.
     */
    @Override
    public void updateSize() {
        int w = getWidth();
        int h = getHeight();
        int displayWidth = w;
        int displayHeight = h;
        int deg = _degree;
        if (deg<0) {
            deg = 360 + deg;
        }
        if (deg==0) {
            _transformCA = new AffineTransform();
        } else {
            double rad = (Math.PI*deg)/180;
            if (deg <= 90) {
                displayWidth = (int)(Math.round(w*Math.cos(rad)+h*Math.sin(rad)));
                displayHeight = (int)(Math.round(h*Math.cos(rad)+w*Math.sin(rad)));
                _transformCA = AffineTransform.getTranslateInstance(h * Math.sin(rad), 0.0);
            } else if (deg <= 180) {
                displayWidth = (int)(Math.round(-w*Math.cos(rad)+h*Math.sin(rad)));
                displayHeight = (int)(Math.round(-h*Math.cos(rad)+w*Math.sin(rad)));
                _transformCA = AffineTransform.getTranslateInstance(h * Math.sin(rad) - w * Math.cos(rad), -h * Math.cos(rad));
            } else if (deg <= 270) {
                displayWidth = (int)(Math.round(-h*Math.sin(rad)-w*Math.cos(rad)));
                displayHeight = (int)(Math.round(-w*Math.sin(rad)-h*Math.cos(rad)));
                _transformCA = AffineTransform.getTranslateInstance(-w * Math.cos(rad), -w * Math.sin(rad) - h * Math.cos(rad));
            } else {
                displayWidth = (int)(Math.round(-h*Math.sin(rad)+w*Math.cos(rad)));
                displayHeight = (int)(Math.round(-w*Math.sin(rad)+h*Math.cos(rad)));
                _transformCA = AffineTransform.getTranslateInstance(0.0, -w * Math.sin(rad));
            }
            AffineTransform transformR = AffineTransform.getRotateInstance(rad);
            _transformCA.concatenate(transformR);
        }
        if (_scale >= .01) {
             AffineTransform transformS = AffineTransform.getScaleInstance(_scale, _scale);                
            displayWidth = (int)Math.round(_scale*displayWidth);
            displayHeight = (int)Math.round(_scale*displayHeight);
            _transformCA.concatenate(transformS);
        }
        if (_flip != NOFLIP) {
            AffineTransform transformF;    // Flipped or Mirrored
            if (_flip == HORIZONTALFLIP) {
                transformF = AffineTransform.getScaleInstance(-1, 1);
                transformF.concatenate(AffineTransform.getTranslateInstance(-getWidth(), 0));
            } else if (_flip == VERTICALFLIP) {
                transformF = AffineTransform.getScaleInstance(1, -1);
                transformF.concatenate(AffineTransform.getTranslateInstance(0, -getHeight()));
            } else {
                transformF = new AffineTransform(); // keep compiler happy
            }
            _transformCA.concatenate(transformF);            
        }
        _displayDim = new Dimension(displayWidth, displayHeight);
        setPreferredSize(_displayDim);
        setSize(_displayDim);

        if (_editor!=null && _editor.getTargetPanel()!=null) {
            _editor.getTargetPanel().repaint();
        }
        setBorder();
        System.out.println("updateSize: displayWidth="+displayWidth+" displayHeight="+displayHeight+
                " NameString="+" \""+getNameString()+"\"");
        repaint();
    }
    
    @Override
    public final AffineTransform getTransform() {
        return _transformCA;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return _displayDim;
    }

    @Override
    public Rectangle getContentBounds(Rectangle r) {
        return super.getBounds(r);
    }

    @Override
    public Rectangle getBounds() {
        Rectangle bds = super.getBounds();
        if (bds!=null && _displayDim!=null) {
            return new Rectangle(bds.x, bds.y, _displayDim.width, _displayDim.height);            
        } else {
            return bds;
        }
    }
    
    @Override
    public Rectangle getBounds(Rectangle b) {
        Rectangle bds = super.getBounds(b);
        if (bds!=null && _displayDim!=null) {
            bds.width = _displayDim.width;
            bds.height = _displayDim.height;
            return bds;
        } else {
            return bds;
        }
    }
    
    @Override
    public void setBorder() {
        Color color = getBackgroundColor();
        setOpaque(color != null);
        super.setBackground(color);
        Border borderMargin;        
        if (isOpaque()) {
            borderMargin = BorderFactory.createLineBorder(color, _marginSize);
        } else {
            borderMargin = BorderFactory.createEmptyBorder(_marginSize, _marginSize, _marginSize, _marginSize);
        }
        color = getBorderColor();
        Border borderOutline;
        if (color != null) {
            borderOutline = BorderFactory.createLineBorder(color, _borderSize);
        } else {
            borderOutline = BorderFactory.createEmptyBorder(_borderSize, _borderSize, _borderSize, _borderSize);
        }
        if (_marginSize > 0 || _borderSize > 0) {
            super.setBorder(new javax.swing.border.CompoundBorder(borderOutline, borderMargin));            
        }
    }

/*    protected Graphics getTransfomGraphics(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.transform(getTransform());

        // set antialiasing hint for macOS and Windows
        // note: antialiasing has performance problems on constrained systems
        // like the Raspberry Pi, assuming Linux variants are constrained
        if (SystemType.isMacOSX() || SystemType.isWindows()) {
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            // Turned off due to poor performance, see Issue #3850 and PR #3855 for background
            // g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            //        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }

        java.awt.Color backgroundColor = getBackgroundColor();
        if (backgroundColor!=null) {
            setOpaque(true);
            setBackground(backgroundColor);
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            setOpaque(false);
        }
        super.paintBorder(g2d);
        return g2d;
    }*/

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.transform(getTransform());

        // set antialiasing hint for macOS and Windows
        // note: antialiasing has performance problems on constrained systems
        // like the Raspberry Pi, assuming Linux variants are constrained
        if (SystemType.isMacOSX() || SystemType.isWindows()) {
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            // Turned off due to poor performance, see Issue #3850 and PR #3855 for background
            // g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            //        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }

        java.awt.Color backgroundColor = getBackgroundColor();
        if (backgroundColor!=null) {
            setOpaque(true);
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            setOpaque(false);
        }
        super.paintBorder(g2d);
        super.paint(g2d);
        g2d.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponent.class);
}
