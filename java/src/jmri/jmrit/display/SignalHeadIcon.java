package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
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
import jmri.jmrit.display.palette.TableItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a SignalHead.
 * <p>
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
    private NameCollection _validStateNames;

    public SignalHeadIcon(Editor editor) {
        super(editor);
        setIsIcon(true);
        makeStateNameCollection();
        makeDisplayMap();
        setDisplayState("SignalHeadStateDark");
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
            pos._validStateNames = new NameCollection(_validStateNames.getArray());
       }
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        return super.finishClone(pos);
    }

    private void makeStateNameCollection() {
        SignalHead h = getSignalHead();
        String[] names;
        if (h == null) {
            names = jmri.implementation.AbstractSignalHead.getDefaultValidStateKeys();
        } else {
            names = h.getValidStateKeys();
        }
        String[] validnames = new String[names.length + 3];
        for (int i = 0 ; i < names.length ; i++) {
            validnames[i] = names[i];
        }
        validnames[names.length] = "SignalHeadStateHeld";
        validnames[names.length + 1] = "SignalHeadStateDark";
        validnames[names.length + 2] = "SignalHeadStateLit";
        _validStateNames = new NameCollection(validnames);
    }

    @Override
    protected Collection<String> getStateNameCollection() {
        return _validStateNames;
    }

    @Override
    protected void makeDisplayMap() {
        makeStateNameCollection();
        super.makeDisplayMap();
    }

    /**
     * Attach a SignalHead element to this display item by bean.
     *
     * @param sh the specific SignalHead object to attach
     */
    public void setSignalHead(NamedBeanHandle<SignalHead> sh) {
        if (namedHead != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = sh;
        if (namedHead != null) {
            getSignalHead().addPropertyChangeListener(this, namedHead.getName(), "SignalHead Icon");
            displayState(headState());
        }
    }

    /**
     * Attach a SignalHead element to this display item by name. Taken from the
     * Layout Editor.
     *
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        SignalHead mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getNamedBean(pName);
        if (mHead == null) {
            log.warn("did not find a SignalHead named {}", pName);
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

    @Override
    public void displayState() {
        displayState(headState());
    }
    /**
     * Drive the current state of the display from the state of the underlying
     * SignalHead object.
     * <ul>
     * <li>If the signal is held, display that.
     * <li>If set to monitor the status of the lit parameter and lit is false,
     * show the dark icon ("dark", when set as an explicit appearance, is
     * displayed anyway)
     * <li>Show the icon corresponding to one of the (max seven) appearances.
     * </ul>
     * @param state appearance of head
     */
    private void displayState(int state) {
        updateSize();
        SignalHead h = getSignalHead();
        if (h != null) {
            if (h.getHeld()) {
                displayState("SignalHeadStateHeld");
                return;
            } else if (getLitMode() && !h.getLit()) {
                displayState("SignalHeadStateDark");
                return;
            }
        }
        displayState(getSignalHead().getAppearanceKey(state));
    }



    /**
     * Get current appearance of the head.
     *
     * @return an appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    private int headState() {
        SignalHead h = getSignalHead();
        if (h == null) {
            return 0;
        } else {
            return h.getAppearance();
        }
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("property change: {} current state: {}", e.getPropertyName(), headState());
        displayState(headState());
        _editor.getTargetPanel().repaint();
    }

    @Override
    public @Nonnull
    String getNameString() {
        if (namedHead == null) {
            return Bundle.getMessage("NotConnected");
        }
        return namedHead.getName(); // short NamedIcon name
    }

    private ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
   @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add menu to select action on click
            JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
            ButtonGroup clickButtonGroup = new ButtonGroup();
            JRadioButtonMenuItem r;
            r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
            r.addActionListener(e -> setClickMode(3));
            clickButtonGroup.add(r);
            if (clickMode == 3) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("Cycle3Aspects"));
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
            r.addActionListener(e -> setLitMode(false));
            litButtonGroup.add(r);
            if (!litMode) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            litMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("ShowDarkIcon"));
            r.setIconTextGap(10);
            r.addActionListener(e -> setLitMode(true));
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
        return false;
    }

   TableItemPanel<SignalHead> _itemPanel;

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameSignalHead")));
        _itemPanel = new SignalHeadItemPanel(_paletteFrame, "SignalHead", getFamily(),
                PickListModel.signalHeadPickModelInstance()); //NOI18N
        ActionListener updateAction = a -> updateItem();
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
        makeDisplayMap();
        HashMap<String, NamedIcon> iconMap = _itemPanel.getIconMap();
        if (iconMap != null) {
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) {
                    log.debug("key= " + entry.getKey());
                }
                NamedIcon newIcon = new NamedIcon(entry.getValue());
                setStateIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _itemPanel);
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
     * <p>
     * False means ignore (always show R/Y/G/etc appearance on screen); True
     * means show "dark" if lit is set false.
     * <p>
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
                log.debug("Set state= {}", state);
                return;
            default:
                log.error("Click in mode {}", clickMode);
        }
    }

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
