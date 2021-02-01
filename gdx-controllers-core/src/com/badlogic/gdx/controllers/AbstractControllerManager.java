package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.Array;

public abstract class AbstractControllerManager implements ControllerManager {
    protected final Array<Controller> controllers = new Array<>();

    @Override
    public Array<Controller> getControllers () {
        return controllers;
    }
}
