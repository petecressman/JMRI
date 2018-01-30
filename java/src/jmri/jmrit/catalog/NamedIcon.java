package jmri.jmrit.catalog;

import java.awt.Component;
import java.awt.Image;
import java.net.URL;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend an ImageIcon to remember the name from which it was created
 * Since Aug 2017 all transformations are done in PaintComponent
 * <p>
 * We store both a "URL" for finding the file this was made from (so we can load
 * this later), plus a shorter "name" for display.
 * <p>
 * These can be persisted by storing their name
 *
 * @see jmri.jmrit.display.configurexml.PositionableLabelXml
 * @author Bob Jacobsen Copyright 2002, 2008
 * @author Pete Cressman Copyright: Copyright (c) 2009, 2010, 2017
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
     * @param comp the container the new icon is embedded in
     */
    public NamedIcon(NamedIcon pOld, Component comp) {
        this(pOld.mURL, pOld.mName);
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     * <p>
     * The default access form is "file:", so a bare pathname to an icon file
     * will also work for the URL argument.
     *
     * @param pUrl  URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIcon(String pUrl, String pName) {
        super(FileUtil.findURL(pUrl));
        URL u = FileUtil.findURL(pUrl);
        if (u == null) {
            log.warn("Could not load image from {} (file does not exist)", pUrl);
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
     * Find the NamedIcon corresponding to a file path. Understands the
     * <a href="http://jmri.org/help/en/html/doc/Technical/FileNames.shtml">standard
     * portable filename prefixes</a>.
     *
     * @param path The path to the file, either absolute or portable
     * @return the desired icon with this same name as its path
     */
    static public NamedIcon getIconByName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (FileUtil.findURL(path) == null) {
            return null;
        }
        return new NamedIcon(path, path);
    }

    /**
     * Return the human-readable name of this icon.
     *
     * @return the name or null if not set
     */
    @CheckForNull
    public String getName() {
        return mName;
    }

    /**
     * Set the human-readable name for this icon.
     *
     * @param name the new name, can be null
     */
    public void setName(@Nullable String name) {
        mName = name;
    }

    /**
     * Get the URL of this icon.
     *
     * @return the path to this icon in JMRI portable format or null if not set
     */
    @CheckForNull
    public String getURL() {
        return mURL;
    }

    /**
     * Set URL of original icon image. Setting this after initial construction
     * does not change the icon.
     *
     * @param url the URL associated with this icon
     */
    public void setURL(@Nullable String url) {
        mURL = url;
    }

    private String mName = null;
    private String mURL = null;
    private final static Logger log = LoggerFactory.getLogger(NamedIcon.class);

}
