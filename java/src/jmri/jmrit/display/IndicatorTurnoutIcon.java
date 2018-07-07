package jmri.jmrit.display;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
 * An icon to display a status and state of a color coded turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <p>
 * "state" is the state of the underlying turnout ("closed", "thrown", etc.)
 * <p>
 * "status" is the operating condition of the track ("clear", "occupied", etc.)
 * <P>
 * A click on the icon will command a state change. Specifically, it will set
 * the CommandedState to the opposite (THROWN vs CLOSED) of the current
 * KnownState. This will display the setting of the turnout points.
 * <P>
 * The status is indicated by color and changes are done only done by the
 * occupancy sensing - OBlock or other sensor.
 * <p>
 * The default icons are for a left-handed turnout, facing point for east-bound
 * traffic.
 *
 * @author Pete Cressman Copyright (c) 2010 2012, 2018
 */
public class IndicatorTurnoutIcon extends TurnoutIcon implements IndicatorTrack {

    // Replaces _iconMap of PositionableIcon ancestor
    //HashMap<String, HashMap<String, NamedIcon>> _iconMaps;      // state icons for each status

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private IndicatorTrackPaths _pathUtil;
    private IndicatorTOItemPanel _TOPanel;
    private String _status;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= {}, isText()= {}", isIcon(), isText());
        _pathUtil = new IndicatorTrackPaths();
        _status = "ClearTrack";
        setDisplayState("ClearTrack");
    }
    
    static final String[] STATUSNAME = {"ClearTrack", "OccupiedTrack", "PositionTrack", "AllocatedTrack", "DontUseTrack", "ErrorTrack"};

    @Override
    protected HashMap<String, PositionableLabel> makeDefaultMap() {
        HashMap<String, PositionableLabel> map = new HashMap<> ();
        for (String status : STATUSNAME) {
            PositionableIcon pos = new TurnoutIcon(getEditor());
            pos.setIconMap(pos. makeDefaultMap());  // each status needs state icons and text
            map.put(status, pos);
        }
        return map;
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

/*    @Override
    public void setTurnout(NamedBeanHandle<Turnout> to) {
        super.setTurnout(to);
        Iterator<PositionableLabel> iter = getIconMap().values().iterator();
        while (iter.hasNext()) {
            ((PositionableIcon)iter.next()).setTurnout(to);
        }

    }*/

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
                log.error("Occupancy Sensor '" + pName + "' not available, icon won't see changes");
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
            log.error("Detection OBlock '" + pName + "' not available, icon won't see changes");
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
     * @param status    - the track condition of the icon
     * @param stateName - NamedBean name of turnout state
     * @param icon      - icon corresponding to status and state
     */
    public void setStateIcon(String status, String stateName, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("setIcon for status= \"{}\", state= \"{}\" icom= {}",
                    status, stateName, icon.getURL());
        }
        PositionableIcon statusMap = (PositionableIcon)getStateData(status);
        statusMap.setStateIcon(stateName, icon);
        if (status == _status && stateName == turnoutState()) {
            setIcon(icon);
        }
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
            setDisconnectedText("disconnected");
        } else {
            restoreConnectionDisplay();
        }
        setDisplayState(status);
        PositionableIcon turnoutIcon = (PositionableIcon)getStateData(_status);
        turnoutIcon.setDisplayState(state);
        PositionableLabel pos = turnoutIcon.getStateData(state);
        pos.updateSize();
        if (log.isDebugEnabled()) {
            log.debug("displayState of status= \"{}\", state= \"{}\" icon= {}",
                    status, state, turnoutIcon.getIcon(state).getURL());
        }

//        turnoutIcon.displayState(state);
        if (isText() && isIcon()) {  // Overlaid text
            setIcon(turnoutIcon.getIcon(state));
        }
        repaint();
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
            super.propertyChange(evt);
        } else if (source instanceof OBlock) {
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
        _TOPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO", getFamily(),
                PickListModel.turnoutPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // Convert _iconMaps state (ints) to Palette's bean names
        HashMap<String, HashMap<String, NamedIcon>> iconMaps = new HashMap<>();
        for (String status : STATUSNAME) {
            PositionableIcon pos = (PositionableIcon)getStateData(status);
            HashMap<String, NamedIcon> map = new HashMap<>();
            Iterator<String> iter = pos.getIconStateNames();    // state names for this status name
            while (iter.hasNext()) {
                String state  = iter.next();
                NamedIcon oldIcon = pos.getIcon(state);
                NamedIcon newIcon = new NamedIcon(oldIcon);
                map.put(state, newIcon);
            }
            iconMaps.put(status, map);
        }
        _TOPanel.initUpdate(updateAction, iconMaps);
        _TOPanel.setSelection(getTurnout());
        if (namedOccSensor != null) {
            _TOPanel.setOccDetector(namedOccSensor.getBean().getDisplayName());
        }
        if (namedOccBlock != null) {
            _TOPanel.setOccDetector(namedOccBlock.getBean().getDisplayName());
        }
        _TOPanel.setShowTrainName(_pathUtil.showTrain());
        _TOPanel.setPaths(_pathUtil.getPaths());
        initPaletteFrame(_paletteFrame, _TOPanel);
    }

    @Override
    void updateItem() {
        if (log.isDebugEnabled()) {
            log.debug("updateItem: " + getNameString() + " family= " + _TOPanel.getFamilyName());
        }
        setTurnout(_TOPanel.getTableSelection().getSystemName());
        setOccSensor(_TOPanel.getOccSensor());
        setOccBlock(_TOPanel.getOccBlock());
        _pathUtil.setShowTrain(_TOPanel.getShowTrainName());
        setFamily(_TOPanel.getFamilyName());
        _pathUtil.setPaths(_TOPanel.getPaths());
        HashMap<String, HashMap<String, NamedIcon>> iconMap = _TOPanel.getIconMaps();
        if (iconMap != null) {
            Iterator<Entry<String, HashMap<String, NamedIcon>>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, HashMap<String, NamedIcon>> entry = it.next();
                String status = entry.getKey();
                PositionableIcon pos = (PositionableIcon)getStateData(status);
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    if (log.isDebugEnabled()) {
                        log.debug("key= " + ent.getKey());
                    }
                    NamedIcon icon = new NamedIcon(ent.getValue());
                    pos.setStateIcon(ent.getKey(), icon);
                }
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _TOPanel);
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

    @Override
    public void paintComponent(Graphics g) {

        long time = 0;
        if (System.currentTimeMillis() - time > 1000) {
            System.out.println("Paint "+getClass().getName()+", _status= "+_status);
            time = System.currentTimeMillis();
        }
       PositionableIcon turnoutIcon = (PositionableIcon)getStateData(_status);
       turnoutIcon.paintComponent(g);
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIcon.class);
}
