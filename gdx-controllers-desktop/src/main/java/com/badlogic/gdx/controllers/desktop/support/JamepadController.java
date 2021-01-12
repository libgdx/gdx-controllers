package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerUnpluggedException;

import java.util.UUID;

public class JamepadController implements Controller {
    private static final IntMap<ControllerButton> CODE_TO_BUTTON = new IntMap<>(ControllerButton.values().length);
    private static final IntMap<ControllerAxis> CODE_TO_AXIS = new IntMap<>(ControllerAxis.values().length);
    private static final Logger logger = new Logger(JamepadController.class.getSimpleName());

    static {
        for (ControllerButton button : ControllerButton.values()) {
            CODE_TO_BUTTON.put(button.ordinal(), button);
        }

        for (ControllerAxis axis : ControllerAxis.values()) {
            CODE_TO_AXIS.put(axis.ordinal(), axis);
        }
    }

    private final CompositeControllerListener compositeControllerListener = new CompositeControllerListener();
    private final ControllerIndex controllerIndex;
    private final IntMap<Boolean> buttonState = new IntMap<>();
    private final IntMap<Float> axisState = new IntMap<>();
    private final String uuid;
    private boolean connected = true;
    private boolean canVibrate = true;
    private long vibrationEndMs;
    private int axisCount = -1;
    private int maxButtonIndex = -1;

    public JamepadController(ControllerIndex controllerIndex) {
        this.controllerIndex = controllerIndex;
        this.uuid = UUID.randomUUID().toString();
        initializeState();
    }

    @Override
    public boolean getButton(final int buttonCode) {
        try {
            ControllerButton button = toButton(buttonCode);
            return button != null && controllerIndex.isButtonPressed(button);
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }
        return false;
    }

    @Override
    public float getAxis(final int axisCode) {
        try {
            ControllerAxis axis = toAxis(axisCode);

            if (axis == null) {
                return 0.0f;
            } else {
                return controllerIndex.getAxisState(axis);
            }
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }
        return 0f;
    }

    @Override
    public String getName() {
        try {
            return controllerIndex.getName();
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }
        return "Unknown";
    }

    private void setDisconnected() {
        if (connected) {
            connected = false;
            logger.info("Failed querying controller at index: " + controllerIndex.getIndex());
            compositeControllerListener.disconnected(this);
        }
    }

    @Override
    public void addListener(ControllerListener listener) {
        compositeControllerListener.addListener(listener);
    }

    @Override
    public void removeListener(ControllerListener listener) {
        compositeControllerListener.removeListener(listener);
    }

    public boolean update() {
        updateButtonsState();
        updateAxisState();
        return connected;
    }

    private ControllerButton toButton(int buttonCode) {
        return CODE_TO_BUTTON.get(buttonCode);
    }

    private ControllerAxis toAxis(int axisCode) {
        return CODE_TO_AXIS.get(axisCode);
    }

    private void updateAxisState() {
        for (ControllerAxis axis : ControllerAxis.values()) {
            int id = axis.ordinal();

            float value = getAxis(id);
            if (value != axisState.get(id)) {
                if (logger.getLevel() == Logger.DEBUG) {
                    logger.debug("Axis [" + id + " - " + toAxis(id) + "] moved [" + value + "]");
                }
                compositeControllerListener.axisMoved(this, id, value);
            }
            axisState.put(id, value);
        }
    }

    private void updateButtonsState() {
        for (ControllerButton button : ControllerButton.values()) {
            int id = button.ordinal();

            boolean pressed = getButton(id);
            if (pressed != buttonState.get(id)) {
                if (pressed) {
                    compositeControllerListener.buttonDown(this, id);
                } else {
                    compositeControllerListener.buttonUp(this, id);
                }

                if (logger.getLevel() == Logger.DEBUG) {
                    logger.debug("Button [" + id + " - " + toButton(id) + "] is " + (pressed ? "pressed" : "released"));
                }
            }
            buttonState.put(id, pressed);
        }
    }

    private void initializeState() {
        for (ControllerAxis axis : ControllerAxis.values()) {
            axisState.put(axis.ordinal(), 0.0f);
        }

        for (ControllerButton button : ControllerButton.values()) {
            buttonState.put(button.ordinal(), false);
        }
    }

    @Override
    public boolean canVibrate() {
        return canVibrate;
    }

    @Override
    public boolean isVibrating() {
        return canVibrate() && TimeUtils.millis() < vibrationEndMs;
    }

    @Override
    public void startVibration(int duration, float strength) {
        try {
            if (controllerIndex.doVibration(strength, strength, duration)) {
                vibrationEndMs = TimeUtils.millis() + duration;
                canVibrate = true;
            } else {
                canVibrate = false;
            }
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }
    }

    @Override
    public void cancelVibration() {
        if (isVibrating()) {
            // starting a vibration of strength 0 cancels the last one
            startVibration(0, 0);
        }
    }

    @Override
    public String getUniqueId() {
        return uuid;
    }

    @Override
    public boolean supportsPlayerIndex() {
        return true;
    }

    @Override
    public int getPlayerIndex() {
        try {
            return controllerIndex.getPlayerIndex();
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
            return PLAYER_IDX_UNSET;
        }
    }

    @Override
    public void setPlayerIndex(int index) {
        try {
            controllerIndex.setPlayerIndex(index);
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }
    }

    @Override
    public int getMinButtonIndex() {
        return 0;
    }

    @Override
    public int getMaxButtonIndex() {
        if (maxButtonIndex >= 0) {
            return maxButtonIndex;
        }

        maxButtonIndex = CODE_TO_BUTTON.size - 1;
        try {
            while (maxButtonIndex >= 0 && !controllerIndex.isButtonAvailable(CODE_TO_BUTTON.get(maxButtonIndex))) {
                maxButtonIndex--;
            }
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }

        return maxButtonIndex;
    }

    @Override
    public int getAxisCount() {
        if (axisCount >= 0) {
            return axisCount;
        }

        axisCount = CODE_TO_AXIS.size;
        try {
            while (axisCount > 0 && !controllerIndex.isAxisAvailable(CODE_TO_AXIS.get(axisCount - 1))) {
                axisCount--;
            }
        } catch (ControllerUnpluggedException e) {
            setDisconnected();
        }

        return axisCount;
    }

    @Override
    public boolean isConnected() {
        return connected && controllerIndex.isConnected();
    }

    @Override
    public ControllerMapping getMapping() {
        return JamepadMapping.getInstance();
    }

    @Override
    public ControllerPowerLevel getPowerLevel() {
        try {
            switch (controllerIndex.getPowerLevel()) {
                case POWER_MAX:
                case POWER_FULL:
                    return ControllerPowerLevel.POWER_FULL;
                case POWER_MEDIUM:
                    return ControllerPowerLevel.POWER_MEDIUM;
                case POWER_LOW:
                    return ControllerPowerLevel.POWER_LOW;
                case POWER_EMPTY:
                    return ControllerPowerLevel.POWER_EMPTY;
                case POWER_WIRED:
                    return ControllerPowerLevel.POWER_WIRED;
                default:
                    return ControllerPowerLevel.POWER_UNKNOWN;
            }
        } catch (Throwable t) {
            return ControllerPowerLevel.POWER_UNKNOWN;
        }
    }
}
