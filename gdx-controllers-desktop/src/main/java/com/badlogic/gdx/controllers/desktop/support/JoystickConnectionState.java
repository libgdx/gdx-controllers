package com.badlogic.gdx.controllers.desktop.support;

import org.lwjgl.sdl.SDLJoystick;

/**
 * Possible connection states for a joystick device.
 *
 * This is used by SDL_GetJoystickConnectionState to report how a device is
 * connected to the system.
 */
public enum JoystickConnectionState {
    INVALID(SDLJoystick.SDL_JOYSTICK_CONNECTION_INVALID),
    UNKNOWN(SDLJoystick.SDL_JOYSTICK_CONNECTION_UNKNOWN),
    WIRED(SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRED),
    WIRELESS(SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRELESS),;

    public static final JoystickConnectionState[] VALUES = values();

    private final int id;

    JoystickConnectionState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static JoystickConnectionState getById(int id) {
        for (JoystickConnectionState state : VALUES) {
            if (state.id == id)
                return state;
        }

        throw new IllegalArgumentException("No such joystick connection state with id " + id);
    }
}
