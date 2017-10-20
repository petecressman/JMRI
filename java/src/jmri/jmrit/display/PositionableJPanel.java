package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Jacobsen copyright (C) 2009
 */
public class PositionableJPanel extends JPanel implements Positionable, MouseListener, MouseMotionListener {

    protected Editor _editor = null;
    protected boolean debug = false;

    private ToolTip _tooltip;
    protected boolean _showTooltip = true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = false;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected int _displayLevel;
    private double _scale = 1.0;    // scaling factor
    private int _degree;
    PositionablePopupUtil _popupUtil;

    private AffineTransform _transformS = new AffineTransform();    // Scaled
    private AffineTransform _transformR = new AffineTransform();   // Scaled & Rotated
    private AffineTransform _transformCA = new AffineTransform();   // Scaled, Rotated & translated for coord alias

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;

    public PositionableJPanel(Editor editor) {
        _editor = editor;
        debug = log.isDebugEnabled();
    }

    @Override
    public Positionable deepClone() {
        PositionableJPanel pos = new PositionableJPanel(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableJPanel pos) {
        pos.setLocation(getX(), getY());
        pos._displayLevel = _displayLevel;
        pos._controlling = _controlling;
        pos._hidden = _hidden;
        pos._positionable = _positionable;
        pos._showTooltip = _showTooltip;
        pos.setToolTip(getToolTip());
        pos._editable = _editable;
        if (getPopupUtility() == null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone());
        }
        pos.updateSize();
        return pos;
    }

    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    public boolean isPositionable() {
        return _positionable;
    }

    public void setEditable(boolean enabled) {
        _editable = enabled;
    }

    public boolean isEditable() {
        return _editable;
    }

    public void setViewCoordinates(boolean enabled) {
        _viewCoordinates = enabled;
    }

    public boolean getViewCoordinates() {
        return _viewCoordinates;
    }

    public void setControlling(boolean enabled) {
        _controlling = enabled;
    }

    public boolean isControlling() {
        return _controlling;
    }

    public void setHidden(boolean hide) {
        _hidden = hide;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void showHidden() {
        if (!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    /**
     * Delayed setDisplayLevel for DnD
     */
    public void setLevel(int l) {
        _displayLevel = l;
    }

    public void setDisplayLevel(int l) {
        int oldDisplayLevel = _displayLevel;
        _displayLevel = l;
        if (oldDisplayLevel != l) {
            log.debug("Changing label display level from " + oldDisplayLevel + " to " + _displayLevel);
            _editor.displayLevelChange(this);
        }
    }

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
    
    public void displayState() {
    }

    public void setScale(double s) {
        if (s < .1) {
            _scale = .1;
            log.error(getName()+" Scale cannot be less than 10%!!");
        } else {
            _scale = s;            
        }
        _transformS = AffineTransform.getScaleInstance(_scale, _scale);
        updateSize();
        displayState();
    }


    public double getScale() {
        return _scale;
    }

    public void setDegrees(int deg) {
        _degree = deg % 360;
        updateSize();
        displayState();
    }

    public final int getDegrees() {
        return _degree;
    }

    public void setFlip(int f) {
    }

    public JComponent getTextComponent() {
        return _popupUtil._textComponent;
    }

    public String getNameString() {
        return getName();
    }

    public Editor getEditor() {
        return _editor;
    }

    public void setEditor(Editor ed) {
        _editor = ed;
    }

    public void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }

    public PositionablePopupUtil getPopupUtility() {
        return _popupUtil;
    }
   // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {
    }

    public void doMouseReleased(MouseEvent event) {
    }

    public void doMouseClicked(MouseEvent event) {
    }

    public void doMouseDragged(MouseEvent event) {
    }

    public void doMouseMoved(MouseEvent event) {
    }

    public void doMouseEntered(MouseEvent event) {
    }

    public void doMouseExited(MouseEvent event) {
    }

    public boolean storeItem() {
        return true;
    }

    public boolean doViemMenu() {
        return true;
    }

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setRotateMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setDisableControlMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }

    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    JFrame _iconEditorFrame;
    IconAdder _iconEditor;

    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }

    /**
     * Utility
     */
    protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor != null) {
            _iconEditor = editor;
        } else {
            log.error("IconAdder missing for name={}. cannot make _iconEditorFrame", name);
//            _iconEditor = new IconAdder(name, getEditor());
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                _iconEditorFrame.dispose();
                _iconEditorFrame = null;
            }
        });
        _iconEditorFrame.setLocationRelativeTo(pos);
        _iconEditorFrame.toFront();
        _iconEditorFrame.setVisible(true);
    }

    void edit() {
    }

    /**
     * ************** end Positionable methods *********************
     */
   /**
     * Removes this object from display and persistance
     */
    public void remove() {
        _editor.removeFromContents(this);
        cleanup();
        // remove from persistance by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done
     */
    void cleanup() {
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    public void mousePressed(MouseEvent e) {
        _editor.mousePressed(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseReleased(MouseEvent e) {
        _editor.mouseReleased(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseClicked(MouseEvent e) {
        _editor.mouseClicked(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseExited(MouseEvent e) {
//    	transferFocus();
        _editor.mouseExited(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseEntered(MouseEvent e) {
        _editor.mouseEntered(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseMoved(MouseEvent e) {
        _editor.mouseMoved(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseDragged(MouseEvent e) {
        _editor.mouseDragged(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    /**************************************************************/

    public int getWidth() {
        int width = 0;
        if (_popupUtil != null && _popupUtil.getFixedWidth() != 0) {
            width = _popupUtil.getFixedWidth();
            if (width < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                _popupUtil.setFixedWidth(PositionablePopupUtil.MIN_SIZE);
                width = PositionablePopupUtil.MIN_SIZE;
            }
        } else {
            width = getPreferredSize().width;
            if (width < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                width = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (_popupUtil != null) {
            width += (_popupUtil.getBorderSize() + _popupUtil.getMarginSize()) * 2;
        }
        if (debug) {
            log.debug("width= " + width + " preferred width= " + getPreferredSize().width);
        }
        return width;
    }

    public int getHeight() {
        int height = 0;
        if (_popupUtil != null && _popupUtil.getFixedHeight() != 0) {
            height = _popupUtil.getFixedHeight();
            if (height < PositionablePopupUtil.MIN_SIZE) {   // don't let item disappear
                _popupUtil.setFixedHeight(PositionablePopupUtil.MIN_SIZE);
                height = PositionablePopupUtil.MIN_SIZE;
            }
        } else {
            height = getPreferredSize().height;
            if (height < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                height = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (_popupUtil != null) {
            height += (_popupUtil.getBorderSize() + _popupUtil.getMarginSize()) * 2;
        }
        if (debug) {
            log.debug("height= " + height + " preferred height= " + getPreferredSize().height);
        }
        return height;
    }

    public jmri.NamedBean getNamedBean() {
        return null;
    }
    
    public Rectangle getContentBounds(Rectangle r) {
        return getBounds(r);
    }

    public void updateSize() {
        int w = getWidth();
        int h = getHeight();
        int _displayWidth = w;
        int _displayHeight = h;
        int deg = _degree;
        if (deg<0) {
            deg = 360 + deg;
        }
        if (deg==0) {
            _transformR = new AffineTransform();
            _transformCA = new AffineTransform();
        } else {
            double rad = (Math.PI*deg)/180;
            if (deg <= 90) {
                _displayWidth = (int)(Math.round(w*Math.cos(rad)+h*Math.sin(rad)));
                _displayHeight = (int)(Math.round(h*Math.cos(rad)+w*Math.sin(rad)));
                _transformCA = AffineTransform.getTranslateInstance(h * Math.sin(rad), 0.0);
            } else if (deg <= 180) {
                _displayWidth = (int)(Math.round(-w*Math.cos(rad)+h*Math.sin(rad)));
                _displayHeight = (int)(Math.round(-h*Math.cos(rad)+w*Math.sin(rad)));
                _transformCA = AffineTransform.getTranslateInstance(h * Math.sin(rad) - w * Math.cos(rad), -h * Math.cos(rad));
            } else if (deg <= 270) {
                _displayWidth = (int)(Math.round(-h*Math.sin(rad)-w*Math.cos(rad)));
                _displayHeight = (int)(Math.round(-w*Math.sin(rad)-h*Math.cos(rad)));
                _transformCA = AffineTransform.getTranslateInstance(-w * Math.cos(rad), -w * Math.sin(rad) - h * Math.cos(rad));
            } else {
                _displayWidth = (int)(Math.round(-h*Math.sin(rad)+w*Math.cos(rad)));
                _displayHeight = (int)(Math.round(-w*Math.sin(rad)+h*Math.cos(rad)));
                _transformCA = AffineTransform.getTranslateInstance(0.0, -w * Math.sin(rad));
            }
            _transformR = AffineTransform.getRotateInstance(rad);
            _transformCA.concatenate(_transformR);
        }
        if (_scale >= .01) {
            _transformS = AffineTransform.getScaleInstance(_scale, _scale);                
            _displayWidth = (int)Math.round(_scale*_displayWidth);
            _displayHeight = (int)Math.round(_scale*_displayHeight);
            _transformCA.concatenate(_transformS);
        }
        setSize(_displayWidth, _displayHeight);
        repaint();
//      System.out.println("displayWidth= "+_displayWidth+" displayHeight= "+_displayHeight);
    }
    
    public final AffineTransform getTransform() {
        return _transformCA;
    }
    
    public void setBorder() {
        Color color = _popupUtil.getBackgroundColor();
        setBackground(color);
        int size = _popupUtil.getMarginSize();
        Border borderMargin;        
        if (isOpaque()) {
//            borderMargin = new LineBorder(color, size);
            borderMargin = BorderFactory.createLineBorder(color, size);
        } else {
            borderMargin = BorderFactory.createEmptyBorder(size, size, size, size);
        }
        color = _popupUtil.getBorderColor();
        size = _popupUtil.getBorderSize();
        Border outlineBorder;
        if (color != null) {
//            outlineBorder = new LineBorder(color, size);
            outlineBorder = BorderFactory.createLineBorder(color, size);
        } else {
            outlineBorder = BorderFactory.createEmptyBorder(size, size, size, size);
        }
        super.setBorder(new CompoundBorder(outlineBorder, borderMargin));
        
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
//        Graphics2D g2d = (Graphics2D)g;
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.transform(getTransform());
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setClip(null);
        
//        setBorder();
        super.paintBorder(g);

/*        int borderSize = 0;
        java.awt.Color backgroundColor = null;
        java.awt.Color borderColor = null;
        if (_popupUtil!=null) {
            borderSize = _popupUtil.getBorderSize();
            backgroundColor = _popupUtil.getBackgroundColor();
            borderColor = _popupUtil.getBorderColor();
        }
        Rectangle borderRect = new Rectangle (0, 0, getWidth(), getHeight());
        if (backgroundColor!=null) {
            g2d.setColor(backgroundColor);
            g2d.fillRect(borderRect.x, borderRect.y, borderRect.width, borderRect.height);
        }
        if (borderColor!=null && borderSize>0) {
            g2d.setColor(borderColor);
            g2d.setStroke(new java.awt.BasicStroke(borderSize));
            g2d.drawRect(borderRect.x, borderRect.y, borderRect.width, borderRect.height);
        }            
        getTextComponent().setBackground(backgroundColor);
        this.setBackground(backgroundColor);
        this.setOpaque(false);
        if (backgroundColor==null) {
            getTextComponent().setOpaque(false);            
        }
        super.paintComponent(g2d);
        g2d.dispose();
        */
    }
    
    private final static Logger log = LoggerFactory.getLogger(PositionableJPanel.class.getName());
}
