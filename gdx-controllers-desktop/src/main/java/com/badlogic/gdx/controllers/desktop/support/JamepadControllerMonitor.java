package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.controllers.desktop.JamepadControllerManager;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;

public class JamepadControllerMonitor implements Runnable {
    private final ControllerManager controllerManager;
    private final ControllerListener listener;
    private final IntMap<Tuple> indexToController = new IntMap<>();
    //temporary array for cleaning disconnected controllers
    private final IntArray disconnectedControllers = new IntArray();

    public JamepadControllerMonitor(ControllerManager controllerManager, ControllerListener listener) {
        this.controllerManager = controllerManager;
        this.listener = listener;
    }

    @Override
    public void run() {
        controllerManager.update();

        checkForNewControllers();
        update();

        Gdx.app.postRunnable(this);
    }

    private void checkForNewControllers() {
        int numControllers = JamepadControllerManager.jamepadConfiguration.maxNumControllers;
        for (int i = 0; i < numControllers; i++) try {
            ControllerIndex controllerIndex = controllerManager.getControllerIndex(i);

            if (!indexToController.containsKey(controllerIndex.getIndex()) && controllerIndex.isConnected()) {
                Tuple tuple1 = new Tuple(controllerIndex);
                tuple1.controller.addListener(listener);

                indexToController.put(controllerIndex.getIndex(), tuple1);
                listener.connected(tuple1.controller);
            }
        } catch (ArrayIndexOutOfBoundsException t) {
            // more controllers connected than we can handle according to our config
        }
    }

    private void update() {
        disconnectedControllers.clear();
        for (Tuple tuple : indexToController.values()) {
            JamepadController controller = tuple.controller;
            boolean connected = controller.update();

            if (!connected) {
                disconnectedControllers.add(tuple.index.getIndex());
            }
        }

        for (int i = 0; i < disconnectedControllers.size; i++) {
            indexToController.remove(disconnectedControllers.get(i));
        }
    }

    private class Tuple {
        public final ControllerIndex index;
        public final JamepadController controller;

        public Tuple(ControllerIndex index) {
            this.index = index;
            this.controller = new JamepadController(index);
        }
    }
}
