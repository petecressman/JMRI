package jmri.jmrit.swing.meter;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jmri.*;

/**
 * Swing action to create and register a AmpMeterFrame object.
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2007
 *
 * This was a direct steal form the LCDClock code by Ken Cameron,
 * which was a direct steal from the Nixie clock code, ver 1.5. 
 * Thank you Bob Jacobsen and Ken Cameron.
 */
public class MeterAction extends AbstractAction {

    public MeterAction() {
        this(Bundle.getMessage("MenuItemMeter"));
    }

    public MeterAction(String s) {
        super(s);
/*        
        // disable ourself if no Meter is available
        if (!hasMeters()) {
            setEnabled(false);
        }
*/        
    }
/*    
    private boolean hasMeters() {
        MeterManager mm = InstanceManager.getNullableDefault(MeterManager.class);
        if (mm == null) return false;
        
        for (Meter m : mm.getNamedBeanSet()) {
            if ((m != null) && (m instanceof VoltageMeter)) return true;
            if ((m != null) && (m instanceof CurrentMeter)) return true;
        }
        return false;
    }
*/
    @Override
    public void actionPerformed(ActionEvent e) {
        MeterFrame f = new MeterFrame();
        f.initComponents();
        f.setVisible(true);
    }

}
