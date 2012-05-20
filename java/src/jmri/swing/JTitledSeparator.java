/* JTitledSeparator based on
 * https://github.com/rhwood/DJ-Swing-Suite/blob/master/DJSwingSuite/src/chrriis/dj/swingsuite/JTitledSeparator.java
 * by
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 */
package jmri.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * A separator with a title.
 *
 * @author Randall Wood
 */
public class JTitledSeparator extends JPanel {

    private final static class SeparatorPane extends JPanel {

        private SeparatorPane() {
            super(new GridBagLayout());
            setOpaque(false);
            setDoubleBuffered(false);
            add(new JSeparator(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }

        @SuppressWarnings("deprecation")
        @Override
        public void reshape(int x, int y, int w, int h) {
            super.reshape(x, y, w, h);
            doLayout();
        }
    }
    private JLabel label = new JLabel();

    /**
     * Construct a separator with a title.
     *
     * @param title the title to set.
     */
    public JTitledSeparator(String title) {
        super(new BorderLayout());
        JPanel westPanel = new JPanel(new BorderLayout()) {

            @SuppressWarnings("deprecation")
            @Override
            public void reshape(int x, int y, int w, int h) {
                super.reshape(x, y, w, h);
                doLayout();
            }
        };
        westPanel.setOpaque(false);
        westPanel.setDoubleBuffered(false);
        boolean isLeftToRight = getComponentOrientation().isLeftToRight();
        setOpaque(false);
        westPanel.add(label, BorderLayout.CENTER);
        if (isLeftToRight) {
            add(westPanel, BorderLayout.WEST);
        } else {
            add(westPanel, BorderLayout.EAST);
        }
        SeparatorPane separatorPane = new SeparatorPane();
        if (isLeftToRight) {
            separatorPane.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
        } else {
            separatorPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
        }
        add(separatorPane, BorderLayout.CENTER);
        setTitle(title);
        adjustLook();
    }

    /**
     * Get the title of this separator.
     *
     * @return the title.
     */
    public String getTitle() {
        return label.getText();
    }

    /**
     * Set the title of the separator.
     *
     * @param title the new title.
     */
    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        boolean isVisible = title.length() != 0;
        label.setVisible(isVisible);
        label.setText(title);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        adjustLook();
    }

    private void adjustLook() {
        if (label != null) {
            Color titleColor = UIManager.getColor("TitledBorder.titleColor");
            Font font = UIManager.getFont("TitledBorder.font");
            if (titleColor == null || font == null) {
                TitledBorder titledBorder = new TitledBorder("");
                titleColor = titledBorder.getTitleColor();
                font = titledBorder.getTitleFont();
            }
            label.setForeground(titleColor);
            label.setFont(font);
        }
    }
}