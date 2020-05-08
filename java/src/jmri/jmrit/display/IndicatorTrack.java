package jmri.jmrit.display;

import java.util.ArrayList;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.logix.OBlock;

/**
 * Defines track objects that display status color.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public interface IndicatorTrack{

    void setOccSensor(String pName);

    void setOccSensorHandle(NamedBeanHandle<Sensor> senHandle);

    Sensor getOccSensor();

    NamedBeanHandle<Sensor> getNamedOccSensor();

    void setOccBlock(String pName);

    void setOccBlockHandle(NamedBeanHandle<OBlock> blockHandle);

    OBlock getOccBlock();

    NamedBeanHandle<OBlock> getNamedOccBlock();

    void setShowTrain(boolean set);

    boolean showTrain();

    ArrayList<String> getPaths();

    void addPath(String path);

    void removePath(String path);

    public void setStatus(int state);

    public void setControlling(boolean enabled);

    public boolean isControlling();

}
