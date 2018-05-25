package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.IconItemPanel;
import jmri.jmrit.display.palette.ItemPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionableLabel is a container for text and icons
 * <P>
 * The positionable parameter is a global, set from outside. The 'fixed'
 * parameter is local, set from the popup here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 */
public class PositionableLabel extends PositionableJComponent {

    private boolean debug = false;
    private String _textString;
    protected NamedIcon _namedIcon;
    private boolean _icon = false;
    private boolean _text = false;

    public PositionableLabel(Editor editor) {
        super(editor);
    }

    /**
     * {@inheritDoc}
     * @param s text
     * @param editor where this label is displayed
     */
    public PositionableLabel(String s, Editor editor) {
        super(editor);
        _text = true;
        _textString = s;
        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PositionableLabel ctor (text) " + s);
        }
        setPopupUtility(new PositionablePopupUtil(this));
        updateSize();
    }

    public PositionableLabel(NamedIcon s, Editor editor) {
        super(editor);
        _icon = true;
        _namedIcon = s;
        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PositionableLabel ctor (icon) {}", s != null ? s.getName() : null);
       }
        updateSize();
     }

    public final boolean isIcon() {
        return _icon;
    }

    public final boolean isText() {
        return _text;
    }
    
    public final void setIsIcon(boolean b) {
        _icon = b;
    }

    public final void setIsText(boolean b) {
        _text = b;
    }
    
    public void setIcon(NamedIcon icon) {
        _namedIcon = icon;
        updateSize();
    }
    
    public NamedIcon getIcon() {
        return _namedIcon;
    }

    public void setText(String text) {
        _textString = text;
    }
    
    public String getText() {
        return _textString;
    }

    @Override
    public String getNameString() {
        if (_icon) {
            return _namedIcon.getName();
        } else if (_text) {
            return "Text Label";
        } else if (getDisplayLevel() > Editor.BKG) {
            return getName();
        } else {
            return "Background";
        }
    }

    @Override
    public Positionable deepClone() {
        PositionableLabel pos;
        if (_icon) {
            NamedIcon icon = new NamedIcon(_namedIcon);
            pos = new PositionableLabel(icon, _editor);
        } else {
            pos = new PositionableLabel(_textString, _editor);
        }
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableLabel pos) {
        pos._text = _text;
        pos._icon = _icon;
        pos._textString = _textString;
        if (_namedIcon != null) {
            pos._namedIcon = new NamedIcon(_namedIcon);
            pos.setIcon(pos._namedIcon);
        }
        return super.finishClone(pos);
    }

    /**
     * ************** end Positionable methods *********************
     */
    @Override
    public int getWidth() {
        int width = Math.max(getIconWidth(), getTextWidth());
        width += (getBorderSize() + getMarginSize()) * 2;            
        if (log.isDebugEnabled()) {
            log.debug("width= " + width + " preferred width= " + getPreferredSize().width);
        }
        return Math.max(width, PositionablePopupUtil.MIN_SIZE); // don't let item disappear
    }

    @Override
    public int getHeight() {
        int height = Math.max(getIconHeight(), getTextHeight());
        height += (getBorderSize() + getMarginSize()) * 2;
        if (log.isDebugEnabled()) {
            log.debug("height= " + height + " preferred height= " + getPreferredSize().height);
        }
        return Math.max(height, PositionablePopupUtil.MIN_SIZE);    // don't let item disappear
    }
    
    public int getIconWidth() {
        int width = 0;
        if (_icon && _namedIcon != null) {
            width = _namedIcon.getIconWidth();
        }
        return width;
    }
    
    public int getIconHeight() {
        int height = 0;
        if (_icon && _namedIcon != null) {
            height = _namedIcon.getIconHeight();
        }
        return height;
    }
    
    private int getTextWidth() {
        if (!_text) {
            return 0;
        }
        int width = 0;
        if (_popupUtil != null && _popupUtil.getFixedWidth() > 0) {
            width = _popupUtil.getFixedWidth();
        } else {
            if (_textString != null && getFont()!=null) {
                width = getFontMetrics(getFont()).stringWidth(_textString);
            }
        }
        return width;
    }
    
    private int getTextHeight() {
        int height = 0;
        if (_popupUtil != null && _popupUtil.getFixedHeight() > 0) {
            height = _popupUtil.getFixedHeight();
        } else {
            if (_text && _textString != null && getFont()!=null) {
                height = Math.max(height, getFontMetrics(getFont()).getHeight());
            }
        }
        return height;
    }

    /**
     * Scale icon size to be less than width x height
     * @param width width
     * @param height height
     * @param limit - minimum scale factor allowed
     * @return size
     */
    public double reduceTo(int width, int height, double limit) {
        double scale = 1.0;
        if (_namedIcon!=null) {
            int w = getIconWidth();
            int h = getIconHeight();
            if (w > width) {
                scale = ((double) width) / w;
            }
            if (h > height) {
                scale = Math.min(scale, ((double) height) / h);
            }
            scale = Math.max(scale, limit);  // but not too small
        }
        setScale(scale);
        return scale;
    }


    public boolean isBackground() {
        return (getDisplayLevel() == Editor.BKG);
    }

    /*
     * ***** Methods to add menu items to popup *******
     */

    /**
     * PanelEditor Rotate othogonally return true if popup is set
     */
    @Override
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {

        if (isIcon() && getDisplayLevel() > Editor.BKG) {
            // Bundle property includes degree symbol
            popup.add(new AbstractAction(Bundle.getMessage("RotateOrthoSign", getDegrees())) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rotateOrthogonal();
                }
            });
            return true;
        }
        return false;
    }

    protected void rotateOrthogonal() {
        int deg = getDegrees();
        setDegrees(deg+90);
        updateSize();
    }
    
    public boolean setFlipMenu(JPopupMenu popup) {
        if (isIcon() && getDisplayLevel() > Editor.BKG) {
            JMenu edit = new JMenu(Bundle.getMessage("mirrorMenu")+"...");
            JMenu flipMenu = new JMenu("mirrorMenu");
            ButtonGroup buttonGrp = new ButtonGroup();
            buttonGrp.add(flipMenuEntry(flipMenu, HORIZONTALFLIP));
            buttonGrp.add(flipMenuEntry(flipMenu, VERTICALFLIP));
            buttonGrp.add(flipMenuEntry(flipMenu, NOFLIP));
            edit.add(flipMenu);
            popup.add(edit);        
            popup.add(new AbstractAction(Bundle.getMessage("mirrorMenu")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    makeFlipMenu();
                }
            });
            return true;
        }
        return false;
        
    }
    
    private void makeFlipMenu() {
        JMenu edit = new JMenu(Bundle.getMessage("mirrorMenu")+"...");
        JMenu flipMenu = new JMenu(Bundle.getMessage("mirrorMenu"));
        ButtonGroup buttonGrp = new ButtonGroup();
        buttonGrp.add(flipMenuEntry(flipMenu, HORIZONTALFLIP));
        buttonGrp.add(flipMenuEntry(flipMenu, VERTICALFLIP));
        buttonGrp.add(flipMenuEntry(flipMenu, NOFLIP));
        edit.add(flipMenu);
    }
    JRadioButtonMenuItem flipMenuEntry(JMenu menu, int flip) {
        String menuItem;
        switch (flip) {
            case HORIZONTALFLIP:
                menuItem = "flipHorizontal";
                break;
            case VERTICALFLIP:
                menuItem = "flipVertical";
                break;
            default:
                menuItem = "flipNone";
        }
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(Bundle.getMessage(menuItem));
        r.addActionListener(new ActionListener() {
            final int f = flip;
            @Override
            public void actionPerformed(ActionEvent e) {
                setFlip(f);
            }
        });
        if (getFlip() == flip) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        return r;
    }

/*    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }*/

    /**
     * ********** Methods for Item Popups in Panel editor ************************
     */

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        if (_icon && !_text) {
            String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
            popup.add(new AbstractAction(txt) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
            return true;
        }
        return false;
    }

    protected void edit() {
        makeIconEditorFrame(this, "Icon", false, null);
        NamedIcon icon = new NamedIcon(_namedIcon);
        _iconEditor.setIcon(0, "plainIcon", icon);
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = (ActionEvent a) -> {
            editIcon();
        };
        _iconEditor.complete(addIconAction, true, false, true);

    }

    protected void editIcon() {
        String url = _iconEditor.getIcon("plainIcon").getURL();
        _namedIcon = NamedIcon.getIconByName(url);
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
        repaint();
    }

    public jmri.jmrit.display.DisplayFrame _paletteFrame;

    //
    // ********** Methods for Item Popups in Control Panel editor *******************
    //
    /**
     * Create a palette window.
     *
     * @param title the name of the palette
     * @return DisplayFrame for palette item
     */
    public DisplayFrame makePaletteFrame(String title) {
        jmri.jmrit.display.palette.ItemPalette.loadIcons(_editor);

        DisplayFrame paletteFrame = new DisplayFrame(title, false, false);
        paletteFrame.setLocationRelativeTo(this);
        paletteFrame.toFront();
        return paletteFrame;
    }

    public void initPaletteFrame(DisplayFrame paletteFrame, ItemPanel itemPanel) {
        Dimension dim = itemPanel.getPreferredSize();
        JScrollPane sp = new JScrollPane(itemPanel);
        dim = new Dimension(dim.width +25, dim.height + 25);
        sp.setPreferredSize(dim);
        paletteFrame.add(sp);
        paletteFrame.pack();
        paletteFrame.setVisible(true);
    }

    public void finishItemUpdate(DisplayFrame paletteFrame, ItemPanel itemPanel) {
        itemPanel.closeDialogs();
        itemPanel = null;
        paletteFrame.dispose();
        paletteFrame = null;
        invalidate();
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        if (!_icon) {
            return false;
        }
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
        popup.add(new AbstractAction(txt) {

            @Override
            public void actionPerformed(ActionEvent e) {
                editIconItem();
            }
        });
        return true;
    }

    IconItemPanel _iconItemPanel;

    protected void editIconItem() {
        _paletteFrame = makePaletteFrame(
                java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout")));
        _iconItemPanel = new IconItemPanel(_paletteFrame, "Icon", _editor); // NOI18N
        ActionListener updateAction = (ActionEvent a) -> {
                updateIconItem();
         };
        _iconItemPanel.init(updateAction);
        initPaletteFrame(_paletteFrame, _iconItemPanel);
    }

    private void updateIconItem() {
        NamedIcon icon = _iconItemPanel.getIcon();
        if (icon != null) {
            String url = icon.getURL();
            setIcon(NamedIcon.getIconByName(url));
            updateSize();
        }
        _paletteFrame.dispose();
        _paletteFrame = null;
        _iconItemPanel = null;
        invalidate();
    }
/* future use to replace editor.setTextAttributes
    public boolean setEditTextMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("TextAttributes"), Bundle.getMessage("Text"));
        popup.add(new AbstractAction(txt) {

            @Override
            public void actionPerformed(ActionEvent e) {
                editTextItem();
            }
        });
        return true;
    }

    TextItemPanel _textItemPanel;
    
    protected void editTextItem() {
        makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("TextAttributes"), Bundle.getMessage("BeanNameTurnout")));
        _textItemPanel = new TextItemPanel(_paletteFrame, "Text", _editor); // NOI18N
        ActionListener updateAction = (ActionEvent a) -> {
                updateTextItem();
         };
        _textItemPanel.init(updateAction, this);
        initPaletteFrame(_textItemPanel);
    }

    private void updateTextItem() {
        _textItemPanel.updateAttributes(this);
        updateSize();
        _paletteFrame.dispose();
        _paletteFrame = null;
        _iconItemPanel = null;
        invalidate();
    }*/

    /**
     * Rotate degrees return true if popup is set.
     */
    @Override
    public boolean setRotateMenu(JPopupMenu popup) {
        if (getDisplayLevel() > Editor.BKG) {
            popup.add(CoordinateEdit.getRotateEditAction(this));
            return true;
        }
        return false;
    }

    /**
     * Scale percentage form display.
     *
     * @return true if popup is set
     */
    @Override
    public boolean setScaleMenu(JPopupMenu popup) {
        if (isIcon() && getDisplayLevel() > Editor.BKG) {
            popup.add(CoordinateEdit.getScaleEditAction(this));
            return true;
        }
        return false;
    }

    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        if (isText()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "EditText"));
            return true;
        }
        return false;
    }

    @Override
    public void paintComponent(Graphics g) {
        int iconWidth = getIconWidth();
        int iconHeight = getIconHeight();
        int textWidth = getTextWidth();
        int textHeight = getTextHeight();
        int borderSize = getBorderSize();
        int marginSize = getMarginSize();
        int hOffSet =  borderSize + marginSize;
        int vOffSet =  borderSize + marginSize;
        if (_icon && _namedIcon!=null) {
            if (textWidth>iconWidth) {
                hOffSet += (textWidth - iconWidth)/2;               
            }
            if (textHeight>iconHeight) {
                vOffSet += (textHeight - iconHeight)/2;             
            }
            g.setClip(hOffSet, vOffSet, iconWidth, iconHeight);
            _namedIcon.paintIcon(this, g, hOffSet, vOffSet);
//            g2d.setColor(java.awt.Color.red);
//            g2d.drawRect(0, 0, getWidth(), getHeight());
//            g2d.setColor(Color.green);
//            java.awt.Rectangle r = g2d.getClipBounds();
//            g2d.drawRect(r.x, r.y, r.width, r.height);
        }

        if (_text && _textString!=null && _textString.length()>0) {
            java.awt.Font font = getFont();
            g.setFont(font);
            hOffSet = borderSize + marginSize;
            vOffSet = borderSize + marginSize;
            int fixedWidth = 0;
            int fixedHeight = 0;
            int justification = PositionablePopupUtil.CENTRE;
            if (_popupUtil!=null) {
                fixedWidth = _popupUtil.getFixedWidth();
                fixedHeight = _popupUtil.getFixedHeight();
                justification = _popupUtil.getJustification();
            }
            int height = getFontMetrics(font).getHeight();
            int width = getFontMetrics(font).stringWidth(_textString);
            int ascent = getFontMetrics(font).getAscent();
            if (fixedHeight > 0) {
                if (fixedHeight > height) {
                    vOffSet += Math.max(0, (fixedHeight - height)/2);
                } else {
                    ascent -= (height-fixedHeight)/2;
                    height = fixedHeight;                    
                }
            }
            if (fixedWidth > 0) {
               switch (justification) {
                    case PositionablePopupUtil.LEFT:
                        break;
                    case PositionablePopupUtil.RIGHT:
                        hOffSet += fixedWidth - width; // + borderSize/2;
                        break;
                    default:        // and PositionablePopupUtil.CENTRE:
                        hOffSet += (fixedWidth - width + borderSize/2)/2;
               }
               width = fixedWidth;
            }
            
            if (iconWidth>textWidth) {
                hOffSet += (iconWidth - textWidth)/2;
            }
            if (iconHeight>textHeight) {
                vOffSet += (iconHeight - textHeight)/2;
            }
            g.setClip(borderSize, vOffSet, getWidth()-2*borderSize, height);
            vOffSet += ascent;
            g.setColor(getForeground());
            g.drawString(_textString, hOffSet, vOffSet);             
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableLabel.class);
}
