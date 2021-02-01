package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.Array;

public abstract class AbstractControllerManager implements ControllerManager {
    protected final Array<Controller> controllers = new Array<>();
    private Controller currentController;

    @Override
    public Array<Controller> getControllers () {
        return controllers;
    }

    @Override
    public Controller getCurrentController() {
        return currentController;
    }

    /**
     * Manages currentController field. Must be added to controller listeners as first listener
     */
    public class ManageCurrentControllerListener extends ControllerAdapter {

        @Override
        public void connected(Controller controller) {
            if (currentController == null) {
                currentController = controller;
            }
        }

        @Override
        public void disconnected(Controller controller) {
            if (currentController == controller) {
                currentController = null;
            }
        }

        @Override
        public boolean buttonDown(Controller controller, int buttonIndex) {
            currentController = controller;
            return false;
        }

        @Override
        public boolean buttonUp(Controller controller, int buttonIndex) {
            currentController = controller;
            return false;
        }

        @Override
        public boolean axisMoved(Controller controller, int axisIndex, float value) {
            currentController = controller;
            return false;
        }
    }
}
