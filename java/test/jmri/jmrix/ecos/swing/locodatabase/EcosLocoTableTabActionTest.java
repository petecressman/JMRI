package jmri.jmrix.ecos.swing.locodatabase;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosLocoTableTabActionTest {

    @Test
    public void testCTor() {
        EcosLocoTableTabAction t = new EcosLocoTableTabAction();
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosLocoTableTabActionTest.class);

}
