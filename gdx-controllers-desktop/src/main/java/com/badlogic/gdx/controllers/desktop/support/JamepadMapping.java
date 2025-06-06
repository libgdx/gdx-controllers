package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.ControllerMapping;

public class JamepadMapping extends ControllerMapping {
    private static JamepadMapping instance;

    JamepadMapping() {
        super(ControllerAxis.LEFTX.getId(), ControllerAxis.LEFTY.getId(),
                ControllerAxis.RIGHTX.getId(), ControllerAxis.RIGHTY.getId(),
                ControllerButton.SOUTH.getId(), ControllerButton.EAST.getId(),
                ControllerButton.WEST.getId(), ControllerButton.NORTH.getId(),
                ControllerButton.BACK.getId(), ControllerButton.START.getId(),
                ControllerButton.LEFT_SHOULDER.getId(), UNDEFINED,
                ControllerButton.RIGHT_SHOULDER.getId(), UNDEFINED,
                ControllerButton.LEFT_STICK.getId(), ControllerButton.LEFT_SHOULDER.getId(),
                ControllerButton.DPAD_UP.getId(), ControllerButton.DPAD_DOWN.getId(),
                ControllerButton.DPAD_LEFT.getId(), ControllerButton.DPAD_RIGHT.getId());
    }

    static JamepadMapping getInstance() {
        if (instance == null)
            instance = new JamepadMapping();

        return instance;
    }
}
