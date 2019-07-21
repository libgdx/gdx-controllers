package com.badlogic.gdx.controllers;

/**
 * Default axis and button constants returned by {@link AdvancedController#getMapping}
 */
public class ControllerMapping {
    public static final int UNDEFINED = -1;

    public final int axisLeftX;
    public final int axisLeftY;
    public final int axisRightX;
    public final int axisRightY;

    public final int buttonA;
    public final int buttonB;
    public final int buttonX;
    public final int buttonY;
    public final int buttonBack;
    public final int buttonStart;

    public final int buttonL1;
    public final int buttonL2;
    public final int buttonR1;
    public final int buttonR2;

    public final int buttonLeftStick;
    public final int buttonRightStick;

    protected ControllerMapping(int axisLeftX, int axisLeftY, int axisRightX, int axisRightY,
                                int buttonA, int buttonB, int buttonX, int buttonY, int buttonBack, int buttonStart,
                                int buttonL1, int buttonL2, int buttonR1, int buttonR2,
                                int buttonLeftStick, int buttonRightStick) {
        this.axisLeftX = axisLeftX;
        this.axisLeftY = axisLeftY;
        this.axisRightX = axisRightX;
        this.axisRightY = axisRightY;

        this.buttonA = buttonA;
        this.buttonB = buttonB;
        this.buttonX = buttonX;
        this.buttonY = buttonY;
        this.buttonBack = buttonBack;
        this.buttonStart = buttonStart;
        this.buttonL1 = buttonL1;
        this.buttonL2 = buttonL2;
        this.buttonR1 = buttonR1;
        this.buttonR2 = buttonR2;
        this.buttonLeftStick = buttonLeftStick;
        this.buttonRightStick = buttonRightStick;
    }
}
