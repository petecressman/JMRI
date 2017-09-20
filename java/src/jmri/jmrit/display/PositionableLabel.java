package jmri.jmrit.display;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.SystemType;
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
    protected boolean _control = false;

    public PositionableLabel(String s, Editor editor) {
        super(editor);
        _text = true;
        _textString = s;
        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PositionableLabel ctor (text) " + s);
        }
        setPopupUtility(new PositionablePopupUtil(this, this));
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

    public Positionable finishClone(Positionable p) {
        PositionableLabel pos = (PositionableLabel) p;
        pos._text = _text;
        pos._icon = _icon;
        pos._control = _control;
        pos._textString = _textString;
        if (getPopupUtility() == null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone());
        }
        pos.setOpaque(isOpaque());
        if (_namedIcon != null) {
            pos._namedIcon = new NamedIcon(_namedIcon);
            pos.setIcon(pos._namedIcon);
        }
//        pos.updateSize();
        return super.finishClone(pos);
    }

    public JComponent getTextComponent() {
        return this;
    }
    /**
     * ************** end Positionable methods *********************
     */
    @Override
    public int getWidth() {
        int width = Math.max(getIconWidth(), getTextWidth());
        if (_popupUtil!=null) {
            width += (_popupUtil.getBorderSize() + _popupUtil.getMarginSize()) * 2;            
        }
        return Math.max(width, PositionablePopupUtil.MIN_SIZE); // don't let item disappear
    }

    @Override
    public int getHeight() {
        int height = Math.max(getIconHeight(), getTextHeight());
        if (_popupUtil!=null) {
            height += (_popupUtil.getBorderSize() + _popupUtil.getMarginSize()) * 2;
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

    /**
     * ***** Methods to add menu items to popup *******
     */
    /**
     * Call to a Positionable that has unique requirements - e.g.
     * RpsPositionIcon, SecurityElementIcon
     */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
     * PanelEditor Rotate othogonally return true if popup is set
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {

        if (isIcon() && getDisplayLevel() > Editor.BKG) {
            popup.add(new AbstractAction(Bundle.getMessage("Rotate")) {
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
            public void actionPerformed(ActionEvent e) {
                flipIcon(f);
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

    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }

    /**
     * ********** Methods for Item Popups in Panel editor
     * ************************
     */

    public boolean setEditIconMenu(JPopupMenu popup) {
        if (_icon && !_text) {
            String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
            popup.add(new AbstractAction(txt) {

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

    public jmri.util.JmriJFrame _paletteFrame;

    //
    // ********** Methods for Item Popups in Control Panel editor *******************
    //
    /**
     * Create a palette window.
     *
     * @param title the name of the palette
     */
    protected void makePaletteFrame(String title) {
        jmri.jmrit.display.palette.ItemPalette.loadIcons(_editor);

        _paletteFrame = new jmri.util.JmriJFrame(title, false, false);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                ImageIndexEditor.getDefault().checkImageIndex();   // write maps to tree
            }
        });
    }

    /**
     * Rotate degrees return true if popup is set
     */
    @Override
    public boolean setRotateMenu(JPopupMenu popup) {
        if (getDisplayLevel() > Editor.BKG) {
            return _editor.setShowRotationMenu(this, popup);
        }
        return false;
    }

    /**
     * Scale percentage return true if popup is set
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

    JCheckBoxMenuItem disableItem = null;

    @Override
    public boolean setDisableControlMenu(JPopupMenu popup) {
        if (_control) {
            disableItem = new JCheckBoxMenuItem(Bundle.getMessage("Disable"));
            disableItem.setSelected(!isControlling());
            popup.add(disableItem);
            disableItem.addActionListener((java.awt.event.ActionEvent e) -> {
                setControlling(!disableItem.isSelected());
            });
            return true;
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
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
        super.paintComponent(g2d);

        int iconWidth = getIconWidth();
        int iconHeight = getIconHeight();
        int textWidth = getTextWidth();
        int textHeight = getTextHeight();
        int borderSize = 0;
        int marginSize = 0;
        if (_popupUtil!=null) {
            borderSize = _popupUtil.getBorderSize();
            marginSize = _popupUtil.getMarginSize();            
        }
        int hOffSet =  borderSize + marginSize;
        int vOffSet =  borderSize + marginSize;
        if (_icon && _namedIcon!=null) {
            if (textWidth>iconWidth) {
                hOffSet += (textWidth - iconWidth)/2;               
            }
            if (textHeight>iconHeight) {
                vOffSet += (textHeight - iconHeight)/2;             
            }
            _namedIcon.paintIcon(this, g2d, hOffSet, vOffSet);            
        }

        if (_text && _textString!=null && _textString.length()>0) {
            java.awt.Font font = getFont();
            g2d.setFont(font);
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
//            g2d.setStroke(new java.awt.BasicStroke(1));
//            g2d.setColor(java.awt.Color.white);
//            g2d.drawRect(borderSize, vOffSet, getWidth()-2*borderSize, height);
            g2d.setClip(borderSize, vOffSet, getWidth()-2*borderSize, height);
            vOffSet += ascent;
            g2d.setColor(getForeground());
            g2d.drawString(_textString, hOffSet, vOffSet);             
        }
        g2d.dispose();
    }

    /**
     * Provides a generic method to return the bean associated with the
     * Positionable
     */
    public jmri.NamedBean getNamedBean() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableLabel.class);
}
