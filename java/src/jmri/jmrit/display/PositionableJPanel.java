package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob Jacobsen copyright (C) 2009
 */
abstract public class PositionableJPanel extends Positionable implements MouseListener, MouseMotionListener {
    
    private final JTextField _textComponent;
    private final JComponent _bodyComponent;
    
    public PositionableJPanel(Editor editor) {
        super(editor);
        setLayout(new java.awt.GridBagLayout());
        _bodyComponent = getContainerComponent();
        add(_bodyComponent, new java.awt.GridBagConstraints());
//        invalidate();

        _textComponent = getTextField();

//        Dimension dim = getPreferredSize();
//        super.setFixedSize(dim.width, dim.height);
    }
    
    abstract protected JTextField getTextField();
    abstract protected JComponent getContainerComponent();
    abstract public String getText();
    abstract public boolean setIconEditMenu(javax.swing.JPopupMenu popup);

    /**
     * Provides a generic method to return the bean associated with the
     * Positionable
     */
    @Override
    abstract public jmri.NamedBean getNamedBean();

    public JComponent getContainer() {
        return _bodyComponent;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        _editor.mousePressed(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        _editor.mouseReleased(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        _editor.mouseClicked(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseExited(MouseEvent e) {
//    	transferFocus();
        _editor.mouseExited(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        _editor.mouseEntered(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        _editor.mouseMoved(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        _editor.mouseDragged(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    /**************************************************************/

    @Override
    public int getWidth() {
        int width = _bodyComponent.getPreferredSize().width;;
        width += (getBorderSize() + getMarginSize()) * 2;            
        if (log.isDebugEnabled()) {
            log.debug("width= " + width + " preferred width= " + getPreferredSize().width);
        }
        return width;
    }

    @Override
    public int getHeight() {
        int height = _bodyComponent.getPreferredSize().height;
        height += (getBorderSize() + getMarginSize()) * 2;
        if (log.isDebugEnabled()) {
            log.debug("height= " + height + " preferred height= " + getPreferredSize().height);
        }
        return height;
    }

    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        _bodyComponent.setForeground(c);
    }

    @Override
    public Color getForeground() {
        return _bodyComponent.getForeground();
    }

    @Override
    public void setFont(Font font) {
        Font oldFont = _bodyComponent.getFont();
        Font newFont = new Font(font.getFamily(), oldFont.getStyle(), oldFont.getSize());
        if (!oldFont.equals(newFont)) {
            _bodyComponent.setFont(newFont);
            super.setFont(newFont);
            updateSize();
        }
    }

    @Override
    public Font getFont() {
        return _bodyComponent.getFont();
    }

    /*
     ****************** Fixed width & height *************** 
     *
    @Override
    public void setFixedSize(int w, int h) {
        _bodyComponent.setPreferredSize(new Dimension(w, h));
        super.setFixedSize(w, h);
    }

    @Override
    public int getFixedWidth() {
        return getPreferredSize().width;
    }

    @Override
    public void setFixedWidth(int w) {
        Dimension dim = getPreferredSize();
        _bodyComponent.setPreferredSize(new Dimension(w, dim.height));
        super.setFixedWidth(w);
    }

    @Override
    public int getFixedHeight() {
        return getPreferredSize().height;
    }

    @Override
    public void setFixedHeight(int h) {
        Dimension dim = getPreferredSize();
        _bodyComponent.setPreferredSize(new Dimension(dim.width, h));
        super.setFixedHeight(h);
    }

    @Override
    public void setJustification(int just) {
        log.debug("setJustification: justification={}", just);
        int justification = getJustification();
        switch (justification) {
            case PositionableJComponent.LEFT:
                _textComponent.setHorizontalAlignment(JTextField.LEFT);
                break;
            case PositionableJComponent.RIGHT:
                _textComponent.setHorizontalAlignment(JTextField.RIGHT);
                break;
            default:
                _textComponent.setHorizontalAlignment(JTextField.CENTER);
        }
        super.setJustification(just);
    }

/*    @Override
    public void paintComponent(Graphics g) {
//        g = getTransfomGraphics(g);

        if (_textComponent instanceof JTextField) {
        }
        super.paintComponent(g); 
    }*/
    
    private final static Logger log = LoggerFactory.getLogger(PositionableJPanel.class.getName());
}
