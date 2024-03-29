package com.badlogic.gdx.controllers.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.AbstractControllerManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.desktop.support.CompositeControllerListener;
import com.badlogic.gdx.controllers.desktop.support.JamepadControllerMonitor;
import com.badlogic.gdx.controllers.desktop.support.JamepadShutdownHook;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.io.IOException;

public class JamepadControllerManager extends AbstractControllerManager implements Disposable {
    // assign a Jamepad configuration to this field at game startup to override defaults
    public static com.studiohartman.jamepad.Configuration jamepadConfiguration;

    private static boolean nativeLibInitialized = false;
    private static com.studiohartman.jamepad.ControllerManager controllerManager;

    private final CompositeControllerListener compositeListener = new CompositeControllerListener();

    public JamepadControllerManager() {
        compositeListener.addListener(new ManageControllers());

        if (!nativeLibInitialized) {
            if (jamepadConfiguration == null) {
                jamepadConfiguration = new com.studiohartman.jamepad.Configuration();
            }

            controllerManager = new com.studiohartman.jamepad.ControllerManager(jamepadConfiguration);
            controllerManager.initSDLGamepad();

            JamepadControllerMonitor monitor = new JamepadControllerMonitor(controllerManager, compositeListener);
            monitor.run();

            Gdx.app.addLifecycleListener(new JamepadShutdownHook(controllerManager));

            nativeLibInitialized = true;
        }
    }

    @Override
    public void addListener(ControllerListener listener) {
        compositeListener.addListener(listener);
    }

    @Override
    public void removeListener(ControllerListener listener) {
        compositeListener.removeListener(listener);
    }

    @Override
    public Array<ControllerListener> getListeners() {
        Array<ControllerListener> array = new Array<>();
        array.add(compositeListener);
        return array;
    }

    @Override
    public void clearListeners() {
        compositeListener.clear();
        compositeListener.addListener(new ManageControllers());
    }

    @Override
    public void dispose() {
        controllerManager.quitSDLGamepad();
    }

    /**
     * @see com.studiohartman.jamepad.ControllerManager#addMappingsFromFile(String)
     */
    public static void addMappingsFromFile(String path) throws IOException, IllegalStateException {
        controllerManager.addMappingsFromFile(path);
    }

    /**
     * Writes last native SDL error to error output.
     * Note: Output might not indicate an error, but could be a warning as well.
     * Use for debugging purposes.
     */
    public static void logLastNativeGamepadError() {
        Gdx.app.error("Jamepad", controllerManager.getLastNativeError());
    }

    private class ManageControllers extends ManageCurrentControllerListener {
        @Override
        public void connected(Controller controller) {
            synchronized (controllers) {
                controllers.add(controller);
            }
            super.connected(controller);
        }

        @Override
        public void disconnected(Controller controller) {
            synchronized (controllers) {
                controllers.removeValue(controller, true);
            }
            super.disconnected(controller);
        }
    }
}
