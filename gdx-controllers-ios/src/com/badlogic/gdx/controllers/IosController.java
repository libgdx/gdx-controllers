package com.badlogic.gdx.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

import org.robovm.apple.corehaptic.CHHapticEngine;
import org.robovm.apple.corehaptic.CHHapticEvent;
import org.robovm.apple.corehaptic.CHHapticEventParameter;
import org.robovm.apple.corehaptic.CHHapticEventParameterID;
import org.robovm.apple.corehaptic.CHHapticEventType;
import org.robovm.apple.corehaptic.CHHapticParameterCurve;
import org.robovm.apple.corehaptic.CHHapticPattern;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.gamecontroller.GCController;
import org.robovm.apple.gamecontroller.GCControllerAxisInput;
import org.robovm.apple.gamecontroller.GCControllerButtonInput;
import org.robovm.apple.gamecontroller.GCControllerDirectionPad;
import org.robovm.apple.gamecontroller.GCControllerElement;
import org.robovm.apple.gamecontroller.GCControllerPlayerIndex;
import org.robovm.apple.gamecontroller.GCExtendedGamepad;
import org.robovm.apple.gamecontroller.GCGamepad;
import org.robovm.apple.gamecontroller.GCHapticsLocality;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.objc.block.VoidBlock2;

import java.util.UUID;

public class IosController extends AbstractController {
    public static final int BUTTON_BACK = 8;
    public final static int BUTTON_PAUSE = 9;
    public static final int BUTTON_LEFT_STICK = 10;
    public static final int BUTTON_RIGHT_STICK = 11;
    public static final int BUTTON_DPAD_UP = 12;
    public static final int BUTTON_DPAD_DOWN = 13;
    public static final int BUTTON_DPAD_LEFT = 14;
    public static final int BUTTON_DPAD_RIGHT = 15;

    private final GCController controller;
    private final String uuid;
    private final boolean[] pressedButtons;
    private long lastPausePressedMs = 0;

    private CHHapticEngine hapticEngine;

    public IosController(GCController controller) {
        this.controller = controller;
        uuid = UUID.randomUUID().toString();

        pressedButtons = new boolean[getMaxButtonIndex() + 1];

        controller.retain();
        if (Foundation.getMajorSystemVersion() < 13) {
            controller.setControllerPausedHandler(new VoidBlock1<GCController>() {
                @Override
                public void invoke(GCController gcController) {
                    onPauseButtonPressed();
                }
            });
        }
        if (controller.getExtendedGamepad() != null)
            controller.getExtendedGamepad().setValueChangedHandler(new VoidBlock2<GCExtendedGamepad, GCControllerElement>() {
                @Override
                public void invoke(GCExtendedGamepad gcExtendedGamepad, GCControllerElement gcControllerElement) {
                    onControllerValueChanged(gcControllerElement);
                }
            });
        else if (controller.getGamepad() != null)
            controller.getGamepad().setValueChangedHandler(new VoidBlock2<GCGamepad, GCControllerElement>() {
                @Override
                public void invoke(GCGamepad gcGamepad, GCControllerElement gcControllerElement) {
                    onControllerValueChanged(gcControllerElement);
                }
            });

        if (Foundation.getMajorSystemVersion() >= 14) try {
            hapticEngine = controller.getHaptics().createEngine(GCHapticsLocality.Default);
            hapticEngine.retain();
        } catch (Throwable t) {
            Gdx.app.error("Controllers", "Failed to create haptics engine", t);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        controller.setControllerPausedHandler(null);
        if (controller.getExtendedGamepad() != null)
            controller.getExtendedGamepad().setValueChangedHandler(null);
        else if (controller.getGamepad() != null)
            controller.getGamepad().setValueChangedHandler(null);

        controller.release();
        if (hapticEngine != null) {
            hapticEngine.release();
        }
    }

    protected void onPauseButtonPressed() {
        lastPausePressedMs = TimeUtils.millis();
        notifyListenersButtonDown(BUTTON_PAUSE);
        notifyListenersButtonUp(BUTTON_PAUSE);
    }

    protected void onControllerValueChanged(GCControllerElement gcControllerElement) {
        if (gcControllerElement instanceof GCControllerButtonInput) {
            GCControllerButtonInput buttonElement = (GCControllerButtonInput) gcControllerElement;
            boolean pressed = buttonElement.isPressed();
            int buttonNum = getConstFromButtonInput(buttonElement);
            if (buttonNum >= 0 && pressedButtons[buttonNum] != pressed) {
                pressedButtons[buttonNum] = pressed;
                if (pressed)
                    notifyListenersButtonDown(buttonNum);
                else
                    notifyListenersButtonUp(buttonNum);
            }

        } else if (gcControllerElement instanceof GCControllerAxisInput) {
            GCControllerAxisInput axisInput = (GCControllerAxisInput) gcControllerElement;
            int axisNum = getConstFromAxisInput(axisInput);
            notifyListenersAxisMoved(axisNum, axisInput.getValue());

        } else if (gcControllerElement instanceof GCControllerDirectionPad) {

            // some dpad button changed, cycle to find them all
            for (int buttonNum = BUTTON_DPAD_UP; buttonNum <= BUTTON_DPAD_RIGHT; buttonNum++) {
                GCControllerButtonInput dpadButton = getButtonFromConst(buttonNum);
                if (dpadButton != null && pressedButtons[buttonNum] != dpadButton.isPressed()) {
                    pressedButtons[buttonNum] = dpadButton.isPressed();
                    if (pressedButtons[buttonNum])
                        notifyListenersButtonDown(buttonNum);
                    else
                        notifyListenersButtonUp(buttonNum);
                }
            }
        }
    }

    /**
     * @return constant from button, following W3C recommendations. -1 if not found
     */
    protected int getConstFromButtonInput(GCControllerButtonInput controllerButtonInput) {
        int maxButtonNum = getMaxButtonIndex();
        for (int i = 0; i < maxButtonNum; i++) {
            GCControllerButtonInput buttonFromConst = getButtonFromConst(i);
            if (buttonFromConst != null && controllerButtonInput == buttonFromConst)
                return i;
        }

        if (controllerButtonInput != null)
            Gdx.app.log("Controllers", "Pressed unknown button: " +
                    controllerButtonInput.toString());

        return -1;
    }

    /**
     * @return button from constant, following W3C recommendations
     */
    protected GCControllerButtonInput getButtonFromConst(int i) {
        switch (i) {
            case 0:
                // Button A
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getButtonA();
                else
                    return controller.getGamepad().getButtonA();
            case 1:
                // Button B
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getButtonB();
                else
                    return controller.getGamepad().getButtonB();
            case 2:
                // Button X
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getButtonX();
                else
                    return controller.getGamepad().getButtonX();
            case 3:
                // Button Y
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getButtonY();
                else
                    return controller.getGamepad().getButtonY();
            case 4:
                // L1
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getLeftShoulder();
                else
                    return controller.getGamepad().getLeftShoulder();
            case 5:
                // R1
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getRightShoulder();
                else
                    return controller.getGamepad().getRightShoulder();
            case 6:
                // L2
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getLeftTrigger();
                break;
            case 7:
                // R2
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getRightTrigger();
                break;
            case BUTTON_BACK:
                // Back
                if (Foundation.getMajorSystemVersion() >= 13 && controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getButtonOptions();
                }
                break;
            case BUTTON_PAUSE:
                // Start
                if (Foundation.getMajorSystemVersion() >= 13 && controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getButtonMenu();
                }
                break;
            case BUTTON_LEFT_STICK:
                // Left stick button
                if (Foundation.getMajorSystemVersion() >= 13 && controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getLeftThumbstickButton();
                }
                break;
            case BUTTON_RIGHT_STICK:
                // right stick button
                if (Foundation.getMajorSystemVersion() >= 13 && controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getRightThumbstickButton();
                }
                break;
            case BUTTON_DPAD_UP:
                // Dpad up
                if (controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getDpad().getUp();
                } else {
                    return controller.getGamepad().getDpad().getUp();
                }
            case BUTTON_DPAD_DOWN:
                // dpad down
                if (controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getDpad().getDown();
                } else {
                    return controller.getGamepad().getDpad().getDown();
                }
            case BUTTON_DPAD_LEFT:
                // dpad left
                if (controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getDpad().getLeft();
                } else {
                    return controller.getGamepad().getDpad().getLeft();
                }
            case BUTTON_DPAD_RIGHT:
                // dpad right
                if (controller.getExtendedGamepad() != null) {
                    return controller.getExtendedGamepad().getDpad().getRight();
                } else {
                    return controller.getGamepad().getDpad().getRight();
                }
        }

        return null;
    }

    @Override
    public int getMinButtonIndex() {
        return 0;
    }

    @Override
    public int getMaxButtonIndex() {
        return Math.max(BUTTON_DPAD_RIGHT, BUTTON_PAUSE);
    }

    @Override
    public boolean getButton(int i) {
        GCControllerButtonInput buttonFromConst = getButtonFromConst(i);

        if (i == BUTTON_PAUSE && buttonFromConst == null) {
            if (lastPausePressedMs > 0 && (TimeUtils.millis() - lastPausePressedMs) <= 250) {
                lastPausePressedMs = 0;
                return true;
            } else
                return false;
        } else if (buttonFromConst != null)
            return buttonFromConst.isPressed();
        else
            return false;
    }

    protected int getConstFromAxisInput(GCControllerAxisInput axis) {
        for (int i = 0; i <= 3; i++) {
            if (getAxisFromConst(i) == axis)
                return i;
        }

        return -1;
    }

    protected GCControllerAxisInput getAxisFromConst(int i) {
        switch (i) {
            case 0:
                // Left X
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getLeftThumbstick().getXAxis();
                break;

            case 1:
                // Left Y
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getLeftThumbstick().getYAxis();
                break;

            case 2:
                // Right X
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getRightThumbstick().getXAxis();
                break;

            case 3:
                // Right Y
                if (controller.getExtendedGamepad() != null)
                    return controller.getExtendedGamepad().getRightThumbstick().getYAxis();
                break;
        }

        return null;
    }

    @Override
    public float getAxis(int i) {
        GCControllerAxisInput axisFromConst = getAxisFromConst(i);

        if (axisFromConst != null)
            return axisFromConst.getValue();

        return 0;
    }

    @Override
    public String getName() {
        return controller.getVendorName();
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
        GCControllerPlayerIndex playerIndex = controller.getPlayerIndex();
        return playerIndex != null ? (int) playerIndex.value() : PLAYER_IDX_UNSET;
    }

    @Override
    public void setPlayerIndex(int index) {
        controller.setPlayerIndex(GCControllerPlayerIndex.valueOf(index));
    }

    @Override
    public int getAxisCount() {
        return controller.getExtendedGamepad() != null ? 4 : 0;
    }

    @Override
    public boolean canVibrate() {
        return hapticEngine != null;
    }

    @Override
    public void startVibration(int duration, float strength) {
        if (canVibrate()) {
            try {
                hapticEngine.start(null);
                hapticEngine.createPlayer(constructRumbleEvent((float) duration / 1000, strength)).start(0, null);
            } catch (Throwable t) {
                Gdx.app.error("Controllers", "Vibration failed", t);
            }
        }
    }

    @Override
    public ControllerMapping getMapping() {
        return MfiMapping.getInstance();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IosController && ((IosController) o).getController() == controller);
    }

    public GCController getController() {
        return controller;
    }

    public CHHapticPattern constructRumbleEvent(float length, float strength) throws NSErrorException {
        NSArray<CHHapticEventParameter> params = new NSArray<>(new CHHapticEventParameter(CHHapticEventParameterID.HapticIntensity, strength),
                new CHHapticEventParameter(CHHapticEventParameterID.HapticSharpness, .5f));
        return new CHHapticPattern(new NSArray<>(new CHHapticEvent(CHHapticEventType.HapticContinuous, params, 0, length)),
                new NSArray<CHHapticParameterCurve>());
    }
}
