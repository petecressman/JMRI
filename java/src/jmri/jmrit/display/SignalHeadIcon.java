package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.display.palette.SignalHeadItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a SignalHead.
 * <P>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager.
 *
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Pete Cressman Copyright (C) 2018
 */
public class SignalHeadIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    private NamedBeanHandle<SignalHead> namedHead;
    /*
     * All implementations of SignalHead use localized names for aspects.
     * Instead of the English aspects names previously used in panel files,,
     * this class and its xml utility use name keys as listed in jmri.NamedBeanBundle.properties
     */
    private final static HashMap<String, String> _nameMap = new HashMap<>(10);
    static {
        _nameMap.put(Bundle.getMessage("SignalHeadStateRed"), "SignalHeadStateRed");
        _nameMap.put(Bundle.getMessage("SignalHeadStateYellow"), "SignalHeadStateYellow");
        _nameMap.put(Bundle.getMessage("SignalHeadStateGreen"), "SignalHeadStateGreen");
        _nameMap.put(Bundle.getMessage("SignalHeadStateLunar"), "SignalHeadStateLunar");
        _nameMap.put(Bundle.getMessage("SignalHeadStateHeld"), "SignalHeadStateHeld");
        _nameMap.put(Bundle.getMessage("SignalHeadStateDark"), "SignalHeadStateDark");
        _nameMap.put(Bundle.getMessage("SignalHeadStateFlashingRed"), "SignalHeadStateFlashingRed");
        _nameMap.put(Bundle.getMessage("SignalHeadStateFlashingYellow"), "SignalHeadStateFlashingYellow");
        _nameMap.put(Bundle.getMessage("SignalHeadStateFlashingGreen"), "SignalHeadStateFlashingGreen");
        _nameMap.put(Bundle.getMessage("SignalHeadStateFlashingLunar"), "SignalHeadStateFlashingLunar");
    }

    HashMap<String, HashMap<String, NamedIcon>> _signalIconFamilies; 

    private String[] _validKey; // localized names from SignalHead implementations

    public SignalHeadIcon(Editor editor) {
        super(editor);
        setIsIcon(true);
        // map localized names from SignalHead implementation to names in jmri.NamedBeanBundle.properties
        
        _signalIconFamilies = ItemPalette.getFamilyMaps("SignalHead");
        setDisplayState(_nameMap.get(Bundle.getMessage("SignalHeadStateDark")));
    }

    @Override
    public Positionable deepClone() {
        SignalHeadIcon pos = new SignalHeadIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(SignalHeadIcon pos) {
        NamedBeanHandle<SignalHead> head = getNamedSignalHead();
        if (head != null) {
            pos.setSignalHead(head.getName());
            pos._validKey = Arrays.copyOf(_validKey, _validKey.length);
       }
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        return super.finishClone(pos);
    }

    @Override
    protected HashMap<String, DisplayState> makeDefaultMap() {
        getValidKeys();
        HashMap<String, DisplayState> oldMap = getDisplayStateMap();
        HashMap<String, DisplayState> map = new HashMap<>();
        log.debug("makeDefaultMap {} valid keys", (_validKey!=null?_validKey.length: "no"));
        if (_validKey == null) {
            if (oldMap != null) {
                return oldMap;
            }
            // initialize state classes
            for (Entry<String,String> entry : _nameMap.entrySet()) {
                DisplayState pos = new DisplayState();
                pos.setText(entry.getKey());
                map.put(entry.getValue(), pos);
            }
            return map;
        }
        HashMap<String, NamedIcon> icons = _signalIconFamilies.get(getFamily());
        if (icons == null) {
            log.warn("No Icon map for family \"{}\".", getFamily());
        } else {
            log.debug("Family \"{}\" has full map of {} icons.", getFamily(), icons.size());
        }
        DisplayState pos;
        for (String key : _validKey) {
            String state = _nameMap.get(key);
            NamedIcon icon = null;
            if (oldMap != null && oldMap.get(state) != null) {
                pos = oldMap.get(state);
                icon = pos.getIcon();
            } else {
                pos = new DisplayState();
            }
            if (icons != null) {
                icon = icons.get(key);
            }
            if (icon != null) {
                pos.setIcon(icon);
                log.debug("Family \"{}\" key {} for state {} found icon {} ",
                        (getFamily()!=null ? getFamily() : "no family"), key, state, icon.getURL());
                pos.setText(state);
            } else {
                log.warn("Family \"{}\" key {} for state {} did not find icon.",
                        getFamily(), key, state);
            }
            map.put(state, pos);
        }
        if (!map.containsKey("SignalHeadStateHeld")) {
            if (oldMap != null && oldMap.get("SignalHeadStateHeld") != null) {
                pos = oldMap.get("SignalHeadStateHeld");
            } else {
                pos = new DisplayState();                
                pos.setText("SignalHeadStateHeld");
                setIcon(new NamedIcon(_redX, _redX));
            }
            map.put("SignalHeadStateHeld", pos);
        }
        if (!map.containsKey("SignalHeadStateDark")) {
            if (oldMap != null && oldMap.get("SignalHeadStateDark") != null) {
                pos = oldMap.get("SignalHeadStateDark");
            } else {
                pos = new DisplayState();                
                pos.setText("SignalHeadStateDark");
                setIcon(new NamedIcon(_redX, _redX));
            }
            map.put("SignalHeadStateDark", pos);
        }
/*        if (!isIconMapOK()) {
            JOptionPane.showMessageDialog(_editor.getTargetFrame(),
                    java.text.MessageFormat.format(Bundle.getMessage("SignalHeadLoadError"),
                            new Object[]{getSignalHead().getDisplayName()}),
                    Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }*/
        return map;
    }

    /**
     * Attached a signalhead element to this display item
     *
     * @param sh Specific SignalHead object
     */
    public void setSignalHead(NamedBeanHandle<SignalHead> sh) {
        if (namedHead != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = sh;
        if (namedHead != null) {
            setIconMap(makeDefaultMap());
            getSignalHead().addPropertyChangeListener(this, namedHead.getName(), "SignalHead Icon");
            displayState(headState());
        }
    }

    /**
     * Taken from the layout editor Attached a numbered element to this display
     * item
     *
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        SignalHead mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getNamedBean(pName);
        if (mHead == null) {
            log.warn("did not find a SignalHead named " + pName);
        } else {
            setSignalHead(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, mHead));
        }
    }

    public NamedBeanHandle<SignalHead> getNamedSignalHead() {
        return namedHead;
    }

    public SignalHead getSignalHead() {
        if (namedHead == null) {
            return null;
        }
        return namedHead.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getSignalHead();
    }

    private void getValidKeys() {
        SignalHead h = getSignalHead();
        if (h == null) {
            log.info("Valid keys unknown. no signal Head");
            return;
        }
        _validKey = h.getValidStateNames();
        if (log.isDebugEnabled()) {
            for (String key : _validKey) {
                log.debug("ValidKey= {}", key);                
            }
        }
    }
    /**
     * Check that device supports the state valid state names returned by the
     * bean are localized
     */
    private boolean isValidState(String key) {
        getValidKeys();
        if (_validKey == null) {
            log.info("Valid keys unknown. _validKey is null");
            return false;
        }
        if (key.equals(Bundle.getMessage("SignalHeadStateDark"))
                || key.equals(Bundle.getMessage("SignalHeadStateHeld"))) {
            if (log.isDebugEnabled()) {
                log.debug(key + " is a valid state. ");
            }
            return true;
        }
       for (String state : _validKey) {
           if (state.equals(key)) {
               if (log.isDebugEnabled()) {
                   log.debug(key + " is a valid state. ");
               }
               return true;
           }
        }
        log.info("aspect {} for head {} is NOT a valid aspect.", key, getNameString());
        
        return false;
    }

    /**
     * Place icon by its SignalHead aspect name key found in 
     * the properties file jmri.NamedBeanBundle.properties
     *
     * @param name SignalHead aspect name
     * @param icon the icon to display the ascpect
     */
    @Override
    public void setStateIcon(String state, NamedIcon icon) {
        super.setStateIcon(state, icon);
        if (log.isDebugEnabled()) {
            log.debug("setStateIcon for {}, validation= {}",
                    state, isValidState(Bundle.getMessage(state)));
        }
    }

    /**
     * Get current appearance of the head.
     *
     * @return An appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    public String headState() {
        if (getSignalHead() == null) {
            return Bundle.getMessage("SignalHeadStateDark");
        } else {
            return getSignalHead().getAppearanceName();
        }
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: " + e.getPropertyName()
                    + " current state: " + headState());
        }
        displayState(headState());
        _editor.getTargetPanel().repaint();
    }

    @Override
    public String getNameString() {
        if (namedHead == null) {
            return Bundle.getMessage("NotConnected");
        }
        return namedHead.getName();
    }

    @Override
    public void displayState() {
        displayState(headState());
    }

    /**
     * Drive the current state of the display from the state of the underlying
     * SignalHead object.
     * <UL>
     * <LI>If the signal is held, display that.
     * <LI>If set to monitor the status of the lit parameter and lit is false,
     * show the dark icon ("dark", when set as an explicit appearance, is
     * displayed anyway)
     * <LI>Show the icon corresponding to one of the seven appearances.
     * </UL>
     */
    private void displayState(String state) {
        String beanState = _nameMap.get(state);
        if (getSignalHead() == null) {
            setDisconnectedText("BeanDisconnected");
        } else if (state == null) {
            setDisconnectedText("BeanStateUnknown");
        } else {
            restoreConnectionDisplay();
       }
        if (log.isDebugEnabled()) {
            DisplayState pos = getStateData(beanState);
            log.debug("displayState: state= \"{}\" beanState = \"{}\" icon= {}",
                    state, beanState, (pos.getIcon()!=null?pos.getIcon().getURL():"null"));
        }
        getDisplayState(beanState).setDisplayParameters(this);
        setDisplayState(beanState);
        updateSize();
    }

    //////////////////////////////// Popup Menu methods //////////////////////////////////////

    SignalHeadItemPanel _itemPanel;
    ButtonGroup litButtonGroup = null;

    @Override
    public boolean setIconEditMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSignalHead"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });

        // add menu to select action on click
        JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
        ButtonGroup clickButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setClickMode(3);
            }
        });
        clickButtonGroup.add(r);
        if (clickMode == 3) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        clickMenu.add(r);
        r = new JRadioButtonMenuItem(Bundle.getMessage("Cycle3Aspects"));
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

        popup.add(new AbstractAction(Bundle.getMessage("EditLogic")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                String name = getNameString();
                f.setTitle(java.text.MessageFormat.format(Bundle.getMessage("SignalLogic"), name));
                f.setSignal(getSignalHead());
                f.setVisible(true);
            }
        });
        
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameSignalHead")));
        _itemPanel = new SignalHeadItemPanel(_paletteFrame, "SignalHead", getFamily(),
                PickListModel.signalHeadPickModelInstance(), _editor); //NOI18N
        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // _iconMap keys with local names - Let SignalHeadItemPanel figure this out
        HashMap<String, NamedIcon> map = new HashMap<>();
        Iterator<String> iter = getStateNames();
        while (iter.hasNext()) {
            String  state = iter.next();
            NamedIcon oldIcon = getIcon(state);
            NamedIcon newIcon = new NamedIcon(oldIcon);
            map.put(state, newIcon);
        }
        _itemPanel.init(updateAction, map);
        _itemPanel.setSelection(getSignalHead());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    protected void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        setSignalHead(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        HashMap<String, NamedIcon> iconMap = _itemPanel.getIconMap();
        if (iconMap != null) {
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) {
                    log.debug("key= " + entry.getKey());
                }
                NamedIcon newIcon = new NamedIcon(entry.getValue());
                setStateIcon(_nameMap.get(entry.getKey()), newIcon);
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _itemPanel);
    }
/*
    void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        setSignalHead(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        HashMap<String, NamedIcon> map1 = _itemPanel.getIconMap();
        if (map1 != null) {
            // map1 may be keyed with NamedBean names.  Convert to local name keys.
            // However perhaps keys are local - See above
            Hashtable<String, NamedIcon> map2 = new Hashtable<>();
            Iterator<Entry<String, NamedIcon>> it = map1.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                // TODO I18N use existing NamedBeanBundle keys before calling convertText(entry.getKey())?
                map2.put(jmri.jmrit.display.palette.ItemPalette.convertText(entry.getKey()), entry.getValue());
            }
            setIcons(map2);
        }   // otherwise retain current map
        displayState(getSignalHead().getAppearance());
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSignalHead"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "SignalHead", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.signalHeadPickModelInstance());
        Iterator<String> e = _iconMap.keySet().iterator();
        int i = 0;
        while (e.hasNext()) {
            String key = e.next();
            _iconEditor.setIcon(i++, key, new NamedIcon(_iconMap.get(key)));
        }
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateSignal();
            }
        };
        _iconEditor.complete(addIconAction, true, false, true);
        _iconEditor.setSelection(getSignalHead());
    }

    /**
     * replace the icons in _iconMap with those from map, but preserve the scale
     * and rotation.
     *
    private void setIcons(Hashtable<String, NamedIcon> map) {
        HashMap<String, NamedIcon> tempMap = new HashMap<>();
        Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            String name = entry.getKey();
            NamedIcon icon = entry.getValue();
            NamedIcon oldIcon = _saveMap.get(name); // setSignalHead() has cleared _iconMap
            if (log.isDebugEnabled()) {
                log.debug("key= " + entry.getKey() + ", localKey= " + name
                        + ", newIcon= " + icon + ", oldIcon= " + oldIcon);
            }
            tempMap.put(name, icon);
        }
        _iconMap = tempMap;
    }

    void updateSignal() {
        _saveMap = _iconMap;  // setSignalHead() clears _iconMap.  we need a copy for setIcons()
        setSignalHead(_iconEditor.getTableSelection().getDisplayName());
        setIcons(_iconEditor.getIconMap());
        displayState(headState());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
     * What to do on click? 0 means sequence through aspects; 1 means alternate
     * the "lit" aspect; 2 means alternate the "held" aspect.
     */
    protected int clickMode = 3;

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
     * means show "dark" if lit is set false.
     * <P>
     * Note that setting the appearance "DARK" explicitly will show the dark
     * icon regardless of how this is set.
     */
    protected boolean litMode = false;

    public void setLitMode(boolean mode) {
        litMode = mode;
    }

    public boolean getLitMode() {
        return litMode;
    }

    /**
     * Change the SignalHead state when the icon is clicked. Note that this
     * change may not be permanent if there is logic controlling the signal
     * head.
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
        if (getSignalHead() == null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0:
                switch (getSignalHead().getAppearance()) {
                    case jmri.SignalHead.RED:
                    case jmri.SignalHead.FLASHRED:
                        getSignalHead().setAppearance(jmri.SignalHead.YELLOW);
                        break;
                    case jmri.SignalHead.YELLOW:
                    case jmri.SignalHead.FLASHYELLOW:
                        getSignalHead().setAppearance(jmri.SignalHead.GREEN);
                        break;
                    case jmri.SignalHead.GREEN:
                    case jmri.SignalHead.FLASHGREEN:
                        getSignalHead().setAppearance(jmri.SignalHead.RED);
                        break;
                    default:
                        getSignalHead().setAppearance(jmri.SignalHead.RED);
                        break;
                }
                return;
            case 1:
                getSignalHead().setLit(!getSignalHead().getLit());
                return;
            case 2:
                getSignalHead().setHeld(!getSignalHead().getHeld());
                return;
            case 3:
                SignalHead sh = getSignalHead();
                int[] states = sh.getValidStates();
                int state = sh.getAppearance();
                for (int i = 0; i < states.length; i++) {
//                    if (log.isDebugEnabled()) log.debug("state= "+state+" states["+i+"]= "+states[i]);
                    if (state == states[i]) {
                        i++;
                        if (i >= states.length) {
                            i = 0;
                        }
                        state = states[i];
                        break;
                    }
                }
                sh.setAppearance(state);
                if (log.isDebugEnabled()) {
                    log.debug("Set state= " + state);
                }
                return;
            default:
                log.error("Click in mode " + clickMode);
        }
    }

    //private static boolean warned = false;
    @Override
    public void dispose() {
        if (getSignalHead() != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadIcon.class);
}
