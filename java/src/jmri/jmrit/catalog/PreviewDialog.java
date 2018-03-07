package jmri.jmrit.catalog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Dialog to display the images in a file system directory.
 * <p>
 * PreviewDialog is not modal to allow dragNdrop of icons from it to catalog
 * panels and functioning of the catalog panels without dismissing this dialog.
 * Component is used in {@link jmri.jmrit.catalog.DirectorySearcher}, accessed
 * from {@link jmri.jmrit.catalog.ImageIndexEditor} File menu items.
 *
 * @author Pete Cressman Copyright 2009
 * @author Egbert Broerse Copyright 2017
 */
public class PreviewDialog extends JDialog {

    JPanel _selectedImage;
    static Color _grayColor = new Color(235, 235, 235);
    static Color _darkGrayColor = new Color(150, 150, 150);
    protected Color[] colorChoice = new Color[]{Color.white, _grayColor, _darkGrayColor};
    /**
     * Active base color for Preview background, copied from active Panel where
     * available.
     */
    protected BufferedImage[] _backgrounds;

    JLabel _previewLabel = new JLabel();
    protected ImagePanel _preview;
    protected JScrollPane js;

    int _cnt;           // number of files displayed when setIcons() method runs
    int _startNum;      // total number of files displayed from a directory
    boolean needsMore = true;

    File _currentDir;   // current FS directory
    String[] _filter;   // file extensions of types to display
    ActionListener _lookAction;
    boolean _mode;

    /**
    *
    * @param frame  JFrame on screen to center this dialog over
    * @param title  title for the frame
    * @param dir    starting icon file directory
    * @param filter file patterns to display in icon tree
    * @param modality mode
    */
    protected PreviewDialog(Frame frame, String title, File dir, String[] filter, boolean modality) {
        super(frame, Bundle.getMessage(title), modality);
        _currentDir = dir;
        _filter = new String[filter.length];
        for (int i = 0; i < filter.length; i++) {
            _filter[i] = filter[i];
        }
        _mode = modality;
    }

    protected void init(ActionListener moreAction, ActionListener lookAction, ActionListener cancelAction, int startNum) {
        if (log.isDebugEnabled()) {
            log.debug("Enter _previewDialog.init dir= {}", _currentDir.getPath());
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                InstanceManager.getDefault(DirectorySearcher.class).close();
                dispose();
            }
        });
        JPanel pTop = new JPanel();
        pTop.setLayout(new BoxLayout(pTop, BoxLayout.Y_AXIS));
        pTop.add(new JLabel(_currentDir.getPath()));
        JTextField msg = new JTextField();
        msg.setFont(new Font("Dialog", Font.BOLD, 12));
        msg.setEditable(false);
        msg.setBackground(pTop.getBackground());
        pTop.add(msg);
        getContentPane().add(pTop, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(5));

        JPanel previewPanel = setupPanel(); // provide panel for images, add to bottom of window
        _startNum = startNum;
        needsMore = setIcons(startNum);
        if (_noMemory) {
            int choice = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("OutOfMemory", _cnt), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new String[]{Bundle.getMessage("ButtonStop"), Bundle.getMessage("ShowContents")}, 1);
            if (choice == 0) {
                return;
            }
        }

        if (needsMore) {
            if (moreAction != null) {
                p.add(Box.createHorizontalStrut(5));
                JButton moreButton = new JButton(Bundle.getMessage("ButtonDisplayMore"));
                moreButton.addActionListener(moreAction);
                moreButton.setVisible(needsMore);
                p.add(moreButton);
            } else {
                log.error("More ActionListener missing");
            }
            msg.setText(Bundle.getMessage("moreMsg", Bundle.getMessage("ButtonDisplayMore")));
        }

        boolean hasButtons = needsMore;
        msg.setText(Bundle.getMessage("dragMsg"));

        _lookAction = lookAction;
        if (lookAction != null) {
            p.add(Box.createHorizontalStrut(5));
            JButton lookButton = new JButton(Bundle.getMessage("ButtonKeepLooking"));
            lookButton.addActionListener(lookAction);
            p.add(lookButton);
            hasButtons = true;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (hasButtons) {
            p.add(Box.createHorizontalStrut(5));
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener(cancelAction);
            p.add(cancelButton);
            p.add(Box.createHorizontalStrut(5));
            p.setPreferredSize(new Dimension(400, cancelButton.getPreferredSize().height));
            panel.add(p);
            panel.add(new JSeparator());
        }

        panel.add(previewPanel);
        getContentPane().add(panel);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    ActionListener getLookActionListener() {
        return _lookAction;
    }

    /**
     * Set up a display panel to display icons.
     * Includes a "View on:" drop down list.
     * Employs a normal JComboBox, no Panel Background option.
     * @see jmri.jmrit.catalog.CatalogPanel#makeButtonPanel()
     *
     * @return a JPanel with preview pane and background color drop down
     */
    private JPanel setupPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(_previewLabel);
        previewPanel.add(p);
        _preview = new ImagePanel();
        log.debug("Preview ImagePanel created");
        _preview.setLayout(new BoxLayout(_preview, BoxLayout.Y_AXIS));
        _preview.setOpaque(false);
        js = new JScrollPane(_preview);
        previewPanel.add(js);
        // create array of backgrounds
        if (_backgrounds == null) {
            _backgrounds = new BufferedImage[4];
            for (int i = 0; i <= 2; i++) {
                _backgrounds[i] = DrawSquares.getImage(300, 400, 10, colorChoice[i], colorChoice[i]);
            _backgrounds[3] = DrawSquares.getImage(300, 400, 10, Color.white, _grayColor);
            }
        }
        // create background selection combo box
        JComboBox<String> bgColorBox = new JComboBox<>();
        bgColorBox.addItem(Bundle.getMessage("White"));
        bgColorBox.addItem(Bundle.getMessage("LightGray"));
        bgColorBox.addItem(Bundle.getMessage("DarkGray"));
        bgColorBox.addItem(Bundle.getMessage("Checkers")); // checkers option, under development
        bgColorBox.setSelectedIndex(0); // white
        bgColorBox.addActionListener((ActionEvent e) -> {
            // load background image
            _preview.setImage(_backgrounds[bgColorBox.getSelectedIndex()]);
            log.debug("Preview setImage called");
            _preview.setOpaque(false);
            // _preview.repaint(); // force redraw
            _preview.invalidate();
        });

        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        pp.add(bgColorBox);
        previewPanel.add(pp);

        return previewPanel;
    }

    void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        log.debug("resetPanel");
        _preview.removeAll();
        _preview.setImage(_backgrounds[0]);
        _preview.invalidate();
        pack();
    }

    protected int getNumFilesShown() {
        return _startNum + _cnt;
    }

    class MemoryExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            _noMemory = true;
            log.error("MemoryExceptionHandler: {} {} files read from directory {}", e, _cnt, _currentDir);
            if (log.isDebugEnabled()) {
                log.debug("memoryAvailable = {}", availableMemory());
            }
        }
    }

    boolean _noMemory = false;

    /**
     * Display (thumbnails if image is large) of the current directory. Number
     * of images displayed may be restricted due to memory constraints.
     *
     * @return true if memory limits displaying all the images
     */
    private boolean setIcons(int startNum) throws OutOfMemoryError {
        // VM launches another thread to run ImageFetcher.
        // This handler will catch memory exceptions from that thread
        _noMemory = false;
        Thread.setDefaultUncaughtExceptionHandler(new MemoryExceptionHandler());
        int numCol = 6;
        int numRow = 5;
        long memoryNeeded = 0;
        // allow room for ImageFetcher threads
        long memoryAvailable = availableMemory() - 10000000;
        if (log.isDebugEnabled()) {
            log.debug("setIcons: startNum= " + startNum + " memoryAvailable = " + availableMemory());
        }
        boolean newCol = false;
        GridBagLayout gridbag = new GridBagLayout();
        _preview.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = -1;
        c.gridx = 0;
        _cnt = 0;       // number of images displayed in this panel
        int cnt = 0;    // total number of images in directory
        File[] files = _currentDir.listFiles(); // all files, filtered below
        if (files != null) { // prevent spotbugs NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
            int nCols = 1;
            int nRows = 1;
            int nAvail = 1;

            for (int i = 0; i < files.length; i++) {
                String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
                for (int k = 0; k < _filter.length; k++) {
                    if (ext != null && ext.equalsIgnoreCase(_filter[k])) {
                        // files[i] filtered to be an image file
                        if (cnt < startNum) {
                            cnt++;
                            continue;
                        }
                        String name = files[i].getName();
                        int index = name.indexOf('.');
                        if (index > 0) {
                            name = name.substring(0, index);
                        }
                        String path = files[i].getAbsolutePath();
                        NamedIcon icon = new NamedIcon(path, name);
                        try {
                        memoryNeeded += 3 * icon.getIconWidth() * icon.getIconHeight();
                        if (memoryAvailable < memoryNeeded) {
                            _noMemory = true;
                            continue;
                        }
                        if (_noMemory) {
                            continue;
                        }
                        if (c.gridx < numCol) {
                            c.gridx++;
                        } else if (c.gridy < numRow) { //start next row
                            c.gridy++;
                            if (!newCol) {
                                c.gridx = 0;
                            }
                        } else if (!newCol) { // start new column
                            c.gridx++;
                            numCol++;
                            c.gridy = 0;
                            newCol = true;
                        } else {  // start new row
                            c.gridy++;
                            numRow++;
                            c.gridx = 0;
                            newCol = false;
                        }
                        c.insets = new Insets(5, 5, 0, 0);
                        PositionableLabel image;
                        if (_mode) {
                            image = new PositionableLabel(icon, null);
                        } else {
                            //modeless is for ImageEditor dragging
                            try {
                                image = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime), icon);
                                image.setIsIcon(true);
                            } catch (java.lang.ClassNotFoundException cnfe) {
                                cnfe.printStackTrace();
                                image = new PositionableLabel(icon, null);
                            }
                        }
                        image.setOpaque(false);
                        image.setToolTipText(name);
                        image.setName(name);
                        double scale = image.reduceTo(CatalogPanel.ICON_WIDTH,
                                CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
                        PositionablePopupUtil util = image.getPopupUtility();
                        JPanel p = new JPanel();
                        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                        p.add(image);
                        if (name.length() > 18) {
                            name = name.substring(0, 18);
                        }
                        JLabel nameLabel = new JLabel(name);
                        JLabel slabel = new JLabel(java.text.MessageFormat.format(Bundle.getMessage("scale"),
                                new Object[]{CatalogPanel.printDbl(scale, 2)}));
                        p.add(slabel);
                        JLabel label = new JLabel(java.text.MessageFormat.format(Bundle.getMessage("dimension"),
                                icon.getIconWidth(), icon.getIconHeight()));
                        p.add(nameLabel);
                        p.add(label);
                        gridbag.setConstraints(p, c);
                        if (_noMemory) {
                            continue;
                        }
                        if (log.isDebugEnabled()) {
                            log.debug(name + " inserted at (" + c.gridx + ", " + c.gridy + ")");
                        }
                        _preview.add(p);
                        _cnt++;
                        cnt++;
                        if (_cnt > 300) { // somewhere above this number, VM can't build display of panel
                            _noMemory = true;
                        }
                    } catch (OutOfMemoryError oome) {
                        JOptionPane.showMessageDialog(this,
                                java.text.MessageFormat.format(Bundle.getMessage("OutOfMemory"),
                                        new Object[]{Integer.valueOf(_cnt)}),
                                Bundle.getMessage("error"), JOptionPane.INFORMATION_MESSAGE);
                        _noMemory = true;
                    }
                }
            }
            c.gridy++;
            c.gridx++;
        }
        }
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);
        String msg = java.text.MessageFormat.format(Bundle.getMessage("numImagesInDir"),
                new Object[]{_currentDir.getName(), Integer.valueOf(cnt)});
        if (startNum > 0) {
            msg = msg + " " + java.text.MessageFormat.format(Bundle.getMessage("numImagesShown"),
                    new Object[]{Integer.valueOf(startNum)});
        }
        _previewLabel.setText(msg);
//        _preview.setMinimumSize(new Dimension(CatalogPanel.ICON_WIDTH, 2 * CatalogPanel.ICON_HEIGHT));
        CatalogPanel.packParentFrame(this);

        if (_noMemory) {
            JOptionPane.showMessageDialog(this,
                    java.text.MessageFormat.format(Bundle.getMessage("OutOfMemory"),
                            new Object[]{Integer.valueOf(_cnt)}),
                    Bundle.getMessage("error"), JOptionPane.INFORMATION_MESSAGE);
        }
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
        return _noMemory;
    }

    static int CHUNK = 500000;

    private long availableMemory() {
        long total = 0;
        ArrayList<byte[]> memoryTest = new ArrayList<byte[]>();
        try {
            while (true) {
                memoryTest.add(new byte[CHUNK]);
                total += CHUNK;
            }
        } catch (OutOfMemoryError me) {
            for (int i = 0; i < memoryTest.size(); i++) {
                memoryTest.remove(i);
            }
            if (log.isDebugEnabled()) {
                log.debug("availableMemory= {}", total);
            }
        }
        return total;
    }

    public void paintComponents(Graphics g) {
        super.paintComponents(g);
    }
    
    public void dispose() {
        if (_preview != null) {
            resetPanel();
        }
        this.removeAll();
        _preview = null;
        super.dispose();
        log.debug("PreviewDialog disposed.");
    }

    private final static Logger log = LoggerFactory.getLogger(PreviewDialog.class);

}
