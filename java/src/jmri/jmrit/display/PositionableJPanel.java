package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
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
public class PositionableJPanel extends PositionableJComponent implements MouseListener, MouseMotionListener {
    
    private JComponent _textComponent;
    
    public PositionableJPanel(Editor editor) {
        super(editor);
        setLayout(new FlowLayout());
    }
    
    protected void setTextComponent(JComponent t) {
        _textComponent = t;
        Dimension dim = t.getPreferredSize();
        super.setFixedSize(dim.width, dim.height);
    }

    protected JComponent getTextComponent() {
        return _textComponent;
    }
    
    @Override
    public Positionable deepClone() {
        PositionableJPanel pos = new PositionableJPanel(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableJPanel pos) {
        return super.finishClone(pos);
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
        int width = _textComponent.getPreferredSize().width;;
        width += (getBorderSize() + getMarginSize()) * 2;            
        if (log.isDebugEnabled()) {
            log.debug("width= " + width + " preferred width= " + getPreferredSize().width);
        }
        return width;
    }

    @Override
    public int getHeight() {
        int height = _textComponent.getPreferredSize().height;
        height += (getBorderSize() + getMarginSize()) * 2;
        if (log.isDebugEnabled()) {
            log.debug("height= " + height + " preferred height= " + getPreferredSize().height);
        }
        return height;
    }

    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        _textComponent.setForeground(c);
    }

    @Override
    public Color getForeground() {
        return _textComponent.getForeground();
    }

    @Override
    public void setFont(Font font) {
        Font oldFont = _textComponent.getFont();
        Font newFont = new Font(font.getFamily(), oldFont.getStyle(), oldFont.getSize());
        if (!oldFont.equals(newFont)) {
            _textComponent.setFont(newFont);
            super.setFont(newFont);
            updateSize();
        }
    }

    @Override
    public Font getFont() {
        return _textComponent.getFont();
    }

    /*
     ****************** Fixed width & height *************** 
     */
    @Override
    public int getFixedWidth() {
        return getPreferredSize().width;
    }

    @Override
    public void setFixedWidth(int w) {
        Dimension dim = getPreferredSize();
        setPreferredSize(new Dimension(w, dim.height));
        super.setFixedWidth(w);
    }

    @Override
    public int getFixedHeight() {
        return getPreferredSize().height;
    }

    @Override
    public void setFixedHeight(int h) {
        Dimension dim = getPreferredSize();
        setPreferredSize(new Dimension(dim.width, h));
        super.setFixedHeight(h);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (_textComponent instanceof JTextField && _popupUtil!=null) {
            int justification = getJustification();
            switch (justification) {
                case PositionableJComponent.LEFT:
                    ((JTextField) _textComponent).setHorizontalAlignment(JTextField.LEFT);
                    break;
                case PositionableJComponent.RIGHT:
                    ((JTextField) _textComponent).setHorizontalAlignment(JTextField.RIGHT);
                    break;
                default:
                    ((JTextField) _textComponent).setHorizontalAlignment(JTextField.CENTER);
            }
        }
        super.paintComponent(g); 
    }
    
    private final static Logger log = LoggerFactory.getLogger(PositionableJPanel.class.getName());
}
