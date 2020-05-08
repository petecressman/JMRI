package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.InstanceManager;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.palette.DecoratorPanel.AJSpinner;
import jmri.util.swing.JmriColorChooser;

/**
 * A dialog to color a Component in a Window.  The ColorChooser
 * interactively modifies the color of the component on the window
 * until the user either cancels or decides to keep the changes.
 *
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class ColorDialog extends JDialog implements ChangeListener, FontPanelListener {

    public static final int STRUT = 6;

    public static final int ONLY = 0;
    public static final int BORDER = DecoratorPanel.BORDER; // (= 1)
    public static final int MARGIN = DecoratorPanel.MARGIN; // (= 2)
    public static final int FWIDTH = DecoratorPanel.FWIDTH; // (= 3)
    public static final int FHEIGHT = DecoratorPanel.FHEIGHT;   // (= 4)
    public static final int FONT = 5;
    public static final int TEXT = 6;

        JColorChooser _chooser;
        JComponent _target;
        int _type;
        Color _saveColor;
        boolean _saveOpaque;
        String _saveText;
        Positionable _pos;
        Positionable _savePos;
        ActionListener _colorAction;
        JPanel _preview;

        /**
         * 
         * @param client Window holding the component
         * @param t target whose color may be changed
         * @param type which attribute is being changed
         * @param ca callback to tell client the component's color was changed. 
         * May be null if client doesen't care.
         */
        public ColorDialog(Frame client, JComponent t, int type, ActionListener ca) {
            super(client, true);
            _target = t;
            _type = type;
            if (t instanceof Positionable) {
                _pos = (Positionable)t;
                _savePos = _pos.deepClone();
                _pos.setSuppressRecentColor(true);
           } else {
                _pos = null;
            }
            _saveOpaque = t.isOpaque();
            _colorAction = ca;

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createVerticalGlue());

            String title;
            switch (type) {
                case ONLY:
                    title = "PanelColor";
                    _saveColor = t.getBackground();
                    break;
                case BORDER:
                    title = "SetBorderSizeColor";
                    _saveColor = _pos.getBorderColor();
                    JPanel p = makePanel(makeColorPanel(BORDER));
                    panel.add(p);
                    break;
                case MARGIN:
                    title = "SetMarginSizeColor";
                    _saveColor = _pos.getBackground();
                    p = makePanel(makeColorPanel(MARGIN));
                    panel.add(p);
                    break;
                case FONT:
                    title = "SetFontSizeColor";
                    _saveColor = _pos.getForeground();
                    FontPanel fontPanel = new FontPanel(this);
                    fontPanel.setFontSelections(_pos.getFont());
                    panel.add(makePanel(fontPanel));
                    break;
                case TEXT:
                    title = "SetTextSizeColor";
                    _saveColor = _pos.getBackground();
                    _saveText = ((PositionableLabel)t).getText();
                    JPanel spins = makeTextSpinnerPanel();
                    final FontPanel.AJComboBox _fontJustBox = new FontPanel.AJComboBox(DecoratorPanel.JUSTIFICATION, 0);
                    DecoratorPanel.setJustificationIndex(_pos, _fontJustBox);
                   ItemListener listen = new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            int just;
                            switch (_fontJustBox.getSelectedIndex()) {
                                case 0:
                                    just = Positionable.LEFT;
                                    break;
                                case 1:
                                    just = Positionable.CENTRE;
                                    break;
                                case 2:
                                    just = Positionable.RIGHT;
                                    break;
                                default:
                                    just = Positionable.CENTRE;
                                    break;
                            }
                            _pos.setJustification(just);
                        }
                    };
                    spins.add(Box.createHorizontalStrut(STRUT));
                    spins.add(FontPanel.makeBoxPanel("Justification", _fontJustBox, listen)); // NOI18N
                    panel.add(makePanel(spins));
                    panel.add(Box.createVerticalGlue());
                    panel.add(makePanel(makeTextPanel()));
                    break;
                default:
                    title = "ColorChooser";
                    _saveColor = t.getBackground();
            }
            panel.add(Box.createVerticalStrut(STRUT));
            setTitle(Bundle.getMessage(title));

            Color c = _saveColor;
            if (c == null) {
                c = Color.black;
            }
            _chooser = JmriColorChooser.extendColorChooser(new JColorChooser(c));
            _chooser.getSelectionModel().addChangeListener(this);
            _chooser.setPreviewPanel(new JPanel());
            JmriColorChooser.suppressAddRecentColor(true);
            panel.add(_chooser);
            panel.add(Box.createVerticalStrut(STRUT));

            panel.add(makeDoneButtonPanel());
            panel.add(Box.createVerticalGlue());

            super.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancel();
                }
            });
            setContentPane(panel);

            pack();
            InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(client, t, this);
            setVisible(true);
        }

        JPanel makePanel(JPanel p) {
            JPanel panel = new JPanel();
            panel.add(p);
            return panel;
        }
        
        JPanel makeColorPanel(int type) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            JPanel p;
            SpinnerNumberModel model;
            if (type == BORDER) {
                model = new SpinnerNumberModel(_pos.getBorderSize(), 0, 100, 1);
                p = makePanel(DecoratorPanel.makeSpinPanel("borderSize", new AJSpinner(model, BORDER), this));
            }
            else {
                model = new SpinnerNumberModel(_pos.getMarginSize(), 0, 100, 1);
                p = makePanel(DecoratorPanel.makeSpinPanel("marginSize", new AJSpinner(model, MARGIN), this));
           }
            panel.add(p);
            
            JButton button = new JButton(Bundle.getMessage("transparentBack"));
            button.addActionListener((ActionEvent event) -> {
                _pos.setBackground(null);
            });
            panel.add(button);
            return panel;
        }

        JPanel makeTextSpinnerPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            SpinnerNumberModel model = new SpinnerNumberModel(_pos.getFixedWidth(), 0, 1000, 1);
            panel.add(DecoratorPanel.makeSpinPanel("fixedWidth", new AJSpinner(model, FWIDTH), this));
            panel.add(Box.createHorizontalStrut(STRUT));
            model = new SpinnerNumberModel(_pos.getFixedHeight(), 0, 1000, 1);
            panel.add(DecoratorPanel.makeSpinPanel("fixedHeight", new AJSpinner(model, FHEIGHT), this));
            return panel;
        }

        JPanel makeTextPanel() {
            JPanel panel = new JPanel();
            JTextField textField = new JTextField(_saveText, 25);
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent evt) {
                }
                @Override
                public void keyPressed(KeyEvent evt) {
                }
                @Override
                public void keyReleased(KeyEvent evt) {
                    JTextField tmp = (JTextField) evt.getSource();
                    ((PositionableLabel)_target).setText(tmp.getText());
                }
            });
            panel.add(textField);
            return panel;
        }

        protected JPanel makeDoneButtonPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener((ActionEvent event) -> {
                done();
            });
            panel.add(doneButton);

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener((ActionEvent event) -> cancel());

            panel.add(cancelButton);
            return panel;
        }

        void done() {
            log.debug("Done button: color= {}", _chooser.getColor());
            if (_colorAction != null) {
                _colorAction.actionPerformed(null);
            }
            if (_pos != null) {
                _pos.setSuppressRecentColor(false);
            }
            JmriColorChooser.suppressAddRecentColor(false);
            JmriColorChooser.addRecentColor(_chooser.getColor());
            dispose();
        }

        void cancel() {
            if (_pos != null) {
                _savePos.setAttributesOf(_pos);
                _pos.setSuppressRecentColor(false);
                _pos.updateSize();
                if (_type == TEXT) {
                    ((PositionableLabel)_target).setText(_saveText);
                }
            } else {
                _target.setBackground(_saveColor);
            }
            _target.setOpaque(_saveOpaque);
            log.debug("Cancel: color= {}", _saveColor);
            JmriColorChooser.suppressAddRecentColor(false);
            dispose();
        }

        @Override
        public void setFontFace(Font font) {
            _pos.setFont(font);
        }

        @Override
        public void setFontSize(float size) {
            _pos.setFontSize(size);
        }

        @Override
        public void setFontStyle(int style) {
            _pos.setFontStyle(style);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object obj = e.getSource();
            if (obj instanceof AJSpinner) {
                int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
                switch (((AJSpinner) obj)._which) {
                    case BORDER:
                        _pos.setBorderSize(num);
                        break;
                    case MARGIN:
                        _pos.setMarginSize(num);
                        break;
                    case FWIDTH:
                        _pos.setFixedWidth(num);
                        break;
                    case FHEIGHT:
                        _pos.setFixedHeight(num);
                        break;
                    default:
                        log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                        break;
                }
            } else {
                log.debug("stateChanged: color= {}", _chooser.getColor());
                if (_pos != null) {
                    switch (_type) {
                        case BORDER:
                            _pos.setBorderColor(_chooser.getColor());
                            break;
                        case MARGIN:
                            _pos.setBackground(_chooser.getColor());
                            break;
                        case FONT:
                        case TEXT:
                            _pos.setForeground(_chooser.getColor());
                            break;
                        default:
                    }
                } else {
                    _target.setOpaque(true);
                    _target.setBackground(_chooser.getColor());
                }
            }
        }

        private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ColorDialog.class);
}

