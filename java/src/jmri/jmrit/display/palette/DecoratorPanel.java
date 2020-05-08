package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.DisplayState;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.PositionableJPanel;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.palette.FontPanel.AJComboBox;
import jmri.jmrit.display.PreviewPanel;
import jmri.jmrit.display.palette.TextItemPanel.DragDecoratorLabel;
import jmri.util.swing.ImagePanel;
import jmri.util.swing.JmriColorChooser;

/**
 * Panel for positionables with text and/or colored margins and borders.
 * @see ItemPanel palette class diagram
 *
 * @author PeteCressman Copyright (C) 2009, 2015
 */
public class DecoratorPanel extends JPanel implements ChangeListener, FontPanelListener {

    public static final int STRUT = 6;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    static final int FOREGROUND_BUTTON = 1;
    static final int BACKGROUND_BUTTON = 2;
    static final int TRANSPARENT_BUTTON = 3;
    static final int BORDERCOLOR_BUTTON = 4;

    static String[] BUTTONS = {"None", "Font", "Background", "Transparent", "Border"};
    static String[] JUSTIFICATION = {Bundle.getMessage("left"),
            Bundle.getMessage("center"),
            Bundle.getMessage("right")};

    private AJSpinner _borderSpin;
    private AJSpinner _marginSpin;
    private AJSpinner _widthSpin;
    private AJSpinner _heightSpin;

    private FontPanel _fontPanel;
    private AJComboBox _fontJustBox;

    private JColorChooser _chooser;
    private PreviewPanel _previewPanel;
    private final ImagePanel _samplePanel;
    private final HashMap<String, ColorButtonPanel> _colorPanels;
    private HashMap<String, PositionableLabel> _samples = null;    
    private int _selectedButton;
    private String _selectedState;
    private final ButtonGroup _buttonGroup = new ButtonGroup();

    Editor _editor;
    protected DisplayFrame _frame;

    public DecoratorPanel(DisplayFrame frame) {
        _editor = frame.getEditor();
        _frame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color panelBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        _chooser = new JColorChooser(panelBackground);
        _samples = new HashMap<>();
        _colorPanels =  new HashMap<>();

        _samplePanel = new ImagePanel();
        _samplePanel.add(Box.createVerticalStrut(50));
        _samplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _samplePanel.setOpaque(false);
    }

    static class AJSpinner extends JSpinner {
        int _which;

        AJSpinner(SpinnerModel model, int which) {
            super(model);
            _which = which;
        }
    }

    static class AJRadioButton extends JRadioButton {

        int _which;
        String _state;

        AJRadioButton(String text, int which, String state) {
            super(text);
            _which = which;
            _state = state;
        }
        
        String getState() {
            return _state;
        }
    }
    
    class ColorButtonPanel extends JPanel {
        AJRadioButton _fontButton;
        AJRadioButton _borderButton;
        AJRadioButton _transparentButton;
        AJRadioButton _backgroundButton;

        void setFontButton(AJRadioButton button) {
            _fontButton = button;
            add(button);
        }
        void setBackgroundButton(AJRadioButton button) {
            _backgroundButton = button;
            add(button);
        }
        void setTransparentButton(AJRadioButton button) {
            _transparentButton = button;
            add(button);
        }
        void setBorderButton(AJRadioButton button) {
            _borderButton = button;
            add(button);
        }
        void setButtonSelected (int select) {
            switch (select) {
                case FOREGROUND_BUTTON:
                    _fontButton.setSelected(true);
                    _selectedButton = FOREGROUND_BUTTON;
                    break;
                case BACKGROUND_BUTTON:
                    _backgroundButton.setSelected(true);
                    _selectedButton = BACKGROUND_BUTTON;
                    break;
                case TRANSPARENT_BUTTON:
                    _transparentButton.setSelected(true);
                    _selectedButton = TRANSPARENT_BUTTON;
                    break;
                case BORDERCOLOR_BUTTON:
                    _borderButton.setSelected(true);
                    _selectedButton = BORDERCOLOR_BUTTON;
                    break;
                default:
            }
        }
    }

    public static JPanel makeSpinPanel(String caption, JSpinner spin, ChangeListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        spin.addChangeListener(listener);
        panel.add(spin);
        return panel;
    }

    public static void setJustificationIndex(Positionable pos, JComboBox<?> box) {
        int just;
        switch (pos.getJustification()) {
            case Positionable.LEFT:
                just = 0;
                break;
            case Positionable.RIGHT:
                just = 2;
                break;
            case Positionable.CENTRE:
                just = 1;
                break;
            default:
                just = 1;
        }
        box.setSelectedIndex(just);
    }

    /* Called by Palette's TextItemPanel i.e. make a new panel item to drag */
    protected void initDecoratorPanel(DragDecoratorLabel sample) {
        sample.setDisplayLevel(Editor.LABELS);
        sample.setSuppressRecentColor(true);
        _samples.put("Text", sample);
        _selectedState = "Text";
        makeFontPanels(true);
        JPanel panel = makeTextPanel("Text", sample);
        panel.add(makeColorButtonPanel("Text"));
        add(panel);
        _samplePanel.add(sample);
        log.debug("DragDecoratorLabel size {} | panel size {}", sample.getPreferredSize(), _samplePanel.getPreferredSize());
        finishInit(sample);
    }

    /* Called by Editor's TextAttrDialog - i.e. update a panel item from menu */
    public void initDecoratorPanel(Positionable pos) {

        if (pos instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon) pos;
            _selectedState = pi.getState();
            makeFontPanels(true);
            if (pi.isText() && !pi.isIcon()) {
                Iterator<Map.Entry<String, DisplayState>> iter = pi.getDisplayStateMap().entrySet().iterator();
                while(iter.hasNext()) {
                    Map.Entry<String, DisplayState> entry = iter.next();
                    DisplayState val = entry.getValue();
                    PositionableLabel sample = new PositionableLabel(val.getText(), _editor);
                    if (pi.isIcon()) {
                        sample.setIsIcon(true);
                    }
                    val.setDisplayParameters(sample);
                    setSample(entry.getKey(), sample, true, false);
                }
            }
        } else {
            PositionableLabel sample = new PositionableLabel("", _editor);
            _selectedState = "Text";
            boolean addTextField;
            boolean addFixedField;
            if (pos instanceof PositionableLabel) {
                PositionableLabel p = (PositionableLabel)pos;
                sample.setText(p.getText());
                addTextField = !(pos instanceof jmri.jmrit.display.MemoryIcon);
                addFixedField = addTextField;
                sample.setFixedWidth(p.getFixedWidth());
                sample.setFixedHeight(p.getFixedHeight());
            } else {
                // To display PositionableJPanel types as PositionableLabels, set fixed sizes.
                sample.setFixedWidth(pos.getWidth() - 2*pos.getBorderSize());
                sample.setFixedHeight(pos.getHeight() - 2*pos.getBorderSize());
                if (pos instanceof PositionableJPanel) {
                    sample.setText(((PositionableJPanel)pos).getText());
                    addTextField = false;
                    addFixedField = false;
                } else {
                    addTextField = true;
                    addFixedField = true;
                    log.error("Unknown Postionable Type {}", pos.getClass().getName());
                }
            }
            makeFontPanels(addFixedField);
            pos.setAttributesOf(sample);
            setSample("Text", sample, addTextField, addFixedField);
        }
        finishInit(pos);
    }

    private void finishInit(Positionable  p) {
        _chooser = JmriColorChooser.extendColorChooser(_chooser);
        setSuppressRecentColor(true);
        _chooser.getSelectionModel().addChangeListener(this);
        _chooser.setPreviewPanel(new JPanel());
        add(_chooser);
        _fontPanel.setFontSelections(p.getFont());
        updateSamples();
    }
/*
    private void setSelections(Positionable pos) {
        Positionable p = _samples.get(_selectedState);
        _fontPanel.setFontSelections(p.getFont());
        setJustificationIndex(p, _fontJustBox);
        _borderSpin.setValue(pos.getBorderSize());
        _marginSpin.setValue(pos.getMarginSize());
        _widthSpin.setValue(pos.getFixedWidth());
        _heightSpin.setValue(pos.getFixedHeight());
    }*/

    private void setSample(String state, PositionableLabel sample, boolean editText, boolean editFixed) {
        sample.setSuppressRecentColor(true);
        JPanel panel = makeColorButtonPanel(state);
        if (editText) {
            JPanel p = makeTextPanel(state, sample);
            p.add(panel);
            add(p);
        } else {
            add(panel);
        }
        _samples.put(state, sample);
        _samplePanel.add(sample);
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
    }

    private void makeFontPanels(boolean editFixed) {
        _fontPanel = new FontPanel( this);
        add(_fontPanel);

        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100, 1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin, this));

        model = new SpinnerNumberModel(0, 0, 100, 1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin, this));

        if (editFixed) {
            model = new SpinnerNumberModel(0, 0, 1000, 1);
            _widthSpin = new AJSpinner(model, FWIDTH);
            sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin, this));
            model = new SpinnerNumberModel(0, 0, 1000, 1);
            _heightSpin = new AJSpinner(model, FHEIGHT);
            sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin, this));
        }
        this.add(sizePanel);
    }

    private JPanel makeTextPanel(String state, PositionableLabel sample) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage(state)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        JTextField textField = new JTextField(sample.getText(), 25);
        textField.addKeyListener(new KeyListener() {
            PositionableLabel sample;

            KeyListener init(PositionableLabel s) {
                sample = s;
                return this;
            }
            @Override
            public void keyTyped(KeyEvent evt) {
            }
            @Override
            public void keyPressed(KeyEvent evt) {
            }
            @Override
            public void keyReleased(KeyEvent evt) {
                JTextField tmp = (JTextField) evt.getSource();
                sample.setText(tmp.getText());
                updateSamples();
            }
        }.init(sample));
        p.add(textField);
        panel.add(p);
        return panel;
    }
    
    private JPanel makeColorButtonPanel(String state) {
        ColorButtonPanel panel = new ColorButtonPanel();
        panel.setFontButton(makeColorRadioButton("FontColor", FOREGROUND_BUTTON, state));
        panel.setBackgroundButton(makeColorRadioButton("FontBackgroundColor", BACKGROUND_BUTTON, state));
        panel.setTransparentButton(makeColorRadioButton("transparentBack", TRANSPARENT_BUTTON, state));
        panel.setBorderButton(makeColorRadioButton("borderColor", BORDERCOLOR_BUTTON, state));
        _colorPanels.put(state, panel);
        return panel;
    }

    private AJRadioButton makeColorRadioButton(String caption, int which, String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage(caption), which, state);
        button.addActionListener(a -> {
            if (button.isSelected()) {
                _selectedButton =button._which;
                _selectedState = button._state;
                PositionableLabel pos =_samples.get(_selectedState);
                _fontPanel.setFontSelections(pos.getFont());
                setJustificationIndex(pos, _fontJustBox);
                switch (button._which) {
                    case FOREGROUND_BUTTON:
                        _chooser.setColor(pos.getForeground());
                        break;
                    case BACKGROUND_BUTTON:
                        _chooser.setColor(pos.getBackground());
                        break;
                    case BORDERCOLOR_BUTTON:
                        _chooser.setColor(pos.getBorderColor());
                        break;
                    case TRANSPARENT_BUTTON:
                        _chooser.setColor(pos.getBackground());
                        pos.setBackground(null);
                        break;
                    default:    // TRANSPARENT_BUTTON
               }
                log.debug("Button actionPerformed Colors opaque= {} _state= {} _which= {}",
                        pos.isOpaque(), button._state, button._which);
                updateSamples();
            }
        });
        _buttonGroup.add(button);            
        return button;
    }

    protected void setPreviewIndex(int idx) {
        
    }

    // called when editor changed
    protected void sampleBgColorChange() {
        _previewPanel.setBackgroundSelection(_frame.getPreviewBg());
    }

    @Override
    public void setFontFace(Font font) {
        if (log.isDebugEnabled()) {
            log.debug("fontChange: fontFace= {}, _selectedState= {}",
                    font.getFontName(), _selectedState);
        }
        for (PositionableLabel p : _samples.values()) {
            p.setFont(font);
        }
        fontChange();
    }

    @Override
    public void setFontSize(float size) {
        if (log.isDebugEnabled()) {
            log.debug("fontChange: size= {}, _selectedState= {}",
                    size, _selectedState);
        }
        for (PositionableLabel p : _samples.values()) {
            p.setFontSize(size);
        }
        fontChange();
    }

    @Override
    public void setFontStyle(int style) {
        if (log.isDebugEnabled()) {
            log.debug("fontChange: style= {}, _selectedState= {}",
                   style, _selectedState);
        }
        for (PositionableLabel p : _samples.values()) {
            p.setFontStyle(style);
        }
        fontChange();
    }

    private void fontChange() {
        _colorPanels.get(_selectedState).setButtonSelected(FOREGROUND_BUTTON);
        _chooser.setColor(_samples.get(_selectedState).getForeground());
        updateSamples();
     }

    private void updateSamples() {
        for (PositionableLabel sam : _samples.values()) {
            sam.invalidate();
        }
        _previewPanel.revalidate();
    }

   @Override
    public void stateChanged(ChangeEvent e) {
        PositionableLabel pos = _samples.get(_selectedState);
        if (pos == null) {  // initial default selections call before setup is complete
            return;
        }
        Object obj = e.getSource();
        log.debug("stateChanged source= {} _selectedState= {},  _selectedButton= {}",
                obj.getClass().getName(), _selectedState, BUTTONS[_selectedButton]);
       if (obj instanceof AJSpinner) {
            int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
            switch (((AJSpinner) obj)._which) {
                case BORDER:
                    for (PositionableLabel p : _samples.values()) {
                        p.setBorderSize(num);
                    }
                    _colorPanels.get(_selectedState).setButtonSelected(BORDERCOLOR_BUTTON);
                    _chooser.setColor(pos.getBorderColor());
                    break;
                case MARGIN:
                    for (PositionableLabel p : _samples.values()) {
                        p.setMarginSize(num);
                    }
                    _colorPanels.get(_selectedState).setButtonSelected(BACKGROUND_BUTTON);
                    Color c = pos.getBackground();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                case FWIDTH:
                    for (PositionableLabel p : _samples.values()) {
                        p.setFixedWidth(num);
                    }
                    _colorPanels.get(_selectedState).setButtonSelected(BACKGROUND_BUTTON);
                    c = pos.getBackground();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                case FHEIGHT:
                    for (PositionableLabel p : _samples.values()) {
                        p.setFixedHeight(num);
                    }
                    _colorPanels.get(_selectedState).setButtonSelected(BACKGROUND_BUTTON);
                    c = pos.getBackground();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                default:
                    log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                    break;
            }
            log.debug("stateChanged sizes which= {} _selectedState= {} _selectedButton= {}",
                    ((AJSpinner)obj)._which, _selectedState, BUTTONS[_selectedButton]);
            updateSamples();
        } else {
            colorChange();
        }
    }
    
    private void colorChange() {
        PositionableLabel pos =_samples.get(_selectedState);
        switch (_selectedButton) {
            case FOREGROUND_BUTTON:
                pos.setForeground(_chooser.getColor());
                break;
            case BACKGROUND_BUTTON:
                pos.setBackground(_chooser.getColor());                    
                break;
            case TRANSPARENT_BUTTON:
                pos.setOpaque(false);
                break;
            case BORDERCOLOR_BUTTON:
                pos.setBorderColor(_chooser.getColor());
                break;
            default:
                log.warn("Unexpected color change for state {}, button# {}", _selectedState, _selectedButton);
                break;
        }
        log.debug("colorChange Colors opaque= {} _selectedState= {} _selectedButton= {}",
                pos.isOpaque(), _selectedState, BUTTONS[_selectedButton]);
        updateSamples();
    }

    public void setAttributes(Positionable pos) {
        if (pos instanceof PositionableIcon) {
            for (Map.Entry<String, DisplayState> entry : ((PositionableIcon)pos).getDisplayStateMap().entrySet()) {
                DisplayState val = entry.getValue();
                PositionableLabel sample = _samples.get(entry.getKey());
                val.setText(sample.getText());
                val.setIcon(sample.getIcon());
                val.setForeground(sample.getForeground());
                val.setBackground(sample.getBackground());
                val.setBorderColor(sample.getBorderColor());
           }
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel sample = _samples.get("Text");
            sample.setAttributesOf(pos);
            if (!(pos instanceof jmri.jmrit.display.MemoryIcon)) {
                ((PositionableLabel) pos).setText(sample.getText());
            }
        } else {
            PositionableLabel sample = _samples.get("Text");
            sample.setAttributesOf(pos);
        }
        pos.invalidate();
    }
    
    public void setSuppressRecentColor(boolean bool) {
        for (PositionableLabel positionableLabel : _samples.values()) {
            positionableLabel.setSuppressRecentColor(bool);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecoratorPanel.class);
}
