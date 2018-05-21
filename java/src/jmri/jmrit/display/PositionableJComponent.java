package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
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
    
    protected PositionablePopupUtil _popupUtil;
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


    private AffineTransform _transformCA = new AffineTransform();   // Scaled, Rotated & translated for coord alias

    public PositionableJComponent(Editor editor) {
        _editor = editor;
        _scale = 1.0;
        _flip = NOFLIP;
        setOpaque(false);
        setPopupUtility(new PositionablePopupUtil(this));
    }

    @Override
    public Positionable deepClone() {
        PositionableJComponent pos = new PositionableJComponent(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableJComponent pos) {
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

        if (getPopupUtility() == null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone());
        }
        pos.setOpaque(isOpaque());
        pos.setBorderSize(_borderSize);
        pos.setBorderColor(_borderColor);
        pos.setMarginSize(_marginSize);
        pos.setBackgroundColor(_backgroundColor);
        pos.setForeground(getForeground());

        pos.updateSize();
        return pos;
    }

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

    @Override
    public boolean doViemMenu() {
        return true;
    }

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
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

    @Override
    public void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }

    @Override
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
//        System.out.println("updateSize: displayWidth="+displayWidth+" displayHeight="+displayHeight+
//                " NameString="+" \""+getNameString()+"\""+" Name="+" \""+getName()+"\"");
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
    
/*    public void setBackground(Color c) {
        java.awt.Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            comps[i].setBackground(c);
        }
        super.setBackground(c);
    }*/

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

        if (_popupUtil!=null) {
            java.awt.Color backgroundColor = getBackgroundColor();
            if (backgroundColor!=null) {
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
        super.paintBorder(g2d);
        super.paint(g2d);
        g2d.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponent.class);
}
