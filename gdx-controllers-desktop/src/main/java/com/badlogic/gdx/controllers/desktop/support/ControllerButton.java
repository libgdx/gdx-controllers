package com.badlogic.gdx.controllers.desktop.support;

import org.lwjgl.sdl.SDLGamepad;

/**
 * The list of buttons available on a gamepad
 *
 * For controllers that use a diamond pattern for the face buttons, the
 * south/east/west/north buttons below correspond to the locations in the
 * diamond pattern. For Xbox controllers, this would be A/B/X/Y, for Nintendo
 * Switch controllers, this would be B/A/Y/X, for GameCube controllers this
 * would be A/X/B/Y, for PlayStation controllers this would be
 * Cross/Circle/Square/Triangle.
 *
 * For controllers that don't use a diamond pattern for the face buttons, the
 * south/east/west/north buttons indicate the buttons labeled A, B, C, D, or
 * 1, 2, 3, 4, or for controllers that aren't labeled, they are the primary,
 * secondary, etc. buttons.
 *
 * The activate action is often the south button and the cancel action is
 * often the east button, but in some regions this is reversed, so your game
 * should allow remapping actions based on user preferences.
 *
 * You can query the labels for the face buttons using
 * SDL_GetGamepadButtonLabel()
 * */
public enum ControllerButton {

    INVALID(SDLGamepad.SDL_GAMEPAD_BUTTON_INVALID),
    SOUTH(SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH),                    /** Bottom face button (e.g. Xbox A button) */
    EAST(SDLGamepad.SDL_GAMEPAD_BUTTON_EAST),                      /** Right face button (e.g. Xbox B button) */
    WEST(SDLGamepad.SDL_GAMEPAD_BUTTON_WEST),                      /** Left face button (e.g. Xbox X button) */
    NORTH(SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH),                    /** Top face button (e.g. Xbox Y button) */
    BACK(SDLGamepad.SDL_GAMEPAD_BUTTON_BACK),
    GUIDE(SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE),
    START(SDLGamepad.SDL_GAMEPAD_BUTTON_START),
    LEFT_STICK(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK),
    RIGHT_STICK(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK),
    LEFT_SHOULDER(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
    RIGHT_SHOULDER(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
    DPAD_UP(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP),
    DPAD_DOWN(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN),
    DPAD_LEFT(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT),
    DPAD_RIGHT(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT),
    MISC1(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC1),                    /** Additional button (e.g. Xbox Series X share button, PS5 microphone button, Nintendo Switch Pro capture button, Amazon Luna microphone button, Google Stadia capture button) */
    RIGHT_PADDLE1(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1),    /** Upper or primary paddle, under your right hand (e.g. Xbox Elite paddle P1) */
    LEFT_PADDLE1(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE1),      /** Upper or primary paddle, under your left hand (e.g. Xbox Elite paddle P3) */
    RIGHT_PADDLE2(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2),    /** Lower or secondary paddle, under your right hand (e.g. Xbox Elite paddle P2) */
    LEFT_PADDLE2(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE2),      /** Lower or secondary paddle, under your left hand (e.g. Xbox Elite paddle P4) */
    TOUCHPAD(SDLGamepad.SDL_GAMEPAD_BUTTON_TOUCHPAD),              /** PS4/PS5 touchpad button */
    MISC2(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC2),                    /** Additional button */
    MISC3(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC3),                    /** Additional button */
    MISC4(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC4),                    /** Additional button */
    MISC5(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC5),                    /** Additional button */
    MISC6(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC6),;                   /** Additional button */

    public static final ControllerButton[] VALUES = values();

    private final int id;

    ControllerButton(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ControllerButton getById(int id) {
        for (ControllerButton button : VALUES) {
            if (button.id == id)
                return button;
        }

        return null;
    }
}
