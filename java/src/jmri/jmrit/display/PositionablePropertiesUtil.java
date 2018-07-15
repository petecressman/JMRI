package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import jmri.util.swing.SplitButtonColorChooserPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the UI to set the properties of a range of Positionable Icons on
 * (Control) Panels.
 */
public class PositionablePropertiesUtil {

    Frame mFrame = null;
    protected Positionable _parent;
    JPanel detailpanel = new JPanel();
    JTabbedPane propertiesPanel;

    PositionablePropertiesUtil(Positionable p) {
        _parent = p;
    }

    public void display() {
        propertiesPanel = new JTabbedPane();
        getCurrentValues();
        JPanel exampleHolder = new JPanel();
        //example = new JLabel(text);

        for (int i = 0; i < _textMap.size(); i++) {
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createTitledBorder(_textMap.get(i).getDescription()));
            p.add(_textMap.get(i).getLabel()); // add a visual example for each
            exampleHolder.add(p);
        }
        //exampleHolder.add(example);
        JPanel tmp = new JPanel();

        tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
        tmp.add(propertiesPanel);
        tmp.add(detailpanel);
        tmp.add(exampleHolder);
        textPanel();
        editText();
        borderPanel();
        sizePosition();

        JPanel _buttonArea = new JPanel();

        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        _buttonArea.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            undoChanges();
            mFrame.dispose();
        });

        JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));
        _buttonArea.add(applyButton);
        applyButton.addActionListener((ActionEvent e) -> {
            fontApply();
        });

        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
        _buttonArea.add(okButton);
        okButton.addActionListener((ActionEvent e) -> {
            fontApply();
            mFrame.dispose();
        });
        tmp.add(_buttonArea);

        exampleHolder.setBackground(_parent.getParent().getBackground());
        mFrame = new JFrame(_parent.getNameString());
        mFrame.add(tmp);
        mFrame.pack();
        mFrame.setVisible(true);
        preview();
    }

    JComponent _textPanel;

    JTextField fontSizeField;

    String[] _justification = {Bundle.getMessage("left"), Bundle.getMessage("right"), Bundle.getMessage("center")};
    JComboBox<String> _justificationCombo;

    /**
     * Create and fill in the Font (Decoration) tab of the UI.
     */
    void textPanel() {
        _textPanel = new JPanel();
        _textPanel.setLayout(new BoxLayout(_textPanel, BoxLayout.Y_AXIS));
        JPanel fontColorPanel = new JPanel();
        fontColorPanel.add(new JLabel(Bundle.getMessage("FontColor") + ": "));

        JPanel fontSizePanel = new JPanel();
        fontSizePanel.setLayout(new BoxLayout(fontSizePanel, BoxLayout.Y_AXIS));
        fontSizeChoice = new JList<>(fontSizes);

        fontSizeChoice.setSelectedValue("" + fontSize, true);
        fontSizeChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(fontSizeChoice);
        listScroller.setPreferredSize(new Dimension(60, 80));

        JPanel FontPanel = new JPanel();
        fontSizeField = new JTextField("" + fontSize, fontSizeChoice.getWidth());
        fontSizeField.addKeyListener(previewKeyActionListener);
        fontSizePanel.add(fontSizeField);
        fontSizePanel.add(listScroller);
        FontPanel.add(fontSizePanel);

        JPanel Style = new JPanel();
        Style.setLayout(new BoxLayout(Style, BoxLayout.Y_AXIS));
        Style.add(bold);
        Style.add(italic);
        FontPanel.add(Style);
        _textPanel.add(FontPanel);

        JPanel justificationPanel = new JPanel();
        _justificationCombo = new JComboBox<>(_justification);
        switch (justification) {
            case 0x00:
                _justificationCombo.setSelectedIndex(0);
                break;
            case 0x02:
                _justificationCombo.setSelectedIndex(1);
                break;
            default:
                _justificationCombo.setSelectedIndex(2);
                break;
        }
        justificationPanel.add(new JLabel(Bundle.getMessage("Justification") + ": "));
        justificationPanel.add(_justificationCombo);
        _textPanel.add(justificationPanel);

        _justificationCombo.addActionListener(previewActionListener);
        bold.addActionListener(previewActionListener);
        italic.addActionListener(previewActionListener);
        //fontSizeChoice.addActionListener(previewActionListener);
        fontSizeChoice.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            fontSizeField.setText(fontSizeChoice.getSelectedValue());
            preview();
        });

        for (int i = 0; i < _textMap.size(); i++) { // repeat 4 times for sensor icons, or just once
            final int x = i;

            JPanel txtPanel = new JPanel();

            JColorChooser txtColorChooser = new JColorChooser(defaultForeground);
            txtColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel txtColorPanels[] = { new SplitButtonColorChooserPanel()};
            txtColorChooser.setChooserPanels(txtColorPanels);
            txtColorChooser.getSelectionModel().addChangeListener(previewChangeListener);
            txtPanel.add(txtColorChooser);
            txtColorChooser.getSelectionModel().addChangeListener((ChangeEvent ce) -> {
                _textMap.get(x).setForeground(txtColorChooser.getColor());
            });

            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("FontColor") + ": "));
            p.add(txtColorChooser);

            txtPanel.add(p);

            defaultBackground = _parent.getBackground();
            JColorChooser txtBackColorChooser = new JColorChooser(defaultBackground);
            txtBackColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel txtBackColorPanels[] = { new SplitButtonColorChooserPanel()};
            txtBackColorChooser.setChooserPanels(txtBackColorPanels);
            txtBackColorChooser.getSelectionModel().addChangeListener(previewChangeListener);
            txtPanel.add(txtBackColorChooser);
            txtBackColorChooser.getSelectionModel().addChangeListener((ChangeEvent ce) -> {
                _textMap.get(x).setBackground(txtBackColorChooser.getColor());
            });
            p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("FontBackgroundColor") + ": "));
            p.add(txtBackColorChooser);

            String _borderTitle = _textMap.get(i).getDescription();
            if (_borderTitle.equals(Bundle.getMessage("TextExampleLabel"))) {
                _borderTitle = Bundle.getMessage("TextDecoLabel"); // replace default label by an appropriate one for text decoration box on Font tab
            }
            txtPanel.setBorder(BorderFactory.createTitledBorder(_borderTitle));
            txtPanel.add(p);

            _textPanel.add(txtPanel);

        }
        propertiesPanel.addTab(Bundle.getMessage("FontTabTitle"), null, _textPanel, Bundle.getMessage("FontTabTooltip"));
    }

    ActionListener previewActionListener = (ActionEvent actionEvent) -> {
        preview();
    };

    ChangeListener spinnerChangeListener = (ChangeEvent actionEvent) -> {
        preview();
    };

    FocusListener textFieldFocus = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
            JTextField tmp = (JTextField) e.getSource();
            if (tmp.getText().equals("")) {
                tmp.setText("0");
                preview();
            }
        }
    };

    KeyListener previewKeyActionListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent E) {
        }

        @Override
        public void keyPressed(KeyEvent E) {
        }

        @Override
        public void keyReleased(KeyEvent E) {
            JTextField tmp = (JTextField) E.getSource();
            if (!tmp.getText().equals("")) {
                preview();
            }
        }
    };

    ChangeListener previewChangeListener = (ChangeEvent ce) -> {
        preview();
    };

    private JColorChooser borderColorChooser = null;
    javax.swing.JSpinner borderSizeTextSpin;
    javax.swing.JSpinner marginSizeTextSpin;

    /**
     * Create and fill in the Border tab of the UI.
     */
    void borderPanel() {
        JPanel borderPanel = new JPanel();

        borderColorChooser = new JColorChooser(defaultBorderColor);
        AbstractColorChooserPanel borderColorPanels[] = { new SplitButtonColorChooserPanel()};
        borderColorChooser.setChooserPanels(borderColorPanels);
        borderColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel

        borderColorChooser.getSelectionModel().addChangeListener(previewChangeListener);

        JPanel borderColorPanel = new JPanel();
        borderColorPanel.add(new JLabel(Bundle.getMessage("borderColor") + ": "));
        borderColorPanel.add(borderColorChooser);

        JPanel borderSizePanel = new JPanel();
        borderSizeTextSpin = getSpinner(borderSize, Bundle.getMessage("borderSize"));
        borderSizeTextSpin.addChangeListener(spinnerChangeListener);
        borderSizePanel.add(new JLabel(Bundle.getMessage("borderSize") + ": "));
        borderSizePanel.add(borderSizeTextSpin);

        JPanel marginSizePanel = new JPanel();
        marginSizeTextSpin = getSpinner(marginSize, Bundle.getMessage("marginSize"));
        marginSizeTextSpin.addChangeListener(spinnerChangeListener);

        marginSizePanel.add(new JLabel(Bundle.getMessage("marginSize") + ": "));
        marginSizePanel.add(marginSizeTextSpin);

        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));
        borderPanel.add(borderColorPanel);
        borderPanel.add(borderSizePanel);
        borderPanel.add(marginSizePanel);

        propertiesPanel.addTab(Bundle.getMessage("Border"), null, borderPanel, Bundle.getMessage("BorderTabTooltip"));

    }

    javax.swing.JSpinner xPositionTextSpin;
    javax.swing.JSpinner yPositionTextSpin;
    javax.swing.JSpinner widthSizeTextSpin;
    javax.swing.JSpinner heightSizeTextSpin;
    JCheckBox autoWidth;

    /**
     * Create and fill in the Contents tab of the UI (Text Label objects).
     */
    void editText() {
        JPanel editText = new JPanel();
        editText.setLayout(new BoxLayout(editText, BoxLayout.Y_AXIS));
        for (int i = 0; i < _textMap.size(); i++) {
            final int x = i;
            JPanel p = new JPanel();

            String _borderTitle = _textMap.get(i).getDescription();
            if (_borderTitle.equals(Bundle.getMessage("TextExampleLabel"))) {
                _borderTitle = Bundle.getMessage("TextBorderLabel"); // replace label provided by Ctor by an appropriate one for text string box on Contents tab
            }
            p.setBorder(BorderFactory.createTitledBorder(_borderTitle));

            JLabel txt = new JLabel(Bundle.getMessage("TextValueLabel") + ": ");
            JTextField textField = new JTextField(_textMap.get(i).getText(), 20);
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent E) {
                }

                @Override
                public void keyPressed(KeyEvent E) {
                }

                @Override
                public void keyReleased(KeyEvent E) {
                    JTextField tmp = (JTextField) E.getSource();
                    _textMap.get(x).setText(tmp.getText());
                    preview();
                }
            });
            p.add(txt);
            p.add(textField);
            editText.add(p);
        }
        propertiesPanel.addTab(Bundle.getMessage("EditTextLabel"), null, editText, Bundle.getMessage("EditTabTooltip"));
    }

    /**
     * Create and fill in the Size &amp; Position tab of the UI.
     */
    void sizePosition() {

        JPanel posPanel = new JPanel();

        JPanel xyPanel = new JPanel();
        xyPanel.setLayout(new BoxLayout(xyPanel, BoxLayout.Y_AXIS));
        JPanel xPanel = new JPanel();
        JLabel txt = new JLabel(" X: ");
        xPositionTextSpin = getSpinner(xPos, "x position");
        xPositionTextSpin.addChangeListener(spinnerChangeListener);
        xPanel.add(txt);
        xPanel.add(xPositionTextSpin);

        JPanel yPanel = new JPanel();
        txt = new JLabel(" Y: ");
        yPositionTextSpin = getSpinner(yPos, "y position");
        yPositionTextSpin.addChangeListener(spinnerChangeListener);
        yPanel.add(txt);
        yPanel.add(yPositionTextSpin);

        xyPanel.add(xPanel);
        xyPanel.add(yPanel);

        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.Y_AXIS));
        JPanel widthPanel = new JPanel();
        widthSizeTextSpin = getSpinner(fixedWidth, Bundle.getMessage("width"));
        widthSizeTextSpin.addChangeListener(spinnerChangeListener);
        /*widthSizeText = new JTextField(""+fixedWidth, 10);
         widthSizeText.addKeyListener(previewKeyActionListener);*/
        txt = new JLabel(Bundle.getMessage("width") + ": ");
        widthPanel.add(txt);
        widthPanel.add(widthSizeTextSpin);

        JPanel heightPanel = new JPanel();
        /*heightSizeText = new JTextField(""+fixedHeight, 10);
         heightSizeText.addKeyListener(previewKeyActionListener);*/
        heightSizeTextSpin = getSpinner(fixedHeight, Bundle.getMessage("height"));
        heightSizeTextSpin.addChangeListener(spinnerChangeListener);
        txt = new JLabel(Bundle.getMessage("height") + ": ");
        heightPanel.add(txt);
        heightPanel.add(heightSizeTextSpin);

        sizePanel.add(widthPanel);
        sizePanel.add(heightPanel);

        posPanel.add(xyPanel);
        posPanel.add(sizePanel);
        posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.Y_AXIS));

        propertiesPanel.addTab(Bundle.getMessage("SizeTabTitle"), null, posPanel, Bundle.getMessage("SizeTabTooltip"));
    }

    void fontApply() {
        _parent.setFontSize(Integer.parseInt(fontSizeField.getText()));
        if (bold.isSelected()) {
            _parent.setFontStyle(Font.BOLD, 0);
        } else {
            _parent.setFontStyle(0, Font.BOLD);
        }
        if (italic.isSelected()) {
            _parent.setFontStyle(Font.ITALIC, 0);
        } else {
            _parent.setFontStyle(0, Font.ITALIC);
        }

        Color desiredColor;
        if (_parent instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon) _parent;
            if (pi.isIcon() && pi.isText()) {   // text overlaid icon
                TextDetails det = _textMap.get("parent");
                pi.setText(det.getText());
                pi.setForeground(det.getForeground());
                pi.setBackgroundColor(det.getBackground());
            } else {
                for (Map.Entry<String, PositionableLabel> entry : pi.getIconMap().entrySet()) {
                    String state = entry.getKey();
                    TextDetails det = _textMap.get(state);
                    PositionableLabel p = entry.getValue();
                    p.setText(det.getText());
                    p.setForeground(det.getForeground());
                    p.setBackground(det.getBackground());
                }
            }
        } else if (_parent instanceof PositionableLabel) {
            PositionableLabel pp = (PositionableLabel) _parent;
            TextDetails det = _textMap.get("parent");
            pp.setText(det.getText());
            pp.setForeground(det.getForeground());
            pp.setBackground(det.getBackground());
        } else if (_parent instanceof PositionableJPanel) {
            PositionableJPanel pj = (PositionableJPanel)_parent;
            TextDetails det = _textMap.get("noText");
            pj.setForeground(det.getForeground());
            pj.setBackground(det.getBackground());
        } else {
            TextDetails det = _textMap.get("noText");
            _parent.setForeground(det.getForeground());
            _parent.setBackground(det.getBackground());
        }

        desiredColor = borderColorChooser.getColor();
        _parent.setBorderColor(desiredColor);

        _parent.setBorderSize(((Number) borderSizeTextSpin.getValue()).intValue());

        _parent.setMarginSize(((Number) marginSizeTextSpin.getValue()).intValue());
        _parent.setLocation(((Number) xPositionTextSpin.getValue()).intValue(), ((Number) yPositionTextSpin.getValue()).intValue());
        _parent.setFixedWidth(((Number) widthSizeTextSpin.getValue()).intValue());
        _parent.setFixedHeight(((Number) heightSizeTextSpin.getValue()).intValue());
        switch (_justificationCombo.getSelectedIndex()) {
            case 0:
                _parent.setJustification(0x00);
                break;
            case 1:
                _parent.setJustification(0x02);
                break;
            case 2:
                _parent.setJustification(0x04);
                break;
            default:
                log.warn("Unhandled combo index: {}", _justificationCombo.getSelectedIndex());
                break;
        }
    }

    void cancelButton() {
        mFrame.dispose();
    }

    void preview() {
        int attrs = Font.PLAIN;
        if (bold.isSelected()) {
            attrs = Font.BOLD;
        }
        if (italic.isSelected()) {
            attrs |= Font.ITALIC;
        }

        Font newFont = new Font(_parent.getFont().getName(), attrs, Integer.parseInt(fontSizeField.getText()));

        Color desiredColor;

        desiredColor = borderColorChooser.getColor();
        Border borderMargin;
        int margin = ((Number) marginSizeTextSpin.getValue()).intValue();
        Border outlineBorder;
        if (desiredColor != null) {
            outlineBorder = new LineBorder(desiredColor, ((Number) borderSizeTextSpin.getValue()).intValue());
        } else {
            outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        }
        int hoz = 0;
        switch (_justificationCombo.getSelectedIndex()) {
            case 0:
                hoz = (0x02);
                break;
            case 1:
                hoz = (0x04);
                break;
            case 2:
                hoz = (0x00);
                break;
            default:
                log.warn("Unhandled combo index: {}", _justificationCombo.getSelectedIndex());
                break;
        }

        for (int i = 0; i < _textMap.size(); i++) {
            JLabel tmp = _textMap.get(i).getLabel();
            if (tmp.isOpaque()) {
                borderMargin = new LineBorder(tmp.getBackground(), margin);
            } else {
                borderMargin = BorderFactory.createEmptyBorder(margin, margin, margin, margin);
            }
            tmp.setFont(newFont);
            tmp.setHorizontalAlignment(hoz);
            tmp.setBorder(new CompoundBorder(outlineBorder, borderMargin));
            tmp.setSize(new Dimension(maxWidth(tmp), maxHeight(tmp)));
            tmp.setPreferredSize(new Dimension(maxWidth(tmp), maxHeight(tmp)));

        }
        mFrame.pack();
    }

    int maxWidth(JLabel tmp) {
        int max = 0;
        if (((Number) widthSizeTextSpin.getValue()).intValue() != 0) {
            max = ((Number) widthSizeTextSpin.getValue()).intValue();
            max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
        } else {
            if (tmp.getText().trim().length() > 0) {
                max = tmp.getFontMetrics(tmp.getFont()).stringWidth(tmp.getText());
            }
            max += ((Number) marginSizeTextSpin.getValue()).intValue() * 2;
            max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
        }
        return max;
    }

    public int maxHeight(JLabel tmp) {
        int max = 0;
        if (((Number) heightSizeTextSpin.getValue()).intValue() != 0) {
            max = ((Number) heightSizeTextSpin.getValue()).intValue();
            max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
        } else {
            if (tmp.getText().trim().length() > 0) {
                max = tmp.getFontMetrics(tmp.getFont()).getHeight();
            }
            max += ((Number) marginSizeTextSpin.getValue()).intValue() * 2;
            max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
        }

        return max;
    }

    private void undoChanges() {
        if (_parent instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon) _parent;
            if (pi.isIcon() && pi.isText()) {   // text overlaid icon
                TextDetails det = _textMap.get("parent");
                pi.setText(det.getOrigText());
                pi.setForeground(det.getOrigForeground());
                pi.setBackgroundColor(det.getOrigBackground());
            } else {
                for (Map.Entry<String, PositionableLabel> entry : pi.getIconMap().entrySet()) {
                    String state = entry.getKey();
                    TextDetails det = _textMap.get(state);
                    PositionableLabel p = entry.getValue();
                    p.setText(det.getOrigText());
                    p.setForeground(det.getOrigForeground());
                    p.setBackground(det.getOrigBackground());
                }
            }
        } else if (_parent instanceof PositionableLabel) {
            PositionableLabel pp = (PositionableLabel) _parent;
            TextDetails det = _textMap.get("parent");
            pp.setText(det.getOrigText());
            pp.setForeground(det.getOrigForeground());
            pp.setBackground(det.getOrigBackground());
        } else if (_parent instanceof PositionableJPanel) {
            PositionableJPanel pj = (PositionableJPanel)_parent;
            TextDetails det = _textMap.get("noText");
            pj.setForeground(det.getOrigForeground());
            pj.setBackground(det.getOrigBackground());
        } else {
            TextDetails det = _textMap.get("noText");
            _parent.setForeground(det.getOrigForeground());
            _parent.setBackground(det.getOrigBackground());
        }
        _parent.setJustification(justification);
        _parent.setFixedWidth(fixedWidth);
        _parent.setFixedHeight(fixedHeight);
        _parent.setMarginSize(marginSize);
        _parent.setBorderSize(borderSize);
        _parent.setFontStyle(0, fontStyle);
        _parent.setFontSize(fontSize);
        _parent.setBorderColor(defaultBorderColor);
        _parent.setLocation(xPos, yPos);
//        _parent.rotate(deg);
    }

    private void getCurrentValues() {
        _textMap = new HashMap<>();

        if (_parent instanceof PositionableIcon) {
            PositionableIcon pi = (PositionableIcon) _parent;
            if (pi.isIcon() && pi.isText()) {   // text overlaid icon
                // just 1 label Example
                _textMap.put("parent", new TextDetails(Bundle.getMessage("TextExampleLabel"), pi.getText(), pi.getForeground(), pi.getBackgroundColor()));
            } else {
                for (Map.Entry<String, PositionableLabel> entry : pi.getIconMap().entrySet()) {
                    String state = entry.getKey();
                    PositionableLabel p = entry.getValue();
                    _textMap.put(state, new TextDetails(Bundle.getMessage(state), p.getText(), p.getForeground(), p.getBackgroundColor()));                    
                }
            }
        } else if (_parent instanceof PositionableLabel) {
            PositionableLabel p = (PositionableLabel)_parent;
            _textMap.put("parent", new TextDetails(Bundle.getMessage("TextExampleLabel"), p.getText(), p.getForeground(), p.getBackgroundColor()));
        } else if (_parent instanceof PositionableJPanel) {
            PositionableJPanel pj = (PositionableJPanel)_parent;
            _textMap.put("noText", new TextDetails(Bundle.getMessage("TextExampleLabel"), pj.getText(), pj.getForeground(), pj.getBackgroundColor()));
        } else {
            // just 1 label Example
            _textMap.put("noText", new TextDetails(Bundle.getMessage("TextExampleLabel"), null, _parent.getForeground(), _parent.getBackgroundColor()));
        }

        fixedWidth = _parent.getFixedWidth();
        fixedHeight = _parent.getFixedHeight();
        marginSize = _parent.getMarginSize();
        borderSize = _parent.getBorderSize();
        justification = _parent.getJustification();
        fontStyle = _parent.getFont().getStyle();
        fontSize = _parent.getFont().getSize();
        if ((Font.BOLD & fontStyle) == Font.BOLD) {
            bold.setSelected(true);
        }
        if ((Font.ITALIC & fontStyle) == Font.ITALIC) {
            italic.setSelected(true);
        }
        if (_parent.isOpaque()) {
            defaultBackground = _parent.getBackground();
        }
        defaultForeground = _parent.getForeground();
        defaultBorderColor = _parent.getBorderColor();
        if (_parent instanceof MemoryIcon) {
            MemoryIcon pm = (MemoryIcon) _parent;
            xPos = pm.getOriginalX();
            yPos = pm.getOriginalY();
        } else {
            xPos = _parent.getX();
            yPos = _parent.getY();
        }
    }
    private int fontStyle;
    private Color defaultForeground = Color.black;
    private Color defaultBackground;
    private Color defaultBorderColor = Color.black;
    private int fixedWidth = 0;
    private int fixedHeight = 0;
    private int marginSize = 0;
    private int borderSize = 0;
    private int justification;
    private int fontSize;
    private int xPos;
    private int yPos;

    private HashMap<String, TextDetails> _textMap = null;

    private final JCheckBox italic = new JCheckBox(Bundle.getMessage("Italic"), false);
    private final JCheckBox bold = new JCheckBox(Bundle.getMessage("Bold"), false);

    protected JList<String> fontSizeChoice;

    protected String fontSizes[] = {"6", "8", "10", "11", "12", "14", "16",
        "20", "24", "28", "32", "36"};

    javax.swing.JSpinner getSpinner(int value, String tooltip) {
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1000, 1);
        javax.swing.JSpinner spinX = new javax.swing.JSpinner(model);
        spinX.setValue(value);
        spinX.setToolTipText(tooltip);
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        return spinX;
    }

    static class TextDetails {

        TextDetails(String desc, String txt, Color fore, Color back) {
            if (txt == null) {
                text = "";
                // contents of icon state labels <active> are entered in SensorIcon.java
            } else {
                text = txt;
            }
            description = desc;
            example = new JLabel(text);
            setForeground(fore);
            setBackground(back);
            origForeground = fore;
            origBackground = back;
            origText = txt;
        }

        Color foreground;
        Color background;
        Color origForeground;
        Color origBackground;
        String origText;
        String text;
        JLabel example;
        String description;

        Color getForeground() {
            return foreground;
        }

        Color getBackground() {
            return background;
        }

        String getText() {
            return text;
        }

        Color getOrigForeground() {
            return origForeground;
        }

        Color getOrigBackground() {
            return origBackground;
        }

        String getOrigText() {
            return origText;
        }

        String getDescription() {
            return description;
        }

        void setForeground(Color fore) {
            foreground = fore;
            example.setForeground(fore);
        }

        void setBackground(Color back) {
            background = back;
            if (back != null) {
                example.setOpaque(true);
                example.setBackground(back);
            } else {
                example.setOpaque(false);
            }
        }

        void setText(String txt) {
            text = txt;
            example.setText(txt);
        }

        JLabel getLabel() {
            return example;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(PositionablePropertiesUtil.class);
}
