package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

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
