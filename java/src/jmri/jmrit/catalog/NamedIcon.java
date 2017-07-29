package jmri.jmrit.catalog;

import java.awt.Component;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend an ImageIcon to remember the name from which it was created and
 * provide rotation & scaling services.
 * <p>
 * We store both a "URL" for finding the file this was made from (so we can load
 * this later), plus a shorter "name" for display.
 * <p>
 * These can be persisted by storing their name and rotation
 *
 * @see jmri.jmrit.display.configurexml.PositionableLabelXml
 * @author Bob Jacobsen Copyright 2002, 2008
 * @author Pete Cressman Copyright: Copyright (c) 2009, 2010
 * @version $Revision$
 */
public class NamedIcon extends ImageIcon {
    /**
     * Create a NamedIcon that is a complete copy of an existing NamedIcon
     *
     * @param pOld Object to copy i.e. copy of the original icon, but NOT a
     *             complete copy of pOld (no transformations done)
     */
    public NamedIcon(NamedIcon pOld) {
        this(pOld.mURL, pOld.mName);
    }

    /**
     * Create a NamedIcon that is really a complete copy of an existing
     * NamedIcon
     *
     * @param pOld Object to copy
     */
    public NamedIcon(NamedIcon pOld, Component comp) {
        this(pOld.mURL, pOld.mName);
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     * <p>
     * The default access form is "file:", so a bare pathname to an icon file
     * will also work for the URL argument
     *
     * @param pUrl  URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(String pUrl, String pName) {
        super(FileUtil.findURL(pUrl));
        URL u = FileUtil.findURL(pUrl);
        if (u == null) {
            log.warn("Could not load image from " + pUrl + " (file does not exist)");
        }
        if (getImage() == null) {
            log.warn("Could not load image from " + pUrl + " (image is null)");
        }
        mName = pName;
        mURL = FileUtil.getPortableFilename(pUrl);
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     *
     * @param pUrl  String-form URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(URL pUrl, String pName) {
        this(pUrl.toString(), pName);
    }

    public NamedIcon(Image im) {
        super(im);
    }

    /**
     * Find the NamedIcon corresponding to a name. Understands the
     * <a href="http://jmri.org/help/en/html/doc/Technical/FileNames.shtml">standard
     * portable filename prefixes</a>.
     *
     * @param pName The name string, possibly starting with file: or resource:
     * @return the desired icon with this same pName as its name.
     */
    static public NamedIcon getIconByName(String pName) {
        if (pName == null || pName.length() == 0) {
            return null;
        }
        URL u = FileUtil.findURL(pName);
        if (u == null) {
            return null;
        }
        return new NamedIcon(pName, pName);
    }

    /**
     * Return the human-readable name of this icon
     */
    public String getName() {
        return mName;
    }

    /**
     * Actually it is mName that is the URL that loads the icon!
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Return the URL of this icon
     */
    public String getURL() {
        return mURL;
    }

    /**
     * Set URL of original icon image
     */
    public void setURL(String url) {
        mURL = url;
    }

    private String mName = null;
    private String mURL = null;
    
    private final static Logger log = LoggerFactory.getLogger(NamedIcon.class.getName());
}
