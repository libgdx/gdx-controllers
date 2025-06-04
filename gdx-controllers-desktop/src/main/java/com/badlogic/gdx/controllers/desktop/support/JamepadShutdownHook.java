package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.LifecycleListener;

public class JamepadShutdownHook implements LifecycleListener {
    private final SDLControllerManager controllerManager;

    public JamepadShutdownHook(SDLControllerManager controllerManager) {
        this.controllerManager = controllerManager;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        controllerManager.quitSDLGamepad();
    }
}
