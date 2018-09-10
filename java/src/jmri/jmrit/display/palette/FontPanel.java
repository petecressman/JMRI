package jmri.jmrit.display.palette;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

public class FontPanel extends JPanel implements ItemListener {

    static final String[] STYLES = {Bundle.getMessage("Plain"),
        Bundle.getMessage("Bold"),
        Bundle.getMessage("Italic"),
        Bundle.getMessage("Bold/italic")};

    static final String[] FONTSIZE = {"6", "8", "10", "11", "12", "14", "16",
            "20", "24", "28", "32", "36"};

    public static final int SIZE = 1;
    public static final int STYLE = 2;
    public static final int FACE = 3;

    private AJComboBox _fontFaceBox;
    private AJComboBox _fontSizeBox;
    private AJComboBox _fontStyleBox;

    HashMap<Object, Font> _fontMap;
    FontPanelListener _listener;

    static class AJSpinner extends JSpinner {
        int _which;

        AJSpinner(SpinnerModel model, int which) {
            super(model);
            _which = which;
        }
    }

    @SuppressWarnings("unchecked")
    static class AJComboBox extends JComboBox<String> {
        int _which;

        AJComboBox(String[] items, int which) {
            super(items);
            _which = which;
        }
    }

    public FontPanel(FontPanelListener listener) {
        _listener = listener;
        makeFontPanels();
    }

    public static JPanel makeBoxPanel(String caption, AJComboBox box, ItemListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        box.addItemListener(listener);
        panel.add(box);
        return panel;
    }

    class FontListRenderer extends DefaultListCellRenderer {

        private final HashMap<Object, Font> _map;

        FontListRenderer(HashMap<Object, Font> map) {
            _map = map;
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Font font = _map.get(value);
            label.setFont(font);
            return label;
        }
    }

    @SuppressWarnings("unchecked")
    private void makeFontPanels() {
        
        JPanel fontPanel = new JPanel();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontFamilyNames[] = ge.getAvailableFontFamilyNames();

        _fontMap = new HashMap<>();
        String[] fontNames = new String[fontFamilyNames.length];
        int k = 0;
        for (String fontFamilyName : fontFamilyNames) {
            Font font = new Font(fontFamilyName, Font.PLAIN, 12);
            fontNames[k++] = fontFamilyName;
            _fontMap.put(fontFamilyName, font);
        }
        _fontFaceBox = new AJComboBox(fontNames, FACE);
        fontPanel.add(makeBoxPanel("EditFont", _fontFaceBox, this)); // NOI18N
        _fontFaceBox.setRenderer(new FontListRenderer(_fontMap));

        _fontSizeBox = new AJComboBox(FONTSIZE, SIZE);
        fontPanel.add(makeBoxPanel("FontSize", _fontSizeBox, this)); // NOI18N

        _fontStyleBox = new AJComboBox(STYLES, STYLE);
        fontPanel.add(makeBoxPanel("FontStyle", _fontStyleBox, this)); // NOI18N

        this.add(fontPanel);
    }
    
    public void setFontSelections(Font font) {
        int size = font.getSize();
        for (int i = 0; i < FONTSIZE.length; i++) {
            if (size == Integer.parseInt(FONTSIZE[i])) {
                size = i;
                break;
            }
        }
        int style = font.getStyle();
        _fontSizeBox.setSelectedIndex(size);
        _fontStyleBox.setSelectedIndex(style);
        _fontFaceBox.setSelectedItem(font.getFamily());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AJComboBox comboBox = (AJComboBox)e.getSource();
        switch (comboBox._which) {
            case SIZE:
                _listener.setFontSize(Float.valueOf((String)comboBox.getSelectedItem()));
                break;
            case STYLE:
                _listener.setFontStyle(getStyle());
                break;
            case FACE:
                Font font = _fontMap.get(comboBox.getSelectedItem());
                float size = Float.valueOf((String)_fontSizeBox.getSelectedItem());
                _listener.setFontFace(font.deriveFont(getStyle(), size));
               break;
            default:
                log.warn("Unexpected _which {}  in itemStateChanged", comboBox._which);
                break;
        }
    }

    private int getStyle() {
        switch (_fontStyleBox.getSelectedIndex()) {
            case 0:
                return Font.PLAIN;
            case 1:
                return Font.BOLD;
            case 2:
                return Font.ITALIC;
            case 3:
                return (Font.BOLD | Font.ITALIC);
            default:
                log.warn("Unexpected index {} from _fontStyleBox", _fontStyleBox.getSelectedIndex());
                return Font.PLAIN;
        }
    }    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FontPanel.class);
}
