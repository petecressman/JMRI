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
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Jacobsen copyright (C) 2009
 */
public class PositionableJPanel extends PositionableJComponent implements MouseListener, MouseMotionListener {
    
    private JPanel _itemPanel;

    public PositionableJPanel(Editor editor) {
        super(editor);
    }

    protected void addItem(JPanel panel) {
        _itemPanel = panel;
        add(_itemPanel);
    }
    
    @Override
    public Positionable deepClone() {
        PositionableJPanel pos = new PositionableJPanel(_editor);
        return finishClone(pos);
    }

    @Override
    public Positionable finishClone(Positionable pos) {
        if (getPopupUtility() == null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone());
        }
        pos.updateSize();
        return super.finishClone(pos);
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
        if (log.isDebugEnabled()) {
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
        if (log.isDebugEnabled()) {
            log.debug("height= " + height + " preferred height= " + getPreferredSize().height);
        }
        return height;
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
        super.paint(g2d);

        _itemPanel.paintComponents(g);

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
