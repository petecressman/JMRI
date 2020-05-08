package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Transit;
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.AbstractSignalMast;
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
        setIsIcon(true);
        super.setFamily("default");
        setDisplayState("BeanStateUnknown");
    }

    private NamedBeanHandle<SignalMast> namedMast;
    private NameCollection _aspectNames;

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

    private void makeStateNameCollection() {
        SignalMast m = getSignalMast();
        Vector<String> names = m.getValidAspects();
        int size = names.size();
        String[] validnames = new String[size + 2];
        validnames = names.toArray(validnames);
        validnames[size] = "$dark";
        validnames[size + 1] = "$held";
        _aspectNames = new NameCollection(validnames);
    }

    @Override
    protected Collection<String> getStateNameCollection() {
        return _aspectNames;
    }

    @Override
    protected void makeDisplayMap() {
        makeStateNameCollection();
        super.makeDisplayMap();
        java.util.Enumeration<String> e = getSignalMast().getValidAspects().elements();
        boolean error = false;
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            loadIcon(aspect);
        }
        if (error) {
        }
        //Add in specific appearances for dark and held
        loadIcon("$dark");
        loadIcon("$held");
    }

    private String useIconSet = "default";  // see setImageTypeList()
    
    private void loadIcon(String aspect) {
        String s = getSignalMast().getAppearanceMap().getImageLink(aspect, useIconSet);
        if (s.equals("")) {
            if (aspect.startsWith("$")) {
                log.debug("No icon found for specific appearance {}", aspect);
            } else {
                log.error("No icon found for appearance {}", aspect);
            }
            JOptionPane.showMessageDialog(_editor.getTargetFrame(),
                    java.text.MessageFormat.format(Bundle.getMessage("SignalMastIconLoadError"),
                            new Object[]{getSignalMast().getDisplayName()}),
                    Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            if (!s.contains("preference:")) {
                s = s.substring(s.indexOf("resources"));
            }
            NamedIcon n;
            try {
                n = new NamedIcon(s, s);
            } catch (java.lang.NullPointerException e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalMastIconLoadError2", 
                        new Object[]{aspect, s, getNameString()}), Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.error(Bundle.getMessage("SignalMastIconLoadError2", aspect, s, getNameString()));
                return;
            }
            setStateIcon(aspect, n);
        }
    }

/*    
    @Override
    public void setFamily(String fam) {
        if (fam == null) {
            fam = "default";
        }
        if (getFamily() == fam) {
            return;
        }
        super.setFamily(fam);
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
            makeDisplayMap();
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
            log.warn("did not find a SignalMast named {}", pName);
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
        } else {
            name = getSignalMast().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    /**
     * Pop-up just displays the name
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            editableShowPopUp(popup);
        } else {
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
        }
        return true;
    }

    private void editableShowPopUp(JPopupMenu popup) {
        JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
        ButtonGroup clickButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
        r.addActionListener(e -> setClickMode(0));
        clickButtonGroup.add(r);
        if (clickMode == 0) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        clickMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateLit"));
        r.addActionListener(e -> setClickMode(1));
        clickButtonGroup.add(r);
        if (clickMode == 1) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        clickMenu.add(r);
        r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateHeld"));
        r.addActionListener(e -> setClickMode(2));
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
        r.addActionListener(e -> {
            setLitMode(false);
            displayState(mastState());
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
        r.addActionListener(e -> {
            setLitMode(true);
            displayState(mastState());
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
    }

    private void addTransitPopup(JPopupMenu popup) {
        if ((InstanceManager.getDefault(jmri.SectionManager.class).getNamedBeanSet().size()) > 0
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
        im.addActionListener(e -> useIconSet = item);
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
        editableShowPopUp(popup);
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameSignalMast")));
        _itemPanel = new SignalMastItemPanel(_paletteFrame, "SignalMast", getFamily(),
                PickListModel.signalMastPickModelInstance());
        ActionListener updateAction = a -> updateItem();
        // _iconMap keys with local names
        HashMap<String, NamedIcon> map = new HashMap<>();
        Iterator<String> iter = getStateNames();
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
                log.error("Click in mode {}", clickMode);
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
     * @param state the state (= mastState()) to display
     */
    @Override
    public void displayState(String state) {
        SignalMast m = getSignalMast();
        if (m != null) {
            if (m.getHeld()) {
                displayState("$held");
                return;
            } else if (getLitMode() && !m.getLit()) {
                displayState("$dark");
                return;
            }
        }
        super.displayState(state);
        updateSize();
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
     * <p>
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
