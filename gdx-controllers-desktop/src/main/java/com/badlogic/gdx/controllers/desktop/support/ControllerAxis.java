package com.badlogic.gdx.controllers.desktop.support;

import org.lwjgl.sdl.SDLGamepad;

/**
 * The list of axes available on a gamepad
 *
 * Thumbstick axis values range from SDL_JOYSTICK_AXIS_MIN to
 * SDL_JOYSTICK_AXIS_MAX, and are centered within ~8000 of zero, though
 * advanced UI will allow users to set or autodetect the dead zone, which
 * varies between gamepads.
 *
 * Trigger axis values range from 0 (released) to SDL_JOYSTICK_AXIS_MAX (fully
 * pressed) when reported by SDL_GetGamepadAxis(). Note that this is not the
 * same range that will be reported by the lower-level SDL_GetJoystickAxis().
 */
public enum ControllerAxis {
    INVALID(SDLGamepad.SDL_GAMEPAD_AXIS_INVALID),
    LEFTX(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX),
    LEFTY(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY),
    RIGHTX(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX),
    RIGHTY(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY),
    LEFT_TRIGGER(SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER),
    RIGHT_TRIGGER(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER),;

    public static final ControllerAxis[] VALUES = values();

    private final int id;

    ControllerAxis(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ControllerAxis getById(int id) {
        for (ControllerAxis axis : VALUES) {
            if (axis.id == id)
                return axis;
        }

        return null;
    }
}
