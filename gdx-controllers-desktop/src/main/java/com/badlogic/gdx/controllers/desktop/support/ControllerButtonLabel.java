package com.badlogic.gdx.controllers.desktop.support;

import org.lwjgl.sdl.SDLGamepad;

/**
 * The set of gamepad button labels
 *
 * This isn't a complete set, just the face buttons to make it easy to show
 * button prompts.
 *
 * For a complete set, you should look at the button and gamepad type and have
 * a set of symbols that work well with your art style.
 */
public enum ControllerButtonLabel {
    UNKNOWN(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_UNKNOWN),
    A(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_A),
    B(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_B),
    X(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_X),
    Y(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_Y),
    CROSS(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_CROSS),
    CIRCLE(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_CIRCLE),
    SQUARE(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_SQUARE),
    TRIANGLE(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_TRIANGLE),;

    public static final ControllerButtonLabel[] VALUES = values();

    private final int id;

    ControllerButtonLabel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ControllerButtonLabel getById(int id) {
        for (ControllerButtonLabel label : VALUES) {
            if (label.id == id)
                return label;
        }

        return null;
    }
}
