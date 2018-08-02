package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
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
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.PositionableJPanel;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.palette.TextItemPanel.DragDecoratorLabel;
import jmri.util.swing.ImagePanel;
import jmri.util.swing.JmriColorChooser;

/**
 * Panel for positionables with text and/or colored margins and borders.
 * @see ItemPanel palette class diagram
 *
 * @author PeteCressman Copyright (C) 2009, 2015
 */
public class DecoratorPanel extends JPanel implements ChangeListener, ItemListener {

    static final String[] JUSTIFICATION = {Bundle.getMessage("left"),
        Bundle.getMessage("center"),
        Bundle.getMessage("right")};

    static final String[] STYLES = {Bundle.getMessage("Plain"),
        Bundle.getMessage("Bold"),
        Bundle.getMessage("Italic"),
        Bundle.getMessage("Bold/italic")};

    static final String[] FONTSIZE = {"6", "8", "10", "11", "12", "14", "16",
        "20", "24", "28", "32", "36"};

    public static final int SIZE = 1;
    public static final int STYLE = 2;
    public static final int JUST = 3;
    public static final int FONT = 4;

    private AJComboBox _fontBox;
    private AJComboBox _fontSizeBox;
    private AJComboBox _fontStyleBox;
    private AJComboBox _fontJustBox;

    public static final int STRUT = 6;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    static final int FOREGROUND_BUTTON = 1;
    static final int BACKGROUND_BUTTON = 2;
    static final int TRANSPARENT_BUTTON = 3;
    static final int BORDERCOLOR_BUTTON = 4;

    private AJSpinner _borderSpin;
    private AJSpinner _marginSpin;
    private AJSpinner _widthSpin;
    private AJSpinner _heightSpin;

    private JColorChooser _chooser;
    ImagePanel _previewPanel;
    JPanel _samplePanel;
    private int _selectedButton;
    private String _selectedState;
    private boolean _isPositionableLabel;
    private final ButtonGroup _buttonGroup = new ButtonGroup();
    private AJRadioButton _fontButton;
    private AJRadioButton _borderButton;
    private AJRadioButton _backgroundButton;

    protected BufferedImage[] _backgrounds; // array of Image backgrounds
    protected JComboBox<String> _bgColorBox;

    Editor _editor;
    protected DisplayFrame _paletteFrame;
    // map of _target's state components for preview panel
    private final HashMap<String, PositionableLabel> _samples;
    private Positionable _panelPos;     // positionable on  panel
    private Positionable _savePos;      // clone of original _panelPos fo

    public DecoratorPanel(Editor editor, DisplayFrame paletteFrame) {
        _editor = editor;
        _paletteFrame = paletteFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color panelBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        // create array of backgrounds, _currentBackground already set and used
        _backgrounds = ItemPanel.makeBackgrounds(null,  panelBackground);
        _chooser = new JColorChooser(panelBackground);
        _samples = new HashMap<>();

        _previewPanel = new ImagePanel();
        _previewPanel.setLayout(new BorderLayout());
        _previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        _previewPanel.add(Box.createVerticalStrut(STRUT), BorderLayout.PAGE_START);
        _previewPanel.add(Box.createVerticalStrut(STRUT), BorderLayout.PAGE_END);

        _samplePanel = new JPanel();
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
        _samplePanel.setOpaque(false);
    }

    @SuppressWarnings("unchecked")
    static class AJComboBox extends JComboBox  {
        int _which;

        AJComboBox(Font[] items, int which) {
            super(items);
            _which = which;
        }
        AJComboBox(String[] items, int which) {
            super(items);
            _which = which;
        }
    }

    private JPanel makeBoxPanel(String caption, JComboBox<Class<?>> box) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        box.addItemListener(this);
        panel.add(box);
        return panel;
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

    private JPanel makeSpinPanel(String caption, JSpinner spin) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        spin.addChangeListener(this);
        panel.add(spin);
        return panel;
    }

    /* Called by Palette's TextItemPanel i.e. make a new panel item to drag */
    protected void initDecoratorPanel(DragDecoratorLabel sample) {
        sample.setDisplayLevel(Editor.LABELS);
        sample.setSuppressRecentColor(true);
        _samples.put("Text", sample);
        _selectedState = "Text";
        _isPositionableLabel = true;
        makeFontPanels(sample);
        this.add(makeTextPanel("Text", sample, true));
        _samplePanel.add(sample);
        log.debug("DragDecoratorLabel size {} | panel size {}", sample.getPreferredSize(), _samplePanel.getPreferredSize());
        finishInit(true, sample);
    }

    /* Called by Editor's TextAttrDialog - i.e. update a panel item from menu */
    public void initDecoratorPanel(Positionable pos) {
//        makeFontPanels(pos);
        _savePos = pos.deepClone(); // need copy of PositionableJPanel in PopupUtility
        _panelPos = pos;
//        item.remove();      // don't need copy any more. Removes ghost image of PositionableJPanels
//        pos.setSuppressRecentColor(true);
        _isPositionableLabel = (pos instanceof PositionableLabel);
        makeFontPanels(pos);

        if (pos instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon) pos;
            if (pi.isText()) {
                Iterator<Map.Entry<String, PositionableLabel>> iter = pi.getIconMap().entrySet().iterator();
                while(iter.hasNext()) {
                    Map.Entry<String, PositionableLabel> entry = iter.next();
                    PositionableLabel val = entry.getValue();
                    PositionableLabel sample = new PositionableLabel(val.getText(), _editor);
                    if (pi.isIcon()) {
                        sample.setIsIcon(true);
                    }
                    setSample(entry.getKey(), val, sample, true);
                }
            }
        } else {
            PositionableLabel sample = new PositionableLabel("", _editor);
            boolean addtextField;
            if (pos instanceof PositionableLabel) {
                PositionableLabel p = (PositionableLabel)pos;
                sample.setText(p.getText());
                addtextField = !(pos instanceof jmri.jmrit.display.MemoryIcon);
                sample.setFixedWidth(p.getFixedWidth());
                sample.setFixedHeight(p.getFixedHeight());
            } else {
                // To display PositionableJPanel types as PositionableLabels, set fixed sizes.
                sample.setFixedWidth(pos.getWidth() - 2*pos.getBorderSize());
                sample.setFixedHeight(pos.getHeight() - 2*pos.getBorderSize());
                if (pos instanceof PositionableJPanel) {
                    sample.setText(((PositionableJPanel)pos).getText());
                    addtextField = false;
                } else {
                    addtextField = true;
                    log.error("Unknown Postionable Type {}", pos.getClass().getName());
                }
            }
            setSample("Text", pos, sample, addtextField);
        }
        finishInit(false, pos);
    }

    private void finishInit(boolean addBgCombo, Positionable pos) {
        _chooser.setPreviewPanel(new JPanel());
        _chooser = JmriColorChooser.extendColorChooser(_chooser);
        setSuppressRecentColor(true);
        _chooser.getSelectionModel().addChangeListener(this);
        add(_chooser);
        _previewPanel.add(_samplePanel, BorderLayout.CENTER);

        // add a SetBackground combo
        if (addBgCombo) {
            add(add(makeBgButtonPanel(_previewPanel, null, _backgrounds))); // no listener on this variant
        }
        add(_previewPanel);
        _previewPanel.setImage(_backgrounds[0]);
        _previewPanel.revalidate();        // force redraw
        // after everything created, set selections
        setFontSelections(pos);
        updateSamples();
    }

    private void setSample(String state, Positionable pos, PositionableLabel sample, boolean editText) {
        pos.setAttributesOf(sample);
        sample.setSuppressRecentColor(true);
        _samples.put(state, sample);
        this.add(makeTextPanel(state, sample, editText));
        _samplePanel.add(sample);
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
    }

    protected void makeFontPanels(Positionable pos) {
        JPanel fontPanel = new JPanel();

        Font defaultFont = pos.getFont();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontFamilyNames[] = ge.getAvailableFontFamilyNames();
        Font[] fonts = new Font[fontFamilyNames.length];
        int k = 0;
        for (String fontFamilyName : fontFamilyNames) {
            fonts[k++] = new Font(fontFamilyName, defaultFont.getStyle(), defaultFont.getSize()) {
                @Override
                public String toString() {
                    return getFamily();
                }
            };
        }
        _fontBox = new AJComboBox(fonts, FONT);
        fontPanel.add(makeBoxPanel("EditFont", _fontBox)); // NOI18N

        _fontSizeBox = new AJComboBox(FONTSIZE, SIZE);
        fontPanel.add(makeBoxPanel("FontSize", _fontSizeBox)); // NOI18N

        _fontStyleBox = new AJComboBox(STYLES, STYLE);
        fontPanel.add(makeBoxPanel("FontStyle", _fontStyleBox)); // NOI18N

        _fontJustBox = new AJComboBox(JUSTIFICATION, JUST);
        fontPanel.add(makeBoxPanel("Justification", _fontJustBox)); // NOI18N
        this.add(fontPanel);

        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(pos.getBorderSize(), 0, 100, 1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin));
        model = new SpinnerNumberModel(pos.getMarginSize(), 0, 100, 1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin));
        model = new SpinnerNumberModel(pos.getFixedWidth(), 0, 1000, 1);
        if (_isPositionableLabel) {
            _widthSpin = new AJSpinner(model, FWIDTH);
            sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin));
            model = new SpinnerNumberModel(pos.getFixedHeight(), 0, 1000, 1);
            _heightSpin = new AJSpinner(model, FHEIGHT);
            sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin));
        }
        this.add(sizePanel);
    }
    
    private void setFontSelections(Positionable pos) {
        _fontBox.setSelectedItem(pos.getFont());
        int row = 4;
        for (int i = 0; i < FONTSIZE.length; i++) {
            if (pos.getFont().getSize() == Integer.parseInt(FONTSIZE[i])) {
                row = i;
                break;
            }
        }
        _fontSizeBox.setSelectedIndex(row);

        _fontStyleBox = new AJComboBox(STYLES, STYLE);
        _fontStyleBox.setSelectedIndex(pos.getFont().getStyle());

        _fontJustBox = new AJComboBox(JUSTIFICATION, JUST);
        switch (pos.getJustification()) {
            case Positionable.LEFT:
                row = 0;
                break;
            case Positionable.RIGHT:
                row = 2;
                break;
            case Positionable.CENTRE:
                row = 1;
                break;
            default:
                row = 2;
        }
        _fontJustBox.setSelectedIndex(row);

        _selectedButton = FOREGROUND_BUTTON;
        _chooser.setColor(pos.getForeground());
        _fontButton.setSelected(true);
    }

    private JPanel makeTextPanel(String state, PositionableLabel sample, boolean addTextField) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage(state)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        if (addTextField) {
            JTextField textField = new JTextField(sample.getText(), 25);
            textField.addKeyListener(new KeyListener() {
                PositionableLabel sample;

                KeyListener init(PositionableLabel s) {
                    sample = s;
                    return this;
                }
                @Override
                public void keyTyped(KeyEvent E) {
                }
                @Override
                public void keyPressed(KeyEvent E) {
                }
                @Override
                public void keyReleased(KeyEvent E) {
                    JTextField tmp = (JTextField) E.getSource();
                    sample.setText(tmp.getText());
                    updateSamples();
                }
            }.init(sample));
            p.add(textField);
        }
        panel.add(p);

        p = new JPanel();
        _fontButton = makeColorRadioButton("FontColor", FOREGROUND_BUTTON, state);
        p.add(_fontButton);
        
        _backgroundButton = makeColorRadioButton("FontBackgroundColor", BACKGROUND_BUTTON, state);
        p.add(_backgroundButton);
        
        AJRadioButton button = makeColorRadioButton("transparentBack", TRANSPARENT_BUTTON, state);
        p.add(button);

        _borderButton = makeColorRadioButton("borderColor", BORDERCOLOR_BUTTON, state);
        p.add(_borderButton);

        panel.add(p);
        return panel;
    }

    private AJRadioButton makeColorRadioButton(String caption, int which, String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage(caption), which, state);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    _selectedButton = button._which;
                    _selectedState = button._state;
                    PositionableLabel pos =_samples.get(_selectedState);
                    switch (button._which) {
                        case FOREGROUND_BUTTON:
                            _chooser.setColor(pos.getForeground());
                            break;
                        case BACKGROUND_BUTTON:
                            Color c = pos.getBackgroundColor();
                            if (c != null) {
                                _chooser.setColor(c);
                            }
                            break;
                        case TRANSPARENT_BUTTON:
                            pos.setOpaque(false);
                            pos.setBackgroundColor(null);
                            break;
                        case BORDERCOLOR_BUTTON:
                            _chooser.setColor(pos.getBorderColor());
                            break;
                        default:    // TRANSPARENT_BUTTON
                    }
                    log.debug("Button actionPerformed Color button _state= {} _which= {}",
                           button._state, button._which);
                    updateSamples();
                }
            }
        });
        _buttonGroup.add(button);            
        return button;
    }
    /**
     * Create panel element containing [Set background:] drop down list.
     * Special version for Decorator, no access to shared variable previewBgSet.
     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
     * @see ItemPanel
     *
     * @param preview1 ImagePanel containing icon set
     * @param preview2 not used, matches method in ItemPanel
     * @param imgArray array of colored background images
     * @return a JPanel with label and drop down
     */
    private JPanel makeBgButtonPanel(@Nonnull ImagePanel preview1, ImagePanel preview2, BufferedImage[] imgArray) {
        _bgColorBox = new JComboBox<>();
        _bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, too long for combo
        _bgColorBox.addItem(Bundle.getMessage("White"));
        _bgColorBox.addItem(Bundle.getMessage("LightGray"));
        _bgColorBox.addItem(Bundle.getMessage("DarkGray"));
        _bgColorBox.addItem(Bundle.getMessage("Checkers"));
        int index;
        if (_paletteFrame != null) {
            index = _paletteFrame.getPreviewBg();
        } else {
            index = 0;
        }
        _bgColorBox.setSelectedIndex(index);
        _bgColorBox.addActionListener((ActionEvent e) -> {
            if (imgArray != null) {
                // index may repeat
                int previewBgSet = _bgColorBox.getSelectedIndex(); // store user choice
                if (_paletteFrame != null) {
                    _paletteFrame.setPreviewBg(previewBgSet);
                }
                // load background image
                log.debug("Palette Decorator setImage called {}", previewBgSet);
                preview1.setImage(imgArray[previewBgSet]);
                preview1.revalidate();        // force redraw
            } else {
                log.debug("imgArray is empty");
            }
        });
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        pp.add(_bgColorBox);
        backgroundPanel.add(pp);
        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
        return backgroundPanel;
    }

    // called when editor changed
    protected BufferedImage[] getBackgrounds() {
        return _backgrounds;
    }
    // called when editor changed
    protected void setBackgrounds(BufferedImage[] imgArray) {
        _backgrounds = imgArray;
        _previewPanel.setImage(imgArray[0]);
        _previewPanel.revalidate();        // force redraw
    }


    private void updateSamples() {
        if (_previewPanel == null) {
            return;
        }
        Iterator<PositionableLabel> it = _samples.values().iterator();
        while (it.hasNext()) {
            PositionableLabel sam = it.next();
            sam.updateSize();
        }
        _samplePanel.repaint();
    }

   @Override
    public void stateChanged(ChangeEvent e) {
        PositionableLabel pos = _samples.get(_selectedState);
        if (pos == null) {  // initial default selections call before setup is complete
            return;
        }
        Object obj = e.getSource();
        log.debug("stateChanged source= {} _selectedState= {},  _selectedButton= {}", obj.getClass().getName(), _selectedState, _selectedButton);
       if (obj instanceof AJSpinner) {
            int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
            switch (((AJSpinner) obj)._which) {
                case BORDER:
                    pos.setBorderSize(num);
                    _borderButton.setSelected(true);
                    _selectedButton = BORDERCOLOR_BUTTON;
                    _chooser.setColor(pos.getBorderColor());
                    break;
                case MARGIN:
                    pos.setMarginSize(num);
                    _backgroundButton.setSelected(true);
                    _selectedButton = BACKGROUND_BUTTON;
                    Color c = pos.getBackgroundColor();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                case FWIDTH:
                    pos.setFixedWidth(num);
                    _backgroundButton.setSelected(true);
                    _selectedButton = BACKGROUND_BUTTON;
                    c = pos.getBackgroundColor();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                case FHEIGHT:
                    pos.setFixedHeight(num);
                    _backgroundButton.setSelected(true);
                    _selectedButton = BACKGROUND_BUTTON;
                    c = pos.getBackgroundColor();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                default:
                    log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                    break;
            }
            log.debug("stateChanged sizes which= {} _selectedState= {} _selectedButton= {}",
                    ((AJSpinner)obj)._which, _selectedState, _selectedButton);
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
                pos.setBackgroundColor(_chooser.getColor());
                pos.setOpaque(true);
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
                pos.isOpaque(), _selectedState, _selectedButton);
        updateSamples();
    }

    public void setAttributes(Positionable pos) {
        if (pos instanceof PositionableIcon) {
            for (Map.Entry<String, PositionableLabel> entry : ((PositionableIcon)pos).getIconMap().entrySet()) {
                PositionableLabel val = entry.getValue();
                PositionableLabel sample = _samples.get(entry.getKey());
                sample.setAttributesOf(pos);
                val.setText(sample.getText());
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

    @Override
    public void itemStateChanged(ItemEvent e) {
        AJComboBox comboBox = (AJComboBox)e.getSource();
        PositionableLabel pos = _samples.get(_selectedState);
        if (pos == null) {  // initial default selections call before setup is complete
            return;
        }
        switch (comboBox._which) {
            case SIZE:
                String size = (String) comboBox.getSelectedItem();
                pos.setFontSize(Float.valueOf(size));
                break;
            case STYLE:
                int style = 0;
                switch (comboBox.getSelectedIndex()) {
                    case 0:
                        style = Font.PLAIN;
                        break;
                    case 1:
                        style = Font.BOLD;
                        break;
                    case 2:
                        style = Font.ITALIC;
                        break;
                    case 3:
                        style = (Font.BOLD | Font.ITALIC);
                        break;
                    default:
                        log.warn("Unexpected index {}  in itemStateChanged", comboBox.getSelectedIndex());
                        break;
                }
                pos.setFontStyle(style);
                break;
            case JUST:
                int just = 0;
                switch (comboBox.getSelectedIndex()) {
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
                        log.warn("Unexpected index {}  in itemStateChanged", comboBox.getSelectedIndex());
                        break;
                }
                pos.setJustification(just);
                break;
            case FONT:
                Font font = (Font) comboBox.getSelectedItem();
                int st = pos.getFont().getStyle();
                int s = pos.getFont().getSize();
                pos.setFont(font);
                pos.setFontStyle(st);
                pos.setFontSize(s);
                break;
            default:
                log.warn("Unexpected _which {}  in itemStateChanged", comboBox._which);
                break;
            }
        _fontButton.setSelected(true);
        _selectedButton = FOREGROUND_BUTTON;
        _chooser.setColor(pos.getForeground());
        updateSamples();
    }
    
    public void setSuppressRecentColor(boolean bool) {
        Iterator<PositionableLabel> iter = _samples.values().iterator();
        while (iter.hasNext()) {
            iter.next().setSuppressRecentColor(bool);
        }
    }
    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecoratorPanel.class);

}
