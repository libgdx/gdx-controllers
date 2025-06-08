package com.badlogic.gdx.controllers.desktop.support;

import org.lwjgl.sdl.SDLPower;

/**
 * The basic state for the system's power supply.
 *
 * These are results returned by SDL_GetPowerInfo().
 */
public enum PowerState {
    ERROR(SDLPower.SDL_POWERSTATE_ERROR),                    /** error determining power status */
    UNKNOWN(SDLPower.SDL_POWERSTATE_UNKNOWN),                /** cannot determine power status */
    ON_BATTERY(SDLPower.SDL_POWERSTATE_ON_BATTERY),          /** Not plugged in, running on the battery */
    NO_BATTERY(SDLPower.SDL_POWERSTATE_NO_BATTERY),          /** Plugged in, no battery available */
    CHARGING(SDLPower.SDL_POWERSTATE_CHARGING),              /** Plugged in, charging battery */
    CHARGED(SDLPower.SDL_POWERSTATE_CHARGED),;               /** Plugged in, battery charged */

    public static final PowerState[] VALUES = values();

    private final int id;

    PowerState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PowerState getById(int id) {
        for (PowerState state : VALUES) {
            if (state.id == id)
                return state;
        }

        throw new IllegalArgumentException("No such power state with id " + id);
    }
}
