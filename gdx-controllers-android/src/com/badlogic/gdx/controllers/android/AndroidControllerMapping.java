package com.badlogic.gdx.controllers.android;

import android.view.KeyEvent;

import com.badlogic.gdx.controllers.ControllerMapping;

public class AndroidControllerMapping extends ControllerMapping {
    private static AndroidControllerMapping instance;

    AndroidControllerMapping() {
        super(0, 1, 2, 3,
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_START,
                KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_L2,
                KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2,
                KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR,
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    static AndroidControllerMapping getInstance() {
        if (instance == null)
            instance = new AndroidControllerMapping();

        return instance;
    }
}
