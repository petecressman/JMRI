package jmri.jmrit.display.palette;

import java.awt.Font;

public interface FontPanelListener {

    void setFontFace(Font font);

    void setFontSize(float size);

    void setFontStyle(int style);
}

