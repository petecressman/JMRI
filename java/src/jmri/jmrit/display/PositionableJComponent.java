package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 2016 - change method of painting items to editor's target frame.  Rather than
 * transforming the image of the panel object, paint the image by aliasing the 
 * coordinates of Graphics2D.  This allows animated icons to continue their
 * animation after an AffineTransform.
 * </p>
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
    
    protected PositionablePopupUtil _popupUtil;
    protected JFrame _iconEditorFrame;
    protected IconAdder _iconEditor;

    private double _scale = 1.0;         // user's scaling factor
    private int _degree;
    private Dimension _displayDim = new Dimension(0, 0);
    private int _flip;
    private AffineTransform _transformS = new AffineTransform();    // Scaled
    private AffineTransform _transformR = new AffineTransform();   // Scaled & Rotated
    private AffineTransform _transformCA = new AffineTransform();   // Scaled, Rotated & translated for coord alias
    private AffineTransform _transformF = new AffineTransform();    // Flipped or Mirrored

    public PositionableJComponent(Editor editor) {
        _editor = editor;
        _scale = 1.0;
        _flip = NOFLIP;
        JLabel l = new JLabel();
        setFont(l.getFont());
        setOpaque(false);
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    @Override
    public Positionable deepClone() {
        PositionableJComponent pos = new PositionableJComponent(_editor);
        return finishClone(pos);
    }


    public Positionable finishClone(Positionable pos) {
        pos.setScale(_scale);
        pos.setDegrees(_degree);
        pos.setFlip(_flip);
        pos.setLocation(getX(), getY());
        pos.setDisplayLevel(_displayLevel);
        pos.setControlling(_controlling);
        pos.setHidden(_hidden);
        pos.setPositionable(_positionable);
        pos.setShowToolTip(_showTooltip);
        pos.setToolTip(_tooltip);
        pos.setEditable(_editable);
        pos.updateSize();
        return pos;
    }

    public JComponent getTextComponent() {
        return this;
    }

    public void displayState() {
    }

    /**
     * *************** Positionable methods *********************
     */
    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    public boolean isPositionable() {
        return _positionable;
    }

    public void setEditable(boolean enabled) {
        _editable = enabled;
        showHidden();
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
     * @param l display level
     */
    public void setLevel(int l) {
        _displayLevel = l;
    }

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
    
    public void setScale(double s) {
        if (s < .01) {
            _scale = .1;
            log.error(getName()+" Scale cannot be less than 1%!!");
        } else {
            _scale = s;            
        }
        _transformS = AffineTransform.getScaleInstance(_scale, _scale);
        updateSize();
        displayState();
    }

    public final double getScale() {
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

    public String getNameString() {
        return getName();
    }

    public final Editor getEditor() {
        return _editor;
    }

    public final void setEditor(Editor ed) {
        _editor = ed;
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

    public boolean setEditItemMenu(JPopupMenu popup) {
        return false;
    }

    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    public void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }

    public PositionablePopupUtil getPopupUtility() {
        return _popupUtil;
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

    public final static int NOFLIP = 0X00;
    public final static int HORIZONTALFLIP = 0X01;
    public final static int VERTICALFLIP = 0X02;
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
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    public void dispose() {
    }
    /**
     * Removes this object from display and persistance
     */
    public void remove() {
        _editor.removeFromContents(this);
        cleanup();
        // remove from persistence by flagging inactive
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
     * @return should object be stored.
     */
    public boolean isActive() {
        return active;
    }

    public jmri.NamedBean getNamedBean() {
        return null;
    }
    
    /**
     * This method must be called whenever there is a change in text, rotation,
     * scale, mirror, font size or justification borders or margins.  That is,
     * whenever any property may change the display size of the object.
     * Done so the class "display" members can make more efficient repaints.
     */
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
            _transformR = new AffineTransform();
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
            _transformR = AffineTransform.getRotateInstance(rad);
            _transformCA.concatenate(_transformR);
        }
        if (_scale >= .01) {
            _transformS = AffineTransform.getScaleInstance(_scale, _scale);                
            displayWidth = (int)Math.round(_scale*displayWidth);
            displayHeight = (int)Math.round(_scale*displayHeight);
            _transformCA.concatenate(_transformS);
        }
        if (_flip != NOFLIP) {
            if (_flip == HORIZONTALFLIP) {
                _transformF = AffineTransform.getScaleInstance(-1, 1);
                _transformF.concatenate(AffineTransform.getTranslateInstance(-getWidth(), 0));
            } else if (_flip == VERTICALFLIP) {
                _transformF = AffineTransform.getScaleInstance(1, -1);
                _transformF.concatenate(AffineTransform.getTranslateInstance(0, -getHeight()));
            }
            _transformCA.concatenate(_transformF);            
        }
        _displayDim = new Dimension(displayWidth, displayHeight);
        setPreferredSize(_displayDim);
        setSize(_displayDim);

        if (_editor!=null && _editor.getTargetPanel()!=null) {
            _editor.getTargetPanel().repaint();
        }
//        System.out.println("updateSize: displayWidth="+displayWidth+" displayHeight="+displayHeight+
//                " NameString="+" \""+getNameString()+"\""+" Name="+" \""+getName()+"\"");
        repaint();
    }
    
    public final AffineTransform getTransform() {
        return _transformCA;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return _displayDim;
    }

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
    
/*    public void setBackground(Color c) {
        java.awt.Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            comps[i].setBackground(c);
        }
        super.setBackground(c);
    }*/

    public void setBorder() {
        if (_popupUtil == null) {
            return;
        }
        Color color = _popupUtil.getBackgroundColor();
        setOpaque(color != null);
//        super.setBackground(color);
        _popupUtil.setBackground(color);
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
        Border borderOutline;
        if (color != null) {
//            borderOutline = new LineBorder(color, size);
            borderOutline = BorderFactory.createLineBorder(color, size);
        } else {
            borderOutline = BorderFactory.createEmptyBorder(size, size, size, size);
        }
        super.setBorder(new CompoundBorder(borderOutline, borderMargin));
        
    }
    
    @Override
    protected void paintComponent(Graphics g) {

        this.setBorder();
        super.paintBorder(g);
/*        if (!(g instanceof Graphics2D)) {
            return;
        }
//        Graphics2D g2d = (Graphics2D)g;
//        super.paintComponent(g2d);

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
        }*/          
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponent.class);
}
