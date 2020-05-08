package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.IconItemPanel;
import jmri.jmrit.display.palette.ItemPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

/**
 * PositionableLabel is a container for text and icons
 * <P>
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Pete Cressman Copyright 2017
 */
public class PositionableLabel extends Positionable {

    private boolean debug = false;
    private String _textString;

    
    protected NamedIcon _namedIcon;
    private boolean _icon = false;
    private boolean _text = false;


    public PositionableLabel(Editor editor) {
        super(editor);
    }

    /**
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
//        updateSize();
    }

    public PositionableLabel(NamedIcon s, Editor editor) {
        super(editor);
        _icon = true;
        _namedIcon = s;
        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PositionableLabel ctor (icon) {}", s != null ? s.getName() : null);
       }
//        updateSize();
     }

    /**
     * Answers whether icon should be displayed when
     * paintComponent(Graphics g)
     * @return display icon
     */
    public boolean isIcon() {
        return _icon;
    }

    /**
     * Answers whether text should be displayed when
     * paintComponent(Graphics g) is called
     * @return display text
     */
    public boolean isText() {
        return _text;
    }
    
    /**
     * Set whether icon should be displayed
     * @param b if true, display icon
     */
    public void setIsIcon(boolean b) {
        log.debug("setIsIcon = {}", b);
        if (_namedIcon == null) {
            _namedIcon = new NamedIcon(PositionableIcon._redX, PositionableIcon._redX);
        }
        _icon = b;
    }

    /**
     * Set whether text should be displayed
     * @param b if true, display text
     */
    public void setIsText(boolean b) {
        log.debug("setIsText = {}", b);
        _text = b;
        if (_text == true) {
            setBordered(true);
        }
    }
    
    public void setIcon(NamedIcon icon) {
        if (icon != null) {
            _namedIcon = icon;
        }
        updateSize();
    }
    
    public NamedIcon getIcon() {
        return _namedIcon;
    }

    public void setText(String text) {
        _textString = text;
        _text = (text != null && !text.isEmpty());
        updateSize();
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
    @Nonnull
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

    protected @Nonnull Positionable finishClone(@Nonnull PositionableLabel pos) {
        pos._text = _text;
        pos._icon = _icon;
        pos._textString = _textString;
        if (_namedIcon != null) {
            pos._namedIcon = new NamedIcon(_namedIcon);
        }
        return super.finishClone(pos);
    }

    /*
     * ************** end Positionable methods *********************
     */
    @Override
    public int getWidth() {
        int width = Math.max(getIconWidth(), getTextWidth());
        if (isBordered()) {
            width += (getBorderSize() + getMarginSize()) * 2;            
        }
        if (log.isTraceEnabled()) {
            log.trace("width= " + width + " preferred width= " + getPreferredSize().width);
        }
        return Math.max(width, MIN_SIZE); // don't let item disappear
    }

    @Override
    public int getHeight() {
        int height = Math.max(getIconHeight(), getTextHeight());
        if (isBordered()) {
            height += (getBorderSize() + getMarginSize()) * 2;
        }
        if (log.isTraceEnabled()) {
            log.trace("height= " + height + " preferred height= " + getPreferredSize().height);
        }
        return Math.max(height, MIN_SIZE);    // don't let item disappear
    }
    
    public int getIconWidth() {
        if (!_icon) {
            return 0;
        }
        if (_namedIcon != null) {
            return _namedIcon.getIconWidth();
        } else {
            log.error("No icon!");
        }
        return 0;
    }
    
    public int getIconHeight() {
        if (!_icon) {
            return 0;
        }
        if (_namedIcon != null) {
            return _namedIcon.getIconHeight();
        } else {
            log.error("No icon!");
        }
        return 0;
    }
    
    private int getTextWidth() {
        if (!_text) {
            return 0;
        }
        int width = getFixedWidth();
        if (width == 0) {
            if (_textString != null && getFont()!=null) {
                width = getFontMetrics(getFont()).stringWidth(_textString);
            } else {
                log.error("No text or font");
            }
        }
        return width;
    }
    
    private int getTextHeight() {
        if (!_text) {
            return 0;
        }
        int height = getFixedHeight();
        if (height == 0) {
            if (_textString != null && getFont()!=null) {
                height = Math.max(height, getFontMetrics(getFont()).getHeight());
            } else {
                log.error("No text or font");
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

    ///////////////////////////// popup methods ////////////////////////////

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

        ActionListener addIconAction = (ActionEvent a) -> editIcon();
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
        jmri.jmrit.display.palette.ItemPalette.loadIcons();

        return new DisplayFrame(title, _editor);
    }

    public void initPaletteFrame(DisplayFrame paletteFrame, @Nonnull ItemPanel itemPanel) {
        Dimension dim = itemPanel.getPreferredSize();
        JScrollPane sp = new JScrollPane(itemPanel);
        dim = new Dimension(dim.width + 25, dim.height + 25);
        sp.setPreferredSize(dim);
        paletteFrame.add(sp);
        paletteFrame.pack();
        jmri.InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(_editor, this, paletteFrame);
        paletteFrame.setVisible(true);
    }

    public void finishItemUpdate(DisplayFrame paletteFrame, @Nonnull ItemPanel itemPanel) {
        itemPanel.closeDialogs();
        paletteFrame.dispose();
        invalidate();
    }

    public boolean setIconEditMenu(JPopupMenu popup) {
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
        _iconItemPanel = new IconItemPanel(_paletteFrame, "Icon"); // NOI18N
        ActionListener updateAction = (ActionEvent a) -> updateIconItem();
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
/* future use to replace editor.setTextAttributes  maybe???
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

    public boolean setDisplayModeMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("DisplayMode"));
        final JCheckBoxMenuItem cbi = new JCheckBoxMenuItem(Bundle.getMessage("Icons"));
        cbi.addActionListener((ActionEvent e) -> {
            setIsIcon(cbi.isSelected());
            setIsText(!cbi.isSelected());
            setBordered(false);
            displayState();
        });
        cbi.setSelected(_icon && !_text);
        edit.add(cbi);
        final JCheckBoxMenuItem cbib = new JCheckBoxMenuItem(Bundle.getMessage("BorderedIcons"));
        cbi.addActionListener((ActionEvent e) -> {
            setIsIcon(cbib.isSelected());
            setIsText(!cbib.isSelected());
            setBordered(true);
            displayState();
        });
        cbib.setSelected(_icon && !_text);
        edit.add(cbib);
        final JCheckBoxMenuItem cbt = new JCheckBoxMenuItem(Bundle.getMessage("Texts"));
        cbt.addActionListener((ActionEvent e) -> {
            setIsIcon(!cbt.isSelected());
            setIsText(cbt.isSelected());
            displayState();
        });
        cbt.setSelected(_text  && !_icon);
        edit.add(cbt);
        final JCheckBoxMenuItem cb = new JCheckBoxMenuItem(Bundle.getMessage("OverlayText"));
        cb.addActionListener((ActionEvent e) -> {
            setIsIcon(cb.isSelected());
            setIsText(cb.isSelected());
            displayState();
        });
        cb.setSelected(_icon && _text);
        edit.add(cb);
        popup.add(edit);
        return true;
    }
    
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
        }

        if (_text && _textString!=null && _textString.length()>0) {
            java.awt.Font font = getFont();
            g.setFont(font);
            hOffSet = borderSize + marginSize;
            vOffSet = borderSize + marginSize;
            int height = getFontMetrics(font).getHeight();
            int width = getFontMetrics(font).stringWidth(_textString);
            int ascent = getFontMetrics(font).getAscent();
            int fixedHeight = getFixedHeight();
            int fixedWidth = getFixedWidth();
            if (fixedHeight > 0) {
                if (fixedHeight > height) {
                    vOffSet += Math.max(0, (fixedHeight - height)/2);
                } else {
                    ascent -= (height-fixedHeight)/2;
                    height = fixedHeight;
                }
            }
            if (fixedWidth > 0) {
               switch (getJustification()) {
                    case LEFT:
                        break;
                    case RIGHT:
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
/*        long time = 0;
        if (System.currentTimeMillis() - time > 1000) {
            System.out.println("Paint " + getClass().getName());
            time = System.currentTimeMillis();
        }*/
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableLabel.class);

}
