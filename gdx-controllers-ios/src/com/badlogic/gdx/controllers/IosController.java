package com.badlogic.gdx.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import org.robovm.apple.gamecontroller.GCController;
import org.robovm.apple.gamecontroller.GCControllerAxisInput;
import org.robovm.apple.gamecontroller.GCControllerButtonInput;
import org.robovm.apple.gamecontroller.GCControllerDirectionPad;
import org.robovm.apple.gamecontroller.GCControllerElement;
import org.robovm.apple.gamecontroller.GCExtendedGamepad;
import org.robovm.apple.gamecontroller.GCGamepad;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.objc.block.VoidBlock2;

public class IosController implements Controller, Disposable {
    public final static int BUTTON_PAUSE = 9;

    private final GCController controller;
    private final Array<ControllerListener> listeners = new Array<>();

    public IosController(GCController controller) {
        this.controller = controller;
        controller.retain();
        controller.setControllerPausedHandler(new VoidBlock1<GCController>() {
            @Override
            public void invoke(GCController gcController) {
                onPauseButtonPressed();
            }
        });
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
    }

    @Override
    public void dispose() {
    	synchronized (listeners) {
			listeners.clear();
		}
        controller.setControllerPausedHandler(null);
        if (controller.getExtendedGamepad() != null)
            controller.getExtendedGamepad().setValueChangedHandler(null);
        if (controller.getGamepad() != null)
            controller.getGamepad().setValueChangedHandler(null);

        controller.release();
    }

    protected void onPauseButtonPressed() {
        if (listeners.size > 0 || Controllers.getListeners().size > 0) {
            notifyListenersButtonDown(BUTTON_PAUSE);
            notifyListenersButtonUp(BUTTON_PAUSE);
        } else {
            // TODO cache the button until next call to getButton(BUTTON_PAUSE)
        }
    }

    protected void onControllerValueChanged(GCControllerElement gcControllerElement) {
        if (gcControllerElement instanceof GCControllerButtonInput) {
            GCControllerButtonInput buttonElement = (GCControllerButtonInput) gcControllerElement;
            boolean pressed = buttonElement.isPressed();
            int buttonNum = getConstFromButtonInput(buttonElement);
            if (buttonNum >= 0) {
                if (pressed)
                    notifyListenersButtonDown(buttonNum);
                else
                    notifyListenersButtonUp(buttonNum);
            }

        } else if (gcControllerElement instanceof GCControllerAxisInput) {
            //TODO

        } else if (gcControllerElement instanceof GCControllerDirectionPad) {
            //TODO
        }
    }

    private void notifyListenersButtonUp(int button) {
		Array<ControllerListener> managerListeners = Controllers.getListeners();
		synchronized (managerListeners) {
			for (ControllerListener listener : managerListeners) {
				if (listener.buttonUp(this, button))
					break;
			}
		}

		synchronized (this.listeners) {
			for (ControllerListener listener : this.listeners) {
				if (listener.buttonUp(this, button))
					break;
			}
		}
    }

    protected void notifyListenersButtonDown(int button) {
		Array<ControllerListener> managerListeners = Controllers.getListeners();
		synchronized (managerListeners) {
			for (ControllerListener listener : managerListeners) {
				if (listener.buttonDown(this, button))
					break;
			}
		}

		synchronized (listeners) {
			for (ControllerListener listener : listeners) {
				if (listener.buttonDown(this, button))
					break;
			}
		}
    }

    /**
     * @return constant from button, following W3C recommendations. -1 if not found
     */
    protected int getConstFromButtonInput(GCControllerButtonInput controllerButtonInput) {
        int maxButtonNum = getMaxButtonNum();
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
            case 8:
                // Back
            case 9:
                // Start
            case 10:
                // Left stick button
            case 11:
                // right stick button
                return null;

            // 12-15: DPad
        }

        return null;
    }

    public int getMaxButtonNum() {
        return Math.max(7, BUTTON_PAUSE);
    }

    @Override
    public boolean getButton(int i) {
        GCControllerButtonInput buttonFromConst = getButtonFromConst(i);

        if (buttonFromConst != null)
            return buttonFromConst.isPressed();
        else
            return false;
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
    public PovDirection getPov(int i) {
        GCControllerDirectionPad dpad = null;

        if (i == 0) {
            if (controller.getExtendedGamepad() != null)
                dpad = controller.getExtendedGamepad().getDpad();
            else
                dpad = controller.getGamepad().getDpad();
        }

        if (dpad == null)
            return PovDirection.center;
        else if (dpad.getDown().isPressed() && dpad.getLeft().isPressed())
            return PovDirection.southWest;
        else if (dpad.getLeft().isPressed() && dpad.getUp().isPressed())
            return PovDirection.northWest;
        else if (dpad.getUp().isPressed() && dpad.getRight().isPressed())
            return PovDirection.northEast;
        else if (dpad.getRight().isPressed() && dpad.getDown().isPressed())
            return PovDirection.southEast;
        else if (dpad.getDown().isPressed() && !dpad.getUp().isPressed())
            return PovDirection.south;
        else if (dpad.getUp().isPressed() && !dpad.getUp().isPressed())
            return PovDirection.north;
        else if (dpad.getLeft().isPressed() && !dpad.getRight().isPressed())
            return PovDirection.west;
        else if (dpad.getRight().isPressed() && !dpad.getLeft().isPressed())
            return PovDirection.east;
        else
            return PovDirection.center;
    }

    @Override
    public boolean getSliderX(int i) {
        return false;
    }

    @Override
    public boolean getSliderY(int i) {
        return false;
    }

    @Override
    public Vector3 getAccelerometer(int i) {
        // TODO
        return Vector3.Zero;
    }

    @Override
    public void setAccelerometerSensitivity(float v) {
        // not supported
    }

    @Override
    public String getName() {
        return controller.getVendorName();
    }

    @Override
    public void addListener(ControllerListener controllerListener) {
		synchronized (listeners) {
			if (!listeners.contains(controllerListener, true))
				listeners.add(controllerListener);
		}
    }

    @Override
    public void removeListener(ControllerListener controllerListener) {
		synchronized (listeners) {
			listeners.removeValue(controllerListener, true);
		}
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IosController && ((IosController) o).getController() == controller);
    }

    public GCController getController() {
        return controller;
    }
}
