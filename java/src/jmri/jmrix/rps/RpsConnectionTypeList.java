package jmri.jmrix.rps;

/**
 * Returns a list of valid lenz XPressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
public class RpsConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String NAC = "NAC Services";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.rps.serial.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{NAC};
    }

}
