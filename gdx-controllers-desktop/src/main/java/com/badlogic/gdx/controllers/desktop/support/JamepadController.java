package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.UUID;

public class JamepadController implements Controller {
    private static final Logger logger = new Logger(JamepadController.class.getSimpleName());

    private final CompositeControllerListener compositeControllerListener = new CompositeControllerListener();
    private final IntIntMap buttonState = new IntIntMap(); // there is no IntBoolMap, so we do "1" = true, "0" = false
    private final IntFloatMap axisState = new IntFloatMap();
    private final String uuid;
    private final String name;
    private ControllerIndex controllerIndex;
    private boolean connected = true;
    private Boolean canVibrate = null;
    private long vibrationEndMs;
    private int axisCount = -1;
    private int maxButtonIndex = -1;

    public JamepadController(ControllerIndex controllerIndex) {
        this.controllerIndex = controllerIndex;
        this.uuid = UUID.randomUUID().toString();
        this.name = getInitialName();
        initializeState();
    }

    @Override
    public boolean getButton(final int buttonCode) {
        try {
            ControllerButton button = ControllerButton.getById(buttonCode);
            return button != null && controllerIndex.isButtonPressed(button);
        } catch (ControllerUnpluggedException | NullPointerException e) {
            setDisconnected();
        }
        return false;
    }

    @Override
    public float getAxis(final int axisCode) {
        try {
            ControllerAxis axis = ControllerAxis.getById(axisCode);

            if (axis == null) {
                return 0.0f;
            } else {
                return controllerIndex.getAxisState(axis);
            }
        } catch (ControllerUnpluggedException | NullPointerException e) {
            setDisconnected();
        }
        return 0f;
    }

    private String getInitialName() {
        try {
            return controllerIndex.getName();
        } catch (ControllerUnpluggedException | NullPointerException e) {
            // this is only called in the constructor, so disconnecting here wouldn't make sense
        }
        return "Unknown";
    }

    @Override
    public String getName() {
        return name;
    }

    public void setControllerIndex(ControllerIndex controllerIndex) {
        this.controllerIndex = controllerIndex;
    }

    public void setDisconnected() {
        if (connected) {
            connected = false;
            if (controllerIndex != null) {
                logger.info("Failed querying controller at index: " + controllerIndex.getIndex());
            }
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

    private void updateAxisState() {
        for (ControllerAxis axis : ControllerAxis.VALUES) {
            int id = axis.ordinal();

            float value = getAxis(id);
            if (value != axisState.get(id, Float.NaN)) {
                if (logger.getLevel() == Logger.DEBUG) {
                    logger.debug("Axis [" + id + " - " + ControllerAxis.getById(id) + "] moved [" + value + "]");
                }
                compositeControllerListener.axisMoved(this, id, value);
            }
            axisState.put(id, value);
        }
    }

    private void updateButtonsState() {
        for (ControllerButton button : ControllerButton.VALUES) {
            int id = button.ordinal();

            boolean pressed = getButton(id);
            boolean oldState = buttonState.get(id, -1) == 1;
            if (pressed != oldState) {
                if (pressed) {
                    compositeControllerListener.buttonDown(this, id);
                } else {
                    compositeControllerListener.buttonUp(this, id);
                }

                if (logger.getLevel() == Logger.DEBUG) {
                    logger.debug("Button [" + id + " - " + ControllerButton.getById(id) + "] is " + (pressed ? "pressed" : "released"));
                }
            }
            buttonState.put(id, pressed ? 1 : 0);
        }
    }

    private void initializeState() {
        for (ControllerAxis axis : ControllerAxis.VALUES) {
            axisState.put(axis.ordinal(), 0.0f);
        }

        for (ControllerButton button : ControllerButton.VALUES) {
            buttonState.put(button.ordinal(), 0);
        }
    }

    @Override
    public boolean canVibrate() {
        if (canVibrate == null) {
            try {
                canVibrate = controllerIndex.canVibrate();
            } catch (ControllerUnpluggedException | NullPointerException e) {
                setDisconnected();
            }
        }

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
            }
        } catch (ControllerUnpluggedException | NullPointerException e) {
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
        } catch (ControllerUnpluggedException | NullPointerException e) {
            setDisconnected();
            return PLAYER_IDX_UNSET;
        }
    }

    @Override
    public void setPlayerIndex(int index) {
        try {
            controllerIndex.setPlayerIndex(index);
        } catch (ControllerUnpluggedException | NullPointerException e) {
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

        // TODO: 04.06.2025 I don't think this code makes any sense, but I might be mistaken
        maxButtonIndex = ControllerButton.VALUES.length - 1;
        try {
            while (maxButtonIndex > 0 && !controllerIndex.isButtonAvailable(ControllerButton.VALUES[maxButtonIndex])) {
                maxButtonIndex--;
            }
        } catch (ControllerUnpluggedException | NullPointerException e) {
            setDisconnected();
        }

        return maxButtonIndex;
    }

    @Override
    public int getAxisCount() {
        if (axisCount >= 0) {
            return axisCount;
        }

        axisCount = 0;
        try {
            for (ControllerAxis axis : ControllerAxis.VALUES) {
                if (axis == ControllerAxis.INVALID)
                    continue;

                if (controllerIndex.isAxisAvailable(axis))
                    axisCount++;

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
            return controllerIndex.getPowerLevel();
        } catch (ControllerUnpluggedException e) {
            return ControllerPowerLevel.POWER_UNKNOWN;
        }
    }
}
