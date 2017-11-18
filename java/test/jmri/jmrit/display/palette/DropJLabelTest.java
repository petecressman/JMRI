package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DropJLabelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NamedIcon i = new NamedIcon("program:resources/logo.gif","logo");
        HashMap<String, NamedIcon> map = new HashMap();
        DropJLabel t = new DropJLabel(i, map, false);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DropJLabelTest.class);

}
