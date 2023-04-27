package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.controllers.desktop.JamepadControllerManager;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;

public class JamepadControllerMonitor implements Runnable {
    private final ControllerManager controllerManager;
    private final ControllerListener listener;
    private final IntMap<Tuple> indexToController
        = new IntMap<>(JamepadControllerManager.jamepadConfiguration.maxNumControllers);
    // temporary array for delaying connect messages
    private final Array<JamepadController> connectedControllers = new Array<JamepadController>();

    public JamepadControllerMonitor(ControllerManager controllerManager, ControllerListener listener) {
        this.controllerManager = controllerManager;
        this.listener = listener;

        reconcileControllers();
    }

    @Override
    public void run() {
        boolean controllersChanged = controllerManager.update();

        if (controllersChanged) {
            reconcileControllers();
        }

        update();

        Gdx.app.postRunnable(this);
    }

    private void reconcileControllers() {
        // Break old connections to help detect disconnected objects later.
        for (Tuple tuple : indexToController.values()) {
            JamepadController controller = tuple.controller;
            tuple.index = null;
            controller.setControllerIndex(null);
        }

        // Get already-connected controllers paired with their existing objects.
        // Create objects for new controllers, but don't send connect messages yet.
        connectedControllers.clear();
        int numControllers = JamepadControllerManager.jamepadConfiguration.maxNumControllers;
        for (int i = 0; i < numControllers; i++) try {
            ControllerIndex controllerIndex = controllerManager.getControllerIndex(i);
            try {
                int instanceID = controllerIndex.getDeviceInstanceID();
                if (indexToController.containsKey(instanceID)) {
                    // Pre-existing controller, pair with existing object.
                    Tuple tuple1 = indexToController.get(instanceID);
                    tuple1.index = controllerIndex;
                    tuple1.controller.setControllerIndex(controllerIndex);
                } else {
                    // New controller. Create new object, and store it for connect message later.
                    Tuple tuple1 = new Tuple(controllerIndex);
                    indexToController.put(instanceID, tuple1);
                    connectedControllers.add(tuple1.controller);
                }
            } catch (ControllerUnpluggedException e) {
                // controller not connected, no need to pair it
            }
        } catch (ArrayIndexOutOfBoundsException t) {
            // more controllers connected than we can handle according to our config
        }

        // Remove disconnected objects.
        IntMap.Values<Tuple> values = indexToController.values();
        while (values.hasNext()) {
            Tuple tuple = values.next();
            if (tuple.index == null) {
                tuple.controller.setDisconnected();
                values.remove();
            }
        }

        // Set up listeners for new controllers and send connect messages.
        for (JamepadController controller: connectedControllers) {
            controller.addListener(listener);
            listener.connected(controller);
        }
    }

    private void update() {
        IntMap.Values<Tuple> values = indexToController.values();
        while (values.hasNext()) {
            Tuple tuple = values.next();
            JamepadController controller = tuple.controller;
            boolean connected = controller.update();

            if (!connected) {
                values.remove();
            }
        }
    }

    private class Tuple {
        public ControllerIndex index;
        public final JamepadController controller;

        public Tuple(ControllerIndex index) {
            this.index = index;
            this.controller = new JamepadController(index);
        }
    }
}
