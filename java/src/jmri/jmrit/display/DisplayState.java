package jmri.jmrit.display;

import java.awt.Color;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* A data structure of the parameters needed to display the
* state of a multi-state panel object, such as a turnout, sensor, etc.
* @author Pete Cressman Copyright (C) 2018
*/
public class DisplayState {

    private String _textString;    // state name
    private NamedIcon _namedIcon;  // state icon

    private Color _foregroundColor = null;
    private Color _backgroundColor = null;
    private Color _borderColor = null;

    public DisplayState() {
    }

    DisplayState(String t, NamedIcon i, Color fore, Color back, Color bord) {
        _textString = t;
        _namedIcon = i;

        _foregroundColor = fore;
        _backgroundColor = back;
        _borderColor = bord;
    }

    public void setText(String t) {
        _textString = t;
    }

    public String getText() {
        return _textString;
    }

    public void setIcon(NamedIcon i) {
        _namedIcon = i;
    }

    public NamedIcon getIcon() {
        return _namedIcon;
    }

    public void setForeground(Color c) {
        _foregroundColor = c;
    }

    public Color getForeground() {
        return _foregroundColor;
    }

    public void setBackground(Color c) {
        _backgroundColor = c;
    }

    public Color getBackground() {
        return _backgroundColor;
    }

    public void setBorderColor(Color c) {
        _borderColor = c;
    }

    public Color getBorderColor() {
        return _borderColor;
    }

    DisplayState deepClone() {
        return new DisplayState(_textString, _namedIcon,
                _foregroundColor, _backgroundColor, _borderColor);
    }

    public void setDisplayParameters(PositionableLabel pos) {
        pos.setText(_textString);
        pos.setIcon(_namedIcon);
        pos.setForeground(_foregroundColor);
        pos.setBackground(_backgroundColor);
        pos.setBorderColor(_borderColor);
    }

    public void setParametersOf(DisplayState pos, boolean all) {
        if (all) {
            pos.setText(_textString);
            pos.setIcon(_namedIcon);
        }
        pos.setForeground(_foregroundColor);
        pos.setBackground(_backgroundColor);
        pos.setBorderColor(_borderColor);
    }

//    private final static Logger log = LoggerFactory.getLogger(DisplayState.class);
}