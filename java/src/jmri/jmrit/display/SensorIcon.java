package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.TableItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a Sensor.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Pete Cressman Copyright (C) 2010, 2011, 2018
 */
public class SensorIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    // the associated Sensor object
    private NamedBeanHandle<Sensor> namedSensor;
    boolean _momentary = false;

    //  Map state integer to state name found in jmri.NamedBeanBundle.properties to display
    private HashMap<Integer, String> _state2nameMap;

    public SensorIcon(Editor editor) {
        super(editor);
        setIsIcon(true);
    }

    public SensorIcon(NamedIcon s, Editor editor) {
        super(s, editor);
    }

    public SensorIcon(String s, Editor editor) {
        super(s, editor);
    }

    @Override
    public Positionable deepClone() {
        SensorIcon pos = new SensorIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(SensorIcon pos) {
        NamedBeanHandle<Sensor> sensor = getNamedSensor();
        if (sensor != null) {
            pos.setSensor(sensor.getName());            
        }
        pos.makeStateNameMap();
        pos.setMomentary(getMomentary());
        return super.finishClone(pos);
    }

    /**
     * Attached a named sensor to this display item
     *
     * @param pName System/user name to lookup the sensor object
     */
    public void setSensor(String pName) {
        if (InstanceManager.getNullableDefault(jmri.SensorManager.class) != null) {
            try {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                setSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
            } catch (IllegalArgumentException ex) {
                log.error("Sensor '" + pName + "' not available, icon won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
    }

    /**
     * Attached a named sensor to this display item
     *
     * @param s the Sensor
     */
    public void setSensor(NamedBeanHandle<Sensor> s) {
        if (namedSensor != null) {
            getSensor().removePropertyChangeListener(this);
        }

        namedSensor = s;
        if (namedSensor != null) {
            getSensor().addPropertyChangeListener(this, s.getName(), "SensorIcon on Panel " + _editor.getName());
            setName(namedSensor.getName());  // Swing name for e.g. tests
            displayState(sensorState());
        }
    }

    public Sensor getSensor() {
        if (namedSensor == null) {
            return null;
        }
        return namedSensor.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getSensor();
    }

    public NamedBeanHandle<Sensor> getNamedSensor() {
        return namedSensor;
    }

    private void makeStateNameMap() {
        _state2nameMap = new HashMap<>();
        _state2nameMap.put(Sensor.UNKNOWN, "BeanStateUnknown");
        _state2nameMap.put(Sensor.INCONSISTENT, "BeanStateInconsistent");
        _state2nameMap.put(Sensor.ACTIVE, "SensorStateActive");
        _state2nameMap.put(Sensor.INACTIVE, "SensorStateInactive");
    }

    @Override
    protected HashMap<String, PositionableLabel> makeDefaultMap() {
        makeStateNameMap();
        HashMap<String, PositionableLabel> oldMap = getIconMap();
        HashMap<String, PositionableLabel> map = new HashMap<>();
        Iterator <String> iter = _state2nameMap.values().iterator();
        while (iter.hasNext()) {
            String state = iter.next();
            PositionableLabel pos;
            if (oldMap != null) {
                pos = oldMap.get(state);
            } else {
                pos = new PositionableLabel(getEditor());
                pos.setText(Bundle.getMessage(state));
            }
            map.put(state, pos);
        }
        return map;
    }

    /**
     * Place icon by its bean state name key found in 
     * the properties file jmri.NamedBeanBundle.properties
     *
     * @param name sensor state name
     * @param icon the icon to display the state
     */
    @Override
    public void setStateIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("setIcon for name \"" + name + "\"");
        }
        super.setStateIcon(name, icon);
//        displayState(sensorState());
    }

    /**
     * Get icon by its localized bean state name.
     *
     * @param state the state to get the icon for
     * @return the icon or null if state not found
     *
    public NamedIcon getIcon(int state) {
        return getIcon(_state2nameMap.get(state));
    }*/

    /**
     * Get current state of attached sensor
     * state name key found in
     * jmri.NamedBeanBundle.properties
     *
     * @return A state variable name from a Sensor
     */
    protected String sensorState() {
        if (namedSensor != null) {
            return  _state2nameMap.get(getSensor().getKnownState());
        } else {
            return _state2nameMap.get(Sensor.UNKNOWN);
        }
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("property change: {}", e);
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue());
            displayState(_state2nameMap.get((now)));
            _editor.repaint();
        }
    }

    @Override
    public String getNameString() {
        String name;
        if (namedSensor == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (getSensor().getUserName() == null) {
            name = getSensor().getSystemName();
        } else {
            name = getSensor().getUserName() + " (" + getSensor().getSystemName() + ")";
        }
        return name;
    }

    /**
     * Pop-up just displays the sensor name.
     *
     * @param popup the menu to display
     * @return always true
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        setAdditionalViewPopUpMenu(popup);
        return true;
    }

    @Override
    public void displayState() {
        displayState(sensorState());
    }

    /**
     * Drive the current state of the display from the state of the sensor.
     *
     * @param state the sensor state
     */
    private void displayState(String state) {
        if (getNamedSensor() == null) {
            setDisconnectedText("disconnected");
        } else {
            restoreConnectionDisplay();
        }
        setDisplayState(state);
        if (isText() && isIcon()) {  // Overlaid text
            setIcon(getIcon(state));
        }
    }

    TableItemPanel _itemPanel;
    JCheckBoxMenuItem momentaryItem = new JCheckBoxMenuItem(Bundle.getMessage("Momentary"));


    @Override
    public boolean setIconEditMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSensor"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });

        popup.add(momentaryItem);
        momentaryItem.setSelected(getMomentary());
        momentaryItem.addActionListener((java.awt.event.ActionEvent e) -> {
            setMomentary(momentaryItem.isSelected());
        });
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSensor")));
        _itemPanel = new TableItemPanel(_paletteFrame, "Sensor", getFamily(),
                PickListModel.sensorPickModelInstance(), _editor); // NOI18N
        ActionListener updateAction = (ActionEvent a) -> {
            updateItem();
        };
        // duplicate _iconMap map
        HashMap<String, NamedIcon> map = new HashMap<>();
        Iterator<String> iter = getIconStateNames();
        while (iter.hasNext()) {
            String  state = iter.next();
            NamedIcon oldIcon = getIcon(state);
            NamedIcon newIcon = new NamedIcon(oldIcon);
            map.put(state, newIcon);
        }
        _itemPanel.init(updateAction, map);
        _itemPanel.setSelection(getSensor());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    protected void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        setSensor(_itemPanel.getTableSelection().getSystemName());
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
                setStateIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _itemPanel);
    }
/*
    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSensor"));
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
        makeIconEditorFrame(this, "Sensor", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.sensorPickModelInstance());
        Iterator<String> e = _iconMap.keySet().iterator();
        int i = 0;
        while (e.hasNext()) {
            String key = e.next();
            _iconEditor.setIcon(i++, /*_state2nameMap.get(key) key, _iconMap.get(key));
        }
        _iconEditor.makeIconPanel(false);

        // set default icons, then override with this turnout's icons
        ActionListener addIconAction = (ActionEvent a) -> {
            updateSensor();
        };
        _iconEditor.complete(addIconAction, true, true, true);
        _iconEditor.setSelection(getSensor());
    }

    void updateSensor() {
        HashMap<String, NamedIcon> oldMap = cloneMap(_iconMap, this);
        setSensor(_iconEditor.getTableSelection().getDisplayName());
        Hashtable<String, NamedIcon> iconMap = _iconEditor.getIconMap();

        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            if (log.isDebugEnabled()) {
                log.debug("key= " + entry.getKey());
            }
            NamedIcon newIcon = entry.getValue();
            setStateIcon(entry.getKey(), newIcon);
        }
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }*/

    public boolean getMomentary() {
        return _momentary;
    }

    public void setMomentary(boolean m) {
        _momentary = m;
    }

    public boolean buttonLive() {
        if (namedSensor == null) {  // no sensor connected for this protocol
            log.error("No sensor connection, can't process click");
            return false;
        }
        return _editor.getFlag(Editor.OPTION_CONTROLS, isControlling());
    }

    @Override
    public void doMousePressed(MouseEvent e) {
        log.debug("doMousePressed buttonLive={}, getMomentary={}", buttonLive(), getMomentary());
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button press
            try {
                getSensor().setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: " + reason);
            }
        }
        super.doMousePressed(e);
    }

    @Override
    public void doMouseReleased(MouseEvent e) {
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button release
            try {
                getSensor().setKnownState(jmri.Sensor.INACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: " + reason);
            }
        }
        super.doMouseReleased(e);
    }

    @Override
    public void doMouseClicked(MouseEvent e) {
        if (buttonLive() && !getMomentary()) {
            // this button responds to clicks
            if (!e.isMetaDown() && !e.isAltDown()) {
                try {
                    if (getSensor().getKnownState() == jmri.Sensor.INACTIVE) {
                        getSensor().setKnownState(jmri.Sensor.ACTIVE);
                    } else {
                        getSensor().setKnownState(jmri.Sensor.INACTIVE);
                    }
                } catch (jmri.JmriException reason) {
                    log.warn("Exception flipping sensor: " + reason);
                }
            }
        }
        super.doMouseClicked(e);
    }

    @Override
    public void dispose() {
        if (namedSensor != null) {
            getSensor().removePropertyChangeListener(this);
        }
        namedSensor = null;
        _state2nameMap = null;
        super.dispose();
    }

    int flashStateOn = -1;
    int flashStateOff = -1;
    boolean flashon = false;
    ActionListener taskPerformer;
    Timer flashTimer;

    synchronized public void flashSensor(int tps, int state1, int state2) {
        if ((flashTimer != null) && flashTimer.isRunning()) {
            return;
        }
        //Set the maximum number of state changes to 10 per second
        if (tps > 10) {
            tps = 10;
        } else if (tps <= 0) {
            return;
        }
        if ((_state2nameMap.get(state1) == null) || _state2nameMap.get(state2) == null) {
            log.error("one or other of the states passed for flash is null");
            return;
        } else if (state1 == state2) {
            log.debug("Both states to flash between are the same, therefore no flashing will occur");
            return;
        }
        int interval = (1000 / tps) / 2;
        flashStateOn = state1;
        flashStateOff = state2;
        if (taskPerformer == null) {
            taskPerformer = (ActionEvent evt) -> {
                if (flashon) {
                    flashon = false;
                    displayState(_state2nameMap.get(flashStateOn));
                } else {
                    flashon = true;
                    displayState(_state2nameMap.get(flashStateOff));
                }
            };
        }
        flashTimer = new Timer(interval, taskPerformer);
        flashTimer.start();
    }

    synchronized public void stopFlash() {
        if (flashTimer != null) {
            flashTimer.stop();
        }
        displayState(sensorState());
    }

    private final static Logger log = LoggerFactory.getLogger(SensorIcon.class);
}
