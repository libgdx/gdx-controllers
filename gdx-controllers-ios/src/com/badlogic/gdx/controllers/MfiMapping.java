package com.badlogic.gdx.controllers;

public class MfiMapping extends ControllerMapping {
    private static MfiMapping instance;

    MfiMapping() {
        super(0, 1, 2, 3,
                0, 1, 2, 3,
                IosController.BUTTON_BACK, IosController.BUTTON_PAUSE,
                4, 6, 5, 7,
                IosController.BUTTON_LEFT_STICK, IosController.BUTTON_RIGHT_STICK,
                IosController.BUTTON_DPAD_UP, IosController.BUTTON_DPAD_DOWN,
                IosController.BUTTON_DPAD_LEFT, IosController.BUTTON_DPAD_RIGHT);
    }

    static MfiMapping getInstance() {
        if (instance == null)
            instance = new MfiMapping();

        return instance;
    }
}
