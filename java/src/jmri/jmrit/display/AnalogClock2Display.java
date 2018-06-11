package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Analog Clock for displaying in a panel
 * <p>
 * Time code copied in part from code for the Nixie clock by Bob Jacobsen </p>
 *
 * @author Howard G. Penny - Copyright (C) 2005
 */
public class AnalogClock2Display extends Positionable implements LinkingObject {

    Timebase clock;
    double rate;
    double minuteAngle;
    double hourAngle;
    String amPm;
    Color color = Color.black;

    // Define common variables
    NamedIcon jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
    NamedIcon clockIcon = new NamedIcon("resources/clock2.gif", "resources/clock2.gif");

    int hourX[] = {
        -12, -11, -25, -10, -10, 0, 10, 10, 25, 11, 12};
    int hourY[] = {
        -31, -163, -170, -211, -276, -285, -276, -211, -170, -163, -31};
    int minuteX[] = {
        -12, -11, -24, -11, -11, 0, 11, 11, 24, 11, 12};
    int minuteY[] = {
        -31, -261, -266, -314, -381, -391, -381, -314, -266, -261, -31};
    int scaledHourX[] = new int[hourX.length];
    int scaledHourY[] = new int[hourY.length];
    int scaledMinuteX[] = new int[minuteX.length];
    int scaledMinuteY[] = new int[minuteY.length];
    int rotatedHourX[] = new int[hourX.length];
    int rotatedHourY[] = new int[hourY.length];
    int rotatedMinuteX[] = new int[minuteX.length];
    int rotatedMinuteY[] = new int[minuteY.length];

    Polygon scaledHourHand;
    Polygon scaledMinuteHand;

    String _url;

    public AnalogClock2Display(Editor editor) {
        super(editor);
        clock = InstanceManager.getDefault(jmri.Timebase.class);

        rate = (int) clock.userGetRate();

        init();
        updateSize();
    }

    public AnalogClock2Display(Editor editor, String url) {
        this(editor);
        _url = url;
    }

    @Override
    public Positionable deepClone() {
        AnalogClock2Display pos;
        if (_url == null || _url.trim().length() == 0) {
            pos = new AnalogClock2Display(_editor);
        } else {
            pos = new AnalogClock2Display(_editor, _url);
        }
        return finishClone(pos);
    }

    protected Positionable finishClone(AnalogClock2Display pos) {
        return super.finishClone(pos);
    }

    @Override
    public int getWidth() {
        return clockIcon.getIconWidth();
    }

    @Override
    public int getHeight() {
        return clockIcon.getIconHeight();
    }

    final void init() {
        // Create an unscaled set of hands to get the original size (height)to use
        // in the scaling calculations
        Polygon hourHand = new Polygon(hourX, hourY, 11);
        Polygon minuteHand = new Polygon(minuteX, minuteY, 11);
        int minuteHeight = minuteHand.getBounds().getSize().height;

        amPm = "AM";

        // request callback to update time
        clock.addMinuteChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });
        // request callback to update changes in properties
        clock.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });

        double scaleRatio = getWidth() / 2.7 / minuteHeight;
        for (int i = 0; i < minuteX.length; i++) {
            scaledMinuteX[i] = (int) (minuteX[i] * scaleRatio);
            scaledMinuteY[i] = (int) (minuteY[i] * scaleRatio);
            scaledHourX[i] = (int) (hourX[i] * scaleRatio);
            scaledHourY[i] = (int) (hourY[i] * scaleRatio);
        }
        scaledHourHand = new Polygon(scaledHourX, scaledHourY,
                scaledHourX.length);
        scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY,
                scaledMinuteX.length);
    }

    ButtonGroup colorButtonGroup = null;
    ButtonGroup rateButtonGroup = null;
    JMenuItem runMenu = null;

    @Override
    public boolean setScaleMenu(JPopupMenu popup) {

        popup.add(new JMenuItem(Bundle.getMessage("FastClock")));
        JMenu rateMenu = new JMenu("Clock rate");
        rateButtonGroup = new ButtonGroup();
        addRateMenuEntry(rateMenu, 1);
        addRateMenuEntry(rateMenu, 2);
        addRateMenuEntry(rateMenu, 4);
        addRateMenuEntry(rateMenu, 8);
        popup.add(rateMenu);
        runMenu = new JMenuItem(getRun() ? "Stop" : "Start");
        runMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRun(!getRun());
                update();
            }
        });
        popup.add(runMenu);
        popup.add(CoordinateEdit.getScaleEditAction(this));
        popup.addSeparator();
        JMenuItem colorMenuItem = new JMenuItem(Bundle.getMessage("Color"));
        colorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("DefaultTextColor", ""),
                                 color);
            if (desiredColor!=null && !color.equals(desiredColor)) {
               setColor(desiredColor);
           }
        });
        popup.add(colorMenuItem);

        return true;
    }

    @Override
    public String getNameString() {
        return "Clock";
    }

    void addRateMenuEntry(JMenu menu, final int newrate) {
        JRadioButtonMenuItem button = new JRadioButtonMenuItem("" + newrate + ":1");
        button.addActionListener(new ActionListener() {
            final int r = newrate;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clock.userSetRate(r);
                    rate = r;
                } catch (TimebaseRateException t) {
                    log.error("TimebaseRateException for rate= " + r + ". " + t);
                }
            }
        });
        rateButtonGroup.add(button);
        if (rate == newrate) {
            button.setSelected(true);
        } else {
            button.setSelected(false);
        }
        menu.add(button);
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        update();
    }

    // Method to convert degrees to radians
    // Math.toRadians was not available until Java 1.2
    double toRadians(double degrees) {
        return degrees / 180.0 * Math.PI;
    }


    @SuppressWarnings("deprecation")
    public void update() {
        Date now = clock.getTime();
        if (runMenu != null) {
            runMenu.setText(getRun() ? "Stop" : "Start");
        }
        int hours = now.getHours();
        int minutes = now.getMinutes();
        minuteAngle = minutes * 6.;
        hourAngle = hours * 30. + 30. * minuteAngle / 360.;
        if (hours < 12) {
            amPm = "AM " + (int) clock.userGetRate() + ":1";
        } else {
            amPm = "PM " + (int) clock.userGetRate() + ":1";
        }
        if (hours == 12 && minutes == 0) {
            amPm = "Noon";
        }
        if (hours == 0 && minutes == 0) {
            amPm = "Midnight";
        }
        repaint();
    }

    public boolean getRun() {
        return clock.getRun();
    }

    public void setRun(boolean next) {
        clock.setRun(next);
    }

    @Override
    public String getURL() {
        return _url;
    }

    @Override
    public void setULRL(String u) {
        _url = u;
    }

    @Override
    public boolean setLinkMenu(JPopupMenu popup) {
        if (_url == null || _url.trim().length() == 0) {
            return false;
        }
        popup.add(CoordinateEdit.getLinkEditAction(this, "EditLink"));
        return true;
    }

    @Override
    public void doMouseClicked(MouseEvent event) {
        log.debug("click to " + _url);
        if (_url == null || _url.trim().length() == 0) {
            return;
        }
        try {
            if (_url.startsWith("frame:")) {
                // locate JmriJFrame and push to front
                String frame = _url.substring(6);
                final jmri.util.JmriJFrame jframe = jmri.util.JmriJFrame.getFrame(frame);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jframe.toFront();
                        jframe.repaint();
                    }
                });
            } else {
                jmri.util.ExternalLinkContentViewerUI.activateURL(new java.net.URL(_url));
            }
        } catch (IOException t) {
            log.error("Error handling link", t);
        } catch (URISyntaxException t) {
            log.error("Error handling link", t);
        }
        super.doMouseClicked(event);
    }
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.transform(getTransform());
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setClip(null);
        super.paint(g2d);
        
        int iconWidth = clockIcon.getIconWidth();
        
        clockIcon.paintIcon(this, g2d, 0, 0);

        float logoScale = .17f;
        AffineTransform logo = AffineTransform.getScaleInstance(logoScale, logoScale);
        logo.concatenate(getTransform());
        Graphics2D gg = (Graphics2D)g.create();
        gg.transform(logo);
        gg.setClip(null);
        int logoWidth = jmriIcon.getIconWidth();
        jmriIcon.paintIcon(this, gg, Math.round((iconWidth/logoScale-logoWidth)/2), 
                     Math.round(iconWidth/logoScale/4));            
        
        // Draw hour hand rotated to appropriate angle
        // Calculation mimics the AffineTransform class calculations in Graphics2D
        // Grpahics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
        for (int i = 0; i < scaledMinuteX.length; i++) {
            rotatedMinuteX[i] = (int) (scaledMinuteX[i] * Math.cos(toRadians(minuteAngle))
                    - scaledMinuteY[i] * Math.sin(toRadians(minuteAngle)) + iconWidth/2);
            rotatedMinuteY[i] = (int) (scaledMinuteX[i] * Math.sin(toRadians(minuteAngle))
                    + scaledMinuteY[i] * Math.cos(toRadians(minuteAngle)) + iconWidth/2);
        }
        scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY, rotatedMinuteX.length);
        for (int i = 0; i < scaledHourX.length; i++) {
            rotatedHourX[i] = (int) (scaledHourX[i] * Math.cos(toRadians(hourAngle))
                    - scaledHourY[i] * Math.sin(toRadians(hourAngle)) + iconWidth/2);
            rotatedHourY[i] = (int) (scaledHourX[i] * Math.sin(toRadians(hourAngle))
                    + scaledHourY[i] * Math.cos(toRadians(hourAngle)) + iconWidth/2);
        }
        scaledHourHand = new Polygon(rotatedHourX, rotatedHourY, rotatedHourX.length);

        g2d.fillPolygon(scaledHourHand);
        g2d.fillPolygon(scaledMinuteHand);

        // Draw AM/PM indicator in slightly smaller font than hour digits
        int amPmFontSize = (int) (iconWidth * .075);
        if (amPmFontSize < 1) {
            amPmFontSize = 1;
        }
        Font amPmSizedFont = new Font("Serif", Font.BOLD, amPmFontSize);
        g2d.setFont(amPmSizedFont);
        FontMetrics amPmFontM = g2d.getFontMetrics(amPmSizedFont);

        g2d.drawString(amPm, (iconWidth-amPmFontM.stringWidth(amPm))/2, (iconWidth*7)/10);
        
       g2d.dispose();
    }
    private static final Logger log = LoggerFactory.getLogger(AnalogClock2Display.class);
}
