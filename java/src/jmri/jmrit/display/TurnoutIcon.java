package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.TableItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a turnout.
 * <p>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <p>
 * A click on the icon will command a state change. Specifically, it will set
 * the CommandedState to the opposite (THROWN vs CLOSED) of the current
 * KnownState.
 * <p>
 * The default icons are for a left-handed turnout, facing point for east-bound
 * traffic.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author PeteCressman Copyright (C) 2010, 2011, 2018
 */
public class TurnoutIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    // the associated Turnout object
    private NamedBeanHandle<Turnout> namedTurnout = null;
    protected HashMap<Integer, String> _state2nameMap =  new HashMap<>(4);

    public TurnoutIcon(Editor editor) {
        super(editor);
        makeStateNameMap();
        setIsIcon(true);
        makeDisplayMap();
        setDisplayState(Bundle.getMessage("BeanStateUnknown"));
    }

    @Override
    public Positionable deepClone() {
        TurnoutIcon pos = new TurnoutIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(TurnoutIcon pos) {
        NamedBeanHandle<Turnout> to = getNamedTurnout();
        if (to != null) {
            pos.setTurnout(to.getName());
        }
        pos.setTristate(getTristate());
        pos.setMomentary(getMomentary());
        pos.setDirectControl(getDirectControl());
        return super.finishClone(pos);
    }

    private void makeStateNameMap() {
        _state2nameMap.clear();
        _state2nameMap.put(Integer.valueOf(Turnout.UNKNOWN), "BeanStateUnknown");
        _state2nameMap.put(Integer.valueOf(Turnout.INCONSISTENT), "BeanStateInconsistent");
        _state2nameMap.put(Integer.valueOf(Turnout.CLOSED), "TurnoutStateClosed");
        _state2nameMap.put(Integer.valueOf(Turnout.THROWN), "TurnoutStateThrown");
    }

    @Override
    protected Collection<String> getStateNameCollection() {
        return _state2nameMap.values();
    }

    /**
     * Attach a named turnout to this display item.
     *
     * @param pName Used as a system/user name to lookup the turnout object
     */
    public void setTurnout(String pName) {
        if (InstanceManager.getNullableDefault(jmri.TurnoutManager.class) != null) {
            try {
                Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout(pName);
                setTurnout(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, turnout));
            } catch (IllegalArgumentException ex) {
                log.error("Turnout '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No TurnoutManager for this protocol, icon won't see changes");
        }
    }

    public void setTurnout(NamedBeanHandle<Turnout> to) {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = to;
        if (namedTurnout != null) {
            displayState(turnoutState());
            getTurnout().addPropertyChangeListener(this, namedTurnout.getName(), "Panel Editor Turnout Icon");
        }
    }

    public Turnout getTurnout() {
        return namedTurnout.getBean();
    }

    public NamedBeanHandle<Turnout> getNamedTurnout() {
        return namedTurnout;
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getTurnout();
    }

    /**
     * Get current state of attached turnout
     *
     * @return A state name from a Turnout, e.g. Turnout.CLOSED
     */
    protected String turnoutState() {
        String state;
        if (namedTurnout != null) {
            state = _state2nameMap.get(getTurnout().getKnownState());
        } else {
            state = _state2nameMap.get(Turnout.UNKNOWN);
        }
        return state;
    }

    @Override
    public void displayState() {
        displayState(turnoutState());
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: {} {} is now {}", getNameString(), e.getPropertyName(), e.getNewValue());
        }

        // when there's feedback, transition through inconsistent icon for better
        // animation
        if (getTristate()
                && (getTurnout().getFeedbackMode() != Turnout.DIRECT)
                && (e.getPropertyName().equals("CommandedState"))) {
            if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
                int now = Turnout.INCONSISTENT;
                displayState(_state2nameMap.get(now));
            }
            // this takes care of the quick double click
            if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
                int now = ((Integer) e.getNewValue()).intValue();
                displayState(_state2nameMap.get(now));
            }
        }

        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(_state2nameMap.get(now));
        }
    }

    public String getStateName(int state) {
        return _state2nameMap.get(state);

    }

    @Override
    public String getNameString() {
        String name;
        if (namedTurnout == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getTurnout().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    public void setTristate(boolean set) {
        tristate = set;
    }

    public boolean getTristate() {
        return tristate;
    }
    private boolean tristate = false;

    boolean momentary = false;

    public boolean getMomentary() {
        return momentary;
    }

    public void setMomentary(boolean m) {
        momentary = m;
    }

    boolean directControl = false;

    public boolean getDirectControl() {
        return directControl;
    }

    public void setDirectControl(boolean m) {
        directControl = m;
    }

    JCheckBoxMenuItem momentaryItem = new JCheckBoxMenuItem(Bundle.getMessage("Momentary"));
    JCheckBoxMenuItem directControlItem = new JCheckBoxMenuItem(Bundle.getMessage("DirectControl"));

    /**
     * Pop-up displays non editing popups
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add tristate option if turnout has feedback
            if (namedTurnout != null && getTurnout().getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
            }

            popup.add(momentaryItem);
            momentaryItem.setSelected(getMomentary());
            momentaryItem.addActionListener(e -> setMomentary(momentaryItem.isSelected()));

            popup.add(directControlItem);
            directControlItem.setSelected(getDirectControl());
            directControlItem.addActionListener(e -> setDirectControl(directControlItem.isSelected()));
        } else if (getDirectControl()) {
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        }
        return true;
    }

    javax.swing.JCheckBoxMenuItem tristateItem = null;

    void addTristateEntry(JPopupMenu popup) {
        tristateItem = new javax.swing.JCheckBoxMenuItem(Bundle.getMessage("Tristate"));
        tristateItem.setSelected(getTristate());
        popup.add(tristateItem);
        tristateItem.addActionListener(e -> setTristate(tristateItem.isSelected()));
    }

    /**
     * ****** popup AbstractAction method overrides ********
     */

    TableItemPanel<Turnout> _itemPanel;

    @Override
    public boolean setIconEditMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        if (namedTurnout != null && getTurnout().getFeedbackMode() != Turnout.DIRECT) {
            addTristateEntry(popup);
        }

        popup.add(momentaryItem);
        momentaryItem.setSelected(getMomentary());
        momentaryItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setMomentary(momentaryItem.isSelected());
            }
        });

        popup.add(directControlItem);
        directControlItem.setSelected(getDirectControl());
        directControlItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setDirectControl(directControlItem.isSelected());
            }
        });
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameTurnout")));
        _itemPanel = new TableItemPanel<>(_paletteFrame, "Turnout", getFamily(),
                PickListModel.turnoutPickModelInstance()); // NOI18N
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
        _itemPanel.setSelection(getTurnout());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        setTurnout(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        HashMap<String, NamedIcon> iconMap = _itemPanel.getIconMap();
        if (iconMap != null) {
            for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("key= {}", entry.getKey());
                }
                NamedIcon newIcon = new NamedIcon(entry.getValue());
                setStateIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

/*    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "Turnout", true, null); // NOI18N
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.turnoutPickModelInstance());
        int i = 0;
        for (Entry<Integer, NamedIcon> entry : _iconStateMap.entrySet()) {
            _iconEditor.setIcon(i++, _state2nameMap.get(entry.getKey()), entry.getValue());
        }
        _iconEditor.makeIconPanel(false);

        // set default icons, then override with this turnout's icons
        ActionListener addIconAction = a -> updateTurnout();
        _iconEditor.complete(addIconAction, true, true, true);
        _iconEditor.setSelection(getTurnout());
    }

    void updateTurnout() {
        HashMap<Integer, NamedIcon> oldMap = cloneMap(_iconStateMap, this);
        setTurnout(_iconEditor.getTableSelection().getDisplayName());
        Hashtable<String, NamedIcon> iconMap = _iconEditor.getIconMap();

        for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug("key= {}", entry.getKey());
            }
            NamedIcon newIcon = entry.getValue();
            NamedIcon oldIcon = oldMap.get(_name2stateMap.get(entry.getKey()));
            setIcon(entry.getKey(), newIcon);
        }
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }*/

    public boolean buttonLive() {
        if (namedTurnout == null) {
            log.error("No turnout connection, can't process click");
            return false;
        }
        return true;
    }

    @Override
    public void doMousePressed(MouseEvent e) {
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button press
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        }
        super.doMousePressed(e);
    }

    @Override
    public void doMouseReleased(MouseEvent e) {
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button release
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
        }
        super.doMouseReleased(e);
    }

    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        if (e.isMetaDown() || e.isAltDown() || !buttonLive() || getMomentary()) {
            return;
        }

        if (getDirectControl() && !isEditable()) {
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
        } else {
            alternateOnClick();
        }
    }

    void alternateOnClick() {
        if (getTurnout().getKnownState() == jmri.Turnout.CLOSED) {  // if clear known state, set to opposite
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        } else if (getTurnout().getKnownState() == jmri.Turnout.THROWN) {
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
        } else if (getTurnout().getCommandedState() == jmri.Turnout.CLOSED) {
            getTurnout().setCommandedState(jmri.Turnout.THROWN);  // otherwise, set to opposite of current commanded state if known
        } else {
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);  // just force closed.
        }
    }

    @Override
    public void dispose() {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = null;
        _state2nameMap = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutIcon.class);
}
