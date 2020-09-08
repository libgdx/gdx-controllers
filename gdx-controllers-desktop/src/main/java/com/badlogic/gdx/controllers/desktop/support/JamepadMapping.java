package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.ControllerMapping;
import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;

public class JamepadMapping extends ControllerMapping {
    private static JamepadMapping instance;

    JamepadMapping() {
        super(ControllerAxis.LEFTX.ordinal(), ControllerAxis.LEFTY.ordinal(),
                ControllerAxis.RIGHTX.ordinal(), ControllerAxis.RIGHTY.ordinal(),
                ControllerButton.A.ordinal(), ControllerButton.B.ordinal(),
                ControllerButton.X.ordinal(), ControllerButton.Y.ordinal(),
                ControllerButton.BACK.ordinal(), ControllerButton.START.ordinal(),
                ControllerButton.LEFTBUMPER.ordinal(), UNDEFINED,
                ControllerButton.RIGHTBUMPER.ordinal(), UNDEFINED,
                ControllerButton.LEFTSTICK.ordinal(), ControllerButton.RIGHTSTICK.ordinal());
    }

    static JamepadMapping getInstance() {
        if (instance == null)
            instance = new JamepadMapping();

        return instance;
    }
}
