package de.golfgl.gdx.controllers.jamepad.support;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.IntMap;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;

public class JamepadControllerMonitor implements Runnable {
    private final ControllerManager controllerManager;
    private final ControllerListener listener;
    private final IntMap<Tuple> currentControllers = new IntMap<>();

    public JamepadControllerMonitor(ControllerManager controllerManager, ControllerListener listener) {
        this.controllerManager = controllerManager;
        this.listener = listener;
    }

    @Override
    public void run() {
        controllerManager.update();

        int newNumControllers = controllerManager.getNumControllers();
        for (int i = 0; i < newNumControllers; i++) {
            ControllerIndex controllerIndex = controllerManager.getControllerIndex(i);

            if (!currentControllers.containsKey(controllerIndex.getIndex())) {
                Tuple tuple = new Tuple(controllerIndex);
                tuple.controller.addListener(listener);

                currentControllers.put(controllerIndex.getIndex(), tuple);
                listener.connected(tuple.controller);
            }
        }

        for (Tuple tuple : currentControllers.values()) {
            tuple.controller.update();
        }

        Gdx.app.postRunnable(this);
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
