package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalMast;
import jmri.Transit;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.SignalMastItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a {@link jmri.SignalMast}.
 * <p>
 * The icons displayed are loaded from the {@link jmri.SignalAppearanceMap} in
 * the {@link jmri.SignalMast}.
 *
 * @see jmri.SignalMastManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2009, 2014
 */
public class SignalMastIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    public SignalMastIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        super.setFamily("default");
        setIsIcon(true);
        setDisplayState("$dark");
    }

    private NamedBeanHandle<SignalMast> namedMast;

    public void setShowAutoText(boolean state) {
        setIsText(state);
        setIsIcon(!state);
    }

    @Override
    public Positionable deepClone() {
        SignalMastIcon pos = new SignalMastIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(SignalMastIcon pos) {
        NamedBeanHandle<SignalMast> mast = getNamedSignalMast();
        if (mast != null) {
            pos.setSignalMast(mast);
        }
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        return super.finishClone(pos);
    }

    @Override
    protected HashMap<String, PositionableLabel> makeDefaultMap() {
        HashMap<String, PositionableLabel> map = new HashMap<>();
        SignalMast m = getSignalMast();
        if (m == null) {    // new map made when Mast is installed
            return map;
        }
        java.util.Enumeration<String> e = m.getAppearanceMap().getAspects();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            loadIcon(map, aspect, aspect);
        }
        loadIcon(map, "$dark", Bundle.getMessage("Dark"));
        loadIcon(map, "$held", Bundle.getMessage("Held"));
        return map;
    }
    
    private void loadIcon(HashMap<String, PositionableLabel> map, String aspect, String text) {
        NamedIcon icon  = getAspectIcon(aspect);
        if (icon != null) {
            PositionableLabel pos = new PositionableLabel(getEditor());
            pos.setText(text);
            pos.setIcon(icon);
            map.put(aspect, pos);
        } else {
            log.error("No icon found for aspect " + aspect);
        }
    }

    private NamedIcon getAspectIcon(String aspect) {
        String s = getSignalMast().getAppearanceMap().getImageLink(aspect, getFamily());
        if (s.equals("")) {
            if (aspect.startsWith("$")) {
                log.warn("No icon found for specific appearance " + aspect);
            } else {
                log.error("No icon found for appearance " + aspect);
            }
            return null;
        }
        if (!s.contains("preference:")) {
            s = s.substring(s.indexOf("resources"));
        }
        NamedIcon icon = null;
        try {
            icon = new NamedIcon(s, s);
        } catch (java.lang.NullPointerException e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalMastIconLoadError2", new Object[]{aspect, s, getNameString()}),
                    Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error(Bundle.getMessage("SignalMastIconLoadError2", aspect, s, getNameString()));
        }
        log.debug("getAspectIcon for aspect= {}, icon= {}", aspect, (icon!=null ? icon.getURL() : "null"));
        return icon;
    }
    
    @Override
    public void setFamily(String fam) {
        if (fam == null) {
            fam = "default";
        }
        if (getFamily() == fam) {
            return;
        }
        super.setFamily(fam);
        setIconMap(makeDefaultMap());
        displayState(mastState());
        _editor.getTargetPanel().repaint();
    }

    /**
     * Attached a signalmast element to this display item
     *
     * @param sh Specific SignalMast handle
     */
    public void setSignalMast(NamedBeanHandle<SignalMast> sh) {
        if (namedMast != null) {
            getSignalMast().removePropertyChangeListener(this);
        }
        namedMast = sh;
        if (namedMast != null) {
            setIconMap(makeDefaultMap());
/*            if (!isIconMapOK()) {
                JOptionPane.showMessageDialog(_editor.getTargetFrame(),
                        java.text.MessageFormat.format(Bundle.getMessage("SignalMastIconLoadError"),
                                new Object[]{getSignalMast().getDisplayName()}),
                        Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }*/
            displayState(mastState());
            getSignalMast().addPropertyChangeListener(this, namedMast.getName(), "SignalMast Icon");
        }
    }

    /**
     * Taken from the layout editor Attached a numbered element to this display
     * item
     *
     * @param pName Used as a system/user name to lookup the SignalMast object
     */
    public void setSignalMast(String pName) {
        SignalMast mMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(pName);
        if (mMast == null) {
            log.warn("did not find a SignalMast named " + pName);
        } else {
            setSignalMast(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, mMast));
        }
    }
  
    public NamedBeanHandle<SignalMast> getNamedSignalMast() {
        return namedMast;
    }

    public SignalMast getSignalMast() {
        if (namedMast == null) {
            return null;
        }
        return namedMast.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getSignalMast();
    }

    /**
     * Get current appearance of the mast
     *
     * @return An aspect from the SignalMast
     */
    public String mastState() {
        if (getSignalMast() != null) {
            return getSignalMast().getAspect();
        }
        return null;
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("property change: {} current state: {}", e.getPropertyName(), mastState());
        displayState(mastState());
        _editor.getTargetPanel().repaint();
    }

//    public String getPName() { return namedMast.getName(); }
    @Override
    public String getNameString() {
        String name;
        if (getSignalMast() == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (getSignalMast().getUserName() == null) {
            name = getSignalMast().getSystemName();
        } else {
            name = getSignalMast().getUserName() + " (" + getSignalMast().getSystemName() + ")";
        }
        return name;
    }

    /**
     * Pop-up just displays the name
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        final java.util.Vector<String> aspects = getSignalMast().getValidAspects();
        for (int i = 0; i < aspects.size(); i++) {
            final int index = i;
            popup.add(new AbstractAction(aspects.elementAt(index)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getSignalMast().setAspect(aspects.elementAt(index));
                }
            });
        }
        return true;
    }

    private void addTransitPopup(JPopupMenu popup) {
        if ((InstanceManager.getDefault(jmri.SectionManager.class).getSystemNameList().size()) > 0
                && jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).isAdvancedRoutingEnabled()) {

            if (tct == null) {
                tct = new jmri.jmrit.display.layoutEditor.TransitCreationTool();
            }
            popup.addSeparator();
            String addString = Bundle.getMessage("MenuTransitCreate");
            if (tct.isToolInUse()) {
                addString = Bundle.getMessage("MenuTransitAddTo");
            }
            popup.add(new AbstractAction(addString) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        tct.addNamedBean(getSignalMast());
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("TransitErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            if (tct.isToolInUse()) {
                popup.add(new AbstractAction(Bundle.getMessage("MenuTransitAddComplete")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Transit created;
                        try {
                            tct.addNamedBean(getSignalMast());
                            created = tct.createTransit();
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("TransitCreatedMessage", created.getDisplayName()), Bundle.getMessage("TransitCreatedTitle"), JOptionPane.INFORMATION_MESSAGE);
                        } catch (jmri.JmriException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("TransitErrorTitle"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("MenuTransitCancel")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tct.cancelTransitCreate();
                    }
                });
            }
            popup.addSeparator();
        }
    }

    static jmri.jmrit.display.layoutEditor.TransitCreationTool tct;

    private void setImageTypeList(ButtonGroup iconTypeGroup, JMenu iconSetMenu, final String item) {
        JRadioButtonMenuItem im;
        im = new JRadioButtonMenuItem(item);
        im.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFamily(item);
            }
        });
        iconTypeGroup.add(im);
        if (getFamily().equals(item)) {
            im.setSelected(true);
        } else {
            im.setSelected(false);
        }
        iconSetMenu.add(im);

    }

    SignalMastItemPanel _itemPanel;
    ButtonGroup litButtonGroup = null;

    @Override
    public boolean setIconEditMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSignalMast"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });

        JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
        ButtonGroup clickButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setClickMode(0);
            }
        });
        clickButtonGroup.add(r);
        if (clickMode == 0) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        clickMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateLit"));
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setClickMode(1);
            }
        });
        clickButtonGroup.add(r);
        if (clickMode == 1) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        clickMenu.add(r);
        r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateHeld"));
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setClickMode(2);
            }
        });
        clickButtonGroup.add(r);
        if (clickMode == 2) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        clickMenu.add(r);
        popup.add(clickMenu);

        // add menu to select handling of lit parameter
        JMenu litMenu = new JMenu(Bundle.getMessage("WhenNotLit"));
        litButtonGroup = new ButtonGroup();
        r = new JRadioButtonMenuItem(Bundle.getMessage("ShowAppearance"));
        r.setIconTextGap(10);
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLitMode(false);
                displayState(mastState());
            }
        });
        litButtonGroup.add(r);
        if (!litMode) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        litMenu.add(r);
        r = new JRadioButtonMenuItem(Bundle.getMessage("ShowDarkIcon"));
        r.setIconTextGap(10);
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLitMode(true);
                displayState(mastState());
            }
        });
        litButtonGroup.add(r);
        if (litMode) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        litMenu.add(r);
        popup.add(litMenu);

        java.util.Enumeration<String> en = getSignalMast().getSignalSystem().getImageTypeList();
        if (en.hasMoreElements()) {
            JMenu iconSetMenu = new JMenu(Bundle.getMessage("SignalMastIconSet"));
            ButtonGroup iconTypeGroup = new ButtonGroup();
            setImageTypeList(iconTypeGroup, iconSetMenu, "default");
            while (en.hasMoreElements()) {
                setImageTypeList(iconTypeGroup, iconSetMenu, en.nextElement());
            }
            popup.add(iconSetMenu);
        }
        popup.add(new jmri.jmrit.signalling.SignallingSourceAction(Bundle.getMessage("SignalMastLogic"), getSignalMast()));
        JMenu aspect = new JMenu(Bundle.getMessage("ChangeAspect"));
        final java.util.Vector<String> aspects = getSignalMast().getValidAspects();
        for (int i = 0; i < aspects.size(); i++) {
            final int index = i;
            aspect.add(new AbstractAction(aspects.elementAt(index)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getSignalMast().setAspect(aspects.elementAt(index));
                }
            });
        }
        popup.add(aspect);
        addTransitPopup(popup);
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameSignalMast")));
        _itemPanel = new SignalMastItemPanel(_paletteFrame, "SignalMast", getFamily(),
                PickListModel.signalMastPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // _iconMap keys with local names
        HashMap<String, NamedIcon> map = new HashMap<>();
        Iterator<String> iter = getIconStateNames();
        while (iter.hasNext()) {
            String  state = iter.next();
            NamedIcon oldIcon = getIcon(state);
            NamedIcon newIcon = new NamedIcon(oldIcon);
            map.put(state, newIcon);
        }
        _itemPanel.init(updateAction, map);
        _itemPanel.setSelection(getSignalMast());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        setSignalMast(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

    /**
     * Change the SignalMast aspect when the icon is clicked.
     *
     */
    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        performMouseClicked(e);
    }

    /**
     * Handle mouse clicks when no modifier keys are pressed. Mouse clicks with
     * modifier keys pressed can be processed by the containing component.
     *
     * @param e the mouse click event
     */
    public void performMouseClicked(java.awt.event.MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown()) {
            return;
        }
        if (getSignalMast() == null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0:
                java.util.Vector<String> aspects = getSignalMast().getValidAspects();
                int idx = aspects.indexOf(getSignalMast().getAspect()) + 1;
                if (idx >= aspects.size()) {
                    idx = 0;
                }
                getSignalMast().setAspect(aspects.elementAt(idx));
                return;
            case 1:
                getSignalMast().setLit(!getSignalMast().getLit());
                return;
            case 2:
                getSignalMast().setHeld(!getSignalMast().getHeld());
                return;
            default:
                log.error("Click in mode " + clickMode);
        }
    }

    @Override
    public void displayState() {
        displayState(mastState());
    }

    /**
     * Drive the current state of the display from the state of the underlying
     * SignalMast object.
     *
     * @param s the state (= mastState()) to display
     */
    private void displayState(String s) {
        String state = s;
        SignalMast mast = getSignalMast();
        if (mast == null) {
            setDisconnectedText("BeanDisconnected");
            return;
        } else if (state == null) {
            setDisconnectedText("BeanStateUnknown");
        } else {
            restoreConnectionDisplay();
        }
        log.debug("Display state= {}, isText()= {} isIcon()= {}", state, isText(), isIcon());
        if (mast.getHeld()) {
            state = "$held";
        }
        if (getLitMode() && !mast.getLit()) {
            state = "$dark";
        }
        setDisplayState(state);
        return;
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    /**
     * What to do on click? 0 means sequence through aspects; 1 means alternate
     * the "lit" aspect; 2 means alternate the
     * {@link jmri.SignalAppearanceMap#HELD} aspect.
     */
    protected int clickMode = 0;

    public void setClickMode(int mode) {
        clickMode = mode;
    }

    public int getClickMode() {
        return clickMode;
    }

    /**
     * How to handle lit vs not lit?
     * <P>
     * False means ignore (always show R/Y/G/etc appearance on screen); True
     * means show {@link jmri.SignalAppearanceMap#DARK} if lit is set false.
     */
    protected boolean litMode = false;

    public void setLitMode(boolean mode) {
        litMode = mode;
    }

    public boolean getLitMode() {
        return litMode;
    }

    @Override
    public void dispose() {
        getSignalMast().removePropertyChangeListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastIcon.class);
}
