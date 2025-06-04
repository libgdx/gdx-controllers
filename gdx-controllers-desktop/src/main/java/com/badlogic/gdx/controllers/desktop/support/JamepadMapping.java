package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.ControllerMapping;

public class JamepadMapping extends ControllerMapping {
    private static JamepadMapping instance;

    JamepadMapping() {
        super(ControllerAxis.LEFTX.ordinal(), ControllerAxis.LEFTY.ordinal(),
                ControllerAxis.RIGHTX.ordinal(), ControllerAxis.RIGHTY.ordinal(),
                ControllerButton.SOUTH.ordinal(), ControllerButton.EAST.ordinal(),
                ControllerButton.WEST.ordinal(), ControllerButton.NORTH.ordinal(),
                ControllerButton.BACK.ordinal(), ControllerButton.START.ordinal(),
                ControllerButton.LEFT_SHOULDER.ordinal(), UNDEFINED,
                ControllerButton.RIGHT_SHOULDER.ordinal(), UNDEFINED,
                ControllerButton.LEFT_STICK.ordinal(), ControllerButton.LEFT_SHOULDER.ordinal(),
                ControllerButton.DPAD_UP.ordinal(), ControllerButton.DPAD_DOWN.ordinal(),
                ControllerButton.DPAD_LEFT.ordinal(), ControllerButton.DPAD_RIGHT.ordinal());
    }

    static JamepadMapping getInstance() {
        if (instance == null)
            instance = new JamepadMapping();

        return instance;
    }
}
