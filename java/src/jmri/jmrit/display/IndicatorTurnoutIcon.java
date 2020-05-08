package jmri.jmrit.display;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status and state of a color coded turnout.<p>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <p>
 * "state" is the state of the underlying turnout ("closed", "thrown", etc.)
 * <p>
 * "status" is the operating condition of the track ("clear", "occupied", etc.)
 * <p>
 * A click on the icon will command a state change. Specifically, it will set
 * the CommandedState to the opposite (THROWN vs CLOSED) of the current
 * KnownState. This will display the setting of the turnout points.
 * <p>
 * The status is indicated by color and changes are done only done by the
 * occupancy sensing - OBlock or other sensor.
 * <p>
 * The default icons are for a left-handed turnout, facing point for east-bound
 * traffic.
 *
 * @author Pete Cressman Copyright (c) 2010 2012, 2018
 */
public class IndicatorTurnoutIcon extends TurnoutIcon implements IndicatorTrack {

    private HashMap<String, HashMap<String, DisplayState>> _displayMaps;      // state maps for each status

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private IndicatorTrackPaths _pathUtil;
    private IndicatorTOItemPanel _itemPanel;
    private String _status;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= {}, isText()= {}", isIcon(), isText());
        _status = "ClearTrack";
        setDisplayState("ClearTrack");
    }
    
    @Override
    protected void makeDisplayMap() {
        if (_pathUtil == null) {
            _pathUtil = new IndicatorTrackPaths();
        }
        Collection<String> col = _pathUtil.getStatusNameCollection();
        if (_displayMaps == null) {
            _displayMaps = new HashMap<>(col.size());
        }
        for (String status : col) {
            super.makeDisplayMap();     // make a bare DisplayStateMap
            HashMap<String, DisplayState> map = getDisplayStateMap();
            _displayMaps.put(status, map);
        }
    }
    
    public HashMap<String, HashMap<String, DisplayState>> getDisplayMaps() {
        return _displayMaps;
    }

    @Override
    public DisplayState getDisplayState(String state) {
        return _displayMaps.get(_status).get(state);
    }


//    @Override
    public static HashMap<String, PositionableLabel> cloneMap(HashMap<String, PositionableLabel> map,
            PositionableIcon pos) {
        HashMap<String, PositionableLabel> clone = new HashMap<>();
        if (map != null) {
            Iterator<Entry<String, PositionableLabel>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, PositionableLabel> entry = it.next();
                clone.put(entry.getKey(), (PositionableIcon)entry.getValue().deepClone());
                if (log.isDebugEnabled()) log.debug("clone status ", entry.getKey());
            }
        }
        return clone;
    }

    @Override
    public Positionable deepClone() {
        IndicatorTurnoutIcon pos = new IndicatorTurnoutIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(IndicatorTurnoutIcon pos) {
        pos.setOccBlockHandle(namedOccBlock);
        pos.setOccSensorHandle(namedOccSensor);
        pos._pathUtil = _pathUtil.deepClone();
        return super.finishClone(pos);
    }

    @Override
    public void setTurnout(NamedBeanHandle<Turnout> to) {
        super.setTurnout(to);
        displayState(_status, turnoutState());
    }

    /**
     * Attached a named sensor to display status from OBlocks
     *
     * @param pName Used as a system/user name to lookup the sensor object
     */
    @Override
    public void setOccSensor(String pName) {
        if (pName == null || pName.trim().length() == 0) {
            setOccSensorHandle(null);
            return;
        }
        if (InstanceManager.getNullableDefault(jmri.SensorManager.class) != null) {
            try {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                setOccSensorHandle(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
            } catch (IllegalArgumentException ex) {
                log.error("Occupancy Sensor '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No SensorManager for this protocol, block icons won't see changes");
        }
    }

    @Override
    public void setOccSensorHandle(NamedBeanHandle<Sensor> sen) {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = sen;
        if (namedOccSensor != null) {
            Sensor sensor = getOccSensor();
            sensor.addPropertyChangeListener(this, namedOccSensor.getName(), "Indicator Turnout Icon");
            _status = _pathUtil.getStatus(sensor.getKnownState());
            displayState(_status, turnoutState());
        }
    }

    @Override
    public Sensor getOccSensor() {
        if (namedOccSensor == null) {
            return null;
        }
        return namedOccSensor.getBean();
    }

    @Override
    public NamedBeanHandle<Sensor> getNamedOccSensor() {
        return namedOccSensor;
    }

    /**
     * Attached a named OBlock to display status
     *
     * @param pName Used as a system/user name to lookup the OBlock object
     */
    @Override
    public void setOccBlock(String pName) {
        if (pName == null || pName.trim().length() == 0) {
            setOccBlockHandle(null);
            return;
        }
        OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(pName);
        if (block != null) {
            setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(pName, block));
        } else {
            log.error("Detection OBlock '{}' not available, icon won't see changes", pName);
        }
    }

    @Override
    public void setOccBlockHandle(NamedBeanHandle<OBlock> blockHandle) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = blockHandle;
        if (namedOccBlock != null) {
            OBlock block = getOccBlock();
            block.addPropertyChangeListener(this, namedOccBlock.getName(), "Indicator Turnout Icon");
            setStatus(block, block.getState());
            displayState(_status, turnoutState());
            setToolTip(new ToolTip(block.getDescription(), 0, 0));
        } else {
            setToolTip(new ToolTip(null, 0, 0));
        }
    }

    @Override
    public OBlock getOccBlock() {
        if (namedOccBlock == null) {
            return null;
        }
        return namedOccBlock.getBean();
    }

    @Override
    public NamedBeanHandle<OBlock> getNamedOccBlock() {
        return namedOccBlock;
    }

    @Override
    public void setShowTrain(boolean set) {
        _pathUtil.setShowTrain(set);
    }

    @Override
    public boolean showTrain() {
        return _pathUtil.showTrain();
    }

    @Override
    public ArrayList<String> getPaths() {
        return _pathUtil.getPaths();
    }

    public void setPaths(ArrayList<String> paths) {
        _pathUtil.setPaths(paths);
    }

    @Override
    public void addPath(String path) {
        _pathUtil.addPath(path);
    }

    @Override
    public void removePath(String path) {
        _pathUtil.removePath(path);
    }

    /**
     * get track name for known state of occupancy sensor
     */
    @Override
    public void setStatus(int state) {
        _status = _pathUtil.getStatus(state);
    }

    /**
     * Place icon by its localized bean state name
     *
     * @param status     the track condition of the icon
     * @param stateName  NamedBean name of turnout state
     * @param icon       icon corresponding to status and state
     */
    public void setStateIcon(String status, String stateName, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("setStateIcon for status= \"{}\", state= \"{}\" icom= {}",
                    status, stateName, (icon!=null?icon.getURL():"null"));
        }
        HashMap<String, DisplayState> stateMap = _displayMaps.get(status);
        DisplayState ds = stateMap.get(stateName);
        ds.setIcon(icon);
    }

    public String getStatus() {
        return _status;
    }

    @Override
    public void displayState() {
        displayState(_status, turnoutState());
    }

    /**
     * Drive the current state of the display from the state of the turnout and
     * status of track.
     */
    private void displayState(String status, String state) {
        if (getNamedTurnout() == null) {
            setDisconnectedText("BeanDisconnected");
        } else {
            restoreConnectionDisplay();
        }

        HashMap<String, DisplayState> stateMap = _displayMaps.get(status);
        DisplayState displayState = stateMap.get(state);
        displayState.setDisplayParameters(this);
        setDisplayState(state);

        updateSize();
    }

    @Override
    public String getNameString() {
        String str = "";
        if (namedOccBlock != null) {
            str = " in " + namedOccBlock.getBean().getDisplayName();
        } else if (namedOccSensor != null) {
            str = " on " + namedOccSensor.getBean().getDisplayName();
        }
        return "ITrack " + super.getNameString() + str;
    }

    // update icon as state of turnout changes and status of track changes
    // Override
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("property change of {}: property \"{}\"= {} from ",
                getNameString(), evt.getPropertyName(), evt.getNewValue(), evt.getSource().getClass().getName());
        }

        Object source = evt.getSource();
        if (source instanceof Turnout) {
            // when there's feedback, transition through inconsistent icon for better
            // animation
            if (getTristate()
                    && (getTurnout().getFeedbackMode() != Turnout.DIRECT)
                    && (evt.getPropertyName().equals("CommandedState"))) {
                if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
                    int now = Turnout.INCONSISTENT;
                    displayState(_status, _state2nameMap.get(now));
                }
                // this takes care of the quick double click
                if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
                    int now = ((Integer) evt.getNewValue()).intValue();
                    displayState(_status, _state2nameMap.get(now));
                }
            }

            if (evt.getPropertyName().equals("KnownState")) {
                int now = ((Integer) evt.getNewValue()).intValue();
                displayState(_status, _state2nameMap.get(now));
            }
        } else {
            if (source instanceof OBlock) {
                String property = evt.getPropertyName();
                if ("state".equals(property) || "pathState".equals(property)) {
                    int now = ((Integer) evt.getNewValue()).intValue();
                    setStatus((OBlock) source, now);
                } else if ("pathName".equals(property)) {
                    _pathUtil.removePath((String) evt.getOldValue());
                    _pathUtil.addPath((String) evt.getNewValue());
                }
            } else if (source instanceof Sensor) {
                if (evt.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) evt.getNewValue()).intValue();
                    if (source.equals(getOccSensor())) {
                        _status = _pathUtil.getStatus(now);
                    }
                }
            }
            displayState(_status, turnoutState());
        }
    }

    private void setStatus(OBlock block, int state) {
        _status = _pathUtil.getStatus(block, state);
        if ((state & (OBlock.OCCUPIED | OBlock.RUNNING)) != 0) {
            _pathUtil.setLocoIcon(block, getLocation(), getSize(), _editor);
            repaint();
        }
        if ((block.getState() & OBlock.OUT_OF_SERVICE) != 0) {
            setControlling(false);
        } else {
            setControlling(true);
        }
    }

    @Override
    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("IndicatorTO")));
        _itemPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO", getFamily(),
                PickListModel.turnoutPickModelInstance());
        ActionListener updateAction = a -> updateItem();
        HashMap<String, HashMap<String, NamedIcon>> iconMaps = new HashMap<>();
        for (String status : _pathUtil.getStatusNameCollection()) {
            HashMap<String, DisplayState> statusMap = _displayMaps.get(status);

            HashMap<String, NamedIcon> map = new HashMap<>();
            Iterator<String> iter = getStateNames();    // state names for this status name
            while (iter.hasNext()) {
                String state  = iter.next();
                DisplayState displayState = statusMap.get(state);
                NamedIcon oldIcon = displayState.getIcon();
                NamedIcon newIcon = new NamedIcon(oldIcon);
                map.put(state, newIcon);
            }
            iconMaps.put(status, map);
        }
        _itemPanel.initUpdate(updateAction, iconMaps);
        
        if (namedOccSensor != null) {
            _itemPanel.setOccDetector(namedOccSensor.getBean().getDisplayName());
        }
        if (namedOccBlock != null) {
            _itemPanel.setOccDetector(namedOccBlock.getBean().getDisplayName());
        }
        _itemPanel.setShowTrainName(_pathUtil.showTrain());
        _itemPanel.setPaths(_pathUtil.getPaths());
        _itemPanel.setSelection(getTurnout());  // do after all other params set - calls resize()
        
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    @Override
    void updateItem() {
        if (log.isDebugEnabled()) {
            log.debug("updateItem: {} family= {}", getNameString(), _itemPanel.getFamilyName());
        }
        setTurnout(_itemPanel.getTableSelection().getSystemName());
        setOccSensor(_itemPanel.getOccSensor());
        setOccBlock(_itemPanel.getOccBlock());
        _pathUtil.setShowTrain(_itemPanel.getShowTrainName());
        setFamily(_itemPanel.getFamilyName());
        _pathUtil.setPaths(_itemPanel.getPaths());
        HashMap<String, HashMap<String, NamedIcon>> iconMap = _itemPanel.getIconMaps();
        if (iconMap != null) {
            for (Entry<String, HashMap<String, NamedIcon>> entry : iconMap.entrySet()) {
                String status = entry.getKey();
                HashMap<String, DisplayState> statusMap = _displayMaps.get(status);
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    if (log.isDebugEnabled()) {
                        log.debug("key= {}", ent.getKey());
                    }
                    NamedIcon icon = new NamedIcon(ent.getValue());
                    DisplayState displayState = statusMap.get(ent.getKey());
                    displayState.setIcon(icon);
                }
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _itemPanel);
        displayState(_status, turnoutState());
    }

    @Override
    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccSensor = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIcon.class);
}
