package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.ControllerPowerLevel;
import static org.lwjgl.sdl.SDLGamepad.*;
import static org.lwjgl.sdl.SDLJoystick.*;
import static org.lwjgl.sdl.SDLProperties.*;

import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * This class is the main thing you're gonna need to deal with if you want lots of
 * control over your gamepads or want to avoid lots of ControllerState allocations.
 *
 * A Controller index cannot be made from outside the Jamepad package. You're gonna need to go
 * through a ControllerManager to get your controllers.
 *
 * A ControllerIndex represents the controller at a given index. There may or may not actually
 * be a controller at that index. Exceptions are thrown if the controller is not connected.
 *
 * @author William Hartman
 */
public final class ControllerIndex {

    private static final float AXIS_MAX_VAL = 32767;
    private int index;
    private long controllerPtr;

    private boolean[] heldDownButtons;
    private boolean[] justPressedButtons;

    /**
     * Constructor. Builds a controller at the given index and attempts to connect to it.
     * This is only accessible in the Jamepad package, so people can't go trying to make controllers
     * before the native library is loaded or initialized.
     *
     * @param index The index of the controller
     */
    ControllerIndex(int index) {
        this.index = index;

        heldDownButtons = new boolean[ControllerButton.VALUES.length];
        justPressedButtons = new boolean[ControllerButton.VALUES.length];
        for(int i = 0; i < heldDownButtons.length; i++) {
            heldDownButtons[i] = false;
            justPressedButtons[i] = false;
        }

        connectController();
    }
    private void connectController() {
        controllerPtr = SDL_OpenGamepad(index);
    }

    /**
     * Close the connection to this controller.
     */
    public void close() {
        if(controllerPtr != 0) {
            if (SDL_GamepadConnected(controllerPtr))
                SDL_CloseGamepad(controllerPtr);
            controllerPtr = 0;
        }
    }

    /**
     * Close and reconnect to the native gamepad at the index associated with this ControllerIndex object.
     * This is will refresh the gamepad represented here. This should be called if something is plugged
     * in or unplugged.
     *
     * @return whether or not the controller could successfully reconnect.
     */
    public boolean reconnectController() {
        close();
        connectController();

        return isConnected();
    }

    /**
     * Return whether or not the controller is currently connected. This first checks that the controller
     * was successfully connected to our SDL backend. Then we check if the controller is currently plugged
     * in.
     *
     * @return Whether or not the controller is plugged in.
     */
    public boolean isConnected() {
        if (controllerPtr == 0)
            return false;
        return SDL_GamepadConnected(controllerPtr);
    }

    /**
     * Returns the index of the current controller.
     * @return The index of the current controller.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return true of controller can vibrate
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public boolean canVibrate() throws ControllerUnpluggedException {
        ensureConnected();
        int propertyId = SDL_GetGamepadProperties(controllerPtr);
        return SDL_GetBooleanProperty(propertyId, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false);
    }

    /**
     * Vibrate the controller using the new rumble API
     * Each call to this function cancels any previous rumble effect, and calling it with 0 intensity stops any rumbling.
     *
     * This will return false if the controller doesn't support vibration or if SDL was unable to start
     * vibration (maybe the controller doesn't support left/right vibration, maybe it was unplugged in the
     * middle of trying, etc...)
     *
     * @param leftMagnitude The intensity of the left rumble motor (this should be between 0 and 1)
     * @param rightMagnitude The intensity of the right rumble motor (this should be between 0 and 1)
     * @return Whether or not the controller was able to be vibrated (i.e. if haptics are supported)
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public boolean doVibration(float leftMagnitude, float rightMagnitude, int duration_ms) throws ControllerUnpluggedException {
        ensureConnected();

        //Check the values are appropriate
        boolean leftInRange = leftMagnitude >= 0 && leftMagnitude <= 1;
        boolean rightInRange = rightMagnitude >= 0 && rightMagnitude <= 1;
        if(!(leftInRange && rightInRange)) {
            throw new IllegalArgumentException("The passed values are not in the range 0 to 1!");
        }

        return SDL_RumbleGamepad(controllerPtr, (short) (65535 * leftMagnitude), (short) (65535 * rightMagnitude), duration_ms);
    }

    /**
     * Returns whether or not a given button has been pressed.
     *
     * @param toCheck The ControllerButton to check the state of
     * @return Whether or not the button is pressed.
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public boolean isButtonPressed(ControllerButton toCheck) throws ControllerUnpluggedException {
        if (toCheck == ControllerButton.INVALID)
            return false;
        updateButton(toCheck.getId());
        return heldDownButtons[toCheck.getId()];
    }

    /**
     * Returns whether or not a given button has just been pressed since you last made a query
     * about that button (either through this method, isButtonPressed(), or through the ControllerState
     * side of things). If the button was not pressed the last time you checked but is now, this method
     * will return true.
     *
     * @param toCheck The ControllerButton to check the state of
     * @return Whether or not the button has just been pressed.
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public boolean isButtonJustPressed(ControllerButton toCheck) throws ControllerUnpluggedException {
        if (toCheck == ControllerButton.INVALID)
            return false;

        updateButton(toCheck.getId());
        return justPressedButtons[toCheck.getId()];
    }

    private void updateButton(int buttonIndex) throws ControllerUnpluggedException {
        if (buttonIndex < 0)
            return;
        ensureConnected();

        SDL_UpdateGamepads();

        boolean currButtonIsPressed = SDL_GetGamepadButton(controllerPtr, buttonIndex);
        justPressedButtons[buttonIndex] = (currButtonIsPressed && !heldDownButtons[buttonIndex]);
        heldDownButtons[buttonIndex] = currButtonIsPressed;
    }

    /**
     * Returns if a given button is available on controller.
     *
     * @param toCheck The ControllerButton to check
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public boolean isButtonAvailable(ControllerButton toCheck) throws ControllerUnpluggedException {
        ensureConnected();
        return SDL_GamepadHasButton(controllerPtr, toCheck.getId());
    }

    /**
     * Returns the current state of a passed axis.
     *
     * @param toCheck The ControllerAxis to check the state of
     * @return The current state of the requested axis.
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public float getAxisState(ControllerAxis toCheck) throws ControllerUnpluggedException {
        ensureConnected();

        SDL_UpdateGamepads();
        return SDL_GetGamepadAxis(controllerPtr, toCheck.getId()) / AXIS_MAX_VAL;
    }

    /**
     * Returns if passed axis is available on controller.
     *
     * @param toCheck The ControllerAxis to check
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public boolean isAxisAvailable(ControllerAxis toCheck) throws ControllerUnpluggedException {
        ensureConnected();
        return SDL_GamepadHasAxis(controllerPtr, toCheck.getId());
    }

    /**
     * Returns the implementation dependent name of this controller.
     *
     * @return The the name of this controller
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public String getName() throws ControllerUnpluggedException {
        ensureConnected();

        String controllerName = SDL_GetGamepadName(controllerPtr);

        //Return a descriptive string instead of null if the attached controller does not have a name
        if(controllerName == null) {
            return "Unnamed Controller";
        }
        return controllerName;
    }

    /**
     * Returns the instance ID of the current controller, which uniquely identifies
     * the device from the time it is connected until it is disconnected.
     *
     * @return The instance ID of the current controller
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public int getDeviceInstanceID() throws ControllerUnpluggedException {
        ensureConnected();
        long joystick = SDL_GetGamepadJoystick(controllerPtr);
        return SDL_GetJoystickID(joystick);
    }

    /**
     * @return player index if set and supported, -1 otherwise
     */
    public int getPlayerIndex() throws ControllerUnpluggedException {
        ensureConnected();
        return SDL_GetGamepadPlayerIndex(controllerPtr);
    }

    /**
     * Sets player index. At the time being, this doesn't seem to change the indication lights on
     * a controller on Windows, Linux and Mac, but only an internal representation index.
     * @param index index to set
     */
    public void setPlayerIndex(int index) throws ControllerUnpluggedException {
        ensureConnected();
        SDL_SetGamepadPlayerIndex(controllerPtr, index);
    }

    /**
     * @return current power level of game controller, see {@link ControllerPowerLevel} enum values
     * @throws ControllerUnpluggedException If the controller is not connected
     */
    public ControllerPowerLevel getPowerLevel() throws ControllerUnpluggedException {
        ensureConnected();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer powerLevel = stack.mallocInt(1);

            long joystick = SDL_GetGamepadJoystick(controllerPtr);
            PowerState powerState = PowerState.getById(SDL_GetJoystickPowerInfo(joystick, powerLevel));
            int level = powerLevel.get(0);

            switch (powerState) {
                case ERROR:
                case UNKNOWN:
                    return ControllerPowerLevel.POWER_UNKNOWN;
                case NO_BATTERY:
                case CHARGING:
                case CHARGED:
                    return ControllerPowerLevel.POWER_WIRED;
            }

            if (level == -1)
                return ControllerPowerLevel.POWER_UNKNOWN;
            else if (level <= 5)
                return ControllerPowerLevel.POWER_EMPTY;
            else if (level <= 20)
                return ControllerPowerLevel.POWER_LOW;
            else if (level <= 70)
                return ControllerPowerLevel.POWER_MEDIUM;
            else
                return ControllerPowerLevel.POWER_FULL;
        }
    }

    /**
     * Convenience method to throw an exception if the controller is not connected.
     */
    private void ensureConnected() throws ControllerUnpluggedException {
        if(!isConnected()) {
            throw new ControllerUnpluggedException("Controller at index " + index + " is not connected!");
        }
    }
}
