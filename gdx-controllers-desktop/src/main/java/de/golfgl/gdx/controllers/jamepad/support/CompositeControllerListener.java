package de.golfgl.gdx.controllers.jamepad.support;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

import java.util.LinkedList;

public class CompositeControllerListener implements ControllerListener {
    private final LinkedList<ControllerListener> listeners = new LinkedList<>();

    @Override
    public void connected(Controller controller) {
        for (ControllerListener listener : listeners) {
            listener.connected(controller);
        }
    }

    @Override
    public void disconnected(Controller controller) {
        for (ControllerListener listener : listeners) {
            listener.disconnected(controller);
        }
    }

    @Override
    public boolean buttonDown(final Controller controller, final int buttonCode) {
        for (ControllerListener listener : listeners) {
            if (listener.buttonDown(controller, buttonCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean buttonUp(final Controller controller, final int buttonCode) {
        for (ControllerListener listener : listeners) {
            if (listener.buttonUp(controller, buttonCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
        for (ControllerListener listener : listeners) {
            if (listener.axisMoved(controller, axisCode, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean povMoved(final Controller controller, final int povCode, final PovDirection value) {
        for (ControllerListener listener : listeners) {
            if (listener.povMoved(controller, povCode, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean xSliderMoved(final Controller controller, final int sliderCode, final boolean value) {
        for (ControllerListener listener : listeners) {
            if (listener.xSliderMoved(controller, sliderCode, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean ySliderMoved(final Controller controller, final int sliderCode, final boolean value) {
        for (ControllerListener listener : listeners) {
            if (listener.ySliderMoved(controller, sliderCode, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accelerometerMoved(final Controller controller, final int accelerometerCode, final Vector3 value) {
        for (ControllerListener listener : listeners) {
            if (listener.accelerometerMoved(controller, accelerometerCode, value)) {
                return true;
            }
        }
        return false;
    }

    public void addListener(ControllerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ControllerListener listener) {
        listeners.remove(listener);
    }

    public void clear() {
        listeners.clear();
    }
}
