package com.badlogic.gdx.controllers;

/**
 * This is an enumerated type for power level of controllers.
 *
 * @author Benjamin Schulte
 */
public enum ControllerPowerLevel {
    /**
     * Power level unknown
     */
    POWER_UNKNOWN,
    /**
     * Power level 0-5%
     */
    POWER_EMPTY,
    /**
     * Power level 6-20%
     */
    POWER_LOW,
    /**
     * Power level 21-70%
     */
    POWER_MEDIUM,
    /**
     * Power level 71-100%
     */
    POWER_FULL,
    /**
     * Controller is wired or charging
     */
    POWER_WIRED;
}
