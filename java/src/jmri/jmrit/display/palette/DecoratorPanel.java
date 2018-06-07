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
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.palette.TextItemPanel.DragDecoratorLabel;
import jmri.util.swing.ImagePanel;

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

    AJComboBox _fontBox;
    AJComboBox _fontSizeBox;
    AJComboBox _fontStyleBox;
    AJComboBox _fontJustBox;

    public static final int STRUT = 6;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    static final int FOREGROUND_BUTTON = 1;
    static final int BACKGROUND_BUTTON = 2;
    static final int TRANSPARENT_BUTTON = 3;
    static final int BORDERCOLOR_BUTTON = 4;
/*
    public static final int TEXT_FONT = 10;
    public static final int ACTIVE_FONT = 11;
    public static final int INACTIVE_FONT = 12;
    public static final int UNKOWN_FONT = 13;
    public static final int INCONSISTENT_FONT = 14;
    public static final int TEXT_BACKGROUND = 20;
    public static final int ACTIVE_BACKGROUND = 21;
    public static final int INACTIVE_BACKGROUND = 22;
    public static final int UNKOWN_BACKGROUND = 23;
    public static final int INCONSISTENT_BACKGROUND = 24;
    public static final int TRANSPARENT_COLOR = 30;
    public static final int ACTIVE_TRANSPARENT_COLOR = 31;
    public static final int INACTIVE_TRANSPARENT_COLOR = 32;
    public static final int UNKNOWN_TRANSPARENT_COLOR = 33;
    public static final int INCONSISTENT_TRANSPARENT_COLOR = 34;
    public static final int BORDER_COLOR = 40;
*/
    AJSpinner _borderSpin;
    AJSpinner _marginSpin;
    AJSpinner _widthSpin;
    AJSpinner _heightSpin;

    JColorChooser _chooser;
    ImagePanel _previewPanel;
    JPanel _samplePanel;
    private HashMap<String, PositionableLabel> _sample = null;    // collection of preview items
    private int _selectedButton;
    private String _selectedState;
    ButtonGroup _buttonGroup = new ButtonGroup();
    AJRadioButton _fontButton;
    AJRadioButton _borderButton;
    AJRadioButton _backgroundButton;

    protected BufferedImage[] _backgrounds; // array of Image backgrounds
    protected JComboBox<String> _bgColorBox;

    Editor _editor;
    protected DisplayFrame _paletteFrame;
    private HashMap<String, PositionableLabel> _componentMap;   // map of _target's components

    public DecoratorPanel(Editor editor, DisplayFrame paletteFrame) {
        _editor = editor;
        _paletteFrame = paletteFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color panelBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        // create array of backgrounds, _currentBackground already set and used
        _backgrounds = ItemPanel.makeBackgrounds(null,  panelBackground);
        _chooser = new JColorChooser(panelBackground);
        _sample = new HashMap<>();
        _componentMap = new HashMap<>();

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
        String _state;

        AJRadioButton(String text, String w) {
            super(text);
            _state = w;
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
        _componentMap.put("Text", (PositionableLabel)sample.deepClone());
        _sample.put("Text", sample);
        makeFontPanels(sample);
        this.add(makeTextPanel("Text", sample, true));
        _samplePanel.add(sample);
        finishInit(true);
    }

    /* Called by Editor's TextAttrDialog - i.e. update a panel item from menu */
    public void initDecoratorPanel(Positionable pos) {
        makeFontPanels(pos);

        if (pos instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon) pos;
            _componentMap = pi.getIconMap(); 
            if (pi.isText()) {
                Iterator<Map.Entry<String, PositionableLabel>> iter = pi.getIconMap().entrySet().iterator();
                while(iter.hasNext()) {
                    Map.Entry<String, PositionableLabel> entry = iter.next();
                    PositionableLabel val = entry.getValue();
                    PositionableLabel sample = new PositionableLabel(val.getText(), _editor);
                    sample.setForeground(val.getForeground());
                    Color color = val.getBackgroundColor();
                    if (color!=null) {
                        sample.setBackground(color);
                        sample.setOpaque(true);
                    }
                    doPopupUtility(entry.getKey(), val, sample, true); // NOI18N
                }
            }
            if (pi.isIcon()) {
                PositionableLabel sample = new PositionableLabel(pi.getText(), _editor); 
                Color color = pi.getBackgroundColor();
                if (color!=null) {
                    sample.setBackground(color);
                    sample.setOpaque(true);
                }
                PositionableLabel p = (PositionableLabel)sample.deepClone();
                _componentMap.put("Text", p);
                doPopupUtility("Text", p, sample, true); // NOI18N
            }
        } if (pos instanceof PositionableLabel) {
            PositionableLabel p = (PositionableLabel)pos;
            PositionableLabel sample = new PositionableLabel(p.getText(), _editor); 
            Color color = p.getBackgroundColor();
            if (color!=null) {
                sample.setBackground(color);
                sample.setOpaque(true);
            } else {
                sample.setOpaque(true);                
            }
            _componentMap.put("Text", (PositionableLabel)pos.deepClone());
            doPopupUtility("Text", p, sample, !(pos instanceof jmri.jmrit.display.MemoryIcon));
        } else {
            PositionableLabel sample = new PositionableLabel("", _editor);
            sample.setForeground(pos.getForeground());
            sample.setBackground(pos.getBackground());
            if (pos instanceof jmri.jmrit.display.MemoryInputIcon) {
                JTextField field = (JTextField)((jmri.jmrit.display.MemoryInputIcon)pos).getTextComponent();
                sample.setText(field.getText());
           } else if (pos instanceof jmri.jmrit.display.MemoryComboIcon) {
                JComboBox<String> box = ((jmri.jmrit.display.MemoryComboIcon)pos).getTextComponent();
                sample.setText(box.getSelectedItem().toString());
            } else if (pos instanceof jmri.jmrit.display.MemorySpinnerIcon) {
                JTextField field = (JTextField)((jmri.jmrit.display.MemorySpinnerIcon)pos).getTextComponent();
                sample.setText(field.getText());
            }
            PositionableLabel p = (PositionableLabel)pos.deepClone();   // COULD BE TROUBLE HERE !!!
            _componentMap.put("Text", p);
            doPopupUtility("Text", p, sample, false);
        }
        finishInit(false);
    }

    private void finishInit(boolean addBgCombo) {
        _chooser.getSelectionModel().addChangeListener(this);
        _chooser.setPreviewPanel(new JPanel());
        add(_chooser);
        _previewPanel.add(_samplePanel, BorderLayout.CENTER);

        // add a SetBackground combo
        if (addBgCombo) {
            add(add(makeBgButtonPanel(_previewPanel, null, _backgrounds))); // no listener on this variant            
        }
        add(_previewPanel);
        _previewPanel.setImage(_backgrounds[0]);
        _previewPanel.revalidate();        // force redraw
        updateSamples();
        _fontButton.setSelected(true);
    }

    private void doPopupUtility(String state, PositionableLabel pos, PositionableLabel sample, boolean editText) {
        sample.setJustification(pos.getJustification());
        sample.setFixedWidth(pos.getFixedWidth());
        sample.setFixedHeight(pos.getFixedHeight());
        sample.setMarginSize(pos.getMarginSize());
        sample.setBorderSize(pos.getBorderSize());
        sample.setBorderColor(pos.getBorderColor());
        sample.setFont(sample.getFont().deriveFont(pos.getFont().getStyle()));
        sample.setFontSize(pos.getFont().getSize());
        sample.setFontStyle(pos.getFont().getStyle());
//        sample.updateSize();

        _sample.put(state, sample);
        this.add(makeTextPanel(state, sample, editText));
        _samplePanel.add(sample);
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
    }

    @SuppressWarnings("unchecked")
    protected void makeFontPanels(Positionable comp) {
        JPanel fontPanel = new JPanel();

        Font defaultFont = comp.getFont();
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
        _fontBox.setSelectedItem(defaultFont);
        
        _fontSizeBox = new AJComboBox(FONTSIZE, SIZE);
        fontPanel.add(makeBoxPanel("FontSize", _fontSizeBox)); // NOI18N
        int row = 4;
        for (int i = 0; i < FONTSIZE.length; i++) {
            if (comp.getFont().getSize() == Integer.parseInt(FONTSIZE[i])) {
                row = i;
                break;
            }
        }
        _fontSizeBox.setSelectedIndex(row);

        _fontStyleBox = new AJComboBox(STYLES, STYLE);
        fontPanel.add(makeBoxPanel("FontStyle", _fontStyleBox)); // NOI18N
        _fontStyleBox.setSelectedIndex(comp.getFont().getStyle());

        _fontJustBox = new AJComboBox(JUSTIFICATION, JUST);
        fontPanel.add(makeBoxPanel("Justification", _fontJustBox)); // NOI18N
        switch (comp.getJustification()) {
            case PositionableJComponent.LEFT:
                row = 0;
                break;
            case PositionableJComponent.RIGHT:
                row = 2;
                break;
            case PositionableJComponent.CENTRE:
                row = 1;
                break;
            default:
                row = 2;
        }
        _fontJustBox.setSelectedIndex(row);
        this.add(fontPanel);

        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(comp.getBorderSize(), 0, 100, 1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin));
        model = new SpinnerNumberModel(comp.getMarginSize(), 0, 100, 1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin));
        model = new SpinnerNumberModel(comp.getFixedWidth(), 0, 1000, 1);
        _widthSpin = new AJSpinner(model, FWIDTH);
        sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin));
        model = new SpinnerNumberModel(comp.getFixedHeight(), 0, 1000, 1);
        _heightSpin = new AJSpinner(model, FHEIGHT);
        sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin));
        this.add(sizePanel);
    }

    String bundleCaption = null;

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
        _fontButton = makeForegroundRadioButton(state);
        p.add(_fontButton);
        
        _backgroundButton = makeBackgroundRadioButton(state);
        p.add(_backgroundButton);
        
        AJRadioButton button = makeTransparentRadioButton(state);
        p.add(button);

        _borderButton = makeBorderRadioButton(state);
        p.add(_borderButton);

        panel.add(p);
        return panel;
    }

    private AJRadioButton makeForegroundRadioButton(String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage("FontColor"), state);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    _selectedButton = FOREGROUND_BUTTON;
                    _selectedState = button._state;
                }
            }

        });
        _buttonGroup.add(button);            
        return button;
    }

    private AJRadioButton makeBackgroundRadioButton(String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage("FontBackgroundColor"), state);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    _selectedButton = BACKGROUND_BUTTON;
                    _selectedState = button._state;
                }
            }

        });
        _buttonGroup.add(button);            
        return button;
    }

    private AJRadioButton makeTransparentRadioButton(String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage("transparentBack"), state);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    _selectedButton = TRANSPARENT_BUTTON;
                    _selectedState = button._state;
                }
            }

        });
        _buttonGroup.add(button);            
        return button;
    }

    private AJRadioButton makeBorderRadioButton(String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage("borderColor"), state);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    _selectedButton = BORDERCOLOR_BUTTON;
                    _selectedState = button._state;
                }
            }

        });
        _buttonGroup.add(button);            
        return button;
    }

    protected void updateSamples() {
        if (_previewPanel == null) {
            return;            
        }
        
        Iterator<Map.Entry<String, PositionableLabel>> it = _sample.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PositionableLabel> entry = it.next();
            PositionableLabel pos = _componentMap.get(entry.getKey());
            PositionableLabel sam = entry.getValue();
            sam.setFont(pos.getFont());
            sam.setFixedWidth(pos.getFixedWidth());
            sam.setFixedHeight(pos.getFixedHeight());
            sam.setMarginSize(pos.getMarginSize());
            sam.setBorderSize(pos.getBorderSize());
            sam.setBorderColor(pos.getBorderColor());
            sam.setBackgroundColor(pos.getBackgroundColor());
            sam.setForeground(pos.getForeground());
            sam.updateSize();
        }
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
                // preview.setOpaque(false); // needed?
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

    @Override
    public void stateChanged(ChangeEvent e) {
        PositionableLabel pos = _componentMap.get(_selectedState);
        if (pos == null) {  // initial default selections call before setup is complete
            return;
        }
        Object obj = e.getSource();
        if (obj instanceof AJSpinner) {
            int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
            switch (((AJSpinner) obj)._which) {
                case BORDER:
                    pos.setBorderSize(num);
                    _borderButton.setSelected(true);
                    break;
                case MARGIN:
                    pos.setMarginSize(num);
                    _backgroundButton.setSelected(true);
                    break;
                case FWIDTH:
                    pos.setFixedWidth(num);
                    _backgroundButton.setSelected(true);
                    break;
                case FHEIGHT:
                    pos.setFixedHeight(num);
                    _backgroundButton.setSelected(true);
                    break;
                default:
                    log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                    break;
            }
        } else {
            switch (_selectedButton) {
                case FOREGROUND_BUTTON:
                    pos.setForeground(_chooser.getColor());
                    break;
                case BACKGROUND_BUTTON:
                    pos.setBackgroundColor(_chooser.getColor());
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
        }
        updateSamples();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AJComboBox comboBox = (AJComboBox)e.getSource();
        PositionableLabel pos = _componentMap.get("Text");
        if (pos == null) {  // initial default selections call before setup is complete
            return;
        }
        switch (comboBox._which) {
            case SIZE:
                String size = (String) comboBox.getSelectedItem();
                pos.setFontSize(Float.valueOf(size));
                _fontButton.setSelected(true);
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
                _fontButton.setSelected(true);
                break;
            case JUST:
                int just = 0;
                switch (comboBox.getSelectedIndex()) {
                    case 0:
                        just = PositionableJComponent.LEFT;
                        break;
                    case 1:
                        just = PositionableJComponent.CENTRE;
                        break;
                    case 2:
                        just = PositionableJComponent.RIGHT;
                        break;
                    default:
                        log.warn("Unexpected index {}  in itemStateChanged", comboBox.getSelectedIndex());
                        break;
                }
                pos.setJustification(just);
                break;
            case FONT:
                Font font = (Font) comboBox.getSelectedItem();
                pos.setFont(font);
                _fontButton.setSelected(true);
                break;
            default:
                log.warn("Unexpected _which {}  in itemStateChanged", comboBox._which);
                break;
            }
        updateSamples();
    }

    public void setAttributes(Positionable pos) {
        if (pos instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon)pos;
            Iterator<Map.Entry<String, PositionableLabel>> it = pi.getIconMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, PositionableLabel> entry = it.next();
                PositionableLabel p = _componentMap.get(entry.getKey());
                PositionableLabel sam = entry.getValue();
                sam.setText(p.getText());
                sam.setFont(p.getFont());
                sam.setFontSize(p.getFont().getSize());
                sam.setFontStyle(p.getFont().getStyle());
                sam.setJustification(p.getJustification());
                sam.setFixedWidth(p.getFixedWidth());
                sam.setFixedHeight(p.getFixedHeight());
                sam.setMarginSize(p.getMarginSize());
                sam.setBorderSize(p.getBorderSize());
                sam.setBorderColor(p.getBorderColor());
                sam.setBackgroundColor(p.getBackgroundColor());
                sam.setForeground(p.getForeground());
            }
            PositionableLabel p = _componentMap.get("Text");
            pi.setText(p.getText());
            pi.setFont(p.getFont());
            pi.setFontSize(p.getFont().getSize());
            pi.setFontStyle(p.getFont().getStyle());
            pi.setJustification(p.getJustification());
            pi.setFixedWidth(p.getFixedWidth());
            pi.setFixedHeight(p.getFixedHeight());
            pi.setMarginSize(p.getMarginSize());
            pi.setBorderSize(p.getBorderSize());
            pi.setBorderColor(p.getBorderColor());
            pi.setBackgroundColor(p.getBackgroundColor());
            pi.setForeground(p.getForeground());
        } else {
            PositionableLabel p = _componentMap.get("Text");
            if (pos instanceof PositionableLabel &&
                    !(pos instanceof jmri.jmrit.display.MemoryIcon)) {
                ((PositionableLabel) pos).setText(p.getText());
            }
            pos.setFont(p.getFont());
            pos.setFontSize(p.getFont().getSize());
            pos.setFontStyle(p.getFont().getStyle());
            pos.setJustification(p.getJustification());
            pos.setFixedWidth(p.getFixedWidth());
            pos.setFixedHeight(p.getFixedHeight());
            pos.setMarginSize(p.getMarginSize());
            pos.setBorderSize(p.getBorderSize());
            pos.setBorderColor(p.getBorderColor());
            if (p.isOpaque()) {
                pos.setBackgroundColor(p.getBackground());
                pos.setOpaque(true);
            } else {
                pos.setBackgroundColor(null);                
                pos.setOpaque(false);
            }
            pos.setForeground(p.getForeground());
        }
    }
    

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecoratorPanel.class);

}
