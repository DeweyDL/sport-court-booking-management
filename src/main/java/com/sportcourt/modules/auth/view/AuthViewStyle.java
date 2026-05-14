package com.sportcourt.modules.auth.view;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Color;

final class AuthViewStyle {
    static final int INPUT_LINE_THICKNESS = 1;
    private static final Color INPUT_LINE_COLOR = new Color(200, 200, 200);

    private AuthViewStyle() {
    }

    static Border createInputUnderlineBorder() {
        return BorderFactory.createMatteBorder(0, 0, INPUT_LINE_THICKNESS, 0, INPUT_LINE_COLOR);
    }
}
