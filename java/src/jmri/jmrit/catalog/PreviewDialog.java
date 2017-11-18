package jmri.jmrit.catalog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Dialog to display the images in a file system directory.
 * <BR>
 * PreviewDialog is not modal to allow dragNdrop of icons from it to catalog panels and
 * functioning of the catalog panels without dismissing this dialog
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Pete Cressman Copyright 2009
 *
 */
public class PreviewDialog extends JDialog {

    JPanel _selectedImage;
    static Color _grayColor = new Color(235, 235, 235);
    Color _currentBackground = _grayColor;

    JLabel _previewLabel = new JLabel();
    JPanel _preview;

    int _cnt;           // number of files displayed when setIcons() method runs
    int _startNum;      // total number of files displayed from a directory
    boolean needsMore = true;

    File _currentDir;   // current FS directory
    String[] _filter;   // file extensions of types to display
    ActionListener _lookAction;
    boolean _mode;

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

        JPanel previewPanel = setupPanel();     // provide panel for images, add to bottom of window
        _startNum = startNum;
        needsMore = setIcons(startNum);
        if (_noMemory) {
            int choice = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("OutOfMemory", _cnt), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new String[]{Bundle.getMessage("Quit"), Bundle.getMessage("ShowContents")}, 1);
            if (choice==0) {
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
            msg.setText(Bundle.getMessage("moreMsg"));
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
     * Setup a display panel to display icons
     */
    private JPanel setupPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(_previewLabel);
        previewPanel.add(p);
        _preview = new JPanel();
        JScrollPane js = new JScrollPane(_preview);
        previewPanel.add(js);
        JRadioButton whiteButton = new JRadioButton(Bundle.getMessage("white"), false);
        JRadioButton grayButton = new JRadioButton(Bundle.getMessage("lightGray"), true);
        JRadioButton darkButton = new JRadioButton(Bundle.getMessage("darkGray"), false);
        whiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _currentBackground =  Color.white;
                setBackGround(_preview, _currentBackground);
            }
        });
        grayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _currentBackground =  _grayColor;
                setBackGround(_preview, _currentBackground);
            }
        });
        darkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _currentBackground =  new Color(150, 150, 150);
                setBackGround(_preview, _currentBackground);
            }
        });
        JPanel pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        previewPanel.add(pp);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        ButtonGroup selGroup = new ButtonGroup();
        selGroup.add(whiteButton);
        selGroup.add(grayButton);
        selGroup.add(darkButton);
        panel.add(whiteButton);
        panel.add(grayButton);
        panel.add(darkButton);
        previewPanel.add(panel);
        return previewPanel;
    }

    static public void setBackGround(java.awt.Container panel, Color color) {
        panel.setBackground(color);
        Component[] component = panel.getComponents();
        for (int k = 0; k < component.length; k++) {
//            System.out.println("1. Class: "+component[k].getClass().getName());
            if (component[k] instanceof JPanel) {
                Component[] comp = ((JPanel)component[k]).getComponents();
                for (int i = 0; i < comp.length; i++) {
//                    System.out.println("  2. Class: "+comp[i].getClass().getName());
                    if (comp[i] instanceof PositionableLabel || comp[i] instanceof DragJLabel) {
                        PositionableLabel p = (PositionableLabel) comp[i];
                        PositionablePopupUtil util = p.getPopupUtility();
                        if (util!=null) {
                            util.setBackgroundColor(color);                
                        }
                    } else if (comp[i].getClass().getName().equals("javax.swing.JLabel")) {
                        JLabel l = (JLabel) comp[i];
                        l.setBackground(color);
                    } else {
                        comp[i].setBackground(color);
                    }
                    comp[i].invalidate();
                    comp[i].repaint();
                }
            }
            component[k].setBackground(color);
            component[k].invalidate();
        }
        panel.setBackground(color);
        panel.invalidate();
    }

    void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("resetPanel");
        }
        Component[] comp = _preview.getComponents();
        for (int i = comp.length - 1; i >= 0; i--) {
            _preview.remove(i);
            comp[i] = null;
        }
        _preview.removeAll();
        _preview.setBackground(_currentBackground);
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
     * Displays (thumbnails if image is large) of the current directory. Number
     * of images displayed may be restricted due to memory constraints. Returns
     * true if memory limits displaying all the images
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
        int nCols = 1;
        int nRows = 1;
        int nAvail = 1;

        long memoryUsed = 0;        // estmate
        for (int i = 0; i < files.length; i++) {
            String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
            for (int k = 0; k < _filter.length; k++) {
                if (ext != null && ext.equalsIgnoreCase(_filter[k])) {
                    if (cnt < startNum || _noMemory) {
                        cnt++;
                        continue;
                    }
                    String name = files[i].getName();
                    int index = name.indexOf('.');
                    if (index > 0) {
                        name = name.substring(0, index);
                    }
                    try {
                        String path = files[i].getAbsolutePath();
                        NamedIcon icon = new NamedIcon(path, name);
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
                        image.setToolTipText(name);
                        image.setName(name);
                        double scale = image.reduceTo(CatalogPanel.ICON_WIDTH,
                                CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
                        PositionablePopupUtil util = image.getPopupUtility();
                        if (util!=null) {
                            util.setBackgroundColor(_currentBackground);                
                        }
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
        }
        c.gridy++;
        c.gridx++;
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
            if (log.isDebugEnabled()) log.debug("availableMemory= {}", total);
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
